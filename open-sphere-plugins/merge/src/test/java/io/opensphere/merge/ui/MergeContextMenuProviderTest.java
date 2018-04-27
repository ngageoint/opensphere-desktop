package io.opensphere.merge.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenuItem;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.core.datafilter.columns.ColumnMappingController;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.MultiDataGroupContextKey;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.merge.controller.MergeController;

/**
 * Unit test for {@link MergeContextMenuProvider}.
 */
public class MergeContextMenuProviderTest
{
    /**
     * Tests getting the menu items for two layers with data.
     */
    @Test
    public void testGetMenuItems()
    {
        EasyMockSupport support = new EasyMockSupport();

        MultiDataGroupContextKey key = createKeyFeatures(support, 2);
        DataElementCache cache = createCache(support, key.getActualDataTypes(), 1);
        Toolbox toolbox = createToolbox(support, cache);

        support.replayAll();

        MergeContextMenuProvider provider = new MergeContextMenuProvider(toolbox, null);
        Collection<? extends Component> menuItems = provider.getMenuItems(toString(), key);

        assertEquals(2, menuItems.size());
        Iterator<? extends Component> iterator = menuItems.iterator();
        assertEquals("Merge...", ((JMenuItem)iterator.next()).getText());
        assertEquals("Join...", ((JMenuItem)iterator.next()).getText());
        assertTrue(provider.getPriority() > 10);
        assertTrue(provider.hasData(key.getActualDataTypes()));

        support.verifyAll();
    }

    /**
     * Tests getting the menu items for two groups selected.
     */
    @Test
    public void testGetMenuItemsGroups()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<DataTypeInfo> featureLayers = New.list(createFeatureLayer(support), createFeatureLayer(support));
        MultiDataGroupContextKey key = createKeyGroups(support, featureLayers);
        DataElementCache cache = createCache(support, featureLayers, 1);
        Toolbox toolbox = createToolbox(support, cache);

        support.replayAll();

        MergeContextMenuProvider provider = new MergeContextMenuProvider(toolbox, null);
        Collection<? extends Component> menuItems = provider.getMenuItems(toString(), key);

        assertEquals(2, menuItems.size());
        Iterator<? extends Component> iterator = menuItems.iterator();
        assertEquals("Merge...", ((JMenuItem)iterator.next()).getText());
        assertEquals("Join...", ((JMenuItem)iterator.next()).getText());
        assertTrue(provider.hasData(featureLayers));

