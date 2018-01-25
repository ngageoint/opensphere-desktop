package io.opensphere.overlay.arc;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.callout.Callout;
import io.opensphere.core.callout.CalloutImpl;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/**
 * Representation of polyline drawn along the surface of the globe. Each line
 * segment has a label which shows relevant information about the segment and
 * for arcs which have been completed, a label is shown for information which
 * relates to all segments.
 */
public class Arc
{
    /** Bubble background color. */
    private static final Color BACKGROUND_COLOR = new Color(128, 128, 128, 200);

    // TODO once the color, font, etc. can be configured, these should be
    // removed.
    /** Foreground color of the text for the label. */
    private static final Color LABEL_COLOR = Color.WHITE;

    /** Font to use for labels. */
    private static final Font LABEL_FONT = Font.decode(Font.SANS_SERIF + " PLAIN 11");

    /** The line color for the bubbles. */
    private static final Color LINE_COLOR = Color.WHITE;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(Arc.class);

    /**
     * When true, the units for this arc have been manually set to something
     * other than the application's preferred settings.
     */
    private boolean myHasCustomUnits;

    /** The polyline which is the renderable arc line. */
    private final PolylineGeometry myLine;

    /** The units used for the information bubbles. */
    private final Class<? extends Length> myUnits;

    /**
     * The vertices that make up the track along with the associated geometry if
     * any.
     */
    private final List<Pair<GeographicPosition, AbstractRenderableGeometry>> myVertices = New.list();

    /**
     * Create the an arc to be displayed.
     *
     * @param positions Geographic positions of the points of the polyline.
     * @param pickable true if the line should be pickable.
     * @return The newly create polyline.
     */
    protected static PolylineGeometry createPolyline(List<GeographicPosition> positions, boolean pickable)
    {
        PolylineGeometry.Builder<GeographicPosition> lineBuilder = new PolylineGeometry.Builder<GeographicPosition>();
        // TODO It would be nice for the line type to be selectable by the user.
        lineBuilder.setLineType(LineType.GREAT_CIRCLE);
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(ZOrderRenderProperties.TOP_Z - 1000, true, pickable);
        props.setColor(Color.RED);
        final float lineWidth = 2f;
        props.setWidth(lineWidth);
        lineBuilder.setLineSmoothing(false);
        lineBuilder.setRapidUpdate(true);
        lineBuilder.setVertices(positions);

        return new PolylineGeometry(lineBuilder, props, null);
    }

    /**
     * Constructor.
     *
     * @param arc An arc which will make up part of this arc.
     * @param position A new position which will be added to the end of the arc.
     * @param replaceLast When true, the last position of the given arc will be
     *            replaced with the new position. Otherwise, the new position
     *            will be appended.
     * @param completed When true summation information will be show for the
     *            arc.
     */
    public Arc(Arc arc, Pair<GeographicPosition, AbstractRenderableGeometry> position, boolean replaceLast, boolean completed)
    {
        myUnits = arc.getUnits();
        myHasCustomUnits = arc.hasCustomUnits();
        myVertices.addAll(arc.getVertices());
        if (replaceLast)
        {
            myVertices.remove(myVertices.size() - 1);
        }

        myVertices.add(position);

        List<GeographicPosition> geoVerts = getGeographicVertices();
        myLine = createPolyline(geoVerts, completed);
    }

    /**
     * Constructor.
     *
     * @param positions The positions that define the segments of the arc.
     * @param units The units used for the segment labels.
     * @param completed When true summation information will be show for the
     *            arc.
     */
    public Arc(List<Pair<GeographicPosition, AbstractRenderableGeometry>> positions, Class<? extends Length> units,
            boolean completed)
    {
        if (positions == null || positions.size() < 2)
        {
            throw new IllegalArgumentException("An arc must have at least two positions");
        }

        myUnits = units;
        myVertices.addAll(positions);
        List<GeographicPosition> geoPositions = New.list(positions.size());

        Pair<GeographicPosition, AbstractRenderableGeometry> start = positions.get(0);
        geoPositions.add(start.getFirstObject());
        Pair<GeographicPosition, AbstractRenderableGeometry> end;
        for (int i = 1; i < positions.size(); ++i)
        {
            end = positions.get(i);
            geoPositions.add(end.getFirstObject());
//            addBubble(start.getFirstObject(), end.getFirstObject(), completed);
            start = end;
        }

        myLine = createPolyline(geoPositions, completed);
    }

    /**
     * Create the callouts for this track.
     *
     * @param pickable If the callouts are pickable.
     * @param summarize If a summary callout should be created.
     *
     * @return The callouts.
     */
    public Collection<? extends Callout> createCallouts(boolean pickable, boolean summarize)
    {
        Collection<Callout> callouts = New.collection();

        GeographicPosition start = null;
        GeographicPosition end;
        for (int i = 0; i < getVertices().size(); ++i)
        {
            end = getVertices().get(i).getFirstObject();
            if (start != null)
            {
                callouts.add(createSegmentCallout(start, end));
            }
            start = end;
        }

        if (summarize)
        {
            callouts.add(createSummaryCallout());
        }

        return callouts;
    }

    /**
     * Create a callout for the last segment of the arc.
     *
     * @return The callout.
     */
    public Callout createLastSegmentCallout()
    {
        GeographicPosition start = getVertices().get(getVertices().size() - 2).getFirstObject();
        GeographicPosition end = getVertices().get(getVertices().size() - 1).getFirstObject();
        return createSegmentCallout(start, end);
    }

    /**
     * Get all of the geometries associated with this arc.
     *
     * @return all of the geometries associated with this arc.
     */
    public Collection<? extends Geometry> getAllGeometries()
    {
        return Collections.singleton(myLine);
    }

