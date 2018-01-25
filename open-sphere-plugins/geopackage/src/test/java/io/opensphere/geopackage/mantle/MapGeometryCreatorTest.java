package io.opensphere.geopackage.mantle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Color;
import java.io.Serializable;
import java.util.Map;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageColumns;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;

/**
 * Test for the {@link MapGeometryCreator}.
 */
public class MapGeometryCreatorTest
{
    /**
     * Tests creating geometry support for a point.
     */
    @Test
    public void testCreateGeometrySupport()
    {
        Map<String, Serializable> row = New.map();
        row.put(GeoPackageColumns.GEOMETRY_COLUMN, new GeometryFactory().createPoint(new Coordinate(10, 11, 12)));

        MapGeometryCreator creator = new MapGeometryCreator();
        MapGeometrySupport support = creator.createGeometrySupport(row);

        SimpleMapPointGeometrySupport simpleSupport = (SimpleMapPointGeometrySupport)support;

        assertEquals(Color.white, simpleSupport.getColor());
        assertEquals(11, simpleSupport.getLocation().getLatD(), 0d);
        assertEquals(10, simpleSupport.getLocation().getLonD(), 0d);
        assertEquals(12, simpleSupport.getLocation().getAltitude().getMeters(), 0d);
    }

    /**
     * Tests creating geometry support with no geometry.
     */
    @Test
    public void testCreateGeometrySupportNoGeom()
    {
        Map<String, Serializable> row = New.map();

        MapGeometryCreator creator = new MapGeometryCreator();
        MapGeometrySupport support = creator.createGeometrySupport(row);

        assertNull(support);
    }
}
