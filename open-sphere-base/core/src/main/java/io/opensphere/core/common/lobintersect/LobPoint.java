package io.opensphere.core.common.lobintersect;

import java.util.Date;

/**
 * Container class for working with line of bearings.
 */
public class LobPoint extends Point implements Comparable<LobPoint>
{

    protected double freq;

    protected double bearing;

    protected double range;

    protected String featureId;

    protected boolean hasConf; // SOIConfirmation=="definite"

    public LobPoint(LobPoint other)
    {
        super(other.lat, other.lon, other.alt, other.ptName, other.timeStamp);
        freq = other.freq;
        bearing = other.bearing;
        range = other.range;
        featureId = other.featureId;
    }

    public LobPoint(Date timeStamp, String ptName, double freq, double lat, double lon, double alt, double bearing, double range,
            String featureId)
    {
        super(lat, lon, alt, ptName, timeStamp);

        this.freq = freq;
        this.bearing = bearing;
        this.range = range;
        this.featureId = featureId;
    }

    /**
     * Gets the frequency in Hz
     *
     * @return double
     */
    public double getFreq()
    {
        return freq;
    }

    /**
     * Sets the frequency in Hz
     *
     * @param freq
     */
    public void setFreq(double freq)
    {
        this.freq = freq;
    }

    /**
     * Gets the bearing in radians
     *
     * @return double
     */
    public double getBearing()
    {
        return bearing;
    }

    /**
     * Sets the bearing in radians
     *
     * @param bearing
     */
    public void setBearing(double bearing)
    {
        this.bearing = bearing;
    }

    /**
     * Gets the range in meters
     *
     * @return double
     */
    public double getRange()
    {
        return range;
    }

    /**
     * Sets the range in meters
     *
     * @param range
     */
    public void setRange(double range)
    {
        this.range = range;
    }

    public String getFeatureId()
    {
        return featureId;
    }

    public void setFeatureId(String featureId)
    {
        this.featureId = featureId;
    }

    /**
     * @return the hasConf
     */
    public boolean hasConf()
    {
        return hasConf;
    }

    /**
     * @param hasConf the hasConf to set
     */
    public void setHasConf(boolean hasConf)
    {
        this.hasConf = hasConf;
    }

    /**
     * Converts this instance to a string.
     */
    @Override
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("Alt=" + alt);
        str.append(", Bearing= " + bearing);
        str.append(", Range= " + range);
        str.append(", Rf= " + freq);
        str.append(", Lat= " + lat);
        str.append(", Lon= " + lon);
        str.append(", PtName= " + ptName);
        str.append(", Range= " + range);
        str.append(", UTC seconds= " + timeMs);
        str.append(", Date= " + timeStamp);

        return str.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(bearing);
        result = prime * result + (int)(temp ^ temp >>> 32);
        result = prime * result + (featureId == null ? 0 : featureId.hashCode());
        temp = Double.doubleToLongBits(freq);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(range);
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
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        LobPoint other = (LobPoint)obj;
        if (Double.doubleToLongBits(bearing) != Double.doubleToLongBits(other.bearing))
        {
            return false;
        }
        if (featureId == null)
        {
            if (other.featureId != null)
            {
                return false;
            }
        }
        else if (!featureId.equals(other.featureId))
        {
            return false;
        }
        if (Double.doubleToLongBits(freq) != Double.doubleToLongBits(other.freq))
        {
            return false;
        }
        if (Double.doubleToLongBits(range) != Double.doubleToLongBits(other.range))
        {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(LobPoint other)
    {
        if (equals(other))
        {
            return 0;
        }

        if (!timeStamp.equals(other.timeStamp))
        {
            return timeStamp.compareTo(other.timeStamp);
        }

        if (freq < other.freq)
        {
            return -1;
        }
        else if (freq > other.freq)
        {
            return 1;
        }

        if (lat < other.lat)
        {
            return -1;
        }
        else if (lat > other.lat)
        {
            return 1;
        }

        if (lon < other.lon)
        {
            return -1;
        }
        else if (lon > other.lon)
        {
            return 1;
        }

        return ptName.compareTo(other.ptName);
    }
}
