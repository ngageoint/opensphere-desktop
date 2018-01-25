package io.opensphere.wfs.state.activate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.SecurityManager;
import io.opensphere.core.SystemToolbox;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.metrics.MetricsRegistry;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.registry.GenericRegistry;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.server.control.DefaultServerDataGroupInfo;
import io.opensphere.server.control.ServerConnectionParamsImpl;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.state.StateConstants;
import io.opensphere.server.toolbox.LayerConfiguration;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;
import io.opensphere.server.util.ServerConstants;
import io.opensphere.wfs.WFSPlugin;
import io.opensphere.wfs.envoy.WFSEnvoy;
import io.opensphere.wfs.envoy.WFSToolbox;
import io.opensphere.wfs.envoy.WFSTools;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wfs.layer.WFSLayerColumnManager;
import io.opensphere.wfs.layer.WFSMapVisualizationInfo;
import io.opensphere.wfs.layer.WFSMetaDataInfo;
import io.opensphere.wfs.state.model.BasicFeatureStyle;
import io.opensphere.wfs.state.model.WFSLayerState;
import io.opensphere.wfs.state.model.WFSStateParameters;
import io.opensphere.wfs.util.WFSConstants;

/**
 * Tests building a WFSDataType from an existing WFSDataType.
 */
public class WFSDataTypeBuilderTest
{
    /** The Constant ourDataLayer1. */
    private static final String ourDataLayer1 = "dataLayer1";

    /** The Constant ourDataLayer2. */
    private static final String ourDataLayer2 = "dataLayer2";

    /**
     * The expected server protocol.
     */
    private static final String ourProtocol = "http://";

    /** The Constant ourServerTitle. */
    private static final String ourServerTitle = "somehost/ogc";

    /** The STAT e1. */
    private static final String STATE1 = "state1";

    /** The Layer1 URL. */
    private String myLaye1Url1;

    /** The Layer key1. */
    private String myLayerKey1;

    /** The Layer key2. */
    private String myLayerKey2;

    /** The Layer2 URL. */
    private String myLayerUrl2;

