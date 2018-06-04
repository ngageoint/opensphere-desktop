package io.opensphere.infinity.json;

import io.opensphere.core.model.GeographicPosition;

/** Elasticsearch coordinate JSON bean. */
public class Coordinate
{
    /** The lat. */
    private double myLat;

    /** The lon. */
    private double myLon;

    public Coordinate()
    {
    }

    public Coordinate(GeographicPosition position)
    {
        myLat = position.getLatLonAlt().getLatD();
        myLon = position.getLatLonAlt().getLonD();
    }

    /**
     * Gets the lat.
     *
     * @return the lat
     */
    public double getLat()
    {
        return myLat;
    }

    /**
     * Sets the lat.
     *
     * @param lat the lat
     */
    public void setLat(double lat)
    {
        myLat = lat;
    }

    /**
     * Gets the lon.
     *
     * @return the lon
     */
    public double getLon()
    {
        return myLon;
    }

    /**
     * Sets the lon.
     *
     * @param lon the lon
     */
    public void setLon(double lon)
    {
        myLon = lon;
    }
}
