package io.opensphere.geopackage.mantle;

import java.awt.Color;
import java.io.Serializable;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.geopackage.model.GeoPackageColumns;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.util.jts.JTSGeometryToGeometrySupportFactory;

/**
 * Creates the {@link MapGeometrySupport} object for a specified geopackage row.
 */
public class MapGeometryCreator
{
    /**
     * Creates the {@link MapGeometrySupport} for the specified row.
     *
     * @param row The geopackage row to create the geometry support for.
     * @return The {@link MapGeometrySupport} or null if the geomtry doesn't
     *         exist or is unsupported.
     */
    public MapGeometrySupport createGeometrySupport(Map<String, Serializable> row)
    {
        MapGeometrySupport support = null;

        Geometry geometry = (Geometry)row.get(GeoPackageColumns.GEOMETRY_COLUMN);

        if (geometry != null)
        {
            support = JTSGeometryToGeometrySupportFactory.createGeometrySupportFromWKTGeometry(geometry, Color.white);
        }

        return support;
    }
}