    /**
     * Test.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test()
    {
        List<WFSLayerState> states = New.list();
        states.add(createState1());
        states.add(createState2());

        Toolbox toolbox = EasyMock.createNiceMock(Toolbox.class);

        PreferencesRegistry preferencesRegistry = EasyMock.createNiceMock(PreferencesRegistry.class);
        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(preferencesRegistry).anyTimes();

        Preferences serverPrefs = EasyMock.createNiceMock(Preferences.class);
        EasyMock.expect(preferencesRegistry.getPreferences(ServerConstants.class)).andReturn(serverPrefs).anyTimes();

        Preferences dataTypePrefs = EasyMock.createNiceMock(Preferences.class);
        EasyMock.expect(preferencesRegistry.getPreferences(DefaultDataTypeInfo.class)).andReturn(dataTypePrefs).anyTimes();

        OrderManagerRegistry orderManagerRegistry = EasyMock.createNiceMock(OrderManagerRegistry.class);
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderManagerRegistry).anyTimes();
        OrderManager manager = EasyMock.createNiceMock(OrderManager.class);
        EasyMock.expect(orderManagerRegistry.getOrderManager(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY,
                DefaultOrderCategory.FEATURE_CATEGORY)).andReturn(manager).anyTimes();

        PluginToolboxRegistry toolboxRegistry = EasyMock.createNiceMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).anyTimes();

        DataRegistry dataRegistry = EasyMock.createNiceMock(DataRegistry.class);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry).anyTimes();

        MantleToolbox mantleToolbox = EasyMock.createNiceMock(MantleToolbox.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantleToolbox).anyTimes();

        DataTypeInfoPreferenceAssistant dtiPrefAssistant = EasyMock.createNiceMock(DataTypeInfoPreferenceAssistant.class);
        EasyMock.expect(mantleToolbox.getDataTypeInfoPreferenceAssistant()).andReturn(dtiPrefAssistant).anyTimes();

        DataGroupController dataGroupController = EasyMock.createNiceMock(DataGroupController.class);
        EasyMock.expect(mantleToolbox.getDataGroupController()).andReturn(dataGroupController).anyTimes();

        DataTypeController dataTypeController = EasyMock.createNiceMock(DataTypeController.class);
        EasyMock.expect(mantleToolbox.getDataTypeController()).andReturn(dataTypeController).anyTimes();

        VisualizationStyleRegistry visReg = EasyMock.createNiceMock(VisualizationStyleRegistry.class);
        EasyMock.expect(mantleToolbox.getVisualizationStyleRegistry()).andReturn(visReg).anyTimes();

        WFSToolbox wfsToolbox = EasyMock.createNiceMock(WFSToolbox.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(WFSToolbox.class))).andReturn(wfsToolbox).anyTimes();

        MutableVisualizationStyle style = EasyMock.createNiceMock(MutableVisualizationStyle.class);
        EasyMock.expect(visReg.getDefaultStyleInstanceForStyleClass(EasyMock.isA(Class.class))).andReturn(style).anyTimes();

        EventManager eventManager = EasyMock.createNiceMock(EventManager.class);
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager).anyTimes();

        MetricsRegistry metricsRegistry = EasyMock.createNiceMock(MetricsRegistry.class);
        EasyMock.expect(toolbox.getMetricsRegistry()).andReturn(metricsRegistry).anyTimes();

        MapManager mapManager = EasyMock.createNiceMock(MapManager.class);
        EasyMock.expect(toolbox.getMapManager()).andReturn(mapManager).anyTimes();

        UIRegistry uiRegistry = EasyMock.createNiceMock(UIRegistry.class);
        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiRegistry).anyTimes();

        SecurityManager secManager = EasyMock.createNiceMock(SecurityManager.class);
        EasyMock.expect(toolbox.getSecurityManager()).andReturn(secManager).anyTimes();

        SystemToolbox sysToolbox = EasyMock.createNiceMock(SystemToolbox.class);
        EasyMock.expect(toolbox.getSystemToolbox()).andReturn(sysToolbox).anyTimes();

        NetworkConfigurationManager ncm = EasyMock.createNiceMock(NetworkConfigurationManager.class);
        EasyMock.expect(sysToolbox.getNetworkConfigurationManager()).andReturn(ncm).anyTimes();

        ServerToolbox serverToolbox = EasyMock.createNiceMock(ServerToolbox.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(ServerToolbox.class))).andReturn(serverToolbox).anyTimes();

        MenuBarRegistry mbr = EasyMock.createNiceMock(MenuBarRegistry.class);
        EasyMock.expect(uiRegistry.getMenuBarRegistry()).andReturn(mbr).anyTimes();
        EasyMock.replay(uiRegistry);

        GenericRegistry<Envoy> envoyRegistry = new GenericRegistry<>();
        EasyMock.expect(toolbox.getEnvoyRegistry()).andReturn(envoyRegistry).anyTimes();

        ServerSourceControllerManager serverMgr = EasyMock.createNiceMock(ServerSourceControllerManager.class);
        EasyMock.expect(serverToolbox.getServerSourceControllerManager()).andReturn(serverMgr).anyTimes();

        WFSLayerConfigurationManager layerConfigurationManager = new WFSLayerConfigurationManager();
        EasyMock.expect(serverToolbox.getLayerConfigurationManager()).andReturn(layerConfigurationManager).anyTimes();

        ServerSourceController serverCtrl = EasyMock.createNiceMock(ServerSourceController.class);
        EasyMock.expect(serverMgr.getServerSourceController("serverType")).andReturn(serverCtrl).anyTimes();

        OGCServerSource serverSource = new OGCServerSource();
        serverSource.setName("source1");
        serverSource.setServerType("serverType");
        serverSource.setWFSServerURL(ourProtocol + ourServerTitle + "/wfsServer");

        EasyMock.replay(toolbox, toolboxRegistry, preferencesRegistry, sysToolbox, secManager, mapManager, ncm);
        EasyMock.replay(serverPrefs, dataTypePrefs, serverToolbox, serverMgr, serverCtrl, mbr);

        ServerConnectionParams scp = new ServerConnectionParamsImpl(serverSource, uiRegistry.getMainFrameProvider(), toolbox,
                null);

        WFSTools wfsTools = new WFSTools(toolbox);

        WFSEnvoy wfsEnvoy = new WFSEnvoy(toolbox, preferencesRegistry.getPreferences(WFSPlugin.class), scp, wfsTools);

        envoyRegistry.addObjectsForSource(this, Collections.singleton(wfsEnvoy));

        EasyMock.replay(manager);
        EasyMock.replay(eventManager);
        EasyMock.replay(metricsRegistry);
        EasyMock.replay(dtiPrefAssistant);
        EasyMock.replay(dataTypeController);
        EasyMock.replay(visReg);
        EasyMock.replay(style);
        EasyMock.replay(mantleToolbox, dataRegistry, orderManagerRegistry);

        // Create the types to clone
        WFSDataType type1 = createTypeToClone(toolbox, manager, ourServerTitle, myLayerKey1, ourDataLayer1, myLaye1Url1, "ALT1");
        WFSDataType type2 = createTypeToClone(toolbox, manager, ourServerTitle, myLayerKey2, ourDataLayer2, myLayerUrl2, "ALT2");

        DefaultServerDataGroupInfo rootGroup = new DefaultServerDataGroupInfo(false, toolbox, "rootGroup");

        DefaultDataGroupInfo group1 = new DefaultDataGroupInfo(false, toolbox, "WFSDataTypeBuilder", "layer1_group");
        group1.addMember(type1, this);

        DefaultDataGroupInfo group2 = new DefaultDataGroupInfo(false, toolbox, "WFSDataTypeBuilder", "layer2_group");
        group2.addMember(type2, this);

        Set<DataGroupInfo> members = New.set();
        members.add(group1);
        members.add(group2);

        rootGroup.addChild(group1, this);
        rootGroup.addChild(group2, this);

        EasyMock.expect(dataGroupController.findMemberById(type1.getTypeKey())).andReturn(type1).anyTimes();
        EasyMock.expect(dataGroupController.findMemberById(type2.getTypeKey())).andReturn(type2).anyTimes();

        EasyMock.replay(dataGroupController);

        dataGroupController.addRootDataGroupInfo(rootGroup, this);

        WFSDataTypeBuilder builder = new WFSDataTypeBuilder(toolbox);
        List<WFSDataType> types = builder.createWFSTypes(STATE1, states);

        assertEquals(2, types.size());

        Map<String, WFSDataType> expected = New.map();
        expected.put(type1.getDisplayName(), type1);
        expected.put(type2.getDisplayName(), type2);

        compare(expected.get(types.get(0).getDisplayName().split(" ")[0]), types.get(0));
        compare(expected.get(types.get(1).getDisplayName().split(" ")[0]), types.get(1));

        Map<String, WFSDataType> actualTypes = New.map();

        for (WFSDataType type : types)
        {
            actualTypes.put(type.getDisplayName().split(" ")[0], type);
        }
    }

    /**
     * Compares the original data type to the cloned data type.
     *
     * @param type1 the original data type
     * @param type2 the cloned data type
     */
    private void compare(WFSDataType type1, WFSDataType type2)
    {
        assertTrue(type2.getTypeKey().startsWith(type1.getTypeKey()));
        assertEquals(type1.getTypeName(), type2.getTypeName());
        assertEquals(type1.getDisplayName(), type2.getDisplayName().split(" ")[0]);
        assertEquals(type1.getSourcePrefix(), type2.getSourcePrefix());
        assertEquals(Boolean.valueOf(type1.isVisible()), Boolean.valueOf(type2.isVisible()));
        assertEquals(type1.getUrl(), type2.getUrl());
        assertEquals(Boolean.valueOf(type1.isQueryable()), Boolean.valueOf(type2.isQueryable()));
        assertEquals(type1.getOrderKey(), type2.getOrderKey());
        assertEquals(type1.getOutputFormat(), type2.getOutputFormat());
        assertEquals(Boolean.valueOf(type1.isLatBeforeLon()), Boolean.valueOf(type2.isLatBeforeLon()));
        assertEquals(type1.getTimeExtents(), type2.getTimeExtents());

        BasicVisualizationInfo type1BasicVisInfo = type1.getBasicVisualizationInfo();
        BasicVisualizationInfo newTypeBasicVisInfo = type2.getBasicVisualizationInfo();
        assertEquals(type1BasicVisInfo.getTypeColor(), newTypeBasicVisInfo.getTypeColor());
        assertEquals(type1BasicVisInfo.getTypeOpacity(), newTypeBasicVisInfo.getTypeOpacity());
        assertEquals(type1BasicVisInfo.getLoadsTo(), newTypeBasicVisInfo.getLoadsTo());

        WFSMetaDataInfo mdi = (WFSMetaDataInfo)type1.getMetaDataInfo();
        WFSMetaDataInfo newMdi = (WFSMetaDataInfo)type2.getMetaDataInfo();
        assertEquals(Boolean.valueOf(mdi.isDynamicTime()), Boolean.valueOf(newMdi.isDynamicTime()));
        // assertEquals(type1_mdi.getDeselectedColumns(),
        // newType1_mdi.getDeselectedColumns());
        // assertEquals(Boolean.valueOf(type1_mdi.automaticallyDisableEmptyColumns()),
        // Boolean.valueOf(newType1_mdi.automaticallyDisableEmptyColumns()));
        assertEquals(mdi.getGeometryColumn(), newMdi.getGeometryColumn());
        assertEquals(mdi.getAltitudeKey(), newMdi.getAltitudeKey());
    }

