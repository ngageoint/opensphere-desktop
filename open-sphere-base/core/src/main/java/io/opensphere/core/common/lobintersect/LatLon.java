package io.opensphere.core.common.lobintersect;

/**
 * Container class for a location on the earth.
 */
public class LatLon
{

    /**
     * Latitude, any units can be used since there are no calculations or
     * transformations.
     */
    private double lat;

    /**
     * Longitude, any units can be used since there are no calculations or
     * transformations.
     */
    private double lon;

    /**
     * Constructor accepting lat and lon as double.
     *
     * @param lat
     * @param lon
     */
    public LatLon(double lat, double lon)
    {
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * Sets the latitude
     *
     * @param lat
     */
    public void setLat(double lat)
    {
        this.lat = lat;
    }

    /**
     * Gets the latitude
     *
     * @return double
     */
    public double getLat()
    {
        return lat;
    }

    /**
     * Sets the longitude
     *
     * @param lat
     */
    public void setLon(double lon)
    {
        this.lon = lon;
    }

    /**
     * Gets the longitude
     *
     * @return double
     */
    public double getLon()
    {
        return lon;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(lat);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(lon);
        result = prime * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        LatLon other = (LatLon)obj;
        if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
        {
            return false;
        }
        if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon))
        {
            return false;
        }
        return true;
    }
}
