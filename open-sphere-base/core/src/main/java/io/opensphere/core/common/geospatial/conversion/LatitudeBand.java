package io.opensphere.core.common.geospatial.conversion;

public class LatitudeBand
{
    /** letter representing latitude band */
    public long letter;

    /** minimum northing for latitude band */
    public double minNorthing;

    /** upper latitude for latitude band */
    public double north;

    /** lower latitude for latitude band */
    public double south;

    /** latitude band northing offset */
    public double northingOffset;

    public LatitudeBand(int l, double mn, double n, double s, double no)
    {
        letter = l;
        minNorthing = mn;
        north = n;
        south = s;
        northingOffset = no;
    }
}
