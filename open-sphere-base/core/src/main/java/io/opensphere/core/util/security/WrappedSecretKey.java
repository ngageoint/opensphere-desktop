package io.opensphere.core.util.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;

import net.jcip.annotations.Immutable;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * A {@link SecretKey} that is <i>wrapped</i> using
 * {@link Cipher#wrap(java.security.Key)}, along with the cipher algorithm
 * associated with the secret key, and the digest of the certificate associated
 * with the private key used to wrap the secret key.
 */
@XmlRootElement
@Immutable
@SuppressFBWarnings("JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
public class WrappedSecretKey implements Cloneable
{
    /** The algorithm used to generate the certificate digests. */
    private static final String DIGEST_ALGORITHM = "MD5";

    /**
     * The digest of the certificate associated with the private key used to
     * wrap the secret key.
     */
    @XmlElement(name = "Digest")
    @SuppressWarnings("PMD.ImmutableField")
    private byte[] myDigest = new byte[0];

    /** The algorithm associated with the secret key. */
    @XmlElement(name = "Algorithm")
    @SuppressWarnings("PMD.ImmutableField")
    private String mySecretKeyAlgorithm = "";

    /** The wrapped secret key. */
    @XmlElement(name = "WrappedSecretKeyBytes")
    @SuppressWarnings("PMD.ImmutableField")
    private byte[] myWrappedSecretKeyBytes = new byte[0];

    /**
     * Constructor that takes a secret key and a private key provider.The secret
     * key will be wrapped using the private key from the provider.
     *
     * @param secretKey The secret key.
     * @param privateKeyProvider The provider for the private key used to wrap
     *            the secret key.
     * @throws CipherException If the cipher cannot be initialized.
     * @throws PrivateKeyProviderException If the key or digest cannot be
     *             retrieved from the private key provider.
     * @throws CertificateEncodingException If the certificate cannot be
     *             encoded.
     */
    public WrappedSecretKey(SecretKey secretKey, PrivateKeyProvider privateKeyProvider)
        throws CipherException, PrivateKeyProviderException, CertificateEncodingException
    {
        try
        {
            myDigest = privateKeyProvider.getDigest(DIGEST_ALGORITHM).getMessageDigest();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeSecurityException("Failed to load digest algorithm: " + e, e);
        }

        PrivateKey privateKey = privateKeyProvider.getPrivateKey();
        try
        {
            Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
            cipher.init(Cipher.WRAP_MODE, privateKey);
            myWrappedSecretKeyBytes = cipher.wrap(secretKey);
            mySecretKeyAlgorithm = secretKey.getAlgorithm();
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            throw new CipherException("Failed to get cipher for private key: " + e, e);
        }
        catch (InvalidKeyException e)
        {
            throw new CipherException("Failed to initialize cipher: " + e, e);
        }
        catch (IllegalBlockSizeException e)
        {
            throw new CipherException("Cipher cannot be used to wrap secret key: " + e, e);
        }
    }

    /**
     * Default constructor for JAXB.
     */
    protected WrappedSecretKey()
    {
    }

    @Override
    public WrappedSecretKey clone()
    {
        try
        {
            return (WrappedSecretKey)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        WrappedSecretKey other = (WrappedSecretKey)obj;
        return Arrays.equals(myDigest, other.myDigest) && mySecretKeyAlgorithm.equals(other.mySecretKeyAlgorithm)
                && Arrays.equals(myWrappedSecretKeyBytes, other.myWrappedSecretKeyBytes);
    }

    /**
     * Get the digest of the certificate associated with the private key used to
     * wrap the secret key.
     *
     * @return The digest.
     */
    public Digest getDigest()
    {
        return new Digest(DIGEST_ALGORITHM, myDigest);
    }

    /**
     * Get the secret key. This will unwrap the internal representation using
     * the private key from the provided {@link PrivateKeyProvider}.
     *
     * @param privateKeyProvider The provider for the private key used to unwrap
     *            the secret key.
     *
     * @return The secret key.
     * @throws NoSuchAlgorithmException If the algorithm for the secret key is
     *             unsupported.
     * @throws IllegalArgumentException If the private key provider's digest
     *             does not match the digest of the private key used to wrap my
     *             secret key.
     * @throws CipherException If the secret key cannot be unwrapped.
     * @throws PrivateKeyProviderException If the private key cannot be
     *             retrieved.
     */
    public SecretKey getSecretKey(PrivateKeyProvider privateKeyProvider)
        throws NoSuchAlgorithmException, IllegalArgumentException, CipherException, PrivateKeyProviderException
    {
        if (!privateKeyProvider.hasDigest(getDigest()))
        {
            throw new IllegalArgumentException("The private key provider's digest does not match my digest.");
        }

        PrivateKey privateKey = privateKeyProvider.getPrivateKey();
        try
        {
            Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
            cipher.init(Cipher.UNWRAP_MODE, privateKey);
            return (SecretKey)cipher.unwrap(myWrappedSecretKeyBytes, mySecretKeyAlgorithm, Cipher.SECRET_KEY);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            throw new CipherException("Failed to get cipher for private key: " + e, e);
        }
        catch (InvalidKeyException e)
        {
            throw new CipherException("Failed to initialize cipher: " + e, e);
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(myDigest);
        result = prime * result + mySecretKeyAlgorithm.hashCode();
        result = prime * result + Arrays.hashCode(myWrappedSecretKeyBytes);
        return result;
    }
}
