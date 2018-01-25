package io.opensphere.stkterrain.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.TerrainTileGeometry;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.stkterrain.model.TileSetMetadata;
import io.opensphere.stkterrain.util.Constants;

/**
 * Unit test for {@link STKLayerTransformer}.
 */
public class STKLayerTransformerTest
{
    /**
     * The test server.
     */
    private static final String ourServer = "http://somehost/terrain";

    /**
     * The test tile set name.
     */
    private static final String ourTileSet = "world";

    /**
     * The expected {@link ZYXImageKey} at zoom level 0.
     */
    private static final List<ZYXImageKey> ourZeroExpectedKeys = New.list(
            new ZYXImageKey(0, 0, 0,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -180), LatLonAlt.createFromDegrees(90, 0))),
            new ZYXImageKey(0, 0, 1,
                    new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 0), LatLonAlt.createFromDegrees(90, 180))));

    /**
     * The visibility listener to test.
     */
    private EventListener<DataTypeVisibilityChangeEvent> myVisibilityListener;

    /**
     * Tests handling when all values are removed.
     */
    @Test
    public void testAllValuesRemoved()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = createDataRegistry(support);
        List<Geometry> publishedGeometries = New.list();
        GenericSubscriber<Geometry> subscriber = createSubscriber(support, publishedGeometries);
        DataGroupController groupController = createGroupController(support, Boolean.TRUE);
        EventManager eventManager = createEventManager(support);

        support.replayAll();

        STKLayerTransformer transformer = new STKLayerTransformer(dataRegistry, groupController, eventManager);
        transformer.addSubscriber(subscriber);
        transformer.open();
        TileSetMetadata metadata = new TileSetMetadata();
        transformer.valuesAdded(new DataModelCategory(ourServer, TileSetMetadata.class.getName(), ourTileSet), new long[] { 1 },
                New.list(metadata), this);

        assertEquals(2, publishedGeometries.size());

        transformer.allValuesRemoved(this);

        assertEquals(0, publishedGeometries.size());

        transformer.close();

        support.verifyAll();
    }

    /**
     * Tests when terrain layers are activated.
     */
    @Test
    public void testValuesAdded()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = createDataRegistry(support);
        List<Geometry> publishedGeometries = New.list();
        GenericSubscriber<Geometry> subscriber = createSubscriber(support, publishedGeometries);
        DataGroupController typeController = createGroupController(support, Boolean.TRUE);
        EventManager eventManager = createEventManager(support);

        support.replayAll();

        STKLayerTransformer transformer = new STKLayerTransformer(dataRegistry, typeController, eventManager);
        transformer.addSubscriber(subscriber);
        transformer.open();
        TileSetMetadata metadata = new TileSetMetadata();
        transformer.valuesAdded(new DataModelCategory(ourServer, TileSetMetadata.class.getName(), ourTileSet), new long[] { 1 },
                New.list(metadata), this);

        assertEquals(2, publishedGeometries.size());

        int index = 0;
        for (ZYXImageKey key : ourZeroExpectedKeys)
        {
            TerrainTileGeometry geometry = (TerrainTileGeometry)publishedGeometries.get(index);
            assertEquals(key.toString(), geometry.getImageManager().getImageKey().toString());
            index++;
        }

        transformer.valuesRemoved(new DataModelCategory(ourServer, TileSetMetadata.class.getName(), "Some other Tile"),
                new long[] { 2 }, this);
        assertEquals(2, publishedGeometries.size());

        transformer.valuesRemoved(new DataModelCategory(ourServer, TileSetMetadata.class.getName(), ourTileSet), new long[] { 1 },
                this);
        assertEquals(0, publishedGeometries.size());

        transformer.close();

        support.verifyAll();
    }

    /**
     * Tests when terrain layers are activated.
     */
    @Test
    public void testValuesAddedNotVisible()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = createDataRegistry(support);
        List<Geometry> publishedGeometries = New.list();
        GenericSubscriber<Geometry> subscriber = createSubscriber(support, publishedGeometries);
        DataGroupController typeController = createGroupController(support, Boolean.FALSE);
        EventManager eventManager = createEventManager(support);
        DataTypeInfo theType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(theType.getTypeKey()).andReturn(ourServer + ourTileSet);

        support.replayAll();

        STKLayerTransformer transformer = new STKLayerTransformer(dataRegistry, typeController, eventManager);
        transformer.addSubscriber(subscriber);
        transformer.open();
        TileSetMetadata metadata = new TileSetMetadata();
        transformer.valuesAdded(new DataModelCategory(ourServer, TileSetMetadata.class.getName(), ourTileSet), new long[] { 1 },
                New.list(metadata), this);

        assertTrue(publishedGeometries.isEmpty());

        myVisibilityListener.notify(new DataTypeVisibilityChangeEvent(theType, true, false, this));
        assertEquals(2, publishedGeometries.size());

        int index = 0;
        for (ZYXImageKey key : ourZeroExpectedKeys)
        {
            TerrainTileGeometry geometry = (TerrainTileGeometry)publishedGeometries.get(index);
            assertEquals(key.toString(), geometry.getImageManager().getImageKey().toString());
            index++;
        }

        transformer.valuesRemoved(new DataModelCategory(ourServer, TileSetMetadata.class.getName(), "Some other Tile"),
                new long[] { 2 }, this);
        assertEquals(2, publishedGeometries.size());

        transformer.valuesRemoved(new DataModelCategory(ourServer, TileSetMetadata.class.getName(), ourTileSet), new long[] { 1 },
                this);
        assertEquals(0, publishedGeometries.size());

        transformer.close();

        support.verifyAll();
    }

    /**
     * Tests when terrain layers are activated.
     */
    @Test
    public void testVisibility()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = createDataRegistry(support);
        List<Geometry> publishedGeometries = New.list();
        GenericSubscriber<Geometry> subscriber = createSubscriber(support, publishedGeometries);
        DataGroupController typeController = createGroupController(support, Boolean.TRUE);
        EventManager eventManager = createEventManager(support);
        DataTypeInfo someOtherType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(someOtherType.getTypeKey()).andReturn("someotherkey");
        DataTypeInfo theType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(theType.getTypeKey()).andReturn(ourServer + ourTileSet).times(2);

        support.replayAll();

        STKLayerTransformer transformer = new STKLayerTransformer(dataRegistry, typeController, eventManager);
        transformer.addSubscriber(subscriber);
        transformer.open();
        TileSetMetadata metadata = new TileSetMetadata();
        transformer.valuesAdded(new DataModelCategory(ourServer, TileSetMetadata.class.getName(), ourTileSet), new long[] { 1 },
                New.list(metadata), this);

        assertEquals(2, publishedGeometries.size());

        int index = 0;
        for (ZYXImageKey key : ourZeroExpectedKeys)
        {
            TerrainTileGeometry geometry = (TerrainTileGeometry)publishedGeometries.get(index);
            assertEquals(key.toString(), geometry.getImageManager().getImageKey().toString());
            index++;
        }

        myVisibilityListener.notify(new DataTypeVisibilityChangeEvent(someOtherType, false, false, this));
        assertEquals(2, publishedGeometries.size());

        myVisibilityListener.notify(new DataTypeVisibilityChangeEvent(theType, false, false, this));
        assertEquals(0, publishedGeometries.size());

        myVisibilityListener.notify(new DataTypeVisibilityChangeEvent(theType, true, false, this));
        assertEquals(2, publishedGeometries.size());

        index = 0;
        for (ZYXImageKey key : ourZeroExpectedKeys)
        {
            TerrainTileGeometry geometry = (TerrainTileGeometry)publishedGeometries.get(index);
            assertEquals(key.toString(), geometry.getImageManager().getImageKey().toString());
            index++;
        }

        transformer.close();

        support.verifyAll();
    }

    /**
     * Creates the {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @return The {@link DataRegistry}.
     */
    private DataRegistry createDataRegistry(EasyMockSupport support)
    {
        DataRegistry dataRegistry = support.createMock(DataRegistry.class);

        dataRegistry.addChangeListener(EasyMock.isA(STKLayerTransformer.class),
                EasyMock.eq(new DataModelCategory(null, TileSetMetadata.class.getName(), null)),
                EasyMock.eq(Constants.TILESET_METADATA_PROPERTY_DESCRIPTOR));
        dataRegistry.removeChangeListener(EasyMock.isA(STKLayerTransformer.class));

        return dataRegistry;
    }

    /**
     * Creates an easy mocked {@link EventManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link EventManager}.
     */
    @SuppressWarnings("unchecked")
    private EventManager createEventManager(EasyMockSupport support)
    {
        EventManager eventManager = support.createMock(EventManager.class);

        eventManager.subscribe(EasyMock.eq(DataTypeVisibilityChangeEvent.class), EasyMock.notNull());
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myVisibilityListener = (EventListener<DataTypeVisibilityChangeEvent>)EasyMock.getCurrentArguments()[1];
            return null;
        });
        eventManager.unsubscribe(EasyMock.eq(DataTypeVisibilityChangeEvent.class), EasyMock.notNull());
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals(myVisibilityListener, EasyMock.getCurrentArguments()[1]);
            return null;
        });

        return eventManager;
    }

    /**
     * Creates an easy mocked {@link DataGroupController}.
     *
     * @param support Used to create the mock.
     * @param visibility The visibility state of the mocked {@link DataTypeInfo}
     *            .
     * @return The DataTypeController.
     */
    private DataGroupController createGroupController(EasyMockSupport support, Boolean visibility)
    {
        TileRenderProperties props = support.createMock(TileRenderProperties.class);

        MapVisualizationInfo mapVisInfo = support.createMock(MapVisualizationInfo.class);
        EasyMock.expect(mapVisInfo.getTileRenderProperties()).andReturn(props);

        DataTypeInfo dataTypeInfo = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataTypeInfo.getMapVisualizationInfo()).andReturn(mapVisInfo);
        EasyMock.expect(Boolean.valueOf(dataTypeInfo.isVisible())).andReturn(visibility);

        DataGroupController groupController = support.createMock(DataGroupController.class);

        EasyMock.expect(groupController.findMemberById(EasyMock.cmpEq(ourServer + ourTileSet))).andReturn(dataTypeInfo);

        return groupController;
    }

    /**
     * Creates an easy mocked subscriber.
     *
     * @param support Used to create the mock.
     * @param publishedGeometries The list to add/remove published geometries to
     *            and from.
     * @return The mocked subscriber.
     */
    private GenericSubscriber<Geometry> createSubscriber(EasyMockSupport support, List<Geometry> publishedGeometries)
    {
        @SuppressWarnings("unchecked")
        GenericSubscriber<Geometry> subscriber = support.createMock(GenericSubscriber.class);

        subscriber.receiveObjects(EasyMock.isA(Object.class), EasyMock.notNull(), EasyMock.notNull());
        EasyMock.expectLastCall().andAnswer(() -> receiveAnswer(publishedGeometries)).atLeastOnce();

        return subscriber;
    }

    /**
     * The answer for the generic subscriber.
     *
     * @param publishedGeometries The list to add/remove published geometries to
     *            and from.
     * @return Null.
     */
    @SuppressWarnings("unchecked")
    private Void receiveAnswer(List<Geometry> publishedGeometries)
    {
        Collection<Geometry> adds = (Collection<Geometry>)EasyMock.getCurrentArguments()[1];
        Collection<Geometry> removes = (Collection<Geometry>)EasyMock.getCurrentArguments()[2];
        publishedGeometries.addAll(adds);
        publishedGeometries.removeAll(removes);
        return null;
    }
}
