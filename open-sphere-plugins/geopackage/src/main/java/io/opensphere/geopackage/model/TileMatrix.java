package io.opensphere.geopackage.model;

import java.io.Serializable;

/**
 * A lite version of the {@link mil.nga.geopackage.tiles.matrix.TileMatrix}
 * class that is serializable.
 */
public class TileMatrix implements Serializable
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The matrix height.
     */
    private final long myMatrixHeight;

    /**
     * The matrix width.
     */
    private final long myMatrixWidth;

    /**
     * Constructs a new lite tile matrix.
     *
     * @param matrixHeight The matrix height.
     * @param matrixWidth The matrix width.
     */
    public TileMatrix(long matrixHeight, long matrixWidth)
    {
        myMatrixWidth = matrixWidth;
        myMatrixHeight = matrixHeight;
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean equals = false;

        if (obj instanceof TileMatrix)
        {
            TileMatrix other = (TileMatrix)obj;
            if (other.myMatrixHeight == myMatrixHeight && other.myMatrixWidth == myMatrixWidth)
            {
                equals = true;
            }
        }

        return equals;
    }

    /**
     * Gets the matrix height.
     *
     * @return The matrix height.
     */
    public long getMatrixHeight()
    {
        return myMatrixHeight;
    }

    /**
     * Gets the matrix widht.
     *
     * @return The matrix width.
     */
    public long getMatrixWidth()
    {
        return myMatrixWidth;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int)(myMatrixHeight ^ myMatrixHeight >>> 32);
        result = prime * result + (int)(myMatrixWidth ^ myMatrixWidth >>> 32);
        return result;
    }
}
