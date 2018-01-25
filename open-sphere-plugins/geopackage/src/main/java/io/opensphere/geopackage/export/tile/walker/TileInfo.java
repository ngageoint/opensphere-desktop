package io.opensphere.geopackage.export.tile.walker;

import io.opensphere.core.geometry.AbstractTileGeometry;

/** Tile info. */
public class TileInfo
{
    /** The geometry. */
    private final AbstractTileGeometry<?> myGeometry;

    /** The zoom level. */
    private final int myZoomLevel;

    /** The row. */
    private final int myRow;

    /** The column. */
    private final int myCol;

    /**
     * Constructor.
     *
     * @param geometry The geometry
     * @param zoomLevel The zoom level
     * @param row The row
     * @param col The column
     */
    public TileInfo(AbstractTileGeometry<?> geometry, int zoomLevel, int row, int col)
    {
        myGeometry = geometry;
        myZoomLevel = zoomLevel;
        myRow = row;
        myCol = col;
    }

    /**
     * Gets the geometry.
     *
     * @return the geometry
     */
    public AbstractTileGeometry<?> getGeometry()
    {
        return myGeometry;
    }

    /**
     * Gets the zoom level.
     *
     * @return the zoom level
     */
    public int getZoomLevel()
    {
        return myZoomLevel;
    }

    /**
     * Gets the row.
     *
     * @return the row
     */
    public int getRow()
    {
        return myRow;
    }

    /**
     * Gets the column.
     *
     * @return the column
     */
    public int getCol()
    {
        return myCol;
    }

    @Override
    public String toString()
    {
        return "TileInfo [zoomLevel=" + myZoomLevel + ", row=" + myRow + ", col=" + myCol + "]";
    }
}
