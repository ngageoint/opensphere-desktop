package io.opensphere.wms.state.activate.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.Event;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.TileLevelController;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.wms.config.v1.WMSBoundingBoxConfig;
import io.opensphere.wms.config.v1.WMSInheritedLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig.LayerType;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;
import io.opensphere.wms.config.v1.WMSServerConfig;
import io.opensphere.wms.envoy.WMSEnvoy;
import io.opensphere.wms.envoy.WMSLayerCreator;
import io.opensphere.wms.layer.WMSDataType;
import io.opensphere.wms.state.model.Parameters;
import io.opensphere.wms.state.model.WMSEnvoyAndLayerEnvoy;
import io.opensphere.wms.state.model.WMSEnvoyAndState;
import io.opensphere.wms.state.model.WMSLayerState;

/**
 * Tests the layer builder class.
 *
 */
public class LayerBuilderTest
{
    /**
     * The minimum display size in the state.
     */
    private static final int ourMinimumDisplaySize = 100;

    /**
     * The maximum display size in the state.
     */
    private static final int ourMaximumDisplaySize = 101;

    /**
     * The hold level in the state.
     */
    private static final int ourHoldLevel = 12;

    /**
     * The minimum display size in the state.
     */
    private static final String ourServerId = "testServer";

    /**
     * The original layer key.
     */
    private static final String ourLayerKey = "layerKey";

    /**
     * The state id.
     */
    private static final String ourStateId = "state-1";

    /**
     * The original layer display name.
     */
    private static final String ourDisplayName = "layer";

    /**
     * The url in the state.
     */
    private static final String ourUrl = "url";

    /**
     * The get map url in the state.
     */
    private static final String ourGetMapUrl = "getMapUrl";

    /**
     * The split levels in the state.
     */
    private static final int ourSplitLevels = 7;

    /**
     * The layer type in the state.
     */
    private static final LayerType ourLayerType = LayerType.General;

    /**
     * The background color in the state.
     */
    private static final String ourBgColor = "color";

    /**
     * The custom parameters in the state.
     */
    private static final String ourCustom = "custom";

    /**
     * The image format in the state.
     */
    private static final String ourFormat = "format";

    /**
     * The texture height in the state.
     */
    private static final int ourHeight = 200;

    /**
     * The texture width in the state.
     */
    private static final int ourWidth = 150;

    /**
     * The srs in the state.
     */
    private static final String ourSrs = "srs";

    /**
     * The style in the state.
     */
    private static final String ourStyle = "style";

    /**
     * The transparency in the state.
     */
    private static final boolean ourTransparent = true;

    /**
     * The original group id.
     */
    private static final String ourGroupId = "groupId";

    /**
     * The layer name.
     */
    private static final String ourLayerName = "Layer";

    /**
     * The tags to add to the layer.
     */
    private static final String ourTag = "tagify";

    /**
     * Tests building layers.
     */
    @Test
    public void testBuildLayers()
    {
        EventManager eventManager = createEventManager();
        Toolbox toolbox = createToolbox(eventManager);
        TileLevelController tlc = createTlc();
        DataTypeInfo dti = createDTI();
        MapVisualizationInfo visInfo = createMapVisInfo(tlc, dti);
        WMSDataType newType = createNewType(visInfo, toolbox, null);
        WMSLayerCreator creator = createCreator(newType);
        Preferences preferences = createPreferences();
        WMSLayerConfigurationSet config = createConfig();
        WMSDataTypeFactory factory = createFactory(toolbox, preferences, config, newType);
        ServerConnectionParams params = createServerConfig();
        WMSEnvoy envoy = createEnvoy(params);
        DataGroupInfo stateGroup = createStateGroup(newType);
        DataGroupInfo parentGroup = createParent(stateGroup);
        DataGroupInfo dataGroup = createDataGroup(parentGroup);
        WMSDataType existingType = createExistingType(config, toolbox, preferences, dataGroup);
        WMSLayerState state = createState(false);
        WMSEnvoyAndState envoyAndLayer = createEnvoyAndLayer(envoy, existingType, state);

        EasyMock.replay(toolbox, tlc, visInfo, newType, creator, preferences, factory, envoy, parentGroup, dataGroup,
                existingType, eventManager, stateGroup, params);

        LayerBuilder builder = new LayerBuilder(creator, factory);
        List<WMSEnvoyAndLayerEnvoy> layerAndEnvoy = builder.buildLayers(New.list(envoyAndLayer), ourStateId, New.list(ourTag));
        assertEquals(1, layerAndEnvoy.size());
        assertEquals(envoy, layerAndEnvoy.get(0).getEnvoy());

        EasyMock.verify(toolbox, tlc, visInfo, newType, creator, preferences, factory, envoy, parentGroup, dataGroup,
                existingType, eventManager, stateGroup, params);
    }

