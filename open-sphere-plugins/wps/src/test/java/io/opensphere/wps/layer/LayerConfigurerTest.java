package io.opensphere.wps.layer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javafx.scene.paint.Color;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.order.OrderCategory;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.filter.DataLayerFilter;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;
import io.opensphere.wps.request.WpsProcessConfiguration;
import io.opensphere.wps.util.WPSConstants;
import net.opengis.wps._100.ProcessDescriptionType;

/**
 * Unit test for {@link LayerConfigurer}.
 */
@SuppressWarnings("restriction")
public class LayerConfigurerTest
{
    /**
     * The test instance name.
     */
    private static final String ourInstanceName = "my instance name";

    /**
     * The test type name.
     */
    private static final String ourTypeName = "thetyepname";

    /**
     * The children for the root group.
     */
    private final List<DataGroupInfo> myChildren = New.list();

    /**
     * The layer being tested.
     */
    private WpsDataTypeInfo myWpsLayer;

    /**
     * Tests the layer configurer.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        WpsProcessConfiguration configuration = createConfiguraton();
        Toolbox toolbox = createToolbox(support, false);

        support.replayAll();

        myWpsLayer = new WpsDataTypeInfo(toolbox, "wpsServer", ourTypeName, ourTypeName, ourInstanceName,
                new DefaultMetaDataInfo());

        LayerConfigurer configurer = new LayerConfigurer(toolbox);
        DataGroupInfo group = configurer.configureLayer(configuration, myWpsLayer);

        assertEquals(java.awt.Color.RED, myWpsLayer.getBasicVisualizationInfo().getDefaultTypeColor());
        assertEquals(java.awt.Color.RED, myWpsLayer.getBasicVisualizationInfo().getTypeColor());

        assertEquals(String.class, myWpsLayer.getMetaDataInfo().getKeyClassType("column1"));
        assertEquals(MapVisualizationType.POINT_ELEMENTS, myWpsLayer.getMapVisualizationInfo().getVisualizationType());
        assertEquals(myChildren.get(0), group);

        support.verifyAll();
    }

    /**
     * Tests the layer configurer when the layer is already associated with a
     * group.
     */
    @Test
    public void testSavedWps()
    {
        EasyMockSupport support = new EasyMockSupport();

        WpsProcessConfiguration configuration = createConfiguraton();
        Toolbox toolbox = createToolbox(support, true);

        support.replayAll();

        myWpsLayer = new WpsDataTypeInfo(toolbox, "wpsServer", ourTypeName, ourTypeName, ourInstanceName,
                new DefaultMetaDataInfo());
        DataGroupInfo existingGroup = new DefaultDataGroupInfo(false, null, "wps", "saved");
        existingGroup.addMember(myWpsLayer, this);
        myWpsLayer.getParent();

        LayerConfigurer configurer = new LayerConfigurer(toolbox);
        DataGroupInfo group = configurer.configureLayer(configuration, myWpsLayer);

        assertEquals(java.awt.Color.RED, myWpsLayer.getBasicVisualizationInfo().getDefaultTypeColor());
        assertEquals(java.awt.Color.RED, myWpsLayer.getBasicVisualizationInfo().getTypeColor());

        assertEquals(String.class, myWpsLayer.getMetaDataInfo().getKeyClassType("column1"));
        assertEquals(MapVisualizationType.POINT_ELEMENTS, myWpsLayer.getMapVisualizationInfo().getVisualizationType());
        assertEquals(existingGroup, group);

        support.verifyAll();
    }

    /**
     * Creates a test {@link WpsProcessConfiguration}.
     *
     * @return The test configuration.
     */
    @SuppressWarnings("restriction")
    private WpsProcessConfiguration createConfiguraton()
    {
        WpsProcessConfiguration config = new WpsProcessConfiguration("serverId", new ProcessDescriptionType());

        config.getInputs().put(WPSConstants.COLOR_PROP, Color.RED.toString());
        config.getInputs().put(WPSConstants.PROCESS_INSTANCE_NAME, ourInstanceName);

        return config;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the toolbox.
     * @param isSaved True if the test is testing save and run, false otherwise.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, boolean isSaved)
    {
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getTypeName()).andReturn("another type").atLeastOnce();
        DataGroupInfo group1 = support.createMock(DataGroupInfo.class);
        EasyMock.expect(group1.getMembers(EasyMock.eq(false))).andReturn(New.set(dataType));

