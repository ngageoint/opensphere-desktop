package io.opensphere.mantle.data.geom;

/**
 * A Map Point Geometry Support.
 */
public interface MapEllipseGeometrySupport extends MapLocationGeometrySupport
{
    /**
     * Gets the ellipse orientation.
     *
     * @return the orientation ( degrees clockwise from north )
     */
    float getOrientation();

    /**
     * Gets the Semi Major Axis (Units: km).
     *
     * @return the Semi Major Axis
     */
    float getSemiMajorAxis();

    /**
     * Gets the Semi Minor Axis (Units: km).
     *
     * @return the Semi-Minor Axis.
     */
    float getSemiMinorAxis();

    /**
     * Sets the orientation ( degrees clockwise from north ).
     *
     * @param orient - the orientation
     */
    void setOrientation(float orient);

    /**
     * Sets the Semi Major Axis (Units: km).
     *
     * @param sma - the semi major axis
     */
    void setSemiMajorAxis(float sma);

    /**
     * Sets the Semi Minor Axis (Units: km).
     *
     * @param smi - the Semi-Minor Axis.
     */
    void setSemiMinorAxis(float smi);
}
