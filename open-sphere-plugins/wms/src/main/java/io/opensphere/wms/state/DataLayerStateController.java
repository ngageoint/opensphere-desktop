package io.opensphere.wms.state;

import io.opensphere.core.Plugin;
import io.opensphere.core.Toolbox;

/**
 * The state controller used to register with the system's save state component
 * that saves and restores WMS data layers.
 */
public class DataLayerStateController extends BaseWmsStateController
{
    /**
     * The module name for the controller.
     */
    public static final String MODULE_NAME = "Layers";

    /**
     * Constructs a new WMS state controller.
     *
     * @param wmsPlugin The wms plugin used to get the get capabilities envoys.
     * @param toolbox The system toolbox.
     */
    public DataLayerStateController(Plugin wmsPlugin, Toolbox toolbox)
    {
        super(wmsPlugin, toolbox);
    }

    @Override
    public boolean canSaveState()
    {
        return true;
    }

    @Override
    public boolean isSaveStateByDefault()
    {
        return true;
    }

    @Override
    protected boolean isDataLayer()
    {
        return true;
    }
}
