package io.opensphere.core.common.connection;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.codec.binary.Base64;

/**
 * This class encapsulates a password and the ability to encrypt/decrypt it.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProtectedPassword implements Cloneable
{
    /**
     * The persistent encrypted password.
     */
    @XmlElement(name = "EncryptedPassword")
    private String encryptedPassword;

    /**
     * The transient unencrypted password.
     */
    @XmlTransient
    private char[] password;

    /**
     * Returns the encrypted password in Base64 encoding.
     *
     * @return the encrypted password.
     */
    public String getEncryptedPassword()
    {
        return encryptedPassword;
    }

    /**
     * Sets the encrypted password in Base64 encoding.
     *
     * @param encryptedPassword the encrypted password.
     */
    public void setEncryptedPassword(String encryptedPassword)
    {
        this.encryptedPassword = encryptedPassword;
        password = null;
    }

    /**
     * Returns the decrypted password using the provided password. For stronger
     * security, it is recommended that the returned character array be cleared
     * after use by setting each character to zero.
     *
     * @return the decrypted password or an empty array.
     */
    public char[] getPassword()
    {
        char[] charPassword = null;

        // Make a copy of the password and return it.
        if (password != null)
        {
            charPassword = new char[password.length];
            System.arraycopy(password, 0, charPassword, 0, password.length);
        }
        else
        {
            charPassword = new char[0];
        }
        return charPassword;
    }

    /**
     * Sets the unencrypted password then encrypts it using
     * <code>encryptionPassword</code>. <br/>
     * <br/>
     * NOTE: the <code>password</code> array will be cleared before this method
     * returns.
     *
     * @param password the unencrypted password.
     */
    public void setPassword(char[] password)
    {
        try
        {
            // Store a copy of the password.
            encryptedPassword = null;
            this.password = new char[password.length];
            System.arraycopy(password, 0, this.password, 0, password.length);
        }
        finally
        {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Indicates if the password is encrypted.
     *
     * @return <code>true</code> if the password has already been encrypted.
     */
    public boolean isPasswordEncrypted()
    {
        return encryptedPassword != null;
    }

    /**
     * Indicates if the password is decrypted.
     *
     * @return <code>true</code> if the password has already been decrypted.
     */
    public boolean isPasswordDecrypted()
    {
        return password != null;
    }

    /**
     * Decrypts the password. The results of which can be fetched by calling
     * {@link #getPassword()}.<br/>
     * <br/>
     * NOTE: the <code>decryptionPassword</code> array will be cleared before
     * this method returns.
     *
     * @param decryptionPassword the password to use in the decryption process.
     * @throws IllegalStateException if either the encrypted password or
     *             decryption passwords are <code>null</code>.
     * @throws GeneralSecurityException if an  error occurs while decrypting the
     *             password.
     */
    public void decryptPassword(char[] decryptionPassword) throws GeneralSecurityException
    {
        password = decryptPassword(encryptedPassword, decryptionPassword);
    }

    /**
     * Encrypts the password. The results of which can be fetched by calling
     * {@link #getEncryptedPassword()}.<br/>
     * <br/>
     * NOTE: the <code>encryptionPassword</code> array will be cleared before
     * this method returns.
     *
     * @param encryptionPassword the password to use during encryption.
     * @throws GeneralSecurityException if an  error occurs while decrypting the
     *             password.
     * @throws IllegalStateException if either the encrypted password or
     *             decryption passwords are <code>null</code>.
     */
    public void encryptPassword(char[] encryptionPassword) throws GeneralSecurityException
    {
        encryptedPassword = encryptPassword(password, encryptionPassword);
        Arrays.fill(password, '\0');
        password = null;
    }

    /**
     * Changes the encryption password for the protected password.<br/>
     * <br/>
     * NOTE: the <code>decryptionPassword</code> and
     * <code>encryptionPassword</code> arrays will be cleared before this method
     * returns.
     *
     * @param decryptionPassword the password used to encrypt the protected
     *            password.
     * @param encryptionPassword the password to use during encryption.
     * @throws GeneralSecurityException if an  error occurs while decrypting or
     *             encrypting the password.
     * @throws IllegalStateException if any of the encrypted, decryption or
     *             encryption passwords are <code>null</code>.
     */
    public void changeEncryptionPassword(char[] decryptionPassword, char[] encryptionPassword) throws GeneralSecurityException
    {
        try
        {
            if (encryptionPassword == null)
            {
                throw new IllegalStateException("The encryption password is null");
            }

            // Decrypt the password if it hasn't already.
            if (!isPasswordDecrypted())
            {
                if (decryptionPassword == null)
                {
                    throw new IllegalStateException("The decryption password is null");
                }
                decryptPassword(decryptionPassword);
            }
            encryptPassword(encryptionPassword);
        }
        finally
        {
            // Clear the encryption and decryption passwords.
            if (decryptionPassword != null)
            {
                Arrays.fill(decryptionPassword, '\0');
            }
            if (encryptionPassword != null)
            {
                Arrays.fill(encryptionPassword, '\0');
            }
        }
    }

    /**
     * Decrypts the password. The results of which can be fetched by calling
     * {@link #getPassword()}.<br/>
     * <br/>
     * NOTE: the <code>decryptionPassword</code> array will be cleared before
     * this method returns.
     *
     * @param encryptedPassword The password to decrypt
     * @param decryptionPassword the password to use in the decryption process.
     * @return The decrypted password
     * @throws IllegalStateException if either the encrypted password or
     *             decryption passwords are <code>null</code>.
     * @throws GeneralSecurityException if an  error occurs while decrypting the
     *             password.
     */
    public static char[] decryptPassword(String encryptedPassword, char[] decryptionPassword) throws GeneralSecurityException
    {
        char[] password = null;

        byte[] bytePassword = null;
        try
        {
            if (decryptionPassword == null)
            {
                throw new IllegalStateException("The decryption password is null");
            }
            if (encryptedPassword == null)
            {
                throw new IllegalStateException("The encrypted password is null");
            }

            // Generate the secret key using the decryption password.
            SecretKeySpec secretkeySpec = getSecretKeySpec(decryptionPassword);

            // Create a cipher based upon AES.
            Cipher cipher = Cipher.getInstance(secretkeySpec.getAlgorithm());

            // Initialize the cipher with secret key.
            cipher.init(Cipher.DECRYPT_MODE, secretkeySpec);

            // Base64 decode the encrypted password.
            byte[] encryptedPasswordArray = new Base64().decode(encryptedPassword.getBytes());

            // Decrypt the password.
            bytePassword = cipher.doFinal(encryptedPasswordArray);
            password = new char[bytePassword.length];
            for (int index = 0; index < bytePassword.length; index++)
            {
                password[index] = (char)bytePassword[index];
            }

            // For improved security, clear out the decrypted byte array.
            Arrays.fill(bytePassword, (byte)0);
        }
        finally
        {
            // Clear the password.
            if (decryptionPassword != null)
            {
                Arrays.fill(decryptionPassword, '\0');
            }
            if (bytePassword != null)
            {
                Arrays.fill(bytePassword, (byte)0);
            }
        }

        return password;
    }

    /**
     * Encrypts the given password. <br/>
     * NOTE: the <code>encryptionPassword</code> array will be cleared before
     * this method returns.
     *
     * @param password The password to encrypt
     * @param encryptionPassword the password to use during encryption.
     * @return The encrypted password
     * @throws GeneralSecurityException if an  error occurs while decrypting the
     *             password.
     * @throws IllegalStateException if either the encrypted password or
     *             decryption passwords are <code>null</code>.
     */
    public static String encryptPassword(char[] password, char[] encryptionPassword) throws GeneralSecurityException
    {
        String encryptedPassword = null;

        byte[] bytePassword = null;
        try
        {
            if (encryptionPassword == null)
            {
                throw new IllegalStateException("The encryption password is null");
            }
            if (password == null)
            {
                throw new IllegalStateException("The decrypted password is null");
            }

            // Generate the secret key using the encryption password.
            SecretKeySpec secretKeySpec = getSecretKeySpec(encryptionPassword);

            // Create a cipher based upon the secret key's algorithm.
            Cipher cipher = Cipher.getInstance(secretKeySpec.getAlgorithm());

            // Initialize the cipher with secret key.
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            // Transfer the password to a byte array.
            bytePassword = new byte[password.length];
            for (int index = 0; index < password.length; index++)
            {
                bytePassword[index] = (byte)password[index];
            }

            // Encrypt the password
            byte[] encrypted = cipher.doFinal(bytePassword);

            // Base64 encode the encrypted password.
            encryptedPassword = new String(new Base64().encode(encrypted));
        }
        finally
        {
            // Clear the password.
            if (encryptionPassword != null)
            {
                Arrays.fill(encryptionPassword, '\0');
            }
            if (bytePassword != null)
            {
                Arrays.fill(bytePassword, (byte)0);
            }
        }

        return encryptedPassword;
    }

    /**
     * Generates a <code>SecretKeySpec</code> from the given encryption
     * password.
     *
     * @param encryptionPassword the encryption password.
     * @return the <code>SecretKeySpec</code> from the
     * @throws NoSuchAlgorithmException
     */
    private static SecretKeySpec getSecretKeySpec(char[] encryptionPassword) throws NoSuchAlgorithmException
    {
        byte[] encryptionPasswordBytes = new byte[encryptionPassword.length];
        for (int ii = 0; ii < encryptionPassword.length; ii++)
        {
            encryptionPasswordBytes[ii] = (byte)encryptionPassword[ii];
        }
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(encryptionPasswordBytes, 0, encryptionPasswordBytes.length);

        SecretKeySpec SECRET_KEY_SPEC = new SecretKeySpec(digest.digest(), "AES");
        return SECRET_KEY_SPEC;
    }

    @Override
    public ProtectedPassword clone() throws CloneNotSupportedException
    {
        return (ProtectedPassword)super.clone();
    }
}
