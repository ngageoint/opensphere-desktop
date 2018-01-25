package io.opensphere.core.preferences;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.security.CipherFactory;
import io.opensphere.core.util.security.DefaultSecretKeyProvider;

/** Tests for {@link FilePreferencesPersistenceManager}. */
public class FilePreferencesPersistenceManagerTest
{
    /**
     * Test saving and loading preferences.
     *
     * @throws IOException If the test fails.
     * @throws JAXBException If the test fails.
     * @throws NoSuchAlgorithmException If the test fails.
     */
    @Test
    public void testSaveAndLoad() throws IOException, JAXBException, NoSuchAlgorithmException
    {
        FilePreferencesPersistenceManager manager = new FilePreferencesPersistenceManager(System.getProperty("java.io.tmpdir"));
        String topic = "testtopic";
        Preferences prefs = new PreferencesImpl(topic);
        String booleanKey = "testBoolean";
        prefs.putBoolean(booleanKey, true, this);

        boolean compressed;
        CipherFactory cipherFactory;
        Preferences loadedPrefs;

        compressed = false;
        cipherFactory = null;
        manager.save(prefs, cipherFactory, compressed);
        loadedPrefs = manager.load(topic, cipherFactory, compressed);
        Assert.assertTrue(loadedPrefs.getBoolean(booleanKey, false));
        if (!manager.getFile(topic, manager.getExtension(false, compressed)).delete())
        {
            Assert.fail();
        }

        compressed = true;
        manager.save(prefs, cipherFactory, compressed);
        loadedPrefs = manager.load(topic, cipherFactory, compressed);
        Assert.assertTrue(loadedPrefs.getBoolean(booleanKey, false));
        if (!manager.getFile(topic, manager.getExtension(false, compressed)).delete())
        {
            Assert.fail();
        }

        SecretKey key = KeyGenerator.getInstance("AES").generateKey();
        cipherFactory = new CipherFactory(new DefaultSecretKeyProvider(key), "AES/CBC/PKCS5Padding");
        manager.save(prefs, cipherFactory, compressed);
        loadedPrefs = manager.load(topic, cipherFactory, compressed);
        Assert.assertTrue(loadedPrefs.getBoolean(booleanKey, false));
        if (!manager.getFile(topic, manager.getExtension(true, compressed)).delete())
        {
            Assert.fail();
        }

        compressed = false;
        manager.save(prefs, cipherFactory, compressed);
        loadedPrefs = manager.load(topic, cipherFactory, compressed);
        Assert.assertTrue(loadedPrefs.getBoolean(booleanKey, false));
        if (!manager.getFile(topic, manager.getExtension(true, compressed)).delete())
        {
            Assert.fail();
        }
    }
}
