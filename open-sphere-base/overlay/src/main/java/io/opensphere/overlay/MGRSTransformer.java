package io.opensphere.overlay;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.mgrs.GenericGrid;
import io.opensphere.core.mgrs.GridRectangle;
import io.opensphere.core.mgrs.GridSegments;
import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.mgrs.MGRSConverter.LatitudeBandData;
import io.opensphere.core.mgrs.MGRSCoreUtil;
import io.opensphere.core.mgrs.UTM;
import io.opensphere.core.mgrs.UTM.Hemisphere;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.Position;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.impl.HammerProjection;
import io.opensphere.core.viewer.Viewer;

/**
 * Class that encompasses creating and displaying MGRS grid lines on the map.
 */
@SuppressWarnings("PMD.GodClass")
public class MGRSTransformer extends DefaultTransformer
{
    /** The default altitude to use. */
    private static final double ALTM = 0.5;

    /** The color for the lines. */
    private static final Color LINE_COLOR = new Color(255, 200, 0, 255);

    /** Max longitude. */
    private static final double MAX_LON = 180.;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MGRSTransformer.class);

    /** If I'm enabled to display or not. */
    private boolean myEnabled;

    /** The MGRS sub grid geometries. */
    private final Set<GridGeometry> myGridGeometries = Collections.synchronizedSet(new HashSet<GridGeometry>());

    /** The latitude band letters. */
    private Set<LabelGeometry> myLatitudeLabels = Collections.synchronizedSet(new HashSet<LabelGeometry>());

    /** Projection used in coordinate conversions. */
    private volatile Projection myProjection;

    /** The UTM level label geometries. */
    private List<LabelGeometry> myUTMLabels = new ArrayList<>();

    /** The UTM level line geometries. */
    private final List<PolylineGeometry> myUTMLines = new ArrayList<>();

    /** This is needed to determine what is visible and what isn't. */
    private volatile Viewer myView;

    /** The bounding box created from the viewer. */
    private GeographicBoundingBox myViewBBox;

    /** The height of the viewer. */
    private double myViewHeight;

    /** The length of the viewer. */
    private double myViewLength;

    /** The longitude zone numbers. */
    private Set<LabelGeometry> myZoneLabels = Collections.synchronizedSet(new HashSet<LabelGeometry>());

    /** A collection of all the zones. */
    private final Map<String, UTMZone> myZones = new HashMap<>();

    /** Default constructor. */
    public MGRSTransformer()
    {
        super((DataRegistry)null);
    }

    /**
     * Goes through the collection of currently displayed grids and determines
     * if the grid is still visible. If a grid is no longer visible, it will be
     * removed from the display and the collection.
     */
    private void cleanGrids()
    {
        Set<GridGeometry> gridGeometries = new HashSet<>();
        Set<GridGeometry> toRemove = new HashSet<>();

        gridGeometries.addAll(getGridGeometries());
        for (GridGeometry geometry : gridGeometries)
        {
            if (!geometry.isInView())
            {
                geometry.clearGridGeometry(true);
                toRemove.add(geometry);
            }
        }
        getGridGeometries().removeAll(toRemove);
    }

    /** Remove the latitude band letters. */
    private void clearLatitudeBandLetter()
    {
        if (!getLatitudeLabels().isEmpty())
        {
            Set<LabelGeometry> latLabels = new HashSet<>(getLatitudeLabels());
            publishGeometries(Collections.<LabelGeometry>emptySet(), latLabels);
            getLatitudeLabels().clear();
        }
    }

    /** Remove the UTM lines. */
    private void clearUTMLines()
    {
        publishGeometries(Collections.<PolylineGeometry>emptySet(), myUTMLines);
    }

    /**
     * Remove UTM labels.
     */
    private void clearUTMZoneLabels()
    {
        if (myUTMLabels != null && !myUTMLabels.isEmpty())
        {
            publishGeometries(Collections.<PolylineGeometry>emptySet(), myUTMLabels);
        }
    }

    /** Remove the zone numbers. */
    private void clearZoneNumbers()
    {
        if (!getLongitudeLabels().isEmpty())
        {
            Set<LabelGeometry> lonLabels = new HashSet<>(getLongitudeLabels());
            publishGeometries(Collections.<LabelGeometry>emptySet(), lonLabels);
            getLongitudeLabels().clear();
        }
    }

    /**
     * Go through and create the zones.
     */
    private void createZones()
    {
        MGRSConverter parser = new MGRSConverter();
        List<LatitudeBandData> latitudeData = parser.getLatitudeBands();

        // This should always be six.

        final double lonSpacing = 6.;
        for (LatitudeBandData latData : latitudeData)
        {
            int index = 1;
            double lon;
            for (lon = -MAX_LON; lon < MAX_LON; lon += lonSpacing)
            {
                double lonAddRight = 0.;
                double lonAddLeft = 0.;
                // There are some special zones that we have to account for
                // (X32, X34, X36)
                // For the special zones determine right and left side
                // differences.
                if (latData.getLatitudeBand() == 'X' && index == 31)
                {
                    lonAddRight = 3;
                }
                if (latData.getLatitudeBand() == 'X' && (index == 33 || index == 35))
                {
                    lonAddRight = 3;
                    lonAddLeft = -3;
                }
                if (latData.getLatitudeBand() == 'X' && index == 37)
                {
                    lonAddLeft = -3;
                }

                if (latData.getLatitudeBand() == 'X' && (index == 32 || index == 34 || index == 36))
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(" Continuing for " + latData.getLatitudeBand() + index);
                    }
                    index++;
                    continue;
                }

                // And 31 & 32V around Norway
                if (latData.getLatitudeBand() == 'V' && index == 31)
                {
                    lonAddRight = -3;
                }
                if (latData.getLatitudeBand() == 'V' && index == 32)
                {
                    lonAddLeft = -3;
                }

                String zoneNumber = constructZone(index++);
                UTMZone zone = new UTMZone(latData, zoneNumber, lon + lonAddLeft, lon + lonSpacing + lonAddRight);

                myZones.put(zoneNumber + latData.getLatitudeBand(), zone);
            }
        }
    }

    /**
     * Display the MGRS grids.
     *
     * @param view The current view.
     * @param projection The current projection.
     */
    public void display(Viewer view, Projection projection)
    {
        createZones();
        drawUTMZonesLines();

        updateGrid(view, projection);
    }

    /**
     * Draws a band of the UTM latitude letters.
     */
    private synchronized void drawLatitudeBandLetter()
    {
        Set<LabelGeometry> old = new HashSet<>(getLatitudeLabels());
        myLatitudeLabels = new HashSet<>();

        // Use a longitude of 50 degrees west of current position.
        final double lonOffset = -50.;

        MGRSConverter parser = new MGRSConverter();
        List<LatitudeBandData> latitudeData = parser.getLatitudeBands();

        LabelGeometry.Builder<GeographicPosition> labelBuilder = new LabelGeometry.Builder<>();
        LabelRenderProperties props = new DefaultLabelRenderProperties(ZOrderRenderProperties.TOP_Z, true, false);
        props.setColor(Color.ORANGE);
        labelBuilder.setFont(Font.SANS_SERIF + " BOLD 18");
        labelBuilder.setHorizontalAlignment(.5f);
        labelBuilder.setVerticalAlignment(.5f);

        for (LatitudeBandData latData : latitudeData)
        {
            GeographicPosition geoPos = getProjection().convertToPosition(getViewer().getPosition().getLocation(),
                    ReferenceLevel.ELLIPSOID);
            GeographicPosition newPos = geoPos.add(new Vector3d(lonOffset, 0, 0));

            labelBuilder.setText(Character.toString(latData.getLatitudeBand()));

            labelBuilder.setPosition(new GeographicPosition(LatLonAlt.createFromDegreesMeters(latData.getSouth() + 4,
                    newPos.getLatLonAlt().getLonD(), ALTM, Altitude.ReferenceLevel.TERRAIN)));
            getLatitudeLabels().add(new LabelGeometry(labelBuilder, props, null));
        }

        publishGeometries(getLatitudeLabels(), old);
    }

    /**
     * Go through and draw the zones labels only.
     */
    private void drawUTMZoneLabels()
    {
        if (!myZones.isEmpty())
        {
            ArrayList<LabelGeometry> labels = new ArrayList<>();

            LabelGeometry.Builder<GeographicPosition> labelBuilder = new LabelGeometry.Builder<>();
            LabelRenderProperties props = new DefaultLabelRenderProperties(ZOrderRenderProperties.TOP_Z, true, true);
            props.setColor(Color.ORANGE);
            labelBuilder.setFont(Font.SANS_SERIF + " BOLD 18");
            labelBuilder.setHorizontalAlignment(.5f);
            labelBuilder.setVerticalAlignment(.5f);

            for (Map.Entry<String, UTMZone> entry : myZones.entrySet())
            {
                UTMZone zone = entry.getValue();

                if (!isVisible(zone))
                {
                    continue;
                }

                double latLoc = (zone.getMinLatitude() + zone.getMaxLatitude()) / 2.;
                double lonLoc = (zone.getMinLongitude() + zone.getMaxLongitude()) / 2.;
                labelBuilder.setText(zone.getName());
                labelBuilder.setPosition(new GeographicPosition(
                        LatLonAlt.createFromDegreesMeters(latLoc, lonLoc, ALTM, Altitude.ReferenceLevel.TERRAIN)));
                labels.add(new LabelGeometry(labelBuilder, props, null));
            }

            publishGeometries(labels, myUTMLabels);

            myUTMLabels = labels;
        }
    }

    /**
     * Go through and draw the zones (But not the labels).
     */
    private void drawUTMZonesLines()
    {
        if (!myZones.isEmpty())
        {
            PolylineGeometry.Builder<GeographicPosition> lineBuilder = new PolylineGeometry.Builder<>();
            PolylineRenderProperties props = new DefaultPolylineRenderProperties(1000, true, false);
            props.setColor(LINE_COLOR);
            final float lineWidth = .05f;
            props.setWidth(lineWidth);
            lineBuilder.setLineType(LineType.GREAT_CIRCLE);
            // /lineBuilder.setLineType(LineType.STRAIGHT_LINE);
            lineBuilder.setRapidUpdate(false);
            lineBuilder.setLineSmoothing(true);

            if (myUTMLines.isEmpty())
            {
                for (Map.Entry<String, UTMZone> entry : myZones.entrySet())
                {
                    UTMZone zone = entry.getValue();

                    List<GeographicPosition> vertices = new ArrayList<>();

                    // south east corner
                    vertices.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(zone.getMinLatitude(),
                            zone.getMaxLongitude(), ALTM, Altitude.ReferenceLevel.TERRAIN)));

                    // south west corner
                    vertices.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(zone.getMinLatitude(),
                            zone.getMinLongitude(), ALTM, Altitude.ReferenceLevel.TERRAIN)));

                    // north west corner
                    vertices.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(zone.getMaxLatitude(),
                            zone.getMinLongitude(), ALTM, Altitude.ReferenceLevel.TERRAIN)));

                    /* Only draw the south and west sides of the rectangle
                     * unless we are the top latitude. Our neighbors should fill
                     * in and complete the north and east sides. */
                    if (zone.getLatBand().getLatitudeBand() == 'X')
                    {
                        // north east corner
                        vertices.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(zone.getMaxLatitude(),
                                zone.getMaxLongitude(), ALTM, Altitude.ReferenceLevel.TERRAIN)));
                    }

                    lineBuilder.setVertices(vertices);
                    myUTMLines.add(new PolylineGeometry(lineBuilder, props, null));
                }
            }

            publishGeometries(myUTMLines, Collections.<PolylineGeometry>emptySet());
        }
    }

    /**
     * Draw a band of the UTM longitudinal zone numbers.
     */
    private synchronized void drawZoneNumbers()
    {
        Set<LabelGeometry> old = new HashSet<>(getLongitudeLabels());
        myZoneLabels = new HashSet<>();

        // Use a longitude of 35 degrees south of current position.
        final double latOffset = -35.;

        LabelGeometry.Builder<GeographicPosition> labelBuilder = new LabelGeometry.Builder<>();
        LabelRenderProperties props = new DefaultLabelRenderProperties(ZOrderRenderProperties.TOP_Z, true, false);
        props.setColor(Color.ORANGE);
        labelBuilder.setFont(Font.SANS_SERIF + " BOLD 16");
        labelBuilder.setHorizontalAlignment(.5f);
        labelBuilder.setVerticalAlignment(.5f);

        final double lonSpacing = 6.;
        int index = 1;
        for (double lon = -MAX_LON; lon < MAX_LON; lon += lonSpacing)
        {
            GeographicPosition geoPos = getProjection().convertToPosition(getViewer().getPosition().getLocation(),
                    ReferenceLevel.ELLIPSOID);
            GeographicPosition newPos = geoPos.add(new Vector3d(0, latOffset, 0));

            labelBuilder.setText(Integer.toString(index++));

            labelBuilder.setPosition(new GeographicPosition(LatLonAlt.createFromDegreesMeters(newPos.getLatLonAlt().getLatD(),
                    lon + 2, ALTM, Altitude.ReferenceLevel.TERRAIN)));
            getLongitudeLabels().add(new LabelGeometry(labelBuilder, props, null));
        }

        publishGeometries(getLongitudeLabels(), old);
    }

    /**
     * Find the visible UTM zones.
     *
     * @return A collection of UTM zones.
     */
    private List<UTMZone> findNeighbors()
    {
        List<UTMZone> neighbors = new ArrayList<>();

        /* Originally was calculating neighbors giving the current position and
         * knowing the size of the latitude band zone and where the neighbors
         * should be, but it was kind of complex with all the special cases so
         * abandoned that for this simple way. */
        for (Map.Entry<String, UTMZone> entry : myZones.entrySet())
        {
            UTMZone zone = entry.getValue();
            if (zone.isInView())
            {
                neighbors.add(zone);
            }
        }
        return neighbors;
    }

    /**
     * Standard getter.
     *
     * @return Return the current grid geometries.
     */
    public Set<GridGeometry> getGridGeometries()
    {
        synchronized (myGridGeometries)
        {
            return myGridGeometries;
        }
    }

    /**
     * Standard getter.
     *
     * @return Return the current latitude labels.
     */
    public Set<LabelGeometry> getLatitudeLabels()
    {
        synchronized (myLatitudeLabels)
        {
            return myLatitudeLabels;
        }
    }

    /**
     * Standard getter.
     *
     * @return Return the current longitude labels.
     */
    public Set<LabelGeometry> getLongitudeLabels()
    {
        synchronized (myZoneLabels)
        {
            return myZoneLabels;
        }
    }

    /**
     * Standard getter.
     *
     * @return The current projection.
     */
    public Projection getProjection()
    {
        return myProjection;
    }

    /**
     * Standard getter.
     *
     * @return The current Viewer.
     */
    public Viewer getViewer()
    {
        return myView;
    }

    /**
     * Standard Getter.
     *
     * @return true if we are enabled, false otherwise.
     */
    public boolean isEnabled()
    {
        return myEnabled;
    }

    /**
     * Remove the MGRS grids.
     */
    public void remove()
    {
        clearUTMLines();
        clearLatitudeBandLetter();
        clearZoneNumbers();
        clearUTMZoneLabels();

        removeAllSubGrids();
    }

    /**
     * Remove all sub grids (if there are any).
     */
    private void removeAllSubGrids()
    {
        if (!getGridGeometries().isEmpty())
        {
            Set<GridGeometry> gridGeometries = new HashSet<>();

            gridGeometries.addAll(getGridGeometries());
            for (GridGeometry geometry : gridGeometries)
            {
                geometry.clearGridGeometry(true);
            }
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(" There are entries in myGridGeometries that have not been removed");
            }
            getGridGeometries().clear();
        }
    }

    /**
     * Set values that will be used in later calculations (The viewer bounding
     * box for example). Calculate the values once here rather than a plethora
     * of times later on.
     */
    private void setCommonValues()
    {
        // Create a bounding box for the viewer. (use the corners of the view
        // port)
        Vector2i lowerLeft = Vector2i.ORIGIN;
        Vector2i upperLeft = new Vector2i(0, getViewer().getViewportHeight());
        Vector2i lowerRight = new Vector2i(getViewer().getViewportWidth(), 0);
        Vector2i upperRight = new Vector2i(getViewer().getViewportWidth(), getViewer().getViewportHeight());

        // Then convert to model coordinates
        Vector3d lowerLeftModel = getViewer().windowToModelCoords(lowerLeft);
        Vector3d upperLeftModel = getViewer().windowToModelCoords(upperLeft);
        Vector3d lowerRightModel = getViewer().windowToModelCoords(lowerRight);
        Vector3d upperRightModel = getViewer().windowToModelCoords(upperRight);

        // We should always be zoomed in when this is called, but still good to
        // check.
        if (lowerLeftModel == null || upperLeftModel == null || lowerRightModel == null || upperRightModel == null)
        {
            LOGGER.warn(" Cannot determine bounding box, null value.");
            return;
        }

        myViewHeight = getViewer().getViewLength(lowerLeftModel, upperLeftModel);
        myViewLength = getViewer().getViewLength(lowerLeftModel, lowerRightModel);

        GeographicPosition lowerLeftPos = getProjection().convertToPosition(lowerLeftModel, ReferenceLevel.ELLIPSOID);
        GeographicPosition upperRightPos = getProjection().convertToPosition(upperRightModel, ReferenceLevel.ELLIPSOID);

        // The hammer projection needs special treatment because grids are
        // distorted as you near the 180 degree border close to the poles. And
        // we have to watch for crossing the 180 degree border when finding the
        // bounding box.
        if (getProjection() instanceof HammerProjection)
        {
            GeographicPosition upperLeftPos = getProjection().convertToPosition(upperLeftModel, ReferenceLevel.ELLIPSOID);
            GeographicPosition lowerRightPos = getProjection().convertToPosition(lowerRightModel, ReferenceLevel.ELLIPSOID);

            myViewBBox = MGRSCoreUtil.calculateBoundingBox(lowerRightPos, lowerLeftPos, upperLeftPos, upperRightPos);
        }
        else
        {
            myViewBBox = new GeographicBoundingBox(lowerLeftPos, upperRightPos);
        }
    }

    /**
     * Standard Setter.
     *
     * @param enabled boolean that determines if enabled or not.
     */
    public void setEnabled(boolean enabled)
    {
        myEnabled = enabled;
    }

    /**
     * Standard setter.
     *
     * @param projection The new projection to use.
     */
    public void setProjection(Projection projection)
    {
        myProjection = projection;
    }

    /**
     * Standard setter.
     *
     * @param viewer The new Viewer to use.
     */
    public void setViewer(Viewer viewer)
    {
        myView = viewer;
    }

    /**
     * Update MGRS grid with new view.
     *
     * @param view The updated view.
     * @param projection The projection.
     */
    protected synchronized void updateGrid(Viewer view, Projection projection)
    {
        setViewer(view);
        setProjection(projection);

        /* We know that the LatBandZone is always 6 degrees wide and 8 degrees
         * tall. Do a check to see how this compares to the view-port size to
         * determine what levels to display. */
        GeographicPosition currentPos = projection.convertToPosition(view.getModelIntersection(), ReferenceLevel.ELLIPSOID);

        LatLonAlt lowerLeft = currentPos.getLatLonAlt().addDegreesMeters(-4, -3, 0).getNormalized();
        LatLonAlt upperLeft = currentPos.getLatLonAlt().addDegreesMeters(4, -3, 0).getNormalized();
        LatLonAlt lowerRight = currentPos.getLatLonAlt().addDegreesMeters(-4, 3, 0).getNormalized();

        // Convert to model coordinates
        Vector3d modelLowerLeft = projection.convertToModel(new GeographicPosition(lowerLeft), Vector3d.ORIGIN);
        Vector3d modelUpperLeft = projection.convertToModel(new GeographicPosition(upperLeft), Vector3d.ORIGIN);
        Vector3d modelLowerRight = projection.convertToModel(new GeographicPosition(lowerRight), Vector3d.ORIGIN);

        double gridLength = view.getViewLength(modelLowerLeft, modelLowerRight);
        double gridHeight = view.getViewLength(modelLowerLeft, modelUpperLeft);
        double gridArea = gridLength * gridHeight;
        double viewArea = view.getViewportHeight() * view.getViewportWidth();

        final double utmThreshold = .1;
        final double mgrsThreshold = .2;

        if (gridArea > viewArea * utmThreshold)
        {
            setCommonValues();
            clearLatitudeBandLetter();
            clearZoneNumbers();

            if (gridArea > viewArea * mgrsThreshold)
            {
                cleanGrids();

                // Clear UTM labels and lines.
                clearUTMZoneLabels();

                // First determine what our zone is
                MGRSConverter parser = new MGRSConverter();
                currentPos = adjustForPoles(currentPos);

                UTM utm = new UTM(currentPos);
                String fullMGRSString = parser.createString(utm);
                if (fullMGRSString == null)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Unable to update grid.");
                    }
                    return;
                }

                String utmString = fullMGRSString.substring(0, 3);
                if (myZones.containsKey(utmString))
                {
                    // Find neighboring UTM zones that are visible
                    List<UTMZone> visibleUTMZones = findNeighbors();

                    for (UTMZone zone : visibleUTMZones)
                    {
                        zone.displayGridGeometries();
                    }
                }
            }
            else
            {
                drawUTMZoneLabels();
                removeAllSubGrids();
            }
        }
        else
        {
            clearUTMZoneLabels();

            drawLatitudeBandLetter();
            drawZoneNumbers();

            removeAllSubGrids();
        }
    }

    /**
     * This is a helper method that takes a position and checks for being close
     * to the poles and out of the range of UTM. If so it will return a position
     * that is just inside the nearest UTM zone.
     *
     * @param geoPos The GeographicPosition to check.
     * @return The updated position.
     */
    private GeographicPosition adjustForPoles(GeographicPosition geoPos)
    {
        GeographicPosition adjustedPos = geoPos;

        /* If we are close to the poles (outside of UTM area), then adjust to
         * show the nearest zone Use a little padding so UTM creation
         * calculations do not put it slightly out of the zone. */
        if (geoPos.getLatLonAlt().getLatD() > MGRSConverter.UPPER_LAT_LIMIT)
        {
            adjustedPos = new GeographicPosition(
                    LatLonAlt.createFromDegrees(MGRSConverter.UPPER_LAT_LIMIT - 3, geoPos.getLatLonAlt().getLonD()));
        }
        else if (geoPos.getLatLonAlt().getLatD() < MGRSConverter.LOWER_LAT_LIMIT)
        {
            adjustedPos = new GeographicPosition(
                    LatLonAlt.createFromDegrees(MGRSConverter.LOWER_LAT_LIMIT + 3, geoPos.getLatLonAlt().getLonD()));
        }
        return adjustedPos;
    }

    /**
     * This method checks for single digit zones (1-9) and will add a zero to
     * the front of single digits and return as a String.
     *
     * @param zone The zone
     * @return A String of the zone.
     */
    private String constructZone(int zone)
    {
        String strZone = Integer.toString(zone);
        StringBuffer strBufZone = new StringBuffer();
        // For zones 1-9 we need to add a beginning zero. i.e. 6 becomes 06
        if (strZone.length() == 1)
        {
            strBufZone.append('0').append(strZone);
        }
        else
        {
            strBufZone.append(strZone);
        }
        return strBufZone.toString();
    }

    /**
     * Determine if the latitude band zone is visible or not.
     *
     * @param zone The zone to check.
     * @return True if visible, false otherwise.
     */
    private boolean isVisible(UTMZone zone)
    {
        boolean isVisible = false;

        // First get corners
        GeographicPosition swGeoPos = new GeographicPosition(
                LatLonAlt.createFromDegrees(zone.getMinLatitude(), zone.getMinLongitude()));
        GeographicPosition nwGeoPos = new GeographicPosition(
                LatLonAlt.createFromDegrees(zone.getMaxLatitude(), zone.getMinLongitude()));
        GeographicPosition neGeoPos = new GeographicPosition(
                LatLonAlt.createFromDegrees(zone.getMaxLatitude(), zone.getMaxLongitude()));
        GeographicPosition seGeoPos = new GeographicPosition(
                LatLonAlt.createFromDegrees(zone.getMinLatitude(), zone.getMaxLongitude()));

        // And convert to model coordinates
        Vector3d swModel = getProjection().convertToModel(swGeoPos, Vector3d.ORIGIN);
        Vector3d nwModel = getProjection().convertToModel(nwGeoPos, Vector3d.ORIGIN);
        Vector3d neModel = getProjection().convertToModel(neGeoPos, Vector3d.ORIGIN);
        Vector3d seModel = getProjection().convertToModel(seGeoPos, Vector3d.ORIGIN);

        // Now check if any of the corners are visible
        if (getViewer().isInView(swModel, 0))
        {
            isVisible = true;
        }
        else if (getViewer().isInView(nwModel, 0))
        {
            isVisible = true;
        }
        else if (getViewer().isInView(neModel, 0))
        {
            isVisible = true;
        }
        else if (getViewer().isInView(seModel, 0))
        {
            isVisible = true;
        }

        return isVisible;
    }

    /**
     * This class describes MGRS grids. The parent geometry will be the MGRS
     * layer and the children will be 10x10 sub grids.
     */
    private class GridGeometry extends GenericGrid
    {
        /** The children easting labels. */
        private final Map<Integer, GeographicPosition> myChildEastingLabels = new HashMap<>();

        /** The easting/northing labels I hold for my children. */
        private List<LabelGeometry> myChildLabels = new ArrayList<>();

        /** The children northing labels. */
        private final SortedMap<Integer, GeographicPosition> myChildNorthingLabels = new TreeMap<>();

        /** The children sub grids. */
        private Set<GridGeometry> myChildren;

        /** The boolean for determining if labels have been drawn or not. */
        private boolean myGridLabelsDrawn;

        /** The segments of grid to be drawn. */
        private GridSegments myGridSegments;

        /** The hemisphere ('NORTH' or 'SOUTH'). */
        private Hemisphere myHemisphere;

        /** The latitude band letter. */
        private char myLatitudeBand;

        /** The MGRS level label geometries. */
        private List<LabelGeometry> myMGRSLabels = new ArrayList<>();

        /** The MGRS level line geometries. */
        private List<PolylineGeometry> myMGRSLines = new ArrayList<>();

        /** The boolean for determining if I border my parent or not. */
        private boolean myOnBorder;

        /** The parent (The top mgrs layer will not have a parent). */
        private GridGeometry myParent;

        /** The size. */
        private double mySize;

        /**
         * Text that will be displayed. This will be the MGRS label for the top
         * layer.
         */
        private String myText;

        /** The utm zone (1-60). */
        private int myZone;

        /**
         * Remove grid geometry.
         *
         * @param removeChildren boolean to determine whether or not to clear
         *            children.
         */
        private void clearGridGeometry(boolean removeChildren)
        {
            if (getGridGeometries().contains(this))
            {
                if (!myMGRSLines.isEmpty())
                {
                    publishGeometries(Collections.<PolylineGeometry>emptySet(), myMGRSLines);
                }

                if (!myMGRSLabels.isEmpty())
                {
                    publishGeometries(Collections.<LabelGeometry>emptySet(), myMGRSLabels);
                }

                // If my parent contains easting/northing labels for me and my
                // siblings, remove them.
                if (myParent != null && myParent.isGridLabelsDrawn())
                {
                    publishGeometries(Collections.<LabelGeometry>emptySet(), myParent.getChildLabels());
                    myParent.setGridLabelsDrawn(false);
                }

                getGridGeometries().remove(this);
            }

            // If I have any children, then remove them as well
            if (removeChildren && getChildren() != null && !getChildren().isEmpty())
            {
                Set<GridGeometry> children = new HashSet<>();
                children.addAll(getChildren());
                for (GridGeometry child : children)
                {
                    child.clearGridGeometry(true);
                }
                myChildren.clear();
                myChildren = null;
            }
        }

        /**
         * Creates a sub grid geometry given the following parameters. Helper
         * method for the divider.
         *
         * @param easting The easting.
         * @param northing The northing.
         * @param size The size of the grid.
         * @param border If this sub grid is adjacent to my border.
         * @return The newly created grid geometry.
         */
        private GridGeometry createSubGrid(double easting, double northing, double size, boolean border)
        {
            GridGeometry newGrid = new GridGeometry();

            newGrid.setParent(this);
            newGrid.setSize(size);
            newGrid.setSWEasting(easting);
            newGrid.setSWNorthing(northing);
            newGrid.setZone(getZone());
            newGrid.setLatitudeBand(getLatitudeBand());
            newGrid.setHemisphere(getHemisphere());
            newGrid.setText(null);
            newGrid.setOnBorder(border);

            UTM utm = new UTM(getZone(), getHemisphere(), easting, northing);
            GeographicPosition swPos = utm.convertToLatLon();

            utm = new UTM(getZone(), getHemisphere(), easting, northing + size);
            GeographicPosition nwPos = utm.convertToLatLon();

            utm = new UTM(getZone(), getHemisphere(), easting + size, northing + size);
            GeographicPosition nePos = utm.convertToLatLon();

            utm = new UTM(getZone(), getHemisphere(), easting + size, northing);
            GeographicPosition sePos = utm.convertToLatLon();

            if (!getBoundingBox().contains(swPos, 0) && !getBoundingBox().contains(nwPos, 0)
                    && !getBoundingBox().contains(nePos, 0) && !getBoundingBox().contains(sePos, 0))
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(" All points outside parent, not creating sub grid " + getBoundingBox().toSimpleString());
                }
                return null;
            }

            newGrid.setGridSegments(MGRSCoreUtil.determineRenderableLines(getBoundingBox(), sePos, swPos, nwPos, nePos));

            newGrid.setSWPos(MGRSCoreUtil.checkPosition(getBoundingBox(), swPos, sePos, nwPos));
            newGrid.setNWPos(MGRSCoreUtil.checkPosition(getBoundingBox(), nwPos, nePos, swPos));
            newGrid.setNEPos(MGRSCoreUtil.checkPosition(getBoundingBox(), nePos, nwPos, sePos));
            newGrid.setSEPos(MGRSCoreUtil.checkPosition(getBoundingBox(), sePos, swPos, nePos));

            MGRSCoreUtil.checkForTriangle(newGrid);

            if (newGrid.getSWPos() == null || newGrid.getNWPos() == null || newGrid.getNEPos() == null
                    || newGrid.getSEPos() == null)
            {
                LOGGER.error(" Cannot create sub grid, NULL value found");
                return null;
            }

            GeographicPosition centerPos = MGRSCoreUtil.getCenterPoint(newGrid.getSEPos(), newGrid.getSWPos(), newGrid.getNWPos(),
                    newGrid.getNEPos());

            newGrid.setCenterPosition(centerPos);

            if (!myChildEastingLabels.containsKey(Integer.valueOf((int)easting)))
            {
                GeographicPosition eastingPos = new GeographicPosition(LatLonAlt.createFromDegrees(
                        getCenterPosition().getLatLonAlt().getLatD(), newGrid.getSWPos().getLatLonAlt().getLonD()));
                myChildEastingLabels.put(Integer.valueOf((int)easting), eastingPos);
            }

            if (!myChildNorthingLabels.containsKey(Integer.valueOf((int)northing)))
            {
                GeographicPosition northingPos = new GeographicPosition(LatLonAlt.createFromDegrees(
                        newGrid.getSWPos().getLatLonAlt().getLatD(), getCenterPosition().getLatLonAlt().getLonD()));
                myChildNorthingLabels.put(Integer.valueOf((int)northing), northingPos);
            }

            newGrid.setBoundingBox(
                    MGRSCoreUtil.createBoundingBox(newGrid.getSEPos(), newGrid.getSWPos(), newGrid.getNWPos(), newGrid.getNEPos()));

            return newGrid;
        }

        /**
         * This method determines whether this geometry should display or not
         * and then takes appropriate action.
         *
         * @param view The Viewer.
         * @param proj The Projection.
         */
        private void displayGrids(Viewer view, Projection proj)
        {
            int value = shouldDisplay(view, proj);

            switch (value)
            {
                // Remove grid
                case 0:
                    clearGridGeometry(true);
                    break;
                // Display grid
                case 1:
                    if (isVisible())
                    {
                        drawGridGeometry();
                    }
                    else
                    {
                        // Remove myself
                        clearGridGeometry(false);
                    }

                    // And then remove any children (if there are any)
                    if (myChildren != null)
                    {
                        Set<GridGeometry> childrenCopy = new HashSet<>();
                        childrenCopy.addAll(myChildren);
                        for (GridGeometry child : childrenCopy)
                        {
                            child.clearGridGeometry(true);
                        }
                        myChildren.clear();
                        myChildren = null;
                    }
                    break;
                // Check sub grids
                case -1:
                    // Don't want to go any further than 1M level.
                    if (mySize == 1)
                    {
                        break;
                    }

                    // Dont bother with my children if I'm not in view
                    if (!isInView())
                    {
                        break;
                    }

                    clearGridGeometry(false);

                    if (myChildren == null)
                    {
                        myChildren = divide(this);
                    }
                    for (GridGeometry child : myChildren)
                    {
                        child.displayGrids(view, proj);
                    }
                    break;
                default:
                    LOGGER.warn(" displayGrids default reached");
                    break;
            }
        }

        /**
         * Divide grid into it's sub grids.
         *
         * @param parent The parent grid to divide.
         * @return A set of geometries describing the children.
         */
        private Set<GridGeometry> divide(GridGeometry parent)
        {
            Set<GridGeometry> result = new HashSet<>(100);

            double gridStep = parent.getSize() / 10;
            for (int i = 0; i < 10; i++)
            {
                double easting = parent.getSWEasting() + gridStep * i;
                for (int j = 0; j < 10; j++)
                {
                    double northing = parent.getSWNorthing() + gridStep * j;
                    boolean border = i == 9 || j == 9;

                    GridGeometry subgrid = parent.createSubGrid(easting, northing, gridStep, border);
                    if (subgrid != null)
                    {
                        result.add(subgrid);
                    }
                }
            }
            return result;
        }

        /**
         * Draw and display the grid geometry.
         */
        private void drawGridGeometry()
        {
            if (!getGridGeometries().contains(this))
            {
                drawLines();

                drawLabels();

                getGridGeometries().add(this);
            }
        }

        /**
         * Standard getter.
         *
         * @return The children easting labels.
         */
        public Map<Integer, GeographicPosition> getChildEastingLabels()
        {
            return myChildEastingLabels;
        }

        /**
         * Standard getter.
         *
         * @return The children's labels.
         */
        public List<LabelGeometry> getChildLabels()
        {
            return myChildLabels;
        }

        /**
         * Standard getter.
         *
         * @return The children northing labels.
         */
        public SortedMap<Integer, GeographicPosition> getChildNorthingLabels()
        {
            return myChildNorthingLabels;
        }

        /**
         * Standard getter.
         *
         * @return The sub geometries.
         */
        public Set<GridGeometry> getChildren()
        {
            return myChildren;
        }

        /**
         * Standard getter.
         *
         * @return The hemisphere ('NORTH' or 'SOUTH').
         */
        public Hemisphere getHemisphere()
        {
            return myHemisphere;
        }

        /**
         * Standard getter.
         *
         * @return The latitude band letter.
         */
        public char getLatitudeBand()
        {
            return myLatitudeBand;
        }

        /**
         * Standard getter.
         *
         * @return The grid size.
         */
        public double getSize()
        {
            return mySize;
        }

        /**
         * Standard getter.
         *
         * @return The text label.
         */
        public String getText()
        {
            return myText;
        }

        /**
         * Standard getter.
         *
         * @return The UTM zone we are in.
         */
        public int getZone()
        {
            return myZone;
        }

        /**
         * Determine if grid labels have been drawn.
         *
         * @return True if labels have already been drawn, false otherwise.
         */
        public boolean isGridLabelsDrawn()
        {
            return myGridLabelsDrawn;
        }

        /**
         * Standard getter.
         *
         * @return True if this grid borders it's parent, false otherwise.
         */
        public boolean isOnBorder()
        {
            return myOnBorder;
        }

        /**
         * Standard setter.
         *
         * @param childLabels The children's labels.
         */
        public void setChildLabels(List<LabelGeometry> childLabels)
        {
            myChildLabels = childLabels;
        }

        /**
         * Standard setter.
         *
         * @param gridLabelsDrawn The value to set to.
         */
        public void setGridLabelsDrawn(boolean gridLabelsDrawn)
        {
            myGridLabelsDrawn = gridLabelsDrawn;
        }

        /**
         * Standard setter.
         *
         * @param gridSegments The segments of the grid to draw.
         */
        public void setGridSegments(GridSegments gridSegments)
        {
            myGridSegments = gridSegments;
        }

        /**
         * Standard setter.
         *
         * @param hemisphere The hemisphere.
         */
        public void setHemisphere(Hemisphere hemisphere)
        {
            myHemisphere = hemisphere;
        }

        /**
         * Standard setter.
         *
         * @param latitudeBand The latitude band letter.
         */
        public void setLatitudeBand(char latitudeBand)
        {
            myLatitudeBand = latitudeBand;
        }

        /**
         * Standard setter.
         *
         * @param onBorder Whether or not this grid borders it's parent.
         */
        public void setOnBorder(boolean onBorder)
        {
            myOnBorder = onBorder;
        }

        /**
         * Standard setter.
         *
         * @param parent The parent.
         */
        public void setParent(GridGeometry parent)
        {
            myParent = parent;
        }

        /**
         * Standard setter.
         *
         * @param size The grid size.
         */
        public void setSize(double size)
        {
            mySize = size;
        }

        /**
         * Standard setter.
         *
         * @param text The text label.
         */
        public void setText(String text)
        {
            myText = text;
        }

        /**
         * Standard setter.
         *
         * @param zone The utm zone.
         */
        public void setZone(int zone)
        {
            myZone = zone;
        }

        /**
         * Check to determine if this geometry should or should not be displayed
         * and whether the sub grids should be checked. (-1 = check sub grids, 0
         * = don't display, 1 = display).
         *
         * @param view The Viewer to use in determination.
         * @param proj The Projection to use in determination.
         * @return int The corresponding value.
         */
        private int shouldDisplay(Viewer view, Projection proj)
        {
            int status = 0;

            // Convert my positions to model coordinates
            Vector3d modelLowerLeft = proj.convertToModel(getSWPos(), Vector3d.ORIGIN);
            Vector3d modelUpperLeft = proj.convertToModel(getNWPos(), Vector3d.ORIGIN);
            Vector3d modelLowerRight = proj.convertToModel(getSEPos(), Vector3d.ORIGIN);

            double gridLength = view.getViewLength(modelLowerLeft, modelLowerRight);
            double gridHeight = view.getViewLength(modelLowerLeft, modelUpperLeft);

            // When to draw myself.
            final double low = .08;
            // When to divide myself.
            final double high = .8;

            if (gridHeight > myViewHeight * low || gridLength > myViewLength * low)
            {
                if (gridHeight > myViewHeight * high || gridLength > myViewLength * high)
                {
                    // Check my sub grids
                    status = -1;
                }
                else
                {
                    // Draw myself
                    status = 1;
                }
            }
            else
            {
                // Remove myself
                status = 0;
            }

            return status;
        }

        /**
         * Draw the easting/northing labels for mgrs sub grids.
         */
        private void drawEastingNorthingLabels()
        {
            /* Check to see if labels have already been drawn or there are not
             * enough sub grids to warrant drawing the labels. */
            if (!myParent.isGridLabelsDrawn() && myParent.getChildren().size() > 50)
            {
                // If we already have labels, use them
                if (!myParent.getChildLabels().isEmpty())
                {
                    publishGeometries(myParent.getChildLabels(), Collections.<LabelGeometry>emptySet());
                    myParent.setGridLabelsDrawn(true);
                }
                else
                // find labels
                {
                    List<LabelGeometry> labels = new ArrayList<>();
                    LabelGeometry.Builder<GeographicPosition> labelBuilder = new LabelGeometry.Builder<>();
                    LabelRenderProperties props = new DefaultLabelRenderProperties(ZOrderRenderProperties.TOP_Z, true, false);
                    props.setColor(Color.ORANGE);
                    labelBuilder.setFont("Arial-9");
                    labelBuilder.setHorizontalAlignment(.5f);
                    labelBuilder.setVerticalAlignment(.5f);

                    if (!myParent.getChildNorthingLabels().isEmpty())
                    {
                        int index = 0;
                        for (Entry<Integer, GeographicPosition> northingEntry : myParent.getChildNorthingLabels().entrySet())
                        {
                            /* Want to skip the middle northing value since it
                             * will be right on top of easting value. */
                            if (index != 5)
                            {
                                labelBuilder.setText(northingEntry.getKey().toString());
                                labelBuilder.setPosition(northingEntry.getValue());

                                LabelGeometry labelGeometry = new LabelGeometry(labelBuilder, props, null);
                                labels.add(labelGeometry);
                            }
                            index++;
                        }
                    }

                    if (!myParent.getChildEastingLabels().isEmpty())
                    {
                        for (Entry<Integer, GeographicPosition> eastingEntry : myParent.getChildEastingLabels().entrySet())
                        {
                            labelBuilder.setText(eastingEntry.getKey().toString());
                            labelBuilder.setPosition(eastingEntry.getValue());

                            LabelGeometry labelGeometry = new LabelGeometry(labelBuilder, props, null);
                            labels.add(labelGeometry);
                        }
                    }
                    publishGeometries(labels, Collections.<LabelGeometry>emptySet());

                    myParent.setChildLabels(labels);
                    myParent.setGridLabelsDrawn(true);
                }
            }
        }

        /**
         * Draw our text. For the top MGRS grid it's the label and for sub grids
         * it's the northing/easting labels.
         */
        private void drawLabels()
        {
            List<LabelGeometry> labels = new ArrayList<>();
            if (getText() != null)
            {
                LabelGeometry.Builder<GeographicPosition> labelBuilder = new LabelGeometry.Builder<>();
                LabelRenderProperties props = new DefaultLabelRenderProperties(ZOrderRenderProperties.TOP_Z, true, false);
                props.setColor(Color.ORANGE);
                labelBuilder.setFont(null);
                labelBuilder.setHorizontalAlignment(.5f);
                labelBuilder.setVerticalAlignment(.5f);

                labelBuilder.setText(getText());
                labelBuilder.setPosition(getCenterPosition());
                labels.add(new LabelGeometry(labelBuilder, props, null));

                publishGeometries(labels, myMGRSLabels);

                myMGRSLabels = labels;
            }
            else if (myParent != null && !myParent.isGridLabelsDrawn())
            {
                drawEastingNorthingLabels();
            }
        }

        /**
         * Draw the lines for this grid.
         */
        private void drawLines()
        {
            PolylineGeometry.Builder<Position> lineBuilder = new PolylineGeometry.Builder<>();
            PolylineRenderProperties props = new DefaultPolylineRenderProperties(ZOrderRenderProperties.TOP_Z, true, true);
            props.setColor(LINE_COLOR);
            final float lineWidth = .2f;
            props.setWidth(lineWidth);
            lineBuilder.setLineType(LineType.GREAT_CIRCLE);
            // /lineBuilder.setLineType(LineType.STRAIGHT_LINE);
            lineBuilder.setRapidUpdate(false);
            lineBuilder.setLineSmoothing(true);

            List<PolylineGeometry> lines = new ArrayList<>();

            List<GeographicPosition> vertices = new ArrayList<>();

            if (myGridSegments.getSouthSegment() != null)
            {
                vertices.add(myGridSegments.getSouthSegment().getFirstPoint());
                vertices.add(myGridSegments.getSouthSegment().getSecondPoint());
                lineBuilder.setVertices(vertices);
                lines.add(new PolylineGeometry(lineBuilder, props, null));
                vertices.clear();
            }

            if (myGridSegments.getWestSegment() != null)
            {
                vertices.add(myGridSegments.getWestSegment().getFirstPoint());
                vertices.add(myGridSegments.getWestSegment().getSecondPoint());
                lineBuilder.setVertices(vertices);
                lines.add(new PolylineGeometry(lineBuilder, props, null));
                vertices.clear();
            }

            if (isOnBorder())
            {
                if (myGridSegments.getNorthSegment() != null)
                {
                    vertices.add(myGridSegments.getNorthSegment().getFirstPoint());
                    vertices.add(myGridSegments.getNorthSegment().getSecondPoint());
                    lineBuilder.setVertices(vertices);
                    lines.add(new PolylineGeometry(lineBuilder, props, null));
                    vertices.clear();
                }

                if (myGridSegments.getEastSegment() != null)
                {
                    vertices.add(myGridSegments.getEastSegment().getFirstPoint());
                    vertices.add(myGridSegments.getEastSegment().getSecondPoint());
                    lineBuilder.setVertices(vertices);
                    lines.add(new PolylineGeometry(lineBuilder, props, null));
                    vertices.clear();
                }
            }
            publishGeometries(lines, myMGRSLines);

            myMGRSLines = lines;
        }

        /**
         * Determine if this geometry is in view by using bounding boxes. This
         * should only be called when we are sufficiently zoomed in so the
         * corners of the view-port are over the earth.
         *
         * @return True if geometry is in view, false otherwise.
         */
        private boolean isInView()
        {
            if (myViewBBox != null)
            {
                // check if we overlap
                return myViewBBox.overlaps(getBoundingBox(), 0.);
            }
            return false;
        }

        /**
         * Determine if this geometry is visible or not.
         *
         * @return True if in visible area, false otherwise.
         */
        private boolean isVisible()
        {
            //@formatter:off
            return myViewBBox != null
                    && (myViewBBox.contains(getSWPos(), 0)
                            || myViewBBox.contains(getNWPos(), 0)
                            || myViewBBox.contains(getNEPos(), 0)
                            || myViewBBox.contains(getSEPos(), 0)
                            || myViewBBox.contains(getCenterPosition(), 0));
            //@formatter:on
        }
    }

    /**
     * This class represents area that is intersection of latitude band and a
     * zone.
     */
    private final class UTMZone extends GridRectangle
    {
        /** The hemisphere. */
        private final Hemisphere myHSphere;

        /** The latitude band. */
        private final LatitudeBandData myLatBand;

        /** The 100km square MGRS zones. */
        private final Map<String, GridGeometry> myMGRSSquares = new HashMap<>();

        /** The name. */
        private final String myName;

        /** The zone. */
        private final int myZone;

        /**
         * Constructor.
         *
         * @param latBand The LatitudeBandData.
         * @param zoneNumber The zone number.
         * @param westLongitude The west longitude.
         * @param eastLongitude The east longitude.
         */
        public UTMZone(LatitudeBandData latBand, String zoneNumber, double westLongitude, double eastLongitude)
        {
            super(latBand.getSouth(), latBand.getNorth(), westLongitude, eastLongitude);
            myLatBand = latBand;
            myHSphere = latBand.getHemisphere();
            myZone = Integer.parseInt(zoneNumber);
            myName = zoneNumber + latBand.getLatitudeBand();
        }

        /**
         * Create a new GridGeometry.
         *
         * @param parentZone The containing LatBandZone.
         * @param swEasting The south west easting.
         * @param swNorthing The south west northing.
         * @param size The size of the grid geometry.
         * @return A new GridGeometry.
         */
        private GridGeometry createGridGeometry(UTMZone parentZone, double swEasting, double swNorthing, double size)
        {
            UTM utm = new UTM(parentZone.getZone(), parentZone.getHemisphere(), swEasting, swNorthing);
            GeographicPosition swPos = utm.convertToLatLon();

            utm = new UTM(parentZone.getZone(), parentZone.getHemisphere(), swEasting, swNorthing + size);
            GeographicPosition nwPos = utm.convertToLatLon();

            utm = new UTM(parentZone.getZone(), parentZone.getHemisphere(), swEasting + size, swNorthing + size);
            GeographicPosition nePos = utm.convertToLatLon();

            utm = new UTM(parentZone.getZone(), parentZone.getHemisphere(), swEasting + size, swNorthing);
            GeographicPosition sePos = utm.convertToLatLon();

            // As we get closer to the poles, there are pie shaped zones that
            // will have child grids outside the parent.
            // So do a quick check here to determine if this is the case.
            if (!parentZone.containsPoint(nwPos) && !parentZone.containsPoint(nePos) && !parentZone.containsPoint(sePos)
                    && !parentZone.containsPoint(swPos))
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(" created mgrs grid is outside parent, no need to add");
                }
                return null;
            }

            GridGeometry newGrid = new GridGeometry();

            GeographicBoundingBox bigBox = getBoundingBox();

            // Find the grid segments that will actually be used to display.
            newGrid.setGridSegments(MGRSCoreUtil.determineRenderableLines(bigBox, sePos, swPos, nwPos, nePos));

            // Find the corner positions that will be used for bounding box.
            GeographicPosition northWest = MGRSCoreUtil.checkPosition(bigBox, nwPos, nePos, swPos);
            GeographicPosition northEast = MGRSCoreUtil.checkPosition(bigBox, nePos, nwPos, sePos);
            GeographicPosition southEast = MGRSCoreUtil.checkPosition(bigBox, sePos, swPos, nePos);
            GeographicPosition southWest = MGRSCoreUtil.checkPosition(bigBox, swPos, sePos, nwPos);

            // Determine the name
            String mgrsName = determineName(parentZone, swEasting + size / 2, swNorthing + size / 2);
            newGrid.setText(mgrsName);

            newGrid.setZone(parentZone.getZone());
            newGrid.setLatitudeBand(parentZone.getLatBand().getLatitudeBand());
            newGrid.setHemisphere(parentZone.getHemisphere());
            newGrid.setSize(size);
            // This is the top level and has no parent
            // newGrid.setParent(null);

            newGrid.setSEPos(southEast);
            newGrid.setSWPos(southWest);
            newGrid.setNWPos(northWest);
            newGrid.setNEPos(northEast);

            MGRSCoreUtil.checkForTriangle(newGrid);

            newGrid.setOnBorder(MGRSCoreUtil.checkForBorder(newGrid, sePos, swPos, nwPos, nePos));

            if (newGrid.getSEPos() == null || newGrid.getSWPos() == null || newGrid.getNWPos() == null
                    || newGrid.getNEPos() == null)
            {
                LOGGER.warn(" A corner position is NULL, ignoring mgrs grid");
                return null;
            }

            GeographicPosition centerPosition = MGRSCoreUtil.getCenterPoint(newGrid.getSEPos(), newGrid.getSWPos(),
                    newGrid.getNWPos(), newGrid.getNEPos());
            newGrid.setCenterPosition(centerPosition);

            newGrid.setBoundingBox(
                    MGRSCoreUtil.createBoundingBox(newGrid.getSEPos(), newGrid.getSWPos(), newGrid.getNWPos(), newGrid.getNEPos()));

            newGrid.setSWEasting(swEasting);
            newGrid.setSWNorthing(swNorthing);

            return newGrid;
        }

        /**
         * Given the parent, easting, and northing values, determine the mgrs
         * name of this zone.
         *
         * @param parentZone The parent zone.
         * @param easting The easting value.
         * @param northing The northing value.
         * @return The MGRS name.
         */
        private String determineName(UTMZone parentZone, double easting, double northing)
        {
            UTM utm = new UTM(parentZone.getZone(), parentZone.getHemisphere(), easting, northing);
            MGRSConverter parser = new MGRSConverter();
            String mgrsString = parser.createString(utm);
            String mgrsName = "";
            if (mgrsString != null && mgrsString.length() > 5)
            {
                mgrsName = mgrsString.substring(3, 5);
            }
            return mgrsName;
        }

        /**
         * Create the mgrs squares.
         */
        private void findSquares()
        {
            /* Using latitude and longitude values I have, need to create UTM
             * from these and find easting and northing values to use in the
             * following to create MGRS grids. */

            // Find easting min/max values
            UTM utm = new UTM(getMinLatitude(), getMinLongitude());
            double minEasting = utm.getEasting();

            utm = new UTM(getMaxLatitude(), getMinLongitude());
            minEasting = utm.getEasting() < minEasting ? utm.getEasting() : minEasting;

            double maxEasting = 1000000 - minEasting;

            // Find northing min/max values
            double centerLongitude = (getMaxLongitude() + getMinLongitude()) / 2;
            utm = new UTM(getMinLatitude(), centerLongitude);
            double minNorthing = utm.getNorthing();

            utm = new UTM(getMaxLatitude(), centerLongitude);
            double maxNorthing = utm.getNorthing();

            // Account for latitude band M just below equator.
            final double number = 10e6;
            maxNorthing = maxNorthing == 0 ? number : maxNorthing;

            double startEasting = Math.floor(minEasting / MGRSConverter.ONEHT) * MGRSConverter.ONEHT;
            double startNorthing = Math.floor(minNorthing / MGRSConverter.ONEHT) * MGRSConverter.ONEHT;

            // Handle the distorted zones
            if ("32V".equals(myName))
            {
                maxNorthing += 20000;
            }
            if ("31X".equals(myName))
            {
                maxEasting += MGRSConverter.ONEHT;
            }

            for (double easting = startEasting; easting < maxEasting; easting += MGRSConverter.ONEHT)
            {
                for (double northing = startNorthing; northing < maxNorthing; northing += MGRSConverter.ONEHT)
                {
                    GridGeometry geom = createGridGeometry(this, easting, northing, MGRSConverter.ONEHT);

                    if (geom != null)
                    {
                        myMGRSSquares.put(geom.getText(), geom);
                    }
                }
            }
        }

        /**
         * Create a GeographicBoundingBox from our position.
         *
         * @return A GeographicBoundingBox.
         */
        public GeographicBoundingBox getBoundingBox()
        {
            // Create a bounding box for us.
            GeographicPosition bBoxSouthWest = new GeographicPosition(
                    LatLonAlt.createFromDegrees(getMinLatitude(), getMinLongitude()));
            GeographicPosition bBoxNorthEast = new GeographicPosition(
                    LatLonAlt.createFromDegrees(getMaxLatitude(), getMaxLongitude()));

            return new GeographicBoundingBox(bBoxSouthWest, bBoxNorthEast);
        }

        /**
         * Standard getter.
         *
         * @return The hemisphere.
         */
        public Hemisphere getHemisphere()
        {
            return myHSphere;
        }

        /**
         * Standard getter.
         *
         * @return The UTM zone data.
         */
        public LatitudeBandData getLatBand()
        {
            return myLatBand;
        }

        /**
         * Standard getter.
         *
         * @return The map of string to grid geometry.
         */
        public Map<String, GridGeometry> getMGRSSquares()
        {
            return myMGRSSquares;
        }

        /**
         * Standard getter.
         *
         * @return The name of this UTM zone.
         */
        public String getName()
        {
            return myName;
        }

        /**
         * Standard getter.
         *
         * @return The zone.
         */
        public int getZone()
        {
            return myZone;
        }

        /**
         * Draw the grid geometries in this zone.
         */
        private void displayGridGeometries()
        {
            if (getMGRSSquares().isEmpty())
            {
                findSquares();
            }

            for (Map.Entry<String, GridGeometry> mgrsEntry : getMGRSSquares().entrySet())
            {
                GridGeometry geom = mgrsEntry.getValue();

                geom.displayGrids(getViewer(), getProjection());
            }
        }

        /**
         * Determine if this geometry is in view by using bounding boxes. This
         * should only be called when we are sufficiently zoomed in so the
         * corners of the view-port are over land.
         *
         * @return True if geometry is in view, false otherwise.
         */
        private boolean isInView()
        {
            if (myViewBBox != null)
            {
                // check if we overlap
                return myViewBBox.overlaps(getBoundingBox(), 0.);
            }
            return false;
        }
    }
}