    /**
     * Tests building layers with inputs expected from 2d.
     */
    @Test
    public void testBuildLayersFrom2d()
    {
        EventManager eventManager = createEventManager();
        Toolbox toolbox = createToolbox(eventManager);
        TileLevelController tlc = createTlc();
        DataTypeInfo dti = createDTI();
        MapVisualizationInfo visInfo = createMapVisInfo(tlc, dti);
        WMSDataType newType = createNewType(visInfo, toolbox, null);
        WMSLayerCreator creator = createCreator(newType);
        Preferences preferences = createPreferences();
        WMSLayerConfigurationSet config = createConfig();
        modifyConfigToContainDefaults(config.getLayerConfig());
        WMSDataTypeFactory factory = createFactory(toolbox, preferences, config, newType);
        ServerConnectionParams params = createServerConfig();
        WMSEnvoy envoy = createEnvoy(params);
        DataGroupInfo stateGroup = createStateGroup(newType);
        DataGroupInfo parentGroup = createParent(stateGroup);
        DataGroupInfo dataGroup = createDataGroup(parentGroup);
        WMSDataType existingType = createExistingType(config, toolbox, preferences, dataGroup);
        WMSLayerState state = createState(true);
        modifyStateToMatchWeb(state);
        WMSEnvoyAndState envoyAndLayer = createEnvoyAndLayer(envoy, existingType, state);

        EasyMock.replay(toolbox, tlc, visInfo, newType, creator, preferences, factory, envoy, parentGroup, dataGroup,
                existingType, eventManager, stateGroup, params);

        LayerBuilder builder = new LayerBuilder(creator, factory);
        List<WMSEnvoyAndLayerEnvoy> layerAndEnvoy = builder.buildLayers(New.list(envoyAndLayer), ourStateId, New.list(ourTag));
        assertEquals(1, layerAndEnvoy.size());
        assertEquals(envoy, layerAndEnvoy.get(0).getEnvoy());

        EasyMock.verify(toolbox, tlc, visInfo, newType, creator, preferences, factory, envoy, parentGroup, dataGroup,
                existingType, eventManager, stateGroup, params);
    }

    /**
     * Creates the configuration.
     *
     * @return The configuration.
     */
    private WMSLayerConfigurationSet createConfig()
    {
        WMSLayerConfig layerConfig = new WMSLayerConfig();
        layerConfig.setLayerName(ourLayerName);
        layerConfig.setBoundingBoxConfig(new WMSBoundingBoxConfig());
        WMSLayerConfigurationSet config = new WMSLayerConfigurationSet(new WMSServerConfig(), layerConfig,
                new WMSInheritedLayerConfig());

        return config;
    }

    /**
     * Creates the layer creator.
     *
     * @param newType The new type returned by the creator.
     * @return The layer creator.
     */
    private WMSLayerCreator createCreator(WMSDataType newType)
    {
        WMSLayerCreator creator = EasyMock.createMock(WMSLayerCreator.class);
        creator.createLayer(EasyMock.eq(newType), EasyMock.eq(ourMinimumDisplaySize), EasyMock.eq(ourMaximumDisplaySize));
        EasyMock.expectLastCall().andReturn(null);

        return creator;
    }

    /**
     * Create the original data group.
     *
     * @param parent The parent data group.
     * @return The original data group.
     */
    private DataGroupInfo createDataGroup(DataGroupInfo parent)
    {
        DataGroupInfo dataGroup = EasyMock.createMock(DataGroupInfo.class);
        dataGroup.getParent();
        EasyMock.expectLastCall().andReturn(parent);
        dataGroup.getId();
        EasyMock.expectLastCall().andReturn(ourGroupId);

        return dataGroup;
    }

