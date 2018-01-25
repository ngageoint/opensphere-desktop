package io.opensphere.xyztile.transformer;

import java.util.List;

import io.opensphere.core.geometry.ImageManager.RequestObserver;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Divider used for tiles in the Web Mercator projection.
 */
public class XYZ3857Divider extends XYZBaseDivider
{
    /**
     * Constructs a new web mercator divider.
     *
     * @param layer The layer we are dividing tiles for.
     * @param queryTracker A query tracker to report downloads to user.
     */
    public XYZ3857Divider(XYZTileLayerInfo layer, RequestObserver queryTracker)
    {
        super(layer, queryTracker);
    }

    @Override
    @SuppressWarnings("PMD.AvoidArrayLoops")
    protected List<GeographicBoundingBox> calculateNewBoxes(int[] newXs, int[] newYs, int zoom,
            GeographicBoundingBox overallBounds)
    {
        List<GeographicBoundingBox> boxes = New.list();

        int[] newYsToIterate = new int[newYs.length];
        if (getLayer().isTms())
        {
            for (int i = newYs.length - 1, j = 0; i >= 0; i--, j++)
            {
                newYsToIterate[j] = newYs[i];
            }
        }
        else
        {
            System.arraycopy(newYs, 0, newYsToIterate, 0, newYs.length);
        }

        for (int newY : newYsToIterate)
        {
            for (int newX : newXs)
            {
                boxes.add(tile2boundingBox(newX, newY, zoom));
            }
        }
        return boxes;
    }

    /**
     * Takes the tile coordinates and converts them to a geographic bounding
     * box.
     *
     * @param x The tile x value.
     * @param y The tile y value.
     * @param zoom The tiles zoom level.
     * @return The geographic bounding box.
     */
    private GeographicBoundingBox tile2boundingBox(final int x, final int y, final int zoom)
    {
        double north = tile2lat(y, zoom);
        double south = tile2lat(y + 1, zoom);
        double west = tile2lon(x, zoom);
        double east = tile2lon(x + 1, zoom);
        return new GeographicBoundingBox(LatLonAlt.createFromDegrees(south, west), LatLonAlt.createFromDegrees(north, east));
    }

    /**
     * Gets the northern latitude value of the tile.
     *
     * @param y The tile's y value.
     * @param z The tile's zoom level.
     * @return The northern edge of the tile.
     */
    private double tile2lat(int y, int z)
    {
        double n = Math.PI - 2.0 * Math.PI * y / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    /**
     * Gets the west longitude value of the tile.
     *
     * @param x The tile x value.
     * @param z The tiles zoom level.
     * @return The western edge of the tile.
     */
    private double tile2lon(int x, int z)
    {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }
}
