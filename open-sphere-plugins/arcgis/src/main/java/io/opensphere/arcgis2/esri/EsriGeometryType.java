package io.opensphere.arcgis2.esri;

/** The Enum for ESRI Geometry Types. */
public enum EsriGeometryType
{
    /** The ESRI type for envelope (Bounding box) geometries. */
    esriGeometryEnvelope("esriGeometryEnvelope"),

    /** The ESRI type for multi-point geometries. */
    esriGeometryMultipoint("esriGeometryMultipoint"),

    /** The ESRI type for point geometries. */
    esriGeometryPoint("esriGeometryPoint"),

    /** The ESRI type for polygon geometries. */
    esriGeometryPolygon("esriGeometryPolygon"),

    /** The ESRI type for polyline geometries. */
    esriGeometryPolyline("esriGeometryPolyline"),

    /** The default ESRI type for unknown geometry types. */
    esriGeometryUnknown("");

    /** My title. */
    private final String myTitle;

    /**
     * Instantiates a new ESRI geometry type.
     *
     * @param title the title
     */
    EsriGeometryType(String title)
    {
        myTitle = title;
    }

    @Override
    public String toString()
    {
        return myTitle;
    }
}
