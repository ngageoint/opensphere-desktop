package io.opensphere.core.common.lobintersect;

import java.util.Date;

/**
 * Container for a simple point.
 */
public class Point
{

    protected double lat;

    protected double lon;

    protected double alt;

    protected String ptName;

    protected Date timeStamp;

    protected long timeMs;

    /**
     *
     * @param lat - rads
     * @param lon - rads
     * @param alt
     * @param ptName
     * @param timeStamp
     */
    public Point(double lat, double lon, double alt, String ptName, Date timeStamp)
    {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.ptName = ptName;
        this.timeStamp = timeStamp;
        timeMs = timeStamp.getTime();
    }

    /**
     * Sets the point name.
     *
     * @param ptName
     */
    public void setPtName(String ptName)
    {
        this.ptName = ptName;
    }

    /**
     * Gets the point name
     *
     * @return string
     */
    public String getPtName()
    {
        return ptName;
    }

    /**
     * Sets the point altitude
     *
     * @param alt
     */
    public void setAlt(double alt)
    {
        this.alt = alt;
    }

    /**
     * Gets the point altitude
     *
     * @return double
     */
    public double getAlt()
    {
        return alt;
    }

    /**
     * Sets the point longitude
     *
     * @param lon
     */
    public void setLon(double lon)
    {
        this.lon = lon;
    }

    /**
     * Gets the points longitude
     *
     * @return double
     */
    public double getLon()
    {
        return lon;
    }

    /**
     * Sets the point latitude
     *
     * @param lat
     */
    public void setLat(double lat)
    {
        this.lat = lat;
    }

    /**
     * Gets the point latitude
     *
     * @return double
     */
    public double getLat()
    {
        return lat;
    }

    /**
     * Sets the points timestamp
     *
     * @param timeStamp
     */
    public void setTimeStamp(Date timeStamp)
    {
        this.timeStamp = timeStamp;
        timeMs = timeStamp.getTime();
    }

    /**
     * Gets the point timestamp
     *
     * @return Date
     */
    public Date getTimeStamp()
    {
        return timeStamp;
    }

    /**
     * Gets the points timestamp in ms
     *
     * @return long
     */
    public long getTimeMs()
    {
        return timeMs;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(alt);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(lat);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(lon);
        result = prime * result + (int)(temp ^ temp >>> 32);
        result = prime * result + (ptName == null ? 0 : ptName.hashCode());
        result = prime * result + (int)(timeMs ^ timeMs >>> 32);
        result = prime * result + (timeStamp == null ? 0 : timeStamp.hashCode());
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
        Point other = (Point)obj;
        if (Double.doubleToLongBits(alt) != Double.doubleToLongBits(other.alt))
        {
            return false;
        }
        if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
        {
            return false;
        }
        if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon))
        {
            return false;
        }
        if (ptName == null)
        {
            if (other.ptName != null)
            {
                return false;
            }
        }
        else if (!ptName.equals(other.ptName))
        {
            return false;
        }
        if (timeMs != other.timeMs)
        {
            return false;
        }
        if (timeStamp == null)
        {
            if (other.timeStamp != null)
            {
                return false;
            }
        }
        else if (!timeStamp.equals(other.timeStamp))
        {
            return false;
        }
        return true;
    }

}
