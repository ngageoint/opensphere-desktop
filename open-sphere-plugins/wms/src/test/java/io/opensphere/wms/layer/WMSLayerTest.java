package io.opensphere.wms.layer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.image.DDSImage;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.wms.WMSTestToolbox;
import io.opensphere.wms.config.v1.WMSBoundingBoxConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;

/**
 * Test for {@link WMSLayer}.
 */
public class WMSLayerTest
{
    /** host name. */
    private static final String DATA_TYPE_HOST_NAME = "WMSLayerHost";

    /** image format. */
    private static final String IMAGE_FORMAT = "image/png";

    /** SRS. */
    private static final String SRS = "EPSG:4326";

    /** Layer key used for testing. */
    private static final String TEST_KEY = "layer_key";

    /** Layer title for testing. */
    private static final String TEST_TITLE = "title";

    /** Test generating a fixed grid. */
    @Test
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void testGenerateFixedGrid()
    {
        List<Vector2d> layerCellDimensions = new ArrayList<Vector2d>();
        layerCellDimensions.add(new Vector2d(45., 45.));
        layerCellDimensions.add(new Vector2d(22.5, 22.5));
        layerCellDimensions.add(new Vector2d(11.25, 11.25));

        WMSLayerConfig conf = createLayerConfig(GeographicBoundingBox.WHOLE_GLOBE);

        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, DATA_TYPE_HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());
        WMSLayer.Builder builder = new WMSLayer.Builder(info);
        builder.setLayerCellDimensions(layerCellDimensions);
        WMSLayer wmsLayer = new WMSLayer(builder);

