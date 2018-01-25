package io.opensphere.arcgis2.model;

import org.codehaus.jackson.annotate.JsonProperty;

/** A level of detail. */
public class LevelOfDetail
{
    /** The level. */
    @JsonProperty("level")
    private int myLevel;

    /** The resolution. */
    @JsonProperty("resolution")
    private double myResolution;

    /** The scale. */
    @JsonProperty("scale")
    private double myScale;

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
     * Get the resolution.
     *
     * @return The resolution.
     */
    public double getResolution()
    {
        return myResolution;
    }

    /**
     * Get the scale.
     *
     * @return The scale.
     */
    public double getScale()
    {
        return myScale;
    }

    /**
     * Set the level.
     *
     * @param level The level.
     */
    public void setLevel(int level)
    {
        myLevel = level;
    }

    /**
     * Set the resolution.
     *
     * @param resolution The resolution.
     */
    public void setResolution(double resolution)
    {
        myResolution = resolution;
    }

    /**
     * Set the scale.
     *
     * @param scale The scale.
     */
    public void setScale(double scale)
    {
        myScale = scale;
    }
}
