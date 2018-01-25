package io.opensphere.xyztile.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.image.DDSImage;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageFormatUnknownException;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZTileLayerInfo;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * Unit test for {@link XYZImageProvider}.
 */
public class XYZImageProviderTest
{
    /**
     * The test image key.
     */
    private static final ZYXImageKey ourImageKey = new ZYXImageKey(3, 2, 1,
            new GeographicBoundingBox(LatLonAlt.createFromDegrees(10, 10), LatLonAlt.createFromDegrees(11, 11)));

    /**
     * The test layer.
     */
    private static final XYZTileLayerInfo ourLayer = new XYZTileLayerInfo("mapbox.dark", "Dark", Projection.EPSG_3857, 1, false,
            0, new XYZServerInfo("serverName", "http://mapbox.geointapps.org"));

    /**
     * Tests providing an image.
     */
    @Test
    public void testImage()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = createRegistry(support, true);

        support.replayAll();

        XYZImageProvider provider = new XYZImageProvider(registry, ourLayer);
        Image image = provider.getImage(ourImageKey);

        assertTrue(image instanceof DDSImage);
        assertTrue(((DDSImage)image).getByteBuffer().remaining() > 0);

        support.verifyAll();
    }

    /**
     * Tests providing an image when there isn't one.
     */
    @Test
    public void testNoImage()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = createRegistry(support, false);

        support.replayAll();

        XYZImageProvider provider = new XYZImageProvider(registry, ourLayer);
        Image image = provider.getImage(ourImageKey);

        assertNull(image);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @param returnResults True if image should be returned in the query, false
     *            if not.
     * @return The mocked data registry.
     */
    private DataRegistry createRegistry(EasyMockSupport support, boolean returnResults)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);
        QueryTracker tracker = support.createMock(QueryTracker.class);
        if (!returnResults)
        {
            EasyMock.expect(tracker.getException()).andReturn(null);
        }

        EasyMock.expect(registry.performQuery(EasyMock.isA(SimpleQuery.class)))
                .andAnswer(() -> queryAnswer(tracker, returnResults));

        return registry;
    }

    /**
     * The answer for the query.
     *
     * @param tracker An easy mocked {@link QueryTracker} to return.
     * @param returnResults True if image should be returned in the query, false
     *            if not.
     * @return The passed in {@link QueryTracker}.
     * @throws IOException Bad IO.
     * @throws ImageFormatUnknownException Bad image.
     */
    private QueryTracker queryAnswer(QueryTracker tracker, boolean returnResults) throws IOException, ImageFormatUnknownException
    {
        @SuppressWarnings("unchecked")
        SimpleQuery<InputStream> query = (SimpleQuery<InputStream>)EasyMock.getCurrentArguments()[0];

        DataModelCategory category = query.getDataModelCategory();

        assertEquals(new DataModelCategory(ourLayer.getServerUrl(), XYZTileUtils.TILES_FAMILY, ourLayer.getName()), category);

        @SuppressWarnings("unchecked")
        PropertyMatcher<String> propertyMatcher = (PropertyMatcher<String>)query.getParameters().get(0);

        assertEquals(XYZTileUtils.KEY_PROPERTY_DESCRIPTOR, propertyMatcher.getPropertyDescriptor());
        assertEquals(ourImageKey.toString(), propertyMatcher.getOperand());

        @SuppressWarnings("unchecked")
        PropertyValueReceiver<InputStream> receiver = (PropertyValueReceiver<InputStream>)query.getPropertyValueReceivers()
                .get(0);
        assertEquals(receiver.getPropertyDescriptor(), receiver.getPropertyDescriptor());

        if (returnResults)
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(ImageUtil.LOADING_IMAGE, "png", output);
            byte[] imageBytes = output.toByteArray();

            InputStream input = Image.getDDSImageStream(new ByteArrayInputStream(imageBytes), "png", imageBytes.length, null);

            receiver.receive(New.list(input));
        }

        return tracker;
    }
}
