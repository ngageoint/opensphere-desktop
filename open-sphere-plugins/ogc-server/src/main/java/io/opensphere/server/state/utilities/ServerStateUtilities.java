package io.opensphere.server.state.utilities;

import java.util.List;

import com.bitsys.fade.mist.state.v4.LayerType;
import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.server.state.StateConstants;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;

/** Server state utilities. */
public final class ServerStateUtilities
{
    /**
     * Gets the WFS layers of the given type.
     *
     * @param configurationManager the manager used to access WFS Layer
     *            configurations.
     * @param state the state for which to test.
     * @return the set of layers defined for the OGC WFS protocol.
     */
    public static List<LayerType> getWfsLayers(WFSLayerConfigurationManager configurationManager, StateType state)
    {
        return StateUtilities.getLayers(state.getDataLayers(),
            layer -> configurationManager.isSupportedServer(layer.getType().toLowerCase()));
    }

    /**
     * Gets the WMS layers of the given type.
     *
     * @param state the state
     * @param activateDataLayer true for data, false for map
     * @return the layers
     */
    public static List<LayerType> getWmsLayers(StateType state, boolean activateDataLayer)
    {
        return StateUtilities.getLayers(activateDataLayer ? state.getDataLayers() : state.getMapLayers(),
                StateConstants.WMS_LAYER_TYPE);
    }

    /**
     * Gets the NRT layers of the given type.
     *
     * @param state the state
     * @return the layers
     */
    public static List<LayerType> getNrtLayers(StateType state)
    {
        return StateUtilities.getLayers(state.getDataLayers(), "nrt");
    }

    /** Disallow instantiation. */
    private ServerStateUtilities()
    {
    }
}
