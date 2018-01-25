package io.opensphere.wms.state.activate.controllers;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.enums.EnumUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.TileLevelController;
import io.opensphere.server.control.DefaultServerDataGroupInfo;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig.LayerType;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;
import io.opensphere.wms.envoy.WMSEnvoy;
import io.opensphere.wms.envoy.WMSGetMapEnvoy;
import io.opensphere.wms.envoy.WMSLayerCreator;
import io.opensphere.wms.envoy.WMSLayerKey;
import io.opensphere.wms.layer.WMSDataType;
import io.opensphere.wms.layer.WMSLayer;
import io.opensphere.wms.state.model.Parameters;
import io.opensphere.wms.state.model.WMSEnvoyAndLayerEnvoy;
import io.opensphere.wms.state.model.WMSEnvoyAndState;
import io.opensphere.wms.state.model.WMSLayerState;

/**
 * Builds all necessary components for a valid layer using values saved from the
 * state objects.
 *
 */
public class LayerBuilder
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(LayerBuilder.class);

    /**
     * Responsible for creating layers.
     */
    private final WMSLayerCreator myLayerCreator;

    /**
     * Responsible for creating WMS data type.
     */
    private final WMSDataTypeFactory myFactory;

    /**
     * Constructs a new layer builder.
     *
     * @param layerCreator Responsible for instantiating new WMS layers.
     * @param factory Responsible for instantiating new WMS data types.
     */
    public LayerBuilder(WMSLayerCreator layerCreator, WMSDataTypeFactory factory)
    {
        myLayerCreator = layerCreator;
        myFactory = factory;
    }

    /**
     * Constructs WMS DataTypeInfo's using the values specified in layer states.
     *
     * @param layerStates The layer states.
     * @param stateId The state id.
     * @param tags Tags to append to the data types.
     * @return The built WMSEnvoy ready for activation.
     */
    public List<WMSEnvoyAndLayerEnvoy> buildLayers(Collection<? extends WMSEnvoyAndState> layerStates, String stateId,
            Collection<? extends String> tags)
    {
        List<WMSEnvoyAndLayerEnvoy> builtEnvoys = New.list();

        for (WMSEnvoyAndState envoyAndState : layerStates)
        {
            WMSEnvoy envoy = envoyAndState.getEnvoy();
            WMSLayerState state = envoyAndState.getState();
            WMSDataType existingWmsType = envoyAndState.getTypeInfo();
            String layerName = envoyAndState.getLayerName();

            WMSDataType newDataType = createNewDataType(existingWmsType, state, stateId, envoy, tags, layerName);

            WMSLayer newLayer = myLayerCreator.createLayer(newDataType, state.getMinDisplaySize(), state.getMaxDisplaySize());
            WMSGetMapEnvoy newEnvoy = new WMSGetMapEnvoy(newLayer, newDataType.getToolbox(), null,
                    envoy.getServerConnectionConfig(), envoy.getWMSVersion());

            WMSEnvoyAndLayerEnvoy envoyAndLayer = new WMSEnvoyAndLayerEnvoy(envoy, newEnvoy);

            builtEnvoys.add(envoyAndLayer);
        }

        return builtEnvoys;
    }

    /**
     * Applies the saved state parameters to the wms layer config.
     *
     * @param layerConfig The configuration.
     * @param parameters The state values.
     */
    private void applyParametersToConfig(WMSLayerConfig layerConfig, Parameters parameters)
    {
        if (StringUtils.isNotEmpty(parameters.getBgColor()))
        {
            layerConfig.getGetMapConfig().setBGColor(parameters.getBgColor());
        }

        layerConfig.getGetMapConfig().setCustomParams(parameters.getCustom());

        if (StringUtils.isNotEmpty(parameters.getFormat()))
        {
            layerConfig.getGetMapConfig().setImageFormat(parameters.getFormat());
        }

        if (parameters.getHeight() != null)
        {
            layerConfig.getGetMapConfig().setTextureHeight(parameters.getHeight());
        }

        if (parameters.getWidth() != null)
        {
            layerConfig.getGetMapConfig().setTextureWidth(parameters.getWidth());
        }

        if (StringUtils.isNotEmpty(parameters.getSrs()))
        {
            layerConfig.getGetMapConfig().setSRS(parameters.getSrs());
        }

        if (StringUtils.isNotEmpty(parameters.getStyle()))
        {
            layerConfig.getGetMapConfig().setStyle(parameters.getStyle());
        }

        if (parameters.isTransparent() != null)
        {
            layerConfig.getGetMapConfig().setTransparent(parameters.isTransparent());
        }
    }

    /**
     * Applies the state values to the configuration.
     *
     * @param config The config to change values for.
     * @param state The state to get values for.
     */
    private void applyStateToConfig(WMSLayerConfigurationSet config, WMSLayerState state)
    {
        WMSLayerConfig layerConfig = config.getLayerConfig();

        if (state.isFixedHeight())
        {
            layerConfig.setFixedHeight(null);
        }

        if (state.isFixedWidth())
        {
            layerConfig.setFixedWidth(null);
        }

        if (StringUtils.isNotEmpty(state.getGetMapUrl()))
        {
            layerConfig.getGetMapConfig().setGetMapURL(state.getGetMapUrl());
        }
        else
        {
            layerConfig.getGetMapConfig().setGetMapURL(state.getUrl());
        }

        if (state.getSplitLevels() != null)
        {
            layerConfig.getDisplayConfig().setResolveLevels(state.getSplitLevels());
        }

        if (StringUtils.isNotEmpty(state.getType()))
        {
            LayerType layerType = EnumUtilities.valueOf(LayerType.class, state.getType(), LayerType.General);
            layerConfig.setLayerType(layerType);
        }

        Parameters parameters = state.getParameters();

        applyParametersToConfig(layerConfig, parameters);
    }

    /**
     * Creates a new data using dataType as a foundation to create from.
     *
     * @param wmsDataType The data type to clone.
     * @param state The state to restore.
     * @param stateId The id of the state being restored, used to name the new
     *            layers.
     * @param serverEnvoy The get capabilities envoy for the server.
     * @param tags Tags to append to the new data type.
     * @param layerName The layer name
     * @return The new data type or null if it was unable to create.
     */
    private WMSDataType createNewDataType(WMSDataType wmsDataType, WMSLayerState state, String stateId, WMSEnvoy serverEnvoy,
            Collection<? extends String> tags, String layerName)
    {
        WMSDataType newType = null;

        WMSLayerConfigurationSet wmsConfig = wmsDataType.getWmsConfig();

        try
        {
            WMSLayerConfigurationSet config = wmsConfig.clone();
            String typeKey;
            if (!StringUtils.isBlank(state.getId()))
            {
                typeKey = StringUtilities.concat(state.getId(), WMSLayerKey.LAYERNAME_SEPARATOR, layerName,
                        WMSLayerKey.LAYERNAME_SEPARATOR, stateId);
            }
            else
            {
                typeKey = StringUtilities.concat(wmsDataType.getTypeKey(), WMSLayerKey.LAYERNAME_SEPARATOR, stateId);
            }

            config.getLayerConfig().setLayerKey(wmsDataType.getWmsConfig().getLayerConfig().getLayerKey());
            applyStateToConfig(config, state);

            String displayName = wmsDataType.getDisplayName() + " (" + stateId + ")";

            newType = myFactory.createDataType(wmsDataType.getToolbox(), wmsDataType.getPrefs(), wmsDataType.getSourcePrefix(),
                    config, typeKey, displayName, wmsDataType.getUrl());

            for (String tag : tags)
            {
                newType.addTag(tag, this);
            }

            newType.setVisible(state.isVisible(), this);

            MapVisualizationInfo mapVisInfo = newType.getMapVisualizationInfo();
            if (mapVisInfo != null)
            {
                if (state.getHoldLevel() != null)
                {
                    TileLevelController leveler = mapVisInfo.getTileLevelController();
                    if (leveler != null)
                    {
                        leveler.setDivisionOverride(true);
                        leveler.setDivisionHoldGeneration(state.getHoldLevel().intValue());
                    }
                }
                int opacity = (int)(state.getAlpha() * ColorUtilities.COLOR_COMPONENT_MAX_VALUE);
                mapVisInfo.getDataTypeInfo().getBasicVisualizationInfo().setTypeOpacity(opacity, this);
            }

            DataGroupInfo groupInfo = wmsDataType.getParent();
            DataGroupInfo groupParent = groupInfo.getParent();

            String groupKey = groupInfo.getId() + WMSLayerKey.LAYERNAME_SEPARATOR + stateId;

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
                group = new DefaultServerDataGroupInfo(false, newType.getToolbox(), groupKey);
                group.setDisplayName(groupInfo.getDisplayName() + " (" + stateId + ")", this);
                groupParent.addChild(group, this);
            }

            group.activationProperty().addListener(serverEnvoy.getActiveLayerChangeListener());
            group.addMember(newType, this);
        }
        catch (CloneNotSupportedException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return newType;
    }
}
