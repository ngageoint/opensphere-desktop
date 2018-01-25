package io.opensphere.stkterrain.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.ByteBuffer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.cache.matcher.ZYXKeyPropertyMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.image.Image;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.stkterrain.model.mesh.QuantizedMesh;
import io.opensphere.stkterrain.model.mesh.QuantizedMeshTest;
import io.opensphere.stkterrain.util.Constants;

/**
 * Unit test for {@link STKTerrainImageProvider}.
 */
public class STKTerrainImageProviderTest
{
    /**
     * The test image key.
     */
    private static final ZYXImageKey ourImageKey = new ZYXImageKey(1, 1, 1,
            new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, -90), LatLonAlt.createFromDegrees(45, 0)));

    /**
     * The test server url.
     */
    private static final String ourServerUrl = "http://somehost/terrain";

    /**
     * The test tile set name.
     */
    private static final String ourTileSetName = "world";

    /**
     * Unit test for getting the image for a given tile.
     */
    @Test
    public void testGetImage()
    {
        EasyMockSupport support = new EasyMockSupport();

        QuantizedMesh mesh = new QuantizedMesh(ByteBuffer.wrap(QuantizedMeshTest.createMeshByes()));
        DataRegistry dataRegistry = createDataRegistry(support, mesh);

        support.replayAll();

        STKTerrainImageProvider provider = new STKTerrainImageProvider(dataRegistry, ourServerUrl, ourTileSetName);
        Image image = provider.getImage(ourImageKey);

        assertEquals(mesh, image);

        support.verifyAll();
    }

    /**
     * Unit test for getting the image for a given tile.
     */
    @Test
    public void testGetImageNull()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = createDataRegistry(support, null);

        support.replayAll();

        STKTerrainImageProvider provider = new STKTerrainImageProvider(dataRegistry, ourServerUrl, ourTileSetName);
        Image image = provider.getImage(ourImageKey);

        assertNull(image);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @param mesh The mesh to return in the query, or null if nothing to
     *            return.
     * @return The mocked {@link DataRegistry}.
     */
    private DataRegistry createDataRegistry(EasyMockSupport support, QuantizedMesh mesh)
    {
        DataRegistry dataRegistry = support.createMock(DataRegistry.class);

        QueryTracker tracker = support.createMock(QueryTracker.class);
        if (mesh == null)
        {
            EasyMock.expect(tracker.getException()).andReturn(null);
        }
        EasyMock.expect(dataRegistry.performQuery(EasyMock.isA(SimpleQuery.class))).andAnswer(() -> queryAnswer(mesh, tracker));

        return dataRegistry;
    }

    /**
     * The answer for the mocked query call.
     *
     * @param mesh The {@link QuantizedMesh} to return in the query, or null to
     *            return null.
     * @param tracker A mocked {@link QueryTracker} to return.
     * @return tracker.
     */
    @SuppressWarnings("unchecked")
    private QueryTracker queryAnswer(QuantizedMesh mesh, QueryTracker tracker)
    {
        SimpleQuery<QuantizedMesh> query = (SimpleQuery<QuantizedMesh>)EasyMock.getCurrentArguments()[0];

        DataModelCategory actual = query.getDataModelCategory();
        DataModelCategory expected = new DataModelCategory(ourServerUrl, QuantizedMesh.class.getName(), ourTileSetName);

        assertEquals(expected, actual);

        ZYXKeyPropertyMatcher matcher = (ZYXKeyPropertyMatcher)query.getParameters().get(0);
        assertEquals(ourImageKey, matcher.getImageKey());

        PropertyValueReceiver<QuantizedMesh> receiver = (PropertyValueReceiver<QuantizedMesh>)query.getPropertyValueReceivers()
                .get(0);

        assertEquals(Constants.QUANTIZED_MESH_PROPERTY_DESCRIPTOR, receiver.getPropertyDescriptor());
        if (mesh != null)
        {
            receiver.receive(New.list(mesh));
        }

        return tracker;
    }
}
