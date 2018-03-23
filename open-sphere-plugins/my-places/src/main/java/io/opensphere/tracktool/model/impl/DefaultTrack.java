package io.opensphere.tracktool.model.impl;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import java.util.List;

import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.time.ExtentAccumulator;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.collections.New;
import io.opensphere.tracktool.model.Track;
import io.opensphere.tracktool.model.TrackNode;

/**
 * The Class DefaultTrack.
 */
public class DefaultTrack implements Track
{
    /**
     * Indicates if this track should be animatable.
     */
    private final boolean myAnimate;

    /**
     * The track color.
     */
    private Color myColor;

    /** The description. */
    private String myDescription;

    /**
     * The font for the bubble.
     */
    private Font myFont;

    /**
     * The id of the track.
     */
    private String myId;

    /**
     * Indicates if the bubble will be filled.
     */
    private boolean myIsFillBubble;

    /**
     * Indicates if bubbles are shown.
     */
    private boolean myIsShowBubble;

    /**
     * Indicates if the bubbles should show segment distances.
     */
    private boolean myIsShowDistance;

    /**
     * Indicates if the bubbles should show segment headings.
     */
    private boolean myIsShowHeading;

    /**
     * When true, indicates if the bubbles should show the velocity field's
     * value.
     */
    private boolean myShowVelocity;

    /**
     * When true, indicates if the bubbles should show the duration field's
     * value.
     */
    private boolean myShowDuration;

    /** The name which uniquely identifies the track. */
    private final String myName;

    /** The Track list. */
    private final List<TrackNode> myNodes = New.list();

    /**
     * The callout offset for the track head bubble.
     */
    private Vector2i myOffset;

    /** When true, show the description in the annotation bubble. */
    private boolean myShowDescription = true;

    /** When true, show the name in the annotation bubble. */
    private boolean myShowName = true;

    /**
     * When true, show the name of each field in the annotation bubble next to
     * the value.
     */
    private boolean myShowFieldTitles = true;

    /**
     * The text color.
     */
    private Color myTextColor;

    /**
     * The tracks timespan.
     */
    private final TimeSpan myTimeSpan;

    /**
     * The units used to express distance in the track.
     */
    private Class<? extends Length> myDistanceUnit;

    /**
     * The units used to express duration in the track.
     */
    private Class<? extends Duration> myDurationUnit;

    /**
     * Constructor.
     *
     * @param id The id of the track.
     * @param name The name which uniquely identifies the track.
     * @param nodes The nodes which make up the track.
     * @param isAnimate Indicates if this track should be animatable.
     */
    public DefaultTrack(String id, String name, Collection<TrackNode> nodes, boolean isAnimate)
    {
        myId = id;
        myName = name;
        myNodes.addAll(nodes);
        myTimeSpan = figureOutSpan(nodes);
        myAnimate = isAnimate;
    }

    /**
     * Constructor.
     *
     * @param copy the track to copy data from.
     */
    public DefaultTrack(Track copy)
    {
        myName = copy.getName();
        myDescription = copy.getDescription();
        myShowDescription = copy.isShowDescription();
        myShowName = copy.isShowName();
        myShowFieldTitles = copy.isShowFieldTitles();
        for (TrackNode copyNode : copy.getNodes())
        {
            DefaultTrackNode node = new DefaultTrackNode(copyNode);
            myNodes.add(node);
        }

        myColor = copy.getColor();
        myFont = copy.getFont();
        myIsFillBubble = copy.isFillBubble();
        myTextColor = copy.getTextColor();
        myIsShowBubble = copy.isShowBubble();
        myDistanceUnit = copy.getDistanceUnit();
        myDurationUnit = copy.getDurationUnit();
        myIsShowDistance = copy.isShowDistance();
        myIsShowHeading = copy.isShowHeading();
        myShowVelocity = copy.isShowVelocity();
        myShowDuration = copy.isShowDuration();
        myTimeSpan = copy.getTimeSpan() == null ? figureOutSpan(myNodes) : copy.getTimeSpan();
        myAnimate = copy.isAnimate();
    }

    @Override
    public Color getColor()
    {
        return myColor;
    }

    @Override
    public String getDescription()
    {
        return myDescription;
    }

    @Override
    public Font getFont()
    {
        return myFont;
    }

