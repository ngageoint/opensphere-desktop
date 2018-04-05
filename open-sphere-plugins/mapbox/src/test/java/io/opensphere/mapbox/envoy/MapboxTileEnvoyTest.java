package io.opensphere.mapbox.envoy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.InputStreamAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.matcher.ZYXKeyPropertyMatcher;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.image.Image;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.test.core.matchers.EasyMockHelper;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * Unit test for {@link MapboxTileEnvoy}.
 */
public class MapboxTileEnvoyTest
{
    /**
     * The test tile key.
     */
    private static final ZYXImageKey ourKey = new ZYXImageKey(3, 2, 1,
            new GeographicBoundingBox(LatLonAlt.createFromDegrees(10, 10), LatLonAlt.createFromDegrees(11, 11)));

    /**
     * The test layer.
     */
    private static final String ourLayer = "mapbox.dark";

    /**
     * The test server url.
     */
    private static final String ourServer = "http://mapbox.geointapps.org";

    /**
     * Tests the thread pool name.
     */
    @Test
    public void testGetThreadPoolName()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        MapboxTileEnvoy envoy = new MapboxTileEnvoy(toolbox, New.set());
        assertTrue(envoy.getThreadPoolName().startsWith("Mapbox"));

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

        support.replayAll();

        MapboxTileEnvoy envoy = new MapboxTileEnvoy(toolbox, New.set(ourServer));
        DataModelCategory provides = new DataModelCategory(ourServer, XYZTileUtils.TILES_FAMILY, ourLayer);
        DataModelCategory notProvides = new DataModelCategory(ourServer, Image.class.getName(), ourLayer);
        DataModelCategory notProvides2 = new DataModelCategory(null, XYZTileUtils.TILES_FAMILY, ourLayer);
        DataModelCategory notProvides3 = new DataModelCategory(ourServer, XYZTileUtils.TILES_FAMILY, null);
        DataModelCategory notProvides4 = new DataModelCategory(ourServer, null, ourLayer);
        DataModelCategory notProvides5 = new DataModelCategory("http://mapbox.org", XYZTileUtils.TILES_FAMILY, ourLayer);

        assertTrue(envoy.providesDataFor(provides));
        assertFalse(envoy.providesDataFor(notProvides));
        assertFalse(envoy.providesDataFor(notProvides2));
        assertFalse(envoy.providesDataFor(notProvides3));
        assertFalse(envoy.providesDataFor(notProvides4));
        assertFalse(envoy.providesDataFor(notProvides5));

