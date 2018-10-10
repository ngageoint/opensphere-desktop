package io.opensphere.core.security;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.io.ByteArrayInputStream;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import io.opensphere.core.CipherChangeListener;
import io.opensphere.core.SecurityManager;
import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.security.config.v1.CryptoConfig;
import io.opensphere.core.security.config.v1.SecurityConfiguration;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.security.CipherEncryptedPrivateKeyProvider;
import io.opensphere.core.util.security.CipherEncryptedUsernamePasswordProvider;
import io.opensphere.core.util.security.CipherFactory;
import io.opensphere.core.util.security.DefaultSecretKeyProvider;
import io.opensphere.core.util.security.Digest;
import io.opensphere.core.util.security.EncryptedPrivateKeyAndCertChain;
import io.opensphere.core.util.security.EncryptedPrivateKeyProvider;
import io.opensphere.core.util.security.EncryptedUsernamePassword;
import io.opensphere.core.util.security.EncryptedUsernamePasswordProvider;
import io.opensphere.core.util.security.KeyUsage;
import io.opensphere.core.util.security.PrivateKeyProvider;
import io.opensphere.core.util.security.PrivateKeyProviderException;
import io.opensphere.core.util.security.PrivateKeyProviderFilter;
import io.opensphere.core.util.security.SecretKeyProvider;
import io.opensphere.core.util.security.SecretKeyProviderException;
import io.opensphere.core.util.security.SecurityUtilities;
import io.opensphere.core.util.security.UsernamePasswordProvider;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.OptionDialog;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/** Implementation of security manager interface. */
@ThreadSafe
@SuppressWarnings("PMD.GodClass")
public class SecurityManagerImpl implements SecurityManager
{
    /** Preference key for the security config. */
    public static final String SECURITY_CONFIG_KEY = "securityConfig";

    /** The algorithm to use for encryption. */
    private static final String CRYPT_ALGORITHM = "AES";

