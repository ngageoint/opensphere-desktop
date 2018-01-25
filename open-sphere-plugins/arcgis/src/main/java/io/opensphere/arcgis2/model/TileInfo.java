package io.opensphere.arcgis2.model;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

/** Tile info. */
public class TileInfo
{
    /** The cols. */
    @JsonProperty("cols")
    private int myCols;

    /** The lods. */
    @JsonProperty("lods")
    private List<LevelOfDetail> myLods;

    /** The origin. */
    @JsonProperty("origin")
    private XYVector myOrigin;

    /** The rows. */
    @JsonProperty("rows")
    private int myRows;

    /**
     * Get the cols.
     *
     * @return The cols.
     */
    public int getCols()
    {
        return myCols;
    }

    /**
     * Gets the lods.
     *
     * @return The lods.
     */
    public List<LevelOfDetail> getLods()
    {
        return myLods;
    }

    /**
     * Get the origin.
     *
     * @return The origin.
     */
    public XYVector getOrigin()
    {
        return myOrigin;
    }

    /**
     * Get the rows.
     *
     * @return The rows.
     */
    public int getRows()
    {
        return myRows;
    }

    /**
     * Set the cols.
     *
     * @param cols The cols.
     */
    public void setCols(int cols)
    {
        myCols = cols;
    }

    /**
     * Sets the lods.
     *
     * @param lods The lods.
     */
    public void setLods(List<LevelOfDetail> lods)
    {
        myLods = lods;
    }

    /**
     * Set the origin.
     *
     * @param origin The origin.
     */
    public void setOrigin(XYVector origin)
    {
        myOrigin = origin;
    }

    /**
     * Set the rows.
     *
     * @param rows The rows.
     */
    public void setRows(int rows)
    {
        myRows = rows;
    }
}
