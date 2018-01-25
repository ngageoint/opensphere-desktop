package io.opensphere.geopackage.model;

/**
 * Contains some constant column names used when importing geopackage features.
 */
public final class GeoPackageColumns
{
    /**
     * The column that contains a geopackage feature's WKT geometry.
     */
    public static final String GEOMETRY_COLUMN = "Geometry";

    /**
     * The primary key column for feature tables.
     */
    public static final String ID_COLUMN = "ID";

    /**
     * Not constructible.
     */
    private GeoPackageColumns()
    {
    }
}
