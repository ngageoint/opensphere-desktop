package io.opensphere.wms.state.save;

import java.util.List;

import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.LayersType;
import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.Toolbox;
import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.wms.layer.WMSLayerValueProvider;
import io.opensphere.wms.layer.WMSLayerValueProviderImpl;
import io.opensphere.wms.state.model.WMSLayerAndState;
import io.opensphere.wms.state.model.WMSLayerState;
import io.opensphere.wms.state.save.controllers.LayersToStateModels;
import io.opensphere.wms.state.save.controllers.NodeWriter;
import io.opensphere.wms.state.save.controllers.WMSLayerProvider;

/**
 * Saves all WMS layer states to the a specified node.
 */
public class StateSaver
{
    /**
     * Provides the wms layers currently active within the system.
     */
    private final WMSLayerProvider myLayerProvider;

    /**
     * Retrieves the different layers.
     */
    private final LayersToStateModels myLayersToStates;

    /**
     * Writes the layer state models to the passed in state node.
     */
    private final NodeWriter myNodeWriter;

    /**
     * Constructs a new state saver.
     *
     * @param toolbox The system toolbox.
     */
    public StateSaver(Toolbox toolbox)
    {
        myLayerProvider = new WMSLayerProvider(toolbox.getDataRegistry());
        myLayersToStates = new LayersToStateModels(toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class));
        myNodeWriter = new NodeWriter();
    }

    /**
     * Saves all WMS layer states to the specified node.
     *
     * @param node The node to save the states to.
     * @param saveDataLayers True if the data layers should be saved, false if
     *            the map layers should be saved.
     */
    public void saveState(Node node, boolean saveDataLayers)
    {
        List<WMSLayerAndState> layerStates = getLayerStates();
        myNodeWriter.writeToNode(node, layerStates, saveDataLayers);
    }

    /**
     * Saves all WMS layer states to the specified state object.
     *
     * @param state The state object.
     * @param saveDataLayers True if the data layers should be saved, false if
     *            the map layers should be saved.
     */
    public void saveState(StateType state, boolean saveDataLayers)
    {
        Pair<List<WMSLayerState>, List<WMSLayerState>> layerStates = getSegregatedLayerStates();
        List<WMSLayerState> wmsDataLayers = layerStates.getFirstObject();
        List<WMSLayerState> wmsMapLayers = layerStates.getSecondObject();

        if (saveDataLayers)
        {
            LayersType dataLayers = StateUtilities.getDataLayers(state);
            for (WMSLayerState layerState : wmsDataLayers)
            {
                dataLayers.getLayer().add(SaveStateV3ToV4Translator.toLayerType(layerState));
            }
        }
        else
        {
            LayersType mapLayers = StateUtilities.getMapLayers(state);
            for (WMSLayerState layerState : wmsMapLayers)
            {
                mapLayers.getLayer().add(SaveStateV3ToV4Translator.toLayerType(layerState));
            }
        }
    }

    /**
     * Gets the layer states separated into data and map layers.
     *
     * @return the pair of data and map layers
     */
    private Pair<List<WMSLayerState>, List<WMSLayerState>> getSegregatedLayerStates()
    {
        List<WMSLayerState> dataLayers = New.list();
        List<WMSLayerState> mapLayers = New.list();
        NodeWriter.segregateLayers(getLayerStates(), dataLayers, mapLayers);
        return new Pair<>(dataLayers, mapLayers);
    }

    /**
     * Gets the layer/state objects.
     *
     * @return the layer/states
     */
    private List<WMSLayerAndState> getLayerStates()
    {
        List<WMSLayerValueProvider> valueProviders = StreamUtilities.map(myLayerProvider.getLayers(),
                WMSLayerValueProviderImpl::new);
        return myLayersToStates.toStateModels(valueProviders);
    }
}