        support.verifyAll();
    }

    /**
     * Tests querying.
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

        Toolbox toolbox = createToolbox(support, HttpURLConnection.HTTP_OK);
        CacheDepositReceiver queryReceiver = createReceiver(support);

        support.replayAll();

        MapboxTileEnvoy envoy = new MapboxTileEnvoy(toolbox, New.set(ourServer));
        DataModelCategory category = new DataModelCategory(ourServer, XYZTileUtils.TILES_FAMILY, ourLayer);
        ZYXKeyPropertyMatcher propertyMatcher = new ZYXKeyPropertyMatcher(XYZTileUtils.KEY_PROPERTY_DESCRIPTOR, ourKey);
        envoy.query(category, New.list(), New.list(propertyMatcher), New.list(), -1,
                New.list(XYZTileUtils.IMAGE_PROPERTY_DESCRIPTOR), queryReceiver);

        support.verifyAll();
    }

    /**
     * Tests when a query fails.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testQueryFailed() throws IOException, URISyntaxException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, HttpURLConnection.HTTP_BAD_REQUEST);
        CacheDepositReceiver queryReceiver = support.createMock(CacheDepositReceiver.class);

        support.replayAll();

        MapboxTileEnvoy envoy = new MapboxTileEnvoy(toolbox, New.set(ourServer));
        DataModelCategory category = new DataModelCategory(ourServer, XYZTileUtils.TILES_FAMILY, ourLayer);
        ZYXKeyPropertyMatcher propertyMatcher = new ZYXKeyPropertyMatcher(XYZTileUtils.KEY_PROPERTY_DESCRIPTOR, ourKey);

        boolean wasExceptionThrown = false;
        try
        {
            envoy.query(category, New.list(), New.list(propertyMatcher), New.list(), -1,
                    New.list(XYZTileUtils.IMAGE_PROPERTY_DESCRIPTOR), queryReceiver);
        }
        catch (QueryException e)
        {
            assertTrue(e.getMessage().contains("Error"));
            assertTrue(e.getMessage().contains("ResponseMessage"));
            assertTrue(e.getMessage().contains(String.valueOf(HttpURLConnection.HTTP_BAD_REQUEST)));
            wasExceptionThrown = true;
        }

        assertTrue(wasExceptionThrown);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link CacheDepositReceiver}.
     *
     * @param support Used to create the mock.
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
     * @param support Used to create the toolbox.
     * @param responseCode The response code to return.
     * @return The mocked toolbox.
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad URI.
     */
    private Toolbox createToolbox(EasyMockSupport support, int responseCode) throws IOException, URISyntaxException
    {
        URL url = new URL(
                ourServer + "/v4/" + ourLayer + "/" + ourKey.getZ() + "/" + ourKey.getX() + "/" + ourKey.getY() + "@2x.png");
        HttpServer server = support.createMock(HttpServer.class);
        EasyMock.expect(server.sendGet(EasyMockHelper.eq(url), EasyMock.isA(ResponseValues.class)))
                .andAnswer(() -> sendGetAnswer(responseCode));

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
     * @throws IOException Bad IO.
     */
    @SuppressWarnings("unchecked")
    private long[] receiveAnswer() throws IOException
    {
        DefaultCacheDeposit<InputStream> deposit = (DefaultCacheDeposit<InputStream>)EasyMock.getCurrentArguments()[0];

        DataModelCategory category = deposit.getCategory();
        DataModelCategory expected = new DataModelCategory(ourServer, XYZTileUtils.TILES_FAMILY, ourLayer);

        assertEquals(expected, category);

        Collection<? extends PropertyAccessor<? super InputStream, ?>> accessors = deposit.getAccessors();
        assertEquals(2, accessors.size());
        Iterator<? extends PropertyAccessor<? super InputStream, ?>> iterator = accessors.iterator();

        PropertyAccessor<InputStream, String> keyAccessor = (PropertyAccessor<InputStream, String>)iterator.next();
        assertTrue(keyAccessor instanceof SerializableAccessor);
        assertEquals(ourKey.toString(), keyAccessor.access(null));

        PropertyAccessor<InputStream, InputStream> imageAccessor = (PropertyAccessor<InputStream, InputStream>)iterator.next();
        assertTrue(imageAccessor instanceof InputStreamAccessor);

        assertEquals(XYZTileUtils.IMAGE_PROPERTY_DESCRIPTOR, imageAccessor.getPropertyDescriptor());
        List<InputStream> inputs = New.list(deposit.getInput());

        assertEquals(1, inputs.size());

        // This just checks one field to make sure it was decoded ok
        InputStream image = inputs.get(0);
        Assert.assertTrue(image.available() > 0);

        assertTrue(deposit.getExpirationDate().getTime() >= TimeInstant.get().plus(XYZTileUtils.TILE_EXPIRATION).getEpochMillis()
                - 1000);

        return new long[] { 0 };
    }

    /**
     * The answer for the sendGet mocked call.
     *
     * @param responseCode The response code.
     * @return The stream returned by the sendGet.
     * @throws IOException Bad IO.
     */
    private CancellableInputStream sendGetAnswer(int responseCode) throws IOException
    {
        InputStream returnStream = null;

        ResponseValues response = (ResponseValues)EasyMock.getCurrentArguments()[1];

        if (responseCode == HttpURLConnection.HTTP_OK)
        {
            BufferedImage image = ImageUtil.LOADING_IMAGE;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);

            byte[] imageBytes = output.toByteArray();
            returnStream = new ByteArrayInputStream(imageBytes);

            response.setContentLength(imageBytes.length);
            response.setResponseCode(responseCode);
        }
        else
        {
            returnStream = new ByteArrayInputStream("Error".getBytes(StringUtilities.DEFAULT_CHARSET));
            response.setResponseCode(responseCode);
            response.setResponseMessage("ResponseMessage");
        }

        return new CancellableInputStream(returnStream, null);
    }
}