    /**
     * Creates the second state.
     *
     * @return the wFS layer state
     */
    private WFSLayerState createState1()
    {
        StringBuilder sb = new StringBuilder(17);
        sb.append(ourProtocol);
        sb.append(ourServerTitle);
        sb.append("/wfsServer");
        myLaye1Url1 = sb.toString();
        sb.append(WFSConstants.LAYERNAME_SEPARATOR);
        sb.append(ourDataLayer1);
        myLayerKey1 = sb.toString();

        WFSStateParameters params = new WFSStateParameters();
        params.setTypeName(ourDataLayer1);
        params.setVersion("1.0.0");

        WFSLayerState state1 = new WFSLayerState();
        state1.setUrl(myLaye1Url1);
        state1.setId(myLayerKey1);
        state1.setDisplayName(ourDataLayer1);
        state1.setWFSParameters(params);
        BasicFeatureStyle style1 = new BasicFeatureStyle();
        style1.setPointColor("aaff00");
        style1.setPointOpacity(255);
        style1.setAltitudeColumn("ALT1");
        state1.setBasicFeatureStyle(style1);
        return state1;
    }

    /**
     * Creates the first state.
     *
     * @return the wFS layer state
     */
    private WFSLayerState createState2()
    {
        StringBuilder sb = new StringBuilder(17);
        sb.append(ourProtocol);
        sb.append(ourServerTitle);
        sb.append("/wfsServer");
        myLayerUrl2 = sb.toString();
        sb.append(WFSConstants.LAYERNAME_SEPARATOR);
        sb.append(ourDataLayer2);
        myLayerKey2 = sb.toString();

        WFSStateParameters params = new WFSStateParameters();
        params.setTypeName(ourDataLayer2);
        params.setVersion("1.0.0");

        WFSLayerState state2 = new WFSLayerState();
        state2.setUrl(myLayerUrl2);
        state2.setId(myLayerKey2);
        state2.setDisplayName(ourDataLayer2);
        state2.setWFSParameters(params);
        BasicFeatureStyle style2 = new BasicFeatureStyle();
        style2.setPointColor("aaff00");
        style2.setPointOpacity(255);
        style2.setAltitudeColumn("ALT2");
        state2.setBasicFeatureStyle(style2);
        return state2;
    }

