package io.opensphere.controlpanels.toolbox;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.opensphere.controlpanels.DetailPane;
import io.opensphere.controlpanels.GenericThingProvider;
import io.opensphere.controlpanels.layers.availabledata.detail.ImagePreviewPane;
import io.opensphere.controlpanels.layers.availabledata.detail.TextDetailPane;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.MapVisualizationType;

/**
 * The default detail panel provider, used to provide detail panels for all data
 * types.
 * @param <T>
 */
public class DefaultDetailPanelProvider<T> implements GenericThingProvider
{
    /**
     * The set of instantiated instances.
     */
    private final Map<MapVisualizationType, DetailPane> myDetailPanelInstances;

    /**
     * The detail pane to use when no data types are detected.
     */
    private final DetailPane myDefaultDetailPane;

    /**
     * The toolbox with which system interactions occur.
     */
    private final Toolbox myToolbox;

    /**
     * Creates a new provider.
     *
     * @param pToolbox The toolbox through which system interactions occur.
     */
    public DefaultDetailPanelProvider(Toolbox pToolbox)
    {
        myToolbox = pToolbox;
        myDetailPanelInstances = new HashMap<>();
        myDefaultDetailPane = new TextDetailPane(pToolbox);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.DetailPanelProvider#supports(DataGroupInfo)
     */
    @Override
    public boolean supports(DataGroupInfo pDataGroup)
    {
        // this provider supports all data types:
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.DetailPanelProvider#getDetailPanel(DataGroupInfo)
     */
    @Override
    public DetailPane getDetailPanel(DataGroupInfo pDataGroup)
    {
        DetailPane returnValue = null;
        Set<MapVisualizationType> visualizationTypes = pDataGroup.getMemberMapVisualizationTypes(false);
        if (visualizationTypes.contains(MapVisualizationType.MOTION_IMAGERY)
                || visualizationTypes.contains(MapVisualizationType.MOTION_IMAGERY_DATA))
        {
            if (!myDetailPanelInstances.containsKey(MapVisualizationType.MOTION_IMAGERY))
            {
                initialize(MapVisualizationType.MOTION_IMAGERY);
            }

            returnValue = myDetailPanelInstances.get(MapVisualizationType.MOTION_IMAGERY);
            returnValue.populate(pDataGroup);
        }
        else
        {
            MapVisualizationType visualizationType = null;
            if (!visualizationTypes.isEmpty())
            {
                visualizationType = visualizationTypes.iterator().next();

                if (!myDetailPanelInstances.containsKey(visualizationType))
                {
                    initialize(visualizationType);
                }

                returnValue = myDetailPanelInstances.get(visualizationType);
                returnValue.populate(pDataGroup);
            }
            else
            {
                returnValue = myDefaultDetailPane;
                returnValue.populate(pDataGroup);
            }
        }
        return returnValue;
    }

    /**
     * Initializes and stores a new detail panel for the supplied visualization
     * type.
     *
     * @param pType the visualization type for which to instantiate a new
     *            visualization type.
     */
    protected void initialize(MapVisualizationType pType)
    {
        synchronized (myDetailPanelInstances)
        {
            Set<MapVisualizationType> keys = New.set(MapVisualizationType.values());

            DetailPane detailPane;
            switch (pType)
            {
                case MOTION_IMAGERY:
                case MOTION_IMAGERY_DATA:
                    detailPane = new ImagePreviewPane(myToolbox);
                    keys.retainAll(New.set(MapVisualizationType.MOTION_IMAGERY, MapVisualizationType.MOTION_IMAGERY_DATA));
                    break;
                default:
                    detailPane = new TextDetailPane(myToolbox);
                    keys.removeAll(New.set(MapVisualizationType.MOTION_IMAGERY, MapVisualizationType.MOTION_IMAGERY_DATA));
                    break;
            }

            // register the new instance for all valid key types:
            keys.forEach(t -> myDetailPanelInstances.put(t, detailPane));
        }
    }
}
