package io.opensphere.core.util.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/** Test for {@link EncryptedUsernamePasswordTest}. */
public class EncryptedUsernamePasswordTest
{
    /** The cipher factory. */
    private static CipherFactory ourCipherFactory;

    /**
     * Setup the ciphers.
     *
     * @throws GeneralSecurityException If the ciphers cannot be created.
     */
    @BeforeClass
    public static void setupCiphers() throws GeneralSecurityException
    {
        SecretKeyFactory pbeSecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        char[] pw = "test password".toCharArray();
        byte[] salt = new byte[16];
        SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);
        SecretKey pbeSecret = pbeSecretKeyFactory.generateSecret(new PBEKeySpec(pw, salt, 50000, 128));
        SecretKey aesSecret = new SecretKeySpec(pbeSecret.getEncoded(), "AES");
        ourCipherFactory = new CipherFactory(new DefaultSecretKeyProvider(aesSecret), "AES/CBC/PKCS5Padding");
    }

    /**
     * Test setting and getting the password.
     *
     * @throws JAXBException If the test fails.
     * @throws GeneralSecurityException If the test fails.
     */
    @Test
    public void testSetAndGetPassword() throws JAXBException, GeneralSecurityException
    {
        EncryptedUsernamePassword obj = new EncryptedUsernamePassword();

        String passwordString = "atestpasswordλ";
        passwordString = "apass";
        char[] password = passwordString.toCharArray();

        obj.setPassword(password, ourCipherFactory);

        EncryptedUsernamePassword obj2;

        obj2 = writeAndRead(obj);
        Assert.assertTrue(Arrays.equals(password, obj2.getPassword(ourCipherFactory)));
        Assert.assertEquals(passwordString, new String(obj2.getPassword(ourCipherFactory)));

        // Test some high-bit characters.
        password = new char[] { 0x8080 };
        obj.setPassword(password, ourCipherFactory);
        obj2 = writeAndRead(obj);
        Assert.assertTrue(Arrays.equals(password, obj2.getPassword(ourCipherFactory)));
    }

    /**
     * Test setting and getting the purpose.
     *
     * @throws JAXBException If the test fails.
     */
    @Test
    public void testSetAndGetPurpose() throws JAXBException
    {
        EncryptedUsernamePassword obj = new EncryptedUsernamePassword();

        String purpose = "someTest";
        obj.setPurpose(purpose);

        EncryptedUsernamePassword obj2;

        obj2 = writeAndRead(obj);
        Assert.assertEquals(purpose, obj2.getPurpose());
    }

    /**
     * Test setting and getting the username.
     *
     * @throws JAXBException If the test fails.
     */
    @Test
    public void testSetAndGetUsername() throws JAXBException
    {
        EncryptedUsernamePassword obj = new EncryptedUsernamePassword();

        String username = "testUser";
        obj.setUsername(username);

        EncryptedUsernamePassword obj2;

        obj2 = writeAndRead(obj);
        Assert.assertEquals(username, obj2.getUsername());
    }

    /**
     * Test helper that marshals and unmarshals an object.
     *
     * @param obj The object.
     * @return The new object.
     * @throws JAXBException If the marshalling/unmarshalling fails.
     */
    private EncryptedUsernamePassword writeAndRead(EncryptedUsernamePassword obj) throws JAXBException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(obj, baos);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        EncryptedUsernamePassword obj2 = XMLUtilities.readXMLObject(bais, EncryptedUsernamePassword.class);
        return obj2;
    }
}
