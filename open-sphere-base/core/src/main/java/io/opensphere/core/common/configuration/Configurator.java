package io.opensphere.core.common.configuration;

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class initializes and provides access to the Apache Configuration
 * instance.
 */
public class Configurator
{
    private static final Log LOGGER = LogFactory.getLog(Configurator.class);

    /**
     * The name of the root configuration file name. This file is the root of
     * the configuration hierarchy.
     */
    public static final String ROOT_CONFIG_FILE_NAME = "config.xml";

    /**
     * The <code>CombinedConfiguration</code> instance representing the root of
     * the configuration tree.
     */
    private static CombinedConfiguration config;

    /**
     * Initializes the <code>CombinedConfiguration</code> instance.
     *
     * @throws ConfigurationException if an  error occurs while creating the
     *             <code>Configuration</code> instance.
     */
    public static void initialize() throws ConfigurationException
    {

        // Asynchronous test, lock, synchronous test, create configuration
        // if needed
        if (config == null)
        {
            // Lock on Object.class since its loaded by the root class loader.
            // This provides synchronization across classloaders
            synchronized (Object.class)
            {
                if (config == null)
                {
                    URL url = Thread.currentThread().getContextClassLoader().getResource(ROOT_CONFIG_FILE_NAME);
                    LOGGER.info("Reading configuration from " + url);
                    DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder(url);
                    CombinedConfiguration combinedConfig = builder.getConfiguration(true);
                    combinedConfig.getRootNode();
                    config = combinedConfig;
                }
            }
        }
    }

    /**
     * Returns the <code>CombinedConfiguration</code> instance representing the
     * root of the configuration tree. The instance will be initialized with the
     * first call to this method if not already initialized.
     *
     * @return The <code>CombinedConfiguration</code> instance.
     */
    public static CombinedConfiguration getConfig()
    {
        try
        {
            Configurator.initialize();
        }
        catch (ConfigurationException e)
        {
            throw new RuntimeException(e);
        }
        return config;
    }

    /**
     * Replaces a leading tilde (~) character with the appropriate user's home
     * directory. If the path does not start with a tilde, the original string
     * is returned.
     *
     * If the path starts with a "~/" or "~\", the tilde character is replaced
     * by the "user.home" <code>System</code> property. Otherwise, the method
     * assumes that a user name follows the tilde and the tilde is replaced by
     * the parent directory of the "user.home" value followed by
     * <code>File.separatorChar</code>.
     *
     * @param path A file or directory path.
     * @return The file or directory path with the leading tilde replaced with
     *         the appropriate user's home directory.
     */
    public static String convertLeadingTilde(String path)
    {
        // If the destination directory starts with a ~, do some extra work
        // because Java does not handle this properly.
        if (path.startsWith("~"))
        {
            String userHome = System.getProperty("user.home");

            // If the next character is a / or \, replace the ~ with the home
            // directory.
            if (path.substring(1, 2).matches("[/\\\\]"))
            {
                path = userHome + File.separatorChar + path.substring(1);
            }

            // Otherwise, ~ is probably followed by a user name so append it to
            // the
            // parent directory of the user's home directory.
            else
            {
                File homeDir = new File(userHome);
                path = homeDir.getParent() + File.separatorChar + path.substring(1);
            }
        }
        return path;
    }
}
