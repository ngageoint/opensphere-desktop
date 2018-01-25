package io.opensphere.stkterrain.model;

import java.io.Serializable;

/**
 * Contains a range of TMS x and y coordinates a TileSet has tiles for at given
 * zoom levels.
 */
public class TileRange implements Serializable
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The end x coordinate.
     */
    private int myEndX;

    /**
     * The end y coordinate.
     */
    private int myEndY;

    /**
     * The start x coordinate.
     */
    private int myStartX;

    /**
     * The start y coordinate.
     */
    private int myStartY;

    /**
     * Gets the end x coordinate.
     *
     * @return The end x coordinate.
     */
    public int getEndX()
    {
        return myEndX;
    }

    /**
     * Gets the end y coordinate.
     *
     * @return The end y coordinate.
     */
    public int getEndY()
    {
        return myEndY;
    }

    /**
     * Gets the start x coordinate.
     *
     * @return The start x coordinate.
     */
    public int getStartX()
    {
        return myStartX;
    }

    /**
     * Gets the start y coordinate.
     *
     * @return The start y coordinate.
     */
    public int getStartY()
    {
        return myStartY;
    }

    /**
     * Sets the end x coordinate.
     *
     * @param endX The end x coordinate.
     */
    public void setEndX(int endX)
    {
        myEndX = endX;
    }

    /**
     * Sets the end y coordinate.
     *
     * @param endY The end y coordinate.
     */
    public void setEndY(int endY)
    {
        myEndY = endY;
    }

    /**
     * Sets the start x coordinate.
     *
     * @param startX The start x coordinate.
     */
    public void setStartX(int startX)
    {
        myStartX = startX;
    }

    /**
     * Sets the start y coordinate.
     *
     * @param startY The start y coordinate.
     */
    public void setStartY(int startY)
    {
        myStartY = startY;
    }
}
