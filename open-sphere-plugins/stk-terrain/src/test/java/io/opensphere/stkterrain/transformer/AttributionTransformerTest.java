package io.opensphere.stkterrain.transformer;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.Viewer.Observer;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.stkterrain.model.TileSetMetadata;
import io.opensphere.stkterrain.util.Constants;

/**
 * Unit test for the {@link AttributionTransformer}.
 */
public class AttributionTransformerTest
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
     * Tests handling when all values are removed.
     */
    @Test
    public void testAllValuesRemoved()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = createDataRegistry(support);
        List<Geometry> publishedGeometries = New.list();
        GenericSubscriber<Geometry> subscriber = createSubscriber(support, publishedGeometries);
        MapManager mapManager = createMapManager(support);

        support.replayAll();

        AttributionTransformer transformer = new AttributionTransformer(dataRegistry, mapManager);
        transformer.addSubscriber(subscriber);
        transformer.open();
        TileSetMetadata metadata = new TileSetMetadata();
        metadata.setAttribution("I am attribution");
        transformer.valuesAdded(new DataModelCategory(ourServer, TileSetMetadata.class.getName(), ourTileSet), new long[] { 1 },
                New.list(metadata), this);

        assertEquals(1, publishedGeometries.size());
        assertEquals("Terrain provided by I am attribution", ((LabelGeometry)publishedGeometries.get(0)).getText());

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
        MapManager mapManager = createMapManager(support);

        support.replayAll();

        AttributionTransformer transformer = new AttributionTransformer(dataRegistry, mapManager);
        transformer.addSubscriber(subscriber);
        transformer.open();
        TileSetMetadata metadata = new TileSetMetadata();
        metadata.setAttribution("I am attribution");
        transformer.valuesAdded(new DataModelCategory(ourServer, TileSetMetadata.class.getName(), ourTileSet), new long[] { 1 },
                New.list(metadata), this);

        assertEquals(1, publishedGeometries.size());
        LabelGeometry label = (LabelGeometry)publishedGeometries.get(0);
        assertEquals("Terrain provided by I am attribution", label.getText());
        assertEquals(2, label.getPosition().asVector2d().getX(), 0);
        assertEquals(498, label.getPosition().asVector2d().getY(), 0);

        transformer.valuesRemoved(new DataModelCategory(ourServer, TileSetMetadata.class.getName(), "Some other Tile"),
                new long[] { 2 }, this);
        assertEquals(1, publishedGeometries.size());

        transformer.valuesRemoved(new DataModelCategory(ourServer, TileSetMetadata.class.getName(), ourTileSet), new long[] { 1 },
                this);
        assertEquals(0, publishedGeometries.size());

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

        dataRegistry.addChangeListener(EasyMock.isA(AttributionTransformer.class),
                EasyMock.eq(new DataModelCategory(null, TileSetMetadata.class.getName(), null)),
                EasyMock.eq(Constants.TILESET_METADATA_PROPERTY_DESCRIPTOR));
        dataRegistry.removeChangeListener(EasyMock.isA(AttributionTransformer.class));

        return dataRegistry;
    }

    /**
     * Creates an easy mocked {@link MapManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link MapManager}.
     */
    private MapManager createMapManager(EasyMockSupport support)
    {
        DynamicViewer viewer = support.createMock(DynamicViewer.class);
        EasyMock.expect(Integer.valueOf(viewer.getViewportHeight())).andReturn(Integer.valueOf(500));

        MapManager mapManager = support.createMock(MapManager.class);
        EasyMock.expect(mapManager.getStandardViewer()).andReturn(viewer).times(3);

        viewer.addObserver(EasyMock.isA(Observer.class));
        viewer.removeObserver(EasyMock.isA(Observer.class));

        return mapManager;
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
        EasyMock.expectLastCall().andAnswer(() -> receiveAnswer(publishedGeometries)).times(2);

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
