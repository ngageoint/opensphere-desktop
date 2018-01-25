package io.opensphere.xyztile.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Unit test for {@link XYZGeometryBuilder}.
 */
public class XYZGeometryBuilderTest
{
    /**
     * Tests building the top level geometries with a min zoom level.
     */
    @Test
    public void testDigitalGlobe()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = createDataRegistry(support);
        UIRegistry uiRegistry = createUIRegistry(support);
        TileRenderProperties props = createProps(support);

        support.replayAll();

        XYZGeometryBuilder builder = new XYZGeometryBuilder(dataRegistry, uiRegistry);

        String serverUrl = "http://mapboxserver";
        String layerName = "dark";
        String layerId = "id";
        String layerDisplayName = "Dark";

        XYZTileLayerInfo layer = new XYZTileLayerInfo(layerName, layerDisplayName, Projection.EPSG_3857, 1, true, 0,
                new XYZServerInfo("serverName", serverUrl));
        TimeSpan span = TimeSpan.get();
        layer.setTimeSpan(span);
        GeographicBoundingBox footprint = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10, -10),
                LatLonAlt.createFromDegrees(10, 10));
        layer.setFootprint(footprint);

        List<TileGeometry> geometries = builder.buildTopGeometry(layer, layerId, props);

        assertEquals(1, geometries.size());

        for (TileGeometry geometry : geometries)
        {
            assertEquals(footprint, geometry.getBounds());
            assertEquals(300, geometry.getMinimumDisplaySize());
            assertEquals(600, geometry.getMaximumDisplaySize());
            assertTrue(geometry.getImageManager().getImageProvider() instanceof XYZImageProvider);
            assertTrue(geometry.getSplitJoinRequestProvider() instanceof XYZ3857Divider);
            assertNull(geometry.getParent());
            assertEquals(span, geometry.getConstraints().getTimeConstraint().getTimeSpan());
            assertFalse(geometry.getConstraints().getTimeConstraint().isMostRecent());
            assertFalse(geometry.getConstraints().getTimeConstraint().isNegative());
        }

        support.verifyAll();
    }

    /**
     * Tests building the top level geometries with a min zoom level.
     */
    @Test
    public void testMinZoomLevel()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = createDataRegistry(support);
        UIRegistry uiRegistry = createUIRegistry(support);
        TileRenderProperties props = createProps(support);

        support.replayAll();

        XYZGeometryBuilder builder = new XYZGeometryBuilder(dataRegistry, uiRegistry);

        String serverUrl = "http://mapboxserver";
        String layerName = "dark";
        String layerId = "id";
        String layerDisplayName = "Dark";

        XYZTileLayerInfo layer = new XYZTileLayerInfo(layerName, layerDisplayName, Projection.EPSG_3857, 1, false, 4,
                new XYZServerInfo("serverName", serverUrl));

        List<TileGeometry> geometries = builder.buildTopGeometry(layer, layerId, props);

        assertEquals(256, geometries.size());

        for (TileGeometry geometry : geometries)
        {
            assertEquals(300, geometry.getMinimumDisplaySize());
            assertEquals(600, geometry.getMaximumDisplaySize());
            assertTrue(geometry.getImageManager().getImageProvider() instanceof XYZImageProvider);
            assertTrue(geometry.getSplitJoinRequestProvider() instanceof XYZ3857Divider);
            assertNull(geometry.getParent());
        }

        support.verifyAll();
    }

    /**
     * Tests building the top level geometries with a min zoom level.
     */
    @Test
    public void testPlatCarree()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = createDataRegistry(support);
        UIRegistry uiRegistry = createUIRegistry(support);
        TileRenderProperties props = createProps(support);

        support.replayAll();

        XYZGeometryBuilder builder = new XYZGeometryBuilder(dataRegistry, uiRegistry);

        String serverUrl = "http://mapboxserver";
        String layerName = "dark";
        String layerId = "id";
        String layerDisplayName = "Dark";

        XYZTileLayerInfo layer = new XYZTileLayerInfo(layerName, layerDisplayName, Projection.EPSG_4326, 2, false, 0,
                new XYZServerInfo("serverName", serverUrl));

        List<TileGeometry> geometries = builder.buildTopGeometry(layer, layerId, props);

        assertEquals(2, geometries.size());

        GeographicBoundingBox expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -180),
                LatLonAlt.createFromDegrees(90, 0));
        ZYXImageKey expectedKey = new ZYXImageKey(0, 0, 0, expected);
        XYZQueryTracker requestObserver = (XYZQueryTracker)((XYZBaseDivider)geometries.get(0).getSplitJoinRequestProvider())
                .getRequestObserver();
        for (TileGeometry geometry : geometries)
        {
            assertEquals(expected, geometry.getBounds());
            assertEquals(300, geometry.getMinimumDisplaySize());
            assertEquals(600, geometry.getMaximumDisplaySize());
            assertTrue(geometry.getImageManager().getImageProvider() instanceof XYZImageProvider);
            assertEquals(expectedKey.toString(), geometry.getImageManager().getImageKey().toString());
            assertTrue(geometry.getSplitJoinRequestProvider() instanceof XYZ4326Divider);
            assertNull(geometry.getParent());
            assertEquals(requestObserver, ((XYZBaseDivider)geometry.getSplitJoinRequestProvider()).getRequestObserver());
            expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 0), LatLonAlt.createFromDegrees(90, 180));
            expectedKey = new ZYXImageKey(0, 0, 1, expected);
        }

        support.verifyAll();
    }

    /**
     * Tests building the top level geometries with a min zoom level.
     */
    @Test
    public void testWebMercator()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = createDataRegistry(support);
        UIRegistry uiRegistry = createUIRegistry(support);
        TileRenderProperties props = createProps(support);

        support.replayAll();

        XYZGeometryBuilder builder = new XYZGeometryBuilder(dataRegistry, uiRegistry);

        String serverUrl = "http://mapboxserver";
        String layerName = "dark";
        String layerId = "id";
        String layerDisplayName = "Dark";

        XYZTileLayerInfo layer = new XYZTileLayerInfo(layerName, layerDisplayName, Projection.EPSG_3857, 1, false, 0,
                new XYZServerInfo("serverName", serverUrl));

        List<TileGeometry> geometries = builder.buildTopGeometry(layer, layerId, props);

        assertEquals(1, geometries.size());

        GeographicBoundingBox expected = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-85.0511, -180),
                LatLonAlt.createFromDegrees(85.0511, 180));
        for (TileGeometry geometry : geometries)
        {
            assertEquals(expected, geometry.getBounds());
            assertEquals(300, geometry.getMinimumDisplaySize());
            assertEquals(600, geometry.getMaximumDisplaySize());
            assertTrue(geometry.getImageManager().getImageProvider() instanceof XYZImageProvider);
            assertTrue(geometry.getSplitJoinRequestProvider() instanceof XYZ3857Divider);
            assertNull(geometry.getParent());
        }

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataRegistry}.
     */
    private DataRegistry createDataRegistry(EasyMockSupport support)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        return registry;
    }

    /**
     * Creates the {@link TileRenderProperties}.
     *
     * @param support Used to create the mock.
     * @return The mocked props.
     */
    private TileRenderProperties createProps(EasyMockSupport support)
    {
        TileRenderProperties props = support.createMock(TileRenderProperties.class);
        EasyMock.expect(Boolean.valueOf(props.isDrawable())).andReturn(Boolean.TRUE).anyTimes();

        return props;
    }

    /**
     * Creates an easy mocked {@link UIRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link UIRegistry}.
     */
    private UIRegistry createUIRegistry(EasyMockSupport support)
    {
        UIRegistry uiRegistry = support.createMock(UIRegistry.class);

        return uiRegistry;
    }
}
