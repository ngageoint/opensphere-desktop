package io.opensphere.core.model;

/**
 * Bound an object on the screen with a geographic attachment point. Screen
 * Coordinates move from UpperLeft to LowerRight. The UpperLeft and LowerRight
 * will be offset by the location of the attachment point transformed to screen
 * coordinates.
 */
public class GeoScreenBoundingBox extends ScreenBoundingBox
{
    /**
     * Geographic position to which this bounding box is attached.
     */
    private final GeographicBoxAnchor myAnchor;

    /**
     * Constructor.
     *
     * @param upperLeft upper left corner.
     * @param lowerRight lower right corner.
     * @param attachment Geographic attachment for the upper left corner of the
     *            bounding box.
     */
    public GeoScreenBoundingBox(ScreenPosition upperLeft, ScreenPosition lowerRight, GeographicBoxAnchor attachment)
    {
        super(upperLeft, lowerRight);
        myAnchor = attachment;
    }

    /**
     * Get the attachment.
     *
     * @return the attachment
     */
    public GeographicBoxAnchor getAnchor()
    {
        return myAnchor;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" at ").append(myAnchor);
        return sb.toString();
    }
}