    /**
     * Create a DTI for use by the layer builder.
     *
     * @return The newly created DTI.
     */
    private DataTypeInfo createDTI()
    {
        BasicVisualizationInfo visInfo = EasyMock.createMock(BasicVisualizationInfo.class);
        visInfo.setTypeOpacity(EasyMock.anyInt(), EasyMock.anyObject());
        EasyMock.expectLastCall();

        DataTypeInfo dti = EasyMock.createMock(DataTypeInfo.class);
        dti.getBasicVisualizationInfo();
        EasyMock.expectLastCall().andReturn(visInfo);

        EasyMock.replay(visInfo, dti);
        return dti;
    }

    /**
     * Creates the original server envoy.
     *
     * @param params The params returned by the envoy.
     * @return The server envoy.
     */
    private WMSEnvoy createEnvoy(ServerConnectionParams params)
    {
        WMSEnvoy envoy = EasyMock.createMock(WMSEnvoy.class);
        envoy.getActiveLayerChangeListener();
        EasyMock.expectLastCall().andReturn(EasyMock.createNiceMock(ActivationListener.class));
        envoy.getServerConnectionConfig();
        EasyMock.expectLastCall().andReturn(params);
        envoy.getWMSVersion();
        EasyMock.expectLastCall().andReturn("1.1.1");

        return envoy;
    }

    /**
     * Creates the envoy and state object.
     *
     * @param envoy The envoy.
     * @param existingType The type.
     * @param state The state.
     * @return The envoy, type, and state.
     */
    private WMSEnvoyAndState createEnvoyAndLayer(WMSEnvoy envoy, WMSDataType existingType, WMSLayerState state)
    {
        WMSEnvoyAndState envoyState = new WMSEnvoyAndState(envoy, existingType, state, ourLayerName);

        return envoyState;
    }

    /**
     * Creates the event manager.
     *
     * @return The event manager.
     */
    private EventManager createEventManager()
    {
        EventManager eventManager = EasyMock.createMock(EventManager.class);
        eventManager.publishEvent(EasyMock.isA(Event.class));
        EasyMock.expectLastCall().anyTimes();

        return eventManager;
    }

    /**
     * Creates the existing data type.
     *
     * @param wmsConfig The configuration.
     * @param toolbox The toolbox.
     * @param prefs The preferences.
     * @param dataGroup The original data group.
     * @return The existing data type.
     */
    private WMSDataType createExistingType(WMSLayerConfigurationSet wmsConfig, Toolbox toolbox, Preferences prefs,
            DataGroupInfo dataGroup)
    {
        WMSDataType dataType = EasyMock.createMock(WMSDataType.class);

        dataType.getWmsConfig();
        EasyMock.expectLastCall().andReturn(wmsConfig).times(2);
        dataType.getDisplayName();
        EasyMock.expectLastCall().andReturn(ourDisplayName);
        dataType.getToolbox();
        EasyMock.expectLastCall().andReturn(toolbox);
        dataType.getPrefs();
        EasyMock.expectLastCall().andReturn(prefs);
        dataType.getSourcePrefix();
        EasyMock.expectLastCall().andReturn(ourServerId);
        dataType.getParent();
        EasyMock.expectLastCall().andReturn(dataGroup);
        dataType.getUrl();
        EasyMock.expectLastCall().andReturn(ourUrl);

        return dataType;
    }

