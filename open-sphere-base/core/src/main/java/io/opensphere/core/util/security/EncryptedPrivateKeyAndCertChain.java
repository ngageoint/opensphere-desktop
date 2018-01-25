package io.opensphere.core.util.security;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * A JAXB-able holder for a private key and certificate chain that keeps the
 * private key encrypted.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EncryptedPrivateKeyAndCertChain implements Cloneable
{
    /** The alias for the private key. */
    @XmlElement(name = "Alias")
    private String myAlias;

    /** The encoded chain of certificates associated with the private key. */
    @XmlElement(name = "CertificateChain")
    private byte[][] myCertificateChain;

    /** The encrypted, encoded private key. */
    @XmlElement(name = "EncryptedPrivateKey")
    private EncryptedByteArray myEncryptedPrivateKey;

    /** The key algorithm. */
    @XmlElement(name = "KeyAlgorithm")
    private String myKeyAlgorithm;

    @Override
    public EncryptedPrivateKeyAndCertChain clone()
    {
        try
        {
            return (EncryptedPrivateKeyAndCertChain)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    /**
     * Get the alias for this key.
     *
     * @return The alias.
     */
    public String getAlias()
    {
        return myAlias;
    }

    /**
     * Get the certificate chain.
     *
     * @return The certificate chain.
     * @throws CertificateException If a certificate cannot be decoded.
     */
    public List<? extends X509Certificate> getCertificateChain() throws CertificateException
    {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        List<X509Certificate> list = New.list(myCertificateChain.length);
        for (byte[] arr : myCertificateChain)
        {
            list.add((X509Certificate)factory.generateCertificate(new ByteArrayInputStream(arr)));
        }
        return list;
    }

    /**
     * Get the private key.
     *
     * @param cipherFactory A factory that may be used to create the decryption
     *            cipher.
     * @return The private key.
     * @throws CipherException If the cipher cannot be initialized.
     * @throws DecryptionException If the private key cannot be decrypted.
     * @throws NoSuchAlgorithmException If the algorithm for the private key is
     *             not available.
     */
    public PrivateKey getPrivateKey(CipherFactory cipherFactory)
        throws CipherException, DecryptionException, NoSuchAlgorithmException
    {
        byte[] encoded = myEncryptedPrivateKey.getDecryptedData(cipherFactory);
        try
        {
            KeyFactory factory = KeyFactory.getInstance(myKeyAlgorithm);
            KeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return factory.generatePrivate(keySpec);
        }
        catch (InvalidKeySpecException e)
        {
            throw new RuntimeSecurityException("Could not generate private key: " + e, e);
        }
        finally
        {
            Arrays.fill(encoded, (byte)0);
        }
    }

    /**
     * Get a {@link SimplePrivateKeyProvider} containing my private key.
     *
     * @param cipherFactory Factory that can create a cipher for decryption.
     *
     * @return The provider.
     * @throws CertificateException If the certificate(s) cannot be decoded.
     */
    public SimplePrivateKeyProvider getPrivateKeyProvider(final CipherFactory cipherFactory) throws CertificateException
    {
        return new SimplePrivateKeyProvider(myAlias, getCertificateChain(), "Preferences")
        {
            @Override
            public PrivateKey getPrivateKey() throws PrivateKeyProviderException
            {
                try
                {
                    return EncryptedPrivateKeyAndCertChain.this.getPrivateKey(cipherFactory);
                }
                catch (CipherException | DecryptionException | NoSuchAlgorithmException e)
                {
                    throw new PrivateKeyProviderException("Could not decrypt private key: " + e, e);
                }
            }
        };
    }

    /**
     * Set the alias associated with the key.
     *
     * @param alias The alias.
     */
    public void setAlias(String alias)
    {
        myAlias = alias;
    }

    /**
     * Set the certificate chain.
     *
     * @param chain The chain.
     * @throws CertificateEncodingException If there is an error encoding a
     *             certificate.
     */
    public void setCertificateChain(Collection<? extends X509Certificate> chain) throws CertificateEncodingException
    {
        myCertificateChain = new byte[chain.size()][];

        int index = 0;
        for (X509Certificate cert : chain)
        {
            myCertificateChain[index++] = cert.getEncoded();
        }
    }

    /**
     * Set the private key and encrypt it.
     *
     * @param pk The private key.
     * @param cipherFactory A factory for creating the encryption cipher.
     * @throws CipherException If the private key cannot be encrypted.
     */
    public void setPrivateKey(PrivateKey pk, CipherFactory cipherFactory) throws CipherException
    {
        myKeyAlgorithm = pk.getAlgorithm();
        myEncryptedPrivateKey = null;

        byte[] encoded = pk.getEncoded();
        try
        {
            myEncryptedPrivateKey = new EncryptedByteArray(encoded, cipherFactory);
        }
        finally
        {
            if (encoded != null)
            {
                Arrays.fill(encoded, (byte)0);
            }
        }
    }

    /**
     * Set my private key (encrypted) and other information to match the given
     * provider.
     *
     * @param in The input provider.
     * @param cipherFactory A factory for creating the encryption cipher.
     * @throws PrivateKeyProviderException If there is an error with the private
     *             key provider.
     * @throws CertificateEncodingException If a certificate cannot be encoded.
     * @throws CipherException If the private key cannot be encrypted.
     */
    public void setPrivateKeyProvider(PrivateKeyProvider in, CipherFactory cipherFactory)
        throws PrivateKeyProviderException, CertificateEncodingException, CipherException
    {
        setAlias(in.getAlias());
        setPrivateKey(in.getPrivateKey(), cipherFactory);
        setCertificateChain(in.getCertificateChain());
    }

    @Override
    public String toString()
    {
        return new StringBuilder(64).append(getClass().getSimpleName()).append(" alias: ").append(myAlias).toString();
    }

    /**
     * Check for basic validity. This does not check for a valid key or valid
     * certificates, but merely checks for null objects.
     *
     * @return {@code true} if this configuration is well-formed
     */
    public boolean validate()
    {
        return myAlias != null && myCertificateChain != null && myEncryptedPrivateKey != null && myKeyAlgorithm != null;
    }
}
