package io.opensphere.wfs.state.save;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.LayerType;
import com.bitsys.fade.mist.state.v4.LayersType;
import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.Toolbox;
import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.core.modulestate.TagList;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.AbstractLOBFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.DynamicEllipseFeatureVisualization;
import io.opensphere.mantle.data.geom.style.impl.IconFeatureVisualizationStyle;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.toolbox.LayerConfiguration;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wfs.layer.WFSMetaDataInfo;
import io.opensphere.wfs.state.controllers.WFSNodeWriter;
import io.opensphere.wfs.state.model.WFSLayerState;
import io.opensphere.wfs.state.model.WFSStateParameters;

/**
 * Saves the state of WFS layers current visualization settings, including a
 * basic set and ellipse, icon, line of bearing, or spike states if applicable.
 */
public class WFSStateSaver
{
    /** The Mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /** The toolbox through which server state is accessed. */
    private final ServerToolbox myServerToolbox;

    /**
     * Get the color from the 6 character hex representation of an integer which
     * defines the color (RGB).
     *
     * @param colorString The string which represents the color
     * @return the Color
     */
    public static Color getColor(String colorString)
    {
        int colorInt = Integer.parseInt(colorString, 16);
        return new Color(colorInt);
    }

    /**
     * Gets the color from an object as a string. If it is a java.awt.Color, the
     * string returned will be the 6 character hex representation of the integer
     * which defines the color (RGB).
     *
     * @param obj the object to check in order to get a color.
     * @return the color
     */
    public static String getColorString(Object obj)
    {
        if (obj instanceof Color)
        {
            Color color = (Color)obj;
            return String.format("%06x", Integer.valueOf(color.getRGB() & 0xffffff));
        }
        else if (obj instanceof String)
        {
            return obj.toString();
        }
        return null;
    }

    /**
     * Instantiates a new wFS state saver.
     *
     * @param toolbox the toolbox
     */
    public WFSStateSaver(Toolbox toolbox)
    {
        myMantleToolbox = MantleToolboxUtils.getMantleToolbox(toolbox);
        myServerToolbox = ServerToolboxUtils.getServerToolbox(toolbox);
    }

    /**
     * Saves the state of currently active data groups.
     *
     * @param node the node to save the state to.
     * @param serverConfigs the server configs
     * @param dataGroups the set of active data groups to save state for.
     */
    public void saveState(Node node, Map<String, ServerConnectionParams> serverConfigs, Set<DataGroupInfo> dataGroups)
    {
        List<WFSLayerState> featureLayers = New.list();
        for (DataGroupInfo dgi : dataGroups)
        {
            for (DataTypeInfo dti : dgi.getMembers(false))
            {
                writeDataTypeInfoState(dti, featureLayers, serverConfigs);
            }
        }
        if (!featureLayers.isEmpty())
        {
            WFSNodeWriter.writeToNode(node, featureLayers);
        }
    }

    /**
     * Saves the state of currently active data groups.
     *
     * @param state the state object to save the state to.
     * @param serverConfigs the server configs
     * @param dataGroups the set of active data groups to save state for.
     */
    public void saveState(StateType state, Map<String, ServerConnectionParams> serverConfigs, Set<DataGroupInfo> dataGroups)
    {
        List<WFSLayerState> featureLayers = New.list();
        for (DataGroupInfo dgi : dataGroups)
        {
            for (DataTypeInfo dti : dgi.getMembers(false))
            {
                writeDataTypeInfoState(dti, featureLayers, serverConfigs);
            }
        }

        LayersType dataLayers = StateUtilities.getDataLayers(state);
        for (WFSLayerState layerState : featureLayers)
        {
            dataLayers.getLayer().add(convertLayerState(layerState));
        }
    }

    /**
     * Writes the state info from the specified data type into the stateLayers.
     *
     * @param dataType The data type to save state for.
     * @param stateLayers The list to add the state information to.
     * @param serverConfigs Map of server configs.
     */
    protected void writeDataTypeInfoState(DataTypeInfo dataType, List<WFSLayerState> stateLayers,
            Map<String, ServerConnectionParams> serverConfigs)
    {
        if (dataType instanceof WFSDataType)
        {
            WFSDataType wfsType = (WFSDataType)dataType;
            WFSLayerState wfsLayerState = new WFSLayerState();

            writeCommonAttributes(wfsLayerState, wfsType);

            String serverCategory = getType(wfsType, serverConfigs);
            if (serverCategory != null)
            {
                wfsLayerState.setType(serverCategory);
            }
            String serverId = getServerId(wfsType, serverConfigs);
            if (serverId != null)
            {
                wfsLayerState.setServerId(serverId);
            }

            // for (Entry<String, ServerConnectionParams> entry :
            // serverConfigs.entrySet())
            // {
            // if (entry.getKey().equalsIgnoreCase(wfsType.getUrl()))
            // {
            // entry.getValue().getServerCustomization().getSrsName();
            // }
            // }

            WFSStateParameters wfsParams = new WFSStateParameters();
            wfsParams.setTypeName(wfsType.getTypeName());
            wfsParams.setVersion(wfsType.getWFSVersion());
            wfsLayerState.setWFSParameters(wfsParams);

            wfsLayerState.getWFSParameters().setTypeName(wfsType.getTypeName());

            wfsLayerState.setId(wfsType.getTypeKey());
            wfsLayerState.setUrl(wfsType.getUrl() == null ? wfsType.getTypeKey() : wfsType.getUrl());

            wfsLayerState.setAnimate(wfsType.getBasicVisualizationInfo().getLoadsTo().isTimelineEnabled());

            WFSMetaDataInfo wfsMDI = (WFSMetaDataInfo)wfsType.getMetaDataInfo();
            List<String> disabledColumns = New.list();
            disabledColumns.addAll(wfsMDI.getDeselectedColumns());
            wfsLayerState.setDisabledColumns(disabledColumns);
            wfsLayerState.setDisableEmptyColumns(wfsMDI.isAutomaticallyDisableEmptyColumns());

            wfsLayerState.setLoadsTo(wfsType.getBasicVisualizationInfo().getLoadsTo());
            wfsLayerState.setMetaData(dataType.getMetaDataInfo());

            stateLayers.add(wfsLayerState);
        }
    }

