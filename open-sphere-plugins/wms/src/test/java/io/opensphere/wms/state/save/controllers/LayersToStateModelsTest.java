package io.opensphere.wms.state.save.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.TileLevelController;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig.LayerType;
import io.opensphere.wms.config.v1.WMSLayerDisplayConfig;
import io.opensphere.wms.config.v1.WMSLayerGetMapConfig;
import io.opensphere.wms.layer.WMSLayerValueProvider;
import io.opensphere.wms.state.model.WMSLayerAndState;
import io.opensphere.wms.state.model.WMSLayerState;

/**
 * Tests the LayersToStateModels class.
 *
 */
public class LayersToStateModelsTest
{
    /**
     * Test converting layers to state models.
     */
    @Test
    public void testToStateModels()
    {
        List<VisualizationStyle> styles = createStyles();
        List<VisualizationStyleRegistry> registries = createRegistries(styles);
        MantleToolbox toolbox = createMantleToolbox(registries);

        List<TileLevelController> levelers = createLevelers();
        List<MapVisualizationInfo> mapVisInfos = createMapVisInfos(levelers);
        List<BasicVisualizationInfo> basics = createBasics();
        List<DataTypeInfo> dataTypeInfos = createTypeInfos(mapVisInfos, basics);
        List<WMSLayerValueProvider> layers = createLayers(dataTypeInfos);

        for (VisualizationStyle style : styles)
        {
            EasyMock.replay(style);
        }

        for (BasicVisualizationInfo basic : basics)
        {
            EasyMock.replay(basic);
        }

        for (VisualizationStyleRegistry registry : registries)
        {
            EasyMock.replay(registry);
        }

        EasyMock.replay(toolbox);

        for (TileLevelController leveler : levelers)
        {
            EasyMock.replay(leveler);
        }

        for (DataTypeInfo info : dataTypeInfos)
        {
            EasyMock.replay(info);
        }

        for (MapVisualizationInfo info : mapVisInfos)
        {
            EasyMock.replay(info);
        }

        for (WMSLayerValueProvider layer : layers)
        {
            EasyMock.replay(layer);
        }

        LayersToStateModels tested = new LayersToStateModels(toolbox);
        List<WMSLayerAndState> states = tested.toStateModels(layers);

        assertEquals(styles.size(), states.size());
        int index = 0;

        for (WMSLayerAndState layerAndState : states)
        {
            WMSLayerState state = layerAndState.getState();

            assertEquals(layers.get(index), layerAndState.getLayer());
            assertEquals("colorizeStyle" + index, state.getColorizeStyle());
            assertEquals("getMapUrl" + index, state.getGetMapUrl());
            assertEquals("layerName" + index, state.getParameters().getLayerName());

            if (index % 2 == 0)
            {
                assertEquals(10 + index, state.getHoldLevel().intValue());
                assertEquals(10 + index, state.getParameters().getHeight().intValue());
                assertEquals(11 + index, state.getParameters().getWidth().intValue());
                assertTrue(state.getParameters().isTransparent().booleanValue());
            }
            else
            {
                assertNull(state.getHoldLevel());
                assertNull(state.getParameters().getHeight());
                assertNull(state.getParameters().getWidth());
                assertNull(state.getParameters().isTransparent());
            }

            assertEquals("typeKey" + index, state.getId());
            assertEquals(1 + index, state.getMaxDisplaySize());
            assertEquals(2 + index, state.getMinDisplaySize());

            assertEquals("bgColor" + index, state.getParameters().getBgColor());
            assertEquals("custom" + index, state.getParameters().getCustom());
            assertEquals("format" + index, state.getParameters().getFormat());

            assertEquals("srs" + index, state.getParameters().getSrs());
            assertEquals("style" + index, state.getParameters().getStyle());
            assertEquals("title" + index, state.getTitle());
            assertEquals(index % 2 == 0, state.isAnimate());

            index++;
        }

        for (VisualizationStyle style : styles)
        {
            EasyMock.verify(style);
        }

        for (VisualizationStyleRegistry registry : registries)
        {
            EasyMock.verify(registry);
        }

        for (BasicVisualizationInfo basic : basics)
        {
            EasyMock.verify(basic);
        }

        EasyMock.verify(toolbox);

        for (TileLevelController leveler : levelers)
        {
            EasyMock.verify(leveler);
        }

        for (DataTypeInfo info : dataTypeInfos)
        {
            EasyMock.verify(info);
        }

        for (MapVisualizationInfo info : mapVisInfos)
        {
            EasyMock.verify(info);
        }

        for (WMSLayerValueProvider layer : layers)
        {
            EasyMock.verify(layer);
        }
    }

