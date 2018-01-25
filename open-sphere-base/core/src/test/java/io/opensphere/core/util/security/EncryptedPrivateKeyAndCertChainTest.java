package io.opensphere.core.util.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/** Tests for {@link EncryptedPrivateKeyAndCertChain}. */
public class EncryptedPrivateKeyAndCertChainTest
{
    /** The test keystore. */
    private static final KeyStore KEYSTORE = getTestKeyStore();

    /** Alias for the DSA key in the test keystore. */
    private static final String KEYSTORE_DSA_ALIAS = "dsakey";

    /** Password for the test keystore. */
    private static final char[] KEYSTORE_PASSWORD = "password".toCharArray();

    /** Alias for the RSA key in the test keystore. */
    private static final String KEYSTORE_RSA_ALIAS = "rsakey";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(EncryptedPrivateKeyAndCertChainTest.class);

    /** The cipher factory. */
    private static CipherFactory ourCipherFactory;

    /** Alias for testing. */
    private static final String TEST_ALIAS = "test alias";

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
     * Get the test key store.
     *
     * @return The key store.
     */
    private static KeyStore getTestKeyStore()
    {
        try
        {
            KeyStore ks = KeyStore.getInstance("JKS");
            InputStream is = EncryptedPrivateKeyAndCertChainTest.class.getResourceAsStream("/keystore.jks");
            try
            {
                char[] password = KEYSTORE_PASSWORD;
                ks.load(is, password);
            }
            finally
            {
                if (is != null)
                {
                    is.close();
                }
            }
            return ks;
        }
        catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e)
        {
            LOGGER.error(e, e);
        }

