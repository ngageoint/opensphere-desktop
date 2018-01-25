package io.opensphere.wms;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.image.DDSImage;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.wms.config.v1.WMSBoundingBoxConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;
import io.opensphere.wms.layer.TileImageKey;
import io.opensphere.wms.layer.WMSDataTypeInfo;

/**
 * Test for {@link FileWMSLayer}.
 */
public class FileWMSLayerTest
{
    /** Constant to use for the image format. */
    private static final String IMAGE_FORMAT = "notnull";

    /** The Constant HOST_NAME. */
    private static final String HOST_NAME = "WMSFileHostName";

    /** Stub LRC image provider. */
    private static final ImageProvider<LevelRowCol> NULL_LRC_IMAGE_PROVIDER = new ImageProvider<LevelRowCol>()
    {
        @Override
        public Image getImage(LevelRowCol key)
        {
            return null;
        }
    };

    /** Layer title used for testing. */
    private static final String TEST_TITLE = "title";

    /** Layer key used for testing. */
    private static final String TEST_KEY = "layer_key";

    /**
     * Test getting the grid coordinates.
     */
    @Test
    public void testGetGridCoordinates()
    {
        List<Vector2d> layerCellDimensions = new ArrayList<>();
        Vector2d twentyByTwenty = new Vector2d(20., 20.);
        Vector2d tenByTen = new Vector2d(10., 10.);
        Vector2d fiveByFive = new Vector2d(5., 5.);
        layerCellDimensions.add(twentyByTwenty);
        layerCellDimensions.add(tenByTen);
        layerCellDimensions.add(fiveByFive);
        WMSLayerConfig conf = new WMSLayerConfig();
        conf.setLayerTitle(TEST_TITLE);
        conf.setLayerKey(TEST_KEY);
        conf.setCacheImageFormat(IMAGE_FORMAT);
        conf.setBoundingBoxConfig(new WMSBoundingBoxConfig());
        conf.getBoundingBoxConfig().setGeographicBoundingBox(GeographicBoundingBox.WHOLE_GLOBE);
        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());

        FileWMSLayer.Builder builder = new FileWMSLayer.Builder(info);
        builder.setLRCImageProvider(NULL_LRC_IMAGE_PROVIDER);
        builder.setLayerCellDimensions(layerCellDimensions);
        FileWMSLayer wmsLayer = new FileWMSLayer(builder);

        LatLonAlt coord;
        LevelRowCol gridCoordinates;
        LevelRowCol expected;

        coord = LatLonAlt.createFromDegrees(-90., -180.);
        gridCoordinates = wmsLayer.getGridCoordinates(coord, twentyByTwenty);
        expected = new LevelRowCol(0, 0, 0);
        assertEquals(expected, gridCoordinates);

        coord = LatLonAlt.createFromDegrees(-90., -180.);
        gridCoordinates = wmsLayer.getGridCoordinates(coord, tenByTen);
        expected = new LevelRowCol(1, 0, 0);
        assertEquals(expected, gridCoordinates);

        coord = LatLonAlt.createFromDegrees(80., 170.);
        gridCoordinates = wmsLayer.getGridCoordinates(coord, tenByTen);
        expected = new LevelRowCol(1, 17, 35);
        assertEquals(expected, gridCoordinates);

        coord = LatLonAlt.createFromDegrees(0., 0.);
        gridCoordinates = wmsLayer.getGridCoordinates(coord, tenByTen);
        expected = new LevelRowCol(1, 9, 18);
        assertEquals(expected, gridCoordinates);

