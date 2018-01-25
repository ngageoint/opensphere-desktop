package io.opensphere.geopackage.importer.tile;

import java.util.List;

import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.GeoPackageTileLayer;
import io.opensphere.geopackage.model.ProgressModel;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Imports all tiles contained with a Geopackage file and saves the data to the
 * registry.
 */
public class TileImporter
{
    /**
     * Imports a single tile into the system.
     */
    private final TileRowImporter myRowImporter;

    /**
     * Constructs a new tile importer.
     *
     * @param rowImporter Used to import a single tile.
     */
    public TileImporter(TileRowImporter rowImporter)
    {
        myRowImporter = rowImporter;
    }

    /**
     * Imports all the tiles contained in the geopackage file.
     *
     * @param geopackage The geopackage file.
     * @param layers The layers contained in the geopackage file.
     * @param ta The task activity to check if user has cancelled the import.
     * @param model The model used by the import classes.
     */
    public void importTiles(GeoPackage geopackage, List<GeoPackageLayer> layers, CancellableTaskActivity ta, ProgressModel model)
    {
        for (GeoPackageLayer layer : layers)
        {
            if (layer instanceof GeoPackageTileLayer)
            {
                importLayer(geopackage, (GeoPackageTileLayer)layer, ta, model);
            }

            if (ta.isCancelled())
            {
                break;
            }
        }
    }

    /**
     * Imports all the tile from a single layer.
     *
     * @param geopackage The geopackage file.
     * @param layer The layer to import tiles for.
     * @param ta The task activity to check if user has cancelled the import.
     * @param model The model used by the import classes
     */
    private void importLayer(GeoPackage geopackage, GeoPackageTileLayer layer, CancellableTaskActivity ta, ProgressModel model)
    {
        TileDao tileDao = geopackage.getTileDao(layer.getName());

        // Get the tile matrix set and bounding box
        TileMatrixSet tileMatrixSet = tileDao.getTileMatrixSet();
        BoundingBox setProjectionBoundingBox = tileMatrixSet.getBoundingBox();

        long firstZoomLevel = tileDao.getMinZoom();
        TileResultSet results = tileDao.queryForTile(firstZoomLevel);
        while (results.moveToNext() && !ta.isCancelled())
        {
            TileRow row = results.getRow();
            myRowImporter.importTile(layer, tileDao, setProjectionBoundingBox, row);
            model.setCompletedCount(model.getCompletedCount() + 1);
        }
    }
}
