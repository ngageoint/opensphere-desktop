package io.opensphere.csv;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.apache.log4j.Logger;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.util.property.PluginPropertyUtils;
import io.opensphere.csvcommon.CSVStateConstants;
import io.opensphere.csvcommon.help.DateFormatHelp;

/**
 * Main control class for the CSV plugin.
 */
public class CSVPlugin extends PluginAdapter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CSVPlugin.class);

    /**
     * The date format help page.
     */
    private DateFormatHelp myDateFormatHelp;

    /** The envoy for managing acquisition of CSV data. */
    @SuppressWarnings("PMD.SingularField")
    private CSVEnvoy myEnvoy;

    /** The Plugin properties. */
    private Properties myPluginProperties;

    /** The State controller for CSV files. */
    private CSVStateController myStateController;

    @Override
    public void close()
    {
    }

    @Override
    public Collection<? extends Envoy> getEnvoys()
    {
        return Collections.singletonList(myEnvoy);
    }

    @Override
    public void initialize(PluginLoaderData data, Toolbox toolbox)
    {
        myPluginProperties = PluginPropertyUtils.convertToProperties(data.getPluginProperty());
        myEnvoy = new CSVEnvoy(toolbox, myPluginProperties);
        myDateFormatHelp = new DateFormatHelp();
        myDateFormatHelp.initializeHelpMenu(toolbox);

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Sending Register for CSVController" + Thread.currentThread().getName());
        }

        myStateController = new CSVStateController(myEnvoy);
        toolbox.getModuleStateManager().registerModuleStateController(CSVStateConstants.MODULE_NAME, myStateController);
    }
}
