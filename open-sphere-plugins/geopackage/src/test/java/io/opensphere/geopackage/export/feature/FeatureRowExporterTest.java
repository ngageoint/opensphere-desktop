package io.opensphere.geopackage.export.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.geopackage.importer.MockFeatureDao;
import io.opensphere.geopackage.importer.MockGeoPackageConnection;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.DefaultDataElement;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.MockFeatureRow;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.Point;

/**
 * Unit test for {@link FeatureRowExporter} class.
 */
public class FeatureRowExporterTest
{
    /**
     * The test columns.
     */
    private static final List<String> ourKeys = New.list("column1", "column2", "column3");

    /**
     * The test location.
     */
    private static final LatLonAlt ourLocation = LatLonAlt.createFromDegreesMeters(5, 10, 15, ReferenceLevel.TERRAIN);

    /**
     * The time column.
     */
    private static final String ourTimeColumn = "Time";

    /**
     * The test values.
     */
    private static final List<Object> ourValues = New.list("value1", 10.1, new Date());

    /**
     * Tests exporting a row.
     */
    @Test
    public void testExportRow()
    {
        EasyMockSupport support = new EasyMockSupport();

        TimeSpan time = TimeSpan.get();
        FeatureDao dao = createFeatureDao(support);
        DataElement element = createDataElement(support, true, time);

        support.replayAll();

        FeatureRowExporter exporter = new FeatureRowExporter();
        List<Pair<FeatureColumn, String>> featureColumns = New.list();
        for (String key : ourKeys)
        {
            FeatureColumn column = FeatureColumn.createColumn(featureColumns.size(), key, GeoPackageDataType.TEXT, false, null);
            featureColumns.add(new Pair<>(column, key));
        }
        featureColumns.add(new Pair<>(
                FeatureColumn.createColumn(featureColumns.size(), ourTimeColumn, GeoPackageDataType.DATETIME, false, null),
                ourTimeColumn));
        exporter.exportRow(element, dao, featureColumns);

        FeatureResultSet resultSet = dao.queryForAll();

        int size = 0;
        while (resultSet.moveToNext())
        {
            FeatureRow row = resultSet.getRow();
            assertTrue(row instanceof MockFeatureRow);
            for (int i = 0; i < ourKeys.size(); i++)
            {
                String columnName = ourKeys.get(i);
                Object value = ourValues.get(i);

                if (value instanceof Date)
                {
                    value = DateTimeUtilities.generateISO8601DateString((Date)value);
                }

                assertEquals(value, row.getValue(columnName));
            }

            assertEquals(DateTimeUtilities.generateISO8601DateString(time.getStartDate()), row.getValue(ourTimeColumn));
            assertEquals(dao.getGeometryColumns().getSrsId(), row.getGeometry().getSrsId());
            Geometry geom = row.getGeometry().getGeometry();
            assertTrue(geom instanceof Point);
            Point point = (Point)geom;

            assertEquals(10, point.getX(), 0);
            assertEquals(5, point.getY(), 0);
            assertEquals(15, point.getZ(), 0);

            size++;
        }

        assertEquals(1, size);

        support.verifyAll();
    }

    /**
     * Tests exporting a row.
     */
    @Test
    public void testExportRowBoundless()
    {
        EasyMockSupport support = new EasyMockSupport();

        TimeSpan time = TimeSpan.TIMELESS;
        FeatureDao dao = createFeatureDao(support);
        DataElement element = createDataElement(support, true, time);

        support.replayAll();

        FeatureRowExporter exporter = new FeatureRowExporter();
        List<Pair<FeatureColumn, String>> featureColumns = New.list();
        for (String key : ourKeys)
        {
            FeatureColumn column = FeatureColumn.createColumn(featureColumns.size(), key, GeoPackageDataType.TEXT, false, null);
            featureColumns.add(new Pair<>(column, key));
        }
        featureColumns.add(new Pair<>(
                FeatureColumn.createColumn(featureColumns.size(), ourTimeColumn, GeoPackageDataType.DATETIME, false, null),
                ourTimeColumn));
        exporter.exportRow(element, dao, featureColumns);

        FeatureResultSet resultSet = dao.queryForAll();

        int size = 0;
        while (resultSet.moveToNext())
        {
            FeatureRow row = resultSet.getRow();
            assertTrue(row instanceof MockFeatureRow);
            for (int i = 0; i < ourKeys.size(); i++)
            {
                String columnName = ourKeys.get(i);
                Object value = ourValues.get(i);

                if (value instanceof Date)
                {
                    value = DateTimeUtilities.generateISO8601DateString((Date)value);
                }

                assertEquals(value, row.getValue(columnName));
            }

            assertNull(row.getValue(ourTimeColumn));
            assertEquals(dao.getGeometryColumns().getSrsId(), row.getGeometry().getSrsId());
            Geometry geom = row.getGeometry().getGeometry();
            assertTrue(geom instanceof Point);
            Point point = (Point)geom;

            assertEquals(10, point.getX(), 0);
            assertEquals(5, point.getY(), 0);
            assertEquals(15, point.getZ(), 0);

            size++;
        }

        assertEquals(1, size);

        support.verifyAll();
    }

