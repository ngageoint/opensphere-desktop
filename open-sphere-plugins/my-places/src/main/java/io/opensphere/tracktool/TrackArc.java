package io.opensphere.tracktool;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.callout.Callout;
import io.opensphere.core.callout.CalloutImpl;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.ExtentAccumulator;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.util.collections.New;
import io.opensphere.tracktool.model.Track;
import io.opensphere.tracktool.model.TrackNode;

/**
 * Representation of polyline drawn along the surface of the globe. Each line
 * segment has a label which shows relevant information about the segment and
 * for tracks which have been completed, a label is shown for information which
 * relates to all segments.
 */
public class TrackArc
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TrackArc.class);

    /**
     * The precision.
     */
    private static final double PRECISION = 1000d;

    /** When true, the label tiles and bubbles have been published. */
    private boolean myLabelsOn;

    /** The track which I wrap. */
    private final Track myTrack;

    /** The distance units used for the information bubbles. */
    private final Class<? extends Length> myDistanceUnits;

    /** The duration units used for information bubbles. */
    private final Class<? extends Duration> myDurationUnits;

    /**
     * A flag used to keep track of the warning state if one or more points
     * contain timeless data (prevents multiple warnings from being issued for a
     * single track.)
     */
    private transient boolean myTimelessWarningEmitted = false;

    /**
     * Constructor.
     *
     * @param track The track which I wrap.
     * @param distanceUnits The units used for the segment labels.
     * @param durationUnits the units of duration used for the segment labels.
     * @param labelsOn True when the labels are published to be published
     *            immediately following creation.
     */
    public TrackArc(Track track, Class<? extends Length> distanceUnits, Class<? extends Duration> durationUnits, boolean labelsOn)
    {
        myDistanceUnits = distanceUnits;
        myDurationUnits = durationUnits;
        myTrack = track;
        myLabelsOn = labelsOn;
    }

    /**
     * Create the callouts for this track.
     *
     * @param distanceUnits The length units to use for the callouts.
     * @param durationUnits The units in which durations are displayed for the
     *            callouts.
     * @return The callouts.
     */
    public Collection<? extends Callout> createCallouts(Class<? extends Length> distanceUnits,
            Class<? extends Duration> durationUnits)
    {
        Collection<Callout> callouts = New.collection();

        if (myTrack.isShowBubble())
        {
            TrackNode startNode = null;
            TrackNode endNode;
            GeographicPosition start = null;
            GeographicPosition end;

            for (int i = 0; i < myTrack.getNodes().size(); ++i)
            {
                endNode = myTrack.getNodes().get(i);
                end = new GeographicPosition(myTrack.getNodes().get(i).getLocation());

                if (start != null)
                {
                    List<String> labelText = createLabelText(startNode, endNode, start, end, distanceUnits, durationUnits);
                    if (!labelText.isEmpty())
                    {
                        LatLonAlt att = GeographicBody3D.greatCircleInterpolate(start.getLatLonAlt(), end.getLatLonAlt(), .5);
                        TrackNode node = myTrack.getNodes().get(i - 1);
                        Callout callout = createCallout(labelText, att, node.getOffset());
                        if (myTrack.isAnimate())
                        {
                            callout.setTime(node.getTime());
                        }
                        callouts.add(callout);
                    }
                }
                start = end;
                startNode = endNode;
            }

            List<GeographicPosition> vertices = getVertices();
            List<String> labelText = createSumLabelText(vertices, distanceUnits, durationUnits);
            if (!labelText.isEmpty())
            {
                LatLonAlt att = vertices.get(vertices.size() - 1).getLatLonAlt();

                Callout callout = createCallout(labelText, att, myTrack.getOffset());
                callout.setBorderWidth(2f);
                callout.setBorderColor(Color.YELLOW);
                if (myTrack.isAnimate())
                {
                    callout.setTime(myTrack.getTimeSpan());
                }
                callouts.add(callout);
            }
        }

        return callouts;
    }

    /**
     * Get the track.
     *
     * @return the track
     */
    public Track getTrack()
    {
        return myTrack;
    }

    /**
     * Get the units.
     *
     * @return the units
     */
    public Class<? extends Length> getDistanceUnits()
    {
        return myDistanceUnits;
    }

    /**
     * Gets the value of the {@link #myDurationUnits} field.
     *
     * @return the value stored in the {@link #myDurationUnits} field.
     */
    public Class<? extends Duration> getDurationUnits()
    {
        return myDurationUnits;
    }

    /**
     * Get the labelsOn.
     *
     * @return the labelsOn
     */
    public boolean isLabelsOn()
    {
        return myLabelsOn;
    }

    /**
     * Set the labelsOn.
     *
     * @param labelsOn the labelsOn to set
     */
    public void setLabelsOn(boolean labelsOn)
    {
        myLabelsOn = labelsOn;
    }

    /**
     * Create a callout.
     *
     * @param labelText The text for the callout.
     * @param att The attachment point for the callout.
     * @param offset The offset for the callout.
     * @return The callout.
     */
    protected Callout createCallout(List<String> labelText, LatLonAlt att, Vector2i offset)
    {
        Callout callout = new CalloutImpl(0L, labelText, att, myTrack.getFont());
        callout.setBorderColor(myTrack.getColor());
        callout.setTextColor(myTrack.getTextColor());
        callout.setCornerRadius(10);

        if (offset != null)
        {
            callout.setAnchorOffset(offset);
        }

        if (myTrack.isFillBubble())
        {
            callout.setBackgroundColor(myTrack.getColor());
        }
        return callout;
    }

    /**
     * Create the text for the label of a single segment.
     *
     * @param startNode the start node of the track segment.
     * @param endNode the end node of the track segment.
     * @param startPosition The start of the segment.
     * @param endPosition The end of the segment.
     * @param distanceUnits The length units to use in the display.
     * @param durationUnits the duration units to use in the display.
     * @return The label text.
     */
    private List<String> createLabelText(TrackNode startNode, TrackNode endNode, GeographicPosition startPosition,
            GeographicPosition endPosition, Class<? extends Length> distanceUnits, Class<? extends Duration> durationUnits)
    {
        List<String> lines = New.list(4);

        if (myTrack.isShowHeading())
        {
            double bearing = GeographicBody3D.greatCircleAzimuthD(startPosition.getLatLonAlt(), endPosition.getLatLonAlt());
            bearing = bearing < 0. ? 360. + bearing : bearing;
            String bearingStr = String.format("%.3f\u00B0", Double.valueOf(bearing));
            String fieldName = "";
            if (myTrack.isShowFieldTitles())
            {
                fieldName = "Heading: ";
            }
            lines.add(fieldName + bearingStr);
        }

        double meters = GeographicBody3D.greatCircleDistanceM(startPosition.getLatLonAlt(), endPosition.getLatLonAlt(),
                WGS84EarthConstants.RADIUS_MEAN_M);
        Length length;
        try
        {
            length = Length.create(distanceUnits, new Meters(meters));
        }
        catch (InvalidUnitsException e)
        {
            LOGGER.warn("Could not use length type: " + e, e);
            length = new Meters(meters);
        }

        if (myTrack.isShowDistance())
        {
            String lengthStr = String.format("%.3f " + length.getShortLabel(true), length.getDisplayMagnitudeObj());
            String fieldName = "";
            if (myTrack.isShowFieldTitles())
            {
                fieldName = "Segment Distance: ";
            }
            lines.add(fieldName + lengthStr);
        }

        TimeSpan startTime = startNode.getTime();
        TimeSpan endTime = endNode.getTime();

        if (!startTime.isTimeless() && !endTime.isTimeless())
        {
            ExtentAccumulator accum = new ExtentAccumulator();
            accum.add(startTime);
            accum.add(endTime);
            TimeSpan segmentSpan = accum.getExtent();
            Duration segmentDuration = Duration.create(durationUnits, segmentSpan.getDuration());
            if (myTrack.isShowDuration())
            {
                String fieldName = "";
                if (myTrack.isShowFieldTitles())
                {
                    fieldName = "Segment Duration: ";
                }
                String durationStr = String.format("%.3f " + segmentDuration.getShortLabel(true),
                        segmentDuration.getMagnitude().doubleValue());
                lines.add(fieldName + durationStr);
            }

            if (myTrack.isShowVelocity())
            {
                double velocity = Double.valueOf(
                        Math.round(length.getDisplayMagnitude() / segmentDuration.getMagnitude().doubleValue() * PRECISION)
                                / PRECISION);
                String fieldName = "";
                if (myTrack.isShowFieldTitles())
                {
                    fieldName = "Segment Velocity: ";
                }
                String velocityStr = String
                        .format("%.3f " + length.getShortLabel(true) + "/" + segmentDuration.getShortLabel(false), velocity);
                lines.add(fieldName + velocityStr);
            }
        }
        else
        {
            if (!myTimelessWarningEmitted && (myTrack.isShowDuration() || myTrack.isShowVelocity()))
            {
                myTimelessWarningEmitted = true;
                Notify.warn(
                        "Unable to calculate duration and / or velocity for one or more segments for track " + myTrack.getName()
                                + ". This is caused by either a point which has no time value, or generation of a track "
                                + "by clicking on the map instead of a point");
            }
        }

        return lines;
    }

    /**
     * Create the text for the sum label.
     *
     * @param positions The positions which make up the track.
     * @param distanceUnits The length units to be used.
     * @param durationUnits The duration units used in generating the displayed
     *            values.
     * @return The label text.
     */
    private List<String> createSumLabelText(List<GeographicPosition> positions, Class<? extends Length> distanceUnits,
            Class<? extends Duration> durationUnits)
    {
        List<String> lines = New.list(3);

        String name = myTrack.isShowName() ? myTrack.getName() : null;
        String description = myTrack.isShowDescription() ? myTrack.getDescription() : null;
        if (name != null)
        {
            String fieldName = "";
            if (myTrack.isShowFieldTitles())
            {
                fieldName = "Title: ";
            }
            lines.add(fieldName + name);
        }

        if (description != null)
        {
            String fieldName = "";
            if (myTrack.isShowFieldTitles())
            {
                fieldName = "Description: ";
            }
            lines.add(fieldName + description);
        }

        double meters = 0;
        GeographicPosition start = positions.get(0);
        for (int i = 1; i < positions.size(); ++i)
        {
            GeographicPosition end = positions.get(i);
            meters += GeographicBody3D.greatCircleDistanceM(start.getLatLonAlt(), end.getLatLonAlt(),
                    WGS84EarthConstants.RADIUS_EQUATORIAL_M);
            start = end;
        }

        Length length;
        try
        {
            length = Length.create(distanceUnits, new Meters(meters));
        }
        catch (InvalidUnitsException e)
        {
            LOGGER.warn("Could not use length type: " + e, e);
            length = new Meters(meters);
        }

        if (myTrack.isShowDistance())
        {
            String lengthStr = String.format("%.3f " + length.getShortLabel(true), length.getDisplayMagnitudeObj());

            String fieldName = "";
            if (myTrack.isShowFieldTitles())
            {
                fieldName = "Total Distance: ";
            }
            lines.add(fieldName + lengthStr);
        }

        ExtentAccumulator accum = new ExtentAccumulator();
        for (TrackNode node : myTrack.getNodes())
        {
            accum.add(node.getTime());
        }

        TimeSpan totalTimeSpan = accum.getExtent();

        if (!totalTimeSpan.isTimeless())
        {
            Duration totalDuration = Duration.create(durationUnits, totalTimeSpan.getDuration());
            if (myTrack.isShowDuration())
            {
                String fieldName = "";
                if (myTrack.isShowFieldTitles())
                {
                    fieldName = "Total Duration: ";
                }
                String durationStr = String.format("%.3f " + totalDuration.getShortLabel(true),
                        totalDuration.getMagnitude().doubleValue());
                lines.add(fieldName + durationStr);
            }

            if (myTrack.isShowVelocity())
            {
                double duration = totalDuration.getMagnitude().doubleValue();
                double velocity = Double.valueOf(Math.round(length.getDisplayMagnitude() / duration * PRECISION) / PRECISION);
                String fieldName = "";
                if (myTrack.isShowFieldTitles())
                {
                    fieldName = "Average Velocity: ";
                }
                String velocityStr = String
                        .format("%.3f " + length.getShortLabel(true) + "/" + totalDuration.getShortLabel(false), velocity);
                lines.add(fieldName + velocityStr);
            }
        }
        else
        {
            if (!myTimelessWarningEmitted)
            {
                myTimelessWarningEmitted = true;
                Notify.warn(
                        "Unable to calculate duration and / or velocity for one or more segments for track " + myTrack.getName()
                                + ". This is caused by either a point which has no time value, or generation of a track "
                                + "by clicking on the map instead of a point");
            }
        }

        return lines;
    }

    /**
     * Get the vertices which define the segments of this track.
     *
     * @return the vertices which define the segments of this track.
     */
    private List<GeographicPosition> getVertices()
    {
        List<GeographicPosition> vertices = New.list(myTrack.getNodes().size());
        for (TrackNode node : myTrack.getNodes())
        {
            vertices.add(new GeographicPosition(node.getLocation()));
        }
        return vertices;
    }
}
