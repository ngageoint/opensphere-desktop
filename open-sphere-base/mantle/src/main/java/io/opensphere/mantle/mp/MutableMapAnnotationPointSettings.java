package io.opensphere.mantle.mp;

/**
 * The Interface MapAnnotationPointSettings.
 */
public interface MutableMapAnnotationPointSettings extends MapAnnotationPointSettings
{
    /**
     * Standard setter.
     *
     * @param annohide The hide annotation flag.
     * @param source the source of the change
     */
    void setAnnohide(boolean annohide, Object source);

    /**
     * Standard setter.
     *
     * @param desc The description flag.
     * @param source the source of the change
     */
    void setDesc(boolean desc, Object source);

    /**
     * Standard setter.
     *
     * @param dms The degree/min/sec flag.
     * @param source the source of the change
     */
    void setDms(boolean dms, Object source);

    /**
     * Standard setter.
     *
     * @param doton The dot on flag.
     * @param source the source of the change
     */
    void setDotOn(boolean doton, Object source);

    /**
     * Standard setter.
     *
     * @param latlon The lat/lon flag.
     * @param source the source of the change
     */
    void setLatLon(boolean latlon, Object source);

    /**
     * Sets the map annotation point to which this annotation settings belongs.
     *
     * @param mmap the new map annotation point
     */
    void setMapAnnotationPoint(MutableMapAnnotationPoint mmap);

    /**
     * Standard setter.
     *
     * @param mgrs The MGRS flag.
     * @param source the source of the change
     */
    void setMgrs(boolean mgrs, Object source);

    /**
     * Standard setter.
     *
     * @param altitude The altitude flag.
     * @param source the source of the change
     */
    void setAltitude(boolean altitude, Object source);

    /**
     * Standard setter.
     *
     * @param title The title flag.
     * @param source the source of the change
     */
    void setTitle(boolean title, Object source);

    /**
     * Standard setter.
     *
     * @param title The field title flag.
     * @param source the source of the change
     */
    void setFieldTitle(boolean title, Object source);

    /**
     * Sets the value of the Duration Flag.
     *
     * @param durationFlag the value to store in the Duration Flag.
     * @param source the source of the change
     */
    void setDuration(boolean durationFlag, Object source);

    /**
     * Sets the value of the Distance Flag.
     *
     * @param distanceFlag the value to store in the Distance Flag.
     * @param source the source of the change
     */
    void setDistance(boolean distanceFlag, Object source);

    /**
     * Sets the value of the Velocity Flag.
     *
     * @param velocityFlag the value to store in the Velocity Flag.
     * @param source the source of the change
     */
    void setVelocity(boolean velocityFlag, Object source);

    /**
     * Sets the value of the Heading Flag.
     *
     * @param headingFlag the value to store in the Heading Flag.
     * @param source the source of the change
     */
    void setHeading(boolean headingFlag, Object source);
}
