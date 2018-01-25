package io.opensphere.core.mgrs;

import io.opensphere.core.model.GeographicPosition;

/** Represents a grid rectangle. */
public abstract class GridRectangle
{
    /** The maximum latitude. */
    private double myMaxLatitude;

    /** The maximum longitude. */
    private double myMaxLongitude;

    /** The minimum latitude. */
    private double myMinLatitude;

    /** The minimum longitude. */
    private double myMinLongitude;

    /**
     * Default constructor.
     */
    public GridRectangle()
    {
    }

    /**
     * Constructor.
     *
     * @param minLat The minimum latitude.
     * @param maxLat The maximum latitude.
     * @param minLon The minimum longitude.
     * @param maxLon The maximum longitude.
     */
    public GridRectangle(double minLat, double maxLat, double minLon, double maxLon)
    {
        this.myMinLatitude = minLat;
        this.myMaxLatitude = maxLat;
        this.myMinLongitude = minLon;
        this.myMaxLongitude = maxLon;
    }

    /**
     * Checks to see if point is within rectangle.
     *
     * @param geoPos The point to check.
     * @return true if this rectangle contains the point, false otherwise
     */
    public boolean containsPoint(GeographicPosition geoPos)
    {
        return geoPos.getLatLonAlt().getLatD() >= myMinLatitude && geoPos.getLatLonAlt().getLatD() <= myMaxLatitude
                && geoPos.getLatLonAlt().getLonD() >= myMinLongitude && geoPos.getLatLonAlt().getLonD() <= myMaxLongitude;
    }

    /**
     * Standard accessor.
     *
     * @return The max latitude.
     */
    public double getMaxLatitude()
    {
        return myMaxLatitude;
    }

    /**
     * Standard accessor.
     *
     * @return The maximum longitude.
     */
    public double getMaxLongitude()
    {
        return myMaxLongitude;
    }

    /**
     * Standard accessor.
     *
     * @return The minimum latitude.
     */
    public double getMinLatitude()
    {
        return myMinLatitude;
    }

    /**
     * Standard accessor.
     *
     * @return The minimum longitude.
     */
    public double getMinLongitude()
    {
        return myMinLongitude;
    }

    /**
     * Standard setter.
     *
     * @param maxLatitude Set the maximum latitude.
     */
    public void setMaxLatitude(double maxLatitude)
    {
        this.myMaxLatitude = maxLatitude;
    }

    /**
     * Standard setter.
     *
     * @param maxLongitude Set the maximum longitude.
     */
    public void setMaxLongitude(double maxLongitude)
    {
        this.myMaxLongitude = maxLongitude;
    }

    /**
     * Standard setter.
     *
     * @param minLatitude The new minimum latitude.
     */
    public void setMinLatitude(double minLatitude)
    {
        this.myMinLatitude = minLatitude;
    }

    /**
     * Standard setter.
     *
     * @param minLongitude Set the minimum longitude.
     */
    public void setMinLongitude(double minLongitude)
    {
        this.myMinLongitude = minLongitude;
    }
}
