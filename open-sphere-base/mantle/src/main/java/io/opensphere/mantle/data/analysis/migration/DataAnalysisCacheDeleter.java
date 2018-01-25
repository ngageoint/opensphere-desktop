package io.opensphere.mantle.data.analysis.migration;

import java.io.File;

import org.apache.log4j.Logger;

import io.opensphere.core.appl.PreConfigurationUpdateModule;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.analysis.DataAnalysisConfig;

/**
 * A pre-configuration update module used to clear the data analysis reporter
 * cache.
 */
public class DataAnalysisCacheDeleter implements PreConfigurationUpdateModule
{
    /**
     * The object used to capture log output.
     */
    private static final Logger LOG = Logger.getLogger(DataAnalysisCacheDeleter.class);

    /**
     * The key with which configuration values are associated.
     */
    private static final String CONFIGURATION_KEY = "dataAnalysisReporterConfig";

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.appl.PreConfigurationUpdateModule#updateConfigs(io.opensphere.core.preferences.PreferencesRegistry)
     */
    @Override
    public void updateConfigs(PreferencesRegistry prefsRegistry)
    {
        Preferences prefs = prefsRegistry.getPreferences("io.opensphere.mantle.data.analysis.DataAnalysisReporter");
        DataAnalysisConfig config = prefs.getJAXBObject(DataAnalysisConfig.class, CONFIGURATION_KEY, null);
        if (config == null)
        {
            config = new DataAnalysisConfig();
        }

        if (!config.isCacheCleared())
        {
            String preferencesDirectory = StringUtilities.expandProperties(System.getProperty("opensphere.path.runtime"),
                    System.getProperties()) + File.separator + "prefs";

            File cacheFile = new File(preferencesDirectory,
                    "io.opensphere.mantle.data.analysis.impl.DataAnalysisReporterImpl.dat");
            if (cacheFile.exists())
            {
                if (cacheFile.delete())
                {
                    LOG.info("Successfully removed data analysis cache file during startup.");
                    config.setCacheCleared(true);
                }
                else
                {
                    LOG.warn("Unable to remove data analysis cache file during startup operations.");
                }
            }

            // persist the state:
            prefs.putJAXBObject(CONFIGURATION_KEY, config, false, this);
        }
    }

}
