package io.opensphere.core.security.config.v1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBException;

import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.security.Digest;
import org.junit.Assert;

/**
 * Test for {@link CryptoConfig}.
 */
public class CryptoConfigTest
{
    /**
     * Test writing and reading a crypto config, as well as generating a secret
     * key from a password.
     *
     * @throws InvalidKeySpecException If the test fails.
     * @throws NoSuchAlgorithmException If the test fails.
     * @throws JAXBException If the test fails.
     */
    @Test
    public void test() throws InvalidKeySpecException, NoSuchAlgorithmException, JAXBException
    {
        SecretKeyFactory pbeSecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        char[] pw = "apass".toCharArray();
        byte[] secretKeySalt = new byte[16];
        SecureRandom.getInstance("SHA1PRNG").nextBytes(secretKeySalt);
        SecretKey pbeSecret = pbeSecretKeyFactory.generateSecret(new PBEKeySpec(pw, secretKeySalt, 50000, 128));
        SecretKey aesSecret = new SecretKeySpec(pbeSecret.getEncoded(), "AES");

        byte[] digestSalt = new byte[16];
        SecureRandom.getInstance("SHA1PRNG").nextBytes(digestSalt);
        byte[] pwDigest = pbeSecretKeyFactory.generateSecret(new PBEKeySpec(pw, digestSalt, 60000, 64)).getEncoded();

        CryptoConfig cryptoConfig = writeAndRead(new CryptoConfig(new Digest("PBE", pwDigest, digestSalt), secretKeySalt));

        SecretKey pbeSecret2 = pbeSecretKeyFactory.generateSecret(new PBEKeySpec(pw, cryptoConfig.getSalt(), 50000, 128));
        SecretKey aesSecret2 = new SecretKeySpec(pbeSecret2.getEncoded(), "AES");
        Assert.assertEquals(aesSecret, aesSecret2);

        byte[] pwDigest2 = pbeSecretKeyFactory.generateSecret(new PBEKeySpec(pw, cryptoConfig.getDigest().getSalt(), 60000, 64))
                .getEncoded();
        Assert.assertTrue(Arrays.equals(pwDigest, pwDigest2));
    }

    /**
     * Test helper that marshals and unmarshals and object.
     *
     * @param obj The object.
     * @return The new object.
     * @throws JAXBException If the marshalling/unmarshalling fails.
     */
    private CryptoConfig writeAndRead(CryptoConfig obj) throws JAXBException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(obj, baos);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        CryptoConfig obj2 = XMLUtilities.readXMLObject(bais, CryptoConfig.class);
        return obj2;
    }
}
