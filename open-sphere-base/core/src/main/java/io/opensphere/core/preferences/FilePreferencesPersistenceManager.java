package io.opensphere.core.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.security.NoSuchAlgorithmException;
import java.util.ServiceLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.security.CipherException;
import io.opensphere.core.util.security.CipherFactory;
import io.opensphere.core.util.security.EncryptionParameters;

/**
 * A class for persisting preferences to a file.
 */
@SuppressWarnings("PMD.GodClass")
public class FilePreferencesPersistenceManager implements PreferencesPersistenceManager
{
    /** Message when there's a load error. */
    private static final String LOAD_ERROR_MESSAGE = "Error loading from preferences file: ";

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(FilePreferencesPersistenceManager.class);

    /** Suffix for temporary files. */
    private static final String TMP_SUFFIX = ".tmp";

    /** The base directory for files. */
    private final String myBaseDirectory;

    /**
     * Constructor.
     */
    public FilePreferencesPersistenceManager()
    {
        this(StringUtilities.expandProperties(System.getProperty("opensphere.path.runtime"), System.getProperties())
                + File.separator + "prefs");
    }

    /**
     * Constructor.
     *
     * @param baseDirectory - the base directory for saving files.
     */
    @SuppressWarnings("PMD.CollapsibleIfStatements")
    public FilePreferencesPersistenceManager(String baseDirectory)
    {
        myBaseDirectory = baseDirectory;

        final File dir = new File(myBaseDirectory);
        if (dir.exists())
        {
            // Delete tmp files hanging around.
            final String[] filenames = dir.list();
            if (filenames != null)
            {
                for (final String filename : filenames)
                {
                    if (filename.endsWith(TMP_SUFFIX) && !new File(dir, filename).delete() && LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("Could not delete temp file [" + filename + "]");
                    }
                }
            }

            // Update preference files if necessary
            for (final FilePreferencesPersistenceUpdater updater : ServiceLoader.load(FilePreferencesPersistenceUpdater.class))
            {
                updater.updateConfigs(baseDirectory);
            }
        }
    }

    @Override
    public synchronized void delete(String topic)
    {
        for (final boolean compressed : new boolean[] { true, false })
        {
            for (final boolean encrypted : new boolean[] { true, false })
            {
                final File aFile = getFile(topic, getExtension(encrypted, compressed));
                try
                {
                    aFile.delete();
                }
                catch (final RuntimeException e)
                {
                    LOGGER.error("Failed to delete preference file: " + e, e);
                }
            }
        }
    }

    @Override
    public InternalPreferencesIF load(String topic, CipherFactory cipherFactory, boolean compressed)
    {
        InternalPreferencesIF prefs = null;
        final File aFile = getFile(topic, getExtension(cipherFactory != null, compressed));
        if (aFile.exists())
        {
            if (aFile.canRead())
            {
                try
                {
                    prefs = loadFile(aFile, cipherFactory, compressed);
                }
                catch (final JAXBException e)
                {
                    if (cipherFactory == null)
                    {
                        LOGGER.error(LOAD_ERROR_MESSAGE + aFile.getAbsolutePath() + ": " + e, e);
                    }
                    else
                    {
                        LOGGER.warn(LOAD_ERROR_MESSAGE + aFile.getAbsolutePath() + ": " + e);
                    }
                }
                catch (final FileNotFoundException e)
                {
                    LOGGER.error(LOAD_ERROR_MESSAGE + aFile.getAbsolutePath() + ": " + e, e);
                }
                catch (final StreamCorruptedException e)
                {
                    if (cipherFactory == null)
                    {
                        LOGGER.error(LOAD_ERROR_MESSAGE + aFile.getAbsolutePath() + ": " + e, e);
                    }
                    else
                    {
                        LOGGER.warn(LOAD_ERROR_MESSAGE + aFile.getAbsolutePath() + ": " + e);
                    }
                }
                catch (IOException | RuntimeException e)
                {
                    LOGGER.error(LOAD_ERROR_MESSAGE + aFile.getAbsolutePath() + ": " + e, e);
                }
            }
            else
            {
                LOGGER.error("Could not read preferences file: " + aFile.getAbsolutePath() + " Check file permissions.");
            }
        }

        return prefs;
    }

