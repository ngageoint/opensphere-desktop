package io.opensphere.tracktool.model.persist.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.util.TimeSpanUtility;
import io.opensphere.tracktool.model.TrackNode;

/**
 * The Class JAXBTrackNode.
 */
@XmlRootElement(name = "TrackNode")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBTrackNode implements TrackNode
{
    /** The Alt m. */
    @XmlAttribute(name = "altM")
    private double myAltM;

    /** The Alt ref. */
    @XmlAttribute(name = "altRef")
    private Altitude.ReferenceLevel myAltRef;

    /** The Lat deg. */
    @XmlAttribute(name = "latDeg")
    private double myLatDeg;

    /** The Long deg. */
    @XmlAttribute(name = "lonDeg")
    private double myLongDeg;

    /** The Time end. */
    @XmlAttribute(name = "timeEnd")
    private long myTimeEnd;

    /** The Time start. */
    @XmlAttribute(name = "timeStart")
    private long myTimeStart;

    /**
     * Instantiates a new jAXB track node.
     */
    public JAXBTrackNode()
    {
    }

    /**
     * Instantiates a new jAXB track node.
     *
     * @param node the node
     */
    public JAXBTrackNode(TrackNode node)
    {
        LatLonAlt lla = node.getLocation();
        if (lla != null)
        {
            myLatDeg = lla.getLatD();
            myLongDeg = lla.getLonD();
            myAltM = lla.getAltM();
            myAltRef = lla.getAltitudeReference();
        }
        TimeSpan ts = node.getTime();
        myTimeStart = TimeSpanUtility.getWorkaroundStart(ts);
        myTimeEnd = TimeSpanUtility.getWorkaroundEnd(ts);

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
        JAXBTrackNode other = (JAXBTrackNode)obj;
        return myTimeEnd == other.myTimeEnd && myTimeStart == other.myTimeStart && myAltRef == other.myAltRef
                && Double.doubleToLongBits(myLatDeg) == Double.doubleToLongBits(other.myLatDeg)
                && Double.doubleToLongBits(myLongDeg) == Double.doubleToLongBits(other.myLongDeg)
                && Double.doubleToLongBits(myAltM) == Double.doubleToLongBits(other.myAltM);
    }

    @Override
    public LatLonAlt getLocation()
    {
        return LatLonAlt.createFromDegreesMeters(myLatDeg, myLongDeg, myAltM, myAltRef);
    }

    @Override
    public Vector2i getOffset()
    {
        return new Vector2i(10, 10);
    }

    @Override
    public TimeSpan getTime()
    {
        return TimeSpanUtility.fromStartEnd(myTimeStart, myTimeEnd);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(myAltM);
        result = prime * result + (int)(temp ^ temp >>> 32);
        result = prime * result + (myAltRef == null ? 0 : myAltRef.hashCode());
        temp = Double.doubleToLongBits(myLatDeg);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(myLongDeg);
        result = prime * result + (int)(temp ^ temp >>> 32);
        result = prime * result + (int)(myTimeEnd ^ myTimeEnd >>> 32);
        result = prime * result + (int)(myTimeStart ^ myTimeStart >>> 32);
        return result;
    }

    /**
     * Sets the equal to.
     *
     * @param node the new equal to
     */
    public void setEqualTo(TrackNode node)
    {
        LatLonAlt lla = node.getLocation();
        if (lla != null)
        {
            myLatDeg = lla.getLatD();
            myLongDeg = lla.getLonD();
            myAltM = lla.getAltM();
            myAltRef = lla.getAltitudeReference();
        }
        TimeSpan ts = node.getTime();
        myTimeStart = TimeSpanUtility.getWorkaroundStart(ts);
        myTimeEnd = TimeSpanUtility.getWorkaroundEnd(ts);
    }

    @Override
    public Long getCacheID()
    {
        return null;
    }
}
