package io.opensphere.tracktool.model;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.length.Length;

/**
 * The Interface Track.
 */
public interface Track
{
    /**
     * Gets the color of the track.
     *
     * @return The color of the track.
     */
    Color getColor();

    /**
     * Get the description.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Gets the font for the track bubble.
     *
     * @return The bubble text font.
     */
    Font getFont();

    /**
     * Gets the id of the track.
     *
     * @return The id.
     */
    String getId();

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the node list.
     *
     * @return the node list
     */
    List<? extends TrackNode> getNodes();

    /**
     * Gets the callout offset for the track head bubble.
     *
     * @return The trackhead bubble offset.
     */
    Vector2i getOffset();

    /**
     * Gets the text color.
     *
     * @return The text color.
     */
    Color getTextColor();

    /**
     * Gets the overall timespan of the track.
     *
     * @return The track's timespan.
     */
    TimeSpan getTimeSpan();

    /**
     * Gets the track's distance units.
     *
     * @return The track's distance units.
     */
    Class<? extends Length> getDistanceUnit();

    /**
     * Gets the track's duration units.
     *
     * @return the track's duration units.
     */
    Class<? extends Duration> getDurationUnit();

    /**
     * Indicates if this track is animatable.
     *
     * @return True if the track can be animated, false otherwise.
     */
    boolean isAnimate();

    /**
     * Indicates if the bubble should be filled, false if hollow.
     *
     * @return True if the bubble's background should be the same color as the
     *         track, false if the bubble should be hollow.
     */
    boolean isFillBubble();

    /**
     * Indicates if the bubbles should be shown.
     *
     * @return True if the bubbles will be shown, false otherwise.
     */
    boolean isShowBubble();

    /**
     * Get the showDescription.
     *
     * @return the showDescription
     */
    boolean isShowDescription();

    /**
     * Indicates if the track bubbles should show the segment distances.
     *
     * @return True if distance should show in bubbles, false otherwise.
     */
    boolean isShowDistance();

    /**
     * Indicates if the track bubbles should show the segment heading.
     *
     * @return True if the heading should show in bubbles, false otherwise.
     */
    boolean isShowHeading();

    /**
     * Tests to determine if the track bubbles should show the velocity field.
     *
     * @return <code>true</code> if the velocity should show in bubbles, <code>false</code> otherwise.
     */
    boolean isShowVelocity();

    /**
     * Tests to determine if the track bubbles should show the duration field.
     *
     * @return <code>true</code> if the duration should be shown in bubbles, <code>false</code> otherwise.
     */
    boolean isShowDuration();

    /**
     * Get the showName.
     *
     * @return the showName
     */
    boolean isShowName();

    /**
     * Tests to determine if the titles of each field should be displayed alongside the value of the field in the bubble.
     *
     * @return <code>true</code> if the title of each field should be displayed, <code>false</code> otherwise.
     */
    boolean isShowFieldTitles();

    /**
     * The Class CompareByNameIgnoreCase.
     */
    class CompareByNameIgnoreCase implements Comparator<Track>, Serializable
    {
        /** Serial. */
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(Track o1, Track o2)
        {
            String track1 = o1.getName();
            String track2 = o2.getName();

            if (track1 == null)
            {
                track1 = "";
            }

            if (track2 == null)
            {
                track2 = "";
            }

            return track1.toLowerCase().compareTo(track2.toLowerCase());
        }
    }
}
