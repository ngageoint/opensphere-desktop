package io.opensphere.core.units.angle;

/**
 * An angle represented as decimal degrees.
 */
public final class DecimalDegrees extends Angle
{
    /** Long label. */
    public static final String DEGREES_LONG_LABEL = "decimal degrees";

    /** Short label. */
    public static final String DEGREES_SHORT_LABEL = "\u00B0";

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Construct this angle from another angle.
     *
     * @param ang The other angle.
     */
    public DecimalDegrees(Angle ang)
    {
        super(ang.getMagnitude());
    }

    /**
     * Constructor.
     *
     * @param degrees The magnitude of the angle in degrees.
     */
    public DecimalDegrees(double degrees)
    {
        super(degrees);
    }

    @Override
    public DecimalDegrees clone()
    {
        return (DecimalDegrees)super.clone();
    }

    @Override
    public String getLongLabel()
    {
        return DEGREES_LONG_LABEL;
    }

    @Override
    public String getShortLabel()
    {
        return DEGREES_SHORT_LABEL;
    }

    @Override
    public String toShortLabelString()
    {
        return toShortLabelString(11, 6);
    }

    @Override
    public String toShortLabelString(char positive, char negative)
    {
        return toShortLabelString(11, 6, positive, negative);
    }

    @Override
    public String toShortLabelString(int width, int precision)
    {
        return new StringBuilder().append(format(width - DEGREES_SHORT_LABEL.length(), precision, getMagnitude()))
                .append(DEGREES_SHORT_LABEL).toString();
    }

    @Override
    public String toShortLabelString(int width, int precision, char positive, char negative)
    {
        if (getMagnitude() > 0.)
        {
            return new StringBuilder().append(format(width - DEGREES_SHORT_LABEL.length() - 1, precision, getMagnitude()))
                    .append(DEGREES_SHORT_LABEL).append(positive).toString();
        }
        else
        {
            return new StringBuilder().append(format(width - DEGREES_SHORT_LABEL.length() - 1, precision, -getMagnitude()))
                    .append(DEGREES_SHORT_LABEL).append(negative).toString();
        }
    }

    /**
     * Format an angle value.
     *
     * @param width The width of the output string.
     * @param precision The precision of the output string.
     * @param mag The value.
     *
     * @return The formatted value.
     */
    protected String format(int width, int precision, double mag)
    {
        return String.format("% " + width + "." + precision + "f", Double.valueOf(mag));
    }
}
