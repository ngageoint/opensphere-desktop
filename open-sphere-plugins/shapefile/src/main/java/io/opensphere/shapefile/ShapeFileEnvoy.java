package io.opensphere.shapefile;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;

/**
 * The Class Shape File Envoy.
 */
public class ShapeFileEnvoy extends AbstractEnvoy
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ShapeFileEnvoy.class);

    /** The shapefile controller. */
    private final ShapeFileDataSourceController myShapeFileController;

    /**
     * Instantiates a new shapefile envoy.
     *
     * @param toolBox the tool box
     */
    public ShapeFileEnvoy(Toolbox toolBox)
    {
        super(toolBox);
        myShapeFileController = new ShapeFileDataSourceController(toolBox);
    }

    @Override
    public void close()
    {
        super.close();
        myShapeFileController.close();
    }

    /**
     * Gets the controller.
     *
     * @return the controller
     */
    public ShapeFileDataSourceController getController()
    {
        return myShapeFileController;
    }

    @Override
    public void open()
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("ShapeFileEnvoy");
        }
        myShapeFileController.setExecutorService(getExecutor());
        myShapeFileController.initialize();
    }
}
