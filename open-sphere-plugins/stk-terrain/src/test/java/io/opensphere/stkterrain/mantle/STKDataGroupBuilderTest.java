package io.opensphere.stkterrain.mantle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.stkterrain.model.TileSet;
import io.opensphere.stkterrain.model.TileSetDataSource;

/**
 * Unit test for {@link STKDataGroupBuilder}.
 */
public class STKDataGroupBuilderTest
{
    /**
     * The test server url.
     */
    private static final String ourServer = "http://somehost/terrain";

    /**
     * The test tile set name.
     */
    private static final String ourTileSetName = "world";

    /**
     * Tests creating data group and type for a tile set.
     */
    @Test
    public void testCreateGroupAndType()
    {
        EasyMockSupport support = new EasyMockSupport();

        TileSet tileSet = createTileSet();
        Toolbox toolbox = createToolbox(support);

        support.replayAll();

        STKDataGroupBuilder builder = new STKDataGroupBuilder(toolbox);

        DataGroupInfo dataGroup = builder.createGroupAndType(tileSet, ourServer);

        assertEquals(ourServer + ourTileSetName, dataGroup.getId());
        assertEquals(ourTileSetName, dataGroup.getDisplayName());
        assertTrue(dataGroup.activationProperty().getListeners().get(0) instanceof STKDataGroupActivationListener);

        String description = dataGroup.getGroupDescription();

        assertTrue(description.contains("I am description"));
        assertTrue(description.endsWith("source1, source2"));

        DataTypeInfo dataType = dataGroup.getMemberById(ourServer + ourTileSetName, false);
        assertEquals(ourServer + ourTileSetName, dataType.getTypeKey());
        assertEquals(ourTileSetName, dataType.getDisplayName());
        assertEquals(ourTileSetName, dataType.getTypeName());
        assertEquals(ourServer, dataType.getSourcePrefix());

        MapVisualizationInfo mapVisInfo = dataType.getMapVisualizationInfo();

        assertTrue(mapVisInfo.getVisualizationType().isTerrainTileType());
        assertEquals(2016, mapVisInfo.getTileRenderProperties().getZOrder());

        support.verifyAll();
    }

    /**
     * Creates a test tile set.
     *
     * @return The test tile set.
     */
    private TileSet createTileSet()
    {
        TileSet tileSet = new TileSet();
        tileSet.setName(ourTileSetName);
        tileSet.setDescription("I am description");

        TileSetDataSource dataSource1 = new TileSetDataSource();
        dataSource1.setName("source1");

        TileSetDataSource dataSource2 = new TileSetDataSource();
        dataSource2.setName("source2");

        tileSet.getDataSources().add(dataSource1);
        tileSet.getDataSources().add(dataSource2);

        return tileSet;
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support Used to create the mock.
     * @return The {@link Toolbox}.
     */
    private Toolbox createToolbox(EasyMockSupport support)
    {
        OrderManager orderManager = support.createMock(OrderManager.class);
        EasyMock.expect(orderManager
                .activateParticipant(EasyMock.eq(new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY,
                        DefaultOrderCategory.EARTH_ELEVATION_CATEGORY, ourServer + ourTileSetName))))
                .andReturn(2016);

        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.eq(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY),
                EasyMock.eq(DefaultOrderCategory.EARTH_ELEVATION_CATEGORY))).andReturn(orderManager);

        DataTypeInfoPreferenceAssistant assistant = support.createMock(DataTypeInfoPreferenceAssistant.class);
        EasyMock.expect(assistant.isVisiblePreference(EasyMock.cmpEq(ourServer + ourTileSetName))).andReturn(true);
        EasyMock.expect(assistant.getColorPreference(EasyMock.eq(ourServer + ourTileSetName), EasyMock.anyInt()))
                .andReturn(Color.red.getRGB());
        EasyMock.expect(assistant.getOpacityPreference(EasyMock.eq(ourServer + ourTileSetName), EasyMock.anyInt()))
                .andReturn(255);

        MantleToolbox mantleBox = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantleBox.getDataTypeInfoPreferenceAssistant()).andReturn(assistant).atLeastOnce();

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantleBox).atLeastOnce();

        EventManager eventManager = support.createNiceMock(EventManager.class);

        DataRegistry dataRegistry = support.createMock(DataRegistry.class);

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry);

        return toolbox;
    }
}
