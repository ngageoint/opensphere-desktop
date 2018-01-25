package io.opensphere.core.preferences;

import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.security.CipherFactory;

/**
 * Loads preferences from the classpath.
 */
public class ClasspathPreferencesPersistenceManager implements PreferencesPersistenceManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ClasspathPreferencesPersistenceManager.class);

    @Override
    public void delete(String topic)
    {
        throw new UnsupportedOperationException(
                ClasspathPreferencesPersistenceManager.class.getName() + " does not support delete.");
    }

    @Override
    public InternalPreferencesIF load(String topic, CipherFactory cipherFactory, boolean compressed)
    {
        if (cipherFactory != null)
        {
            throw new UnsupportedOperationException(getClass().getName() + " does not support encryption.");
        }
        if (compressed)
        {
            throw new UnsupportedOperationException(getClass().getName() + " does not support compression.");
        }

        String resourceName = getResourcePath(topic);
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (url == null)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Classpath preferences for topic [" + topic + "] not found at url [" + resourceName + "]");
            }
            return null;
        }
        else
        {
            try
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Loading preferences for topic [" + topic + "] from URL [" + url + "]");
                }
                PreferencesImpl pref = XMLUtilities.readXMLObject(url.openStream(), PreferencesImpl.class);
                pref.setPreferencesSaveable(false);
                return pref;
            }
            catch (JAXBException | IOException e)
            {
                LOGGER.error("Error loading preferences file from URL [" + url + "]: " + e, e);
                return null;
            }
        }
    }

    @Override
    public void save(Preferences preferences, CipherFactory cipherFactory, boolean compression) throws IOException, JAXBException
    {
        throw new UnsupportedOperationException(
                ClasspathPreferencesPersistenceManager.class.getName() + " does not support save.");
    }

    @Override
    public boolean supportsCompression()
    {
        return false;
    }

    @Override
    public boolean supportsEncryption()
    {
        return false;
    }

    @Override
    public boolean supportsSave()
    {
        return false;
    }

    /**
     * Get the classpath resource name for a preferences topic.
     *
     * @param topic The preferences topic.
     * @return The resource name.
     */
    protected String getResourceName(String topic)
    {
        String name = topic.replaceAll(" ", "").replaceAll("\n", "");
        if (!name.endsWith(".xml"))
        {
            name = StringUtilities.concat(name, ".xml");
        }
        return name;
    }

    /**
     * Get the classpath resource path for a preferences topic.
     *
     * @param topic The preferences topic.
     * @return The resource path.
     */
    protected String getResourcePath(String topic)
    {
        return StringUtilities.concat("prefs/", getResourceName(topic));
    }
}