    /**
     * Creates the data type factory.
     *
     * @param toolbox The toolbox.
     * @param preferences The preferences.
     * @param wmsConfig The configuration.
     * @param wmsDataType The new data type.
     * @return The data type factory.
     */
    private WMSDataTypeFactory createFactory(Toolbox toolbox, Preferences preferences, final WMSLayerConfigurationSet wmsConfig,
            final WMSDataType wmsDataType)
    {
        WMSDataTypeFactory factory = EasyMock.createMock(WMSDataTypeFactory.class);
        factory.createDataType(EasyMock.eq(toolbox), EasyMock.eq(preferences), EasyMock.cmpEq(ourServerId),
                EasyMock.isA(WMSLayerConfigurationSet.class),
                EasyMock.cmpEq(ourLayerKey + "!!" + ourLayerName + "!!" + ourStateId),
                EasyMock.cmpEq(ourDisplayName + " (" + ourStateId + ")"), EasyMock.cmpEq(ourUrl));
        EasyMock.expectLastCall().andAnswer(new IAnswer<WMSDataType>()
        {
            @Override
            public WMSDataType answer()
            {
                WMSLayerConfigurationSet config = (WMSLayerConfigurationSet)EasyMock.getCurrentArguments()[3];
                assertFalse(wmsConfig.equals(config));
                assertNull(config.getLayerConfig().getFixedHeight());
                assertNull(config.getLayerConfig().getFixedWidth());
                assertEquals(ourLayerName, config.getLayerConfig().getLayerName());
                assertEquals(ourGetMapUrl, config.getLayerConfig().getGetMapConfig().getGetMapURL());
                assertEquals(ourSplitLevels, config.getLayerConfig().getDisplayConfig().getResolveLevels().intValue());
                assertEquals(ourLayerType, config.getLayerConfig().getLayerType());
                assertEquals(ourBgColor, config.getLayerConfig().getGetMapConfig().getBGColor());
                assertEquals(ourCustom, config.getLayerConfig().getGetMapConfig().getCustomParams());
                assertEquals(ourFormat, config.getLayerConfig().getGetMapConfig().getImageFormat());
                assertEquals(ourHeight, config.getLayerConfig().getGetMapConfig().getTextureHeight().intValue());
                assertEquals(ourWidth, config.getLayerConfig().getGetMapConfig().getTextureWidth().intValue());
                assertEquals(ourSrs, config.getLayerConfig().getGetMapConfig().getSRS());
                assertEquals(ourStyle, config.getLayerConfig().getGetMapConfig().getStyle());
                assertEquals(ourTransparent, config.getLayerConfig().getGetMapConfig().getTransparent());

                return wmsDataType;
            }
        });

        return factory;
    }

    /**
     * Creates the map visualization info.
     *
     * @param tlc The tile level controller the vis info returns.
     * @param dti The data type info returned by the created vis info.
     * @return The info.
     */
    private MapVisualizationInfo createMapVisInfo(TileLevelController tlc, DataTypeInfo dti)
    {
        MapVisualizationInfo visInfo = EasyMock.createMock(MapVisualizationInfo.class);
        visInfo.getTileLevelController();
        EasyMock.expectLastCall().andReturn(tlc);
        visInfo.getDataTypeInfo();
        EasyMock.expectLastCall().andReturn(dti);

        return visInfo;
    }

    /**
     * Creates the new data type.
     *
     * @param visInfo The visualization info returned by the type.
     * @param toolbox The toolbox returned by the type.
     * @param basic The basic visualization info returned by the type.
     * @return The new data type.
     */
    private WMSDataType createNewType(MapVisualizationInfo visInfo, Toolbox toolbox, BasicVisualizationInfo basic)
    {
        WMSDataType newType = EasyMock.createMock(WMSDataType.class);
        newType.addTag(EasyMock.cmpEq(ourTag), EasyMock.isA(LayerBuilder.class));
        newType.setVisible(EasyMock.eq(true), EasyMock.isA(LayerBuilder.class));
        newType.getMapVisualizationInfo();
        EasyMock.expectLastCall().andReturn(visInfo);
        newType.getToolbox();
        EasyMock.expectLastCall().andReturn(toolbox);

        if (basic != null)
        {
            newType.getBasicVisualizationInfo();
            EasyMock.expectLastCall().andReturn(basic);
        }

        return newType;
    }

    /**
     * Creates the parent data group.
     *
     * @param dataGroup The state data group.
     * @return The parent group.
     */
    private DataGroupInfo createParent(DataGroupInfo dataGroup)
    {
        DataGroupInfo parent = EasyMock.createMock(DataGroupInfo.class);
        parent.getChildren();
        EasyMock.expectLastCall().andReturn(New.list(dataGroup));

        return parent;
    }

    /**
     * Creates the preferences.
     *
     * @return The preferences.
     */
    private Preferences createPreferences()
    {
        Preferences preferences = EasyMock.createMock(Preferences.class);

        return preferences;
    }

