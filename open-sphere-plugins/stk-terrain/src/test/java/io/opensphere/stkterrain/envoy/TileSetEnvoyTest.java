package io.opensphere.stkterrain.envoy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.stkterrain.model.TileSet;
import io.opensphere.stkterrain.util.Constants;
import io.opensphere.test.core.matchers.EasyMockHelper;

/**
 * Unit test for the {@link TileSetEnvoy} class.
 */
public class TileSetEnvoyTest
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
     * Tests getting the thread pool name.
     */
    @Test
    public void testGetThreadPoolName()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);
        DefaultValidatorSupport validatorSupport = new DefaultValidatorSupport(null);

        support.replayAll();

        TileSetEnvoy envoy = new TileSetEnvoy(toolbox, validatorSupport, ourTestServer);

        assertEquals(Constants.ENVOY_THREAD_POOL_NAME + ourTestServer, envoy.getThreadPoolName());

        support.verifyAll();
    }

    /**
     * Tests opening the envoy with a valid response.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testOpen() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = createDataRegistry(support);
        HttpServer server = createServer(support, 200, null);
        Toolbox toolbox = createToolbox(support, server, dataRegistry);

        support.replayAll();

        DefaultValidatorSupport validatorSupport = new DefaultValidatorSupport(null);
        TileSetEnvoy envoy = new TileSetEnvoy(toolbox, validatorSupport, ourTestServer);
        envoy.open();

        assertEquals(ValidationStatus.VALID, validatorSupport.getValidationStatus());

        support.verifyAll();
    }

    /**
     * Tests opening the envoy with an invalid response.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testOpenInvalid() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        HttpServer server = createServer(support, 503, null);
        Toolbox toolbox = createToolbox(support, server, null);

        support.replayAll();

        DefaultValidatorSupport validatorSupport = new DefaultValidatorSupport(null);
        TileSetEnvoy envoy = new TileSetEnvoy(toolbox, validatorSupport, ourTestServer);
        envoy.open();

        assertEquals(ValidationStatus.ERROR, validatorSupport.getValidationStatus());
        assertTrue(validatorSupport.getValidationMessage().contains(ourErrorMessage));

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
        DefaultValidatorSupport validatorSupport = new DefaultValidatorSupport(null);

        support.replayAll();

        TileSetEnvoy envoy = new TileSetEnvoy(toolbox, validatorSupport, ourTestServer);

        DataModelCategory category = new DataModelCategory(null, TileSet.class.getName(), null);
        DataModelCategory noProvidy = new DataModelCategory(null, null, null);
        DataModelCategory provides = new DataModelCategory(ourTestServer, TileSet.class.getName(), null);
        DataModelCategory noProvides = new DataModelCategory("http://someotherserver", TileSet.class.getName(), null);

        assertTrue(envoy.providesDataFor(category));
        assertFalse(envoy.providesDataFor(noProvidy));
        assertTrue(envoy.providesDataFor(provides));
        assertFalse(envoy.providesDataFor(noProvides));

        support.verifyAll();
    }

    /**
     * Tests querying tile sets and an exception occurred.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testQueryException() throws URISyntaxException, IOException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        HttpServer server = createServer(support, 200, new IOException());
        Toolbox toolbox = createToolbox(support, server, null);
        CacheDepositReceiver receiver = support.createMock(CacheDepositReceiver.class);

        support.replayAll();

        DefaultValidatorSupport validatorSupport = new DefaultValidatorSupport(null);
        TileSetEnvoy envoy = new TileSetEnvoy(toolbox, validatorSupport, ourTestServer);
        boolean exceptionThrown = false;
        try
        {
            envoy.query(new DataModelCategory(null, TileSet.class.getName(), null), New.collection(), New.list(), New.list(), -1,
                    New.collection(), receiver);
        }
        catch (QueryException e)
        {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

        support.verifyAll();
    }

    /**
     * The answer for the mocked addModels call.
     *
     * @return The model ids.
     */
    @SuppressWarnings("unchecked")
    private long[] addModelsAnswer()
    {
        DefaultCacheDeposit<TileSet> deposit = (DefaultCacheDeposit<TileSet>)EasyMock.getCurrentArguments()[0];

        DataModelCategory category = deposit.getCategory();
        DataModelCategory expected = new DataModelCategory(ourTestServer, TileSet.class.getName(), TileSet.class.getName());

        assertEquals(expected, category);
        assertEquals(Constants.TILESET_PROPERTY_DESCRIPTOR, deposit.getAccessors().iterator().next().getPropertyDescriptor());
        assertTrue(deposit.getAccessors().iterator().next() instanceof SerializableAccessor);
        List<TileSet> inputs = New.list(deposit.getInput());

        assertEquals(2, inputs.size());

        assertEquals("FODAR", inputs.get(0).getName());
        assertEquals(2, inputs.get(0).getDataSources().size());

        assertEquals("world", inputs.get(1).getName());
        assertEquals(0, inputs.get(1).getDataSources().size());

        return new long[] { 0, 1 };
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked data registry.
     */
    @SuppressWarnings("unchecked")
    private DataRegistry createDataRegistry(EasyMockSupport support)
    {
        DataRegistry dataRegistry = support.createMock(DataRegistry.class);

        EasyMock.expect(dataRegistry.addModels(EasyMock.isA(DefaultCacheDeposit.class))).andAnswer(this::addModelsAnswer);

        return dataRegistry;
    }

    /**
     * Creates an easy mocked {@link HttpServer}.
     *
     * @param support Used to create the mock.
     * @param responseCode The response code to return.
     * @param exception The exception to throw on get call, or null if none.
     * @return The mocked {@link HttpServer}.
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad URI.
     */
    private HttpServer createServer(EasyMockSupport support, int responseCode, IOException exception)
        throws IOException, URISyntaxException
    {
        HttpServer server = support.createMock(HttpServer.class);

        URL url = new URL(ourTestServer + "/v1/tilesets");

        EasyMock.expect(server.sendGet(EasyMockHelper.eq(url), EasyMock.isA(ResponseValues.class)))
                .andAnswer(() -> sendGetAnswer(responseCode, exception));

        return server;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param server A mocked {@link HttpServer} to return.
     * @param dataRegistry A mocked {@link DataRegistry} to return.
     * @return The mocked toolbox.
     * @throws MalformedURLException Bad URL.
     */
    @SuppressWarnings("unchecked")
    private Toolbox createToolbox(EasyMockSupport support, HttpServer server, DataRegistry dataRegistry)
        throws MalformedURLException
    {
        URL url = new URL(ourTestServer + "/v1/tilesets");

        ServerProvider<HttpServer> provider = support.createMock(ServerProvider.class);
        EasyMock.expect(provider.getServer(EasyMockHelper.eq(url))).andReturn(server);

        ServerProviderRegistry serverRegistry = support.createMock(ServerProviderRegistry.class);
        EasyMock.expect(serverRegistry.getProvider(EasyMock.eq(HttpServer.class))).andReturn(provider);

        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getServerProviderRegistry()).andReturn(serverRegistry);

        if (dataRegistry != null)
        {
            EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry);
        }

        return toolbox;
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
            returnString = "[{\"name\":\"FODAR\",\"description\":\"a description\",\"dataSources\":"
                    + "[{\"name\":\"Whitemt\",\"description\":\"\",\"attribution\":\"\",\"_rev\":4}"
                    + ",{\"name\":\"Wales\",\"description\":\"\",\"attribution\":\"\",\"_rev\":4}]},"
                    + "{\"name\":\"world\",\"description\":\"\",\"dataSources\":[]}]";
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(returnString.getBytes(StringUtilities.DEFAULT_CHARSET));
        stream = new CancellableInputStream(inputStream, null);

        ResponseValues responseValues = (ResponseValues)EasyMock.getCurrentArguments()[1];
        responseValues.setResponseCode(responseCode);

        return stream;
    }
}
