package io.opensphere.core.common.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.opensphere.core.common.configuration.Configurator;

/**
 * Provides property utilities for stand alone applications
 */
public class PropertyUtils
{

    /** The <code>Log</code> instance used for logging. */
    private static final Log logger = LogFactory.getLog(PropertyUtils.class);

    /**
     * Load the property file, system, and local override properties
     *
     * @param propertyFileUrl
     * @param overrideFileName
     */
    public static void loadProperties(String propertyFileUrl, String overrideFileName)
    {
        loadProperties(propertyFileUrl, overrideFileName, null);
    }

    /**
     * Load the property file, system, and local override properties
     *
     * @param propertyFileUrl
     * @param overrideFileName
     * @param additionalProperties
     */
    public static void loadProperties(String propertyFileUrl, String overrideFileName, Map<String, String> additionalProperties)
    {

        Properties properties = new Properties();

        combineProperties(properties, propertyFileUrl, overrideFileName);

        CombinedConfiguration config = Configurator.getConfig();
        for (Entry<Object, Object> property : properties.entrySet())
        {
            config.setProperty((String)property.getKey(), property.getValue());
        }

        if (additionalProperties != null)
        {
            for (Map.Entry<String, String> property : additionalProperties.entrySet())
            {
                String value = config.getString(property.getValue());
                if (allowable(value))
                {
                    properties.put(property.getKey(), value);
                }
            }
        }

        System.getProperties().putAll(properties);

    }

    /**
     * Consolidates all properties into System properties.
     */
    private static void combineProperties(Properties properties, String propertyFileUrl, String overrideFileName)
    {
        /* Obtain the instance of the Config and copy all properties to
         * System.properties. */
        CombinedConfiguration config = Configurator.getConfig();
        Iterator<?> iter = config.getKeys();

        while (iter.hasNext())
        {
            String key = (String)iter.next();
            Object val = config.getProperty(key);
            if (allowable(val))
            {
                properties.put(key, val);
            }
        }

        if (propertyFileUrl != null)
        {
            try
            {
                properties.load(new URL(propertyFileUrl).openStream());
            }
            catch (IOException e)
            {
                if (logger.isErrorEnabled())
                {
                    logger.error("Error initializing properties.", e);
                }
                System.exit(1);
            }
        }

        if (overrideFileName != null)
        {

            String userFileName = System.getProperty("user.home") + System.getProperty("file.separator") + overrideFileName;

            /* Read the local properties file into a new configuration and
             * override any previously defined system properties and config
             * values */
            try
            {
                PropertiesConfiguration.PropertiesReader pp;
                FileInputStream fstream = new FileInputStream(userFileName);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                pp = new PropertiesConfiguration.PropertiesReader(br);

                while (pp.nextProperty())
                {
                    String key = pp.getPropertyName();
                    Object val = pp.getPropertyValue();
                    if (allowable(val))
                    {
                        properties.put(key, val);
                    }
                }

                if (logger.isInfoEnabled())
                {
                    logger.info("Loaded properties from local: " + userFileName);
                }

            }
            catch (FileNotFoundException e)
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("Not using properties from local: " + userFileName + ". Not found");
                }
            }
            catch (IOException e)
            {
                if (logger.isErrorEnabled())
                {
                    logger.error("Problem parsing " + userFileName, e);
                }
            }
        }

    }

    /**
     * Determine if the value is allowable in the system properties
     *
     * @param value
     * @return
     */
    private static boolean allowable(Object value)
    {

        return value != null && value instanceof String && !((String)value).isEmpty();

    }

}
