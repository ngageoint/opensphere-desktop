package io.opensphere.core.units.angle;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * An angle represented as degrees/minutes/seconds.
 */
public final class DegreesMinutesSeconds extends Angle
{
    /** Long label. */
    public static final String DMS_LONG_LABEL = "degrees minutes seconds";

    /** Short label. */
    public static final String DMS_SHORT_LABEL = "\u00B0'\"";

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Get the integer degrees portion of the angle.
     *
     * @param angle The angle in degrees.
     * @return The integer degrees portion.
     */
    public static int getDegrees(double angle)
    {
        int val = (int)angle;
        return val == 0 && angle < 0. ? -0 : val;
    }

    /**
     * Get the integer minutes portion of a magnitude in degrees.
     *
     * @param angle The angle in degrees.
     * @return The integer minutes portion.
     */
    public static int getMinutes(double angle)
    {
        return (int)(Math.abs(angle) % 1. * Constants.MINUTES_PER_DEGREE);
    }

    /**
     * Get the seconds portion of the angle.
     *
     * @param angle The angle in degrees.
     * @return The seconds.
     */
    public static double getSeconds(double angle)
    {
        return (Math.abs((angle - getDegrees(angle)) * Constants.MINUTES_PER_DEGREE) - getMinutes(angle))
                * Constants.SECONDS_PER_MINUTE;
    }

    /**
     * Return a string representation of an angle using its short label.
     *
     * @param angle The magnitude of the angle in degrees.
     * @param width The width of the output string.
     * @param precision The amount of precision of the output string.
     * @param positive Character to use if the angle is positive.
     * @param negative Character to use if the angle is negative.
     *
     * @return The string.
     */
    public static String getShortLabelString(double angle, int width, int precision, char positive, char negative)
    {
        if (angle > 0.)
        {
            return toShortLabelString(width - 1, precision, angle) + positive;
        }
        else
        {
            return toShortLabelString(width - 1, precision, -angle) + negative;
        }
    }

    /**
     * Get a format string for the specified width and precision.
     *
     * @param width The allowed width.
     * @param precision The precision.
     * @return The format string.
     */
    protected static String getFormatString(int width, int precision)
    {
        int secWidth = getSecondsWidth(width, precision);
        int minWidth = getMinutesWidth(width);
        int degWidth = width - 1 - (minWidth > 0 ? minWidth + 1 + (secWidth > 0 ? secWidth + 1 : 0) : 0);

        StringBuilder format = new StringBuilder(16);
        if (degWidth > 0)
        {
            format.append('%').append(degWidth).append(".0f\u00B0");
            if (minWidth == 2)
            {
                format.append("%02d'");
                if (secWidth > 0)
                {
                    int secPrec = getSecondsPrecision(precision, secWidth);
                    format.append("%0").append(secWidth).append('.').append(secPrec).append("f\"");
                }
            }
        }
        return format.toString();
    }

    /**
     * Format the given value.
     *
     * @param width The width of the output string.
     * @param precision The precision of the output string.
     * @param magnitude The value.
     * @return The formatted string.
     */
    private static String toShortLabelString(int width, int precision, double magnitude)
    {
        double degrees = (int)magnitude;
        if (degrees == 0. && magnitude < 0.)
        {
            degrees = -0.;
        }
        int minutes = getMinutes(magnitude);
        double seconds = getSeconds(magnitude);

        int secWidth = getSecondsWidth(width, precision);
        int secPrec = getSecondsPrecision(precision, secWidth);

        double roundingThreshold = 60 - 5 * Math.pow(10, -secPrec - 1);
        if (seconds >= roundingThreshold)
        {
            if (minutes == 59)
            {
                minutes = 0;
                degrees = magnitude > 0. ? degrees + 1 : degrees - 1;
            }
            else
            {
                ++minutes;
            }
            seconds = 0.;
        }

        if (getMinutesWidth(width) == 0 && minutes >= 30)
        {
            degrees = magnitude > 0. ? degrees + 1 : degrees - 1;
        }
        return String.format(getFormatString(width, precision), Double.valueOf(degrees), Integer.valueOf(minutes),
                Double.valueOf(seconds));
    }

    /**
     * Given the overall width of a DMS string, get the width of the minutes
     * field.
     *
     * @param width The allowed width.
     * @return The width of the minutes.
     */
    private static int getMinutesWidth(int width)
    {
        return width < 8 ? 0 : 2;
    }

    /**
     * Given the seconds width and the overall precision, get the precision of
     * the seconds field.
     *
     * @param precision The overall precision.
     * @param secWidth The width of the seconds field.
     * @return The precision of the seconds field.
     */
    private static int getSecondsPrecision(int precision, int secWidth)
    {
        return secWidth > 2 ? MathUtil.clamp(precision, 0, secWidth - 3) : 0;
    }

    /**
     * Given the overall width and precision of a DMS string, get the width of
     * the seconds field.
     *
     * @param width The allowed width.
     * @param precision The precision.
     * @return The width of the seconds.
     */
    private static int getSecondsWidth(int width, int precision)
    {
        return width < 11 ? 0 : width < 13 || precision == 0 ? 2
                : Math.min(width - 9, precision > Integer.MAX_VALUE - 3 ? Integer.MAX_VALUE : precision + 3);
    }

    /**
     * Construct this angle from another angle.
     *
     * @param ang The other angle.
     */
    public DegreesMinutesSeconds(Angle ang)
    {
        super(ang.getMagnitude());
    }

    /**
     * Constructor.
     *
     * @param degrees The magnitude of the angle in degrees.
     */
    public DegreesMinutesSeconds(double degrees)
    {
        super(degrees);
    }

    @Override
    public DegreesMinutesSeconds clone()
    {
        return (DegreesMinutesSeconds)super.clone();
    }

    @Override
    public String getLongLabel()
    {
        return DMS_LONG_LABEL;
    }

    @Override
    public String getShortLabel()
    {
        return DMS_SHORT_LABEL;
    }

    @Override
    public String toShortLabelString()
    {
        return toShortLabelString(16, 4);
    }

    @Override
    public String toShortLabelString(char positive, char negative)
    {
        return toShortLabelString(17, 4, positive, negative);
    }

    @Override
    public String toShortLabelString(int width, int precision)
    {
        return toShortLabelString(width, precision, getMagnitude());
    }

    @Override
    public String toShortLabelString(int width, int precision, char positive, char negative)
    {
        return getShortLabelString(getMagnitude(), width, precision, positive, negative);
    }

    /**
     * Get the integer degrees portion of the angle.
     *
     * @return The degrees.
     */
    protected int getDegrees()
    {
        return getDegrees(getMagnitude());
    }

    /**
     * Get the integer minutes portion of the angle.
     *
     * @return The minutes.
     */
    protected int getMinutes()
    {
        double magnitude = getMagnitude();
        return getMinutes(magnitude);
    }

    /**
     * Get the seconds portion of the angle.
     *
     * @return The seconds.
     */
    protected double getSeconds()
    {
        double degrees = getMagnitude();
        return getSeconds(degrees);
    }
}
