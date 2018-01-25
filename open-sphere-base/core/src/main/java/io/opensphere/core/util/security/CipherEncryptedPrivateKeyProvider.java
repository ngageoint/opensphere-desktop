package io.opensphere.core.util.security;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import io.opensphere.core.util.Utilities;

/**
 * A private key provider that stores the private key internally encoded and
 * encrypted.
 */
public class CipherEncryptedPrivateKeyProvider extends AbstractPrivateKeyProvider implements EncryptedPrivateKeyProvider
{
    /** The cipher factory. */
    private final CipherFactory myCipherFactory;

    /** The encrypted private key. */
    private final EncryptedPrivateKeyAndCertChain myEncryptedPrivateKey;

    /** If the private key may be persisted. */
    private final boolean myPersistable;

    /**
     * Construct the private key provider using an already-encrypted private
     * key.
     *
     * @param encryptedPrivateKey The encrypted private key and certificate
     *            chain.
     * @param cipherFactory The cipher factory which will provide the decryption
     *            cipher.
     * @param persistable If the private key may be persisted.
     */
    public CipherEncryptedPrivateKeyProvider(EncryptedPrivateKeyAndCertChain encryptedPrivateKey, CipherFactory cipherFactory,
            boolean persistable)
    {
        super(encryptedPrivateKey.getAlias(), "User");
        myEncryptedPrivateKey = Utilities.checkNull(encryptedPrivateKey, "encryptedPrivateKey");
        myCipherFactory = Utilities.checkNull(cipherFactory, "cipherFactory");
        myPersistable = persistable;
    }

    /**
     * Construct the private key provider. The private key will be encrypted
     * using the cipher from the cipher factory.
     *
     * @param alias The alias for the private key.
     * @param source The source of the private key, to be displayed to the user.
     * @param key The private key.
     * @param chain The certificate chain for the private key.
     * @param cipherFactory The cipher factory which will provide the encryption
     *            cipher.
     * @param persistable If the private key may be persisted.
     * @throws CertificateEncodingException If the certificate chain cannot be
     *             encoded.
     * @throws CipherException If the private key cannot be encrypted.
     */
    public CipherEncryptedPrivateKeyProvider(String alias, String source, PrivateKey key, List<? extends X509Certificate> chain,
            CipherFactory cipherFactory, boolean persistable) throws CertificateEncodingException, CipherException
    {
        super(alias, source);

        myEncryptedPrivateKey = new EncryptedPrivateKeyAndCertChain();
        myEncryptedPrivateKey.setAlias(alias);
        myEncryptedPrivateKey.setPrivateKey(key, cipherFactory);
        myEncryptedPrivateKey.setCertificateChain(chain);
        myCipherFactory = cipherFactory;
        myPersistable = persistable;
    }

    @Override
    public List<? extends X509Certificate> getCertificateChain() throws PrivateKeyProviderException
    {
        try
        {
            return myEncryptedPrivateKey.getCertificateChain();
        }
        catch (CertificateException e)
        {
            throw new PrivateKeyProviderException("Failed to load certificate chain: " + e, e);
        }
    }

    @Override
    public EncryptedPrivateKeyAndCertChain getEncryptedPrivateKey()
    {
        return myEncryptedPrivateKey;
    }

    @Override
    public PrivateKey getPrivateKey() throws PrivateKeyProviderException
    {
        try
        {
            return myEncryptedPrivateKey.getPrivateKey(myCipherFactory);
        }
        catch (CipherException | DecryptionException e)
        {
            throw new PrivateKeyProviderException("The private key could not be decrypted: " + e, e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new PrivateKeyProviderException("The private key could not be created: " + e, e);
        }
    }

    @Override
    public boolean isPeristable()
    {
        return myPersistable;
    }
}
