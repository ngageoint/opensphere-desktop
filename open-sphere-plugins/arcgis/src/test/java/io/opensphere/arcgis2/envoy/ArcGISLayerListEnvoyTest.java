package io.opensphere.arcgis2.envoy;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.arcgis2.model.ArcGISLayer;
import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.PropertyAccessor;
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

/**
 * Unit test for {@link ArcGISLayerListEnvoy}.
 */
public class ArcGISLayerListEnvoyTest
{
    /**
     * The test url.
     */
    private static final String ourTestUrl = "http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Base/MapServer";

    /**
     * The base url.
     */
    private static final String ourBaseUrl = "http://services.arcgisonline.com/ArcGIS/rest/services";

    /**
     * Tests the {@link ArcGISLayerListEnvoy#removeServerComponent(String)}
     * method.
     *
     * @throws IOException Bad IO.
     * @throws URISyntaxException bad URL.
     */
    @Test
    public void removeServerComponent() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();
        Toolbox toolbox = createToolbox(support, "/testLayers.json");
        support.replayAll();

        ArcGISLayerListEnvoy envoy = new ArcGISLayerListEnvoy(toolbox);

        String result = envoy.removeServerComponent("foo/bar/FeatureServer");
        assertEquals("foo/bar", result);
    }

    /**
     * Tests getting Arc layers.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     * @throws CacheException Bad cache.
     * @throws QueryException Bad query.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testQuery() throws IOException, URISyntaxException, CacheException, InterruptedException, QueryException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, "/testLayers.json");
        CacheDepositReceiver receiver = createReceiver(support);

        support.replayAll();

        ArcGISLayerListEnvoy envoy = new ArcGISLayerListEnvoy(toolbox);

        DataModelCategory category = new DataModelCategory(null, ArcGISLayer.class.getName(), ourTestUrl);
        envoy.query(category, null, null, null, 0, null, receiver);

        support.verifyAll();
    }

    /**
     * Tests getting Arc layers.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     * @throws CacheException Bad cache.
     * @throws QueryException Bad query.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testQueryBlankTitle() throws IOException, URISyntaxException, CacheException, InterruptedException, QueryException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, "/testLayersBlankTitle.json");
        CacheDepositReceiver receiver = createReceiver(support);

        support.replayAll();

        ArcGISLayerListEnvoy envoy = new ArcGISLayerListEnvoy(toolbox);

        DataModelCategory category = new DataModelCategory(null, ArcGISLayer.class.getName(), ourTestUrl);
        envoy.query(category, null, null, null, 0, null, receiver);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param jsonResource The test json.
     * @return The mocked toolbox.
     * @throws IOException Bad io.
     * @throws URISyntaxException bad url.
     */
    private Toolbox createToolbox(EasyMockSupport support, String jsonResource) throws IOException, URISyntaxException
    {
        URL url = new URL(ourBaseUrl + "?f=json");
        HttpServer server = support.createMock(HttpServer.class);
        EasyMock.expect(server.sendGet(EasyMockHelper.eq(url), EasyMock.isA(ResponseValues.class))).andAnswer(() ->
        {
            ResponseValues response = (ResponseValues)EasyMock.getCurrentArguments()[1];
            response.setResponseCode(HttpURLConnection.HTTP_OK);
            response.setHeader(New.map());
            response.getHeader().put("content-type", New.list("json"));
            return new CancellableInputStream(getClass().getResourceAsStream(jsonResource), null);
        });

        @SuppressWarnings("unchecked")
        ServerProvider<HttpServer> provider = support.createMock(ServerProvider.class);
        EasyMock.expect(provider.getServer(EasyMock.isA(URL.class))).andReturn(server);

        ServerProviderRegistry serverRegistry = support.createMock(ServerProviderRegistry.class);
        EasyMock.expect(serverRegistry.getProvider(HttpServer.class)).andReturn(provider);

        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getServerProviderRegistry()).andReturn(serverRegistry);

        return toolbox;
    }

    /**
     * Creates an easy mocked {@link CacheDepositReceiver}.
     *
     * @param support Used to create the mock.
     * @return The receiver.
     * @throws CacheException Bad cache.
     */
    @SuppressWarnings("unchecked")
    private CacheDepositReceiver createReceiver(EasyMockSupport support) throws CacheException
    {
        CacheDepositReceiver receiver = support.createMock(CacheDepositReceiver.class);

        EasyMock.expect(receiver.receive(EasyMock.isA(DefaultCacheDeposit.class))).andAnswer(() ->
        {
            DefaultCacheDeposit<ArcGISLayer> deposit = (DefaultCacheDeposit<ArcGISLayer>)EasyMock.getCurrentArguments()[0];

            PropertyAccessor<ArcGISLayer, ArcGISLayer> accessor = (PropertyAccessor<ArcGISLayer, ArcGISLayer>)deposit
                    .getAccessors().iterator().next();

            assertEquals(ArcGISLayer.LAYER_PROPERTY, accessor.getPropertyDescriptor());

            DataModelCategory expected = new DataModelCategory(ArcGISLayerListEnvoy.class.getName(), ArcGISLayer.class.getName(),
                    ourBaseUrl);

            assertEquals(expected, deposit.getCategory());
            assertEquals(CacheDeposit.SESSION_END, deposit.getExpirationDate());
            ArcGISLayer layer = deposit.getInput().iterator().next();

            assertEquals("Canvas Base", layer.getLayerName());
            assertEquals("0", layer.getId());

            return new long[] { 1 };
        });

        return receiver;
    }
}