        support.verifyAll();
    }

    /**
     * Tests getting the menu items for two layers with no data.
     */
    @Test
    public void testGetMenuItemsNoData()
    {
        EasyMockSupport support = new EasyMockSupport();

        MultiDataGroupContextKey key = createKeyFeatures(support, 2);
        DataElementCache cache = createCache(support, key.getActualDataTypes(), 0);
        Toolbox toolbox = createToolbox(support, cache);

        support.replayAll();

        MergeContextMenuProvider provider = new MergeContextMenuProvider(toolbox, null);
        Collection<? extends Component> menuItems = provider.getMenuItems(toString(), key);

        assertEquals("Merge...", ((JMenuItem)menuItems.iterator().next()).getText());
        assertFalse(provider.hasData(key.getActualDataTypes()));

        support.verifyAll();
    }

    /**
     * Tests getting the menu items for one layers with data.
     */
    @Test
    public void testGetMenuItemsOneLayer()
    {
        EasyMockSupport support = new EasyMockSupport();

        MultiDataGroupContextKey key = createKeyFeatures(support, 1);
        DataElementCache cache = createCache(support, key.getActualDataTypes(), 1);
        Toolbox toolbox = createToolbox(support, cache);

        support.replayAll();

        MergeContextMenuProvider provider = new MergeContextMenuProvider(toolbox, null);
        Collection<? extends Component> menuItems = provider.getMenuItems(toString(), key);

        assertEquals(2, menuItems.size());
        Iterator<? extends Component> iterator = menuItems.iterator();
        assertFalse(iterator.next().isEnabled());
        assertFalse(iterator.next().isEnabled());
        assertTrue(provider.hasData(key.getActualDataTypes()));

        support.verifyAll();
    }

    /**
     * Tests getting the menu items for two tile layers.
     */
    @Test
    public void testGetMenuItemsTileLayers()
    {
        EasyMockSupport support = new EasyMockSupport();

        MultiDataGroupContextKey key = createKeyTiles(support, 2);
        DataElementCache cache = support.createMock(DataElementCache.class);
        Toolbox toolbox = createToolbox(support, cache);

        support.replayAll();

        MergeContextMenuProvider provider = new MergeContextMenuProvider(toolbox, null);
        Collection<? extends Component> menuItems = provider.getMenuItems(toString(), key);

        assertEquals(0, menuItems.size());

        support.verifyAll();
    }

    /**
     * Creates the data element cache.
     *
     * @param support Used to create the cache.
     * @param layers The expected layers.
     * @param count The data element count to return.
     * @return The cache.
     */
    private DataElementCache createCache(EasyMockSupport support, Collection<DataTypeInfo> layers, int count)
    {
        DataElementCache cache = support.createMock(DataElementCache.class);

        for (DataTypeInfo layer : layers)
        {
            EasyMock.expect(Integer.valueOf(cache.getElementCountForType(EasyMock.eq(layer)))).andReturn(Integer.valueOf(count)).anyTimes();
        }

        return cache;
    }

    /**
     * Creates an easy mocked feature layer.
     *
     * @param support Used to create the mock.
     * @return The mocked layer.
     */
    private DataTypeInfo createFeatureLayer(EasyMockSupport support)
    {
        BasicVisualizationInfo visInfo = support.createMock(BasicVisualizationInfo.class);
        EasyMock.expect(Boolean.valueOf(visInfo.usesDataElements())).andReturn(Boolean.TRUE);

        DataTypeInfo layer = support.createMock(DataTypeInfo.class);
        EasyMock.expect(layer.getBasicVisualizationInfo()).andReturn(visInfo).atLeastOnce();

        return layer;
    }

    /**
     * Creates an easy mocked feature layer.
     *
     * @param support Used to create the mock.
     * @return The mocked layer.
     */
    private DataTypeInfo createFeatureLayerMap(EasyMockSupport support)
    {
        MapVisualizationInfo visInfo = support.createMock(MapVisualizationInfo.class);
        EasyMock.expect(Boolean.valueOf(visInfo.usesMapDataElements())).andReturn(Boolean.TRUE);

        DataTypeInfo layer = support.createMock(DataTypeInfo.class);
        EasyMock.expect(layer.getBasicVisualizationInfo()).andReturn(null).atLeastOnce();
        EasyMock.expect(layer.getMapVisualizationInfo()).andReturn(visInfo).atLeastOnce();

        return layer;
    }

    /**
     * Creates a context key for for selected feature layers.
     *
     * @param support Used to create mock.
     * @param number The number of layers to call.
     * @return The key.
     */
    private MultiDataGroupContextKey createKeyFeatures(EasyMockSupport support, int number)
    {
        List<DataTypeInfo> actualTypes = New.list();
        for (int i = 0; i < number; i++)
        {
            if (i % 2 == 0)
            {
                actualTypes.add(createFeatureLayer(support));
            }
            else
            {
                actualTypes.add(createFeatureLayerMap(support));
            }
        }

        MultiDataGroupContextKey key = new MultiDataGroupContextKey(New.list(), New.list(), actualTypes);

        return key;
    }

    /**
     * Creates a context key for for selected feature layers.
     *
     * @param support Used to create mock.
     * @param featureLayers The mocked feature layers.
     * @return The key.
     */
    private MultiDataGroupContextKey createKeyGroups(EasyMockSupport support, List<DataTypeInfo> featureLayers)
    {
        List<DataGroupInfo> actualGroups = New.list();
        for (DataTypeInfo featureLayer : featureLayers)
        {
            DataGroupInfo group = support.createMock(DataGroupInfo.class);
            EasyMock.expect(group.getMembers(EasyMock.eq(false))).andReturn(New.set(createTileLayer(support), featureLayer));
            actualGroups.add(group);
        }

        MultiDataGroupContextKey key = new MultiDataGroupContextKey(actualGroups, actualGroups, New.list());

        return key;
    }

    /**
     * Creates a context key for for selected feature layers.
     *
     * @param support Used to create mock.
     * @param number The number of layers to call.
     * @return The key.
     */
    private MultiDataGroupContextKey createKeyTiles(EasyMockSupport support, int number)
    {
        List<DataTypeInfo> actualTypes = New.list();
        for (int i = 0; i < number; i++)
        {
            actualTypes.add(createTileLayer(support));
        }

        MultiDataGroupContextKey key = new MultiDataGroupContextKey(New.list(), New.list(), actualTypes);

        return key;
    }

    /**
     * Creates an easy mocked feature layer.
     *
     * @param support Used to create the mock.
     * @return The mocked layer.
     */
    private DataTypeInfo createTileLayer(EasyMockSupport support)
    {
        BasicVisualizationInfo visInfo = support.createMock(BasicVisualizationInfo.class);
        EasyMock.expect(Boolean.valueOf(visInfo.usesDataElements())).andReturn(Boolean.FALSE);

        MapVisualizationInfo mapInfo = support.createMock(MapVisualizationInfo.class);
        EasyMock.expect(Boolean.valueOf(mapInfo.usesMapDataElements())).andReturn(Boolean.FALSE);

        DataTypeInfo layer = support.createMock(DataTypeInfo.class);
        EasyMock.expect(layer.getBasicVisualizationInfo()).andReturn(visInfo).atLeastOnce();
        EasyMock.expect(layer.getMapVisualizationInfo()).andReturn(mapInfo).atLeastOnce();

        return layer;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param cache A mocked cache to return.
     * @return The toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, DataElementCache cache)
    {
        DataGroupController groupController = support.createNiceMock(DataGroupController.class);

        EasyMock.expect(Boolean.valueOf(
                groupController.addRootDataGroupInfo(EasyMock.isA(DataGroupInfo.class), EasyMock.isA(MergeController.class))))
                .andReturn(Boolean.TRUE);

        MantleToolbox mantle = support.createNiceMock(MantleToolbox.class);
        EasyMock.expect(mantle.getDataElementCache()).andReturn(cache);
        EasyMock.expect(mantle.getDataGroupController()).andReturn(groupController).atLeastOnce();

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantle).atLeastOnce();

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();

        EventManager eventManager = support.createNiceMock(EventManager.class);
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager).anyTimes();

        ColumnMappingController mapper = support.createMock(ColumnMappingController.class);

        DataFilterRegistry filterRegistry = support.createMock(DataFilterRegistry.class);
        EasyMock.expect(filterRegistry.getColumnMappingController()).andReturn(mapper);
        EasyMock.expect(toolbox.getDataFilterRegistry()).andReturn(filterRegistry);

        DataRegistry dataRegistry = support.createMock(DataRegistry.class);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry);

        return toolbox;
    }
}
