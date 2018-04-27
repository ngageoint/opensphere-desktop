package io.opensphere.kml.mantle.controller;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.PolyStyle;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Style;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.util.KMLDataRegistryHelper;
import io.opensphere.kml.common.util.KMLLinkHelper;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapPointGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapIconGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;

/**
 * Builds Mantle geometries from KML geometries.
 */
@SuppressWarnings("PMD.GodClass")
public class KMLGeometryBuilder
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(KMLGeometryBuilder.class);

    /** The broken URL. */
    private static final URL BROKEN_URL;

    static
    {
        URL brokenURL;
        try
        {
            brokenURL = new URL("http://broken.com");
        }
        catch (MalformedURLException e)
        {
            brokenURL = null;
            LOGGER.error(e.getMessage());
        }
        BROKEN_URL = brokenURL;
    }

    /** The data registry. */
    private final DataRegistry myDataRegistry;

    /** The mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

//    /**
//     * The server provider registry.
//     */
//    private final ServerProviderRegistry myServerRegistry;

    /** The KML data source. */
    private final KMLDataSource myDataSource;

    /** The icon href to URL map. */
    private final Map<String, URL> myIconHrefToURLMap;

    /**
     * Constructor.
     *
     * @param dataRegistry The data registry
     * @param serverRegistry The server provider registry.
     * @param mantleToolbox The mantle toolbox
     * @param dataSource the data source
     */
    public KMLGeometryBuilder(DataRegistry dataRegistry, ServerProviderRegistry serverRegistry, MantleToolbox mantleToolbox,
            KMLDataSource dataSource)
    {
        myDataRegistry = dataRegistry;
        myMantleToolbox = mantleToolbox;
        // myServerRegistry = serverRegistry;
        myDataSource = dataSource;
        myIconHrefToURLMap = New.map(KmlIcons.getKmlIconMap(mantleToolbox.getIconRegistry()));
    }

    /**
     * Convert an Geometry to a MapGeometrySupport.
     *
     * @param geometry The Geometry
     * @param style The style
     * @param highlightStyle The highlight style
     * @param useSimpleGeom Whether to use simple geometries where possible
     * @return The equivalent MapGeometrySupport
     */
    public MapGeometrySupport createMapGeometrySupport(Geometry geometry, Style style, Style highlightStyle,
            boolean useSimpleGeom)
    {
        MapGeometrySupport mapGeomSupport = null;

        // Spatial
        if (geometry instanceof Point)
        {
            Point point = (Point)geometry;
            AltitudeMode altitudeMode = getUserAltitudeMode(point.getAltitudeMode());
            List<LatLonAlt> locations = KMLSpatialTemporalUtils.convertCoordinates(point.getCoordinates(), altitudeMode);
            boolean isExtrude = isExtrude(point.isExtrude(), altitudeMode);

            // Create an icon geometry support if possible
            if (myDataSource.isUseIcons())
            {
                mapGeomSupport = createIconMapGeometrySupport(point, style, highlightStyle);
            }

            // Create a normal MapGeometrySupport if no icon
            if (mapGeomSupport == null)
            {
                mapGeomSupport = createMapGeometrySupport(locations, null, style, useSimpleGeom, isExtrude, false, null);
            }
        }
        else if (geometry instanceof LineString)
        {
            LineString lineString = (LineString)geometry;
            AltitudeMode altitudeMode = getUserAltitudeMode(lineString.getAltitudeMode());
            boolean tessellate = lineString.isTessellate() != null && lineString.isTessellate().booleanValue();
            List<LatLonAlt> locations = KMLSpatialTemporalUtils.convertCoordinates(lineString.getCoordinates(), altitudeMode);
            boolean isExtrude = isExtrude(lineString.isExtrude(), altitudeMode);
            mapGeomSupport = createMapGeometrySupport(locations, null, style, useSimpleGeom, isExtrude, tessellate,
                    DefaultMapPolylineGeometrySupport.class);
        }
        else if (geometry instanceof LinearRing)
        {
            LinearRing linearRing = (LinearRing)geometry;
            AltitudeMode altitudeMode = getUserAltitudeMode(linearRing.getAltitudeMode());
            boolean tessellate = linearRing.isTessellate() != null && linearRing.isTessellate().booleanValue();
            List<LatLonAlt> locations = KMLSpatialTemporalUtils.convertCoordinates(linearRing.getCoordinates(), altitudeMode);
            boolean isExtrude = isExtrude(linearRing.isExtrude(), altitudeMode);
            mapGeomSupport = createMapGeometrySupport(locations, null, style, useSimpleGeom, isExtrude, tessellate,
                    DefaultMapPolylineGeometrySupport.class);
        }
        else if (geometry instanceof Polygon)
        {
            Polygon polygon = (Polygon)geometry;
            mapGeomSupport = createMapPolygonSupport(polygon, style, useSimpleGeom);
        }
        else if (geometry instanceof MultiGeometry)
        {
            MultiGeometry multiGeometry = (MultiGeometry)geometry;
            mapGeomSupport = createMapMultiGeometrySupport(multiGeometry, style, highlightStyle);
        }

        return mapGeomSupport;
    }

    /**
     * Helper method to create a new DefaultMapPolygonGeometrySupport.
     *
     * @param location The location
     * @param useSimpleGeom Whether to use a simple geometry
     * @param style The style
     * @return The new MapPointGeometrySupport
     */
    private static MapPointGeometrySupport createPointSupport(LatLonAlt location, final boolean useSimpleGeom, final Style style)
    {
        MapPointGeometrySupport mapGeomSupport;
        if (useSimpleGeom)
        {
            mapGeomSupport = new SimpleMapPointGeometrySupport(location);
        }
        else
        {
            mapGeomSupport = new DefaultMapPointGeometrySupport(location);
        }
        if (style != null && style.getIconStyle() != null)
        {
            Color iconColor = KMLSpatialTemporalUtils.convertColor(style.getIconStyle().getColor());
            if (iconColor != null)
            {
                mapGeomSupport.setColor(iconColor, null);
            }

            if (style.getIconStyle().getScale() > 0)
            {
                final float minScale = .75f;
                float scale = Math.max(minScale, (float)style.getIconStyle().getScale());
                mapGeomSupport.setScale(scale);
            }
            else
            {
                mapGeomSupport = null;
            }
        }
        return mapGeomSupport;
    }

    /**
     * Helper method to create a new DefaultMapPolylineGeometrySupport.
     *
     * @param locations The locations
     * @param style The style
     * @param tessellate the tessellate
     * @return The new DefaultMapPolylineGeometrySupport
     */
    private static DefaultMapPolylineGeometrySupport createPolylineSupport(List<LatLonAlt> locations, final Style style,
            boolean tessellate)
    {
        DefaultMapPolylineGeometrySupport mapGeomSupport = new DefaultMapPolylineGeometrySupport(locations);
        mapGeomSupport.setLineWidth(1);
        mapGeomSupport.setLineType(tessellate ? LineType.GREAT_CIRCLE : LineType.STRAIGHT_LINE_IGNORE_TERRAIN);
        if (style != null && style.getLineStyle() != null)
        {
            int width = (int)style.getLineStyle().getWidth();
            mapGeomSupport.setLineWidth(width > 0 ? width : 1);

            Color lineColor = KMLSpatialTemporalUtils.convertColor(style.getLineStyle().getColor());
            if (lineColor != null)
            {
                mapGeomSupport.setColor(lineColor, null);
            }
        }
        return mapGeomSupport;
    }

    /**
     * Extrudes a point.
     *
     * @param location The point location
     * @param geomSupport The AbstractMapGeometrySupport
     * @param style The style
     * @param tessellate the tessellate
     * @return geomSupport
     */
    private static AbstractMapGeometrySupport extrudePoint(final LatLonAlt location, final AbstractMapGeometrySupport geomSupport,
            final Style style, final boolean tessellate)
    {
        List<LatLonAlt> locations = New.list(2);
        locations.add(location);
        locations.add(getGroundLocation(location));
        AbstractMapGeometrySupport lineGeomSupport = createPolylineSupport(locations, style, tessellate);

        geomSupport.addChild(lineGeomSupport);
        return geomSupport;
    }

    /**
     * Converts a LatLonAlt to a LatLonAlt at the same location but on the
     * ground.
     *
     * @param location The location
     * @return The ground location
     */
    private static LatLonAlt getGroundLocation(LatLonAlt location)
    {
        return LatLonAlt.createFromDegreesMeters(location.getLatD(), location.getLonD(), 0.0, location.getAltitudeReference());
    }

    /**
     * Determines whether this geometry can be extruded.
     *
     * @param isExtrude The isExtrude of the geometry
     * @param altitudeMode The altitudeMode of the geometry
     * @return True if it can be extruded, false otherwise
     */
    private static boolean isExtrude(Boolean isExtrude, AltitudeMode altitudeMode)
    {
        boolean altitude = altitudeMode == AltitudeMode.RELATIVE_TO_GROUND || altitudeMode == AltitudeMode.RELATIVE_TO_SEA_FLOOR
                || altitudeMode == AltitudeMode.ABSOLUTE;
        return isExtrude != null && isExtrude.booleanValue() && altitude;
    }

    /**
     * Creates a MapIconGeometrySupport from a Geometry.
     *
     * @param point The point
     * @param style The style
     * @param highlightStyle The highlight style
     * @return The MapGeometrySupport
     */
    private MapGeometrySupport createIconMapGeometrySupport(Point point, Style style, Style highlightStyle)
    {
        DefaultMapIconGeometrySupport mapGeomSupport = null;

        boolean notNull = style != null && style.getIconStyle() != null && style.getIconStyle().getIcon() != null
                && style.getIconStyle().getIcon().getHref() != null;
        if (notNull && style.getIconStyle().getScale() > 0)
        {
            String iconHref = StringUtilities.trim(style.getIconStyle().getIcon().getHref());
            URL iconURL = myIconHrefToURLMap.get(iconHref);

            // No URL in the map, create one and add it to the map
            if (iconURL == null)
            {
                iconURL = getIconUrl(style, iconHref);
                if (iconURL != null)
                {
                    myIconHrefToURLMap.put(iconHref, iconURL);
                }
            }
            mapGeomSupport = createSupport(point, style, highlightStyle, iconURL);
        }
        return mapGeomSupport;
    }

    /**
     * Creates the map icon geometry support.
     *
     * @param point The point
     * @param style The style
     * @param highlightStyle the highlight style
     * @param iconURL the icon's url
     * @return the MapGeometrySupport
     */
    private DefaultMapIconGeometrySupport createSupport(Point point, Style style, Style highlightStyle, URL iconURL)
    {
        DefaultMapIconGeometrySupport mapGeomSupport = null;
        // Create the Icon MapGeometrySupport
        if (iconURL != null && !iconURL.getHost().equals(BROKEN_URL.getHost()))
        {
            AltitudeMode altitudeMode = getUserAltitudeMode(point.getAltitudeMode());
            List<LatLonAlt> locations = KMLSpatialTemporalUtils.convertCoordinates(point.getCoordinates(), altitudeMode);
            if (locations.size() == 1)
            {
                final int defaultSize = 32;

                int highlightSize = defaultSize;
                if (highlightStyle != null && highlightStyle.getIconStyle() != null
                        && highlightStyle.getIconStyle().getScale() > style.getIconStyle().getScale())
                {
                    double highlightScale = highlightStyle.getIconStyle().getScale() / style.getIconStyle().getScale();
                    highlightSize = (int)Math.round(highlightSize * highlightScale);
                }

                mapGeomSupport = new DefaultMapIconGeometrySupport(locations.get(0), iconURL.toExternalForm(), defaultSize,
                        highlightSize);
                double overScale = (double)highlightSize / defaultSize;
                mapGeomSupport.setImageProcessor(KMLImageProcessorHelper.getIconImageProcessor(style.getIconStyle(), overScale));
                mapGeomSupport.setScaleFunction(KMLMantleUtilities.getScaleFunction(myDataSource));

                Color iconColor = null;
                if (style.getIconStyle() != null)
                {
                    iconColor = KMLSpatialTemporalUtils.convertColor(style.getIconStyle().getColor());
                }
                if (iconColor != null)
                {
                    mapGeomSupport.setColor(iconColor, null);
                }

                boolean isExtrude = isExtrude(point.isExtrude(), altitudeMode);
                if (isExtrude)
                {
                    extrudePoint(locations.get(0), mapGeomSupport, style, false);
                }
            }
        }
        return mapGeomSupport;
    }

    /**
     * Lower-level method to create a MapGeometrySupport from a list of
     * LatLonAlts.
     *
     * @param locations The locations
     * @param holes The interior rings if this is a polygon
     * @param style The style
     * @param useSimpleGeom Whether to use simple geometries where possible
     * @param isExtrude The isExtrude of the geometry
     * @param tessellate the tessellate
     * @param geomClass The MapGeometrySupport class to use for geometries
     * @return The MapGeometrySupport
     */
    private MapGeometrySupport createMapGeometrySupport(List<LatLonAlt> locations, Collection<List<LatLonAlt>> holes,
            final Style style, final boolean useSimpleGeom, boolean isExtrude, boolean tessellate,
            Class<? extends MapGeometrySupport> geomClass)
    {
        MapGeometrySupport mapGeomSupport = null;

        if (locations.isEmpty())
        {
            return mapGeomSupport;
        }

        // Handle extrude
        if (isExtrude)
        {
            mapGeomSupport = extrudeLocations(locations, style, tessellate);
        }
        // Handle point
        else if (locations.size() == 1)
        {
            mapGeomSupport = createPointSupport(locations.get(0), useSimpleGeom, style);
        }
        // Handle polygon/polyline
        else
        {
            if (geomClass == DefaultMapPolygonGeometrySupport.class)
            {
                // GE uses straight lines if fill is on, and ignores the
                // tessellate flag
                mapGeomSupport = createPolygonSupport(locations, holes, style,
                        myDataSource.isPolygonsFilled() ? LineType.STRAIGHT_LINE : LineType.GREAT_CIRCLE);
            }
            else if (geomClass == DefaultMapPolylineGeometrySupport.class)
            {
                mapGeomSupport = createPolylineSupport(locations, style, tessellate);
            }
        }
        return mapGeomSupport;
    }

    /**
     * Creates a MapGeometrySupport for a MultiGeometry.
     *
     * @param multiGeometry The MultiGeometry
     * @param style The style
     * @param highlightStyle The highlight style
     * @return The MapGeometrySupport
     */
    private MapGeometrySupport createMapMultiGeometrySupport(MultiGeometry multiGeometry, Style style, Style highlightStyle)
    {
        MapGeometrySupport mapGeomSupport = null;
        if (!multiGeometry.getGeometry().isEmpty())
        {
            List<MapGeometrySupport> mantleGeoms = multiGeometry.getGeometry().stream()
                    .map(g -> createMapGeometrySupport(g, style, highlightStyle, false)).filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!mantleGeoms.isEmpty())
            {
                // Use the first geometry as the parent
                mapGeomSupport = mantleGeoms.get(0);

                // Add the remainder as children of the first geometry
                if (mapGeomSupport instanceof AbstractMapGeometrySupport)
                {
                    for (int i = 1; i < mantleGeoms.size(); i++)
                    {
                        ((AbstractMapGeometrySupport)mapGeomSupport).addChild(mantleGeoms.get(i));
                    }

                    /* HACK - Work around a limitation in Mantle where child
                     * colors are not used. If the parent has no color, use the
                     * color of the first child with a color. */
                    Color mantleDefault = Color.WHITE;
                    if (mantleDefault.equals(mapGeomSupport.getColor()) && mapGeomSupport.getChildren() != null)
                    {
                        for (MapGeometrySupport child : mapGeomSupport.getChildren())
                        {
                            if (!mantleDefault.equals(child.getColor()))
                            {
                                mapGeomSupport.setColor(child.getColor(), this);
                                break;
                            }
                        }
                    }
                }
                else
                {
                    LOGGER.warn("Unsupported first element in MultiGeometry, MapGeometrySupport not created.");
                }
            }
        }
        return mapGeomSupport;
    }

    /**
     * Creates a MapGeometrySupport for a Polygon.
     *
     * @param polygon The Polygon
     * @param style The style
     * @param useSimpleGeom Whether to use simple geometries where possible
     * @return The MapGeometrySupport
     */
    private MapGeometrySupport createMapPolygonSupport(final Polygon polygon, final Style style, final boolean useSimpleGeom)
    {
        AltitudeMode altitudeMode = getUserAltitudeMode(polygon.getAltitudeMode());
        boolean isExtrude = isExtrude(polygon.isExtrude(), altitudeMode);
        boolean isTessellate = polygon.isTessellate() != null && polygon.isTessellate().booleanValue();

        // Create the outer boundary
        List<LatLonAlt> outerRing = KMLSpatialTemporalUtils
                .convertCoordinates(polygon.getOuterBoundaryIs().getLinearRing().getCoordinates(), altitudeMode);

        // Create the inner boundaries
        Collection<List<LatLonAlt>> innerRings = New.collection(polygon.getInnerBoundaryIs().size());
        for (Boundary innerBoundary : polygon.getInnerBoundaryIs())
        {
            if (!innerBoundary.getLinearRing().getCoordinates().isEmpty())
            {
                List<LatLonAlt> ring = KMLSpatialTemporalUtils.convertCoordinates(innerBoundary.getLinearRing().getCoordinates(),
                        altitudeMode);
                innerRings.add(ring);
            }
        }

        return createMapGeometrySupport(outerRing, innerRings, style, useSimpleGeom, isExtrude, isTessellate,
                DefaultMapPolygonGeometrySupport.class);
    }

    /**
     * Helper method to create a new DefaultMapPolygonGeometrySupport.
     *
     * @param locations The locations
     * @param holes The interior rings of the polygon
     * @param style The style
     * @param lineType The type for the lines in the polygon.
     * @return The new DefaultMapPolygonGeometrySupport
     */
    private DefaultMapPolygonGeometrySupport createPolygonSupport(List<LatLonAlt> locations, Collection<List<LatLonAlt>> holes,
            Style style, LineType lineType)
    {
        DefaultMapPolygonGeometrySupport mapGeomSupport = new DefaultMapPolygonGeometrySupport(locations, holes);
        mapGeomSupport.setLineWidth(1);
        mapGeomSupport.setFilled(myDataSource.isPolygonsFilled());
        mapGeomSupport.setLineType(lineType);

        if (style != null)
        {
            if (style.getLineStyle() != null)
            {
                int width = (int)style.getLineStyle().getWidth();
                mapGeomSupport.setLineWidth(width > 0 ? width : 1);

                Color lineColor = KMLSpatialTemporalUtils.convertColor(style.getLineStyle().getColor());
                if (lineColor != null)
                {
                    mapGeomSupport.setColor(lineColor, null);
                }
            }
            if (style.getPolyStyle() != null)
            {
                PolyStyle polyStyle = style.getPolyStyle();

                boolean isLineDrawn = polyStyle.isOutline() == null || polyStyle.isOutline().booleanValue();
                mapGeomSupport.setLineDrawn(isLineDrawn);

                // If there's no outline, default the polygon fill on so that
                // something is shown.
                boolean isFilled = polyStyle.isFill() != null ? polyStyle.isFill().booleanValue()
                        : !isLineDrawn || !StringUtils.isEmpty(polyStyle.getColor()) || myDataSource.isPolygonsFilled();
                mapGeomSupport.setFilled(isFilled);

                Color fillColor = KMLSpatialTemporalUtils.convertColor(polyStyle.getColor());
                if (fillColor != null)
                {
                    mapGeomSupport.setFillColor(fillColor);
                }
            }
        }
        return mapGeomSupport;
    }

    /**
     * Extrudes line segment specified by two points.
     *
     * @param location1 The first point location
     * @param location2 The second point location
     * @param style The style
     * @param tessellate the tessellate
     * @return The Polygon MapGeometrySupport
     */
    private AbstractMapGeometrySupport extrudeLineSegment(final LatLonAlt location1, final LatLonAlt location2, final Style style,
            final boolean tessellate)
    {
        List<LatLonAlt> locations = New.list(5);
        locations.add(location1);
        locations.add(location2);
        locations.add(getGroundLocation(location2));
        locations.add(getGroundLocation(location1));
        locations.add(location1);

        return createPolygonSupport(locations, null, style,
                tessellate ? LineType.GREAT_CIRCLE : LineType.STRAIGHT_LINE_IGNORE_TERRAIN);
    }

    /**
     * Extrudes a list of locations.
     *
     * @param locations The list of locations
     * @param style The style
     * @param tessellate the tessellate
     * @return The Polygon MapGeometrySupport with potentially multiple children
     */
    private MapGeometrySupport extrudeLocations(final List<LatLonAlt> locations, final Style style, boolean tessellate)
    {
        AbstractMapGeometrySupport mapGeomSupport = null;
        if (locations.size() == 1)
        {
            LatLonAlt location = locations.get(0);
            AbstractMapGeometrySupport point = (AbstractMapGeometrySupport)createPointSupport(location, false, style);
            mapGeomSupport = extrudePoint(location, point, style, tessellate);
        }
        else if (locations.size() > 1)
        {
            mapGeomSupport = extrudeLineSegment(locations.get(0), locations.get(1), style, tessellate);
            for (int i = 1, n = locations.size() - 1; i < n; i++)
            {
                mapGeomSupport.addChild(extrudeLineSegment(locations.get(i), locations.get(i + 1), style, tessellate));
            }
        }
        return mapGeomSupport;
    }

    /**
     * Gets an icon URL for the given style and href.
     *
     * @param style The style
     * @param iconHref The icon href
     * @return The icon URL
     */
    private URL getIconUrl(Style style, String iconHref)
    {
        URL iconURL = null;

        InputStream iconInputStream = KMLDataRegistryHelper.queryAndReturn(myDataRegistry, myDataSource, iconHref);

        // It's coming from the KMZCache, add it to the Mantle icon
        // cache
        if (iconInputStream != null)
        {
            try
            {
                String iconFileName = iconHref.replace('/', '_');
                iconURL = myMantleToolbox.getIconRegistry().getIconCache().cacheIcon(iconInputStream, iconFileName, false);
            }
            catch (IOException e)
            {
                LOGGER.warn(e.getMessage());
            }
            finally
            {
                try
                {
                    iconInputStream.close();
                }
                catch (IOException e)
                {
                    LOGGER.warn("Failed to close input stream: " + e, e);
                }
            }
        }
        // Use the URL directly
        else
        {
            iconURL = KMLLinkHelper.getFullUrlFromBasicLink(style.getIconStyle().getIcon(), myDataSource);

            // HACKISH If we can't read the image, set the URL to broken
            // so that we don't create an icon geometry, and end
            // up creating a dot instead of a broken image on the map.
            // This holds up the KMLPlugin thread, but is arguably
            // better than the exceptions that would ensue, and ugly
            // icons on the map.
//            boolean canReadIcon = true;
//            try
//            {
//                if (UrlUtilities.isFile(iconURL))
//                {
//                    if (ImageIO.read(iconURL) == null)
//                    {
//                        canReadIcon = false;
//                    }
//                }
//                else
//                {
//                    HttpServer server = myServerRegistry.getProvider(HttpServer.class).getServer(iconURL);
//                    ResponseValues response = new ResponseValues();
//                    try (InputStream stream = server.sendGet(iconURL, response))
//                    {
//                        if (response.getResponseCode() != HttpURLConnection.HTTP_OK)
//                        {
//                            canReadIcon = false;
//                        }
//                    }
//                }
//            }
//            catch (IOException | URISyntaxException e)
//            {
//                canReadIcon = false;
//            }
//            if (!canReadIcon)
//            {
//                LOGGER.warn("Unable to read icon, " + iconURL + ", using dots instead");
//                iconURL = BROKEN_URL;
//            }
        }

        return iconURL;
    }

    /**
     * Converts the given altitude mode to the user's preferred altitude mode
     * for this data source.
     *
     * @param altitudeMode The altitude mode
     * @return The equivalent preferred altitude mode of the user
     */
    private AltitudeMode getUserAltitudeMode(AltitudeMode altitudeMode)
    {
        return myDataSource.isClampToTerrain() ? AltitudeMode.CLAMP_TO_GROUND : altitudeMode;
    }
}
