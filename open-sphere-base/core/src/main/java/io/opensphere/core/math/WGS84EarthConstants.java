package io.opensphere.core.math;

import io.opensphere.core.util.MathUtil;

/** Earth constants. */
public final class WGS84EarthConstants
{
    /** Circumference around the equator. */
    public static final double CIRCUMFERENCE_EQUATORIAL_M;

    /** Mean circumference. */
    public static final double CIRCUMFERENCE_MEAN_M;

    /** Circumference passing through the poles. */
    public static final double CIRCUMFERENCE_MERIDIAN_M;

    /** First eccentricity squared. */
    public static final double FIRST_ECCENTRICITY_SQ;

    /** First eccentricity ^ 4. */
    public static final double FIRST_ECCENTRICITY_FOURTH;

    /** First eccentricity ^ 6. */
    public static final double FIRST_ECCENTRICITY_SIXTH;

    /** Flattening. */
    public static final double FLATTENING;

    /** Reciprocal of flattening as defined by WGS84. */
    public static final double INVERSE_FLATTENING = 298.257223563;

    /** One minus the flattening squared. */
    public static final double ONE_MINUS_FLATTENING_SQ;

    /** Equatorial radius. */
    public static final double RADIUS_EQUATORIAL_M;

    /** Mean radius. */
    public static final double RADIUS_MEAN_M;

    /** Polar radius. */
    public static final double RADIUS_POLAR_M;

    /** Second eccentricity squared. */
    public static final double SECOND_ECCENTRICITY_SQ;

    /** Semi-major axis. */
    public static final double SEMI_MAJOR_AXIS_M = 6378137.0;

    /** Semi-major axis squared. */
    public static final double SEMI_MAJOR_AXIS_M_SQ;

    /** Semi-minor axis. */
    public static final double SEMI_MINOR_AXIS_M;

    /** Semi-minor axes squared. */
    public static final double SEMI_MINOR_AXIS_M_SQ;

    static
    {
        FLATTENING = 1 / INVERSE_FLATTENING;
        SEMI_MINOR_AXIS_M = SEMI_MAJOR_AXIS_M - SEMI_MAJOR_AXIS_M / INVERSE_FLATTENING;
        SEMI_MAJOR_AXIS_M_SQ = SEMI_MAJOR_AXIS_M * SEMI_MAJOR_AXIS_M;
        SEMI_MINOR_AXIS_M_SQ = SEMI_MINOR_AXIS_M * SEMI_MINOR_AXIS_M;
        FIRST_ECCENTRICITY_SQ = (SEMI_MAJOR_AXIS_M_SQ - SEMI_MINOR_AXIS_M_SQ) / SEMI_MAJOR_AXIS_M_SQ;
        SECOND_ECCENTRICITY_SQ = (SEMI_MAJOR_AXIS_M_SQ - SEMI_MINOR_AXIS_M_SQ) / SEMI_MINOR_AXIS_M_SQ;
        ONE_MINUS_FLATTENING_SQ = (1 - FLATTENING) * (1 - FLATTENING);
        FIRST_ECCENTRICITY_FOURTH = FIRST_ECCENTRICITY_SQ * FIRST_ECCENTRICITY_SQ;
        FIRST_ECCENTRICITY_SIXTH = FIRST_ECCENTRICITY_FOURTH * FIRST_ECCENTRICITY_SQ;

        RADIUS_EQUATORIAL_M = SEMI_MAJOR_AXIS_M;
        RADIUS_POLAR_M = SEMI_MINOR_AXIS_M;
        RADIUS_MEAN_M = (1 - FLATTENING / 3) * RADIUS_EQUATORIAL_M;
        CIRCUMFERENCE_MERIDIAN_M = RADIUS_POLAR_M * MathUtil.TWO_PI;
        CIRCUMFERENCE_EQUATORIAL_M = RADIUS_EQUATORIAL_M * MathUtil.TWO_PI;
        CIRCUMFERENCE_MEAN_M = RADIUS_MEAN_M * MathUtil.TWO_PI;
    }

    /** Disallow instantiation. */
    private WGS84EarthConstants()
    {
    }
}
