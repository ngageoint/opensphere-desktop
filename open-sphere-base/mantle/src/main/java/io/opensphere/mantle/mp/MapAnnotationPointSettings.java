package io.opensphere.mantle.mp;

/**
 * The Interface BaseMapAnnotationPointSettings.
 */
public interface MapAnnotationPointSettings
{
    /**
     * Standard getter.
     *
     * @return True if hide annotation flag is set, false otherwise.
     */
    boolean isAnnohide();

    /**
     * Standard getter.
     *
     * @return True if description flag is set, false otherwise.
     */
    boolean isDesc();

    /**
     * Standard getter.
     *
     * @return True if degree/min/sec flag is set, false otherwise.
     */
    boolean isDms();

    /**
     * Standard getter.
     *
     * @return True if color flag is set, false otherwise.
     */
    boolean isDotOn();

    /**
     * Standard getter.
     *
     * @return True if lat/lon flag is set, false otherwise.
     */
    boolean isLatLon();

    /**
     * Standard getter.
     *
     * @return True if MGRS flag is set, false otherwise.
     */
    boolean isMgrs();

    /**
     * Standard getter.
     *
     * @return True if altitude flag is set, false otherwise.
     */
    boolean isAltitude();

    /**
     * Standard getter.
     *
     * @return True if title flag is set, false otherwise.
     */
    boolean isTitle();

    /**
     * Standard getter.
     *
     * @return True if field title flag is set, false otherwise.
     */
    boolean isFieldTitle();

    /**
     * Standard getter to get the value of the duration flag.
     *
     * @return True if duration flag is set, false otherwise.
     */
    boolean isDuration();

    /**
     * Standard getter to get the value of the distance flag.
     *
     * @return True if distance flag is set, false otherwise.
     */
    boolean isDistance();

    /**
     * Standard getter to get the value of the velocity flag.
     *
     * @return True if velocity flag is set, false otherwise.
     */
    boolean isVelocity();

    /**
     * Standard getter to get the value of the heading flag.
     *
     * @return True if heading flag is set, false otherwise.
     */
    boolean isHeading();
}
