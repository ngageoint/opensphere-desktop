package io.opensphere.geopackage.model;

import java.io.Serializable;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.model.GeographicBoundingBox;

/**
 * Contains metadata about a specific tile. This class is serializabe and is
 * meant to be saved in the {@link DataRegistry}.
 */
public class GeoPackageTile implements Serializable
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The geographic bounding box of this tile.
     */
    private final GeographicBoundingBox myBoundingBox;

    /**
     * The id of the layer this tile belongs to.
     */
    private final String myLayerId;

    /**
     * The column value of this tile.
     */
    private final int myX;

    /**
     * The row value of this tile.
     */
    private final int myY;

    /**
     * The zoom level value of this tile.
     */
    private final long myZoomLevel;

    /**
     * Constructs a new {@link GeoPackageTile}.
     *
     * @param layerId The id of the layer this tile belongs to.
     * @param zoomLevel The zoom level of this tile.
     * @param boundingBox The bound box of this tile.
     * @param x The column value of this tile.
     * @param y The row value of this tile.
     */
    public GeoPackageTile(String layerId, long zoomLevel, GeographicBoundingBox boundingBox, int x, int y)
    {
        myLayerId = layerId;
        myZoomLevel = zoomLevel;
        myBoundingBox = boundingBox;
        myX = x;
        myY = y;
    }

    /**
     * Gets the bounding box of this tile.
     *
     * @return The bound box.
     */
    public GeographicBoundingBox getBoundingBox()
    {
        return myBoundingBox;
    }

    /**
     * Gets the layer id this tile belongs to.
     *
     * @return The layer id this tile belongs to.
     */
    public String getLayerId()
    {
        return myLayerId;
    }

    /**
     * Gets the column value of this tile.
     *
     * @return The column value.
     */
    public int getX()
    {
        return myX;
    }

    /**
     * Gets the row value of this tile.
     *
     * @return The row value.
     */
    public int getY()
    {
        return myY;
    }

    /**
     * Gets the zoom level of this tile.
     *
     * @return The zoom level.
     */
    public long getZoomLevel()
    {
        return myZoomLevel;
    }
}
