package io.opensphere.xyztile.mantle;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.DataRegistryListenerAdapter;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DefaultDataTypeInfoOrderManager;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultMapTileVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultTileLevelController;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.xyztile.model.XYZDataTypeInfo;
import io.opensphere.xyztile.model.XYZSettings;
import io.opensphere.xyztile.model.XYZTileLayerInfo;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * Responds to {@link XYZTileLayerInfo}s being added and removed to and from the
 * system. When added it will create all the necessary objects so they show up
 * on the layer tree and on the map. When removed all the necessary calls will
 * be made so they are removed from the layer tree and the map.
 */
public class XYZMantleController extends DataRegistryListenerAdapter<XYZTileLayerInfo>
{
    /**
     * The assistant applied to each xyz group, so user can modify settings with
     * xyz layers.
     */
    private final XYZDataGroupInfoAssistant myAssistant;

    /** The order manager. */
    private final DefaultDataTypeInfoOrderManager myBaseOrderManager;

    /**
     * The mantle's data group controller. Used to add and remove layers.
     */
    private final DataGroupController myDataGroupController;

    /**
     * The system data registry. All {@link XYZTileLayerInfo} will be added and
     * removed from this object so we must listen for changes.
     */
    private final DataRegistry myDataRegistry;

    /**
     * The map of layer groups mapped by their data registry ids.
     */
    private final Map<Long, DataGroupInfo> myLayerGroups = Collections.synchronizedMap(New.map());

    /**
     * The mantle toolbox.
     */
    private final MantleToolbox myMantleToolbox;

    /**
     * Used to read saved xyz settings from the system.
     */
    private final SettingsBroker mySettingsBroker;

    /**
     * The order manager for tile layers that have corresponding feature data
     */
    private final DefaultDataTypeInfoOrderManager myTileOrderManager;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new mantle controller.
     *
     * @param toolbox The system toolbox.
     */
    public XYZMantleController(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myDataRegistry = toolbox.getDataRegistry();
        myMantleToolbox = MantleToolboxUtils.getMantleToolbox(myToolbox);
        myDataGroupController = myMantleToolbox.getDataGroupController();
        mySettingsBroker = new XYZSettingsBroker(myToolbox.getPreferencesRegistry());
        myAssistant = new XYZDataGroupInfoAssistant(mySettingsBroker);
        myBaseOrderManager = new DefaultDataTypeInfoOrderManager(toolbox.getOrderManagerRegistry()
                .getOrderManager(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY, DefaultOrderCategory.IMAGE_BASE_MAP_CATEGORY));
        myBaseOrderManager.open();
        myTileOrderManager = new DefaultDataTypeInfoOrderManager(toolbox.getOrderManagerRegistry()
                .getOrderManager(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY, DefaultOrderCategory.IMAGE_DATA_CATEGORY));
        myTileOrderManager.open();
        myDataRegistry.addChangeListener(this, new DataModelCategory(null, XYZTileUtils.LAYERS_FAMILY, null),
                XYZTileUtils.LAYERS_DESCRIPTOR);
    }

    @Override
    public void allValuesRemoved(Object source)
    {
        List<Long> idsToRemove = New.list(myLayerGroups.keySet());
        for (Long id : idsToRemove)
        {
            removeLayer(id);
        }
    }

    /**
     * Stops listening for new layers being added.
     */
    public void close()
    {
        myDataRegistry.removeChangeListener(this);
        myBaseOrderManager.close();
        myTileOrderManager.close();
    }

    @Override
    public boolean isIdArrayNeeded()
    {
        return true;
    }

    @Override
    public void valuesAdded(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends XYZTileLayerInfo> newValues,
            Object source)
    {
        int index = 0;
        for (XYZTileLayerInfo layerInfo : newValues)
        {
            addLayer(layerInfo, ids[index]);
            index++;
        }
    }

    @Override
    public void valuesRemoved(DataModelCategory dataModelCategory, long[] ids, Object source)
    {
        for (long id : ids)
        {
            removeLayer(Long.valueOf(id));
        }
    }

    /**
     * Adds the XYZ layer to the system.
     *
     * @param layer The layer to add.
     * @param modelId The id of the layer, so we can remove it later.
     */
    private void addLayer(XYZTileLayerInfo layer, long modelId)
    {
        applySettingsToLayer(layer);
        DataGroupInfo serverGroup = null;
        DataGroupInfo layerGroup = getExistingGroup(layer);
        boolean addLayerGroup = false;
        if (layerGroup == null)
        {
            addLayerGroup = true;
            serverGroup = new DefaultDataGroupInfo(true, myToolbox, XYZTileUtils.XYZ_PROVIDER + "Server", layer.getServerUrl(),
                    layer.getServerInfo().getServerName());
            myDataGroupController.addRootDataGroupInfo(serverGroup, this);
        }
        else if (layerGroup.isRootNode())
        {
            addLayerGroup = true;
            serverGroup = layerGroup;
            layerGroup = null;
        }

        layerGroup = newDataTypeAndGroup(layer, layerGroup);

        synchronized (myLayerGroups)
        {
            myLayerGroups.put(Long.valueOf(modelId), layerGroup);
        }

        if (serverGroup != null && addLayerGroup)
        {
            serverGroup.addChild(layerGroup, this);
        }
    }

