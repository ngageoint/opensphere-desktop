package io.opensphere.arcgis2.model;

import org.codehaus.jackson.annotate.JsonProperty;

/** Model for geographic extents. */
public class Extent
{
    /** The max x. */
    @JsonProperty("xmax")
    private double myMaxX;

    /** The max y. */
    @JsonProperty("ymax")
    private double myMaxY;

    /** The min x. */
    @JsonProperty("xmin")
    private double myMinX;

    /** The min y. */
    @JsonProperty("ymin")
    private double myMinY;

    /**
     * The spatial reference of the layer.
     */
    @JsonProperty("spatialReference")
    private SpatialReference mySpatialReference;

    /**
     * Get the max x.
     *
     * @return The max x.
     */
    public double getMaxX()
    {
        return myMaxX;
    }

    /**
     * Get the max y.
     *
     * @return The max y.
     */
    public double getMaxY()
    {
        return myMaxY;
    }

    /**
     * Get the min x.
     *
     * @return The min x.
     */
    public double getMinX()
    {
        return myMinX;
    }

    /**
     * Get the min y.
     *
     * @return The min y.
     */
    public double getMinY()
    {
        return myMinY;
    }

    /**
     * Gets the spatial reference of the layer.
     *
     * @return The spatial reference of the layer.
     */
    public SpatialReference getSpatialReference()
    {
        return mySpatialReference;
    }

    /**
     * Set the max x.
     *
     * @param maxX The max x.
     */
    public void setMaxX(double maxX)
    {
        myMaxX = maxX;
    }

    /**
     * Set the max y.
     *
     * @param maxY The max y.
     */
    public void setMaxY(double maxY)
    {
        myMaxY = maxY;
    }

    /**
     * Set the min x.
     *
     * @param minX The min x.
     */
    public void setMinX(double minX)
    {
        myMinX = minX;
    }

    /**
     * Set the min y.
     *
     * @param minY The min y.
     */
    public void setMinY(double minY)
    {
        myMinY = minY;
    }

    /**
     * Sets the spatial reference of the layer.
     *
     * @param spatialReference The spatial reference of the layer.
     */
    public void setSpatialReference(SpatialReference spatialReference)
    {
        mySpatialReference = spatialReference;
    }
}
