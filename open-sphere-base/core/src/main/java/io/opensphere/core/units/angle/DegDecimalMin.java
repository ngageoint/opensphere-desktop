package io.opensphere.core.units.angle;

import io.opensphere.core.model.LatLonAlt;

/** For formatting angles (i.e., lat/lon) in degrees and decimal minutes. */
public class DegDecimalMin extends Angle
{
    private static final int MINUTES_PRECISION = 3;

    /**
     * Construct with a specified value in degrees.
     * @param magnitude degrees
     */
    public DegDecimalMin(double magnitude)
    {
        super(magnitude);
    }

    @Override
    public String getLongLabel()
    {
        return "degrees decimal minutes";
    }

    @Override
    public String getShortLabel()
    {
        return "DDM";
    }

    @Override
    public String toShortLabelString()
    {
        return toShortLabelString(16, MINUTES_PRECISION);
    }

    @Override
    public String toShortLabelString(char pos, char neg)
    {
        return toShortLabelString(17, MINUTES_PRECISION, pos, neg);
    }

    @Override
    public String toShortLabelString(int width, int precision)
    {
        return formatValue(getMagnitude(), precision);
    }

    @Override
    public String toShortLabelString(int width, int precision, char positive, char negative)
    {
        return formatValue(getMagnitude(), precision, positive, negative);
    }

    /**
     * Format a value in degrees with specified precision and sign indicators.
     * @param deg the value in degrees
     * @param precision the number of digits after the decimal (minutes)
     * @param pos positive indicator
     * @param neg negative indicator
     * @return the requested String
     */
    private static String formatValue(double deg, int precision, char pos, char neg)
    {
        if (deg > 0.0)
            return formatValue(deg, precision) + pos;
        else
            return formatValue(-deg, precision) + neg;
    }

    /**
     * Format a value in degrees with specified precision and no sign indicator.
     * @param deg the value in degrees
     * @param precision the number of digits after the decimal (minutes)
     * @return the requested String
     */
    private static String formatValue(double deg, int precision)
    {
        return LatLonAlt.degToDdm(deg, precision);
    }
}
