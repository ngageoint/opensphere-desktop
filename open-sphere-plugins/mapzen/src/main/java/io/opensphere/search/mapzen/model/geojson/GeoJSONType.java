package io.opensphere.search.mapzen.model.geojson;

/** Type for GeoJSON. */
public enum GeoJSONType
{
    /** A collection of features. */
    FeatureCollection("FeatureCollection"),

    /** A feature. */
    Feature("Feature"),

    /** A point. */
    Point("Point"),

    /** Multiple points. */
    MultiPoint("MultiPoint"),

    /** A single-line String. */
    LineString("LineString"),

    /** A multi-line String. */
    MultiLineString("MultiLineString"),

    /** A polygon. */
    Polygon("Polygon"),

    /** Multiple polygons. */
    MultiPolygon("MultiPolygon"),

    /** A collection of geometries. */
    GeometryCollection("GeometryCollection");

    /** The type. */
    private String type;

    /**
     * Constructs a GeoJSONType enum.
     * 
     * @param type the type
     */
    private GeoJSONType(String type)
    {
        this.type = type;
    }

    /**
     * Retrieves the type.
     * 
     * @return {@link #type}
     */
    public String getType()
    {
        return type;
    }
}
