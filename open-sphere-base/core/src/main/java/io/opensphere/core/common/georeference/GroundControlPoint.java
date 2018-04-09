package io.opensphere.core.common.georeference;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simple data container to hold ground control points for georeferencing. The
 * four important pieces of information are: pixel, line, latitude, longitude.
 * Pixel and line assume upper left of image is 0,0. Extending positive, and
 * increasing to the right, and down.
 */
@XmlRootElement(name = "GroundControlPoint")
@XmlAccessorType(XmlAccessType.FIELD)
public class GroundControlPoint
{
    /**
     * Deep clone list of GCPs.
     *
     * @param listOfGCPs
     * @return
     */
    public static List<GroundControlPoint> cloneList(List<GroundControlPoint> listOfGCPs)
    {
        final List<GroundControlPoint> al = new ArrayList<>();
        for (final GroundControlPoint gcp : listOfGCPs)
        {
            al.add((GroundControlPoint)gcp.clone());
        }
        return al;
    }

    @XmlElement(name = "Pixel")
    protected double pixel = 0;

    @XmlElement(name = "Line")
    protected double line = 0;

    @XmlElement(name = "Longitude")
    protected double longitude = 0;

    @XmlElement(name = "Latitude")
    protected double latitude = 0;

    /**
     * This constructor does not initialize any values. User must call setters.
     *
     */
    public GroundControlPoint()
    {
        super();
    }

    /**
     * Simple constructor that just assigns local values as specified. Pixel and
     * line assume upper left of image is 0,0. Extending positive, and
     * increasing to the right, and down. So the lower right is maxPixel,
     * maxLine
     *
     * @param lat - the lat that maps to the pixel/line combo
     * @param lon - the lon that maps to a pixel/line combo
     * @param pixel - the pixel that maps to a lat/lon combo (x location in
     *            image)
     * @param line - the line that maps to a lat/lon combo (y location in image)
     */
    public GroundControlPoint(double lat, double lon, double pixel, double line)
    {
        super();

        latitude = lat;
        longitude = lon;
        this.pixel = pixel;
        this.line = line;
    }

    /**
     * If this class is extended please maintain this as a deep clone.
     */
    @Override
    public Object clone()
    {
        final GroundControlPoint gcp = new GroundControlPoint();
        gcp.latitude = latitude;
        gcp.longitude = longitude;
        gcp.line = line;
        gcp.pixel = pixel;
        return gcp;
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
        final GroundControlPoint other = (GroundControlPoint)obj;
        if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
        {
            return false;
        }
        if (Double.doubleToLongBits(line) != Double.doubleToLongBits(other.line))
        {
            return false;
        }
        if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
        {
            return false;
        }
        if (Double.doubleToLongBits(pixel) != Double.doubleToLongBits(other.pixel))
        {
            return false;
        }
        return true;
    }

    /**
     * Get the latitude of the GCP.
     *
     * @return
     */
    public double getLat()
    {
        return latitude;
    }

    /**
     * Get the line(y image value) of the GCP.
     *
     * @return
     */
    public double getLine()
    {
        return line;
    }

    /**
     * Get the longitude of the GCP.
     *
     * @return
     */
    public double getLon()
    {
        return longitude;
    }

    /**
     * Get the pixel value(x image value).
     *
     * @return
     */
    public double getPixel()
    {
        return pixel;
    }

    /**
     * Set the latitude of the GCP
     *
     * @param lat
     */
    public void setLat(double lat)
    {
        latitude = lat;
    }

    /**
     * Set the line (y image value) of the GCP.
     *
     * @param line
     */
    public void setLine(double line)
    {
        this.line = line;
    }

    /**
     * Set the longitude of the GCP.
     *
     * @param lon
     */
    public void setLon(double lon)
    {
        longitude = lon;
    }

    /**
     * Set the pixel value(x image value).
     *
     * @param pixel - x   image value
     */
    public void setPixel(double pixel)
    {
        this.pixel = pixel;
    }

    @Override
    public String toString()
    {
        return "GCPBits [pixel=" + pixel + ", line=" + line + ", lon=" + longitude + ", lat=" + latitude + "]";
    }

}
