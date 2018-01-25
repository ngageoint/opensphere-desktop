package io.opensphere.geopackage.export.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolylineGeometrySupport;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryCollection;
import mil.nga.wkb.geom.LineString;
import mil.nga.wkb.geom.MultiLineString;
import mil.nga.wkb.geom.MultiPoint;
import mil.nga.wkb.geom.MultiPolygon;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.geom.Polygon;

/**
 * Unit test for {@link GeometryExporter}.
 */
public class GeometryExporterTest
{
    /**
     * Tests importing a polygon.
     */
    @Test
    public void testExportGeometryCollection()
    {
        DefaultMapPolygonGeometrySupport polygon = new DefaultMapPolygonGeometrySupport();
        polygon.addLocation(LatLonAlt.createFromDegreesMeters(0d, 0d, 0d, ReferenceLevel.TERRAIN));
        polygon.addLocation(LatLonAlt.createFromDegreesMeters(0d, 10d, 5d, ReferenceLevel.TERRAIN));
        polygon.addLocation(LatLonAlt.createFromDegreesMeters(10d, 10d, 10d, ReferenceLevel.TERRAIN));
        polygon.addLocation(LatLonAlt.createFromDegreesMeters(10d, 0d, 15d, ReferenceLevel.TERRAIN));
        polygon.addLocation(LatLonAlt.createFromDegreesMeters(0d, 0d, 0d, ReferenceLevel.TERRAIN));

        DefaultMapPointGeometrySupport point = new DefaultMapPointGeometrySupport(
                LatLonAlt.createFromDegreesMeters(10d, 10d, 10d, ReferenceLevel.TERRAIN));

        polygon.addChild(point);

        EasyMockSupport support = new EasyMockSupport();

        MapDataElement element = createElement(support, polygon);

        support.replayAll();

        GeometryExporter exporter = new GeometryExporter();
        Geometry actual = exporter.convertGeometry(element);

        assertTrue(actual instanceof GeometryCollection);

        @SuppressWarnings("unchecked")
        GeometryCollection<Geometry> polygons = (GeometryCollection<Geometry>)actual;
        assertTrue(polygons.hasZ());
        assertFalse(polygons.hasM());

        Polygon actualPolygon = (Polygon)polygons.getGeometries().get(0);
        assertTrue(actualPolygon.hasZ());
        assertFalse(actualPolygon.hasM());

        List<Point> coords = actualPolygon.getRings().get(0).getPoints();

        assertEquals(5, coords.size());

        assertEquals(0, coords.get(0).getX(), 0d);
        assertEquals(0, coords.get(0).getY(), 0d);
        assertEquals(0, coords.get(0).getZ(), 0d);
        assertTrue(coords.get(0).hasZ());
        assertFalse(coords.get(0).hasM());

        assertEquals(10, coords.get(1).getX(), 0d);
        assertEquals(0, coords.get(1).getY(), 0d);
        assertEquals(5, coords.get(1).getZ(), 0d);
        assertTrue(coords.get(1).hasZ());
        assertFalse(coords.get(1).hasM());

        assertEquals(10, coords.get(2).getX(), 0d);
        assertEquals(10, coords.get(2).getY(), 0d);
        assertEquals(10, coords.get(2).getZ(), 0d);
        assertTrue(coords.get(2).hasZ());
        assertFalse(coords.get(2).hasM());

        assertEquals(0, coords.get(3).getX(), 0d);
        assertEquals(10, coords.get(3).getY(), 0d);
        assertEquals(15, coords.get(3).getZ(), 0d);
        assertTrue(coords.get(3).hasZ());
        assertFalse(coords.get(3).hasM());

        assertEquals(0, coords.get(4).getX(), 0d);
        assertEquals(0, coords.get(4).getY(), 0d);
        assertEquals(0, coords.get(4).getZ(), 0d);
        assertTrue(coords.get(4).hasZ());
        assertFalse(coords.get(4).hasM());

        Point actualPoint = (Point)polygons.getGeometries().get(1);

        assertEquals(10, actualPoint.getX(), 0d);
        assertEquals(10, actualPoint.getY(), 0d);
        assertEquals(10, actualPoint.getZ(), 0d);
        assertTrue(actualPoint.hasZ());
        assertFalse(actualPoint.hasM());

        support.verifyAll();
    }

