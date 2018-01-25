package io.opensphere.mantle.util;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.StringUtilities;

/**
 * The Class EncryptionUtilities.
 */
public final class EncryptionUtilities
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(EncryptionUtilities.class);

    /**
     * Creates the decode cipher.
     *
     * @param sks the sks
     * @return the cipher
     */
    public static Cipher createDecodeCipher(SecretKeySpec sks)
    {
        Cipher decodeCipher = null;
        try
        {
            decodeCipher = Cipher.getInstance(sks.getAlgorithm());
            decodeCipher.init(Cipher.DECRYPT_MODE, sks);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e)
        {
            LOGGER.error("Failed to create decode cypher", e);
        }
        return decodeCipher;
    }

    /**
     * Creates the encode cipher.
     *
     * @param sks the sks
     * @return the cipher
     */
    public static Cipher createEncodeCipher(SecretKeySpec sks)
    {
        Cipher encodeCipher = null;
        try
        {
            encodeCipher = Cipher.getInstance(sks.getAlgorithm());
            encodeCipher.init(Cipher.ENCRYPT_MODE, sks);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e)
        {
            LOGGER.error("Failed to create encode cypher", e);
        }
        return encodeCipher;
    }

    /**
     * Creates an MD5Hash of a given string value.
     *
     * @param value the value to use to construct the hash.
     * @return the MD5 Hash
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public static String createMD5Hash(String value) throws NoSuchAlgorithmException
    {
        MessageDigest messageDigester = MessageDigest.getInstance("MD5");
        byte[] valueArray = value.getBytes(StringUtilities.DEFAULT_CHARSET);
        messageDigester.update(valueArray, 0, valueArray.length);
        BigInteger i = new BigInteger(1, messageDigester.digest());
        return String.format("%1$32X", i);
    }

    /**
     * Gets the secret key spec.
     *
     * @param encryptionPassword the encryption password
     * @return the secret key spec
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public static SecretKeySpec createSecretKeySpec(char[] encryptionPassword) throws NoSuchAlgorithmException
    {
        byte[] encryptionPasswordBytes = new byte[encryptionPassword.length];
        for (int ii = 0; ii < encryptionPassword.length; ii++)
        {
            encryptionPasswordBytes[ii] = (byte)encryptionPassword[ii];
        }
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(encryptionPasswordBytes, 0, encryptionPasswordBytes.length);

        SecretKeySpec secretKeySpec = new SecretKeySpec(digest.digest(), "AES");
        return secretKeySpec;
    }

    /**
     * Instantiates a new encryption utilities.
     */
    private EncryptionUtilities()
    {
        // Do nothing.
    }
}
