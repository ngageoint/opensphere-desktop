package io.opensphere.core.util.security;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.function.Predicate;

import javax.net.ssl.X509KeyManager;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;

/**
 * An {@link X509KeyManager} that can get a selected private key from the
 * Windows-MY keystore. This blindly uses the alias selector -- it ignores key
 * types and issuers.
 */
public final class SimpleWindowsKeyStoreX509KeyManager implements X509KeyManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SimpleWindowsKeyStoreX509KeyManager.class);

    /** The Windows-MY keystore. */
    private final KeyStore myKeyStore;

    /** The alias selector. */
    private final Predicate<String> myAliasSelector;

    /**
     * Constructor.
     *
     * @param aliasSelector A predicate used to select the client alias.
     * @throws GeneralSecurityException If the keystore cannot be initialized.
     * @throws IOException If the Windows-MY keystore cannot be loaded.
     */
    public SimpleWindowsKeyStoreX509KeyManager(Predicate<String> aliasSelector) throws GeneralSecurityException, IOException
    {
        myKeyStore = KeyStore.getInstance("Windows-MY");
        myKeyStore.load(null, null);
        myAliasSelector = aliasSelector;
    }

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket)
    {
        Enumeration<String> aliases;
        try
        {
            aliases = myKeyStore.aliases();
        }
        catch (KeyStoreException e)
        {
            LOGGER.warn("Failed to get aliases from keystore: " + e, e);
            return null;
        }
        String alias = Collections.list(aliases).stream().filter(myAliasSelector).findFirst().orElse(null);
        LOGGER.info("Using alias " + alias + " for " + socket.getInetAddress().getHostName());
        return alias;
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias)
    {
        try
        {
            return (X509Certificate[])myKeyStore.getCertificateChain(alias);
        }
        catch (KeyStoreException e)
        {
            LOGGER.warn("Failed to get certificate chain for alias " + alias + ": " + e, e);
            return null;
        }
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers)
    {
        try
        {
            return New.array(Collections.list(myKeyStore.aliases()), String.class);
        }
        catch (KeyStoreException e)
        {
            LOGGER.warn("Failed to get client aliases: " + e, e);
            return null;
        }
    }

    @Override
    public PrivateKey getPrivateKey(String alias)
    {
        try
        {
            return (PrivateKey)myKeyStore.getKey(alias, null);
        }
        catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e)
        {
            LOGGER.warn("Failed to get private key for alias " + alias + ": " + e, e);
            return null;
        }
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers)
    {
        throw new UnsupportedOperationException();
    }
}
