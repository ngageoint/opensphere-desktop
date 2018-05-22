package io.opensphere.search.mapzen.model.geojson;

public enum GeoJSONType
{
    FeatureCollection("FeatureCollection"),
    Feature("Feature"),
    Point("Point"),
    MultiPoint("MultiPoint"),
    LineString("LineString"),
    MultiLineString("MultiLineString"),
    Polygon("Polygon"),
    MultiPolygon("MultiPolygon"),
    GeometryCollection("GeometryCollection");

    private String type;

    private GeoJSONType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