    @Override
    public String getId()
    {
        return myId;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    @Override
    public List<? extends TrackNode> getNodes()
    {
        return New.list(myNodes);
    }

    @Override
    public Vector2i getOffset()
    {
        return myOffset;
    }

    @Override
    public Color getTextColor()
    {
        return myTextColor;
    }

    @Override
    public TimeSpan getTimeSpan()
    {
        return myTimeSpan;
    }

    @Override
    public Class<? extends Length> getDistanceUnit()
    {
        return myDistanceUnit;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.tracktool.model.Track#getDurationUnit()
     */
    @Override
    public Class<? extends Duration> getDurationUnit()
    {
        return myDurationUnit;
    }

    @Override
    public boolean isAnimate()
    {
        return myAnimate;
    }

    @Override
    public boolean isFillBubble()
    {
        return myIsFillBubble;
    }

    @Override
    public boolean isShowBubble()
    {
        return myIsShowBubble;
    }

    @Override
    public boolean isShowDescription()
    {
        return myShowDescription;
    }

    @Override
    public boolean isShowDistance()
    {
        return myIsShowDistance;
    }

    @Override
    public boolean isShowHeading()
    {
        return myIsShowHeading;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.tracktool.model.Track#isShowVelocity()
     */
    @Override
    public boolean isShowVelocity()
    {
        return myShowVelocity;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.tracktool.model.Track#isShowDuration()
     */
    @Override
    public boolean isShowDuration()
    {
        return myShowDuration;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.tracktool.model.Track#isShowName()
     */
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
        return myShowFieldTitles;
    }

    /**
     * Sets the color of the track.
     *
     * @param color The color of the track.
     */
    public void setColor(Color color)
    {
        myColor = color;
    }

    /**
     * Set the description.
     *
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        myDescription = description;
    }

    /**
     * Sets the font.
     *
     * @param font The font to use for the bubble.
     */
    public void setFont(Font font)
    {
        myFont = font;
    }

    /**
     * Sets the isFillBubble.
     *
     * @param isFillBubble True if the bubble should be filled, false otherwise.
     */
    public void setIsFillBubble(boolean isFillBubble)
    {
        myIsFillBubble = isFillBubble;
    }

    /**
     * Sets if the bubbles are shown or not.
     *
     * @param isShowBubble True if bubbles are shown, false otherwise.
     */
    public void setIsShowBubble(boolean isShowBubble)
    {
        myIsShowBubble = isShowBubble;
    }

    /**
     * Sets if the distance should be shown in the track bubbles.
     *
     * @param isShowDistance True if distance should show in bubbles, false
     *            otherwise.
     */
    public void setIsShowDistance(boolean isShowDistance)
    {
        myIsShowDistance = isShowDistance;
    }

    /**
     * Sets if the heading should be shown in the track bubbles.
     *
     * @param isShowHeading True if heading should show in bubbles, false
     *            otherwise.
     */
    public void setIsShowHeading(boolean isShowHeading)
    {
        myIsShowHeading = isShowHeading;
    }

    /**
     * Sets the value of the {@link #myShowVelocity} field.
     *
     * @param showVelocity the value to store in the {@link #myShowVelocity}
     *            field.
     */
    public void setShowVelocity(boolean showVelocity)
    {
        myShowVelocity = showVelocity;
    }

    /**
     * Sets the value of the {@link #myShowDuration} field.
     *
     * @param showDuration the value to store in the {@link #myShowDuration}
     *            field.
     */
    public void setShowDuration(boolean showDuration)
    {
        myShowDuration = showDuration;
    }

    /**
     * Sets the trackhead bubble offset.
     *
     * @param offset The offset.
     */
    public void setOffset(Vector2i offset)
    {
        myOffset = offset;
    }

    /**
     * Set the showDescription.
     *
     * @param showDescription the showDescription to set
     */
    public void setShowDescription(boolean showDescription)
    {
        myShowDescription = showDescription;
    }

    /**
     * Set the showName.
     *
     * @param showName the showName to set
     */
    public void setShowName(boolean showName)
    {
        myShowName = showName;
    }

    /**
     * Sets the value of the {@link #myShowFieldTitles} field.
     *
     * @param showFieldTitles the value to store in the
     *            {@link #myShowFieldTitles} field.
     */
    public void setShowFieldTitles(boolean showFieldTitles)
    {
        myShowFieldTitles = showFieldTitles;
    }

    /**
     * Sets the text color of the bubble.
     *
     * @param textColor The color of the bubble text.
     */
    public void setTextColor(Color textColor)
    {
        myTextColor = textColor;
    }

    /**
     * Sets the units used to express distance for length display in the track
     * bubble.
     *
     * @param distanceUnit The units used to display the track length in the
     *            track bubble.
     */
    public void setDistanceUnit(Class<? extends Length> distanceUnit)
    {
        myDistanceUnit = distanceUnit;
    }

    /**
     * Sets the units used to express duration for length display in the track
     * bubble.
     *
     * @param durationUnit The units used to display the duration in the track
     *            bubble.
     */
    public void setDurationUnit(Class<? extends Duration> durationUnit)
    {
        myDurationUnit = durationUnit;
    }

    /**
     * Calculates the time span of the track based on the nodes.
     *
     * @param nodes the track nodes
     * @return The overall timespan of the track.
     */
    private static TimeSpan figureOutSpan(Collection<? extends TrackNode> nodes)
    {
        ExtentAccumulator accumulator = new ExtentAccumulator();
        for (TrackNode node : nodes)
        {
            accumulator.add(node.getTime());
        }
        TimeSpan extent = accumulator.getExtent();
        // Calling code expects null in these cases
        if (extent.isZero() || extent.isTimeless())
        {
            extent = null;
        }
        return extent;
    }
}
