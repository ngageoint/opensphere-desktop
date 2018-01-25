package io.opensphere.core.model;

import io.opensphere.core.math.Vector2i;

/**
 * A anchor position which describes how to position a box in relation to a
 * specific geographic position.
 */
public class GeographicBoxAnchor
{
    /**
     * This is the screen offset for the anchor point. This may be null if no
     * offset is desired.
     */
    private final Vector2i myAnchorOffset;

    /**
     * Geographic position to which this bounding box is attached.
     */
    private final GeographicPosition myGeographicAnchor;

    /**
     * The left to right ( 0 &lt;= x &lt;= 1 ) alignment of the box with respect
     * to the anchor point.
     */
    private final float myHorizontalAlignment;

    /**
     * The bottom to top ( 0 &lt;= x &lt;= 1 ) alignment of the box with respect
     * to the anchor point.
     */
    private final float myVerticalAlignment;

    /**
     * Constructor.
     *
     * @param anchor The geographic position at which the box is anchored.
     * @param anchorOffset The offset in screen coordinates that the box should
     *            be moved from the anchor position. This may be null if no
     *            offset is desired.
     * @param horizontalAlignment The left to right ( 0 &lt;= x &lt;= 1 )
     *            alignment of the box with respect to the offset anchor point.
     * @param verticalAlignment The bottom to top ( 0 &lt;= x &lt;= 1 )
     *            alignment of the box with respect to the offset anchor point.
     */
    public GeographicBoxAnchor(GeographicPosition anchor, Vector2i anchorOffset, float horizontalAlignment,
            float verticalAlignment)
    {
        myGeographicAnchor = anchor;
        myAnchorOffset = anchorOffset;
        myHorizontalAlignment = horizontalAlignment;
        myVerticalAlignment = verticalAlignment;
    }

    /**
     * Get the screen offset for the anchor point. This may be null if no offset
     * is desired.
     *
     * @return the anchorOffset
     */
    public Vector2i getAnchorOffset()
    {
        return myAnchorOffset;
    }

    /**
     * Get the geographic reference point for the anchor.
     *
     * @return the geographicAnchor
     */
    public GeographicPosition getGeographicAnchor()
    {
        return myGeographicAnchor;
    }

    /**
     * Get the left to right ( 0 &lt;= x &lt;= 1 ) alignment of the box with
     * respect to the offset anchor point.
     *
     * @return the horizontalAlignment
     */
    public float getHorizontalAlignment()
    {
        return myHorizontalAlignment;
    }

    /**
     * Get the bottom to top ( 0 &lt;= x &lt;= 1 ) alignment of the box with
     * respect to the offset anchor point.
     *
     * @return the verticalAlignment
     */
    public float getVerticalAlignment()
    {
        return myVerticalAlignment;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64).append(super.toString());
        sb.append('(').append(myGeographicAnchor).append(")[").append(myAnchorOffset).append(']');
        return sb.toString();
    }
}
