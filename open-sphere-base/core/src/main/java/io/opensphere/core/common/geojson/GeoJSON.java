package io.opensphere.core.common.geojson;

public class GeoJSON
{
    public static enum Type
    {
        FeatureCollection("FeatureCollection"), Feature("Feature"), Point("Point"), MultiPoint("MultiPoint"), LineString(
                "LineString"), MultiLineString("MultiLineString"), Polygon(
                        "Polygon"), MultiPolygon("MultiPolygon"), GeometryCollection("GeometryCollection");

        private String type;

        private Type(String type)
        {
            this.type = type;
        }

        public String getType()
        {
            return type;
        }
    }

}
