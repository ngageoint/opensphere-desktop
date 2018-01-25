package io.opensphere.tracktool.model.persist.v1;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.myplaces.util.PlacemarkUtils;
import io.opensphere.tracktool.model.Track;
import io.opensphere.tracktool.model.TrackNode;

/**
 * The Class JAXBTrack.
 */
@XmlRootElement(name = "Track")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBTrack implements Track
{
    /** The description. */
    @XmlAttribute(name = "description")
    private String myDescription;

    /** The name. */
    @XmlAttribute(name = "name")
    private String myName;

    /** The Node list. */
    @XmlElement(name = "TrackNode")
    private List<JAXBTrackNode> myNodeList;

    /** When true, show the description in the annotation bubble. */
    @XmlAttribute(name = "showDescription")
    private boolean myShowDescription;

    /** When true, show the name in the annotation bubble. */
    @XmlAttribute(name = "showName")
    private boolean myShowName;

    /**
     * Instantiates a new jAXB track.
     */
    public JAXBTrack()
    {
    }

    /**
     * Instantiates a new jAXB track.
     *
     * @param track the track
     */
    public JAXBTrack(Track track)
    {
        List<? extends TrackNode> nodeList = track.getNodes();
        myNodeList = nodeList == null || nodeList.isEmpty() ? New.<JAXBTrackNode>list()
                : New.<JAXBTrackNode>list(nodeList.size());
        if (nodeList != null)
        {
            for (TrackNode node : nodeList)
            {
                myNodeList.add(new JAXBTrackNode(node));
            }
        }
        myName = track.getName();
        myDescription = track.getDescription();
        myShowDescription = track.isShowDescription();
        myShowName = track.isShowName();
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
        JAXBTrack other = (JAXBTrack)obj;
        return EqualsHelper.equals(myName, other.myName, myNodeList, other.myNodeList);
    }

    @Override
    public Color getColor()
    {
        return Color.orange;
    }

    @Override
    public String getDescription()
    {
        return myDescription;
    }

    @Override
    public Font getFont()
    {
        return PlacemarkUtils.DEFAULT_FONT;
    }

    @Override
    public String getId()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    @Override
    public List<? extends TrackNode> getNodes()
    {
        return myNodeList;
    }

    @Override
    public Vector2i getOffset()
    {
        return new Vector2i(10, 10);
    }

    @Override
    public Color getTextColor()
    {
        return Color.white;
    }

    @Override
    public TimeSpan getTimeSpan()
    {
        return null;
    }

    @Override
    public Class<? extends Length> getDistanceUnit()
    {
        return Meters.class;
    }

    @Override
    public Class<? extends Duration> getDurationUnit()
    {
        return Seconds.class;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myName == null ? 0 : myName.hashCode());
        result = prime * result + (myNodeList == null ? 0 : myNodeList.hashCode());
        return result;
    }

    @Override
    public boolean isAnimate()
    {
        return false;
    }

    @Override
    public boolean isFillBubble()
    {
        return false;
    }

    @Override
    public boolean isShowBubble()
    {
        return true;
    }

    @Override
    public boolean isShowDescription()
    {
        return myShowDescription;
    }

    @Override
    public boolean isShowDistance()
    {
        return true;
    }

    @Override
    public boolean isShowHeading()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.tracktool.model.Track#isShowVelocity()
     */
    @Override
    public boolean isShowVelocity()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.tracktool.model.Track#isShowDuration()
     */
    @Override
    public boolean isShowDuration()
    {
        return true;
    }

    @Override
    public boolean isShowName()
    {
        return myShowName;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.tracktool.model.Track#isShowFieldTitles()
     */
    @Override
    public boolean isShowFieldTitles()
    {
        return false;
    }
}