    @Override
    @SuppressWarnings("PMD.CollapsibleIfStatements")
    public synchronized void save(Preferences preferences, CipherFactory cipherFactory, boolean compressed)
        throws IOException, JAXBException
    {
        final File aFile = getFile(preferences.getTopic(), getExtension(cipherFactory != null, compressed));
        if (!aFile.getParentFile().exists() && !aFile.getParentFile().mkdirs())
        {
            throw new IOException("Unable to create parent directory for preferences file: " + aFile.getAbsolutePath());
        }

        // Write to a temp file to avoid leaving a half-written file if the app
        // is killed. Use the same directory as the preferences file to avoid
        // permissions problems or copying across file systems.
        final File temp = File.createTempFile(preferences.getTopic(), TMP_SUFFIX, aFile.getParentFile());
        OutputStream os = new FileOutputStream(temp);
        boolean success = false;
        try
        {
            if (cipherFactory != null)
            {
                CipherOutputStream cos;
                try
                {
                    final Cipher cipher = cipherFactory.initCipher(Cipher.ENCRYPT_MODE);
                    writeEncryptionParametersToStream(os, cipher);
                    cos = new CipherOutputStream(os, cipher);
                }
                catch (final CipherException e)
                {
                    LOGGER.error("Failed to encrypt preferences: " + e, e);
                    return;
                }
                os = cos;
            }
            if (compressed)
            {
                final ZipOutputStream zos = new ZipOutputStream(os);
                zos.putNextEntry(new ZipEntry(preferences.getTopic().replaceAll(" ", "").replaceAll("\n", "").concat(".xml")));
                os = zos;
            }
            XMLUtilities.writeXMLObject(preferences, os);
            success = true;
        }
        finally
        {
            os.close();

            if (success && (aFile.exists() && !aFile.delete() || !temp.renameTo(aFile)))
            {
                LOGGER.warn("Failed to rename preferences temp file [" + temp + "] to [" + aFile
                        + "]: preferences were not saved correctly.");
            }
            else if (!temp.delete() && LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Failed to delete temp file: " + temp);
            }
        }
    }

    @Override
    public boolean supportsCompression()
    {
        return true;
    }

    @Override
    public boolean supportsEncryption()
    {
        return true;
    }

    @Override
    public boolean supportsSave()
    {
        return true;
    }

    /**
     * Get the file extension to use.
     *
     * @param encrypted Flag indicating if the preferences are encrypted.
     * @param compressed Flag indicating if the preferences are compressed.
     * @return The file extension.
     */
    protected String getExtension(boolean encrypted, boolean compressed)
    {
        return encrypted ? ".dat" : compressed ? ".zip" : ".xml";
    }

    /**
     * Get the preferences file for a topic.
     *
     * @param topic The preferences topic.
     * @param extension The extension.
     * @return The file.
     */
    protected File getFile(String topic, String extension)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(myBaseDirectory).append(File.separator).append(topic.replaceAll(" ", "").replaceAll("\n", ""))
                .append(extension);
        return new File(sb.toString());
    }

    /**
     * Load preferences from a file.
     *
     * @param aFile The file.
     * @param cipherFactory Optional cipher factory for encrypted files.
     * @param compressed Indicates if the file is compressed.
     * @return The preferences, or {@code null} if there was a decryption error.
     * @throws FileNotFoundException If the file could not be found.
     * @throws IOException If the file could not be read.
     * @throws JAXBException If the XML is malformed.
     */
    private InternalPreferencesIF loadFile(File aFile, CipherFactory cipherFactory, boolean compressed)
        throws IOException, JAXBException
    {
        InternalPreferencesIF prefs;
        if (compressed || cipherFactory != null)
        {
            InputStream is = new FileInputStream(aFile);
            try
            {
                if (cipherFactory != null)
                {
                    Object obj;
                    obj = readEncryptionParametersFromStream(is);
                    if (obj instanceof EncryptionParameters)
                    {
                        final Cipher decryptCipher = ((EncryptionParameters)obj).getDecryptCipher(cipherFactory);
                        final CipherInputStream cis = new CipherInputStream(is, decryptCipher);
                        is = cis;
                    }
                }
                if (compressed)
                {
                    final ZipInputStream zis = new ZipInputStream(is);
                    zis.getNextEntry();
                    is = zis;
                }
                prefs = XMLUtilities.readXMLObject(new StreamSource(is), PreferencesImpl.class);
            }
            catch (ClassNotFoundException | CipherException | NoSuchAlgorithmException e)
            {
                LOGGER.error(LOAD_ERROR_MESSAGE + aFile.getAbsolutePath() + ": " + e, e);
                return null;
            }
            finally
            {
                is.close();
            }
        }
        else
        {
            prefs = XMLUtilities.readXMLObject(aFile, PreferencesImpl.class);
        }
        return prefs;
    }

    /**
     * Read encryption parameters from an input stream.
     *
     * @param is The input stream.
     * @return The parameters.
     * @throws IOException If there is an error reading from the stream.
     * @throws ClassNotFoundException If the parameters class cannot be found.
     */
    private Object readEncryptionParametersFromStream(InputStream is) throws IOException, ClassNotFoundException
    {
        return new ObjectInputStream(is).readObject();
    }

    /**
     * Write the encryption parameters of a cipher to an output stream.
     *
     * @param os The output stream.
     * @param cipher The cipher.
     * @throws IOException If there is an error writing to the stream or the
     *             parameters cannot be encoded.
     */
    private void writeEncryptionParametersToStream(OutputStream os, Cipher cipher) throws IOException
    {
        new ObjectOutputStream(os).writeObject(new EncryptionParameters(cipher));
    }
}
