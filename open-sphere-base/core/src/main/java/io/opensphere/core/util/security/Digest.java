package io.opensphere.core.util.security;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.util.lang.StringUtilities;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

/**
 * Comprises a message digest along with the name of the algorithm used to
 * generate it.
 */
@ThreadSafe
@Immutable
@XmlAccessorType(XmlAccessType.NONE)
@SuppressFBWarnings("JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
public class Digest implements Cloneable, Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The algorithm used to generate the digest. */
    @XmlElement(name = "Algorithm")
    private String myAlgorithm;

    /** The digest itself. */
    @XmlElement(name = "MessageDigest")
    private byte[] myMessageDigest;

    /** The salt, which may be {@code null}. */
    @XmlElement(name = "Salt")
    private byte[] mySalt;

    /**
     * Constructor.
     *
     * @param algorithm The hash algorithm.
     * @param messageDigest The algorithm result.
     */
    public Digest(String algorithm, byte[] messageDigest)
    {
        this(algorithm, messageDigest, (byte[])null);
    }

    /**
     * Constructor.
     *
     * @param algorithm The hash algorithm.
     * @param messageDigest The algorithm result.
     * @param salt The salt for the digest.
     */
    public Digest(String algorithm, byte[] messageDigest, byte[] salt)
    {
        myAlgorithm = Utilities.checkNull(algorithm, "algorithm");
        myMessageDigest = Utilities.checkNull(messageDigest, "messageDigest").clone();
        mySalt = salt == null ? null : salt.clone();
    }

    /**
     * Construct the digest of a certificate.
     *
     * @param algorithm The algorithm to use to compute the digest, for
     *            {@link MessageDigest#getInstance(String)}.
     * @param certificate The certificate.
     * @throws CertificateEncodingException If the certificate cannot be
     *             encoded.
     * @throws NoSuchAlgorithmException If the digest algorithm is not
     *             supported.
     * @see MessageDigest#getInstance(String)
     */
    public Digest(String algorithm, Certificate certificate) throws CertificateEncodingException, NoSuchAlgorithmException
    {
        this(algorithm, MessageDigest.getInstance(algorithm).digest(certificate.getEncoded()));
    }

    /**
     * Default constructor for JAXB.
     */
    protected Digest()
    {
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
        Digest other = (Digest)obj;
        return myAlgorithm.equals(other.myAlgorithm) && Arrays.equals(myMessageDigest, other.myMessageDigest)
                && Arrays.equals(mySalt, other.mySalt);
    }

    /**
     * The algorithm used to generate this digest.
     *
     * @return The algorithm.
     */
    public String getAlgorithm()
    {
        return myAlgorithm;
    }

    /**
     * The digest value.
     *
     * @return The digest.
     */
    public byte[] getMessageDigest()
    {
        return myMessageDigest.clone();
    }

    /**
     * Get the salt used to generate this digest.
     *
     * @return The salt, or {@code null} if no salt was used.
     */
    public byte[] getSalt()
    {
        return Utilities.clone(mySalt);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myAlgorithm.hashCode();
        result = prime * result + Arrays.hashCode(myMessageDigest);
        result = prime * result + Arrays.hashCode(mySalt);
        return result;
    }

    /**
     * Get if my message digest matches another one.
     *
     * @param messageDigest The digest to test against.
     * @return {@code true} if the digests match.
     */
    public boolean matches(byte[] messageDigest)
    {
        return Arrays.equals(myMessageDigest, messageDigest);
    }

    @Override
    public String toString()
    {
        return getAlgorithm() + " fingerprint: "
                + (myMessageDigest == null ? "(null)" : StringUtilities.toHexString(myMessageDigest, ":"));
    }

    @Override
    public Digest clone()
    {
        try
        {
            return (Digest)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }
}