    /**
     * Reads the saved xyz settings and sets those values on the specified
     * layer.
     *
     * @param layer The layer to apply settings to.
     */
    private void applySettingsToLayer(XYZTileLayerInfo layer)
    {
        XYZSettings settings = mySettingsBroker.getSettings(layer);
        layer.setMaxLevelsUser(settings.getMaxZoomLevelCurrent());
    }

    /**
     * Finds the group with parentId recursively traversing the descendants.
     *
     * @param parentId The id of the group to find.
     * @param parent The root group to traverse.
     * @return The group or null if nothing was found.
     */
    private DataGroupInfo findGroup(String parentId, DataGroupInfo parent)
    {
        DataGroupInfo existingGroup = null;

        for (DataGroupInfo dataGroup : parent.getChildren())
        {
            if (dataGroup.getId().equals(parentId))
            {
                existingGroup = dataGroup;
                break;
            }
            else
            {
                existingGroup = findGroup(parentId, dataGroup);
                if (existingGroup != null)
                {
                    break;
                }
            }
        }

        return existingGroup;
    }

    /**
     * Gets an existing server group if there is one.
     *
     * @param layer The layer we are adding to mantle.
     * @return An existing server group, or null if there isn't one.
     */
    private DataGroupInfo getExistingGroup(XYZTileLayerInfo layer)
    {
        DataGroupInfo existingGroup = null;

        Set<DataGroupInfo> rootGroups = myDataGroupController.getDataGroupInfoSet();
        for (DataGroupInfo rootGroup : rootGroups)
        {
            if (rootGroup.getDisplayName().equals(layer.getServerInfo().getServerName()))
            {
                existingGroup = rootGroup;
                break;
            }
        }

        // Couldn't find the server group as a root group, maybe it is one level
        // below that.
        if (existingGroup == null)
        {
            for (DataGroupInfo rootGroup : rootGroups)
            {
                for (DataGroupInfo child : rootGroup.getChildren())
                {
                    if (child.getDisplayName().equals(layer.getServerInfo().getServerName()))
                    {
                        existingGroup = rootGroup;
                        break;
                    }
                }
            }
        }

        if (existingGroup != null && StringUtils.isNotEmpty(layer.getParentId()))
        {
            existingGroup = findGroup(layer.getParentId(), existingGroup);
        }

        return existingGroup;
    }

    /**
     * Creates a data type.
     *
     * @param layer The layer info
     * @param suggestedLayerGroup The group to add the new type to, or null if
     *            we should create a new group.
     * @return the data type
     */
    private DataGroupInfo newDataTypeAndGroup(XYZTileLayerInfo layer, DataGroupInfo suggestedLayerGroup)
    {
        XYZDataTypeInfo dataType = new XYZDataTypeInfo(myToolbox, layer);

        dataType.setBasicVisualizationInfo(new DefaultBasicVisualizationInfo(LoadsTo.BASE,
                DefaultBasicVisualizationInfo.LOADS_TO_BASE_ONLY, Color.WHITE, false));

        TileRenderProperties props = new DefaultTileRenderProperties(0, true, false);
        float opacity = myMantleToolbox.getDataTypeInfoPreferenceAssistant().getOpacityPreference(dataType.getTypeKey(),
                ColorUtilities.COLOR_COMPONENT_MAX_VALUE) / (float)ColorUtilities.COLOR_COMPONENT_MAX_VALUE;
        props.setOpacity(opacity);
        DefaultMapTileVisualizationInfo mapVisInfo = new DefaultMapTileVisualizationInfo(MapVisualizationType.IMAGE_TILE, props,
                true);
        mapVisInfo.setTileLevelController(new DefaultTileLevelController());
        dataType.setMapVisualizationInfo(mapVisInfo);
        DataGroupInfo layerGroup = suggestedLayerGroup;

        if (layerGroup == null || !layerGroup.hasMembers(false))
        {
            myBaseOrderManager.activateParticipant(dataType);
        }
        else
        {
            myTileOrderManager.activateParticipant(dataType);
        }

        if (layerGroup == null)
        {
            DefaultDataGroupInfo newLayerGroup = new DefaultDataGroupInfo(false, myToolbox, XYZTileUtils.XYZ_PROVIDER,
                    dataType.getTypeKey(), layer.getDisplayName());
            newLayerGroup.setGroupDescription(layer.getDescription());
            layerGroup = newLayerGroup;
        }

        layerGroup.addMember(dataType, this);
        if (layerGroup instanceof DefaultDataGroupInfo)
        {
            DefaultDataGroupInfo defaultGroup = (DefaultDataGroupInfo)layerGroup;
            defaultGroup.setAssistant(myAssistant);
        }

        return layerGroup;
    }

    /**
     * Removes the layer and all of its info from the system.
     *
     * @param id The data registry id of the layer.
     */
    private void removeLayer(Long id)
    {
        DataGroupInfo dataGroup = myLayerGroups.remove(id);
        if (dataGroup != null)
        {
            DataGroupInfo serverGroup = dataGroup.getParent();
            if (serverGroup != null)
            {
                serverGroup.removeChild(dataGroup, this);
                if (!serverGroup.hasChildren())
                {
                    myDataGroupController.removeDataGroupInfo(serverGroup, this);
                }
            }

            DataTypeInfo dataType = dataGroup.getMembers(false).iterator().next();
            myBaseOrderManager.deactivateParticipant(dataType);
        }
    }
}