    /** The default cipher transformation. */
    private static final String DEFAULT_TRANSFORMATION = System.getProperty("opensphere.security.defaultTransformation",
            "AES/CBC/PKCS5Padding");

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SecurityManagerImpl.class);

    /** Change support for cipher changes. */
    private final WeakChangeSupport<CipherChangeListener> myCipherChangeSupport = WeakChangeSupport.create();

    /** The cipher factory. */
    private final CipherFactory myCipherFactory;

    /** The component to use for dialog parents. */
    private final Component myDialogParent;

    /** Indicates if secret key prompting is disallowed. */
    private boolean myDisallowSecretKeyPrompting;

    /** The certificates packaged on the classpath. */
    @GuardedBy("this")
    private Collection<X509Certificate> myPackagedTrustStoreCerts;

    /** Listener for preference changes. */
    private final PreferenceChangeListener myPreferenceChangeListener = evt ->
    {
        synchronized (SecurityManagerImpl.this)
        {
            myUserTrustedCerts = null;

            final CryptoConfig cryptoConfig = getConfig().getCryptoConfig();
            if (cryptoConfig == null || cryptoConfig.getWrappedSecretKey() == null && cryptoConfig.getDigest() == null)
            {
                mySecretKeyCache = null;
            }
        }
        if (!Utilities.sameInstance(evt.getSource(), SecurityManagerImpl.this))
        {
            loadPrivateKeyProviders();
        }
    };

    /**
     * The system preferences used to store the trusted servers and
     * certificates.
     */
    private final Preferences myPreferences;

    /** The system preferences registry. */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * Map of purposes to certificate digests.
     */
    @GuardedBy("this")
    private final Map<String, Digest> myPreselectedCertMap = New.map();

    /** The private key providers. */
    @GuardedBy("myPrivateKeyProviders")
    private final List<PrivateKeyProvider> myPrivateKeyProviders;

    /** Cache for the secret key once it's retrieved. */
    @GuardedBy("this")
    private SecretKey mySecretKeyCache;

    /**
     * A cipher factory that is only valid for the current session. The secret
     * key is not persisted.
     */
    private final CipherFactory mySessionCipherFactory;

    /** The username/password providers. */
    @GuardedBy("this")
    private final Map<String, UsernamePasswordProvider> myUsernamePasswordProviderMap = New.map();

    /** The certificates from the user's configuration. */
    private Collection<X509Certificate> myUserTrustedCerts;

    /** Collection of certificates from the Windows-ROOT trust store. */
    private Collection<? extends X509Certificate> myWindowsTrustStoreCerts;

    /**
     * Get if the security manager is configured to use a master password to
     * construct its secret key.
     *
     * @param prefsRegistry The preferences registry.
     * @return {@code true} if a master password is in use.
     */
    public static boolean isMasterPasswordInUse(PreferencesRegistry prefsRegistry)
    {
        final CryptoConfig cryptoConfig = getConfig(getPreferences(prefsRegistry)).getCryptoConfig();
        return cryptoConfig != null && cryptoConfig.getDigest() != null;
    }

    /**
     * Get the security config from some preferences.
     *
     * @param preferences The preferences.
     * @return The security config.
     */
    private static SecurityConfiguration getConfig(Preferences preferences)
    {
        return preferences.getJAXBObject(SecurityConfiguration.class, SECURITY_CONFIG_KEY, new SecurityConfiguration());
    }

    /**
     * Get the security manager preferences.
     *
     * @param prefsRegistry The preferences registry.
     * @return The security manager preferences.
     */
    private static Preferences getPreferences(PreferencesRegistry prefsRegistry)
    {
        return prefsRegistry.getPreferences(SecurityManagerImpl.class);
    }

    /**
     * Constructor.
     *
     * @param prefsRegistry The preferences registry (used for persistent
     *            storage for master password).
     * @param dialogParent The main application frame (used for dialog
     *            placement).
     */
    public SecurityManagerImpl(PreferencesRegistry prefsRegistry, Window dialogParent)
    {
        myCipherFactory = new CipherFactory(() ->
        {
            synchronized (SecurityManagerImpl.this)
            {
                if (mySecretKeyCache == null)
                {
                    mySecretKeyCache = SecurityManagerImpl.this
                            .getSecretKey("If you do not enter a password, your encrypted data will be lost. Are you sure?");
                }
                return mySecretKeyCache;
            }
        }, DEFAULT_TRANSFORMATION);

        mySessionCipherFactory = new CipherFactory(new SecretKeyProvider()
        {
            /** The session key. */
            private SecretKey myKey;

            @Override
            public synchronized SecretKey getSecretKey() throws SecretKeyProviderException
            {
                if (myKey == null)
                {
                    try
                    {
                        myKey = KeyGenerator.getInstance(CRYPT_ALGORITHM).generateKey();
                    }
                    catch (final NoSuchAlgorithmException e)
                    {
                        throw new SecretKeyProviderException("Could not generate secret key: " + e, e);
                    }
                }
                return myKey;
            }
        }, DEFAULT_TRANSFORMATION);

        myPrivateKeyProviders = New.list();
        myPreferencesRegistry = prefsRegistry;
        myPreferences = getPreferences(prefsRegistry);
        myDialogParent = dialogParent;

        myPreferences.addPreferenceChangeListener(SECURITY_CONFIG_KEY, myPreferenceChangeListener);
        loadPrivateKeyProviders();
    }

    @Override
    public void addCipherChangeListener(CipherChangeListener listener)
    {
        myCipherChangeSupport.addListener(listener);
    }

    @Override
    public final synchronized boolean addPrivateKeyProvider(PrivateKeyProvider provider)
    {
        boolean added;
        synchronized (myPrivateKeyProviders)
        {
            added = myPrivateKeyProviders.add(provider);
        }

        if (added && provider instanceof EncryptedPrivateKeyProvider && ((EncryptedPrivateKeyProvider)provider).isPeristable())
        {
            final SecurityConfiguration config = getConfig().clone();
            config.addEncryptedPrivateKeyAndCertChain(((EncryptedPrivateKeyProvider)provider).getEncryptedPrivateKey());

            saveConfig(config);
        }

        return added;
    }

    @Override
    public synchronized void addTrustedServer(String server)
    {
        final SecurityConfiguration config = getConfig().clone();
        config.addTrustedServer(server);
        saveConfig(config);
    }

    @Override
    public synchronized void addTrustedServerCerts(Collection<? extends X509Certificate> certs)
    {
        final SecurityConfiguration config = getConfig().clone();

        for (final X509Certificate cert : certs)
        {
            byte[] encoded;
            try
            {
                encoded = cert.getEncoded();
                config.addTrustedServerCert(encoded);
            }
            catch (final CertificateEncodingException e)
            {
                LOGGER.error("Failed to encode certificate: " + e, e);
            }
        }
        saveConfig(config);
    }

    @Override
    public final synchronized void addUsernamePasswordProvider(UsernamePasswordProvider provider)
    {
        final UsernamePasswordProvider old = myUsernamePasswordProviderMap.put(provider.getPurpose(), provider);

        if (provider instanceof EncryptedUsernamePasswordProvider && ((EncryptedUsernamePasswordProvider)provider).isPersistable()
                && !provider.equals(old))
        {
            final SecurityConfiguration config = getConfig().clone();

            config.removeUsernamePassword(provider.getPurpose());
            config.addUsernamePassword(((EncryptedUsernamePasswordProvider)provider).getEncryptedUsernamePassword());

            saveConfig(config);
        }
    }

    @Override
    public synchronized void clearEncryptedData()
    {
        myUsernamePasswordProviderMap.clear();
        mySecretKeyCache = null;

        final SecurityConfiguration config = getConfig().clone();
        config.clearEncryptedData();
        config.setCryptoConfig(null);
        saveConfig(config);

        loadPrivateKeyProviders();
    }

    @Override
    public synchronized void clearPreselectedAndPreferredPrivateKeyProviders()
    {
        myPreselectedCertMap.clear();

        final SecurityConfiguration config = getConfig().clone();
        config.clearPreferredCertificateFingerprints();
        saveConfig(config);
    }

    @Override
    public synchronized void clearUsernamePasswordProvider(String purpose)
    {
        myUsernamePasswordProviderMap.remove(purpose);
        final SecurityConfiguration config = getConfig().clone();
        if (config.removeUsernamePassword(purpose))
        {
            saveConfig(config);
        }
    }

    @Override
    public final CipherFactory getCipherFactory()
    {
        return myCipherFactory;
    }

    @Override
    public PrivateKeyProvider getPreferredPrivateKeyProvider(String purpose)
    {
        final SecurityConfiguration config = getConfig();
        final String preferredFingerprint = config.getPreferredCertificateFingerprint(purpose);
        if (preferredFingerprint != null)
        {
            for (final PrivateKeyProvider pkp : getPrivateKeyProviders())
            {
                try
                {
                    if (preferredFingerprint.equals(pkp.getFingerprint("MD5")))
                    {
                        return pkp;
                    }
                }
                catch (CertificateEncodingException | NoSuchAlgorithmException e)
                {
                    LOGGER.error("Failed to encode certificate: " + e, e);
                }
                catch (final PrivateKeyProviderException e)
                {
                    LOGGER.error("Failed to retrieve certificate: " + e, e);
                }
            }
        }

        return null;
    }

    @Override
    public synchronized PrivateKeyProvider getPreselectedPrivateKeyProvider(String serverKey)
    {
        final Digest digest = myPreselectedCertMap.get(serverKey);
        if (digest != null)
        {
            synchronized (myPrivateKeyProviders)
            {
                for (final PrivateKeyProvider pkp : myPrivateKeyProviders)
                {
                    if (pkp.hasDigest(digest))
                    {
                        return pkp;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Collection<? extends PrivateKeyProvider> getPrivateKeyProviders()
    {
        synchronized (myPrivateKeyProviders)
        {
            return New.unmodifiableList(myPrivateKeyProviders);
        }
    }

    @Override
    public PrivateKeyProvider getPrivateKeyProviderWithDigest(Digest digest)
    {
        for (final PrivateKeyProvider pkp : getPrivateKeyProviders())
        {
            if (pkp.hasDigest(digest))
            {
                return pkp;
            }
        }

        return null;
    }

    @Override
    public final CipherFactory getSessionOnlyCipherFactory()
    {
        return mySessionCipherFactory;
    }

    @Override
    public Collection<X509Certificate> getTrustedServerCerts()
    {
        final Collection<X509Certificate> certs = New.collection();

        certs.addAll(getPackagedTrustStoreCerts());
        certs.addAll(getWindowsTrustStoreCerts());
        certs.addAll(getUserTrustedServerCerts());

        return certs;
    }

    @Override
    public Collection<? extends String> getTrustedServers()
    {
        return New.unmodifiableCollection(getConfig().getUserTrustedServers());
    }

    @Override
    public final synchronized UsernamePasswordProvider getUsernamePasswordProvider(String purpose)
    {
        UsernamePasswordProvider provider = myUsernamePasswordProviderMap.get(purpose);
        if (provider == null)
        {
            for (final EncryptedUsernamePassword encryptedUsernamePassword : getConfig().getUsernamePasswords())
            {
                if (encryptedUsernamePassword.getPurpose().equals(purpose))
                {
                    provider = new CipherEncryptedUsernamePasswordProvider(encryptedUsernamePassword, getCipherFactory(), true);
                    myUsernamePasswordProviderMap.put(purpose, provider);
                    break;
                }
            }
        }
        return provider;
    }

    @Override
    public synchronized Collection<? extends X509Certificate> getUserTrustedServerCerts()
    {
        if (myUserTrustedCerts == null)
        {
            final Collection<X509Certificate> certs = New.collection();

            final Collection<byte[]> encodedCerts = getConfig().getUserTrustedCerts();
            if (!encodedCerts.isEmpty())
            {
                CertificateFactory certFactory;
                try
                {
                    certFactory = CertificateFactory.getInstance("X.509");
                    for (final byte[] encodedCert : encodedCerts)
                    {
                        try
                        {
                            certs.add((X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(encodedCert)));
                        }
                        catch (final CertificateException e)
                        {
                            LOGGER.error("Failed to load certificate: " + e, e);
                        }
                    }
                }
                catch (final CertificateException e)
                {
                    LOGGER.error("Failed to get certificate factory: " + e, e);
                }
            }
            myUserTrustedCerts = certs;
        }
        return New.unmodifiableCollection(myUserTrustedCerts);
    }

    @Override
    public void removeCipherChangeListener(CipherChangeListener listener)
    {
        myCipherChangeSupport.removeListener(listener);
    }

    @Override
    public synchronized void resetSecretKey()
    {
        try
        {
            if (mySecretKeyCache == null && getConfig().getCryptoConfig() != null)
            {
                mySecretKeyCache = getSecretKey(null);
            }
        }
        catch (final SecretKeyProviderException e)
        {
            // User did not provide the current encryption data.
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e, e);
            }
            return;
        }

        SecretKey newSecretKey;
        try
        {
            newSecretKey = promptUserForNewSecretKey("Please select a new encryption secret.", false);
        }
        catch (final SecretKeyProviderException e)
        {
            // User cancel.
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e, e);
            }
            return;
        }

        final CipherFactory newCipherFactory = new CipherFactory(new DefaultSecretKeyProvider(newSecretKey),
                DEFAULT_TRANSFORMATION);

        myDisallowSecretKeyPrompting = true;
        try
        {
            final SecurityConfiguration config = getConfig().clone();

            resetUsernamePasswords(newCipherFactory, config);

            resetPrivateKeyProviders(newCipherFactory, config);

            saveConfig(config);

            loadPrivateKeyProviders();

            mySecretKeyCache = newSecretKey;
        }
        finally
        {
            myDisallowSecretKeyPrompting = false;
        }
    }

    @Override
    public synchronized void setPreselectedPrivateKeyProvider(String purpose, PrivateKeyProvider selectedProvider)
    {
        try
        {
            myPreselectedCertMap.put(purpose, selectedProvider == null ? null : selectedProvider.getDigest("MD5"));
        }
        catch (final GeneralSecurityException e)
        {
            LOGGER.error("Failed to generate MD5 digest: " + e, e);
        }

        final SecurityConfiguration config = getConfig().clone();
        try
        {
            config.setPreferredCertificateFingerprint(purpose,
                    selectedProvider == null ? null : selectedProvider.getFingerprint("MD5"));
            saveConfig(config);
        }
        catch (CertificateEncodingException | NoSuchAlgorithmException e)
        {
            LOGGER.error("Failed to encode certificate: " + e, e);
        }
        catch (final PrivateKeyProviderException e)
        {
            LOGGER.error("Failed to retrieve certificate: " + e, e);
        }
    }

    /**
     * Get if prompting the user for the secret key is disallowed.
     *
     * @return {@code true} if prompting is allowed.
     */
    protected synchronized boolean disallowSecretKeyPrompting()
    {
        return myDisallowSecretKeyPrompting;
    }

    /**
     * Get a secret key that can be used for crypto operations.
     *
     * <p>
     * If there is a saved password digest, the user will be prompted to enter
     * the matching password. A secret key will be generated using the entered
     * password.
     * </p>
     * <p>
     * If there is a saved secret key, the private keys in the system will be
     * searched for one that matches the digest saved with the saved secret key.
     * If one is found, the secret key will be decrypted using the private key.
     * </p>
     * <p>
     * If there is no saved password digest and no saved secret key, the user
     * will be prompted to configure the crypto system, and the resulting secret
     * key will be returned.
     * </p>
     *
     * @param cancelWarning Optional warning to display if the user cancels.
     *
     * @return The key.
     * @throws SecretKeyProviderException If the key cannot be created.
     * @see Cipher#init(int, java.security.Key)
     */
    protected SecretKey getSecretKey(String cancelWarning) throws SecretKeyProviderException
    {
        if (disallowSecretKeyPrompting())
        {
            throw new SecretKeyProviderException("Secret key prompting is disabled.");
        }

        final CryptoConfig cryptoConfig = getConfig().getCryptoConfig();

        SecretKey secretKey;

        // If there is a saved password digest, get the password from the user
        // and generate a secret key with it.
        if (cryptoConfig != null && cryptoConfig.getDigest() != null && cryptoConfig.getDigest().getSalt() != null)
        {
            do
            {
                final char[] password = promptUserForCryptoPassword(cryptoConfig.getDigest(), cancelWarning);
                if (password == null)
                {
                    clearEncryptedData();
                    throw new SecretKeyProviderException("Password not provided.");
                }
                try
                {
                    secretKey = SecurityUtilities.getAESSecretKey(password, cryptoConfig.getSalt());
                }
                finally
                {
                    Arrays.fill(password, '\0');
                }
            }
            while (secretKey == null);
        }

        // If there is a wrapped secret key, get a private key that can
        // unwrap it and return the unwrapped secret key.
        else if (cryptoConfig != null && cryptoConfig.getWrappedSecretKey() != null)
        {
            final Digest digest = cryptoConfig.getWrappedSecretKey().getDigest();
            final PrivateKeyProvider pkp = getPrivateKeyProviderWithDigest(digest);
            if (pkp == null)
            {
                secretKey = handleMissingEncryptionKey(digest);
            }
            else
            {
                try
                {
                    secretKey = cryptoConfig.getWrappedSecretKey().getSecretKey(pkp);
                }
                catch (GeneralSecurityException | IllegalArgumentException e)
                {
                    throw new SecretKeyProviderException("Failed to unwrap secret key: " + e, e);
                }
            }
        }
        else
        {
            // No password and no secret key, so ask the user to pick a crypto
            // scheme.
            secretKey = promptUserForNewSecretKey("<html>For better security, passwords, private keys, "
                    + "and other sensitive data will be encrypted using a \"secret\" that you provide.</html>", true);
        }
        return secretKey;
    }

    /**
     * Handle the user cancelling the dialog prompting for a secret key.
     *
     * @param privateKeyAvailable Flag indicating if an encipherment key is
     *            available.
     * @throws SecretKeyProviderException If the user confirms the cancel.
     */
    private void doCancelAction(boolean privateKeyAvailable) throws SecretKeyProviderException
    {
        int choice;
        String message;
        if (privateKeyAvailable)
        {
            message = "If you do not configure a private key or password, password saving and certificate import will not be possible. Are you sure?";
        }
        else
        {
            message = "If you do not configure a password, password saving and certificate import will not be possible. Are you sure?";
        }
        choice = JOptionPane.showOptionDialog(myDialogParent, message, "Confirm", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (choice == JOptionPane.YES_OPTION)
        {
            throw new SecretKeyProviderException("No secret key configured.");
        }
    }

    /**
     * Prompt the user for a secret key to use for encryption. This must be run
     * on the dispatch thread.
     *
     * @param promptMessage The prompt for the user.
     * @param warnOnCancel Indicates if the user should be warned if entering
     *            the secret key is cancelled.
     * @return The secret key.
     * @throws SecretKeyProviderException If the user cancelled.
     */
    private SecretKey doPromptUserForNewSecretKey(String promptMessage, boolean warnOnCancel) throws SecretKeyProviderException
    {
        assert EventQueue.isDispatchThread();
        SecretKey result = null;
        final PrivateKeyProviderFilter filter = getKeyProviderFilter();
        final boolean privateKeyAvailable = isPrivateKeyAvailable(filter);

        do
        {
            final CreateSecretKeyPanel createSecretKeyPanel = new CreateSecretKeyPanel(promptMessage, privateKeyAvailable, filter,
                    this, myPreferencesRegistry);
            final OptionDialog dialog = new OptionDialog(SwingUtilities.getWindowAncestor(myDialogParent), createSecretKeyPanel,
                    "Configure Security");
            dialog.buildAndShow();
            if (dialog.getSelection() == JOptionPane.OK_OPTION)
            {
                final SecurityConfiguration config = getConfig().clone();
                try
                {
                    final Pair<SecretKey, CryptoConfig> pair = createSecretKeyPanel
                            .generateSecretKeyAndCryptoConfig(CRYPT_ALGORITHM);
                    if (pair != null)
                    {
                        result = pair.getFirstObject();
                        config.setCryptoConfig(pair.getSecondObject());
                        saveConfig(config);
                    }
                }
                catch (final NoSuchAlgorithmException e)
                {
                    LOGGER.error(e, e);
                    break;
                }
            }
            else if (warnOnCancel)
            {
                doCancelAction(privateKeyAvailable);
            }
            else
            {
                throw new SecretKeyProviderException("No secret key configured.");
            }
        }
        while (result == null);

        return result;
    }

    /**
     * Get the security config.
     *
     * @return The config.
     */
    private SecurityConfiguration getConfig()
    {
        return getConfig(myPreferences);
    }

    /**
     * Gets the key provider filter.
     *
     * @return the key provider filter
     */
    private PrivateKeyProviderFilter getKeyProviderFilter()
    {
        final Set<? extends String> keyTypes = null;
        final Collection<? extends X500Principal> issuers = null;
        final Boolean encrypted = Boolean.FALSE;
        final Boolean current = Boolean.TRUE;
        final Digest digest = null;
        final PrivateKeyProviderFilter filter = new PrivateKeyProviderFilter(digest, keyTypes, issuers, encrypted, current,
                KeyUsage.KEY_AND_DATA_ENCIPHERMENT);
        return filter;
    }

    /**
     * Get the certificates from the trust stores on the classpath.
     *
     * @return The certificates.
     */
    private synchronized Collection<? extends X509Certificate> getPackagedTrustStoreCerts()
    {
        if (myPackagedTrustStoreCerts == null)
        {
            myPackagedTrustStoreCerts = KeyStoreUtilities.getPackagedTrustStoreCerts();
        }
        return myPackagedTrustStoreCerts;
    }

    /**
     * Get the certificates from the Windows-ROOT trust store.
     *
     * @return The certificates.
     */
    private synchronized Collection<? extends X509Certificate> getWindowsTrustStoreCerts()
    {
        if (myWindowsTrustStoreCerts == null)
        {
            myWindowsTrustStoreCerts = Collections.emptyList();
            final Collection<? extends X509Certificate> certs = KeyStoreUtilities.getWindowsTrustStoreCerts();
            if (!certs.isEmpty())
            {
                myWindowsTrustStoreCerts = certs;
            }
        }
        return myWindowsTrustStoreCerts;
    }

    /**
     * Prompt the user for what to do when the encryption key is missing.
     *
     * @param digest The expected digest.
     * @return The new secret key.
     * @throws SecretKeyProviderException If a new secret key was not generated.
     */
    private SecretKey handleMissingEncryptionKey(final Digest digest) throws SecretKeyProviderException
    {
        try
        {
            final Callable<Boolean> userPromptCallable = () ->
            {
                final int choice = JOptionPane.showOptionDialog(myDialogParent,
                        "<html>The private key used to encrypt local data is not available.<br/>Looking for "
                                + digest.toString()
                                + "<p/>Would you like to delete your encrypted data (including passwords and certificates) and start over?</html>",
                                "Cannot load private key", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
                return Boolean.valueOf(choice == JOptionPane.OK_OPTION);
            };
            if (EventQueueUtilities.callOnEdt(userPromptCallable).booleanValue())
            {
                clearEncryptedData();
                return getSecretKey(null);
            }
            throw new SecretKeyProviderException("Private key not available.");
        }
        catch (final ExecutionException e)
        {
            throw new SecretKeyProviderException(e.getMessage(), e);
        }
    }

    /**
     * Determine if a private key that satisfies a filter is available in the
     * system.
     *
     * @param filter The filter.
     * @return {@code true} if the private key is available.
     */
    private boolean isPrivateKeyAvailable(PrivateKeyProviderFilter filter)
    {
        boolean privateKeyAvailable = false;
        final Collection<? extends PrivateKeyProvider> privateKeyProviders = getPrivateKeyProviders();
        for (final PrivateKeyProvider pkp : privateKeyProviders)
        {
            try
            {
                if (filter.isSatisfied(pkp))
                {
                    privateKeyAvailable = true;
                    break;
                }
            }
            catch (final PrivateKeyProviderException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Private key provider is unavailable: " + e, e);
                }
            }
        }
        return privateKeyAvailable;
    }

    /**
     * Load the private key providers from the preferences and from the windows
     * key store (if available).
     */
    private void loadPrivateKeyProviders()
    {
        synchronized (myPrivateKeyProviders)
        {
            myPrivateKeyProviders.clear();

            myPrivateKeyProviders.addAll(KeyStoreUtilities.getWindowsKeyProviders());

            for (final EncryptedPrivateKeyAndCertChain key : getConfig().getEncryptedPrivateKeyAndCertChains())
            {
                myPrivateKeyProviders.add(new CipherEncryptedPrivateKeyProvider(key, getCipherFactory(), true));
            }
            Collections.sort(myPrivateKeyProviders, PrivateKeyProvider.ALIAS_COMPARATOR);
        }
    }

    /**
     * Notify listeners of a cipher change.
     */
    private void notifyCipherChangeListeners()
    {
        ThreadUtilities.runBackground(() ->
        {
            myCipherChangeSupport.notifyListeners(listener -> listener.cipherChanged());
        });
    }

    /**
     * Prompt the user for the crypto password.
     *
     * @param passwordDigest The password digest.
     * @param cancelWarning Optional message to display if the user cancels.
     * @return The entered password, or {@code null} if no password was entered.
     */
    private char[] promptUserForCryptoPassword(final Digest passwordDigest, final String cancelWarning)
    {
        return EventQueueUtilities.happyOnEdt(() ->
        {
            char[] password;
            do
            {
                password = EnterPasswordDialog.promptForPassword(myDialogParent, "Enter Master Password:", passwordDigest);
                if (password == null && (cancelWarning == null || JOptionPane.showOptionDialog(myDialogParent, cancelWarning,
                        "Missing password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null,
                        null) == JOptionPane.OK_OPTION))
                {
                    break;
                }
            }
            while (password == null);

            return password;
        });
    }

    /**
     * Prompt the user for a secret key to use for encryption.
     *
     * @param promptMessage The prompt for the user.
     * @param warnOnCancel Indicates if the user should be warned if entering
     *            the secret key is cancelled.
     * @return The secret key.
     * @throws SecretKeyProviderException If the user cancelled.
     */
    private SecretKey promptUserForNewSecretKey(final String promptMessage, final boolean warnOnCancel)
            throws SecretKeyProviderException
    {
        try
        {
            return EventQueueUtilities.callOnEdt(() -> doPromptUserForNewSecretKey(promptMessage, warnOnCancel));
        }
        catch (final ExecutionException e)
        {
            throw (SecretKeyProviderException)e.getCause();
        }
    }

    /**
     * Reset the private key providers to use a new cipher factory.
     *
     * @param newCipherFactory The new cipher factory.
     * @param config A clone of the current config.
     */
    private void resetPrivateKeyProviders(CipherFactory newCipherFactory, SecurityConfiguration config)
    {
        myPreselectedCertMap.clear();

        for (final Iterator<? extends EncryptedPrivateKeyAndCertChain> iter = config.getEncryptedPrivateKeyAndCertChains()
                .iterator(); iter.hasNext();)
        {
            final EncryptedPrivateKeyAndCertChain key = iter.next();

            PrivateKey privateKey;
            try
            {
                privateKey = key.getPrivateKey(getCipherFactory());
                try
                {
                    key.setPrivateKey(privateKey, newCipherFactory);
                }
                catch (final GeneralSecurityException e)
                {
                    LOGGER.error("Could not encrypt private key: " + e, e);
                    iter.remove();
                    continue;
                }
            }
            catch (final NoSuchAlgorithmException e)
            {
                LOGGER.error("Could not decrypt private key: " + e, e);
                iter.remove();
                continue;
            }
            catch (final GeneralSecurityException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Could not decrypt private key: " + e, e);
                }
                iter.remove();
                continue;
            }
        }
    }

    /**
     * Reset the username/password providers to use a new cipher factory.
     *
     * @param newCipherFactory The new cipher factory.
     * @param config A clone of the current config.
     */
    private void resetUsernamePasswords(CipherFactory newCipherFactory, SecurityConfiguration config)
    {
        myUsernamePasswordProviderMap.clear();

        for (final Iterator<? extends EncryptedUsernamePassword> iter = config.getUsernamePasswords().iterator(); iter.hasNext();)
        {
            final EncryptedUsernamePassword encryptedUsernamePassword = iter.next();

            try
            {
                final char[] password = encryptedUsernamePassword.getPassword(getCipherFactory());
                try
                {
                    encryptedUsernamePassword.setPassword(password, newCipherFactory);
                }
                catch (final GeneralSecurityException e)
                {
                    LOGGER.error("Could not encrypt password: " + e, e);
                    continue;
                }
                finally
                {
                    Arrays.fill(password, '\0');
                }
            }
            catch (final GeneralSecurityException e)
            {
                LOGGER.info("Could not decrypt password: " + e, e);
                iter.remove();
            }
        }
    }

    /**
     * Save the security configuration.
     *
     * @param config The configuration.
     */
    private void saveConfig(SecurityConfiguration config)
    {
        final Object old = myPreferences.putJAXBObject(SECURITY_CONFIG_KEY, config, true, this);
        if (!(old instanceof SecurityConfiguration)
                || !EqualsHelper.equals(config.getCryptoConfig(), ((SecurityConfiguration)old).getCryptoConfig()))
        {
            notifyCipherChangeListeners();
        }
    }
}
