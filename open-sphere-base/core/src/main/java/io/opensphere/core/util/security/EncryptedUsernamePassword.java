package io.opensphere.core.util.security;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * Configuration for username/password.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class EncryptedUsernamePassword implements Cloneable
{
    /** The encrypted password. */
    @XmlElement(name = "EncryptedPassword")
    private EncryptedByteArray myEncryptedPassword;

    /** The purpose for the username/password. */
    @XmlElement(name = "Purpose")
    private String myPurpose = "";

    /** The username. */
    @XmlElement(name = "Username")
    private String myUsername = "";

    @Override
    public EncryptedUsernamePassword clone()
    {
        try
        {
            return (EncryptedUsernamePassword)super.clone();
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
        EncryptedUsernamePassword other = (EncryptedUsernamePassword)obj;
        return EqualsHelper.equals(myEncryptedPassword, other.myEncryptedPassword) && myPurpose.equals(other.myPurpose)
                && myUsername.equals(other.myUsername);
    }

    /**
     * Get the encrypted password.
     *
     * @return The password.
     */
    public byte[] getEncryptedPassword()
    {
        return myEncryptedPassword.getEncryptedData();
    }

    /**
     * Decrypt and return the password.
     *
     * @param cipherFactory A factory for creating the decryption cipher.
     * @return The password.
     * @throws DecryptionException If the password cannot be decrypted.
     * @throws CipherException If the cipher cannot be created.
     */
    public char[] getPassword(CipherFactory cipherFactory) throws CipherException, DecryptionException
    {
        byte[] arr = myEncryptedPassword.getDecryptedData(cipherFactory);
        try
        {
            char[] password = new char[arr.length / 2];
            for (int index = 0; index < arr.length; index += 2)
            {
                password[index / 2] = (char)(arr[index] << 8 | 0xFF & arr[index + 1]);
            }

            return password;
        }
        finally
        {
            Arrays.fill(arr, (byte)0);
        }
    }

    /**
     * Get the purpose for this username/password pair.
     *
     * @return The purpose.
     */
    public String getPurpose()
    {
        return myPurpose;
    }

    /**
     * Get the username.
     *
     * @return The username.
     */
    public String getUsername()
    {
        return myUsername;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myEncryptedPassword == null ? 0 : myEncryptedPassword.hashCode());
        result = prime * result + myPurpose.hashCode();
        result = prime * result + myUsername.hashCode();
        return result;
    }

    /**
     * Encrypt and set the password. The cipher must be already initialized.
     *
     * @param password The password to set.
     * @param cipherFactory Factory for generating the cipher for encrypting the
     *            password.
     * @throws CipherException If the password cannot be encrypted.
     */
    public void setPassword(char[] password, CipherFactory cipherFactory) throws CipherException
    {
        Utilities.checkNull(password, "password");

        byte[] arr = new byte[password.length * 2];
        try
        {
            for (int index = 0; index < password.length; ++index)
            {
                arr[index * 2] = (byte)(password[index] >> 8);
                arr[index * 2 + 1] = (byte)password[index];
            }

            myEncryptedPassword = new EncryptedByteArray(arr, cipherFactory);
        }
        finally
        {
            Arrays.fill(arr, (byte)0);
        }
    }

    /**
     * Set the purpose.
     *
     * @param purpose The purpose to set.
     */
    public void setPurpose(String purpose)
    {
        myPurpose = Utilities.checkNull(purpose, "purpose");
    }

    /**
     * Set the username.
     *
     * @param username The username to set.
     */
    public void setUsername(String username)
    {
        myUsername = Utilities.checkNull(username, "username");
    }
}
