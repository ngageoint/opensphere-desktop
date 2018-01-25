package io.opensphere.wms.config.v1;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;

/**
 * Bounding box for a layer. Bounds are in decimal degrees.
 */
@XmlRootElement(name = "LayerConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class WMSBoundingBoxConfig implements Cloneable, Serializable
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** Maximum Latitude. */
    @XmlElement(name = "MaximumLatitude")
    private double myMaximumLatitude;

    /** Maximum Longitude. */
    @XmlElement(name = "MaximumLongitude")
    private double myMaximumLongitude;

    /** Minimum Latitude. */
    @XmlElement(name = "MinimumLatitude")
    private double myMinimumLatitude;

    /** Minimum Longitude. */
    @XmlElement(name = "MinimumLongitude")
    private double myMinimumLongitude;

    @Override
    public WMSBoundingBoxConfig clone() throws CloneNotSupportedException
    {
        return (WMSBoundingBoxConfig)super.clone();
    }

    /**
     * Convert me to a GeographicBoundingBox.
     *
     * @return This bounding box represented as a GeographicBoudningBox
     */
    public GeographicBoundingBox getGeographicBoundingBox()
    {
        LatLonAlt lowerLeftCorner = LatLonAlt.createFromDegrees(myMinimumLatitude, myMinimumLongitude);
        LatLonAlt upperRightCorner = LatLonAlt.createFromDegrees(myMaximumLatitude, myMaximumLongitude);
        return new GeographicBoundingBox(lowerLeftCorner, upperRightCorner);
    }

    /**
     * Get the maximumLatitude.
     *
     * @return the maximumLatitude
     */
    public double getMaximumLatitude()
    {
        return myMaximumLatitude;
    }

    /**
     * Get the maximumLongitude.
     *
     * @return the maximumLongitude
     */
    public double getMaximumLongitude()
    {
        return myMaximumLongitude;
    }

    /**
     * Get the minimumLatitude.
     *
     * @return the minimumLatitude
     */
    public double getMinimumLatitude()
    {
        return myMinimumLatitude;
    }

    /**
     * Get the minimumLongitude.
     *
     * @return the minimumLongitude
     */
    public double getMinimumLongitude()
    {
        return myMinimumLongitude;
    }

    /**
     * Set me from a GeographicBoundingBox.
     *
     * @param bbox The bounding box.
     */
    public void setGeographicBoundingBox(GeographicBoundingBox bbox)
    {
        myMinimumLatitude = bbox.getLowerLeft().getLatLonAlt().getLatD();
        myMinimumLongitude = bbox.getLowerLeft().getLatLonAlt().getLonD();
        myMaximumLatitude = bbox.getUpperRight().getLatLonAlt().getLatD();
        myMaximumLongitude = bbox.getUpperRight().getLatLonAlt().getLonD();
    }

    /**
     * Set the maximumLatitude.
     *
     * @param maximumLatitude the maximumLatitude to set
     */
    public void setMaximumLatitude(double maximumLatitude)
    {
        myMaximumLatitude = maximumLatitude;
    }

    /**
     * Set the maximumLongitude.
     *
     * @param maximumLongitude the maximumLongitude to set
     */
    public void setMaximumLongitude(double maximumLongitude)
    {
        myMaximumLongitude = maximumLongitude;
    }

    /**
     * Set the minimumLatitude.
     *
     * @param minimumLatitude the minimumLatitude to set
     */
    public void setMinimumLatitude(double minimumLatitude)
    {
        myMinimumLatitude = minimumLatitude;
    }

    /**
     * Set the minimumLongitude.
     *
     * @param minimumLongitude the minimumLongitude to set
     */
    public void setMinimumLongitude(double minimumLongitude)
    {
        myMinimumLongitude = minimumLongitude;
    }
}
