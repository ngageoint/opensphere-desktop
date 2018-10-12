package io.opensphere.core.model;

/** Key that identifies an image in the Z-Y-X grid. */
public class ZYXImageKey
{
    /** The geographic bounding box for this image. */
    private final GeographicBoundingBox myBounds;

    /** The X coordinate of the tile. */
    private final int myX;

    /** The Y coordinate of the tile. */
    private final int myY;

    /** The Z coordinate of the tile. */
    private final int myZ;

    /**
     * Constructor.
     *
     * @param z The z coordinate.
     * @param y The y coordinate.
     * @param x The x coordinate.
     * @param bbox The geographic bounding box for this image.
     */
    public ZYXImageKey(int z, int y, int x, GeographicBoundingBox bbox)
    {
        myX = x;
        myY = y;
        myZ = z;
        myBounds = bbox;
    }

    /**
     * Get the geographic bounding box for this image.
     *
     * @return The bounds.
     */
    public GeographicBoundingBox getBounds()
    {
        return myBounds;
    }

    /**
     * Get the X coordinate.
     *
     * @return The X coordinate.
     */
    public int getX()
    {
        return myX;
    }

    /**
     * Get the Y coordinate.
     *
     * @return The Y coordinate.
     */
    public int getY()
    {
        return myY;
    }

    /**
     * Get the Z coordinate.
     *
     * @return The Z coordinate.
     */
    public int getZ()
    {
        return myZ;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append(getClass().getSimpleName()).append(" [").append(myZ).append(',').append(myY).append(',').append(myX)
        .append(']');
        return sb.toString();
    }
}
