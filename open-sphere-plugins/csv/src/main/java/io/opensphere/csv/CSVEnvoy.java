package io.opensphere.csv;

import java.util.Properties;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;

/**
 * The Class CSVEnvoy.
 */
public class CSVEnvoy extends AbstractEnvoy
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(CSVEnvoy.class);

    /** The csv file controller. */
    @SuppressWarnings("PMD.SingularField")
    private final CSVFileDataSourceController myCSVFileController;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new CSV envoy.
     *
     * @param toolBox the tool box
     * @param pluginProperties the plugin properties
     */
    public CSVEnvoy(Toolbox toolBox, Properties pluginProperties)
    {
        super(toolBox);
        myToolbox = toolBox;
        myCSVFileController = new CSVFileDataSourceController(myToolbox, pluginProperties);
    }

    @Override
    public void close()
    {
        super.close();
        myCSVFileController.close();
    }

    /**
     * Gets the cSV file controller.
     *
     * @return the cSV file controller
     */
    public CSVFileDataSourceController getCSVFileController()
    {
        return myCSVFileController;
    }

    @Override
    public void open()
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("CSVEnvoy:OPEN");
        }
        myCSVFileController.setExecutorService(getExecutor());
        myCSVFileController.initialize();
    }

    @Override
    public void setFilter(Object filter)
    {
    }
}
