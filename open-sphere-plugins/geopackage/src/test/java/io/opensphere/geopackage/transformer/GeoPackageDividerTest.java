package io.opensphere.geopackage.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageTileLayer;
import io.opensphere.geopackage.model.TileMatrix;

/**
 * Unit test for the {@link GeoPackageDivider} class.
 */
public class GeoPackageDividerTest
{
    /**
     * Tests dividing tile whose on its last generation.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        TileRenderProperties props = createProps(support);
        GeographicBoundingBox bounds = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10, -10),
                LatLonAlt.createFromDegrees(10, 10));
        GeoPackageTileLayer layer = new GeoPackageTileLayer("packageName", "c:\\somefile.gpkg", "layerName", 5);
        layer.setMinZoomLevel(8);
        layer.setMaxZoomLevel(9);
        layer.getZoomLevelToMatrix().put(Long.valueOf(8), new TileMatrix(1, 1));
        layer.getZoomLevelToMatrix().put(Long.valueOf(9), new TileMatrix(2, 2));
        layer.setBoundingBox(bounds);

        DataRegistry registry = createRegistry(support);
        UIRegistry uiRegistry = createUIRegistry(support);

        support.replayAll();

        TileGeometry.Builder<GeographicPosition> builder = new TileGeometry.Builder<GeographicPosition>();
        builder.setBounds(bounds);
        ZYXImageKey parentImageKey = new ZYXImageKey(8, 0, 0, bounds);
        builder.setImageManager(new ImageManager(parentImageKey,
                new GeoPackageImageProvider(registry, layer, new GeoPackageQueryTracker(uiRegistry, "layerName"))));
        TileGeometry tileToDivide = new TileGeometry(builder, props, null);

        GeoPackageDivider divider = new GeoPackageDivider(layer);
        Collection<AbstractTileGeometry<?>> subTiles = divider.divide(tileToDivide);

        assertEquals(4, subTiles.size());
        Map<String, GeographicBoundingBox> actualBounds = New.map();
        for (AbstractTileGeometry<?> subTile : subTiles)
        {
            assertNull(subTile.getSplitJoinRequestProvider());
            String imageKey = subTile.getImageManager().getImageKey().toString();
            GeographicBoundingBox actualTileBounds = (GeographicBoundingBox)subTile.getBounds();

            actualBounds.put(imageKey, actualTileBounds);
        }

        GeographicBoundingBox expectedBounds1 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0d, -10d),
                LatLonAlt.createFromDegrees(10d, 0d));
        GeographicBoundingBox expectedBounds2 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0d, 0d),
                LatLonAlt.createFromDegrees(10d, 10d));
        GeographicBoundingBox expectedBounds3 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10d, 0d),
                LatLonAlt.createFromDegrees(0, 10d));
        GeographicBoundingBox expectedBounds4 = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10d, -10d),
                LatLonAlt.createFromDegrees(0d, 0d));

        ZYXImageKey expectedKey1 = new ZYXImageKey(9, 0, 0, expectedBounds1);
        ZYXImageKey expectedKey2 = new ZYXImageKey(9, 0, 1, expectedBounds2);
        ZYXImageKey expectedKey3 = new ZYXImageKey(9, 1, 1, expectedBounds3);
        ZYXImageKey expectedKey4 = new ZYXImageKey(9, 1, 0, expectedBounds4);

        List<ZYXImageKey> expecteds = New.list(expectedKey1, expectedKey2, expectedKey3, expectedKey4);

        for (ZYXImageKey expected : expecteds)
        {
            GeographicBoundingBox actual = actualBounds.get(expected.toString());
            assertEquals(expected.getBounds(), actual);
        }

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link TileRenderProperties}.
     *
     * @param support Used to create the mock.
     * @return The mocked properties.
     */
    private TileRenderProperties createProps(EasyMockSupport support)
    {
        TileRenderProperties props = support.createMock(TileRenderProperties.class);

        EasyMock.expect(Boolean.valueOf(props.isDrawable())).andReturn(Boolean.TRUE).anyTimes();

        return props;
    }

    /**
     * Creates an easy mocked data registry.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataRegistry}.
     */
    private DataRegistry createRegistry(EasyMockSupport support)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

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
        UIRegistry uiRegistry = support.createMock(UIRegistry.class);

        return uiRegistry;
    }
}
