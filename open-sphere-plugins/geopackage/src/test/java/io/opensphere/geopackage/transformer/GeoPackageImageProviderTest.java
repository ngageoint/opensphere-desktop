package io.opensphere.geopackage.transformer;

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
import org.junit.Ignore;
import org.junit.Test;

import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
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
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.geopackage.model.GeoPackagePropertyDescriptors;
import io.opensphere.geopackage.model.GeoPackageTileLayer;

/**
 * Unit test for {@link GeoPackageImageProvider}.
 */
public class GeoPackageImageProviderTest
{
    /**
     * The test image key.
     */
    private static final ZYXImageKey ourImageKey = new ZYXImageKey(0, 0, 0,
            new GeographicBoundingBox(LatLonAlt.createFromDegrees(10, 10), LatLonAlt.createFromDegrees(11, 11)));

    /**
     * The test layer name.
     */
    private static final String ourLayerName = "testLayer";

    /**
     * The test package file.
     */
    private static final String ourPackageFile = "c:\\somefile.gpkg";

    /**
     * The test package name.
     */
    private static final String ourPackageName = "IamPackage";

    /**
     * Tests providing an image.
     */
    @Test
    @Ignore
    public void testImage()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = createRegistry(support, true);
        GeoPackageTileLayer layer = new GeoPackageTileLayer(ourPackageName, ourPackageFile, ourLayerName, 1000);
        UIRegistry uiRegistry = createUIRegistry(support);

        support.replayAll();

        GeoPackageImageProvider provider = new GeoPackageImageProvider(registry, layer,
                new GeoPackageQueryTracker(uiRegistry, ourLayerName));
        Image image = provider.getImage(ourImageKey);

        assertTrue(image instanceof DDSImage);
        assertTrue(((DDSImage)image).getByteBuffer().array().length > 0);

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
        GeoPackageTileLayer layer = new GeoPackageTileLayer(ourPackageName, ourPackageFile, ourLayerName, 1000);
        UIRegistry uiRegistry = createUIRegistry(support);

        support.replayAll();

        GeoPackageImageProvider provider = new GeoPackageImageProvider(registry, layer,
                new GeoPackageQueryTracker(uiRegistry, ourLayerName));
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
     * Creates an easy mocked {@link UIRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link UIRegistry}.
     */
    private UIRegistry createUIRegistry(EasyMockSupport support)
    {
        MenuBarRegistry menuRegistry = support.createMock(MenuBarRegistry.class);
        menuRegistry.addTaskActivity(EasyMock.isA(TaskActivity.class));

        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        EasyMock.expect(uiRegistry.getMenuBarRegistry()).andReturn(menuRegistry);

        return uiRegistry;
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

        assertEquals(new DataModelCategory(ourPackageFile, ourLayerName, Image.class.getName()), category);

        @SuppressWarnings("unchecked")
        PropertyMatcher<String> propertyMatcher = (PropertyMatcher<String>)query.getParameters().get(0);

        assertEquals(GeoPackagePropertyDescriptors.KEY_PROPERTY_DESCRIPTOR, propertyMatcher.getPropertyDescriptor());
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