    /**
     * Create the server config.
     *
     * @return The server config.
     */
    private ServerConnectionParams createServerConfig()
    {
        ServerConnectionParams params = EasyMock.createNiceMock(ServerConnectionParams.class);

        return params;
    }

    /**
     * Creates the state object for testing.
     *
     * @param isAnimate Sets the is animate in the state.
     * @return The state.
     */
    private WMSLayerState createState(boolean isAnimate)
    {
        WMSLayerState state = new WMSLayerState();

        state.setGetMapUrl(ourGetMapUrl);
        state.setHoldLevel(ourHoldLevel);
        state.setId(ourLayerKey);
        state.setMaxDisplaySize(ourMaximumDisplaySize);
        state.setMinDisplaySize(ourMinimumDisplaySize);
        state.setSplitLevels(ourSplitLevels);
        state.setType(ourLayerType.toString());
        state.setVisible(true);
        state.setIsAnimate(isAnimate);

        Parameters parameters = state.getParameters();

        parameters.setBgColor(ourBgColor);
        parameters.setCustom(ourCustom);
        parameters.setFormat(ourFormat);
        parameters.setHeight(ourHeight);
        parameters.setWidth(ourWidth);
        parameters.setSrs(ourSrs);
        parameters.setStyle(ourStyle);
        parameters.setTransparent(ourTransparent);

        return state;
    }

    /**
     * Creates the data group pertaining to the state.
     *
     * @param dataType The data type expected to be added.
     * @return The state group.
     */
    private DataGroupInfo createStateGroup(DataTypeInfo dataType)
    {
        DataGroupInfo stateGroup = EasyMock.createMock(DataGroupInfo.class);
        stateGroup.getId();
        EasyMock.expectLastCall().andReturn(ourGroupId + "!!" + ourStateId);
        EasyMock.expect(stateGroup.activationProperty()).andReturn(new DataGroupActivationProperty(stateGroup));
        stateGroup.addMember(EasyMock.eq(dataType), EasyMock.isA(LayerBuilder.class));

        return stateGroup;
    }

    /**
     * Creates the tile level controller.
     *
     * @return The tile level controller.
     */
    private TileLevelController createTlc()
    {
        TileLevelController tlc = EasyMock.createMock(TileLevelController.class);
        tlc.setDivisionOverride(EasyMock.eq(true));
        tlc.setDivisionHoldGeneration(EasyMock.eq(ourHoldLevel));

        return tlc;
    }

    /**
     * Creates the toolbox.
     *
     * @param eventManager The event manager.
     * @return The toolbox.
     */
    private Toolbox createToolbox(EventManager eventManager)
    {
        Toolbox toolbox = EasyMock.createMock(Toolbox.class);
        toolbox.getEventManager();
        EasyMock.expectLastCall().andReturn(eventManager);
        EasyMock.expectLastCall().anyTimes();

        return toolbox;
    }

    /**
     * Modifies the config to contain expected results.
     *
     * @param config The config to modify.
     */
    private void modifyConfigToContainDefaults(WMSLayerConfig config)
    {
        config.getGetMapConfig().setImageFormat(ourFormat);
        config.getGetMapConfig().setSRS(ourSrs);
        config.getDisplayConfig().setResolveLevels(ourSplitLevels);
        config.getGetMapConfig().setBGColor(ourBgColor);
        config.getGetMapConfig().setTextureHeight(ourHeight);
        config.getGetMapConfig().setTextureWidth(ourWidth);
        config.getGetMapConfig().setStyle(ourStyle);
        config.getGetMapConfig().setTransparent(ourTransparent);
    }

    /**
     * Modifies the state object so that it is similar to what we receive from
     * the web application.
     *
     * @param state The state to modify.
     */
    private void modifyStateToMatchWeb(WMSLayerState state)
    {
        state.setUrl(ourGetMapUrl);
        state.setGetMapUrl(null);
        state.setType(null);
        state.getParameters().setFormat(null);
        state.getParameters().setSrs(null);
        state.setSplitLevels(null);
        state.getParameters().setBgColor(null);
        state.getParameters().setHeight(null);
        state.getParameters().setWidth(null);
        state.getParameters().setStyle(null);
        state.getParameters().setTransparent(null);
    }
}
