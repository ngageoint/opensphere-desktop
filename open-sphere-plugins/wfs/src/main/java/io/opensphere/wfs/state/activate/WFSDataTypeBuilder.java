package io.opensphere.wfs.state.activate;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleController;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.impl.DynamicEllipseFeatureVisualization;
import io.opensphere.mantle.data.geom.style.impl.DynamicLOBFeatureVisualization;
import io.opensphere.mantle.data.geom.style.impl.IconFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.PointFeatureVisualizationStyle;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.server.control.DefaultServerDataGroupInfo;
import io.opensphere.server.toolbox.LayerConfiguration;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.wfs.envoy.AbstractWFSEnvoy;
import io.opensphere.wfs.envoy.WFSEnvoy;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wfs.layer.WFSLayerColumnManager;
import io.opensphere.wfs.layer.WFSMetaDataInfo;
import io.opensphere.wfs.state.model.BasicFeatureStyle;
import io.opensphere.wfs.state.model.WFSLayerState;
import io.opensphere.wfs.state.save.BasicStyleStateSaver;
import io.opensphere.wfs.state.save.DynamicEllipseStyleStateSaver;
import io.opensphere.wfs.state.save.IconStyleStateSaver;
import io.opensphere.wfs.state.save.LineOfBearingStateSaver;
import io.opensphere.wfs.state.save.StyleStateSaver;
import io.opensphere.wfs.state.save.WFSStateSaver;
import io.opensphere.wfs.util.WFSConstants;

/**
 * Builds new WFSDataType from existing ones and adds them to the correct group.
 */
