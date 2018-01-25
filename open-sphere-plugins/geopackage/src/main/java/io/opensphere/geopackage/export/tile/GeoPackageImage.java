package io.opensphere.geopackage.export.tile;

import java.nio.ByteBuffer;

/**
 * The image to write to a geopackage file.
 */
public class GeoPackageImage
{
    /**
     * The image bytes.
     */
    private final ByteBuffer myImageBytes;

    /**
     * The image width.
     */
    private final int myWidth;

    /**
     * The image height.
     */
    private final int myHeight;

    /**
     * Constructs a new geopackage image.
     *
     * @param imageBytes The image bytes.
     * @param width The width of the image.
     * @param height The height of the image.
     */
    public GeoPackageImage(ByteBuffer imageBytes, int width, int height)
    {
        myImageBytes = imageBytes;
        myWidth = width;
        myHeight = height;
    }

    /**
     * Gets the image bytes.
     *
     * @return the imageBytes
     */
    public ByteBuffer getImageBytes()
    {
        return myImageBytes;
    }

    /**
     * Gets the image widht.
     *
     * @return the width
     */
    public int getWidth()
    {
        return myWidth;
    }

    /**
     * Gets the image height.
     *
     * @return the height
     */
    public int getHeight()
    {
        return myHeight;
    }
}
