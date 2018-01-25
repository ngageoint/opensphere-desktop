package io.opensphere.core.util.security;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * A private key provider that gets the key from a {@link KeyStore}.
 */
public class KeyStorePrivateKeyProvider extends AbstractPrivateKeyProvider
{
    /** The keystore. */
    private final KeyStore myKeyStore;

    /** The password for the keystore. */
    private final char[] myPassword;

    /**
     * Constructor.
     *
     * @param alias The alias for the private key.
     * @param keyStore The keystore containing the private key.
     * @param password The password for the keystore and the private key.
     * @param source The source of the key.
     */
    public KeyStorePrivateKeyProvider(String alias, KeyStore keyStore, char[] password, String source)
    {
        super(alias, source);
        myKeyStore = keyStore;
        myPassword = Utilities.clone(password);
    }

    @Override
    public List<? extends X509Certificate> getCertificateChain() throws PrivateKeyProviderException
    {
        Certificate[] certificateChain;
        try
        {
            certificateChain = getKeyStore().getCertificateChain(getAlias());
        }
        catch (KeyStoreException e)
        {
            throw new PrivateKeyProviderException("Failed to get certificate chain from keystore: " + e, e);
        }
        List<X509Certificate> x509CertificateChain;
        if (certificateChain instanceof X509Certificate[])
        {
            x509CertificateChain = Arrays.asList((X509Certificate[])certificateChain);
        }
        else
        {
            x509CertificateChain = New.<X509Certificate>list(certificateChain.length);
            for (Certificate cert : certificateChain)
            {
                if (cert instanceof X509Certificate)
                {
                    x509CertificateChain.add((X509Certificate)cert);
                }
            }
        }
        return x509CertificateChain;
    }

    @Override
    public PrivateKey getPrivateKey() throws PrivateKeyProviderException
    {
        try
        {
            return (PrivateKey)getKeyStore().getKey(getAlias(), myPassword);
        }
        catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e)
        {
            throw new PrivateKeyProviderException("Failed to get private key from keystore: " + e, e);
        }
    }

    /**
     * Accessor for the keystore.
     *
     * @return The keystore.
     */
    protected KeyStore getKeyStore()
    {
        return myKeyStore;
    }
}