    /**
     * Creates a WFS data type to clone.
     *
     * @param toolbox the toolbox
     * @param manager the order manager
     * @param serverTitle the server title
     * @param layerKey the layer key
     * @param layerName the layer name
     * @param layerURL the layer URL.
     * @param altCol the altitude column
     * @return the wFS data type
     */
    private WFSDataType createTypeToClone(Toolbox toolbox, OrderManager manager, String serverTitle, String layerKey,
            String layerName, String layerURL, String altCol)
    {
        WFSLayerColumnManager columnManager = new WFSLayerColumnManager(toolbox);
        WFSMetaDataInfo type1Mdi = new WFSMetaDataInfo(toolbox, columnManager);
        if (StringUtils.isNotEmpty(altCol))
        {
            type1Mdi.setAltitudeKey(altCol, this);
        }
        LayerConfiguration configuration = ServerToolboxUtils.getServerToolbox(toolbox).getLayerConfigurationManager()
                .getConfigurationFromName(StateConstants.WFS_LAYER_TYPE);
        WFSDataType wfsDataType = new WFSDataType(toolbox, serverTitle, layerKey, layerName, layerName, type1Mdi, configuration);
        wfsDataType.setQueryable(true);
        wfsDataType.setUrl(layerURL);
        MapVisualizationInfo mapInfo1 = new WFSMapVisualizationInfo(MapVisualizationType.UNKNOWN, manager);
        wfsDataType.setMapVisualizationInfo(mapInfo1);
        wfsDataType.setVisible(true, this);
        if (StringUtils.isNotEmpty(altCol))
        {
            wfsDataType.getBasicVisualizationInfo().setTypeColor(new Color(170, 255, 0), this);
        }
        else
        {
            wfsDataType.getBasicVisualizationInfo().setTypeColor(new Color(255, 255, 0), this);
            wfsDataType.getBasicVisualizationInfo().setLoadsTo(LoadsTo.TIMELINE, this);
        }

        wfsDataType.getStreamingSupport().setStreamingEnabled(true);

        return wfsDataType;
    }
}