    /**
     * Creates easy mocked BasicVisualizationInfos.
     *
     * @return The list of infos.
     */
    private List<BasicVisualizationInfo> createBasics()
    {
        List<BasicVisualizationInfo> basics = New.list();

        for (int i = 0; i < 2; i++)
        {
            BasicVisualizationInfo basic = EasyMock.createMock(BasicVisualizationInfo.class);
            basic.getLoadsTo();
            if (i % 2 == 0)
            {
                EasyMock.expectLastCall().andReturn(LoadsTo.TIMELINE);
            }
            else
            {
                EasyMock.expectLastCall().andReturn(LoadsTo.BASE);
            }
            basic.getTypeOpacity();
            EasyMock.expectLastCall().andReturn(1);

            basics.add(basic);
        }

        return basics;
    }

    /**
     * Creates the WMSLayerConfig.
     *
     * @param index The index of the config.
     * @return The layer config.
     */
    private WMSLayerConfig createConfig(int index)
    {
        WMSLayerConfig config = new WMSLayerConfig();

        WMSLayerGetMapConfig mapConfig = new WMSLayerGetMapConfig();
        config.setGetMapConfig(mapConfig);

        WMSLayerDisplayConfig displayConfig = new WMSLayerDisplayConfig();
        config.setDisplayConfig(displayConfig);

        mapConfig.setStyle("style" + index);

        config.setFixedHeight(Integer.valueOf(index % 2));
        config.setFixedWidth(Integer.valueOf(index % 2));

        mapConfig.setGetMapURL("getMapUrl" + index);
        mapConfig.setGetMapURLOverride("getMapUrl" + index);

        config.setLayerName("layerName" + index);

        if (index % 2 == 0)
        {
            displayConfig.setResolveLevels(Integer.valueOf(4 + index));
            mapConfig.setTextureHeight(Integer.valueOf(10 + index));
            mapConfig.setTextureWidth(Integer.valueOf(11 + index));
            mapConfig.setTransparent(Boolean.TRUE);
        }

        if (index % 2 == 0)
        {
            config.setLayerType(LayerType.General);
        }
        else
        {
            config.setLayerType(LayerType.SRTM);
        }

        mapConfig.setBGColor("bgColor" + index);
        mapConfig.setCustomParams("custom" + index);
        mapConfig.setImageFormat("format" + index);
        mapConfig.setSRS("srs" + index);
        mapConfig.setStyle("style" + index);

        return config;
    }

    /**
     * Creates an easy mocked layer value provider.
     *
     * @param index The index of the provider.
     * @param dataTypeInfo The DataTypeInfos returned by the provider.
     * @return The layer value provider.
     */
    private WMSLayerValueProvider createLayer(int index, DataTypeInfo dataTypeInfo)
    {
        WMSLayerValueProvider provider = EasyMock.createMock(WMSLayerValueProvider.class);

        provider.getConfiguration();
        EasyMock.expectLastCall().andReturn(createConfig(index));
        EasyMock.expectLastCall().anyTimes();
        provider.getMaximumDisplaySize();
        EasyMock.expectLastCall().andReturn(Integer.valueOf(1 + index));
        provider.getMinimumDisplaySize();
        EasyMock.expectLastCall().andReturn(Integer.valueOf(2 + index));
        provider.getTypeInfo();
        EasyMock.expectLastCall().andReturn(dataTypeInfo);
        EasyMock.expectLastCall().anyTimes();

        return provider;
    }

    /**
     * Create the easy mocked WMSLayerValueProvider.
     *
     * @param dataTypeInfos The data type infos returned by the providers.
     * @return The list of layer value providers.
     */
    private List<WMSLayerValueProvider> createLayers(List<DataTypeInfo> dataTypeInfos)
    {
        List<WMSLayerValueProvider> layers = New.list();

        int index = 0;

        for (DataTypeInfo dataTypeInfo : dataTypeInfos)
        {
            layers.add(createLayer(index, dataTypeInfo));
            index++;
        }

        return layers;
    }

    /**
     * Creates an easy mocked TileLevelController.
     *
     * @param index The index of the controller.
     * @return The tile level controller.
     */
    private TileLevelController createLeveler(int index)
    {
        TileLevelController leveler = EasyMock.createMock(TileLevelController.class);
        leveler.isDivisionOverride();

        boolean divisionOverride = index % 2 == 0;
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(divisionOverride));

        if (divisionOverride)
        {
            leveler.getDivisionHoldGeneration();
            EasyMock.expectLastCall().andReturn(Integer.valueOf(10 + index));
        }

