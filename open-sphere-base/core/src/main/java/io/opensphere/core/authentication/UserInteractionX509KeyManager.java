package io.opensphere.core.authentication;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.net.Socket;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import javax.security.auth.x500.X500Principal;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.security.CertificateSelectionPanel;
import io.opensphere.core.util.PausingTimeBudget;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.security.DecryptionException;
import io.opensphere.core.util.security.KeyUsage;
import io.opensphere.core.util.security.PrivateKeyProvider;
import io.opensphere.core.util.security.PrivateKeyProviderException;
import io.opensphere.core.util.security.PrivateKeyProviderFilter;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * An {@link X509KeyManager} that will prompt the user for the certificate to be
 * used.
 */
@SuppressWarnings("PMD.GodClass")
public class UserInteractionX509KeyManager extends X509ExtendedKeyManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(UserInteractionX509KeyManager.class);

    /**
     * The system's default key manager to use when the user did not select a
     * certificate.
     */
    private X509KeyManager myDefaultKeyManager;

    /**
     * The system's default extended key manager to use when the user did not
     * select a certificate.
     */
    private X509ExtendedKeyManager myDefaultExtendedKeyManager;

    /** The preferences registry. */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * Holder for the selected private key provider, once it has been selected.
     */
    private volatile PrivateKeyProvider mySelectedProvider;

    /**
     * Indicates if this server uses the default key manager.
     */
    private boolean myUsesDefault;

    /**
     * A delegate instance of the {@link UserInteractionAuthenticator} in which
     * authentication is performed.
     */
    private final UserInteractionAuthenticator myAuthenticatorDelegate;

    /**
     * Construct the key manager.
     *
     * @param serverName The server name to present to the user in dialog text.
     * @param serverKey The unique key to use for the server in the preferences.
     * @param parentProvider The parent component provider.
     * @param preferencesRegistry System preferences.
     * @param securityManager The system security manager used to load/store
     *            user-selected certificates.
     * @param timeBudget Optional pausing time budget which will be paused while
     *            waiting for user interaction.
     */
    public UserInteractionX509KeyManager(String serverName, String serverKey, Supplier<? extends Component> parentProvider,
            PreferencesRegistry preferencesRegistry, SecurityManager securityManager, PausingTimeBudget timeBudget)
    {
        myAuthenticatorDelegate = new UserInteractionAuthenticator(serverName, serverKey, parentProvider, securityManager,
                timeBudget);
        myPreferencesRegistry = preferencesRegistry;
        loadDefaultKeyManager();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: This implementation is used for SSL over standard / basic Java IO
     * (e.g.: {@link java.net.Socket}). If using Java NIO
     * {@link java.nio.channels.SocketChannel}, the
     * {@link #chooseEngineClientAlias(String[], Principal[], SSLEngine)} will
     * be called instead.
     * </p>
     *
     * @see javax.net.ssl.X509KeyManager#chooseClientAlias(java.lang.String[],
     *      java.security.Principal[], java.net.Socket)
     */
    @Override
    public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket)
    {
        String alias = null;
        if (!myUsesDefault)
        {
            final PrivateKeyProvider selectedProvider = getSelectedProvider(New.set(Arrays.asList(keyTypes)), issuers);

            if (selectedProvider != null)
            {
                alias = selectedProvider.getAlias();
            }
            else if (myDefaultKeyManager != null)
            {
                myUsesDefault = true;
            }
        }

        if (myUsesDefault)
        {
            alias = myDefaultKeyManager.chooseClientAlias(keyTypes, issuers, socket);
        }

        return alias;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: This implementation is used for SSL over Java NIO
     * {@link java.nio.channels.SocketChannel} connections. If using SSL over
     * standard / basic Java IO (e.g.: {@link java.net.Socket}), the
     * {@link #chooseClientAlias(String[], Principal[], Socket)} method will be
     * called instead.
     * </p>
     *
     * @see javax.net.ssl.X509ExtendedKeyManager#chooseEngineClientAlias(java.lang.String[],
     *      java.security.Principal[], javax.net.ssl.SSLEngine)
     */
    @Override
    public String chooseEngineClientAlias(String[] keyTypes, Principal[] issuers, SSLEngine engine)
    {
        String alias = null;
        if (!myUsesDefault)
        {
            final PrivateKeyProvider selectedProvider = getSelectedProvider(New.set(Arrays.asList(keyTypes)), issuers);

            if (selectedProvider != null)
            {
                alias = selectedProvider.getAlias();
            }
            else if (myDefaultKeyManager != null)
            {
                myUsesDefault = true;
            }
        }

        if (myUsesDefault)
        {
            alias = myDefaultExtendedKeyManager.chooseEngineClientAlias(keyTypes, issuers, engine);
        }

        return alias;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.X509ExtendedKeyManager#chooseEngineServerAlias(java.lang.String,
     *      java.security.Principal[], javax.net.ssl.SSLEngine)
     */
    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Method called when authentication with the server fails.
     */
    public void failedAuthentication()
    {
        synchronized (UserInteractionX509KeyManager.class)
        {
            mySelectedProvider = null;
            for (final String purpose : new String[] { SecurityManager.GENERAL_AUTHENTICATION_PURPOSE,
                myAuthenticatorDelegate.getServerKey(), })
            {
                myAuthenticatorDelegate.getSecurityManager().setPreselectedPrivateKeyProvider(purpose, null);
            }
        }
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias)
    {
        try
        {
            if (!myUsesDefault)
            {
                PrivateKeyProvider selectedProvider = mySelectedProvider;
                if (selectedProvider == null || !selectedProvider.getAlias().equals(alias))
                {
                    selectedProvider = getProviderWithAlias(alias);
                    if (selectedProvider == null)
                    {
                        if (myDefaultKeyManager != null)
                        {
                            myUsesDefault = true;
                            return myDefaultKeyManager.getCertificateChain(alias);
                        }

                        return null;
                    }
                }
                final List<? extends X509Certificate> chain = selectedProvider.getCertificateChain();
                return chain == null ? null : chain.toArray(new X509Certificate[chain.size()]);
            }
            else
            {
                return myDefaultKeyManager.getCertificateChain(alias);
            }
        }
        catch (final PrivateKeyProviderException e)
        {
            LOGGER.error(e, e);
            return null;
        }
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers)
    {
        String[] aliases = null;
        if (!myUsesDefault)
        {
            final PrivateKeyProvider selectedProvider = getSelectedProvider(Collections.singleton(keyType), issuers);
            if (selectedProvider != null)
            {
                aliases = new String[] { selectedProvider.getAlias() };
            }
            else if (myDefaultKeyManager != null)
            {
                myUsesDefault = true;
            }
        }

        if (myUsesDefault)
        {
            aliases = myDefaultKeyManager.getClientAliases(keyType, issuers);
        }

        return aliases;
    }

    @Override
    public PrivateKey getPrivateKey(String alias)
    {
        final boolean timeBudgetPaused = myAuthenticatorDelegate.getTimeBudget() != null
                && myAuthenticatorDelegate.getTimeBudget().pause();
        try
        {
            PrivateKeyProvider selectedProvider = mySelectedProvider;
            if (selectedProvider != null && selectedProvider.getAlias().equals(alias))
            {
                try
                {
                    return selectedProvider.getPrivateKey();
                }
                catch (final PrivateKeyProviderException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Error getting private key: " + e, e);
                    }
                    selectedProvider = null;
                    mySelectedProvider = null;

                    // If it's a decryption exception, the secret key may have
                    // changed, so try again.
                    if (e.getCause() instanceof DecryptionException)
                    {
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("Error getting private key: " + e, e);
                        }
                    }
                    else
                    {
                        LOGGER.error("Error getting private key: " + e, e);
                        showErrorDialog("Could not get private key.");
                        return null;
                    }
                }
            }
            selectedProvider = getProviderWithAlias(alias);
            if (selectedProvider == null)
            {
                if (myDefaultKeyManager != null)
                {
                    return myDefaultKeyManager.getPrivateKey(alias);
                }

                final String message = "Private key for alias " + alias + " could not be found.";
                LOGGER.error(message);
                showErrorDialog(message);
                return null;
            }
            else
            {
                try
                {
                    final PrivateKey privateKey = selectedProvider.getPrivateKey();
                    mySelectedProvider = selectedProvider;
                    return privateKey;
                }
                catch (final PrivateKeyProviderException e1)
                {
                    LOGGER.error("Error getting private key: " + e1, e1);
                    showErrorDialog("Error getting private key: " + e1.toString());
                    return null;
                }
            }
        }
        finally
        {
            if (timeBudgetPaused)
            {
                myAuthenticatorDelegate.getTimeBudget().unpause();
            }
        }
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Ask the user to select a key provider.
     *
     * @param keyTypes The acceptable key types.
     * @param x500issuers The acceptable certificate issuers.
     * @return The selected key provider.
     */
    private PrivateKeyProvider doQueryUserForProvider(final Set<String> keyTypes,
            final Collection<? extends X500Principal> x500issuers)
    {
        assert EventQueue.isDispatchThread();

        final PrivateKeyProvider preferred = myAuthenticatorDelegate.getSecurityManager()
                .getPreferredPrivateKeyProvider(myAuthenticatorDelegate.getServerKey());

        if (preferred != null && Boolean.getBoolean("opensphere.authentication.usePreferredKey"))
        {
            try
            {
                LOGGER.info("Using preferred key from certificate \"" + preferred.getAlias() + "\" (Serial# "
                        + preferred.getSerialNumber() + ") (SHA1: " + preferred.getFingerprint("SHA1") + ") for "
                        + myAuthenticatorDelegate.getServerKey() + ".");
            }
            catch (CertificateEncodingException | PrivateKeyProviderException | NoSuchAlgorithmException e)
            {
                LOGGER.error(e, e);
            }
            return preferred;
        }

        final JPanel panel = new JPanel(new BorderLayout());

        final JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(new JLabel(
                "<html>A PKI certificate is being requested by <h3>" + myAuthenticatorDelegate.getServerKey() + "</h3></html>"),
                BorderLayout.CENTER);
        panel.add(labelPanel, BorderLayout.NORTH);

        final PrivateKeyProviderFilter filter = new PrivateKeyProviderFilter(null, keyTypes, x500issuers, null, Boolean.TRUE,
                KeyUsage.DIGITAL_SIGNATURE);
        final CertificateSelectionPanel certSelector = new CertificateSelectionPanel(myAuthenticatorDelegate.getSecurityManager(),
                myPreferencesRegistry, filter, preferred, true);
        certSelector.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(certSelector, BorderLayout.CENTER);

        final JCheckBox useForAllCheckbox = new JCheckBox("Use this certificate by default for this session", true);
        useForAllCheckbox.setToolTipText(
                "This certificate will be sent to all compatible servers requesting authentication during this session.");
        final JPanel checkboxPanel = new JPanel(new BorderLayout());
        checkboxPanel.add(useForAllCheckbox, BorderLayout.EAST);
        panel.add(checkboxPanel, BorderLayout.SOUTH);

        PrivateKeyProvider selectedProvider = null;
        do
        {
            final int choice = JOptionPane.showOptionDialog(myAuthenticatorDelegate.getParent(), panel, "Authentication Required",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (choice == JOptionPane.OK_OPTION)
            {
                selectedProvider = certSelector.getSelectedItem();
                try
                {
                    if (selectedProvider == null)
                    {
                        JOptionPane.showMessageDialog(myAuthenticatorDelegate.getParent(), "No certificate selected.");
                    }
                    else if (!filter.isSatisfied(selectedProvider))
                    {
                        JOptionPane.showMessageDialog(myAuthenticatorDelegate.getParent(),
                                filter.getErrorMessage(selectedProvider));
                        selectedProvider = null;
                    }
                    else
                    {
                        myAuthenticatorDelegate.getSecurityManager()
                                .setPreselectedPrivateKeyProvider(myAuthenticatorDelegate.getServerKey(), selectedProvider);

                        String logString;
                        if (useForAllCheckbox.isSelected())
                        {
                            myAuthenticatorDelegate.getSecurityManager().setPreselectedPrivateKeyProvider(
                                    SecurityManager.GENERAL_AUTHENTICATION_PURPOSE, selectedProvider);
                            logString = "general use";
                        }
                        else
                        {
                            logString = myAuthenticatorDelegate.getServerName();
                        }
                        try
                        {
                            LOGGER.info("Certificate \"" + selectedProvider.getAlias() + "\" (Serial# "
                                    + selectedProvider.getSerialNumber() + ") (SHA1: " + selectedProvider.getFingerprint("SHA1")
                                    + ") selected for " + logString + ".");
                        }
                        catch (CertificateEncodingException | NoSuchAlgorithmException e)
                        {
                            LOGGER.warn(e, e);
                        }
                    }
                }
                catch (final PrivateKeyProviderException e)
                {
                    LOGGER.error(e, e);
                }
            }
            else
            {
                break;
            }
        }
        while (selectedProvider == null);

        return selectedProvider;
    }

    /**
     * Check the security manager for a private key provider with a certain
     * alias.
     *
     * @param alias The alias.
     * @return The private key provider or {@code null} if one was not found.
     */
    private PrivateKeyProvider getProviderWithAlias(String alias)
    {
        for (final PrivateKeyProvider privateKeyProvider : myAuthenticatorDelegate.getSecurityManager().getPrivateKeyProviders())
        {
            if (privateKeyProvider.getAlias().equals(alias))
            {
                return privateKeyProvider;
            }
        }
        return null;
    }

    /**
     * Get the user-selected private key provider.
     *
     * @param keyTypes Optional acceptable key types.
     * @param issuers Optional acceptable key issuers.
     *
     * @return The private key provider, or {@code null} if one was not
     *         selected.
     */
    private PrivateKeyProvider getSelectedProvider(final Set<String> keyTypes, Principal[] issuers)
    {
        final boolean timeBudgetPaused = myAuthenticatorDelegate.getTimeBudget() != null
                && myAuthenticatorDelegate.getTimeBudget().pause();
        try
        {
            synchronized (UserInteractionX509KeyManager.class)
            {
                final Collection<? extends X500Principal> x500issuers = getX500Principals(issuers);

                for (final String purpose : new String[] { SecurityManager.GENERAL_AUTHENTICATION_PURPOSE,
                    myAuthenticatorDelegate.getServerKey(), })
                {
                    if (mySelectedProvider == null)
                    {
                        mySelectedProvider = myAuthenticatorDelegate.getSecurityManager()
                                .getPreselectedPrivateKeyProvider(purpose);
                    }
                    try
                    {
                        if (mySelectedProvider != null)
                        {
                            if (mySelectedProvider.isAcceptable(keyTypes, x500issuers))
                            {
                                break;
                            }
                            else
                            {
                                mySelectedProvider = null;
                            }
                        }
                    }
                    catch (final PrivateKeyProviderException e1)
                    {
                        LOGGER.error(e1, e1);
                        mySelectedProvider = null;
                    }
                }

                if (mySelectedProvider == null)
                {
                    queryUserForProvider(keyTypes, x500issuers);
                }

                return mySelectedProvider;
            }
        }
        finally
        {
            if (timeBudgetPaused)
            {
                myAuthenticatorDelegate.getTimeBudget().unpause();
            }
        }
    }

    /**
     * Convert an array of {@link Principal}s to a {@link Collection} of
     * {@link X500Principal}s.
     *
     * @param principals The input principals.
     * @return The collection.
     */
    private Collection<? extends X500Principal> getX500Principals(Principal[] principals)
    {
        Collection<X500Principal> x500issuers;
        if (principals == null)
        {
            x500issuers = null;
        }
        else
        {
            x500issuers = New.collection();
            for (final Principal principal : principals)
            {
                if (principal instanceof X500Principal)
                {
                    x500issuers.add((X500Principal)principal);
                }
            }
        }
        return New.unmodifiableCollection(x500issuers);
    }

    /**
     * Loads the default key manager, used when a cert has not been selected for
     * a given ssl connection.
     */
    private void loadDefaultKeyManager()
    {
        KeyManagerFactory kmf;
        try
        {
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(null, null);

            for (final KeyManager km : kmf.getKeyManagers())
            {
                if (km instanceof X509KeyManager)
                {
                    myDefaultKeyManager = (X509KeyManager)km;
                    if (km instanceof X509ExtendedKeyManager)
                    {
                        myDefaultExtendedKeyManager = (X509ExtendedKeyManager)km;
                    }
                    break;
                }
            }
        }
        catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Ask the user to select a key provider.
     *
     * @param keyTypes The acceptable key types.
     * @param x500issuers The acceptable certificate issuers.
     */
    private void queryUserForProvider(final Set<String> keyTypes, final Collection<? extends X500Principal> x500issuers)
    {
        mySelectedProvider = EventQueueUtilities.happyOnEdt(() -> doQueryUserForProvider(keyTypes, x500issuers));
    }

    /**
     * Display an error message to the user.
     *
     * @param message The message.
     */
    private void showErrorDialog(final String message)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                JOptionPane.showMessageDialog(myAuthenticatorDelegate.getParent(), message, "SSL Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
