/**
 *
 */
package io.opensphere.core.common.convolve;

import cern.colt.matrix.DoubleMatrix2D;

/**
 *
 *
 */
public class Ellipse extends Position
{
    /** 95% ellipse Semi-Major axis */
    private double sma;

    /** 95% ellipse Semi-Minor axis */
    private double smi;

    /** 95% ellipse Orientation */
    private double orientation;

    /** 95% Altitude uncertainty */
    private double altitudeUncertainty;

    /** 95% Ellipsoid Max Axis Length */
    private double maxAxisLength;

    /** Inverse of the covariance matrix */
    private DoubleMatrix2D atwa;

    /** The sum of the atwai(s) that contributed to this ellipse. */
    private DoubleMatrix2D atwai;

    /** S */
    private DoubleMatrix2D s;

    /** Ellipse size in ellipse norm units */
    private double norm2;

    /**
     * Default Constructor that creates a point at (0,0,0) with no error ellipse
     * information.
     */
    public Ellipse()
    {
        super();
        sma = 0;
        smi = 0;
        orientation = 0;
        altitudeUncertainty = 0;
        maxAxisLength = 0;
        atwa = null;
        atwai = null;
        norm2 = 0;
    }

    /**
     * Constructor that creates a point at (x,y,0) with no error ellipse
     * information.
     *
     * @param lon Longitude
     * @param lat Latitude
     */
    public Ellipse(double lon, double lat)
    {
        super(lon, lat);
        sma = 0;
        smi = 0;
        orientation = 0;
        altitudeUncertainty = 0;
        maxAxisLength = 0;
        atwa = null;
        atwai = null;
        norm2 = 0;
    }

    /**
     * Constructor that creates a point at (x,y,z) with no error ellipse
     * information.
     *
     * @param lon Longitude
     * @param lat Latitude
     * @param alt Altitude
     */
    public Ellipse(double lon, double lat, double alt)
    {
        super(lon, lat, alt);
        sma = 0;
        smi = 0;
        orientation = 0;
        altitudeUncertainty = 0;
        maxAxisLength = 0;
        atwa = null;
        atwai = null;
        norm2 = 0;
    }

    /**
     * Constructor that creates a point at (x,y,z) with no error ellipse
     * information.
     *
     * @param lon Longitude
     * @param lat Latitude
     * @param alt Altitude
     * @param sma Semi-Major of the ellipse in Kilometers
     * @param smi Semi-Minor of the ellipse in Kilometers
     * @param orientation Orientation of the ellipse clockwise wrt north.
     */
    public Ellipse(double lon, double lat, double alt, double sma, double smi, double orientation)
    {
        super(lon, lat, alt);
        this.sma = sma;
        this.smi = smi;
        this.orientation = orientation;
        altitudeUncertainty = 0;
        maxAxisLength = 0;
        atwa = null;
        atwai = null;
        norm2 = 0;
    }

    public Ellipse(Position pos)
    {
        super(pos);
        sma = 0;
        smi = 0;
        orientation = 0;
        altitudeUncertainty = 0;
        maxAxisLength = 0;
        atwa = null;
        atwai = null;
        norm2 = 0;
    }

    /**
     * Convenience function, equivalent to getX()
     *
     * @return Longitude
     */
    public double getLon()
    {
        return getX();
    }

    /**
     * Convenience function, equivalent to setX()
     *
     * @param Longitude
     */
    public void setLon(double lon)
    {
        setX(lon);
    }

    /**
     * Convenience function, equivalent to getY()
     *
     * @return Latitude
     */
    public double getLat()
    {
        return getY();
    }

    /**
     * Convenience function, equivalent to setY()
     *
     * @param Latitude
     */
    public void setLat(double lat)
    {
        setY(lat);
    }

    /**
     * Convenience function, equivalent to getZ()
     *
     * @return Altitude
     */
    public double getAlt()
    {
        return getZ();
    }

    /**
     * Convenience function, equivalent to setZ()
     *
     * @param Altitude
     */
    public void setAlt(double alt)
    {
        setZ(alt);
    }

    public double getSMA()
    {
        return sma;
    }

    public void setSMA(double sma)
    {
        this.sma = sma;
    }

    public double getSMI()
    {
        return smi;
    }

    public void setSMI(double smi)
    {
        this.smi = smi;
    }

    public double getOrientation()
    {
        return orientation;
    }

    public void setOrientation(double orientation)
    {
        this.orientation = orientation;
    }

    public double getAltitudeUncertainty()
    {
        return altitudeUncertainty;
    }

    public void setAltitudeUncertainty(double altitudeUncertainty)
    {
        this.altitudeUncertainty = altitudeUncertainty;
    }

    public double getMaxAxisLength()
    {
        return maxAxisLength;
    }

    public void setMaxAxisLength(double maxAxisLength)
    {
        this.maxAxisLength = maxAxisLength;
    }

    public DoubleMatrix2D getAtwa()
    {
        return atwa;
    }

    public void setAtwa(DoubleMatrix2D atwa)
    {
        this.atwa = atwa;
    }

    public DoubleMatrix2D getATWAI()
    {
        return atwai;
    }

    public void setATWAI(DoubleMatrix2D ATWAI)
    {
        atwai = ATWAI;
    }

    public DoubleMatrix2D getS()
    {
        return s;
    }

    public void setS(DoubleMatrix2D s)
    {
        this.s = s;
    }

    /**
     * Get the square of the relative miss distance in ellipse norm units
     *
     * @return norm2
     */
    public double getNorm2()
    {
        return norm2;
    }

    /**
     * Set the square of the relative relative miss distance in ellipse norm
     * units
     *
     * @param norm2 The new norm2 value
     */
    public void setNorm2(double norm2)
    {
        this.norm2 = norm2;
    }

    /**
     * Converts to printable string.
     *
     * @see io.opensphere.core.common.convolve.Position#toString()
     */
    @Override
    public String toString()
    {
        return "Lat: " + getY() + "; Lon: " + getX() + "; Alt: " + getZ() + "; SMA: " + sma + "; SMI: " + smi + "; ORIENT: "
                + orientation + "; Norm2: " + norm2;
    }
}