        DataTypeInfo theDataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(theDataType.getTypeName()).andReturn(ourTypeName).atLeastOnce();
        DataGroupInfo theGroup = support.createMock(DataGroupInfo.class);
        EasyMock.expect(theGroup.getMembers(EasyMock.eq(false))).andReturn(New.set(theDataType));
        DataGroupActivationProperty activationProperty = new DataGroupActivationProperty(theGroup);
        activationProperty.setActive(true);
        EasyMock.expect(theGroup.activationProperty()).andReturn(activationProperty);
        if (!isSaved)
        {
            EasyMock.expect(Boolean.valueOf(theGroup.isRootNode())).andReturn(Boolean.TRUE);
            EasyMock.expect(theGroup.getChildren()).andReturn(myChildren).anyTimes();
            theGroup.addChild(EasyMock.isA(DataGroupInfo.class), EasyMock.isA(Object.class));
            EasyMock.expectLastCall().andAnswer(() ->
            {
                myChildren.add((DataGroupInfo)EasyMock.getCurrentArguments()[0]);
                return null;
            });
        }
        EasyMock.expect(theDataType.getParent()).andReturn(theGroup).atLeastOnce();
        DefaultMetaDataInfo metadataInfo = new DefaultMetaDataInfo();
        metadataInfo.addKey("column1", String.class, this);
        EasyMock.expect(theDataType.getMetaDataInfo()).andReturn(metadataInfo).atLeastOnce();
        MapVisualizationInfo visInfo = support.createMock(MapVisualizationInfo.class);
        EasyMock.expect(visInfo.getVisualizationType()).andReturn(MapVisualizationType.POINT_ELEMENTS);
        EasyMock.expect(theDataType.getMapVisualizationInfo()).andReturn(visInfo).atLeastOnce();

        DataTypeController typeController = support.createMock(DataTypeController.class);
        typeController.addDataType(EasyMock.cmpEq("WPS"), EasyMock.cmpEq("WPS"), EasyMock.isA(WpsDataTypeInfo.class),
                EasyMock.isA(LayerConfigurer.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals(myWpsLayer, EasyMock.getCurrentArguments()[2]);
            return null;
        });

        DataGroupController groupController = support.createMock(DataGroupController.class);
        EasyMock.expect(groupController.createGroupList(EasyMock.isNull(), EasyMock.isA(DataLayerFilter.class)))
                .andReturn(New.list(group1, theGroup));

        MantleToolbox mantle = support.createMock(MantleToolbox.class);
        DataTypeInfoPreferenceAssistant prefAssist = support.createNiceMock(DataTypeInfoPreferenceAssistant.class);
        EasyMock.expect(mantle.getDataTypeInfoPreferenceAssistant()).andReturn(prefAssist).anyTimes();
        EasyMock.expect(mantle.getDataGroupController()).andReturn(groupController);
        EasyMock.expect(mantle.getDataTypeController()).andReturn(typeController);

        ServerToolbox serverToolbox = support.createMock(ServerToolbox.class);

        WFSLayerConfigurationManager configurationManager = new WFSLayerConfigurationManager();
        EasyMock.expect(serverToolbox.getLayerConfigurationManager()).andReturn(configurationManager).anyTimes();

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantle).atLeastOnce();
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(ServerToolbox.class))).andReturn(serverToolbox).atLeastOnce();

        OrderManager orderManager = support.createNiceMock(OrderManager.class);

        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.isA(String.class), EasyMock.isA(OrderCategory.class)))
                .andReturn(orderManager);

        EventManager eventManager = support.createNiceMock(EventManager.class);

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry);
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager).anyTimes();

        return toolbox;
    }
}
