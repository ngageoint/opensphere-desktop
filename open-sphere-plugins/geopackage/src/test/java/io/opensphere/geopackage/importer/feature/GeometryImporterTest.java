package io.opensphere.geopackage.importer.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Map;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.features.user.MockFeatureRow;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.wkb.geom.CircularString;
import mil.nga.wkb.geom.CompoundCurve;
import mil.nga.wkb.geom.GeometryType;

/**
 * Tests the {@link GeometryImporter} class.
 */
public class GeometryImporterTest
{
    /**
     * The tables geometry column.
     */
    private static final String ourGeometryColumn = "geometry";

    /**
     * The tables primary key column name.
     */
    private static final String ourKeyColumn = "theKey";

    /**
     * The test table name.
     */
    private static final String ourTableName = "Table";

    /**
     * Tests importing a circular string.
     */
    @Test
    public void testImportCircularString()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        CircularString geometry = new CircularString(true, false);
        mil.nga.wkb.geom.Point point1 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 0));
        point1.setZ(0d);
        mil.nga.wkb.geom.Point point2 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 0));
        point2.setZ(5d);
        mil.nga.wkb.geom.Point point3 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 10));
        point3.setZ(10d);
        mil.nga.wkb.geom.Point point4 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 10));
        point4.setZ(15d);

        geometry.addPoint(point1);
        geometry.addPoint(point2);
        geometry.addPoint(point3);
        geometry.addPoint(point4);

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, geometry);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        Geometry actual = (Geometry)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);

        assertTrue(actual instanceof LineString);

        LineString actualLineString = (LineString)actual;

        Coordinate[] coords = actualLineString.getCoordinates();

        assertEquals(4, coords.length);

        assertEquals(0, coords[0].x, 0d);
        assertEquals(0, coords[0].y, 0d);
        assertEquals(0, coords[0].z, 0d);

        assertEquals(10, coords[1].x, 0d);
        assertEquals(0, coords[1].y, 0d);
        assertEquals(5, coords[1].z, 0d);

        assertEquals(10, coords[2].x, 0d);
        assertEquals(10, coords[2].y, 0d);
        assertEquals(10, coords[2].z, 0d);

        assertEquals(0, coords[3].x, 0d);
        assertEquals(10, coords[3].y, 0d);
        assertEquals(15, coords[3].z, 0d);
    }

    /**
     * Tests importing a compund curve.
     */
    @Test
    public void testImportCompoundCurve()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        CompoundCurve geometry = new CompoundCurve(true, false);
        mil.nga.wkb.geom.Point point1 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 0));
        point1.setZ(0d);
        mil.nga.wkb.geom.Point point2 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 0));
        point2.setZ(5d);
        mil.nga.wkb.geom.Point point3 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 10));
        point3.setZ(10d);
        mil.nga.wkb.geom.Point point4 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 10));
        point4.setZ(15d);

        mil.nga.wkb.geom.LineString lineString1 = new mil.nga.wkb.geom.LineString(true, false);
        lineString1.addPoint(point1);
        lineString1.addPoint(point2);

        mil.nga.wkb.geom.LineString lineString2 = new mil.nga.wkb.geom.LineString(true, false);
        lineString2.addPoint(point3);
        lineString2.addPoint(point4);

        geometry.addLineString(lineString1);
        geometry.addLineString(lineString2);

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, geometry);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        Geometry actual = (Geometry)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);

        assertTrue(actual instanceof MultiLineString);

        MultiLineString actualLineString = (MultiLineString)actual;

        Coordinate[] coords = actualLineString.getCoordinates();

        assertEquals(4, coords.length);

        assertEquals(0, coords[0].x, 0d);
        assertEquals(0, coords[0].y, 0d);
        assertEquals(0, coords[0].z, 0d);

        assertEquals(10, coords[1].x, 0d);
        assertEquals(0, coords[1].y, 0d);
        assertEquals(5, coords[1].z, 0d);

        assertEquals(10, coords[2].x, 0d);
        assertEquals(10, coords[2].y, 0d);
        assertEquals(10, coords[2].z, 0d);

        assertEquals(0, coords[3].x, 0d);
        assertEquals(10, coords[3].y, 0d);
        assertEquals(15, coords[3].z, 0d);
    }

    /**
     * Tests importing a polygon.
     */
    @Test
    public void testImportGeometryCollection()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        mil.nga.wkb.geom.Polygon polygon = new mil.nga.wkb.geom.Polygon(true, false);
        mil.nga.wkb.geom.Point point1 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 0));
        point1.setZ(0d);
        mil.nga.wkb.geom.Point point2 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 0));
        point2.setZ(5d);
        mil.nga.wkb.geom.Point point3 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 10));
        point3.setZ(10d);
        mil.nga.wkb.geom.Point point4 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 10));
        point4.setZ(15d);

        mil.nga.wkb.geom.LineString lineString = new mil.nga.wkb.geom.LineString(true, false);
        lineString.addPoint(point1);
        lineString.addPoint(point2);
        lineString.addPoint(point3);
        lineString.addPoint(point4);
        lineString.addPoint(point1);
        polygon.addRing(lineString);

        mil.nga.wkb.geom.Point point = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 10));
        point.setZ(10d);

        mil.nga.wkb.geom.GeometryCollection<mil.nga.wkb.geom.Geometry> geometryCollection = new mil.nga.wkb.geom.GeometryCollection<>(
                true, false);
        geometryCollection.addGeometry(polygon);
        geometryCollection.addGeometry(point);

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, geometryCollection);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        Geometry actual = (Geometry)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);

        assertTrue(actual instanceof GeometryCollection);

        GeometryCollection polygons = (GeometryCollection)actual;

        Polygon actualPolygon = (Polygon)polygons.getGeometryN(0);

        Coordinate[] coords = actualPolygon.getCoordinates();

        assertEquals(5, coords.length);

        assertEquals(0, coords[0].x, 0d);
        assertEquals(0, coords[0].y, 0d);
        assertEquals(0, coords[0].z, 0d);

        assertEquals(10, coords[1].x, 0d);
        assertEquals(0, coords[1].y, 0d);
        assertEquals(5, coords[1].z, 0d);

        assertEquals(10, coords[2].x, 0d);
        assertEquals(10, coords[2].y, 0d);
        assertEquals(10, coords[2].z, 0d);

        assertEquals(0, coords[3].x, 0d);
        assertEquals(10, coords[3].y, 0d);
        assertEquals(15, coords[3].z, 0d);

        assertEquals(0, coords[4].x, 0d);
        assertEquals(0, coords[4].y, 0d);
        assertEquals(0, coords[4].z, 0d);

        Point actualPoint = (Point)polygons.getGeometryN(1);

        assertEquals(10, actualPoint.getCoordinate().x, 0d);
        assertEquals(10, actualPoint.getCoordinate().y, 0d);
        assertEquals(10, actualPoint.getCoordinate().z, 0d);
    }

    /**
     * Tests importing a polygon with holes.
     */
    @Test
    public void testImportHolyPolygon()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        mil.nga.wkb.geom.Polygon polygon = new mil.nga.wkb.geom.Polygon(true, false);
        mil.nga.wkb.geom.Point point1 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 0));
        point1.setZ(0d);
        mil.nga.wkb.geom.Point point2 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 0));
        point2.setZ(5d);
        mil.nga.wkb.geom.Point point3 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 10));
        point3.setZ(10d);
        mil.nga.wkb.geom.Point point4 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 10));
        point4.setZ(15d);

        mil.nga.wkb.geom.LineString lineString1 = new mil.nga.wkb.geom.LineString(true, false);
        lineString1.addPoint(point1);
        lineString1.addPoint(point2);
        lineString1.addPoint(point3);
        lineString1.addPoint(point4);
        lineString1.addPoint(point1);
        polygon.addRing(lineString1);

        mil.nga.wkb.geom.Point point5 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 1, 1));
        point5.setZ(0d);
        mil.nga.wkb.geom.Point point6 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 9, 1));
        point6.setZ(5d);
        mil.nga.wkb.geom.Point point7 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 9, 9));
        point7.setZ(10d);
        mil.nga.wkb.geom.Point point8 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 1, 9));
        point8.setZ(15d);

        mil.nga.wkb.geom.LineString lineString2 = new mil.nga.wkb.geom.LineString(true, false);
        lineString2.addPoint(point5);
        lineString2.addPoint(point6);
        lineString2.addPoint(point7);
        lineString2.addPoint(point8);
        lineString2.addPoint(point5);
        polygon.addRing(lineString2);

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, polygon);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        Geometry actual = (Geometry)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);

        assertTrue(actual instanceof Polygon);

        Polygon polygons = (Polygon)actual;

        Coordinate[] coords = polygons.getCoordinates();

        assertEquals(10, coords.length);

        assertEquals(0, coords[0].x, 0d);
        assertEquals(0, coords[0].y, 0d);
        assertEquals(0, coords[0].z, 0d);

        assertEquals(10, coords[1].x, 0d);
        assertEquals(0, coords[1].y, 0d);
        assertEquals(5, coords[1].z, 0d);

        assertEquals(10, coords[2].x, 0d);
        assertEquals(10, coords[2].y, 0d);
        assertEquals(10, coords[2].z, 0d);

        assertEquals(0, coords[3].x, 0d);
        assertEquals(10, coords[3].y, 0d);
        assertEquals(15, coords[3].z, 0d);

        assertEquals(0, coords[4].x, 0d);
        assertEquals(0, coords[4].y, 0d);
        assertEquals(0, coords[4].z, 0d);

        assertEquals(1, coords[5].x, 0d);
        assertEquals(1, coords[5].y, 0d);
        assertEquals(0, coords[5].z, 0d);

        assertEquals(9, coords[6].x, 0d);
        assertEquals(1, coords[6].y, 0d);
        assertEquals(5, coords[6].z, 0d);

        assertEquals(9, coords[7].x, 0d);
        assertEquals(9, coords[7].y, 0d);
        assertEquals(10, coords[7].z, 0d);

        assertEquals(1, coords[8].x, 0d);
        assertEquals(9, coords[8].y, 0d);
        assertEquals(15, coords[8].z, 0d);

        assertEquals(1, coords[9].x, 0d);
        assertEquals(1, coords[9].y, 0d);
        assertEquals(0, coords[9].z, 0d);
    }

    /**
     * Tests importing a line string.
     */
    @Test
    public void testImportLineString()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        mil.nga.wkb.geom.Point point1 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 0));
        point1.setZ(0d);
        mil.nga.wkb.geom.Point point2 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 0));
        point2.setZ(5d);
        mil.nga.wkb.geom.Point point3 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 10));
        point3.setZ(10d);
        mil.nga.wkb.geom.Point point4 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 10));
        point4.setZ(15d);

        mil.nga.wkb.geom.LineString lineString = new mil.nga.wkb.geom.LineString(true, false);
        lineString.addPoint(point1);
        lineString.addPoint(point2);
        lineString.addPoint(point3);
        lineString.addPoint(point4);

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, lineString);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        Geometry actual = (Geometry)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);

        assertTrue(actual instanceof LineString);

        LineString actualLineString = (LineString)actual;

        Coordinate[] coords = actualLineString.getCoordinates();

        assertEquals(4, coords.length);

        assertEquals(0, coords[0].x, 0d);
        assertEquals(0, coords[0].y, 0d);
        assertEquals(0, coords[0].z, 0d);

        assertEquals(10, coords[1].x, 0d);
        assertEquals(0, coords[1].y, 0d);
        assertEquals(5, coords[1].z, 0d);

        assertEquals(10, coords[2].x, 0d);
        assertEquals(10, coords[2].y, 0d);
        assertEquals(10, coords[2].z, 0d);

        assertEquals(0, coords[3].x, 0d);
        assertEquals(10, coords[3].y, 0d);
        assertEquals(15, coords[3].z, 0d);
    }

    /**
     * Tests importing a multi line string.
     */
    @Test
    public void testImportMultiLineString()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        mil.nga.wkb.geom.MultiLineString geometry = new mil.nga.wkb.geom.MultiLineString(true, false);
        mil.nga.wkb.geom.Point point1 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 0));
        point1.setZ(0d);
        mil.nga.wkb.geom.Point point2 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 0));
        point2.setZ(5d);
        mil.nga.wkb.geom.Point point3 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 10));
        point3.setZ(10d);
        mil.nga.wkb.geom.Point point4 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 10));
        point4.setZ(15d);

        mil.nga.wkb.geom.LineString lineString1 = new mil.nga.wkb.geom.LineString(true, false);
        lineString1.addPoint(point1);
        lineString1.addPoint(point2);

        mil.nga.wkb.geom.LineString lineString2 = new mil.nga.wkb.geom.LineString(true, false);
        lineString2.addPoint(point3);
        lineString2.addPoint(point4);

        geometry.addLineString(lineString1);
        geometry.addLineString(lineString2);

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, geometry);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        Geometry actual = (Geometry)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);

        assertTrue(actual instanceof MultiLineString);

        MultiLineString actualLineString = (MultiLineString)actual;

        Coordinate[] coords = actualLineString.getCoordinates();

        assertEquals(4, coords.length);

        assertEquals(0, coords[0].x, 0d);
        assertEquals(0, coords[0].y, 0d);
        assertEquals(0, coords[0].z, 0d);

        assertEquals(10, coords[1].x, 0d);
        assertEquals(0, coords[1].y, 0d);
        assertEquals(5, coords[1].z, 0d);

        assertEquals(10, coords[2].x, 0d);
        assertEquals(10, coords[2].y, 0d);
        assertEquals(10, coords[2].z, 0d);

        assertEquals(0, coords[3].x, 0d);
        assertEquals(10, coords[3].y, 0d);
        assertEquals(15, coords[3].z, 0d);
    }

    /**
     * Tests importing a multi point.
     */
    @Test
    public void testImportMultiPoint()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        mil.nga.wkb.geom.Point point1 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 0));
        point1.setZ(0d);
        mil.nga.wkb.geom.Point point2 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 0));
        point2.setZ(5d);
        mil.nga.wkb.geom.Point point3 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 10));
        point3.setZ(10d);
        mil.nga.wkb.geom.Point point4 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 10));
        point4.setZ(15d);

        mil.nga.wkb.geom.MultiPoint multiPoint = new mil.nga.wkb.geom.MultiPoint(true, false);
        multiPoint.addPoint(point1);
        multiPoint.addPoint(point2);
        multiPoint.addPoint(point3);
        multiPoint.addPoint(point4);

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, multiPoint);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        Geometry actual = (Geometry)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);

        assertTrue(actual instanceof MultiPoint);

        MultiPoint actualLineString = (MultiPoint)actual;

        Coordinate[] coords = actualLineString.getCoordinates();

        assertEquals(4, coords.length);

        assertEquals(0, coords[0].x, 0d);
        assertEquals(0, coords[0].y, 0d);
        assertEquals(0, coords[0].z, 0d);

        assertEquals(10, coords[1].x, 0d);
        assertEquals(0, coords[1].y, 0d);
        assertEquals(5, coords[1].z, 0d);

        assertEquals(10, coords[2].x, 0d);
        assertEquals(10, coords[2].y, 0d);
        assertEquals(10, coords[2].z, 0d);

        assertEquals(0, coords[3].x, 0d);
        assertEquals(10, coords[3].y, 0d);
        assertEquals(15, coords[3].z, 0d);
    }

    /**
     * Tests importing a multi polygon.
     */
    @Test
    public void testImportMultiPolygon()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        mil.nga.wkb.geom.MultiPolygon geometry = new mil.nga.wkb.geom.MultiPolygon(true, false);
        mil.nga.wkb.geom.Polygon polygon1 = new mil.nga.wkb.geom.Polygon(true, false);
        mil.nga.wkb.geom.Point point1 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 0));
        point1.setZ(0d);
        mil.nga.wkb.geom.Point point2 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 0));
        point2.setZ(5d);
        mil.nga.wkb.geom.Point point3 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 10));
        point3.setZ(10d);
        mil.nga.wkb.geom.Point point4 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 10));
        point4.setZ(15d);

        mil.nga.wkb.geom.LineString lineString1 = new mil.nga.wkb.geom.LineString(true, false);
        lineString1.addPoint(point1);
        lineString1.addPoint(point2);
        lineString1.addPoint(point3);
        lineString1.addPoint(point4);
        lineString1.addPoint(point1);
        polygon1.addRing(lineString1);

        mil.nga.wkb.geom.Polygon polygon2 = new mil.nga.wkb.geom.Polygon(true, false);
        mil.nga.wkb.geom.Point point5 = toMercator.transform(new mil.nga.wkb.geom.Point(false, false, 0, 0));
        mil.nga.wkb.geom.Point point6 = toMercator.transform(new mil.nga.wkb.geom.Point(false, false, -10, 0));
        mil.nga.wkb.geom.Point point7 = toMercator.transform(new mil.nga.wkb.geom.Point(false, false, -10, 10));
        mil.nga.wkb.geom.Point point8 = toMercator.transform(new mil.nga.wkb.geom.Point(false, false, 0, 10));

        mil.nga.wkb.geom.LineString lineString2 = new mil.nga.wkb.geom.LineString(true, false);
        lineString2.addPoint(point5);
        lineString2.addPoint(point6);
        lineString2.addPoint(point7);
        lineString2.addPoint(point8);
        lineString2.addPoint(point5);
        polygon2.addRing(lineString2);

        geometry.addPolygon(polygon1);
        geometry.addPolygon(polygon2);

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, geometry);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        Geometry actual = (Geometry)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);

        assertTrue(actual instanceof MultiPolygon);

        MultiPolygon polygons = (MultiPolygon)actual;

        Coordinate[] coords = polygons.getCoordinates();

        assertEquals(10, coords.length);

        assertEquals(0, coords[0].x, 0d);
        assertEquals(0, coords[0].y, 0d);
        assertEquals(0, coords[0].z, 0d);

        assertEquals(10, coords[1].x, 0d);
        assertEquals(0, coords[1].y, 0d);
        assertEquals(5, coords[1].z, 0d);

        assertEquals(10, coords[2].x, 0d);
        assertEquals(10, coords[2].y, 0d);
        assertEquals(10, coords[2].z, 0d);

        assertEquals(0, coords[3].x, 0d);
        assertEquals(10, coords[3].y, 0d);
        assertEquals(15, coords[3].z, 0d);

        assertEquals(0, coords[4].x, 0d);
        assertEquals(0, coords[4].y, 0d);
        assertEquals(0, coords[4].z, 0d);

        assertEquals(0, coords[5].x, 0d);
        assertEquals(0, coords[5].y, 0d);
        assertEquals(0, coords[5].z, 0d);

        assertEquals(-10, coords[6].x, 0d);
        assertEquals(0, coords[6].y, 0d);
        assertEquals(0, coords[6].z, 0d);

        assertEquals(-10, coords[7].x, 0d);
        assertEquals(10, coords[7].y, 0d);
        assertEquals(0, coords[7].z, 0d);

        assertEquals(0, coords[8].x, 0d);
        assertEquals(10, coords[8].y, 0d);
        assertEquals(0, coords[8].z, 0d);

        assertEquals(0, coords[9].x, 0d);
        assertEquals(0, coords[9].y, 0d);
        assertEquals(0, coords[9].z, 0d);
    }

    /**
     * Tests importing a row without a geometry.
     */
    @Test
    public void testImportNullGeometryData()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, null);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        assertNull(importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN));
    }

    /**
     * Tests importing a point.
     */
    @Test
    public void testImportPoint()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        mil.nga.wkb.geom.Point point = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 10));
        point.setZ(10d);

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, point);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        Geometry actual = (Geometry)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);

        assertTrue(actual instanceof Point);

        Point actualPoint = (Point)actual;

        assertEquals(10, actualPoint.getCoordinate().x, 0d);
        assertEquals(10, actualPoint.getCoordinate().y, 0d);
        assertEquals(10, actualPoint.getCoordinate().z, 0d);
    }

    /**
     * Tests importing a point without altitude.
     */
    @Test
    public void testImportPointNoZ()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        mil.nga.wkb.geom.Point point = toMercator.transform(new mil.nga.wkb.geom.Point(false, false, 10, 10));

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, point);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        Geometry actual = (Geometry)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);

        assertTrue(actual instanceof Point);

        Point actualPoint = (Point)actual;

        assertEquals(10, actualPoint.getCoordinate().x, 0d);
        assertEquals(10, actualPoint.getCoordinate().y, 0d);
        assertEquals(0, actualPoint.getCoordinate().z, 0d);
    }

    /**
     * Tests importing a polygon.
     */
    @Test
    public void testImportPolygon()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        mil.nga.wkb.geom.Polygon polygon = new mil.nga.wkb.geom.Polygon(true, false);
        mil.nga.wkb.geom.Point point1 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 0));
        point1.setZ(0d);
        mil.nga.wkb.geom.Point point2 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 0));
        point2.setZ(5d);
        mil.nga.wkb.geom.Point point3 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 10));
        point3.setZ(10d);
        mil.nga.wkb.geom.Point point4 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 10));
        point4.setZ(15d);

        mil.nga.wkb.geom.LineString lineString = new mil.nga.wkb.geom.LineString(true, false);
        lineString.addPoint(point1);
        lineString.addPoint(point2);
        lineString.addPoint(point3);
        lineString.addPoint(point4);
        lineString.addPoint(point1);
        polygon.addRing(lineString);

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, polygon);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        Geometry actual = (Geometry)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);

        assertTrue(actual instanceof Polygon);

        Polygon polygons = (Polygon)actual;

        Coordinate[] coords = polygons.getCoordinates();

        assertEquals(5, coords.length);

        assertEquals(0, coords[0].x, 0d);
        assertEquals(0, coords[0].y, 0d);
        assertEquals(0, coords[0].z, 0d);

        assertEquals(10, coords[1].x, 0d);
        assertEquals(0, coords[1].y, 0d);
        assertEquals(5, coords[1].z, 0d);

        assertEquals(10, coords[2].x, 0d);
        assertEquals(10, coords[2].y, 0d);
        assertEquals(10, coords[2].z, 0d);

        assertEquals(0, coords[3].x, 0d);
        assertEquals(10, coords[3].y, 0d);
        assertEquals(15, coords[3].z, 0d);

        assertEquals(0, coords[4].x, 0d);
        assertEquals(0, coords[4].y, 0d);
        assertEquals(0, coords[4].z, 0d);
    }

    /**
     * Tests importing a polyhedral surface.
     */
    @Test
    public void testImportPolyhedralSurface()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        mil.nga.wkb.geom.PolyhedralSurface geometry = new mil.nga.wkb.geom.PolyhedralSurface(true, false);
        mil.nga.wkb.geom.Polygon polygon1 = new mil.nga.wkb.geom.Polygon(true, false);
        mil.nga.wkb.geom.Point point1 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 0));
        point1.setZ(0d);
        mil.nga.wkb.geom.Point point2 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 0));
        point2.setZ(5d);
        mil.nga.wkb.geom.Point point3 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 10));
        point3.setZ(10d);
        mil.nga.wkb.geom.Point point4 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 10));
        point4.setZ(15d);

        mil.nga.wkb.geom.LineString lineString1 = new mil.nga.wkb.geom.LineString(true, false);
        lineString1.addPoint(point1);
        lineString1.addPoint(point2);
        lineString1.addPoint(point3);
        lineString1.addPoint(point4);
        lineString1.addPoint(point1);
        polygon1.addRing(lineString1);

        mil.nga.wkb.geom.Polygon polygon2 = new mil.nga.wkb.geom.Polygon(true, false);
        mil.nga.wkb.geom.Point point5 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 0));
        point5.setZ(0d);
        mil.nga.wkb.geom.Point point6 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, -10, 0));
        point6.setZ(5d);
        mil.nga.wkb.geom.Point point7 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, -10, 10));
        point7.setZ(10d);
        mil.nga.wkb.geom.Point point8 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 10));
        point8.setZ(15d);

        mil.nga.wkb.geom.LineString lineString2 = new mil.nga.wkb.geom.LineString(true, false);
        lineString2.addPoint(point5);
        lineString2.addPoint(point6);
        lineString2.addPoint(point7);
        lineString2.addPoint(point8);
        lineString2.addPoint(point5);
        polygon2.addRing(lineString2);

        geometry.addPolygon(polygon1);
        geometry.addPolygon(polygon2);

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, geometry);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        Geometry actual = (Geometry)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);

        assertTrue(actual instanceof MultiPolygon);

        MultiPolygon polygons = (MultiPolygon)actual;

        Coordinate[] coords = polygons.getCoordinates();

        assertEquals(10, coords.length);

        assertEquals(0, coords[0].x, 0d);
        assertEquals(0, coords[0].y, 0d);
        assertEquals(0, coords[0].z, 0d);

        assertEquals(10, coords[1].x, 0d);
        assertEquals(0, coords[1].y, 0d);
        assertEquals(5, coords[1].z, 0d);

        assertEquals(10, coords[2].x, 0d);
        assertEquals(10, coords[2].y, 0d);
        assertEquals(10, coords[2].z, 0d);

        assertEquals(0, coords[3].x, 0d);
        assertEquals(10, coords[3].y, 0d);
        assertEquals(15, coords[3].z, 0d);

        assertEquals(0, coords[4].x, 0d);
        assertEquals(0, coords[4].y, 0d);
        assertEquals(0, coords[4].z, 0d);

        assertEquals(0, coords[5].x, 0d);
        assertEquals(0, coords[5].y, 0d);
        assertEquals(0, coords[5].z, 0d);

        assertEquals(-10, coords[6].x, 0d);
        assertEquals(0, coords[6].y, 0d);
        assertEquals(5, coords[6].z, 0d);

        assertEquals(-10, coords[7].x, 0d);
        assertEquals(10, coords[7].y, 0d);
        assertEquals(10, coords[7].z, 0d);

        assertEquals(0, coords[8].x, 0d);
        assertEquals(10, coords[8].y, 0d);
        assertEquals(15, coords[8].z, 0d);

        assertEquals(0, coords[9].x, 0d);
        assertEquals(0, coords[9].y, 0d);
        assertEquals(0, coords[9].z, 0d);
    }

    /**
     * Tests importing a TIN.
     */
    @Test
    public void testImportTIN()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        mil.nga.wkb.geom.TIN geometry = new mil.nga.wkb.geom.TIN(true, false);
        mil.nga.wkb.geom.Polygon polygon1 = new mil.nga.wkb.geom.Polygon(true, false);
        mil.nga.wkb.geom.Point point1 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 0));
        point1.setZ(0d);
        mil.nga.wkb.geom.Point point2 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 0));
        point2.setZ(5d);
        mil.nga.wkb.geom.Point point3 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 10));
        point3.setZ(10d);
        mil.nga.wkb.geom.Point point4 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 10));
        point4.setZ(15d);

        mil.nga.wkb.geom.LineString lineString1 = new mil.nga.wkb.geom.LineString(true, false);
        lineString1.addPoint(point1);
        lineString1.addPoint(point2);
        lineString1.addPoint(point3);
        lineString1.addPoint(point4);
        lineString1.addPoint(point1);
        polygon1.addRing(lineString1);

        mil.nga.wkb.geom.Polygon polygon2 = new mil.nga.wkb.geom.Polygon(true, false);
        mil.nga.wkb.geom.Point point5 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 0));
        point5.setZ(0d);
        mil.nga.wkb.geom.Point point6 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, -10, 0));
        point6.setZ(5d);
        mil.nga.wkb.geom.Point point7 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, -10, 10));
        point7.setZ(10d);
        mil.nga.wkb.geom.Point point8 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 10));
        point8.setZ(15d);

        mil.nga.wkb.geom.LineString lineString2 = new mil.nga.wkb.geom.LineString(true, false);
        lineString2.addPoint(point5);
        lineString2.addPoint(point6);
        lineString2.addPoint(point7);
        lineString2.addPoint(point8);
        lineString2.addPoint(point5);
        polygon2.addRing(lineString2);

        geometry.addPolygon(polygon1);
        geometry.addPolygon(polygon2);

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, geometry);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        Geometry actual = (Geometry)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);

        assertTrue(actual instanceof MultiPolygon);

        MultiPolygon polygons = (MultiPolygon)actual;

        Coordinate[] coords = polygons.getCoordinates();

        assertEquals(10, coords.length);

        assertEquals(0, coords[0].x, 0d);
        assertEquals(0, coords[0].y, 0d);
        assertEquals(0, coords[0].z, 0d);

        assertEquals(10, coords[1].x, 0d);
        assertEquals(0, coords[1].y, 0d);
        assertEquals(5, coords[1].z, 0d);

        assertEquals(10, coords[2].x, 0d);
        assertEquals(10, coords[2].y, 0d);
        assertEquals(10, coords[2].z, 0d);

        assertEquals(0, coords[3].x, 0d);
        assertEquals(10, coords[3].y, 0d);
        assertEquals(15, coords[3].z, 0d);

        assertEquals(0, coords[4].x, 0d);
        assertEquals(0, coords[4].y, 0d);
        assertEquals(0, coords[4].z, 0d);

        assertEquals(0, coords[5].x, 0d);
        assertEquals(0, coords[5].y, 0d);
        assertEquals(0, coords[5].z, 0d);

        assertEquals(-10, coords[6].x, 0d);
        assertEquals(0, coords[6].y, 0d);
        assertEquals(5, coords[6].z, 0d);

        assertEquals(-10, coords[7].x, 0d);
        assertEquals(10, coords[7].y, 0d);
        assertEquals(10, coords[7].z, 0d);

        assertEquals(0, coords[8].x, 0d);
        assertEquals(10, coords[8].y, 0d);
        assertEquals(15, coords[8].z, 0d);

        assertEquals(0, coords[9].x, 0d);
        assertEquals(0, coords[9].y, 0d);
        assertEquals(0, coords[9].z, 0d);
    }

    /**
     * Tests importing a triangle.
     */
    @Test
    public void testImportTriangle()
    {
        Projection webMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toMercator = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        ProjectionTransform toGeodetic = webMercator.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        mil.nga.wkb.geom.Triangle polygon = new mil.nga.wkb.geom.Triangle(true, false);
        mil.nga.wkb.geom.Point point1 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 0));
        point1.setZ(0d);
        mil.nga.wkb.geom.Point point2 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 0));
        point2.setZ(5d);
        mil.nga.wkb.geom.Point point3 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 10, 10));
        point3.setZ(10d);
        mil.nga.wkb.geom.Point point4 = toMercator.transform(new mil.nga.wkb.geom.Point(true, false, 0, 10));
        point4.setZ(15d);

        mil.nga.wkb.geom.LineString lineString = new mil.nga.wkb.geom.LineString(true, false);
        lineString.addPoint(point1);
        lineString.addPoint(point2);
        lineString.addPoint(point3);
        lineString.addPoint(point4);
        lineString.addPoint(point1);
        polygon.addRing(lineString);

        FeatureTable table = new FeatureTable(ourTableName, New.list(FeatureColumn.createPrimaryKeyColumn(0, ourKeyColumn),
                FeatureColumn.createGeometryColumn(1, ourGeometryColumn, GeometryType.POINT, false, null)));
        MockFeatureRow row = new MockFeatureRow(table, polygon);
        GeometryImporter importer = new GeometryImporter();
        Map<String, Serializable> importedRow = New.map();

        importer.importGeometry(importedRow, row, toGeodetic);

        Geometry actual = (Geometry)importedRow.get(GeoPackageColumns.GEOMETRY_COLUMN);

        assertTrue(actual instanceof Polygon);

        Polygon polygons = (Polygon)actual;

        Coordinate[] coords = polygons.getCoordinates();

        assertEquals(5, coords.length);

        assertEquals(0, coords[0].x, 0d);
        assertEquals(0, coords[0].y, 0d);
        assertEquals(0, coords[0].z, 0d);

        assertEquals(10, coords[1].x, 0d);
        assertEquals(0, coords[1].y, 0d);
        assertEquals(5, coords[1].z, 0d);

        assertEquals(10, coords[2].x, 0d);
        assertEquals(10, coords[2].y, 0d);
        assertEquals(10, coords[2].z, 0d);

        assertEquals(0, coords[3].x, 0d);
        assertEquals(10, coords[3].y, 0d);
        assertEquals(15, coords[3].z, 0d);

        assertEquals(0, coords[4].x, 0d);
        assertEquals(0, coords[4].y, 0d);
        assertEquals(0, coords[4].z, 0d);
    }
}
