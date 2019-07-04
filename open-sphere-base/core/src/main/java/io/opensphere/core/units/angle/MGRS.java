package io.opensphere.core.units.angle;

/** A Coordinate represented in the military grid reference system. */
public class MGRS extends Coordinates
{
    /**
     * The serial ID.
     */
    private static final long serialVersionUID = 8396025878700604825L;

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
        return "Military Grid Reference System";
    }

    @Override
    public String getShortLabel()
    {
        return "MGRS";
    }

    @Override
    public String toShortLabelString() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("MGRS only has one format for display");
    }

    @Override
    public String toShortLabelString(char pos, char neg) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("MGRS only has one format for display");
    }

    @Override
    public String toShortLabelString(int width, int precision) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("MGRS only has one format for display");
    }

    @Override
    public String toShortLabelString(int width, int precision, char positive, char negative) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("MGRS only has one format for display");
    }
}