    /**
     * Converts the layer state to a v4 layer.
     *
     * @param layerState the layer state
     * @return the v4 layer
     */
    protected LayerType convertLayerState(WFSLayerState layerState)
    {
        return SaveStateV3ToV4Translator.toLayerType(layerState);
    }

    /**
     * Add the basic visualization settings and the style types (ellipse, lob,
     * etc.) as necessary.
     *
     * @param wfsType The datatype of the WFS layer being saved.
     * @param wfsLayerState The model for saved WFS states.
     */
    private void addVisualizationStyle(DataTypeInfo wfsType, WFSLayerState wfsLayerState)
    {
        BasicStyleStateSaver basicStyleSaver = new BasicStyleStateSaver();
        wfsLayerState.setBasicFeatureStyle(basicStyleSaver.getBasicFeatureStyle());

        Set<VisualizationStyle> typeStyles = myMantleToolbox.getVisualizationStyleRegistry().getStyles(wfsType.getTypeKey());

        if (CollectionUtilities.hasContent(typeStyles))
        {
            for (VisualizationStyle visStyle : typeStyles)
            {
                // TODO if there are multiple styles, this will be a problem
                // since there is only one set of basic settings.
                basicStyleSaver.saveStyleParams(visStyle);
                if (visStyle instanceof DynamicEllipseFeatureVisualization)
                {
                    DynamicEllipseStyleStateSaver ellipseSaver = new DynamicEllipseStyleStateSaver();
                    wfsLayerState.setEllipseStyle(ellipseSaver.getEllipseStyle());
                    ellipseSaver.saveStyleParams(visStyle);
                }
                else if (visStyle instanceof IconFeatureVisualizationStyle)
                {
                    IconStyleStateSaver iconSaver = new IconStyleStateSaver();
                    wfsLayerState.setIconStyle(iconSaver.getIconStyle());
                    iconSaver.saveStyleParams(visStyle);
                }
                else if (visStyle instanceof AbstractLOBFeatureVisualizationStyle)
                {
                    LineOfBearingStateSaver lobSaver = new LineOfBearingStateSaver();
                    wfsLayerState.setLineOfBearingStyle(lobSaver.getLineOfBearingStyle());
                    lobSaver.saveStyleParams(visStyle);
                }
            }
        }

        MetaDataInfo wfsMDI = wfsType.getMetaDataInfo();
        BasicVisualizationInfo visInfo = wfsType.getBasicVisualizationInfo();
        basicStyleSaver.setPointColor(WFSStateSaver.getColorString(visInfo.getTypeColor()));
        basicStyleSaver.setPointOpacity(visInfo.getTypeOpacity());
        basicStyleSaver.setAltitudeColumn(wfsMDI.getAltitudeKey());
    }

    /**
     * Gets the server id associated with this data type.
     *
     * @param wfsType the wfs type
     * @param serverConfigs the server configs
     * @return the server id
     */
    private String getServerId(WFSDataType wfsType, Map<String, ServerConnectionParams> serverConfigs)
    {
        String serverId = null;
        for (Entry<String, ServerConnectionParams> entry : serverConfigs.entrySet())
        {
            if (wfsType.getUrl().equalsIgnoreCase(entry.getValue().getWfsUrl()))
            {
                serverId = entry.getValue().getServerId(OGCServerSource.WFS_SERVICE);
            }
        }
        return serverId;
    }

    /**
     * Gets the server category associated with this data type.
     *
     * @param wfsType the wfs type
     * @param serverConfigs the server configs
     * @return the type
     */
    private String getType(WFSDataType wfsType, Map<String, ServerConnectionParams> serverConfigs)
    {
        WFSLayerConfigurationManager layerConfigurationManager = myServerToolbox.getLayerConfigurationManager();
        for (Entry<String, ServerConnectionParams> entry : serverConfigs.entrySet())
        {
            if (wfsType.getUrl().equalsIgnoreCase(entry.getValue().getWfsUrl()))
            {
                ServerCustomization sc = entry.getValue().getServerCustomization();
                LayerConfiguration configuration = layerConfigurationManager
                        .getConfigurationFromCustomization(sc);
                if (configuration != null)
                {
                    return configuration.getName();
                }
            }
        }
        return null;
    }

    /**
     * Sets the attributes that are common to both a wfs save state and nrt save
     * state.
     *
     * @param wfsLayerState The state object.
     * @param wfsType The wfs data type.
     */
    protected void writeCommonAttributes(WFSLayerState wfsLayerState, DataTypeInfo wfsType)
    {
        wfsLayerState.setDisplayName(wfsType.getDisplayName());
        wfsLayerState.setVisible(wfsType.isVisible());
        addVisualizationStyle(wfsType, wfsLayerState);
        wfsLayerState.setTags(new TagList(wfsType.getTags()));
    }
}
