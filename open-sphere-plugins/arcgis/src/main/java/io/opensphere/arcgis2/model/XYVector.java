package io.opensphere.arcgis2.model;

import org.codehaus.jackson.annotate.JsonProperty;

/** X-Y vector. */
public class XYVector
{
    /** X coordinate. */
    @JsonProperty("x")
    private int myX;

    /** Y coordinate. */
    @JsonProperty("y")
    private int myY;

    /**
     * Get the x.
     *
     * @return The x.
     */
    public int getX()
    {
        return myX;
    }

    /**
     * Get the y.
     *
     * @return The y.
     */
    public int getY()
    {
        return myY;
    }

    /**
     * Set the x.
     *
     * @param x The x.
     */
    public void setX(int x)
    {
        myX = x;
    }

    /**
     * Set the y.
     *
     * @param y The y.
     */
    public void setY(int y)
    {
        myY = y;
    }
}
