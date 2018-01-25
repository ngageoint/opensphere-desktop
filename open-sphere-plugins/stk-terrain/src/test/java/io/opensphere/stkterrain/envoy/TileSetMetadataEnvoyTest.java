package io.opensphere.stkterrain.envoy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.matchers.EasyMockHelper;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.stkterrain.model.TileSetMetadata;
import io.opensphere.stkterrain.util.Constants;

/**
 * Unit test for the {@link TileSetMetadataEnvoy}.
 */
public class TileSetMetadataEnvoyTest
{
    /**
     * A test error message.
     */
    private static final String ourErrorMessage = "Error Message";

    /**
     * The test server url.
     */
    private static final String ourTestServer = "http://somehost/terrain";

    /**
     * The test tile set name.
     */
    private static final String ourTestTileSet = "world";

    /**
     * Tests getting the thread pool name.
     */
    @Test
    public void testGetThreadPoolName()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        TileSetMetadataEnvoy envoy = new TileSetMetadataEnvoy(toolbox, ourTestServer);

        assertEquals(Constants.ENVOY_THREAD_POOL_NAME + ourTestServer, envoy.getThreadPoolName());

        support.verifyAll();
    }

    /**
     * Tests the provides data for.
     */
    @Test
    public void testProvidesDataFor()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);
        DataModelCategory provides = new DataModelCategory(null, TileSetMetadata.class.getName(), ourTestTileSet);
        DataModelCategory noProvidy = new DataModelCategory(null, TileSetMetadata.class.getName(), null);
        DataModelCategory noProvidyToo = new DataModelCategory(null, null, null);
        DataModelCategory providesServer = new DataModelCategory(ourTestServer, TileSetMetadata.class.getName(), ourTestTileSet);
        DataModelCategory noProvides = new DataModelCategory("http://someotherserver", TileSetMetadata.class.getName(),
                ourTestTileSet);

        support.replayAll();

        TileSetMetadataEnvoy envoy = new TileSetMetadataEnvoy(toolbox, ourTestServer);

        assertTrue(envoy.providesDataFor(provides));
        assertFalse(envoy.providesDataFor(noProvidy));
        assertFalse(envoy.providesDataFor(noProvidyToo));
        assertTrue(envoy.providesDataFor(providesServer));
        assertFalse(envoy.providesDataFor(noProvides));

        support.verifyAll();
    }

    /**
     * Tests querying and getting metadata back.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     * @throws CacheException Bad cache.
     * @throws QueryException bad query.
     * @throws InterruptedException don't interrupt.
     */
    @Test
    public void testQuery() throws IOException, URISyntaxException, CacheException, InterruptedException, QueryException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, 200, null);
        CacheDepositReceiver receiver = createReceiver(support);

        support.replayAll();

        TileSetMetadataEnvoy envoy = new TileSetMetadataEnvoy(toolbox, ourTestServer);
        DataModelCategory category = new DataModelCategory(null, TileSetMetadata.class.getName(), ourTestTileSet);
        envoy.query(category, New.list(), New.list(), New.list(), -1, New.list(Constants.TILESET_METADATA_PROPERTY_DESCRIPTOR),
                receiver);

        support.verifyAll();
    }

    /**
     * Tests querying and getting an exception back.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     * @throws CacheException Bad Cache.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testQueryException() throws IOException, URISyntaxException, CacheException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, 200, new IOException());
        CacheDepositReceiver receiver = support.createMock(CacheDepositReceiver.class);

        support.replayAll();

        TileSetMetadataEnvoy envoy = new TileSetMetadataEnvoy(toolbox, ourTestServer);
        DataModelCategory category = new DataModelCategory(null, TileSetMetadata.class.getName(), ourTestTileSet);
        boolean exceptionThrown = false;
        try
        {
            envoy.query(category, New.list(), New.list(), New.list(), -1,
                    New.list(Constants.TILESET_METADATA_PROPERTY_DESCRIPTOR), receiver);
        }
        catch (QueryException e)
        {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

        support.verifyAll();
    }

    /**
     * Tests querying and getting an invalid response back.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     * @throws CacheException Bad Cache.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testQueryInvalidResponse() throws IOException, URISyntaxException, CacheException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, 404, null);
        CacheDepositReceiver receiver = support.createMock(CacheDepositReceiver.class);

        support.replayAll();

        TileSetMetadataEnvoy envoy = new TileSetMetadataEnvoy(toolbox, ourTestServer);
        DataModelCategory category = new DataModelCategory(null, TileSetMetadata.class.getName(), ourTestTileSet);
        boolean exceptionThrown = false;
        try
        {
            envoy.query(category, New.list(), New.list(), New.list(), -1,
                    New.list(Constants.TILESET_METADATA_PROPERTY_DESCRIPTOR), receiver);
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
     * @return The mocked {@link Toolbox}.
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    private Toolbox createToolbox(EasyMockSupport support, int responseCode, IOException exception)
        throws IOException, URISyntaxException
    {
        URL url = new URL(ourTestServer + "/v1/tilesets/" + ourTestTileSet + "/tiles/layer.json");

        HttpServer server = support.createMock(HttpServer.class);
        EasyMock.expect(server.sendGet(EasyMockHelper.eq(url), EasyMock.isA(ResponseValues.class)))
                .andAnswer(() -> sendGetAnswer(responseCode, exception));

        @SuppressWarnings("unchecked")
        ServerProvider<HttpServer> provider = support.createMock(ServerProvider.class);
        EasyMock.expect(provider.getServer(EasyMockHelper.eq(url))).andReturn(server);

        ServerProviderRegistry serverRegistry = support.createMock(ServerProviderRegistry.class);
        EasyMock.expect(serverRegistry.getProvider(EasyMock.eq(HttpServer.class))).andReturn(provider);

        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getServerProviderRegistry()).andReturn(serverRegistry);

        return toolbox;
    }

    /**
     * The answer for the mocked receive call.
     *
     * @return The model ids.
     */
    @SuppressWarnings("unchecked")
    private long[] receiveAnswer()
    {
        DefaultCacheDeposit<TileSetMetadata> deposit = (DefaultCacheDeposit<TileSetMetadata>)EasyMock.getCurrentArguments()[0];

        DataModelCategory category = deposit.getCategory();
        DataModelCategory expected = new DataModelCategory(ourTestServer, TileSetMetadata.class.getName(), ourTestTileSet);

        assertEquals(expected, category);
        assertEquals(Constants.TILESET_METADATA_PROPERTY_DESCRIPTOR,
                deposit.getAccessors().iterator().next().getPropertyDescriptor());
        assertTrue(deposit.getAccessors().iterator().next() instanceof SerializableAccessor);
        List<TileSetMetadata> inputs = New.list(deposit.getInput());

        assertEquals(1, inputs.size());

        assertEquals("world", inputs.get(0).getName());
        assertEquals(2, inputs.get(0).getAvailable().size());

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

        String returnString = ourErrorMessage;

        if (responseCode == 200)
        {
            returnString = "{\"tilejson\":\"2.1.0\",\"name\":\"world\",\"description\":null,\"version\":\"1.16389.0\",\"format\":\"quantized-mesh-1.0\","
                    + "\"attribution\":\"© Analytical Graphics Inc., © CGIAR-CSI, Produced using Copernicus"
                    + " data and information funded by the European Union - EU-DEM layers\","
                    + "\"scheme\":\"tms\",\"extensions\":[\"watermask\",\"vertexnormals\",\"octvertexnormals\"],"
                    + "\"tiles\":[\"{z}/{x}/{y}.terrain?v={version}\"],"
                    + "\"minzoom\":0,\"maxzoom\":16,\"bounds\":[-179.9,-89.9,179.9,89.9],\"projection\":\"EPSG:4326\","
                    + "\"available\":[[{\"startX\":0,\"startY\":0,\"endX\":1,\"endY\":0}],"
                    + "[{\"startX\":0,\"startY\":0,\"endX\":2,\"endY\":0}, {\"startX\":2,\"startY\":1,\"endX\":3,\"endY\":1}]]}";
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(returnString.getBytes(StringUtilities.DEFAULT_CHARSET));
        stream = new CancellableInputStream(inputStream, null);

        ResponseValues responseValues = (ResponseValues)EasyMock.getCurrentArguments()[1];
        responseValues.setResponseCode(responseCode);

        return stream;
    }
}