    /**
     * Get the vertices which define the segments of this arc.
     *
     * @return the vertices which define the segments of this arc.
     */
    public final List<GeographicPosition> getGeographicVertices()
    {
        List<GeographicPosition> vertices = New.list(myVertices.size());
        for (Pair<GeographicPosition, AbstractRenderableGeometry> vertex : myVertices)
        {
            vertices.add(vertex.getFirstObject());
        }
        return vertices;
    }

    /**
     * Get the line.
     *
     * @return the line
     */
    public PolylineGeometry getLine()
    {
        return myLine;
    }

    /**
     * Get the units.
     *
     * @return the units
     */
    public Class<? extends Length> getUnits()
    {
        return myUnits;
    }

    /**
     * Get the vertices which define the segments of this arc and their
     * associated geometries.
     *
     * @return the vertices which define the segments of this arc.
     */
    public final List<Pair<GeographicPosition, AbstractRenderableGeometry>> getVertices()
    {
        return New.list(myVertices);
    }

    /**
     * Get the hasCustomUnits.
     *
     * @return the hasCustomUnits
     */
    public boolean hasCustomUnits()
    {
        return myHasCustomUnits;
    }

    /**
     * Set the hasCustomUnits.
     *
     * @param hasCustomUnits the hasCustomUnits to set
     */
    public void setHasCustomUnits(boolean hasCustomUnits)
    {
        myHasCustomUnits = hasCustomUnits;
    }

    /**
     * Create a callout.
     *
     * @param labelText The text for the callout.
     * @param att The attachment point for the callout.
     * @param bearing the bearing in degrees
     * @return The callout.
     */
    protected Callout createCallout(List<String> labelText, LatLonAlt att, double bearing)
    {
        Callout callout = new CalloutImpl(0L, labelText, att, LABEL_FONT);
        callout.setBorderColor(LINE_COLOR);
        callout.setTextColor(LABEL_COLOR);
        callout.setBackgroundColor(BACKGROUND_COLOR);
        callout.setCornerRadius(10);
        callout.setAnchorOffset(getAnchorOffset(bearing));
        return callout;
    }

    /**
     * Gets the anchor offset for the given bearing.
     *
     * @param bearing the bearing
     * @return the anchor offset
     */
    private Vector2i getAnchorOffset(double bearing)
    {
        double adjBearing = bearing > 180 ? bearing - 180 : bearing;
        int yOffset = MathUtil.between(adjBearing, 10, 85) ? 45 : -10;
        return new Vector2i(10, yOffset);
    }

    /**
     * Create the text for the label of a single segment.
     *
     * @param start The start of the segment.
     * @param end The end of the segment.
     * @param bearing The bearing.
     * @return The label text.
     */
    private List<String> createLabelText(GeographicPosition start, GeographicPosition end, double bearing)
    {
        String bearingStr = String.format("%.3f\u00B0", Double.valueOf(bearing));

        double meters = GeographicBody3D.greatCircleDistanceM(start.getLatLonAlt(), end.getLatLonAlt(),
                WGS84EarthConstants.RADIUS_EQUATORIAL_M);
        Length length;
        try
        {
            length = Length.create(myUnits, new Meters(meters));
        }
        catch (InvalidUnitsException e)
        {
            LOGGER.warn("Could not use length type: " + e, e);
            length = new Meters(meters);
        }

        String lengthStr = String.format("%.3f " + length.getShortLabel(true), length.getDisplayMagnitudeObj());

        List<String> lines = New.list(2);
        lines.add(lengthStr);
        lines.add(bearingStr);
        return lines;
    }

    /**
     * Calculates the bearing from the start to end position.
     *
     * @param start the start position
     * @param end the end position
     * @return the bearing in degrees
     */
    private double calculateBearing(GeographicPosition start, GeographicPosition end)
    {
        double bearing = GeographicBody3D.greatCircleAzimuthD(start.getLatLonAlt(), end.getLatLonAlt());
        bearing = bearing < 0. ? 360. + bearing : bearing;
        return bearing;
    }

    /**
     * Generate a callout for the segment bounded by the given positions and add
     * them to the appropriate collections.
     *
     * @param start The start of the segment.
     * @param end The end of the segment.
     * @return The callout.
     */
    private Callout createSegmentCallout(GeographicPosition start, GeographicPosition end)
    {
        double bearing = calculateBearing(start, end);
        List<String> labelText = createLabelText(start, end, bearing);
        LatLonAlt att = GeographicBody3D.greatCircleInterpolate(start.getLatLonAlt(), end.getLatLonAlt(), .5);

        return createCallout(labelText, att, bearing);
    }

    /**
     * Create the text for the sum label.
     *
     * @param positions The positions which make up the track.
     * @return The label text.
     */
    private List<String> createSumLabelText(List<GeographicPosition> positions)
    {
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
            length = Length.create(myUnits, new Meters(meters));
        }
        catch (InvalidUnitsException e)
        {
            LOGGER.warn("Could not use length type: " + e, e);
            length = new Meters(meters);
        }

        String lengthStr = String.format("%.3f " + length.getShortLabel(true), length.getDisplayMagnitudeObj());

        List<String> lines = New.list(1);
        lines.add(lengthStr);
        return lines;
    }

    /**
     * Generate a summary callout for the entire track.
     *
     * @return The callout.
     */
    private Callout createSummaryCallout()
    {
        List<GeographicPosition> vertices = getGeographicVertices();
        List<String> labelText = createSumLabelText(vertices);
        LatLonAlt att = vertices.get(vertices.size() - 1).getLatLonAlt();

        Callout callout = createCallout(labelText, att, 0);
        callout.setBorderWidth(2f);
        callout.setBorderColor(Color.YELLOW);
        return callout;
    }
}
