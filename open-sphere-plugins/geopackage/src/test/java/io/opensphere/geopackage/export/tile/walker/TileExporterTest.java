package io.opensphere.geopackage.export.tile.walker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.geopackage.export.tile.TileExporter;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.TileLevelController;

/**
 * Unit test for {@link TileExporter}.
 */
public class TileExporterTest
{
    /**
     * Tests the various layers that are exportable.
     */
    @Test
    public void testIsExportable()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo featureLayer = support.createMock(DataTypeInfo.class);
        EasyMock.expect(featureLayer.getMapVisualizationInfo()).andReturn(null);

        DataTypeInfo tileLayer = support.createMock(DataTypeInfo.class);
        TileLevelController levelController = support.createMock(TileLevelController.class);
        MapVisualizationInfo tileVisInfo = support.createMock(MapVisualizationInfo.class);
        EasyMock.expect(tileVisInfo.getVisualizationType()).andReturn(MapVisualizationType.IMAGE_TILE);
        EasyMock.expect(tileVisInfo.getTileLevelController()).andReturn(levelController);
        EasyMock.expect(tileLayer.getMapVisualizationInfo()).andReturn(tileVisInfo);

        DataTypeInfo heatMapLayer = support.createMock(DataTypeInfo.class);
        MapVisualizationInfo heatVisInfo = support.createMock(MapVisualizationInfo.class);
        EasyMock.expect(heatVisInfo.getVisualizationType()).andReturn(MapVisualizationType.IMAGE_TILE);
        EasyMock.expect(heatVisInfo.getTileLevelController()).andReturn(null);
        EasyMock.expect(heatMapLayer.getMapVisualizationInfo()).andReturn(heatVisInfo);

        DataTypeInfo terrainLayer = support.createMock(DataTypeInfo.class);
        MapVisualizationInfo terrainVisInfo = support.createMock(MapVisualizationInfo.class);
        EasyMock.expect(terrainVisInfo.getVisualizationType()).andReturn(MapVisualizationType.TERRAIN_TILE);
        EasyMock.expect(terrainLayer.getMapVisualizationInfo()).andReturn(terrainVisInfo);

        support.replayAll();

        assertFalse(TileExporter.isExportable(featureLayer));
        assertTrue(TileExporter.isExportable(tileLayer));
        assertFalse(TileExporter.isExportable(heatMapLayer));
        assertTrue(TileExporter.isExportable(terrainLayer));

        support.verifyAll();
    }
}
