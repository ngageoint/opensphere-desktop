package io.opensphere.stkterrain.envoy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.matcher.ZYXKeyPropertyMatcher;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.matchers.EasyMockHelper;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.stkterrain.model.TileSetMetadata;
import io.opensphere.stkterrain.model.mesh.QuantizedMesh;
import io.opensphere.stkterrain.model.mesh.QuantizedMeshTest;
import io.opensphere.stkterrain.util.Constants;

/**
 * Unit test for {@link QuantizedMeshEnvoy}.
 */
public class QuantizedMeshEnvoyTest
{
    /**
     * A test error message.
     */
    private static final String ourErrorMessage = "Error Message";

    /**
     * The test image key.
     */
    private static final ZYXImageKey ourKey = new ZYXImageKey(1, 1, 0,
            new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90d, -180d), LatLonAlt.createFromDegrees(-45, -45)));

    /**
     * The test server url.
     */
    private static final String ourTestServer = "http://somehost/terrain";

    /**
     * The test tile set name.
     */
    private static final String ourTestTileSet = "world";

    /**
     * Tests the thread pool name.
     */
    @Test
    public void testGetThreadPoolName()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        QuantizedMeshEnvoy envoy = new QuantizedMeshEnvoy(toolbox, ourTestServer);

        assertEquals(Constants.ENVOY_THREAD_POOL_NAME + ourTestServer, envoy.getThreadPoolName());

        support.verifyAll();
    }

    /**
     * Tests the providesDataFor.
     */
    @Test
    public void testProvidesDataFor()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        DataModelCategory provides = new DataModelCategory(null, QuantizedMesh.class.getName(), ourTestTileSet);
        DataModelCategory providesToo = new DataModelCategory(ourTestServer, QuantizedMesh.class.getName(), ourTestTileSet);
        DataModelCategory noProvidy = new DataModelCategory(null, QuantizedMesh.class.getName(), null);
        DataModelCategory noProvidyToo = new DataModelCategory("http://someotherhost", QuantizedMesh.class.getName(),
                ourTestTileSet);
        DataModelCategory noProvidyAsWell = new DataModelCategory(null, null, null);

        QuantizedMeshEnvoy envoy = new QuantizedMeshEnvoy(toolbox, ourTestServer);

        assertTrue(envoy.providesDataFor(provides));
        assertTrue(envoy.providesDataFor(providesToo));
        assertFalse(envoy.providesDataFor(noProvidy));
        assertFalse(envoy.providesDataFor(noProvidyToo));
        assertFalse(envoy.providesDataFor(noProvidyAsWell));

        support.verifyAll();
    }

    /**
     * Tests querying for a quantized mesh.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     * @throws CacheException Bad Cache.
     * @throws QueryException Bad query.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testQuery() throws IOException, URISyntaxException, CacheException, InterruptedException, QueryException
    {
        EasyMockSupport support = new EasyMockSupport();

        TileSetMetadata metadata = createMetadata();
        Toolbox toolbox = createToolbox(support, 200, null, metadata);
        CacheDepositReceiver queryReceiver = createReceiver(support);

        support.replayAll();

        QuantizedMeshEnvoy envoy = new QuantizedMeshEnvoy(toolbox, ourTestServer);

        ZYXKeyPropertyMatcher matcher = new ZYXKeyPropertyMatcher(Constants.KEY_PROPERTY_DESCRIPTOR, ourKey);
        envoy.query(new DataModelCategory(null, QuantizedMesh.class.getName(), ourTestTileSet), New.list(), New.list(matcher),
                New.list(), -1, New.list(Constants.QUANTIZED_MESH_PROPERTY_DESCRIPTOR), queryReceiver);

        support.verifyAll();
    }

    /**
     * Tests querying and an invalid response was returned.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testQuery404() throws IOException, URISyntaxException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        TileSetMetadata metadata = createMetadata();
        Toolbox toolbox = createToolbox(support, 404, null, metadata);
        CacheDepositReceiver queryReceiver = support.createMock(CacheDepositReceiver.class);

        support.replayAll();

        QuantizedMeshEnvoy envoy = new QuantizedMeshEnvoy(toolbox, ourTestServer);

        ZYXKeyPropertyMatcher matcher = new ZYXKeyPropertyMatcher(Constants.KEY_PROPERTY_DESCRIPTOR, ourKey);

        boolean exceptionThrown = false;
        try
        {
            envoy.query(new DataModelCategory(null, QuantizedMesh.class.getName(), ourTestTileSet), New.list(), New.list(matcher),
                    New.list(), -1, New.list(Constants.QUANTIZED_MESH_PROPERTY_DESCRIPTOR), queryReceiver);
        }
        catch (QueryException e)
        {
            assertTrue(e.getMessage().contains(ourErrorMessage));
            exceptionThrown = true;
        }

        assertFalse(exceptionThrown);

        support.verifyAll();
    }

    /**
     * Tests querying and an exception occurred.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testQueryException() throws IOException, URISyntaxException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        TileSetMetadata metadata = createMetadata();
        Toolbox toolbox = createToolbox(support, 200, new IOException(), metadata);
        CacheDepositReceiver queryReceiver = support.createMock(CacheDepositReceiver.class);

        support.replayAll();

        QuantizedMeshEnvoy envoy = new QuantizedMeshEnvoy(toolbox, ourTestServer);

        ZYXKeyPropertyMatcher matcher = new ZYXKeyPropertyMatcher(Constants.KEY_PROPERTY_DESCRIPTOR, ourKey);

        boolean exceptionThrown = false;
        try
        {
            envoy.query(new DataModelCategory(null, QuantizedMesh.class.getName(), ourTestTileSet), New.list(), New.list(matcher),
                    New.list(), -1, New.list(Constants.QUANTIZED_MESH_PROPERTY_DESCRIPTOR), queryReceiver);
        }
        catch (QueryException e)
        {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

        support.verifyAll();
    }

    /**
     * Tests querying and an invalid response was returned.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testQueryInvalidResponse() throws IOException, URISyntaxException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        TileSetMetadata metadata = createMetadata();
        Toolbox toolbox = createToolbox(support, 503, null, metadata);
        CacheDepositReceiver queryReceiver = support.createMock(CacheDepositReceiver.class);

        support.replayAll();

        QuantizedMeshEnvoy envoy = new QuantizedMeshEnvoy(toolbox, ourTestServer);

        ZYXKeyPropertyMatcher matcher = new ZYXKeyPropertyMatcher(Constants.KEY_PROPERTY_DESCRIPTOR, ourKey);

        boolean exceptionThrown = false;
        try
        {
            envoy.query(new DataModelCategory(null, QuantizedMesh.class.getName(), ourTestTileSet), New.list(), New.list(matcher),
                    New.list(), -1, New.list(Constants.QUANTIZED_MESH_PROPERTY_DESCRIPTOR), queryReceiver);
        }
        catch (QueryException e)
        {
            assertTrue(e.getMessage().contains(ourErrorMessage));
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

        support.verifyAll();
    }

    /**
     * Tests querying and the metadata doesn't exist.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testQueryNoMetadata() throws IOException, URISyntaxException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, 200, null, null);
        CacheDepositReceiver queryReceiver = support.createMock(CacheDepositReceiver.class);

        support.replayAll();

        QuantizedMeshEnvoy envoy = new QuantizedMeshEnvoy(toolbox, ourTestServer);

        ZYXKeyPropertyMatcher matcher = new ZYXKeyPropertyMatcher(Constants.KEY_PROPERTY_DESCRIPTOR, ourKey);

        boolean exceptionThrown = false;
        try
        {
            envoy.query(new DataModelCategory(null, QuantizedMesh.class.getName(), ourTestTileSet), New.list(), New.list(matcher),
                    New.list(), -1, New.list(Constants.QUANTIZED_MESH_PROPERTY_DESCRIPTOR), queryReceiver);
        }
        catch (QueryException e)
        {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

        support.verifyAll();
    }

    /**
     * Creates the test {@link TileSetMetadata}.
     *
     * @return The test metadata.
     */
    private TileSetMetadata createMetadata()
    {
        TileSetMetadata metadata = new TileSetMetadata();
        metadata.setTiles(New.list("{z}/{x}/{y}.terrain?v={version}"));
        metadata.setVersion("2.0");

        return metadata;
    }

    /**
     * Creates an easy mocked {@link CacheDepositReceiver}.
     *
     * @param support Used to create the receiever.
     * @return The mocked {@link CacheDepositReceiver}.
     * @throws CacheException Bad cache.
     */
    @SuppressWarnings("unchecked")
    private CacheDepositReceiver createReceiver(EasyMockSupport support) throws CacheException
    {
        CacheDepositReceiver receiver = support.createMock(CacheDepositReceiver.class);

        EasyMock.expect(receiver.receive(EasyMock.isA(DefaultCacheDeposit.class))).andAnswer(this::receiveAnswer);

        return receiver;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param responseCode The response code the server should return.
     * @param exception The exception the server should throw, or null if none.
     * @param metadata The metadata to return from the data registry.
     * @return The mocked {@link Toolbox}.
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @SuppressWarnings("unchecked")
    private Toolbox createToolbox(EasyMockSupport support, int responseCode, IOException exception, TileSetMetadata metadata)
        throws IOException, URISyntaxException
    {
        URL url = new URL(ourTestServer + "/" + ourTestTileSet + "/1/0/1.terrain?v=2.0");

        Toolbox toolbox = support.createMock(Toolbox.class);

        if (metadata != null)
        {
            HttpServer server = support.createMock(HttpServer.class);
            EasyMock.expect(server.sendGet(EasyMockHelper.eq(url), EasyMock.isA(Map.class),
                    EasyMock.isA(ResponseValues.class))).andAnswer(() -> sendGetAnswer(responseCode, exception));

            ServerProvider<HttpServer> provider = support.createMock(ServerProvider.class);
            EasyMock.expect(provider.getServer(EasyMockHelper.eq(url))).andReturn(server);

            ServerProviderRegistry serverRegistry = support.createMock(ServerProviderRegistry.class);
            EasyMock.expect(serverRegistry.getProvider(EasyMock.eq(HttpServer.class))).andReturn(provider);

            EasyMock.expect(toolbox.getServerProviderRegistry()).andReturn(serverRegistry);
        }

        DataRegistry registry = support.createMock(DataRegistry.class);
        EasyMock.expect(registry.performLocalQuery(EasyMock.isA(SimpleQuery.class))).andAnswer(() -> queryAnswer(metadata));

        EasyMock.expect(toolbox.getDataRegistry()).andReturn(registry);

        return toolbox;
    }

    /**
     * The answer for the perforLocalQuery mock.
     *
     * @param metadata The metadata to return in the mock.
     * @return The model ids.
     */
    @SuppressWarnings("unchecked")
    private long[] queryAnswer(TileSetMetadata metadata)
    {
        SimpleQuery<TileSetMetadata> query = (SimpleQuery<TileSetMetadata>)EasyMock.getCurrentArguments()[0];

        DataModelCategory category = new DataModelCategory(ourTestServer, TileSetMetadata.class.getName(), ourTestTileSet);

        assertEquals(category, query.getDataModelCategory());
        PropertyValueReceiver<TileSetMetadata> receiver = (PropertyValueReceiver<TileSetMetadata>)query
                .getPropertyValueReceivers().get(0);
        assertEquals(Constants.TILESET_METADATA_PROPERTY_DESCRIPTOR, receiver.getPropertyDescriptor());

        long[] ids = new long[0];
        if (metadata != null)
        {
            receiver.receive(New.list(metadata));
            ids = new long[] { 0 };
        }

        return ids;
    }

    /**
     * The answer for the mocked receive call.
     *
     * @return The model ids.
     */
    @SuppressWarnings("unchecked")
    private long[] receiveAnswer()
    {
        DefaultCacheDeposit<QuantizedMesh> deposit = (DefaultCacheDeposit<QuantizedMesh>)EasyMock.getCurrentArguments()[0];

        DataModelCategory category = deposit.getCategory();
        DataModelCategory expected = new DataModelCategory(ourTestServer, QuantizedMesh.class.getName(), ourTestTileSet);

        assertEquals(expected, category);

        Collection<? extends PropertyAccessor<? super QuantizedMesh, ?>> accessors = deposit.getAccessors();
        assertEquals(2, accessors.size());
        Iterator<? extends PropertyAccessor<? super QuantizedMesh, ?>> iterator = accessors.iterator();

        PropertyAccessor<QuantizedMesh, String> keyAccessor = (PropertyAccessor<QuantizedMesh, String>)iterator.next();
        assertTrue(keyAccessor instanceof SerializableAccessor);
        assertEquals(ourKey.toString(), keyAccessor.access(null));

        PropertyAccessor<QuantizedMesh, QuantizedMesh> meshAccessor = (PropertyAccessor<QuantizedMesh, QuantizedMesh>)iterator
                .next();
        assertTrue(meshAccessor instanceof SerializableAccessor);

        assertEquals(Constants.QUANTIZED_MESH_PROPERTY_DESCRIPTOR, meshAccessor.getPropertyDescriptor());
        List<QuantizedMesh> inputs = New.list(deposit.getInput());

        assertEquals(1, inputs.size());

        // This just checks one field to make sure it was decoded ok
        QuantizedMesh mesh = inputs.get(0);
        Assert.assertEquals(1, mesh.getIndexData().getIndex(5));

        assertTrue(deposit.getExpirationDate().getTime() >= TimeInstant.get().plus(Constants.TILE_EXPIRATION).getEpochMillis()
                - 1000);

        return new long[] { 0 };
    }

    /**
     * The answer for the sendGet mocked call.
     *
     * @param responseCode The response code to return.
     * @param exception The exception to throw or null if no exception should be
     *            thrown.
     * @return The input stream of data, or if the responseCode isn't 200 an
     *         error message.
     * @throws IOException The test exception to throw.
     */
    private CancellableInputStream sendGetAnswer(int responseCode, IOException exception) throws IOException
    {
        if (exception != null)
        {
            throw exception;
        }

        CancellableInputStream stream = null;

        byte[] returnBytes = ourErrorMessage.getBytes(StringUtilities.DEFAULT_CHARSET);

        if (responseCode == 200)
        {
            returnBytes = QuantizedMeshTest.createMeshByes();
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(returnBytes);
        stream = new CancellableInputStream(inputStream, null);

        @SuppressWarnings("unchecked")
        Map<String, String> quantizedMeshHeader = (Map<String, String>)EasyMock.getCurrentArguments()[1];
        assertEquals("application/vnd.quantized-mesh,application/octet-stream;q=0.9", quantizedMeshHeader.get("Accept"));
        ResponseValues responseValues = (ResponseValues)EasyMock.getCurrentArguments()[2];
        responseValues.setResponseCode(responseCode);

        return stream;
    }
}
