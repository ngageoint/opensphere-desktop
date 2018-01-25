package io.opensphere.geopackage.importer.tile;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;

import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.InputStreamAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.image.DDSImage;
import io.opensphere.core.image.Image;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackagePropertyDescriptors;
import io.opensphere.geopackage.model.GeoPackageTile;
import io.opensphere.geopackage.model.GeoPackageTileLayer;
import io.opensphere.geopackage.util.GeoPackageCoordinateUtils;
import io.opensphere.geopackage.util.ImageEncoder;
import io.opensphere.geopackage.util.TileKeyGenerator;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Imports a single tile from a Geopackage file and saves the info into the
 * registry. A {@link GeoPackageTile} will be saved seperately from the actual
 * tile image. The {@link GeoPackageTile} will contain metadata about the tile
 * while the image deposit will contain the bytes of the image.
 */
public class TileRowImporterImpl implements TileRowImporter
{
    /**
     * The accessor for getting the input stream for the raw image bytes. The
     * raw bytes for the image may be used directly from this stream.
     */
    private static final InputStreamAccessor<InputStream> IMAGE_STREAM_ACCESSOR = InputStreamAccessor
            .getHomogeneousAccessor(GeoPackagePropertyDescriptors.IMAGE_PROPERTY_DESCRIPTOR);

    /**
     * The accessor for getting the geopackage tile metadata object.
     */
    private static final SerializableAccessor<GeoPackageTile, GeoPackageTile> TILE_ACCESSOR = SerializableAccessor
            .getHomogeneousAccessor(GeoPackagePropertyDescriptors.GEOPACKAGE_TILE_PROPERTY_DESCRIPTOR);

    /**
     * Used to encode images to {@link DDSImage} for faster draw performance.
     */
    private final ImageEncoder myEncoder = new ImageEncoder();

    /**
     * Used to deposit tile information.
     */
    private final DataRegistry myRegistry;

    /**
     * Constructs a new single tile importer.
     *
     * @param registry Used to deposit the tile data.
     */
    public TileRowImporterImpl(DataRegistry registry)
    {
        myRegistry = registry;
    }

    @Override
    public void importTile(GeoPackageTileLayer layer, TileDao tileDao, BoundingBox layerBoundingBox, TileRow tileRow)
    {
        BoundingBox box = TileBoundingBoxUtils.getBoundingBox(layerBoundingBox, tileDao.getTileMatrix(tileRow.getZoomLevel()),
                tileRow.getTileColumn(), tileRow.getTileRow());

        GeoPackageTile tile = new GeoPackageTile(layer.getId(), tileRow.getZoomLevel(),
                GeoPackageCoordinateUtils.getInstance().convertToGeodetic(box, tileDao.getProjection()),
                (int)tileRow.getTileColumn(), (int)tileRow.getTileRow());

        String tileKey = generateTileKey(tile);

        depositTile(layer, tileKey, tile);
        if (tileRow.getTileData() != null)
        {
            InputStream imageData = myEncoder.encodeImage(tileRow.getTileData());
            depositImage(layer, tileKey, imageData);
        }
    }

    /**
     * Deposits the tile image to the {@link DataRegistry}.
     *
     * @param layer The layer the tile belongs to.
     * @param tileKey The key for the tile.
     * @param imageData The image data to deposit.
     */
    private void depositImage(GeoPackageTileLayer layer, String tileKey, InputStream imageData)
    {
        DataModelCategory imageCategory = new DataModelCategory(layer.getPackageFile(), layer.getName(), Image.class.getName());
        Collection<PropertyAccessor<InputStream, ?>> imageAccessors = New.collection();
        imageAccessors.add(SerializableAccessor
                .<InputStream, String>getSingletonAccessor(GeoPackagePropertyDescriptors.KEY_PROPERTY_DESCRIPTOR, tileKey));
        imageAccessors.add(IMAGE_STREAM_ACCESSOR);

        DefaultCacheDeposit<InputStream> imageDeposit = new DefaultCacheDeposit<>(imageCategory, imageAccessors,
                New.list(imageData), true, new Date(Long.MAX_VALUE), false);
        myRegistry.addModels(imageDeposit);
    }

    /**
     * Deposits the {@link GeoPackageTile} into th {@link DataRegistry}.
     *
     * @param layer The layer the tile belongs to.
     * @param tileKey The key for the tile.
     * @param tile The tile to deposit.
     */
    private void depositTile(GeoPackageTileLayer layer, String tileKey, GeoPackageTile tile)
    {
        DataModelCategory category = new DataModelCategory(layer.getPackageFile(), layer.getName(),
                GeoPackageTile.class.getName());

        Collection<PropertyAccessor<GeoPackageTile, ?>> accessors = New.collection();
        accessors.add(SerializableAccessor
                .<GeoPackageTile, String>getSingletonAccessor(GeoPackagePropertyDescriptors.KEY_PROPERTY_DESCRIPTOR, tileKey));
        accessors.add(TILE_ACCESSOR);
        accessors.add(SerializableAccessor.<GeoPackageTile, Long>getSingletonAccessor(
                GeoPackagePropertyDescriptors.ZOOM_LEVEL_PROPERTY_DESCRIPTOR, Long.valueOf(tile.getZoomLevel())));

        DefaultCacheDeposit<GeoPackageTile> tileDeposit = new DefaultCacheDeposit<>(category, accessors, New.list(tile), true,
                new Date(Long.MAX_VALUE), false);
        myRegistry.addModels(tileDeposit);
    }

    /**
     * Generates a tile key.
     *
     * @param tile The tile to generate the key for.
     * @return The unique key for the tile.
     */
    private String generateTileKey(GeoPackageTile tile)
    {
        return TileKeyGenerator.getInstance().generateTileKey(tile.getZoomLevel(), tile.getBoundingBox());
    }
}
