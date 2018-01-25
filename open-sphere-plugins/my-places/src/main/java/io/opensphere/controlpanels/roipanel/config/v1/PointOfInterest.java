package io.opensphere.controlpanels.roipanel.config.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.Utilities;

/** Class that describes a lat/lon point of interest. */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PointOfInterest")
public class PointOfInterest
{
    /** The latitude in degrees. */
    @XmlAttribute(name = "lat")
    private double myLat;

    /** The longitude in degrees. */
    @XmlAttribute(name = "lon")
    private double myLon;

    /**
     * Default constructor.
     */
    public PointOfInterest()
    {
    }

    /**
     * Constructor.
     *
     * @param lat The latitude (in degrees).
     * @param lon The longitude (in degrees).
     */
    public PointOfInterest(double lat, double lon)
    {
        this();
        myLat = lat;
        myLon = lon;
    }

    /**
     * Constructor.
     *
     * @param lla The LatLonAlt point to initialize with.
     */
    public PointOfInterest(LatLonAlt lla)
    {
        this(lla.getLatD(), lla.getLonD());
    }

    /**
     * Copy constructor.
     *
     * @param other The other PointOfInterest to initialize with.
     */
    public PointOfInterest(PointOfInterest other)
    {
        this(other.getLat(), other.getLon());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (this == obj)
        {
            return true;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        PointOfInterest other = (PointOfInterest)obj;
        return Utilities.equalsOrBothNaN(myLat, other.myLat) && Utilities.equalsOrBothNaN(myLon, other.myLon);
    }

    /**
     * Standard getter.
     *
     * @return The latitude (degrees).
     */
    public double getLat()
    {
        return myLat;
    }

    /**
     * Standard getter.
     *
     * @return The longitude (degrees).
     */
    public double getLon()
    {
        return myLon;
    }

    /**
     * Return as a LatLonAlt point.
     *
     * @return The LatLonAlt point.
     */
    public LatLonAlt getPoint()
    {
        return LatLonAlt.createFromDegrees(myLat, myLon);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();

        long temp;
        temp = Double.doubleToLongBits(myLat);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(myLon);
        result = prime * result + (int)(temp ^ temp >>> 32);
        return result;
    }

    /**
     * Standard setter.
     *
     * @param lat The latitude (degrees).
     */
    public void setLat(double lat)
    {
        myLat = lat;
    }

    /**
     * Standard setter.
     *
     * @param lon The longitude (degrees).
     */
    public void setLon(double lon)
    {
        myLon = lon;
    }
}
