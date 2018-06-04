package io.opensphere.infinity.json;

/** Elasticsearch bounding box JSON bean. */
public class BoundingBox
{
    /** The bottom right coordinate. */
    private Coordinate myBottomRight;

    /** The top left coordinate. */
    private Coordinate myTopLeft;

    /**
     * Gets the bottomRight.
     *
     * @return the bottomRight
     */
    public Coordinate getBottom_right()
    {
        return myBottomRight;
    }

    /**
     * Sets the bottomRight.
     *
     * @param bottomRight the bottomRight
     */
    public void setBottom_right(Coordinate bottomRight)
    {
        myBottomRight = bottomRight;
    }

    /**
     * Gets the topLeft.
     *
     * @return the topLeft
     */
    public Coordinate getTop_left()
    {
        return myTopLeft;
    }

    /**
     * Sets the topLeft.
     *
     * @param topLeft the topLeft
     */
    public void setTop_left(Coordinate topLeft)
    {
        myTopLeft = topLeft;
    }
}
