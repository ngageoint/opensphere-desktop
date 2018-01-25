package io.opensphere.wms.state;

import io.opensphere.core.Plugin;
import io.opensphere.core.Toolbox;

/**
 * A state controller used to register with the state module that saves and
 * restores the state of WMS map layer.
 */
public class MapLayerStateController extends BaseWmsStateController
{
    /**
     * The module name for the controller.
     */
    public static final String MODULE_NAME = "Map Layers";

    /**
     * Constructs a new map layer state controller.
     *
     * @param wmsPlugin The wms plugin used to get the get capabilities envoys.
     * @param toolbox The system toolbox.
     */
    public MapLayerStateController(Plugin wmsPlugin, Toolbox toolbox)
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
        return false;
    }

    @Override
    protected boolean isDataLayer()
    {
        return false;
    }
}
