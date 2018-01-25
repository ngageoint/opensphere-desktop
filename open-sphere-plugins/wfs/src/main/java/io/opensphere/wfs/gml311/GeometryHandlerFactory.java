package io.opensphere.wfs.gml311;

import org.apache.log4j.Logger;

/**
 * A factory for creating GeometryHandler objects.
 */
public final class GeometryHandlerFactory
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(GeometryHandlerFactory.class);

    /** The Constant tag for GML Points. */
    public static final String GML_POINT_TAG = "Point";

    /** The Constant tag for GML LineStrings. */
    public static final String GML_LINESTRING_TAG = "LineString";

    /** The Constant tag for GML Polygons. */
    public static final String GML_POLYGON_TAG = "Polygon";

    /** The Constant tag for GML MultiPoints. */
    public static final String GML_MULTIPOINT_TAG = "MultiPoint";

    /** The Constant tag for GML MultiLineStrings. */
    public static final String GML_MULTILINESTRING_TAG = "MultiLineString";

    /** The Constant tag for GML MultiCurves. */
    public static final String GML_MULTICURVE_TAG = "MultiCurve";

    /** The Constant tag for GML MultiPoints. */
    public static final String GML_MULTIPOLYGON_TAG = "MultiPolygon";

    /** The Constant tag for GML MultiPoints. */
    public static final String GML_MULTISURFACE_TAG = "MultiSurface";

    /** The Constant tag for GML MultiPoints. */
    public static final String GML_MULTIGEOMETRY_TAG = "MultiGeometry";

    /**
     * Creates the appropriate feature handler based on the Geometry tag.
     *
     * @param geometryTag the geometry tag
     * @param isLatBeforeLon flag indicating position order in points
     * @return the right feature handler for the passed-in geometry
     */
    public static AbstractGmlGeometryHandler getGeometryHandler(String geometryTag, boolean isLatBeforeLon)
    {
        if (geometryTag.equalsIgnoreCase(GML_POINT_TAG))
        {
            return new GmlPointHandler(geometryTag, isLatBeforeLon);
        }
        else if (geometryTag.equalsIgnoreCase(GML_LINESTRING_TAG))
        {
            return new GmlLinestringHandler(geometryTag, isLatBeforeLon);
        }
        else if (geometryTag.equalsIgnoreCase(GML_POLYGON_TAG))
        {
            return new GmlPolygonHandler(geometryTag, isLatBeforeLon);
        }
        else if (geometryTag.equalsIgnoreCase(GML_MULTIPOINT_TAG))
        {
            return new GmlMultiPointHandler(geometryTag, isLatBeforeLon);
        }
        else if (geometryTag.equalsIgnoreCase(GML_MULTILINESTRING_TAG))
        {
            return new GmlMultiLinestringHandler(geometryTag, isLatBeforeLon);
        }
        else if (geometryTag.equalsIgnoreCase(GML_MULTICURVE_TAG))
        {
            return new GmlMultiCurveHandler(geometryTag, isLatBeforeLon);
        }
        else if (geometryTag.equalsIgnoreCase(GML_MULTIPOLYGON_TAG))
        {
            return new GmlMultiPolygonHandler(geometryTag, isLatBeforeLon);
        }
        else if (geometryTag.equalsIgnoreCase(GML_MULTISURFACE_TAG))
        {
            return new GmlMultiSurfaceHandler(geometryTag, isLatBeforeLon);
        }
        else if (geometryTag.equalsIgnoreCase(GML_MULTIGEOMETRY_TAG))
        {
            return new GmlMultiGeometryHandler(geometryTag, isLatBeforeLon);
        }
        else
        {
            LOGGER.warn("Received unsupported geometry type [" + geometryTag + "] from Server.");
        }
        return null;
    }

    /**
     * Forbid public instantiation of static class.
     */
    private GeometryHandlerFactory()
    {
    }
}