public class WFSDataTypeBuilder
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(WFSDataTypeBuilder.class);

    /** The Data group controller. */
    private final DataGroupController myDataGroupController;

    /** The Envoy. */
    private AbstractWFSEnvoy myEnvoy;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new wFS data type builder.
     *
     * @param toolbox the toolbox
     */
    public WFSDataTypeBuilder(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myDataGroupController = MantleToolboxUtils.getMantleToolbox(toolbox).getDataGroupController();
    }

    /**
     * Indicates if this data type should be activated or if it will be
     * activated by another state module.
     *
     * @param dataType The data type to activate.
     * @return True if this type should be activated by this plugin's state
     *         controller, false otherwise.
     */
    public boolean canActivate(WFSDataType dataType)
    {
        DataGroupInfo parent = dataType.getParent();

        return parent.getMembers(false).size() >= 2;
    }

    /**
     * Creates the WFS types.
     *
     * @param stateId the state id
     * @param theStates the states
     * @return the list
     */
    public List<WFSDataType> createWFSTypes(String stateId, Collection<? extends WFSLayerState> theStates)
    {
        List<WFSDataType> stateTypes = New.list();
        List<WFSLayerState> states = New.list(theStates);

        for (WFSLayerState state : states)
        {
            // At a minimum the following fields should be present in order to
            // attempt to load the state.
            if (state.getId() == null || state.getTypeKey() == null || state.getUrl() == null
                    || state.getBasicFeatureStyle() == null)
            {
                LOGGER.error("Skipping type creation because saved state is missing critical fields.");
                continue;
            }

            String typeKey = state.getTypeKey();
            DataTypeInfo toCopy = myDataGroupController.findMemberById(typeKey);
            if (toCopy != null)
            {
                Collection<WFSEnvoy> envoys = myToolbox.getEnvoyRegistry().getObjectsOfClass(WFSEnvoy.class);
                Iterator<WFSEnvoy> iter = envoys.iterator();
                while (iter.hasNext())
                {
                    WFSEnvoy anEnvoy = iter.next();
                    if (anEnvoy.getGetCapabilitiesURL().equals(toCopy.getUrl()))
                    {
                        myEnvoy = anEnvoy;
                        break;
                    }
                }

                stateTypes.add(createDataType(state.getId(), stateId, state, toCopy));
            }
        }

        return stateTypes;
    }

    /**
     * Gets the {@link DataGroupController}.
     *
     * @return The {@link DataGroupController}.
     */
    protected DataGroupController getDataGroupController()
    {
        return myDataGroupController;
    }

    /**
     * Adds the new type to the correct group.
     *
     * @param stateId the state id
     * @param info the info
     * @param newDti the new data type to add to the group.
     */
    private void addToGroup(String stateId, DataTypeInfo info, WFSDataType newDti)
    {
        DataGroupInfo groupInfo = info.getParent();
        DataGroupInfo groupParent = groupInfo.getParent();
        String groupKey = groupInfo.getId() + "!!" + stateId;

        DataGroupInfo group = null;
        for (DataGroupInfo child : groupParent.getChildren())
        {
            if (groupKey.equals(child.getId()))
            {
                group = child;
                break;
            }
        }

        if (group == null)
        {
            group = new DefaultServerDataGroupInfo(false, newDti.getToolbox(), groupKey);
            group.setDisplayName(groupInfo.getDisplayName() + " (" + stateId + ")", this);
            groupParent.addChild(group, this);
        }

        myEnvoy.registerState(group, newDti);
        group.addMember(newDti, this);
    }

    /**
     * Creates a data type based on parameters from existing types that
     * correspond to this layer.
     *
     * @param typeKey the type key
     * @param stateId the state id
     * @param state the state
     * @param info the data type info to use as a template
     * @return the 'cloned' WFS data type
     */
    private WFSDataType createDataType(String typeKey, String stateId, WFSLayerState state, DataTypeInfo info)
    {
        WFSDataType newDti = null;
        WFSLayerColumnManager columnManager = new WFSLayerColumnManager(myToolbox);
        if (info instanceof WFSDataType && info.getMetaDataInfo() instanceof WFSMetaDataInfo)
        {
            WFSDataType wfsDTI = (WFSDataType)info;

            // The metadata info for the new data type.
            WFSMetaDataInfo metaDataInfo = new WFSMetaDataInfo(myToolbox, columnManager);
            populateMetaData(metaDataInfo, wfsDTI, state);

            String stateIdSuffix = " (" + stateId + ")";
            String typeName = state.getId();
            if (state.getWFSParameters() != null)
            {
                typeName = state.getWFSParameters().getTypeNameNoNameSpace();
            }

            String displayName = (state.getDisplayName() == null ? typeName : state.getDisplayName()) + stateIdSuffix;
            String newTypeKey = typeKey + WFSConstants.LAYERNAME_SEPARATOR + stateId;

            String stateType = "nrt".equals(state.getType()) ? "wfs" : state.getType();
            LayerConfiguration configuration = ServerToolboxUtils.getServerToolbox(myToolbox).getLayerConfigurationManager()
                    .getConfigurationFromName(stateType);

            newDti = new WFSDataType(myToolbox, wfsDTI.getSourcePrefix(), newTypeKey, typeName, displayName, metaDataInfo,
                    configuration);
            metaDataInfo.setDataTypeInfo(newDti);

            newDti.setUrl(state.getUrl());
            newDti.setVisible(state.isVisible(), this);

            for (String tag : state.getTags().getTags())
            {
                newDti.addTag(tag, this);
            }
            newDti.setQueryable(wfsDTI.isQueryable());
            newDti.setMetaDataInfo(metaDataInfo);
            newDti.setOrderKey(wfsDTI.getOrderKey());
            newDti.setOutputFormat(wfsDTI.getOutputFormat());
            newDti.setLatBeforeLon(wfsDTI.isLatBeforeLon());
            newDti.setTimeExtents(wfsDTI.getTimeExtents(), this);
            newDti.setAnimationSensitive(wfsDTI.isAnimationSensitive());

            initializeVisualizations(stateId, state, newDti, wfsDTI);

            newDti.registerInUse(myDataGroupController, false);
        }

        return newDti;
    }

    /**
     * Populates the meta data.
     *
     * @param metaDataInfo the meta data
     * @param wfsDTI the WFS data type
     * @param state the WFS layer state
     */
    protected void populateMetaData(WFSMetaDataInfo metaDataInfo, WFSDataType wfsDTI, WFSLayerState state)
    {
        WFSMetaDataInfo origMDI = (WFSMetaDataInfo)wfsDTI.getMetaDataInfo();
        metaDataInfo.setDynamicTime(origMDI.isDynamicTime());
        metaDataInfo.setGeometryColumn(wfsDTI.getMetaDataInfo().getGeometryColumn());

        for (String col : state.getDisabledColumns())
        {
            metaDataInfo.addDeselectedColumn(col);
        }

        if (state.isDisableEmptyColumns())
        {
            metaDataInfo.setAutomaticallyDisableEmptyColumns(true);
        }

        // Copy the columns into the new data type.
        for (String colName : wfsDTI.getMetaDataInfo().getKeyNames())
        {
            if (wfsDTI.getMetaDataInfo().getSpecialTypeForKey(colName) != null)
            {
                metaDataInfo.setSpecialKey(colName, wfsDTI.getMetaDataInfo().getSpecialTypeForKey(colName), this);
            }
            metaDataInfo.addWFSKey(colName, wfsDTI.getMetaDataInfo().getKeyClassType(colName), this);
        }
        metaDataInfo.copyKeysToOriginalKeys();
    }

    /**
     * Set up the feature style and visualization settings with the values from
     * the saved state.
     *
     * @param stateId The id of the saved state which is being loaded.
     * @param state The saved state.
     * @param newDti The
     * @param wfsDTI The existing data type info which matches the data type key
     *            for the state which is being restored.
     */
    private void initializeVisualizations(String stateId, WFSLayerState state, WFSDataType newDti, WFSDataType wfsDTI)
    {
        BasicFeatureStyle basicFeatureStyle = state.getBasicFeatureStyle();
        addToGroup(stateId, wfsDTI, newDti);

        // Settings which go into the basic visualization info
        BasicVisualizationInfo basicVisInfo = wfsDTI.getBasicVisualizationInfo();
        DefaultBasicVisualizationInfo newVisInfo = new DefaultBasicVisualizationInfo(basicVisInfo.getLoadsTo(),
                basicVisInfo.getDefaultTypeColor(), basicVisInfo.usesDataElements());
        newVisInfo.setLoadsTo(state.isAnimate() ? LoadsTo.TIMELINE : LoadsTo.STATIC, this);
        newVisInfo.setTypeColor(WFSStateSaver.getColor(basicFeatureStyle.getPointColor()), this);
        newVisInfo.setTypeOpacity(basicFeatureStyle.getPointOpacity(), this);
        if (basicFeatureStyle.getAltitudeColumn() != null)
        {
            newDti.getMetaDataInfo().setAltitudeKey(basicFeatureStyle.getAltitudeColumn(), this);
        }

        // Settings for visualization support and custom styles
        Set<VisualizationStyleParameter> vspSet = New.set();
        Class<? extends MutableVisualizationStyle> styleClass;
        StyleStateSaver customStyleSaver = null;
        if (state.getEllipseStyle() != null)
        {
            styleClass = DynamicEllipseFeatureVisualization.class;
            customStyleSaver = new DynamicEllipseStyleStateSaver(state.getEllipseStyle());
        }
        else if (state.getIconStyle() != null)
        {
            styleClass = IconFeatureVisualizationStyle.class;
            customStyleSaver = new IconStyleStateSaver(state.getIconStyle());
        }
        else if (state.getLineOfBearingStyle() != null)
        {
            styleClass = DynamicLOBFeatureVisualization.class;
            customStyleSaver = new LineOfBearingStateSaver(state.getLineOfBearingStyle());
        }
        else
        {
            styleClass = PointFeatureVisualizationStyle.class;
        }

        VisualizationStyleController vsc = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleController();
        if (vsc != null)
        {
            vsc.setUseCustomStyleForDataType(newDti.getParent(), newDti, true, this);

            Class<? extends VisualizationSupport> featureClass = MapLocationGeometrySupport.class;
            MutableVisualizationStyle style = (MutableVisualizationStyle)vsc.getStyleForEditorWithConfigValues(styleClass,
                    featureClass, newDti.getParent(), newDti);

            BasicStyleStateSaver basicSaver = new BasicStyleStateSaver(basicFeatureStyle);
            vspSet.addAll(basicSaver.populateVisualizationStyle(style));
            if (customStyleSaver != null)
            {
                vspSet.addAll(customStyleSaver.populateVisualizationStyle(style));
            }

            style.setParameters(vspSet, VisualizationStyle.NO_EVENT_SOURCE);

            vsc.setSelectedStyleClass(style, featureClass, newDti.getParent(), newDti, this);
        }

        newDti.setBasicVisualizationInfo(newVisInfo);
        DefaultMapFeatureVisualizationInfo mapVisInfo = new DefaultMapFeatureVisualizationInfo(
                wfsDTI.getMapVisualizationInfo().getVisualizationType(), true);
        newDti.setMapVisualizationInfo(mapVisInfo);
        mapVisInfo.setZOrder(wfsDTI.getMapVisualizationInfo().getZOrder(), null);
    }
}
