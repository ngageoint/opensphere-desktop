package io.opensphere.arcgis2.envoy.tile;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.opensphere.arcgis2.model.ArcGISLayer;
import io.opensphere.arcgis2.model.ArcGISLayer.Builder;

/**
 * Unit test for {@link TileUrlBuilderFactory}.
 */
public class TileUrlBuilderFactoryTest
{
    /**
     * Tests creating an export builder.
     */
    @Test
    public void testCreateExportBuilder()
    {
        Builder builder = new Builder();
        builder.setSingleFusedMapCache(false);
        ArcGISLayer layer = new ArcGISLayer(builder);

        TileUrlBuilder urlBuilder = TileUrlBuilderFactory.getInstance().createBuilder(layer);

        assertTrue(urlBuilder instanceof ExportUrlBuilder);
    }

    /**
     * Tests creating an xyz builder.
     */
    @Test
    public void testCreateXYZBuilder()
    {
        Builder builder = new Builder();
        builder.setSingleFusedMapCache(true);
        ArcGISLayer layer = new ArcGISLayer(builder);

        TileUrlBuilder urlBuilder = TileUrlBuilderFactory.getInstance().createBuilder(layer);

        assertTrue(urlBuilder instanceof XYZUrlBuilder);
    }
}
