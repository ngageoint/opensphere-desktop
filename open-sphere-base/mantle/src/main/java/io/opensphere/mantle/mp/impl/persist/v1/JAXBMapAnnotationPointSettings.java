package io.opensphere.mantle.mp.impl.persist.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.mantle.mp.MapAnnotationPointSettings;

/**
 * The Class JAXBMapAnnotationPointSettings.
 */
@XmlRootElement(name = "MapAnnotationPointSettings")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBMapAnnotationPointSettings implements MapAnnotationPointSettings
{
    /** Boolean for annotation hide. */
    @XmlAttribute(name = "annoHideOn")
    private boolean myAnnoHideOn;

    /** Boolean for description. */
    @XmlAttribute(name = "descOn")
    private boolean myDescOn;

    /** Boolean for degrees minutes seconds lat/lon. */
    @XmlAttribute(name = "dmsOn")
    private boolean myDMSOn;

    /** Boolean for color. */
    @XmlAttribute(name = "dotOn")
    private boolean myDotOn;

    /** Boolean for latitude/longitude. */
    @XmlAttribute(name = "latLonOn")
    private boolean myLatLonOn;

    /** Boolean for MGRS. */
    @XmlAttribute(name = "mgrsOn")
    private boolean myMGRSOn;

    /** Boolean for altitude. */
    @XmlAttribute(name = "altitudeOn")
    private boolean myAltitudeOn;

    /** Boolean for title. */
    @XmlAttribute(name = "titleOn")
    private boolean myTitleOn;

    /** Boolean for fieldTitle. */
    @XmlAttribute(name = "fieldTitleOn")
    private boolean myFieldTitleOn;

    /** Boolean for duration. */
    @XmlAttribute(name = "durationOn")
    private boolean myDurationOn;

    /** Boolean for distance. */
    @XmlAttribute(name = "distanceOn")
    private boolean myDistanceOn;

    /** Boolean for velocity. */
    @XmlAttribute(name = "velocityOn")
    private boolean myVelocityOn;

    /** Boolean for heading. */
    @XmlAttribute(name = "headingOn")
    private boolean myHeadingOn;

    /**
     * Default constructor.
     */
    public JAXBMapAnnotationPointSettings()
    {
    }

    /**
     * Copy constructor.
     *
     * @param other What to copy from.
     */
    public JAXBMapAnnotationPointSettings(MapAnnotationPointSettings other)
    {
        myTitleOn = other.isTitle();
        myDescOn = other.isDesc();
        myLatLonOn = other.isLatLon();
        myDMSOn = other.isDms();
        myMGRSOn = other.isMgrs();
        myAltitudeOn = other.isAltitude();
        myDotOn = other.isDotOn();
        myAnnoHideOn = other.isAnnohide();
        myFieldTitleOn = other.isFieldTitle();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        JAXBMapAnnotationPointSettings other = (JAXBMapAnnotationPointSettings)obj;
        //@formatter:off
        return myAnnoHideOn == other.myAnnoHideOn
                && myDescOn == other.myDescOn
                && myDMSOn == other.myDMSOn
                && myDotOn == other.myDotOn
                && myLatLonOn == other.myLatLonOn
                && myMGRSOn == other.myMGRSOn
                && myAltitudeOn == other.myAltitudeOn
                && myTitleOn == other.myTitleOn
                && myFieldTitleOn == other.myFieldTitleOn;
        //@formatter:on
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myAnnoHideOn);
        result = prime * result + HashCodeHelper.getHashCode(myDescOn);
        result = prime * result + HashCodeHelper.getHashCode(myDMSOn);
        result = prime * result + HashCodeHelper.getHashCode(myDotOn);
        result = prime * result + HashCodeHelper.getHashCode(myLatLonOn);
        result = prime * result + HashCodeHelper.getHashCode(myMGRSOn);
        result = prime * result + HashCodeHelper.getHashCode(myAltitudeOn);
        result = prime * result + HashCodeHelper.getHashCode(myTitleOn);
        result = prime * result + HashCodeHelper.getHashCode(myFieldTitleOn);
        return result;
    }

    @Override
    public boolean isAnnohide()
    {
        return myAnnoHideOn;
    }

    @Override
    public boolean isDesc()
    {
        return myDescOn;
    }

    @Override
    public boolean isDms()
    {
        return myDMSOn;
    }

    @Override
    public boolean isDotOn()
    {
        return myDotOn;
    }

    @Override
    public boolean isLatLon()
    {
        return myLatLonOn;
    }

    @Override
    public boolean isMgrs()
    {
        return myMGRSOn;
    }

    @Override
    public boolean isAltitude()
    {
        return myAltitudeOn;
    }

    @Override
    public boolean isTitle()
    {
        return myTitleOn;
    }

    @Override
    public boolean isFieldTitle()
    {
        return myFieldTitleOn;
    }

    @Override
    public boolean isDistance()
    {
        return myDistanceOn;
    }

    @Override
    public boolean isDuration()
    {
        return myDurationOn;
    }

    @Override
    public boolean isHeading()
    {
        return myHeadingOn;
    }

    @Override
    public boolean isVelocity()
    {
        return myVelocityOn;
    }
}