        return leveler;
    }

    /**
     * Creates easy mocked TileLevelControllers.
     *
     * @return The easy mocked tile level controllers.
     */
    private List<TileLevelController> createLevelers()
    {
        List<TileLevelController> levelers = New.list();

        levelers.add(createLeveler(0));
        levelers.add(createLeveler(1));

        return levelers;
    }

    /**
     * Creates an easy mocked mantle toolbox.
     *
     * @param registries The registries the mantle toolbox returns.
     * @return The easy mocked toolbox.
     */
    private MantleToolbox createMantleToolbox(List<VisualizationStyleRegistry> registries)
    {
        MantleToolbox toolbox = EasyMock.createMock(MantleToolbox.class);

        for (VisualizationStyleRegistry registry : registries)
        {
            toolbox.getVisualizationStyleRegistry();
            EasyMock.expectLastCall().andReturn(registry);
        }

        return toolbox;
    }

    /**
     * Creates an easy mocked MapVisualizationInfo.
     *
     * @param index The index of the info.
     * @param leveler The leveler the info returns.
     * @return The easy mocked vis info.
     */
    private MapVisualizationInfo createMapVisInfo(int index, TileLevelController leveler)
    {
        MapVisualizationInfo mapVisInfo = EasyMock.createMock(MapVisualizationInfo.class);
        mapVisInfo.getTileLevelController();
        EasyMock.expectLastCall().andReturn(leveler);

        return mapVisInfo;
    }

    /**
     * Creates the list of easy mocked MapVisualizationInfos.
     *
     * @param levelers The easy mocked levelers returned by the infos.
     * @return The list of infos.
     */
    private List<MapVisualizationInfo> createMapVisInfos(List<TileLevelController> levelers)
    {
        List<MapVisualizationInfo> mapVisInfos = New.list();

        int index = 0;
        for (TileLevelController leveler : levelers)
        {
            mapVisInfos.add(createMapVisInfo(index, leveler));
        }

        return mapVisInfos;
    }

    /**
     * Creates easy mocked VisualizationStyleRegistries.
     *
     * @param styles The easy mocked styles the registries return.
     * @return The easy mocked registries.
     */
    private List<VisualizationStyleRegistry> createRegistries(List<VisualizationStyle> styles)
    {
        List<VisualizationStyleRegistry> registries = New.list();

        int index = 0;
        for (VisualizationStyle style : styles)
        {
            registries.add(createRegistry(index, style));
            index++;
        }

        return registries;
    }

    /**
     * Create an easy mocked registry.
     *
     * @param index The index of the registry
     * @param style The easy mocked style the registry returns.
     * @return The easy mocked registry.
     */
    private VisualizationStyleRegistry createRegistry(int index, VisualizationStyle style)
    {
        VisualizationStyleRegistry registry = EasyMock.createMock(VisualizationStyleRegistry.class);

        registry.getStyle(EasyMock.eq(TileVisualizationSupport.class), EasyMock.cmpEq("typeKey" + index), EasyMock.eq(true));
        EasyMock.expectLastCall().andReturn(style);

        return registry;
    }

    /**
     * Creates easy mocked styles.
     *
     * @return The styles.
     */
    private List<VisualizationStyle> createStyles()
    {
        List<VisualizationStyle> styles = New.list();

        for (int i = 0; i < 2; i++)
        {
            VisualizationStyle style = EasyMock.createMock(VisualizationStyle.class);
            style.getStyleName();
            EasyMock.expectLastCall().andReturn("colorizeStyle" + i);
            styles.add(style);
        }

        return styles;
    }

    /**
     * Creates an easy mocked data type info.
     *
     * @param index The index of the data type info.
     * @param mapVisInfo The easy mocked MapVisualizationInfo returned by the
     *            data type.
     * @param basic The easy mocked BasicMapVisualization returned by the data
     *            type.
     * @return The data type info.
     */
    private DataTypeInfo createTypeInfo(int index, MapVisualizationInfo mapVisInfo, BasicVisualizationInfo basic)
    {
        DataTypeInfo dataType = EasyMock.createMock(DataTypeInfo.class);

        dataType.getUrl();
        EasyMock.expectLastCall().andReturn("url" + index);
        dataType.isVisible();
        EasyMock.expectLastCall().andReturn(Boolean.valueOf(index % 2 == 0));
        dataType.getTypeKey();
        EasyMock.expectLastCall().andReturn("typeKey" + index).times(2);

        dataType.getMapVisualizationInfo();
        EasyMock.expectLastCall().andReturn(mapVisInfo);

        dataType.getDisplayName();
        EasyMock.expectLastCall().andReturn("title" + index);

        dataType.getBasicVisualizationInfo();
        EasyMock.expectLastCall().andReturn(basic);
        EasyMock.expectLastCall().anyTimes();

        return dataType;
    }

    /**
     * Creates easy mocked data type infos.
     *
     * @param mapVisInfos The easy mocked MapVisualizationInfos returned by the
     *            data type infos.
     * @param basics The easy mocked BasicVisulizationInfos returned by the data
     *            type infos.
     * @return The data type infos.
     */
    private List<DataTypeInfo> createTypeInfos(List<MapVisualizationInfo> mapVisInfos, List<BasicVisualizationInfo> basics)
    {
        List<DataTypeInfo> dataTypes = New.list();

        int index = 0;

        for (MapVisualizationInfo mapVisInfo : mapVisInfos)
        {
            dataTypes.add(createTypeInfo(index, mapVisInfo, basics.get(index)));
            index++;
        }

        return dataTypes;
    }
}
