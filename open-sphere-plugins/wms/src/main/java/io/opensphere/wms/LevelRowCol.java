package io.opensphere.wms;

import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * Coordinates for an image in the WMS layer.
 */
public class LevelRowCol
{
    /** The column. */
    private final int myCol;

    /** The level. */
    private final int myLevel;

    /** The row. */
    private final int myRow;

    /**
     * Construct the coordinates.
     *
     * @param level The level.
     * @param row The row.
     * @param col The column.
     */
    public LevelRowCol(int level, int row, int col)
    {
        myLevel = level;
        myRow = row;
        myCol = col;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        LevelRowCol other = (LevelRowCol)obj;
        //@formatter:off
        return myCol == other.myCol
                && myLevel == other.myLevel
                && myRow == other.myRow;
        //@formatter:on
    }

    /**
     * Get the column.
     *
     * @return The column.
     */
    public int getCol()
    {
        return myCol;
    }

    /**
     * Get the level.
     *
     * @return The level.
     */
    public int getLevel()
    {
        return myLevel;
    }

    /**
     * Get the row.
     *
     * @return The row.
     */
    public int getRow()
    {
        return myRow;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myCol);
        result = prime * result + HashCodeHelper.getHashCode(myLevel);
        result = prime * result + HashCodeHelper.getHashCode(myRow);
        return result;
    }

    @Override
    public String toString()
    {
        return "LevelRowCol [myLevel=" + myLevel + ", myRow=" + myRow + ", myCol=" + myCol + "]";
    }
}