    /**
     * Tests importing a polygon with holes.
     */
    @Test
    public void testExportHolyPolygon()
    {
        List<LatLonAlt> outerRing = New.list(LatLonAlt.createFromDegreesMeters(0, 0, 0, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(0, 10, 5, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(10, 10, 10, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(10, 0, 15, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(0, 0, 0, ReferenceLevel.TERRAIN));
        List<LatLonAlt> innerRing = New.list(LatLonAlt.createFromDegreesMeters(1, 1, 0, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(1, 9, 5, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(9, 9, 10, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(9, 1, 15, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(1, 1, 0, ReferenceLevel.TERRAIN));
        List<List<LatLonAlt>> holes = New.list();
        holes.add(innerRing);

        DefaultMapPolygonGeometrySupport polygon = new DefaultMapPolygonGeometrySupport(outerRing, holes);

        EasyMockSupport support = new EasyMockSupport();

        MapDataElement element = createElement(support, polygon);

        support.replayAll();

        GeometryExporter exporter = new GeometryExporter();

        Geometry actual = exporter.convertGeometry(element);

        assertTrue(actual instanceof Polygon);

        Polygon polygons = (Polygon)actual;
        assertTrue(polygons.hasZ());
        assertFalse(polygons.hasM());

        assertEquals(2, polygons.getRings().size());

        LineString actualOuter = polygons.getRings().get(0);
        assertTrue(actualOuter.hasZ());
        assertFalse(actualOuter.hasM());

        assertEquals(5, actualOuter.getPoints().size());

        assertEquals(0, actualOuter.getPoints().get(0).getX(), 0d);
        assertEquals(0, actualOuter.getPoints().get(0).getY(), 0d);
        assertEquals(0, actualOuter.getPoints().get(0).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(0).hasZ());
        assertFalse(actualOuter.getPoints().get(0).hasM());

        assertEquals(10, actualOuter.getPoints().get(1).getX(), 0d);
        assertEquals(0, actualOuter.getPoints().get(1).getY(), 0d);
        assertEquals(5, actualOuter.getPoints().get(1).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(1).hasZ());
        assertFalse(actualOuter.getPoints().get(1).hasM());

        assertEquals(10, actualOuter.getPoints().get(2).getX(), 0d);
        assertEquals(10, actualOuter.getPoints().get(2).getY(), 0d);
        assertEquals(10, actualOuter.getPoints().get(2).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(2).hasZ());
        assertFalse(actualOuter.getPoints().get(2).hasM());

        assertEquals(0, actualOuter.getPoints().get(3).getX(), 0d);
        assertEquals(10, actualOuter.getPoints().get(3).getY(), 0d);
        assertEquals(15, actualOuter.getPoints().get(3).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(3).hasZ());
        assertFalse(actualOuter.getPoints().get(3).hasM());

        assertEquals(0, actualOuter.getPoints().get(4).getX(), 0d);
        assertEquals(0, actualOuter.getPoints().get(4).getY(), 0d);
        assertEquals(0, actualOuter.getPoints().get(4).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(4).hasZ());
        assertFalse(actualOuter.getPoints().get(4).hasM());

        LineString actualInner = polygons.getRings().get(1);
        assertTrue(actualInner.hasZ());
        assertFalse(actualInner.hasM());

        assertEquals(5, actualInner.getPoints().size());

        assertEquals(1, actualInner.getPoints().get(0).getX(), 0d);
        assertEquals(1, actualInner.getPoints().get(0).getY(), 0d);
        assertEquals(0, actualInner.getPoints().get(0).getZ(), 0d);
        assertTrue(actualInner.getPoints().get(0).hasZ());
        assertFalse(actualInner.getPoints().get(0).hasM());

        assertEquals(9, actualInner.getPoints().get(1).getX(), 0d);
        assertEquals(1, actualInner.getPoints().get(1).getY(), 0d);
        assertEquals(5, actualInner.getPoints().get(1).getZ(), 0d);
        assertTrue(actualInner.getPoints().get(0).hasZ());
        assertFalse(actualInner.getPoints().get(0).hasM());

        assertEquals(9, actualInner.getPoints().get(2).getX(), 0d);
        assertEquals(9, actualInner.getPoints().get(2).getY(), 0d);
        assertEquals(10, actualInner.getPoints().get(2).getZ(), 0d);
        assertTrue(actualInner.getPoints().get(0).hasZ());
        assertFalse(actualInner.getPoints().get(0).hasM());

        assertEquals(1, actualInner.getPoints().get(3).getX(), 0d);
        assertEquals(9, actualInner.getPoints().get(3).getY(), 0d);
        assertEquals(15, actualInner.getPoints().get(3).getZ(), 0d);
        assertTrue(actualInner.getPoints().get(0).hasZ());
        assertFalse(actualInner.getPoints().get(0).hasM());

        assertEquals(1, actualInner.getPoints().get(4).getX(), 0d);
        assertEquals(1, actualInner.getPoints().get(4).getY(), 0d);
        assertEquals(0, actualInner.getPoints().get(4).getZ(), 0d);
        assertTrue(actualInner.getPoints().get(0).hasZ());
        assertFalse(actualInner.getPoints().get(0).hasM());

        support.verifyAll();
    }

    /**
     * Tests importing a line string.
     */
    @Test
    public void testExportLineString()
    {
        List<LatLonAlt> points = New.list(LatLonAlt.createFromDegreesMeters(0, 0, 0, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(0, 10, 5, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(10, 10, 10, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(10, 0, 15, ReferenceLevel.TERRAIN));
        DefaultMapPolylineGeometrySupport polyline = new DefaultMapPolylineGeometrySupport(points);

        EasyMockSupport support = new EasyMockSupport();

        MapDataElement element = createElement(support, polyline);

        support.replayAll();

        GeometryExporter exporter = new GeometryExporter();

        Geometry actual = exporter.convertGeometry(element);

        assertTrue(actual instanceof LineString);

        LineString actualLineString = (LineString)actual;
        assertTrue(actualLineString.hasZ());
        assertFalse(actualLineString.hasM());

        List<Point> coords = actualLineString.getPoints();

        assertEquals(4, coords.size());

        assertEquals(0, coords.get(0).getX(), 0d);
        assertEquals(0, coords.get(0).getY(), 0d);
        assertEquals(0, coords.get(0).getZ(), 0d);
        assertTrue(coords.get(0).hasZ());
        assertFalse(coords.get(0).hasM());

        assertEquals(10, coords.get(1).getX(), 0d);
        assertEquals(0, coords.get(1).getY(), 0d);
        assertEquals(5, coords.get(1).getZ(), 0d);
        assertTrue(coords.get(1).hasZ());
        assertFalse(coords.get(1).hasM());

        assertEquals(10, coords.get(2).getX(), 0d);
        assertEquals(10, coords.get(2).getY(), 0d);
        assertEquals(10, coords.get(2).getZ(), 0d);
        assertTrue(coords.get(2).hasZ());
        assertFalse(coords.get(2).hasM());

        assertEquals(0, coords.get(3).getX(), 0d);
        assertEquals(10, coords.get(3).getY(), 0d);
        assertEquals(15, coords.get(3).getZ(), 0d);
        assertTrue(coords.get(3).hasZ());
        assertFalse(coords.get(3).hasM());

        support.verifyAll();
    }

    /**
     * Tests importing a multi line string.
     */
    @Test
    public void testExportMultiLineString()
    {
        List<LatLonAlt> points = New.list(LatLonAlt.createFromDegreesMeters(0, 0, 0, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(0, 10, 5, ReferenceLevel.TERRAIN));
        List<LatLonAlt> points2 = New.list(LatLonAlt.createFromDegreesMeters(10, 10, 10, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(10, 0, 15, ReferenceLevel.TERRAIN));
        DefaultMapPolylineGeometrySupport polyline = new DefaultMapPolylineGeometrySupport(points);
        DefaultMapPolylineGeometrySupport child = new DefaultMapPolylineGeometrySupport(points2);
        polyline.addChild(child);

        EasyMockSupport support = new EasyMockSupport();

        MapDataElement element = createElement(support, polyline);

        support.replayAll();

        GeometryExporter exporter = new GeometryExporter();

        Geometry actual = exporter.convertGeometry(element);

        assertTrue(actual instanceof MultiLineString);

        MultiLineString actualLineString = (MultiLineString)actual;
        assertTrue(actualLineString.hasZ());
        assertFalse(actualLineString.hasM());

        List<LineString> lineStrings = actualLineString.getLineStrings();

        assertEquals(2, lineStrings.size());
        assertTrue(lineStrings.get(0).hasZ());
        assertFalse(lineStrings.get(0).hasM());

        List<Point> coords = lineStrings.get(0).getPoints();

        assertEquals(2, coords.size());

        assertEquals(0, coords.get(0).getX(), 0d);
        assertEquals(0, coords.get(0).getY(), 0d);
        assertEquals(0, coords.get(0).getZ(), 0d);
        assertTrue(coords.get(0).hasZ());
        assertFalse(coords.get(0).hasM());

        assertEquals(10, coords.get(1).getX(), 0d);
        assertEquals(0, coords.get(1).getY(), 0d);
        assertEquals(5, coords.get(1).getZ(), 0d);
        assertTrue(coords.get(1).hasZ());
        assertFalse(coords.get(1).hasM());

        coords = lineStrings.get(1).getPoints();
        assertTrue(lineStrings.get(1).hasZ());
        assertFalse(lineStrings.get(1).hasM());

        assertEquals(2, coords.size());

        assertEquals(10, coords.get(0).getX(), 0d);
        assertEquals(10, coords.get(0).getY(), 0d);
        assertEquals(10, coords.get(0).getZ(), 0d);
        assertTrue(coords.get(0).hasZ());
        assertFalse(coords.get(0).hasM());

        assertEquals(0, coords.get(1).getX(), 0d);
        assertEquals(10, coords.get(1).getY(), 0d);
        assertEquals(15, coords.get(1).getZ(), 0d);
        assertTrue(coords.get(1).hasZ());
        assertFalse(coords.get(1).hasM());

        support.verifyAll();
    }

    /**
     * Tests importing a multi point.
     */
    @Test
    public void testExportMultiPoint()
    {
        DefaultMapPointGeometrySupport point1 = new DefaultMapPointGeometrySupport(
                LatLonAlt.createFromDegreesMeters(0, 0, 0, ReferenceLevel.TERRAIN));
        DefaultMapPointGeometrySupport point2 = new DefaultMapPointGeometrySupport(
                LatLonAlt.createFromDegreesMeters(0, 10, 5, ReferenceLevel.TERRAIN));
        DefaultMapPointGeometrySupport point3 = new DefaultMapPointGeometrySupport(
                LatLonAlt.createFromDegreesMeters(10, 10, 10, ReferenceLevel.TERRAIN));
        DefaultMapPointGeometrySupport point4 = new DefaultMapPointGeometrySupport(
                LatLonAlt.createFromDegreesMeters(10, 0, 15, ReferenceLevel.TERRAIN));

        point1.addChild(point2);
        point1.addChild(point3);
        point1.addChild(point4);

        EasyMockSupport support = new EasyMockSupport();

        MapDataElement element = createElement(support, point1);

        support.replayAll();

        GeometryExporter exporter = new GeometryExporter();

        Geometry actual = exporter.convertGeometry(element);

        assertTrue(actual instanceof MultiPoint);

        MultiPoint actualPoints = (MultiPoint)actual;
        assertTrue(actualPoints.hasZ());
        assertFalse(actualPoints.hasM());

        List<Point> coords = actualPoints.getPoints();

        assertEquals(4, coords.size());

        assertEquals(0, coords.get(0).getX(), 0d);
        assertEquals(0, coords.get(0).getY(), 0d);
        assertEquals(0, coords.get(0).getZ(), 0d);
        assertTrue(coords.get(0).hasZ());
        assertFalse(coords.get(0).hasM());

        assertEquals(10, coords.get(1).getX(), 0d);
        assertEquals(0, coords.get(1).getY(), 0d);
        assertEquals(5, coords.get(1).getZ(), 0d);
        assertTrue(coords.get(1).hasZ());
        assertFalse(coords.get(1).hasM());

        assertEquals(10, coords.get(2).getX(), 0d);
        assertEquals(10, coords.get(2).getY(), 0d);
        assertEquals(10, coords.get(2).getZ(), 0d);
        assertTrue(coords.get(2).hasZ());
        assertFalse(coords.get(2).hasM());

        assertEquals(0, coords.get(3).getX(), 0d);
        assertEquals(10, coords.get(3).getY(), 0d);
        assertEquals(15, coords.get(3).getZ(), 0d);
        assertTrue(coords.get(3).hasZ());
        assertFalse(coords.get(3).hasM());

        support.verifyAll();
    }

    /**
     * Tests importing a multi polygon.
     */
    @Test
    public void testExportMultiPolygon()
    {
        List<LatLonAlt> outerRing = New.list(LatLonAlt.createFromDegreesMeters(0, 0, 0, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(0, 10, 5, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(10, 10, 10, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(10, 0, 15, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(0, 0, 0, ReferenceLevel.TERRAIN));
        List<LatLonAlt> innerRing = New.list(LatLonAlt.createFromDegreesMeters(1, 1, 0, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(1, 9, 5, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(9, 9, 10, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(9, 1, 15, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(1, 1, 0, ReferenceLevel.TERRAIN));

        DefaultMapPolygonGeometrySupport polygon = new DefaultMapPolygonGeometrySupport(outerRing, New.list());
        DefaultMapPolygonGeometrySupport polygon2 = new DefaultMapPolygonGeometrySupport(innerRing, New.list());
        polygon.addChild(polygon2);

        EasyMockSupport support = new EasyMockSupport();

        MapDataElement element = createElement(support, polygon);

        support.replayAll();

        GeometryExporter exporter = new GeometryExporter();

        Geometry actual = exporter.convertGeometry(element);

        assertTrue(actual instanceof MultiPolygon);

        MultiPolygon polygons = (MultiPolygon)actual;
        assertTrue(polygons.hasZ());
        assertFalse(polygons.hasM());

        assertEquals(2, polygons.getPolygons().size());

        Polygon actualPolygon1 = polygons.getPolygons().get(0);
        assertEquals(1, actualPolygon1.getRings().size());
        assertTrue(actualPolygon1.hasZ());
        assertFalse(actualPolygon1.hasM());

        LineString actualOuter = actualPolygon1.getRings().get(0);
        assertTrue(actualOuter.hasZ());
        assertFalse(actualOuter.hasM());

        assertEquals(5, actualOuter.getPoints().size());

        assertEquals(0, actualOuter.getPoints().get(0).getX(), 0d);
        assertEquals(0, actualOuter.getPoints().get(0).getY(), 0d);
        assertEquals(0, actualOuter.getPoints().get(0).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(0).hasZ());
        assertFalse(actualOuter.getPoints().get(0).hasM());

        assertEquals(10, actualOuter.getPoints().get(1).getX(), 0d);
        assertEquals(0, actualOuter.getPoints().get(1).getY(), 0d);
        assertEquals(5, actualOuter.getPoints().get(1).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(1).hasZ());
        assertFalse(actualOuter.getPoints().get(1).hasM());

        assertEquals(10, actualOuter.getPoints().get(2).getX(), 0d);
        assertEquals(10, actualOuter.getPoints().get(2).getY(), 0d);
        assertEquals(10, actualOuter.getPoints().get(2).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(2).hasZ());
        assertFalse(actualOuter.getPoints().get(2).hasM());

        assertEquals(0, actualOuter.getPoints().get(3).getX(), 0d);
        assertEquals(10, actualOuter.getPoints().get(3).getY(), 0d);
        assertEquals(15, actualOuter.getPoints().get(3).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(3).hasZ());
        assertFalse(actualOuter.getPoints().get(3).hasM());

        assertEquals(0, actualOuter.getPoints().get(4).getX(), 0d);
        assertEquals(0, actualOuter.getPoints().get(4).getY(), 0d);
        assertEquals(0, actualOuter.getPoints().get(4).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(4).hasZ());
        assertFalse(actualOuter.getPoints().get(4).hasM());

        Polygon actualPolygon2 = polygons.getPolygons().get(1);
        assertEquals(1, actualPolygon2.getRings().size());
        assertTrue(actualPolygon2.hasZ());
        assertFalse(actualPolygon2.hasM());

        LineString actualInner = actualPolygon2.getRings().get(0);
        assertTrue(actualInner.hasZ());
        assertFalse(actualInner.hasM());

        assertEquals(5, actualInner.getPoints().size());

        assertEquals(1, actualInner.getPoints().get(0).getX(), 0d);
        assertEquals(1, actualInner.getPoints().get(0).getY(), 0d);
        assertEquals(0, actualInner.getPoints().get(0).getZ(), 0d);
        assertTrue(actualInner.getPoints().get(0).hasZ());
        assertFalse(actualInner.getPoints().get(0).hasM());

        assertEquals(9, actualInner.getPoints().get(1).getX(), 0d);
        assertEquals(1, actualInner.getPoints().get(1).getY(), 0d);
        assertEquals(5, actualInner.getPoints().get(1).getZ(), 0d);
        assertTrue(actualInner.getPoints().get(1).hasZ());
        assertFalse(actualInner.getPoints().get(1).hasM());

        assertEquals(9, actualInner.getPoints().get(2).getX(), 0d);
        assertEquals(9, actualInner.getPoints().get(2).getY(), 0d);
        assertEquals(10, actualInner.getPoints().get(2).getZ(), 0d);
        assertTrue(actualInner.getPoints().get(2).hasZ());
        assertFalse(actualInner.getPoints().get(2).hasM());

        assertEquals(1, actualInner.getPoints().get(3).getX(), 0d);
        assertEquals(9, actualInner.getPoints().get(3).getY(), 0d);
        assertEquals(15, actualInner.getPoints().get(3).getZ(), 0d);
        assertTrue(actualInner.getPoints().get(3).hasZ());
        assertFalse(actualInner.getPoints().get(3).hasM());

        assertEquals(1, actualInner.getPoints().get(4).getX(), 0d);
        assertEquals(1, actualInner.getPoints().get(4).getY(), 0d);
        assertEquals(0, actualInner.getPoints().get(4).getZ(), 0d);
        assertTrue(actualInner.getPoints().get(4).hasZ());
        assertFalse(actualInner.getPoints().get(4).hasM());

        support.verifyAll();
    }

    /**
     * Tests importing a row without a geometry.
     */
    @Test
    public void testExportNullGeometryData()
    {
        EasyMockSupport support = new EasyMockSupport();

        MapDataElement element = createElement(support, null);

        support.replayAll();

        GeometryExporter exporter = new GeometryExporter();

        Geometry actual = exporter.convertGeometry(element);

        assertNull(actual);

        support.verifyAll();
    }

    /**
     * Tests importing a point.
     */
    @Test
    public void testExportPoint()
    {
        DefaultMapPointGeometrySupport point = new DefaultMapPointGeometrySupport(
                LatLonAlt.createFromDegreesMeters(10, 5, 15, ReferenceLevel.TERRAIN));

        EasyMockSupport support = new EasyMockSupport();

        MapDataElement element = createElement(support, point);

        support.replayAll();

        GeometryExporter exporter = new GeometryExporter();

        Geometry actual = exporter.convertGeometry(element);

        assertTrue(actual instanceof Point);

        Point actualPoint = (Point)actual;

        assertEquals(5, actualPoint.getX(), 0d);
        assertEquals(10, actualPoint.getY(), 0d);
        assertEquals(15, actualPoint.getZ(), 0d);
        assertTrue(actualPoint.hasZ());
        assertFalse(actualPoint.hasM());

        support.verifyAll();
    }

    /**
     * Tests importing a point without altitude.
     */
    @Test
    public void testExportPointNoZ()
    {
        DefaultMapPointGeometrySupport point = new DefaultMapPointGeometrySupport(LatLonAlt.createFromDegrees(10, 5));

        EasyMockSupport support = new EasyMockSupport();

        MapDataElement element = createElement(support, point);

        support.replayAll();

        GeometryExporter exporter = new GeometryExporter();

        Geometry actual = exporter.convertGeometry(element);

        assertTrue(actual instanceof Point);

        Point actualPoint = (Point)actual;

        assertEquals(5, actualPoint.getX(), 0d);
        assertEquals(10, actualPoint.getY(), 0d);
        assertEquals(0, actualPoint.getZ(), 0d);
        assertTrue(actualPoint.hasZ());
        assertFalse(actualPoint.hasM());

        support.verifyAll();
    }

    /**
     * Tests importing a polygon.
     */
    @Test
    public void testExportPolygon()
    {
        List<LatLonAlt> outerRing = New.list(LatLonAlt.createFromDegreesMeters(0, 0, 0, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(0, 10, 5, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(10, 10, 10, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(10, 0, 15, ReferenceLevel.TERRAIN),
                LatLonAlt.createFromDegreesMeters(0, 0, 0, ReferenceLevel.TERRAIN));

        DefaultMapPolygonGeometrySupport polygon = new DefaultMapPolygonGeometrySupport(outerRing, New.list());

        EasyMockSupport support = new EasyMockSupport();

        MapDataElement element = createElement(support, polygon);

        support.replayAll();

        GeometryExporter exporter = new GeometryExporter();

        Geometry actual = exporter.convertGeometry(element);

        assertTrue(actual instanceof Polygon);

        Polygon actualPolygon = (Polygon)actual;
        assertEquals(1, actualPolygon.getRings().size());
        assertTrue(actualPolygon.hasZ());
        assertFalse(actualPolygon.hasM());

        LineString actualOuter = actualPolygon.getRings().get(0);
        assertTrue(actualOuter.hasZ());
        assertFalse(actualOuter.hasM());

        assertEquals(5, actualOuter.getPoints().size());

        assertEquals(0, actualOuter.getPoints().get(0).getX(), 0d);
        assertEquals(0, actualOuter.getPoints().get(0).getY(), 0d);
        assertEquals(0, actualOuter.getPoints().get(0).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(0).hasZ());
        assertFalse(actualOuter.getPoints().get(0).hasM());

        assertEquals(10, actualOuter.getPoints().get(1).getX(), 0d);
        assertEquals(0, actualOuter.getPoints().get(1).getY(), 0d);
        assertEquals(5, actualOuter.getPoints().get(1).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(1).hasZ());
        assertFalse(actualOuter.getPoints().get(1).hasM());

        assertEquals(10, actualOuter.getPoints().get(2).getX(), 0d);
        assertEquals(10, actualOuter.getPoints().get(2).getY(), 0d);
        assertEquals(10, actualOuter.getPoints().get(2).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(2).hasZ());
        assertFalse(actualOuter.getPoints().get(2).hasM());

        assertEquals(0, actualOuter.getPoints().get(3).getX(), 0d);
        assertEquals(10, actualOuter.getPoints().get(3).getY(), 0d);
        assertEquals(15, actualOuter.getPoints().get(3).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(3).hasZ());
        assertFalse(actualOuter.getPoints().get(3).hasM());

        assertEquals(0, actualOuter.getPoints().get(4).getX(), 0d);
        assertEquals(0, actualOuter.getPoints().get(4).getY(), 0d);
        assertEquals(0, actualOuter.getPoints().get(4).getZ(), 0d);
        assertTrue(actualOuter.getPoints().get(4).hasZ());
        assertFalse(actualOuter.getPoints().get(4).hasM());

        support.verifyAll();
    }

    /**
     * Creates a mock for {@link MapDataElement}.
     *
     * @param support Used to create the mock.
     * @param geomSupport The {@link MapGeometrySupport} the mock should return.
     * @return The mocked {@link MapDataElement}.
     */
    private MapDataElement createElement(EasyMockSupport support, MapGeometrySupport geomSupport)
    {
        MapDataElement mapElement = support.createMock(MapDataElement.class);

        EasyMock.expect(mapElement.getMapGeometrySupport()).andReturn(geomSupport);

        return mapElement;
    }
}