        List<BoundingBox<?>> grid;
        BoundingBox<?> expected;

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateFixedGrid(0));
        assertEquals(32, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90., -180.), LatLonAlt.createFromDegrees(-45., -135.));
        assertEquals(expected, grid.get(0));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0., -135.), LatLonAlt.createFromDegrees(45., -90.));
        assertEquals(expected, grid.get(17));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(45., 135), LatLonAlt.createFromDegrees(90., 180.));
        assertEquals(expected, grid.get(31));

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateFixedGrid(1));
        assertEquals(128, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90., -180.),
                LatLonAlt.createFromDegrees(-67.5, -157.5));
        assertEquals(expected, grid.get(0));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0., -157.5), LatLonAlt.createFromDegrees(22.5, -135.));
        assertEquals(expected, grid.get(65));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(67.5, 157.5), LatLonAlt.createFromDegrees(90., 180.));
        assertEquals(expected, grid.get(127));

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateFixedGrid(2));
        assertEquals(512, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90., -180.),
                LatLonAlt.createFromDegrees(-78.75, -168.75));
        assertEquals(expected, grid.get(0));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0., -146.25), LatLonAlt.createFromDegrees(11.25, -135.));
        assertEquals(expected, grid.get(259));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(78.75, 168.75), LatLonAlt.createFromDegrees(90., 180.));
        assertEquals(expected, grid.get(511));

        try
        {
            wmsLayer.generateFixedGrid(3);
            Assert.fail("Should have thrown exception.");
        }
        catch (IndexOutOfBoundsException e)
        {
            // expected
        }

        layerCellDimensions.clear();
        layerCellDimensions.add(new Vector2d(360., 180.));

        builder.setLayerCellDimensions(layerCellDimensions);
        wmsLayer = new WMSLayer(builder);

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateFixedGrid(0));
        assertEquals(1, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90., -180.), LatLonAlt.createFromDegrees(90., 180.));
        assertEquals(expected, grid.get(0));
    }

    /** Test generating a fixed grid with a layer that has a bounding box. */
    @Test
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void testGenerateFixedGridWithBoundingBox()
    {
        GeographicBoundingBox boundingBox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10.2, -46.6),
                LatLonAlt.createFromDegrees(31.6, -1.4));

        List<Vector2d> layerCellDimensions = new ArrayList<Vector2d>();
        Vector2d fiveByFive = new Vector2d(5., 5.);
        Vector2d twentyByTwenty = new Vector2d(20., 20.);
        Vector2d tenByTen = new Vector2d(10., 10.);
        Vector2d sixtyBySixty = new Vector2d(60., 60.);
        layerCellDimensions.add(twentyByTwenty);
        layerCellDimensions.add(tenByTen);
        layerCellDimensions.add(fiveByFive);
        layerCellDimensions.add(sixtyBySixty);

        WMSLayerConfig conf = createLayerConfig(boundingBox);
        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, DATA_TYPE_HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());

        WMSLayer.Builder builder = new WMSLayer.Builder(info);
        builder.setLayerCellDimensions(layerCellDimensions);
        WMSLayer wmsLayer = new WMSLayer(builder);

        List<BoundingBox<?>> grid;
        BoundingBox<?> expected;

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateFixedGrid(0));
        assertEquals(2, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-30., -60.), LatLonAlt.createFromDegrees(30., 0.));
        assertEquals(expected, grid.get(0));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(30., -60.), LatLonAlt.createFromDegrees(90., 0.));
        assertEquals(expected, grid.get(1));

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateFixedGrid(1));
        assertEquals(6, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10., -60.), LatLonAlt.createFromDegrees(30., -40.));
        assertEquals(expected, grid.get(0));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10., -40.), LatLonAlt.createFromDegrees(30., -20.));
        assertEquals(expected, grid.get(1));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10., -20.), LatLonAlt.createFromDegrees(30., 0.));
        assertEquals(expected, grid.get(2));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(30., -60.), LatLonAlt.createFromDegrees(50., -40.));
        assertEquals(expected, grid.get(3));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(30., -40.), LatLonAlt.createFromDegrees(50., -20.));
        assertEquals(expected, grid.get(4));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(30., -20.), LatLonAlt.createFromDegrees(50., 0.));
        assertEquals(expected, grid.get(5));

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateFixedGrid(2));
        assertEquals(15, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10., -50.), LatLonAlt.createFromDegrees(20., -40));
        assertEquals(expected, grid.get(0));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(30., -10.), LatLonAlt.createFromDegrees(40., 0.));
        assertEquals(expected, grid.get(14));

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateFixedGrid(3));
        assertEquals(50, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10., -50.), LatLonAlt.createFromDegrees(15., -45.));
        assertEquals(expected, grid.get(0));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(30., -5.), LatLonAlt.createFromDegrees(35., 0.));
        assertEquals(expected, grid.get(49));

        try
        {
            wmsLayer.generateFixedGrid(4);
            Assert.fail("Should have thrown exception.");
        }
        catch (IndexOutOfBoundsException e)
        {
            // expected
        }

        layerCellDimensions.clear();
        layerCellDimensions.add(new Vector2d(360., 180.));

        builder.setLayerCellDimensions(layerCellDimensions);
        wmsLayer = new WMSLayer(builder);

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateFixedGrid(0));
        assertEquals(1, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90., -180.), LatLonAlt.createFromDegrees(90., 180.));
        assertEquals(expected, grid.get(0));
    }

    /** Test generating a grid. */
    @Test
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void testGenerateGrid()
    {
        List<Vector2d> layerCellDimensions = new ArrayList<Vector2d>();
        Vector2d fiveByFive = new Vector2d(5., 5.);
        Vector2d twentyByTwenty = new Vector2d(20., 20.);
        Vector2d tenByTen = new Vector2d(10., 10.);
        layerCellDimensions.add(twentyByTwenty);
        layerCellDimensions.add(tenByTen);
        layerCellDimensions.add(fiveByFive);

        WMSLayerConfig conf = createLayerConfig(GeographicBoundingBox.WHOLE_GLOBE);

        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, DATA_TYPE_HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());
        WMSLayer.Builder builder = new WMSLayer.Builder(info);
        builder.setLayerCellDimensions(layerCellDimensions);
        WMSLayer wmsLayer = new WMSLayer(builder);

        List<BoundingBox<?>> grid;
        BoundingBox<?> expected;

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateGrid(0));
        assertEquals(162, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90., -180.), LatLonAlt.createFromDegrees(-70., -160.));
        assertEquals(expected, grid.get(0));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(70., 160.), LatLonAlt.createFromDegrees(90., 180.));
        assertEquals(expected, grid.get(161));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10., 0.), LatLonAlt.createFromDegrees(10., 20.));
        assertEquals(expected, grid.get(81));

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateGrid(1));
        assertEquals(648, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90., -180.), LatLonAlt.createFromDegrees(-80., -170.));
        assertEquals(expected, grid.get(0));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(80., 170.), LatLonAlt.createFromDegrees(90., 180.));
        assertEquals(expected, grid.get(647));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0., 0.), LatLonAlt.createFromDegrees(10., 10.));
        assertEquals(expected, grid.get(342));

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateGrid(2));
        assertEquals(2592, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90., -180.), LatLonAlt.createFromDegrees(-85., -175.));
        assertEquals(expected, grid.get(0));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(85., 175.), LatLonAlt.createFromDegrees(90., 180.));
        assertEquals(expected, grid.get(2591));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0., 0.), LatLonAlt.createFromDegrees(5., 5.));
        assertEquals(expected, grid.get(1332));

        try
        {
            wmsLayer.generateFixedGrid(3);
            Assert.fail("Should have thrown exception.");
        }
        catch (IndexOutOfBoundsException e)
        {
            // expected
        }
    }

    /** Test generating a grid with a layer that has a bounding box. */
    @Test
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void testGenerateGridWithBoundingBox()
    {
        GeographicBoundingBox boundingBox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10.2, -46.6),
                LatLonAlt.createFromDegrees(31.6, -1.4));

        List<Vector2d> layerCellDimensions = new ArrayList<Vector2d>();
        Vector2d fiveByFive = new Vector2d(5., 5.);
        Vector2d twentyByTwenty = new Vector2d(20., 20.);
        Vector2d tenByTen = new Vector2d(10., 10.);
        layerCellDimensions.add(twentyByTwenty);
        layerCellDimensions.add(tenByTen);
        layerCellDimensions.add(fiveByFive);

        WMSLayerConfig conf = createLayerConfig(boundingBox);
        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, DATA_TYPE_HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());

        WMSLayer.Builder builder = new WMSLayer.Builder(info);
        builder.setLayerCellDimensions(layerCellDimensions);
        WMSLayer wmsLayer = new WMSLayer(builder);

        List<BoundingBox<?>> grid;
        BoundingBox<?> expected;

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateGrid(0));
        assertEquals(6, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10.2, -46.6), LatLonAlt.createFromDegrees(30.2, -26.6));
        assertEquals(expected, grid.get(0));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10.2, -26.6), LatLonAlt.createFromDegrees(30.2, -6.6));
        assertEquals(expected, grid.get(1));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10.2, -6.6), LatLonAlt.createFromDegrees(30.2, -1.4));
        assertEquals(expected, grid.get(2));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(30.2, -46.6), LatLonAlt.createFromDegrees(31.6, -26.6));
        assertEquals(expected, grid.get(3));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(30.2, -26.6), LatLonAlt.createFromDegrees(31.6, -6.6));
        assertEquals(expected, grid.get(4));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(30.2, -6.6), LatLonAlt.createFromDegrees(31.6, -1.4));
        assertEquals(expected, grid.get(5));

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateGrid(1));
        assertEquals(15, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10.2, -46.6), LatLonAlt.createFromDegrees(20.2, -36.6));
        assertEquals(expected, grid.get(0));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(30.2, -6.6), LatLonAlt.createFromDegrees(31.6, -1.4));
        assertEquals(expected, grid.get(14));

        grid = new ArrayList<BoundingBox<?>>(wmsLayer.generateGrid(2));
        assertEquals(50, grid.size());
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10.2, -46.6), LatLonAlt.createFromDegrees(15.2, -41.6));
        assertEquals(expected, grid.get(0));
        expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(30.2, -1.6), LatLonAlt.createFromDegrees(31.6, -1.4));
        assertEquals(expected, grid.get(49));

        try
        {
            wmsLayer.generateFixedGrid(3);
            Assert.fail("Should have thrown exception.");
        }
        catch (IndexOutOfBoundsException e)
        {
            // expected
        }
    }

    /**
     * Test getting an image.
     *
     * @throws IOException If an error occurs.
     */
    @Test
    public void testGetImage() throws IOException
    {
        List<Vector2d> layerCellDimensions = new ArrayList<Vector2d>();
        Vector2d twentyByTwenty = new Vector2d(20., 20.);
        Vector2d tenByTen = new Vector2d(10., 10.);
        Vector2d fiveByFive = new Vector2d(5., 5.);
        layerCellDimensions.add(twentyByTwenty);
        layerCellDimensions.add(tenByTen);
        layerCellDimensions.add(fiveByFive);

        final Image expectedImage = new DDSImage(new byte[0]);
        GeographicBoundingBox bbox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0., 0.),
                LatLonAlt.createFromDegrees(1., 1.));
        TimeSpan timeSpan = TimeSpan.get(100L, 200L);
        final TileImageKey imageKey = new TileImageKey(bbox, timeSpan);
        ImageProvider<TileImageKey> imageProvider = new ImageProvider<TileImageKey>()
        {
            @Override
            public Image getImage(TileImageKey key)
            {
                return key.equals(imageKey) ? expectedImage : null;
            }
        };

        WMSLayerConfig conf = createLayerConfig(GeographicBoundingBox.WHOLE_GLOBE);
        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, DATA_TYPE_HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());

        WMSLayer.Builder builder = new WMSLayer.Builder(info);
        builder.setLayerCellDimensions(layerCellDimensions);
        builder.setImageProvider(imageProvider);
        WMSLayer wmsLayer = new WMSLayer(builder);

        Image image = wmsLayer.getImage(imageKey);
        assertEquals(expectedImage, image);
    }

    /** Test getting an image with no image provider. */
    @Test(expected = IllegalStateException.class)
    public void testGetImageNullImageProvider()
    {
        List<Vector2d> emptyList = Collections.emptyList();

        WMSLayerConfig conf = createLayerConfig(GeographicBoundingBox.WHOLE_GLOBE);
        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, DATA_TYPE_HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());

        WMSLayer.Builder builder = new WMSLayer.Builder(info);
        builder.setLayerCellDimensions(emptyList);
        WMSLayer wmsLayer = new WMSLayer(builder);
        wmsLayer.getImage((TileImageKey)null);
    }

    /** Test getting the minimum grid size. */
    @Test
    public void testGetMinimumGridSize()
    {
        List<Vector2d> layerCellDimensions = new ArrayList<Vector2d>();
        Vector2d twentyByTwenty = new Vector2d(20., 20.);
        Vector2d tenByTen = new Vector2d(10., 10.);
        Vector2d fiveByFive = new Vector2d(5., 5.);
        layerCellDimensions.add(twentyByTwenty);
        layerCellDimensions.add(tenByTen);
        layerCellDimensions.add(fiveByFive);

        WMSLayerConfig conf = createLayerConfig(GeographicBoundingBox.WHOLE_GLOBE);
        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, DATA_TYPE_HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());

        WMSLayer.Builder builder = new WMSLayer.Builder(info);
        builder.setLayerCellDimensions(layerCellDimensions);
        WMSLayer wmsLayer = new WMSLayer(builder);
        Vector2d minimumGridSize = wmsLayer.getMinimumGridSize();
        assertEquals(fiveByFive, minimumGridSize);

        layerCellDimensions.clear();
        layerCellDimensions.add(fiveByFive);
        layerCellDimensions.add(twentyByTwenty);
        layerCellDimensions.add(tenByTen);
        wmsLayer = new WMSLayer(builder);
        minimumGridSize = wmsLayer.getMinimumGridSize();
        assertEquals(fiveByFive, minimumGridSize);
    }

    /** Test normal construction. */
    @SuppressWarnings("unused")
    @Test
    public void testWMSLayer()
    {
        WMSLayerConfig conf = createLayerConfig(GeographicBoundingBox.WHOLE_GLOBE);
        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, DATA_TYPE_HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());

        List<Vector2d> emptyList = Collections.emptyList();
        WMSLayer.Builder builder = new WMSLayer.Builder(info);
        builder.setLayerCellDimensions(emptyList);
        new WMSLayer(builder);
    }

    /** Test construction with null cell dimensions. */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testWMSLayerNullCellDimensions()
    {
        WMSLayerConfig conf = new WMSLayerConfig();
        conf.setLayerTitle(TEST_TITLE);
        conf.setLayerKey(TEST_KEY);
        conf.getGetMapConfig().setImageFormat(IMAGE_FORMAT);
        WMSBoundingBoxConfig bboxConf = new WMSBoundingBoxConfig();
        bboxConf.setGeographicBoundingBox(GeographicBoundingBox.WHOLE_GLOBE);
        conf.setBoundingBoxConfig(bboxConf);
        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, DATA_TYPE_HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());

        WMSLayer.Builder builder = new WMSLayer.Builder(info);
        new WMSLayer(builder);
    }

    /**
     * Create a layer config for testing.
     *
     * @param boundingBox The bounding box.
     * @return The layer config.
     */
    private WMSLayerConfig createLayerConfig(GeographicBoundingBox boundingBox)
    {
        WMSLayerConfig conf = new WMSLayerConfig();
        conf.setLayerTitle(TEST_TITLE);
        conf.setLayerKey(TEST_KEY);
        conf.getGetMapConfig().setImageFormat(IMAGE_FORMAT);
        conf.getGetMapConfig().setSRS(SRS);
        WMSBoundingBoxConfig bboxConf = new WMSBoundingBoxConfig();
        bboxConf.setGeographicBoundingBox(boundingBox);
        conf.setBoundingBoxConfig(bboxConf);
        return conf;
    }

    /**
     * Gets the test toolbox.
     *
     * @return the toolbox
     */
    private Toolbox getToolbox()
    {
        return WMSTestToolbox.getToolbox(true);
    }
}
