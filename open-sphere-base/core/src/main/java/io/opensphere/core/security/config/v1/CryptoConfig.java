package io.opensphere.core.security.config.v1;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.security.Digest;
import io.opensphere.core.util.security.WrappedSecretKey;

/**
 * Storage for a crypto configuration. This will either contain a {@link Digest}
 * for password validation with a salt for secret key generation, or a
 * {@link WrappedSecretKey} that can be decrypted with a private key to get a
 * secret key.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CryptoConfig
{
    /** The password digest. */
    @XmlElement(name = "Digest")
    private Digest myDigest;

    /** The salt for the secret key generated from the password. */
    @XmlElement(name = "Salt")
    private byte[] mySalt;

    /** The wrapped secret key. */
    @XmlElement(name = "WrappedSecretKey")
    private WrappedSecretKey myWrappedSecretKey;

    /**
     * Constructor that takes a password digest.
     *
     * @param digest The digest of the password.
     * @param salt The salt of the secret key.
     */
    public CryptoConfig(Digest digest, byte[] salt)
    {
        myDigest = digest;
        mySalt = salt.clone();
    }

    /**
     * Constructor that takes a wrapped secret key.
     *
     * @param wrappedSecretKey The wrapped secret key.
     */
    public CryptoConfig(WrappedSecretKey wrappedSecretKey)
    {
        myWrappedSecretKey = wrappedSecretKey;
    }

    /** Default constructor. */
    protected CryptoConfig()
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
        CryptoConfig other = (CryptoConfig)obj;
        return EqualsHelper.equals(myDigest, other.myDigest, myWrappedSecretKey, other.myWrappedSecretKey)
                && Arrays.equals(mySalt, other.mySalt);
    }

    /**
     * Get the password digest.
     *
     * @return The digest.
     */
    public Digest getDigest()
    {
        return myDigest;
    }

    /**
     * Get the salt.
     *
     * @return The salt.
     */
    public byte[] getSalt()
    {
        return mySalt.clone();
    }

    /**
     * Get the wrapped secret key.
     *
     * @return The wrapped secret key.
     */
    public WrappedSecretKey getWrappedSecretKey()
    {
        return myWrappedSecretKey;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myDigest == null ? 0 : myDigest.hashCode());
        result = prime * result + Arrays.hashCode(mySalt);
        result = prime * result + (myWrappedSecretKey == null ? 0 : myWrappedSecretKey.hashCode());
        return result;
    }
}