        coord = LatLonAlt.createFromDegrees(30., 10.);
        gridCoordinates = wmsLayer.getGridCoordinates(coord, fiveByFive);
        expected = new LevelRowCol(2, 24, 38);
        assertEquals(expected, gridCoordinates);
    }

    /**
     * Get getting the grid coordinates for a layer with a bounding box.
     */
    @Test
    public void testGetGridCoordinatesWithBoundingBox()
    {
        GeographicBoundingBox boundingBox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10.2, -46.6),
                LatLonAlt.createFromDegrees(31.6, -1.4));

        List<Vector2d> layerCellDimensions = new ArrayList<>();
        Vector2d twentyByTwenty = new Vector2d(20., 20.);
        Vector2d tenByTen = new Vector2d(10., 10.);
        Vector2d fiveByFive = new Vector2d(5., 5.);
        layerCellDimensions.add(twentyByTwenty);
        layerCellDimensions.add(tenByTen);
        layerCellDimensions.add(fiveByFive);
        WMSLayerConfig conf = new WMSLayerConfig();
        conf.setLayerTitle(TEST_TITLE);
        conf.setLayerKey(TEST_KEY);
        WMSBoundingBoxConfig bboxConf = new WMSBoundingBoxConfig();
        bboxConf.setGeographicBoundingBox(boundingBox);
        conf.setBoundingBoxConfig(bboxConf);
        conf.setCacheImageFormat(IMAGE_FORMAT);
        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());

        FileWMSLayer.Builder builder = new FileWMSLayer.Builder(info);
        builder.setLRCImageProvider(NULL_LRC_IMAGE_PROVIDER);
        builder.setLayerCellDimensions(layerCellDimensions);
        FileWMSLayer wmsLayer = new FileWMSLayer(builder);

        LatLonAlt coord;
        LevelRowCol gridCoordinates;
        LevelRowCol expected;

        coord = LatLonAlt.createFromDegrees(10.2, -46.6);
        gridCoordinates = wmsLayer.getGridCoordinates(coord, twentyByTwenty);
        expected = new LevelRowCol(0, 0, 0);
        assertEquals(expected, gridCoordinates);

        gridCoordinates = wmsLayer.getGridCoordinates(coord, tenByTen);
        expected = new LevelRowCol(1, 0, 0);
        assertEquals(expected, gridCoordinates);

        coord = LatLonAlt.createFromDegrees(30.1, -6.7);
        gridCoordinates = wmsLayer.getGridCoordinates(coord, tenByTen);
        expected = new LevelRowCol(1, 1, 3);
        assertEquals(expected, gridCoordinates);

        coord = LatLonAlt.createFromDegrees(30.2, -6.6);
        gridCoordinates = wmsLayer.getGridCoordinates(coord, tenByTen);
        expected = new LevelRowCol(1, 2, 4);
        assertEquals(expected, gridCoordinates);

        coord = LatLonAlt.createFromDegrees(30.2, -1.6);
        gridCoordinates = wmsLayer.getGridCoordinates(coord, fiveByFive);
        expected = new LevelRowCol(2, 4, 9);
        assertEquals(expected, gridCoordinates);
    }

    /**
     * Test getting a texture.
     *
     * @throws IOException If an error occurs.
     */
    @Test
    public void testGetTexture() throws IOException
    {
        List<Vector2d> layerCellDimensions = new ArrayList<>();
        Vector2d twentyByTwenty = new Vector2d(20., 20.);
        Vector2d tenByTen = new Vector2d(10., 10.);
        Vector2d fiveByFive = new Vector2d(5., 5.);
        layerCellDimensions.add(twentyByTwenty);
        layerCellDimensions.add(tenByTen);
        layerCellDimensions.add(fiveByFive);

        final Image expectedImage = new DDSImage(new byte[0]);
        final LevelRowCol expectedLRC = new LevelRowCol(0, 0, 0);
        ImageProvider<LevelRowCol> imageProvider = new ImageProvider<LevelRowCol>()
        {
            @Override
            public Image getImage(LevelRowCol key)
            {
                return key.equals(expectedLRC) ? expectedImage : null;
            }
        };

        WMSLayerConfig conf = new WMSLayerConfig();
        conf.setLayerTitle(TEST_TITLE);
        conf.setLayerKey(TEST_KEY);
        conf.setCacheImageFormat(IMAGE_FORMAT);
        conf.setBoundingBoxConfig(new WMSBoundingBoxConfig());
        conf.getBoundingBoxConfig().setGeographicBoundingBox(GeographicBoundingBox.WHOLE_GLOBE);
        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());

        FileWMSLayer.Builder builder = new FileWMSLayer.Builder(info);
        builder.setLRCImageProvider(imageProvider);
        builder.setLayerCellDimensions(layerCellDimensions);
        FileWMSLayer wmsLayer = new FileWMSLayer(builder);

        GeographicBoundingBox bbox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90., -180.),
                LatLonAlt.createFromDegrees(-70., -160.));
        Image image = wmsLayer.getImage(new TileImageKey(bbox, (TimeSpan)null));
        assertEquals(expectedImage, image);
    }

    /**
     * Test normal construction.
     */
    @SuppressWarnings("unused")
    @Test
    public void testWMSLayer()
    {
        WMSLayerConfig conf = new WMSLayerConfig();
        conf.setLayerTitle(TEST_TITLE);
        conf.setLayerKey(TEST_KEY);
        conf.setCacheImageFormat(IMAGE_FORMAT);
        conf.setBoundingBoxConfig(new WMSBoundingBoxConfig());
        conf.getBoundingBoxConfig().setGeographicBoundingBox(GeographicBoundingBox.WHOLE_GLOBE);
        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());

        FileWMSLayer.Builder builder = new FileWMSLayer.Builder(info);
        builder.setLRCImageProvider(NULL_LRC_IMAGE_PROVIDER);
        builder.setLayerCellDimensions(Collections.<Vector2d>emptyList());
        new FileWMSLayer(builder);
    }

    /**
     * Test construction with null dimensions.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testWMSLayerNullCellDimensions()
    {
        WMSLayerConfig conf = new WMSLayerConfig();
        conf.setLayerTitle(TEST_TITLE);
        conf.setCacheImageFormat(IMAGE_FORMAT);
        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());

        FileWMSLayer.Builder builder = new FileWMSLayer.Builder(info);
        builder.setLRCImageProvider(NULL_LRC_IMAGE_PROVIDER);
        new FileWMSLayer(builder);
    }

    /**
     * Test construction with null image provider.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testWMSLayerNullImageProvider()
    {
        WMSLayerConfig conf = new WMSLayerConfig();
        conf.setLayerTitle(TEST_TITLE);
        conf.setCacheImageFormat(IMAGE_FORMAT);
        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());

        FileWMSLayer.Builder builder = new FileWMSLayer.Builder(info);
        builder.setLayerCellDimensions(Collections.<Vector2d>emptyList());
        new FileWMSLayer(builder);
    }

    /**
     * Test construction with null title.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testWMSLayerNullTitle()
    {
        WMSLayerConfig conf = new WMSLayerConfig();
        conf.setCacheImageFormat(IMAGE_FORMAT);
        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, HOST_NAME,
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());

        FileWMSLayer.Builder builder = new FileWMSLayer.Builder(info);
        builder.setLayerCellDimensions(Collections.<Vector2d>emptyList());
        builder.setLRCImageProvider(NULL_LRC_IMAGE_PROVIDER);
        new FileWMSLayer(builder);
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
