package io.opensphere.core.common.util;

/**
 * This class converts angles from one reference frame to another.
 *
 * <pre>
 *
 *            0
 *            |
 *            |
 *            |
 * 270 --------------- 90
 *            |
 *            |
 *            |
 *           180
 *
 *        figure 1: Compass coordinate frame
 *
 *
 *
 *            90
 *            |
 *            |
 *            |
 * 180 --------------- 0
 *            |
 *            |
 *            |
 *           270
 *
 *        figure 2: Mathmatical poloar coordinate frame
 *
 *
 * </pre>
 */
public class Angles
{

    /**
     * This method converts an angle given in compass degrees, a left-hand polar
     * coordinate system (see fig 1), to the common mathmatical right-hand
     * coordinate system (see fig 2).
     *
     *
     * @param navAngle navigation angle in degrees
     * @return int mathAngle
     */
    public static int Nav2Math(int navAngle)
    {
        int mathAngle;

        mathAngle = (-navAngle + 90 + 360) % 360;
        return mathAngle;
    }

    /**
     * This method converts an angle given in compass degrees, a left-hand polar
     * coordinate system (see fig 1), to the common mathmatical right-hand
     * coordinate system (see fig 2).
     *
     *
     * @param navAngle navigation angle in degrees
     * @return double mathAngle
     */
    public static double Nav2Math(double navAngle)
    {
        double mathAngle;

        mathAngle = (-navAngle + 90 + 360) % 360;
        return mathAngle;
    }

    /**
     * This method converts an angle given in compass degrees, a left-hand polar
     * coordinate system (see fig 1), to the common mathmatical right-hand
     * coordinate system (see fig 2).
     *
     *
     * @param navAngle navigation angle in degrees
     * @return float mathAngle
     */
    public static float Nav2Math(float navAngle)
    {
        float mathAngle;

        mathAngle = (-navAngle + 90 + 360) % 360;
        return mathAngle;
    }

    /**
     * This method converts an angle given in the common mathmatical right-hand
     * coordinate system (see fig 2), to compass degrees, a left-hand polar
     * coordinate system (see fig 1).
     *
     *
     * @param mathAngle
     * @return int navAngle
     */
    public static int Math2Nav(int mathAngle)
    {
        int navAngle;

        navAngle = (-mathAngle - 90 + 360) % 360;

        return navAngle;
    }

    /**
     * This method converts an angle given in the common mathmatical right-hand
     * coordinate system (see fig 2), to compass degrees, a left-hand polar
     * coordinate system (see fig 1).
     *
     *
     * @param mathAngle
     * @return double navAngle
     */
    public static double Math2Nav(double mathAngle)
    {
        double navAngle;

        navAngle = (-mathAngle - 90 + 360) % 360;

        return navAngle;
    }

    /**
     * This method converts an angle given in the common mathmatical right-hand
     * coordinate system (see fig 2), to compass degrees, a left-hand polar
     * coordinate system (see fig 1).
     *
     *
     * @param mathAngle
     * @return float navAngle
     */
    public static float Math2Nav(float mathAngle)
    {
        float navAngle;

        navAngle = (-mathAngle - 90 + 360) % 360;

        return navAngle;
    }

}
