package io.opensphere.osh.aerialimagery.transformer.geometrybuilders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.model.GeographicConvexQuadrilateral;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.aerialimagery.model.PlatformMetadataAndImage;

/**
 * Unit test for {@link ImageGeometryBuilder}.
 */
public class ImageGeometryBuilderTest
{
    /**
     * The test opacity.
     */
    private static final int ourOpacity = 150;

    /**
     * The test type key.
     */
    private static final String ourTypeKey = "Iamatypekey";

    /**
     * The test zorder.
     */
    private static final int ourZOrder = 121;

    /**
     * Tests building geometries.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testBuildGeometries() throws IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo uavDataType = support.createMock(DataTypeInfo.class);
        OrderParticipantKey key = support.createMock(OrderParticipantKey.class);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, key);
        DataTypeInfo videoLayer = createVideoLayer(support, key);
        PlatformMetadata model = createTestData(1).get(0);

        support.replayAll();

        ImageGeometryBuilder builder = new ImageGeometryBuilder(orderRegistry);

        Pair<List<Geometry>, List<Geometry>> addsAndRemoves = builder.buildGeometries(model, uavDataType, videoLayer);

        assertTrue(addsAndRemoves.getSecondObject().isEmpty());

        List<Geometry> adds = addsAndRemoves.getFirstObject();

        assertEquals(1, adds.size());

        TileGeometry geometry = (TileGeometry)adds.get(0);
        assertNull(geometry.getSplitJoinRequestProvider());
        assertNull(geometry.getParent());
        assertTrue(geometry.isRapidUpdate());
        assertEquals(model.getFootprint(), geometry.getBounds());

        TileRenderProperties props = geometry.getRenderProperties();
        assertEquals(ourZOrder, props.getZOrder());
        assertTrue(props.isDrawable());
        assertFalse(props.isPickable());
        assertEquals(ourOpacity, props.getOpacity(), 0f);

        ImageManager imageManager = geometry.getImageManager();
        assertDirtyRegion(imageManager);
        assertNotNull(((ImageIOImage)imageManager.getCachedImageData().getImageMap().values().iterator().next()).getAWTImage());

        support.verifyAll();
    }

    /**
     * Tests building a new geometry and removing an old one.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testBuildGeometriesAddAndRemove() throws IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo uavDataType = support.createMock(DataTypeInfo.class);
        OrderParticipantKey key = support.createMock(OrderParticipantKey.class);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, key);
        DataTypeInfo videoLayer = createVideoLayer(support, key);
        List<PlatformMetadataAndImage> models = createTestData(2);

        support.replayAll();

        ImageGeometryBuilder builder = new ImageGeometryBuilder(orderRegistry);

        Pair<List<Geometry>, List<Geometry>> addsAndRemoves = builder.buildGeometries(models.get(0), uavDataType, videoLayer);

        assertTrue(addsAndRemoves.getSecondObject().isEmpty());

        List<Geometry> adds = addsAndRemoves.getFirstObject();

        assertEquals(1, adds.size());

        TileGeometry geometry = (TileGeometry)adds.get(0);
        assertNull(geometry.getSplitJoinRequestProvider());
        assertNull(geometry.getParent());
        assertTrue(geometry.isRapidUpdate());
        assertEquals(models.get(0).getFootprint(), geometry.getBounds());

        TileRenderProperties props = geometry.getRenderProperties();
        assertEquals(ourZOrder, props.getZOrder());
        assertTrue(props.isDrawable());
        assertFalse(props.isPickable());
        assertEquals(ourOpacity, props.getOpacity(), 0f);

        ImageManager imageManager = geometry.getImageManager();
        assertDirtyRegion(imageManager);
        BufferedImage firstImage = ((ImageIOImage)imageManager.getCachedImageData().getImageMap().values().iterator().next())
                .getAWTImage();
        assertNotNull(firstImage);

        addsAndRemoves = builder.buildGeometries(models.get(1), uavDataType, videoLayer);

        assertEquals(1, addsAndRemoves.getSecondObject().size());
        assertEquals(geometry, addsAndRemoves.getSecondObject().get(0));

        adds = addsAndRemoves.getFirstObject();

        assertEquals(1, adds.size());

        geometry = (TileGeometry)adds.get(0);
        assertNull(geometry.getSplitJoinRequestProvider());
        assertNull(geometry.getParent());
        assertTrue(geometry.isRapidUpdate());
        assertEquals(models.get(1).getFootprint(), geometry.getBounds());

        props = geometry.getRenderProperties();
        assertEquals(ourZOrder, props.getZOrder());
        assertTrue(props.isDrawable());
        assertFalse(props.isPickable());
        assertEquals(ourOpacity, props.getOpacity(), 0f);

        imageManager = geometry.getImageManager();
        assertDirtyRegion(imageManager);
        BufferedImage image = ((ImageIOImage)imageManager.getCachedImageData().getImageMap().values().iterator().next())
                .getAWTImage();
        assertFalse(firstImage.equals(image));

        support.verifyAll();
    }

    /**
     * Tests build geometries when there isn't an image.
     */
    @Test
    public void testBuildGeometriesNoImage()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo uavDataType = support.createMock(DataTypeInfo.class);
        OrderParticipantKey key = support.createMock(OrderParticipantKey.class);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, key);
        DataTypeInfo videoLayer = createVideoLayer(support, key);
        PlatformMetadata model = new PlatformMetadata();
        GeographicConvexQuadrilateral footprint = new GeographicConvexQuadrilateral(
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 10)));
        model.setFootprint(footprint);

        support.replayAll();

        ImageGeometryBuilder builder = new ImageGeometryBuilder(orderRegistry);

        Pair<List<Geometry>, List<Geometry>> addsAndRemoves = builder.buildGeometries(model, uavDataType, videoLayer);

        assertTrue(addsAndRemoves.getSecondObject().isEmpty());
        assertTrue(addsAndRemoves.getFirstObject().isEmpty());

        support.verifyAll();
    }

    /**
     * Tests building geometries when only the image changes but the footprint
     * is the same.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testBuildGeometriesUpdateImage() throws IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo uavDataType = support.createMock(DataTypeInfo.class);
        OrderParticipantKey key = support.createMock(OrderParticipantKey.class);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, key);
        DataTypeInfo videoLayer = createVideoLayer(support, key);
        List<PlatformMetadataAndImage> models = createTestData(2);
        models.get(1).setFootprint(models.get(0).getFootprint());

        support.replayAll();

        ImageGeometryBuilder builder = new ImageGeometryBuilder(orderRegistry);

        Pair<List<Geometry>, List<Geometry>> addsAndRemoves = builder.buildGeometries(models.get(0), uavDataType, videoLayer);

        assertTrue(addsAndRemoves.getSecondObject().isEmpty());

        List<Geometry> adds = addsAndRemoves.getFirstObject();

        assertEquals(1, adds.size());

        TileGeometry geometry = (TileGeometry)adds.get(0);
        assertNull(geometry.getSplitJoinRequestProvider());
        assertNull(geometry.getParent());
        assertTrue(geometry.isRapidUpdate());
        assertEquals(models.get(0).getFootprint(), geometry.getBounds());

        TileRenderProperties props = geometry.getRenderProperties();
        assertEquals(ourZOrder, props.getZOrder());
        assertTrue(props.isDrawable());
        assertFalse(props.isPickable());
        assertEquals(ourOpacity, props.getOpacity(), 0f);

        ImageManager imageManager = geometry.getImageManager();
        assertDirtyRegion(imageManager);
        BufferedImage firstImage = ((ImageIOImage)imageManager.getCachedImageData().getImageMap().values().iterator().next())
                .getAWTImage();
        assertNotNull(firstImage);

        addsAndRemoves = builder.buildGeometries(models.get(1), uavDataType, videoLayer);
        assertTrue(addsAndRemoves.getFirstObject().isEmpty());
        assertTrue(addsAndRemoves.getSecondObject().isEmpty());

        imageManager = geometry.getImageManager();
        assertDirtyRegion(imageManager);
        BufferedImage image = ((ImageIOImage)imageManager.getCachedImageData().getImageMap().values().iterator().next())
                .getAWTImage();
        assertFalse(firstImage.equals(image));

        support.verifyAll();
    }

    /**
     * Verifies the cache geometries value.
     */
    @Test
    public void testCachePublishedGeometries()
    {
        EasyMockSupport support = new EasyMockSupport();

        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);

        support.replayAll();

        ImageGeometryBuilder builder = new ImageGeometryBuilder(orderRegistry);
        assertFalse(builder.cachePublishedGeometries());

        support.verifyAll();
    }

    /**
     * Tests closing.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testClose() throws IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo uavDataType = support.createMock(DataTypeInfo.class);
        OrderParticipantKey key = support.createMock(OrderParticipantKey.class);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, key);
        DataTypeInfo videoLayer = createVideoLayer(support, key);
        PlatformMetadata model = createTestData(1).get(0);

        support.replayAll();

        ImageGeometryBuilder builder = new ImageGeometryBuilder(orderRegistry);

        Pair<List<Geometry>, List<Geometry>> addsAndRemoves = builder.buildGeometries(model, uavDataType, videoLayer);

        assertTrue(addsAndRemoves.getSecondObject().isEmpty());

        List<Geometry> adds = addsAndRemoves.getFirstObject();

        assertEquals(1, adds.size());

        TileGeometry geometry = (TileGeometry)adds.get(0);
        assertNull(geometry.getSplitJoinRequestProvider());
        assertNull(geometry.getParent());
        assertTrue(geometry.isRapidUpdate());
        assertEquals(model.getFootprint(), geometry.getBounds());

        TileRenderProperties props = geometry.getRenderProperties();
        assertEquals(ourZOrder, props.getZOrder());
        assertTrue(props.isDrawable());
        assertFalse(props.isPickable());
        assertEquals(ourOpacity, props.getOpacity(), 0f);

        ImageManager imageManager = geometry.getImageManager();
        assertDirtyRegion(imageManager);
        assertNotNull(((ImageIOImage)imageManager.getCachedImageData().getImageMap().values().iterator().next()).getAWTImage());

        List<Geometry> geoms = builder.close();

        assertEquals(1, geoms.size());
        assertEquals(geometry, geoms.get(0));

        support.verifyAll();
    }

    /**
     * Asserts the image manager's dirty regions.
     *
     * @param imageManager The image manager to assert.
     */
    private void assertDirtyRegion(ImageManager imageManager)
    {
        Collection<? extends ImageManager.DirtyRegion> dirtyRegions = imageManager.getDirtyRegions();
        assertEquals(1, dirtyRegions.size());

        ImageManager.DirtyRegion dirtyRegion = dirtyRegions.iterator().next();

        assertEquals(0, dirtyRegion.getMinX());
        assertEquals(0, dirtyRegion.getMinY());
        assertEquals(100, dirtyRegion.getMaxX());
        assertEquals(100, dirtyRegion.getMaxY());
    }

    /**
     * Creates the order registry.
     *
     * @param support Used to create the mock.
     * @param key The expected order participant key.
     * @return The mocked {@link OrderManagerRegistry}.
     */
    private OrderManagerRegistry createOrderRegistry(EasyMockSupport support, OrderParticipantKey key)
    {
        OrderManager orderManager = support.createMock(OrderManager.class);
        EasyMock.expect(Integer.valueOf(orderManager.getOrder(EasyMock.eq(key)))).andReturn(Integer.valueOf(ourZOrder))
                .atLeastOnce();

        OrderManagerRegistry registry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(registry.getOrderManager(EasyMock.eq(key))).andReturn(orderManager).atLeastOnce();

        return registry;
    }

    /**
     * Creates test data.
     *
     * @param count The number of metadatas to create.
     * @return The test data.
     * @throws IOException Bad IO.
     */
    private List<PlatformMetadataAndImage> createTestData(int count) throws IOException
    {
        List<PlatformMetadataAndImage> testDatas = New.list();

        for (int i = 0; i < count; i++)
        {
            PlatformMetadata metadata = new PlatformMetadata();

            GeographicConvexQuadrilateral footprint = new GeographicConvexQuadrilateral(
                    new GeographicPosition(LatLonAlt.createFromDegrees(10 + i, 0 + i)),
                    new GeographicPosition(LatLonAlt.createFromDegrees(0 + i, 0 + i)),
                    new GeographicPosition(LatLonAlt.createFromDegrees(0 + i, 10 + i)),
                    new GeographicPosition(LatLonAlt.createFromDegrees(10 + i, 10 + i)));
            metadata.setFootprint(footprint);

            BufferedImage image = ImageUtil.LOADING_IMAGE;
            if (i % 2 != 0)
            {
                image = ImageUtil.BROKEN_IMAGE;
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            boolean foundWriter = ImageIO.write(image, "png", output);
            assert foundWriter;

            testDatas.add(new PlatformMetadataAndImage(metadata, ByteBuffer.wrap(output.toByteArray())));
            
        }

        return testDatas;
    }

    /**
     * Creates the video layer.
     *
     * @param support Used to create the mock.
     * @param key The expected order key.
     * @return The mocked video layer.
     */
    private DataTypeInfo createVideoLayer(EasyMockSupport support, OrderParticipantKey key)
    {
        BasicVisualizationInfo visInfo = support.createMock(BasicVisualizationInfo.class);

        DataTypeInfo videoLayer = support.createMock(DataTypeInfo.class);

        EasyMock.expect(videoLayer.getTypeKey()).andReturn(ourTypeKey).atLeastOnce();
        EasyMock.expect(Integer.valueOf(visInfo.getTypeOpacity())).andReturn(Integer.valueOf(ourOpacity)).atLeastOnce();
        EasyMock.expect(videoLayer.getBasicVisualizationInfo()).andReturn(visInfo).atLeastOnce();
        EasyMock.expect(videoLayer.getOrderKey()).andReturn(key).atLeastOnce();

        return videoLayer;
    }
}
