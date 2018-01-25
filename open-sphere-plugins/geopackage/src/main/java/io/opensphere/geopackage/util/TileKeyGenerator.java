package io.opensphere.geopackage.util;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.geopackage.model.GeoPackageTile;

/**
 * Generates unique keys for {@link GeoPackageTile}.
 */
public final class TileKeyGenerator
{
    /**
     * The instance of this class.
     */
    private static final TileKeyGenerator ourGenerator = new TileKeyGenerator();

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static TileKeyGenerator getInstance()
    {
        return ourGenerator;
    }

    /**
     * Not constructible.
     */
    private TileKeyGenerator()
    {
    }

    /**
     * Generates a tile key.
     *
     * @param zoomLevel The zoom level for the tile.
     * @param boundingBox The geographic bounding box of the tile.
     * @return The unique key for the tile.
     */
    public String generateTileKey(long zoomLevel, GeographicBoundingBox boundingBox)
    {
        String key = zoomLevel + "|" + boundingBox.toSimpleString();

        return key;
    }

    /**
     * Parses a tile key and returns the zoom level and bounding box of the
     * tile.
     *
     * @param tileKey The key to parse.
     * @return The zoom level and bounding box of the tile.
     */
    public Pair<Long, GeographicBoundingBox> parseKeyString(String tileKey)
    {
        String[] split = tileKey.split("\\|");
        long zoomLevel = Long.parseLong(split[0]);
        String lowerLeftString = split[1];
        String upperRightString = split[2];

        lowerLeftString = lowerLeftString.replace('/', ',');
        upperRightString = upperRightString.replace('/', ',');

        lowerLeftString = lowerLeftString.substring(0, lowerLeftString.lastIndexOf(','));
        upperRightString = upperRightString.substring(0, upperRightString.lastIndexOf(','));

        LatLonAlt lowerLeft = LatLonAlt.parse(lowerLeftString);
        LatLonAlt upperRight = LatLonAlt.parse(upperRightString);

        GeographicBoundingBox boundingBox = new GeographicBoundingBox(lowerLeft, upperRight);

        return new Pair<Long, GeographicBoundingBox>(Long.valueOf(zoomLevel), boundingBox);
    }
}
