package io.opensphere.server.state.activate.serversource.genericserver;

import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.server.state.StateConstants;

/**
 * Gets the list of urls contained in the wms layer nodes within a state node.
 */
public class WmsUrlRetriever extends BaseUrlRetriever
{
    @Override
    protected List<String> getLayerPaths()
    {
        return New.list(StateConstants.WMS_DATA_LAYER_PATH, StateConstants.WMS_MAP_LAYERS_PATH);
    }
}
