package io.opensphere.core.util.security;

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.util.lang.ImpossibleException;
import io.opensphere.core.util.lang.StringUtilities;
import net.jcip.annotations.Immutable;

/**
 * An encrypted byte array that can be used in a JAXB object.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@Immutable
@SuppressFBWarnings("JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
public class EncryptedByteArray implements Cloneable, Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The encryption parameters. */
    @XmlElement(name = "EncryptionParameters")
    private EncryptionParameters myEncryptionParameters;

    /** The encrypted data. */
    @XmlElement(name = "EncryptedData")
    @SuppressWarnings("PMD.ImmutableField")
    private byte[] myEncryptedData = new byte[0];

    /**
     * Constructor that takes the unencrypted data and a cipher. Only the
     * encrypted data is stored.
     *
     * @param data The unencrypted data.
     * @param cipherFactory The cipher factory.
     * @throws CipherException If the data could not be encrypted.
     */
    public EncryptedByteArray(byte[] data, CipherFactory cipherFactory) throws CipherException
    {
        Cipher cipher = cipherFactory.initCipher(Cipher.ENCRYPT_MODE);
        try
        {
            myEncryptionParameters = new EncryptionParameters(cipher);
        }
        catch (IOException e)
        {
            throw new CipherException("Failed to get encryption parameters: " + e, e);
        }

        try
        {
            myEncryptedData = cipher.doFinal(data);
        }
        catch (BadPaddingException e)
        {
            // This can only happen for decryption.
            throw new ImpossibleException(e);
        }
        catch (IllegalBlockSizeException e)
        {
            throw new CipherException("Failed to encrypt data: " + e, e);
        }
    }

    /**
     * Default constructor for JAXB.
     */
    protected EncryptedByteArray()
    {
    }

    @Override
    public EncryptedByteArray clone()
    {
        try
        {
            return (EncryptedByteArray)super.clone();
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
        EncryptedByteArray other = (EncryptedByteArray)obj;
        return myEncryptionParameters.equals(other.myEncryptionParameters)
                && Arrays.equals(myEncryptedData, other.myEncryptedData);
    }

    /**
     * Decrypt my data and return it.
     *
     * @param cipherFactory A factory for creating the decryption cipher.
     * @return The data.
     * @throws CipherException If the cipher cannot be initialized using the
     *             provided factory.
     * @throws DecryptionException If the data cannot be decrypted.
     */
    public byte[] getDecryptedData(CipherFactory cipherFactory) throws CipherException, DecryptionException
    {
        try
        {
            Cipher cipher = myEncryptionParameters.getDecryptCipher(cipherFactory);
            return cipher.doFinal(myEncryptedData);
        }
        catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | IOException e)
        {
            throw new DecryptionException("Failed to decrypt data: " + e, e);
        }
    }

    /**
     * Get the encrypted data.
     *
     * @return The data.
     */
    public byte[] getEncryptedData()
    {
        return myEncryptedData.clone();
    }

    /**
     * Get the encryption parameters.
     *
     * @return The encryption parameters.
     */
    public EncryptionParameters getEncryptionParameters()
    {
        return myEncryptionParameters;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myEncryptionParameters.hashCode();
        result = prime * result + Arrays.hashCode(myEncryptedData);
        return result;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + " "
                + (myEncryptedData == null ? "(null)" : StringUtilities.toHexString(myEncryptedData, null));
    }
}