    /**
     * Tests exporting a row.
     */
    @Test
    public void testExportRowBoundlessStart()
    {
        EasyMockSupport support = new EasyMockSupport();

        TimeSpan time = TimeSpan.get((Date)null, new Date());
        FeatureDao dao = createFeatureDao(support);
        DataElement element = createDataElement(support, true, time);

        support.replayAll();

        FeatureRowExporter exporter = new FeatureRowExporter();
        List<Pair<FeatureColumn, String>> featureColumns = New.list();
        for (String key : ourKeys)
        {
            FeatureColumn column = FeatureColumn.createColumn(featureColumns.size(), key, GeoPackageDataType.TEXT, false, null);
            featureColumns.add(new Pair<>(column, key));
        }
        featureColumns.add(new Pair<>(
                FeatureColumn.createColumn(featureColumns.size(), ourTimeColumn, GeoPackageDataType.DATETIME, false, null),
                ourTimeColumn));
        exporter.exportRow(element, dao, featureColumns);

        FeatureResultSet resultSet = dao.queryForAll();

        int size = 0;
        while (resultSet.moveToNext())
        {
            FeatureRow row = resultSet.getRow();
            assertTrue(row instanceof MockFeatureRow);
            for (int i = 0; i < ourKeys.size(); i++)
            {
                String columnName = ourKeys.get(i);
                Object value = ourValues.get(i);

                if (value instanceof Date)
                {
                    value = DateTimeUtilities.generateISO8601DateString((Date)value);
                }

                assertEquals(value, row.getValue(columnName));
            }

            assertEquals(DateTimeUtilities.generateISO8601DateString(time.getEndDate()), row.getValue(ourTimeColumn));
            assertEquals(dao.getGeometryColumns().getSrsId(), row.getGeometry().getSrsId());
            Geometry geom = row.getGeometry().getGeometry();
            assertTrue(geom instanceof Point);
            Point point = (Point)geom;

            assertEquals(10, point.getX(), 0);
            assertEquals(5, point.getY(), 0);
            assertEquals(15, point.getZ(), 0);

            size++;
        }

        assertEquals(1, size);

        support.verifyAll();
    }

    /**
     * Tests exporting a row without a geometry.
     */
    @Test
    public void testExportRowNoGeometry()
    {
        EasyMockSupport support = new EasyMockSupport();

        FeatureDao dao = createFeatureDao(support);
        DataElement element = createDataElement(support, false, TimeSpan.TIMELESS);

        support.replayAll();

        FeatureRowExporter exporter = new FeatureRowExporter();
        List<Pair<FeatureColumn, String>> featureColumns = New.list();
        for (String key : ourKeys)
        {
            FeatureColumn column = FeatureColumn.createColumn(featureColumns.size(), key, GeoPackageDataType.TEXT, false, null);
            featureColumns.add(new Pair<>(column, key));
        }
        exporter.exportRow(element, dao, featureColumns);

        FeatureResultSet resultSet = dao.queryForAll();

        int size = 0;
        while (resultSet.moveToNext())
        {
            FeatureRow row = resultSet.getRow();
            assertTrue(row instanceof MockFeatureRow);
            for (int i = 0; i < ourKeys.size(); i++)
            {
                String columnName = ourKeys.get(i);
                Object value = ourValues.get(i);

                if (value instanceof Date)
                {
                    value = DateTimeUtilities.generateISO8601DateString((Date)value);
                }

                assertEquals(value, row.getValue(columnName));
            }

            assertNull(row.getValue(ourTimeColumn));
            assertNull(row.getGeometry());

            size++;
        }

        assertEquals(1, size);

        support.verifyAll();
    }

    /**
     * Creates a test {@link DataElement}.
     *
     * @param support Used to create mocks within the element.
     * @param isGeometry True if the element should be a map element, false if
     *            no geometries.
     * @param time The time of the data element.
     * @return The test {@link DataElement}.
     */
    private DataElement createDataElement(EasyMockSupport support, boolean isGeometry, TimeSpan time)
    {
        DataTypeInfo dti = support.createMock(DataTypeInfo.class);

        MetaDataProvider mdp = support.createMock(MetaDataProvider.class);
        int index = 0;
        for (String key : ourKeys)
        {
            EasyMock.expect(mdp.getValue(EasyMock.cmpEq(key))).andReturn(ourValues.get(index));
            index++;
        }

        DataElement dataElement = null;

        if (isGeometry)
        {
            DefaultMapPointGeometrySupport mgs = new DefaultMapPointGeometrySupport(ourLocation);
            dataElement = new DefaultMapDataElement(1, time, dti, mdp, mgs);
        }
        else
        {
            dataElement = new DefaultDataElement(1, time, dti, mdp);
        }

        return dataElement;
    }

    /**
     * Creates a test {@link FeatureDao}.
     *
     * @param support Used to create mocks for objects within the dao.
     * @return The test dao.
     */
    private FeatureDao createFeatureDao(EasyMockSupport support)
    {
        MockGeoPackageConnection db = new MockGeoPackageConnection(support.createMock(Connection.class));
        GeometryColumns geometry = new GeometryColumns();
        Contents contents = new Contents();
        contents.setDataType(ContentsDataType.FEATURES);

        geometry.setContents(contents);
        geometry.setSrs(new SpatialReferenceSystem());
        geometry.getSrs().setOrganization(ProjectionConstants.AUTHORITY_EPSG);
        geometry.getSrs().setOrganizationCoordsysId(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        MockFeatureDao featureDao = new MockFeatureDao(db, geometry, null);

        return featureDao;
    }
}