        return null;
    }

    /**
     * Test for
     * {@link EncryptedPrivateKeyAndCertChain#getPrivateKeyProvider(CipherFactory)}
     * and
     * {@link EncryptedPrivateKeyAndCertChain#setPrivateKeyProvider(io.opensphere.core.util.security.PrivateKeyProvider, CipherFactory)}
     * .
     *
     * @throws GeneralSecurityException If the test fails.
     */
    @Test
    public void testGetAndSetPrivateKeyProvider() throws GeneralSecurityException
    {
        EncryptedPrivateKeyAndCertChain obj = new EncryptedPrivateKeyAndCertChain();
        obj.setAlias(TEST_ALIAS);
        obj.setPrivateKey(getRSAPrivateKey(), ourCipherFactory);
        obj.setCertificateChain(getRSACertChain());

        SimplePrivateKeyProvider pkprovider = obj.getPrivateKeyProvider(ourCipherFactory);

        EncryptedPrivateKeyAndCertChain obj2 = new EncryptedPrivateKeyAndCertChain();
        obj2.setPrivateKeyProvider(pkprovider, ourCipherFactory);

        Assert.assertEquals(obj.getAlias(), obj2.getAlias());
        Assert.assertEquals(obj.getCertificateChain(), obj2.getCertificateChain());
        Assert.assertEquals(obj.getPrivateKey(ourCipherFactory), obj2.getPrivateKey(ourCipherFactory));
    }

    /**
     * Test for {@link EncryptedPrivateKeyAndCertChain#setAlias(String)} and
     * {@link EncryptedPrivateKeyAndCertChain#getAlias()}.
     *
     * @throws JAXBException If the test fails.
     */
    @Test
    public void testSetAndGetAlias() throws JAXBException
    {
        EncryptedPrivateKeyAndCertChain obj = new EncryptedPrivateKeyAndCertChain();
        obj.setAlias(TEST_ALIAS);

        EncryptedPrivateKeyAndCertChain obj2 = writeAndRead(obj);
        Assert.assertEquals(TEST_ALIAS, obj2.getAlias());
    }

    /**
     * Test for
     * {@link EncryptedPrivateKeyAndCertChain#setCertificateChain(Collection)}
     * and {@link EncryptedPrivateKeyAndCertChain#getCertificateChain()}.
     *
     * @throws JAXBException If the test fails.
     * @throws KeyStoreException If the test fails.
     * @throws CertificateException If the test fails.
     */
    @Test
    public void testSetAndGetCertificateChain() throws JAXBException, KeyStoreException, CertificateException
    {
        EncryptedPrivateKeyAndCertChain obj = new EncryptedPrivateKeyAndCertChain();

        Collection<? extends X509Certificate> chain = getRSACertChain();
        obj.setCertificateChain(chain);

        EncryptedPrivateKeyAndCertChain obj2 = writeAndRead(obj);
        Assert.assertEquals(chain, obj2.getCertificateChain());

        chain = getDSACertChain();
        obj.setCertificateChain(chain);

        obj2 = writeAndRead(obj);
        Assert.assertEquals(chain, obj2.getCertificateChain());
    }

    /**
     * Test for
     * {@link EncryptedPrivateKeyAndCertChain#setPrivateKey(PrivateKey, CipherFactory)}
     * and {@link EncryptedPrivateKeyAndCertChain#getPrivateKey(CipherFactory)}.
     *
     * @throws CipherException If the test fails.
     * @throws DecryptionException If the test fails.
     * @throws NoSuchAlgorithmException If the test fails.
     * @throws KeyStoreException If the test fails.
     * @throws UnrecoverableKeyException If the test fails.
     * @throws JAXBException If the test fails.
     */
    @Test
    public void testSetAndGetPrivateKey() throws CipherException, NoSuchAlgorithmException, DecryptionException,
        UnrecoverableKeyException, KeyStoreException, JAXBException
    {
        EncryptedPrivateKeyAndCertChain obj = new EncryptedPrivateKeyAndCertChain();

        PrivateKey privateKey;
        EncryptedPrivateKeyAndCertChain obj2;

        privateKey = getRSAPrivateKey();
        obj.setPrivateKey(privateKey, ourCipherFactory);

        obj2 = writeAndRead(obj);
        Assert.assertEquals(privateKey, obj2.getPrivateKey(ourCipherFactory));

        privateKey = getDSAPrivateKey();
        obj.setPrivateKey(privateKey, ourCipherFactory);

        obj2 = writeAndRead(obj);
        Assert.assertEquals(privateKey, obj2.getPrivateKey(ourCipherFactory));
    }

    /**
     * Test for {@link EncryptedPrivateKeyAndCertChain#validate()}.
     *
     * @throws JAXBException If the test fails.
     * @throws GeneralSecurityException If the test fails.
     */
    @Test
    public void testValidate() throws JAXBException, GeneralSecurityException
    {
        EncryptedPrivateKeyAndCertChain obj;

        obj = new EncryptedPrivateKeyAndCertChain();
        Assert.assertFalse(obj.validate());

        obj = new EncryptedPrivateKeyAndCertChain();
        obj.setAlias(TEST_ALIAS);
        Assert.assertFalse(obj.validate());

        obj = new EncryptedPrivateKeyAndCertChain();
        obj.setPrivateKey(getRSAPrivateKey(), ourCipherFactory);
        Assert.assertFalse(obj.validate());

        obj = new EncryptedPrivateKeyAndCertChain();
        obj.setCertificateChain(getRSACertChain());
        Assert.assertFalse(obj.validate());

        obj = new EncryptedPrivateKeyAndCertChain();
        obj.setPrivateKey(getRSAPrivateKey(), ourCipherFactory);
        obj.setCertificateChain(getRSACertChain());
        Assert.assertFalse(obj.validate());

        obj = new EncryptedPrivateKeyAndCertChain();
        obj.setAlias(TEST_ALIAS);
        obj.setCertificateChain(getRSACertChain());
        Assert.assertFalse(obj.validate());

        obj = new EncryptedPrivateKeyAndCertChain();
        obj.setAlias(TEST_ALIAS);
        obj.setPrivateKey(getRSAPrivateKey(), ourCipherFactory);
        Assert.assertFalse(obj.validate());

        obj = new EncryptedPrivateKeyAndCertChain();
        obj.setAlias(TEST_ALIAS);
        obj.setPrivateKey(getRSAPrivateKey(), ourCipherFactory);
        obj.setCertificateChain(getRSACertChain());
        Assert.assertTrue(obj.validate());
    }

    /**
     * Get the DSA certificate chain from the test keystore.
     *
     * @return The certificate chain.
     * @throws KeyStoreException If the chain cannot be retrieved.
     */
    private Collection<? extends X509Certificate> getDSACertChain() throws KeyStoreException
    {
        X509Certificate cert = (X509Certificate)KEYSTORE.getCertificate(KEYSTORE_DSA_ALIAS);
        return Collections.singletonList(cert);
    }

    /**
     * Get the DSA private key from the test keystore.
     *
     * @return The private key.
     * @throws KeyStoreException If there is an error reading the keystore.
     * @throws NoSuchAlgorithmException If the key algorithm is unsupported.
     * @throws UnrecoverableKeyException If the key password is wrong.
     */
    private PrivateKey getDSAPrivateKey() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException
    {
        return (PrivateKey)KEYSTORE.getKey(KEYSTORE_DSA_ALIAS, KEYSTORE_PASSWORD);
    }

    /**
     * Get the RSA certificate chain from the test keystore.
     *
     * @return The certificate chain.
     * @throws KeyStoreException If the chain cannot be retrieved.
     */
    private Collection<? extends X509Certificate> getRSACertChain() throws KeyStoreException
    {
        X509Certificate cert = (X509Certificate)KEYSTORE.getCertificate(KEYSTORE_RSA_ALIAS);
        return Collections.singletonList(cert);
    }

    /**
     * Get the RSA private key from the test keystore.
     *
     * @return The private key.
     * @throws KeyStoreException If there is an error reading the keystore.
     * @throws NoSuchAlgorithmException If the key algorithm is unsupported.
     * @throws UnrecoverableKeyException If the key password is wrong.
     */
    private PrivateKey getRSAPrivateKey() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException
    {
        return (PrivateKey)KEYSTORE.getKey(KEYSTORE_RSA_ALIAS, KEYSTORE_PASSWORD);
    }

    /**
     * Test helper that marshals and unmarshals an object.
     *
     * @param obj The object.
     * @return The new object.
     * @throws JAXBException If the marshalling/unmarshalling fails.
     */
    private EncryptedPrivateKeyAndCertChain writeAndRead(EncryptedPrivateKeyAndCertChain obj) throws JAXBException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(obj, baos);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        EncryptedPrivateKeyAndCertChain obj2 = XMLUtilities.readXMLObject(bais, EncryptedPrivateKeyAndCertChain.class);
        return obj2;
    }
}
