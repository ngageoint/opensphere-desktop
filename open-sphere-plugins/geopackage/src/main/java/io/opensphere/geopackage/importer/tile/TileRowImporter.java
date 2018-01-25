package io.opensphere.geopackage.importer.tile;

import io.opensphere.geopackage.model.GeoPackageTileLayer;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Interface to an object that imports a single tile into the system.
 */
public interface TileRowImporter
{
    /**
     * Imports the tile row and saves the tile into the data registry.
     *
     * @param layer The layer the tile belongs to.
     * @param tileDao The tile data access object.
     * @param layerBoundingBox The bounding box for the whole tile dao.
     * @param tileRow The tile to import.
     */
    void importTile(GeoPackageTileLayer layer, TileDao tileDao, BoundingBox layerBoundingBox, TileRow tileRow);
}
