package io.opensphere.mantle.crust;

import java.awt.Color;
import java.util.function.Consumer;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.DynamicService;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.order.OrderCategory;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.mantle.GroupService;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.TypeService;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMapTileVisualizationInfo;

/**
 * EXPERIMENTAL. Simplifies some common Mantle use cases.
 */
public abstract class AbstractMantleController extends DynamicService<String, Service>
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /** The provider type. */
    private final String myProviderType;

    /** The root group. */
    private volatile DefaultDataGroupInfo myRootGroup;

    /** The group activation listener. */
    private final ActivationListener myActivationListener = new AbstractActivationListener()
    {
        @Override
        public void commit(DataGroupActivationProperty property, ActivationState state, PhasedTaskCanceller canceller)
        {
            handleGroupActivation(property, state, canceller);
        }
    };

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param providerType The provider type
     */
    public AbstractMantleController(Toolbox toolbox, String providerType)
    {
        super(toolbox.getEventManager());
        myToolbox = toolbox;
        myProviderType = providerType;
        myMantleToolbox = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
    }

    @Override
    public void open()
    {
        super.open();
        myRootGroup = createRootGroup();
        myMantleToolbox.getDataGroupController().addRootDataGroupInfo(myRootGroup, this);
    }

    @Override
    public void close()
    {
        myMantleToolbox.getDataGroupController().removeDataGroupInfo(myRootGroup, this);
        myRootGroup = null;
        super.close();
    }

    /**
     * Adds a folder/layer to the root group.
     *
     * @param folderName the folder name
     * @param layerName the layer name
     * @param layerId the unique layer ID
     * @param orderCategory the order category
     * @return the data group that was created, or the existing one
     */
    public DataGroupInfo add1stLevelLayer(String folderName, String layerName, String layerId, OrderCategory orderCategory)
    {
        return add1stLevelLayer(folderName, layerName, layerId, orderCategory, null);
    }

    /**
     * Adds a folder/layer to the root group.
     *
     * @param folderName the folder name
     * @param layerName the layer name
     * @param layerId the unique layer ID
     * @param orderCategory the order category
     * @param metadataInfo If a feature layer this is required, null if a tile
     *            layer.
     * @return the data group that was created, or the existing one
     */
    public DataGroupInfo add1stLevelLayer(String folderName, String layerName, String layerId, OrderCategory orderCategory,
            MetaDataInfo metadataInfo)
    {
        return add1stLevelLayer(folderName, layerName, layerId, orderCategory, metadataInfo, null);
    }

    /**
     * Adds a folder/layer to the root group.
     *
     * @param folderName the folder name
     * @param layerName the layer name
     * @param layerId the unique layer ID
     * @param orderCategory the order category
     * @param metadataInfo If a feature layer this is required, null if a tile
     *            layer.
     * @param deleteListener An object wanting notification when the group is
     *            deleted. This can be null if the layer can not be deleted by
     *            the user.
     * @return the data group that was created, or the existing one
     */
    public DataGroupInfo add1stLevelLayer(String folderName, String layerName, String layerId, OrderCategory orderCategory,
            MetaDataInfo metadataInfo, Consumer<DataGroupInfo> deleteListener)
    {
        return add1stLevelLayer(folderName, layerName, layerId, orderCategory, metadataInfo, deleteListener,
                new DefaultDataGroupAndTypeFactory());
    }

    /**
     * Adds a folder/layer to the root group.
     *
     * @param folderName the folder name
     * @param layerName the layer name
     * @param layerId the unique layer ID
     * @param orderCategory the order category
     * @param metadataInfo If a feature layer this is required, null if a tile
     *            layer.
     * @param deleteListener An object wanting notification when the group is
     *            deleted. This can be null if the layer can not be deleted by
     *            the user.
     * @param factory Instantiates new {@link DefaultDataGroupInfo} and
     *            {@link DefaultDataTypeInfo} or child classes of those.
     * @return the data group that was created, or the existing one
     */
    public DataGroupInfo add1stLevelLayer(String folderName, String layerName, String layerId, OrderCategory orderCategory,
            MetaDataInfo metadataInfo, Consumer<DataGroupInfo> deleteListener, DataGroupAndTypeFactory factory)
    {
        DataGroupInfo group = myRootGroup.getGroupById(folderName);
        GroupService groupService = null;
        if (group == null)
        {
            groupService = createGroup(folderName, deleteListener, factory);
            group = groupService.getGroup();
        }

        DataTypeInfo dataType = group.getMemberById(layerId, false);
        if (dataType == null)
        {
            dataType = newDataType(layerName, layerId, orderCategory, metadataInfo, factory);
            GroupService theGroupService = groupService != null ? groupService : (GroupService)getDynamicService(folderName);
            TypeService typeService = new TypeService(myToolbox, group, dataType);
            if (groupService == null)
            {
                typeService.open();
            }
            theGroupService.addService(typeService);
        }

        if (groupService != null)
        {
            addDynamicService(folderName, groupService);
        }

        return group;
    }

    /**
     * Creates a group service.
     *
     * @param folderName the folder name
     * @param deleteListener An object wanting notification when the group is
     *            deleted. This can be null if the layer can not be deleted by
     *            the user.
     * @param factory Instantiates new {@link DefaultDataGroupInfo} and
     *            {@link DefaultDataTypeInfo} or child classes of those.
     * @return the group service
     */
    public GroupService createGroup(String folderName, Consumer<DataGroupInfo> deleteListener, DataGroupAndTypeFactory factory)
    {
        DataGroupInfo group = newDataGroup(folderName, deleteListener, factory);
        group.activationProperty().addListener(myActivationListener);
        GroupService groupService = new GroupService(myRootGroup, group);
        return groupService;
    }

    /**
     * Creates a data type service.
     *
     * @param layerName the layer name
     * @param id the layer ID
     * @param orderCategory the order category
     * @param metaDataInfo If a feature layer this is required, null if a tile
     *            layer.
     * @param factory Instantiates new {@link DefaultDataGroupInfo} and
     *            {@link DefaultDataTypeInfo} or child classes of those.
     * @param groupService the group service
     * @return the data type
     */
    public TypeService createType(String layerName, String id, OrderCategory orderCategory, MetaDataInfo metaDataInfo,
            DataGroupAndTypeFactory factory, GroupService groupService)
    {
        DataTypeInfo dataType = newDataType(layerName, id, orderCategory, metaDataInfo, factory);
        TypeService typeService = new TypeService(myToolbox, groupService.getGroup(), dataType);
        groupService.addService(typeService);
        return typeService;
    }

    /**
     * Removes a layer.
     *
     * @param id the unique layer ID (same as group ID)
     */
    public void removeLayer(String id)
    {
        removeDynamicService(id);
    }

    /**
     * Creates a root group.
     *
     * @return The root group.
     */
    protected DefaultDataGroupInfo createRootGroup()
    {
        return new DefaultDataGroupInfo(true, myToolbox, myProviderType, myProviderType);
    }

    /**
     * Handles group de/activation.
     *
     * @param activationProperty the activation property
     * @param state the activation state
     * @param canceller the canceller
     */
    protected abstract void handleGroupActivation(DataGroupActivationProperty activationProperty, ActivationState state,
            PhasedTaskCanceller canceller);

    /**
     * Creates a new data group.
     *
     * @param folderName the folder name
     * @param deleteListener An object wanting notification when the group is
     *            deleted. This can be null if the layer can not be deleted by
     *            the user.
     * @param factory Instantiates new {@link DefaultDataGroupInfo} and
     *            {@link DefaultDataTypeInfo} or child classes of those.
     * @return the data group
     */
    protected DataGroupInfo newDataGroup(String folderName, Consumer<DataGroupInfo> deleteListener,
            DataGroupAndTypeFactory factory)
    {
        return factory.createGroup(myToolbox, myProviderType, folderName, deleteListener);
    }

    /**
     * Creates a new data type.
     *
     * @param layerName the layer name
     * @param id the layer ID
     * @param orderCategory the order category
     * @param metaDataInfo If a feature layer this is required, null if a tile
     *            layer.
     * @param factory Instantiates new {@link DefaultDataGroupInfo} and
     *            {@link DefaultDataTypeInfo} or child classes of those.
     * @return the data type
     */
    protected DataTypeInfo newDataType(String layerName, String id, OrderCategory orderCategory, MetaDataInfo metaDataInfo,
            DataGroupAndTypeFactory factory)
    {
        DefaultDataTypeInfo dataType = factory.createType(myToolbox, myProviderType, id, layerName, layerName);
        if (orderCategory == DefaultOrderCategory.FEATURE_CATEGORY)
        {
            dataType.setBasicVisualizationInfo(new DefaultBasicVisualizationInfo(LoadsTo.TIMELINE,
                    DefaultBasicVisualizationInfo.LOADS_TO_STATIC_AND_TIMELINE, Color.ORANGE, true));
            dataType.applyColorPreferences();

            dataType.setMapVisualizationInfo(new DefaultMapFeatureVisualizationInfo(MapVisualizationType.POINT_ELEMENTS));

            dataType.setOrderKey(new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY, orderCategory,
                    dataType.getTypeKey()));
            dataType.setMetaDataInfo(metaDataInfo);
        }
        else if (orderCategory == DefaultOrderCategory.IMAGE_DATA_CATEGORY)
        {
            dataType.setBasicVisualizationInfo(new DefaultBasicVisualizationInfo(LoadsTo.TIMELINE,
                    DefaultBasicVisualizationInfo.LOADS_TO_BASE_AND_TIMELINE, Color.WHITE, false));
            dataType.applyColorPreferences();

            TileRenderProperties props = new DefaultTileRenderProperties(0, true, false);
            float opacity = myMantleToolbox.getDataTypeInfoPreferenceAssistant().getOpacityPreference(dataType.getTypeKey(),
                    ColorUtilities.COLOR_COMPONENT_MAX_VALUE) / (float)ColorUtilities.COLOR_COMPONENT_MAX_VALUE;
            props.setOpacity(opacity);
            dataType.setMapVisualizationInfo(new DefaultMapTileVisualizationInfo(MapVisualizationType.IMAGE_TILE, props, false));

            dataType.setOrderKey(new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY, orderCategory,
                    dataType.getTypeKey()));
        }
        return dataType;
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    protected Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Gets the mantleToolbox.
     *
     * @return the mantleToolbox
     */
    public MantleToolbox getMantleToolbox()
    {
        return myMantleToolbox;
    }

    /**
     * Gets the providerType.
     *
     * @return the providerType
     */
    protected String getProviderType()
    {
        return myProviderType;
    }

    /**
     * Gets the rootGroup.
     *
     * @return the rootGroup
     */
    public DefaultDataGroupInfo getRootGroup()
    {
        return myRootGroup;
    }
}
