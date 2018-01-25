package io.opensphere.mantle.data.geom;

/**
 * Basic Line-of-Being Geometry. Point with line attached that goes for a
 * specified distance in a specified direction.
 */
public interface MapLineOfBearingGeometrySupport extends MapPointGeometrySupport
{
    /**
     * Gets the length of the line in kilometers.
     *
     * @return the length
     */
    float getLength();

    /**
     * Gets the ellipse orientation.
     *
     * @return the orientation ( degrees clockwise from north )
     */
    float getOrientation();

    /**
     * Sets the line length of the line in kilometers.
     *
     * @param length - the length of the line
     */
    void setLength(float length);

    /**
     * Sets the orientation ( degrees clockwise from north ).
     *
     * @param orient - the orientation
     */
    void setOrientation(float orient);
}
