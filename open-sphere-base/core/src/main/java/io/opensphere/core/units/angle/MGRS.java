package io.opensphere.core.units.angle;

import io.opensphere.core.mgrs.MGRSConverter;

public class MGRS extends Coordinates

{

    /**
     * 
     */
    private static final long serialVersionUID = 8396025878700604825L;

    /** An MGRS converter. */
    static final MGRSConverter MGRS_CONVERTER = new MGRSConverter();

    /**
     * Construct with a specified value in degrees.
     *
     * @param magnitude degrees
     */
    public MGRS(double magnitude)
    {
        super(magnitude);
    }

    @Override
    public String getLongLabel()
    {
        return "MGRS";
    }

    @Override
    public String getShortLabel()
    {
        return "MGRS";
    }

    @Override
    public String toShortLabelString()
    {
        return "";
    }

    @Override
    public String toShortLabelString(char pos, char neg)
    {
        return "";
    }

    @Override
    public String toShortLabelString(int width, int precision)
    {
        return "";
    }

    @Override
    public String toShortLabelString(int width, int precision, char positive, char negative)
    {
        return "";
    }

}
