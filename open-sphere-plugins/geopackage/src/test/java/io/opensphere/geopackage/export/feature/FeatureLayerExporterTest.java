package io.opensphere.geopackage.export.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.DoubleRange;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.importer.MockFeatureDao;
import io.opensphere.geopackage.importer.MockGeoPackageConnection;
import io.opensphere.geopackage.model.GeoPackageColumns;
import io.opensphere.geopackage.model.ProgressModel;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.mantle.data.util.DataElementLookupException;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
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
import mil.nga.wkb.geom.GeometryType;
import mil.nga.wkb.geom.Point;

/**
 * Unit test for {@link FeatureLayerExporter}.
 */
public class FeatureLayerExporterTest
{
    /**
     * The expected created columns.
     */
    private static final List<String> ourExpectedKeys = New.list("column_1", "column_2", "column3", "column4", "column5",
            "column5_1", "column6", "column7", "column8");

    /**
     * The expected types.
     */
    private static final List<GeoPackageDataType> ourExpectedTypes = New.list(GeoPackageDataType.TEXT, GeoPackageDataType.DOUBLE,
            GeoPackageDataType.BOOLEAN, GeoPackageDataType.MEDIUMINT, GeoPackageDataType.FLOAT, GeoPackageDataType.DOUBLE,
            GeoPackageDataType.INTEGER, GeoPackageDataType.DATETIME);

    /**
     * The test columns.
     */
    private static final List<String> ourKeys = New.list("column 1", "column (2)", "column3", "column4", "column5", "column5.1",
            "column6", "column7", "column8");

    /**
     * The test layer name.
     */
    private static final String ourLayerName = "my Name Is/Layer";

    /**
     * The test location.
     */
    private static final List<LatLonAlt> ourLocations = New.list(
            LatLonAlt.createFromDegreesMeters(5, 10, 15, ReferenceLevel.TERRAIN),
            LatLonAlt.createFromDegreesMeters(6, 11, 16, ReferenceLevel.TERRAIN));

    /**
     * The test values.
     */
    private static final List<List<Object>> ourValues = New.list(
            New.list("value1", 10.1, false, 9, 12.3f, new DoubleRange(12.3), 1000L, new Date(1000),
                    new GeometryFactory().createPoint(new Coordinate(1, 1))),
            New.list("value2", 11.1, true, 10, 13.3f, new DoubleRange(13.4), 1001L, new Date(1001),
                    new GeometryFactory().createPoint(new Coordinate(1, 1))));

    /**
     * Tests exporting features.
     *
     * @throws DataElementLookupException Bad element.
     */
    @Test
    public void testExportFeatures() throws DataElementLookupException
    {
        EasyMockSupport support = new EasyMockSupport();

        ProgressModel model = new ProgressModel();
        CancellableTaskActivity ta = new CancellableTaskActivity();

        DataTypeInfo dataType = createDataType(support);
        DataElementLookupUtils lookup = createLookupUtils(support, dataType);
        FeatureDao featureDao = createFeatureDao(support, null);
        GeoPackage geopackage = createGeoPackage(support, featureDao, false);

        support.replayAll();

        FeatureLayerExporter exporter = new FeatureLayerExporter(lookup);
        exporter.exportFeatures(dataType, geopackage, model, ta);

        FeatureResultSet resultSet = featureDao.queryForAll();

        int size = 0;
        while (resultSet.moveToNext())
        {
            FeatureRow row = resultSet.getRow();
            assertTrue(row instanceof MockFeatureRow);
            for (int i = 0; i < ourKeys.size(); i++)
            {
                String columnName = ourExpectedKeys.get(i);
                Object value = ourValues.get(size).get(i);
                if (value instanceof Date)
                {
                    value = DateTimeUtilities.generateISO8601DateString((Date)value);
                }
                else if (value instanceof DoubleRange)
                {
                    value = ((DoubleRange)value).doubleValue();
                }

                if (i < ourKeys.size() - 1)
                {
                    assertEquals(value, row.getValue(columnName));
                }
                else
                {
                    assertNull(row.getValue(columnName));
                }
            }

            assertEquals(featureDao.getGeometryColumns().getSrsId(), row.getGeometry().getSrsId());
            Geometry geom = row.getGeometry().getGeometry();
            assertTrue(geom instanceof Point);
            Point point = (Point)geom;

            assertEquals(ourLocations.get(size).getLonD(), point.getX(), 0);
            assertEquals(ourLocations.get(size).getLatD(), point.getY(), 0);
            assertEquals(ourLocations.get(size).getAltM(), point.getZ(), 0);

            size++;
        }

        assertEquals(2, model.getCompletedCount());
        assertEquals(2, size);

        support.verifyAll();
    }

    /**
     * Tests exporting features and then being cancelled by user.
     *
     * @throws DataElementLookupException Bad element.
     */
    @Test
    public void testExportFeaturesCancelled() throws DataElementLookupException
    {
        EasyMockSupport support = new EasyMockSupport();

        ProgressModel model = new ProgressModel();
        CancellableTaskActivity ta = new CancellableTaskActivity();

        DataTypeInfo dataType = createDataType(support);
        DataElementLookupUtils lookup = createLookupUtils(support, dataType);
        FeatureDao featureDao = createFeatureDao(support, ta);
        GeoPackage geopackage = createGeoPackage(support, featureDao, false);

        support.replayAll();

        FeatureLayerExporter exporter = new FeatureLayerExporter(lookup);
        exporter.exportFeatures(dataType, geopackage, model, ta);

        FeatureResultSet resultSet = featureDao.queryForAll();

        int size = 0;
        while (resultSet.moveToNext())
        {
            FeatureRow row = resultSet.getRow();
            assertTrue(row instanceof MockFeatureRow);
            for (int i = 0; i < ourKeys.size(); i++)
            {
                String columnName = ourExpectedKeys.get(i);
                Object value = ourValues.get(size).get(i);
                if (value instanceof Date)
                {
                    value = DateTimeUtilities.generateISO8601DateString((Date)value);
                }
                else if (value instanceof DoubleRange)
                {
                    value = ((DoubleRange)value).doubleValue();
                }

                if (i < ourKeys.size() - 1)
                {
                    assertEquals(value, row.getValue(columnName));
                }
                else
                {
                    assertNull(row.getValue(columnName));
                }
            }

            assertEquals(featureDao.getGeometryColumns().getSrsId(), row.getGeometry().getSrsId());
            Geometry geom = row.getGeometry().getGeometry();
            assertTrue(geom instanceof Point);
            Point point = (Point)geom;

            assertEquals(ourLocations.get(size).getLonD(), point.getX(), 0);
            assertEquals(ourLocations.get(size).getLatD(), point.getY(), 0);
            assertEquals(ourLocations.get(size).getAltM(), point.getZ(), 0);

            size++;
        }

        assertEquals(1, model.getCompletedCount());
        assertEquals(1, size);

        support.verifyAll();
    }

    /**
     * Tests exporting features.
     *
     * @throws DataElementLookupException Bad element.
     */
    @Test
    public void testExportFeaturesExisting() throws DataElementLookupException
    {
        EasyMockSupport support = new EasyMockSupport();

        ProgressModel model = new ProgressModel();
        CancellableTaskActivity ta = new CancellableTaskActivity();

        DataTypeInfo dataType = createDataType(support);
        DataElementLookupUtils lookup = createLookupUtils(support, dataType);
        FeatureDao featureDao = createFeatureDao(support, null);
        GeoPackage geopackage = createGeoPackage(support, featureDao, true);

        support.replayAll();

        FeatureLayerExporter exporter = new FeatureLayerExporter(lookup);
        exporter.exportFeatures(dataType, geopackage, model, ta);

        FeatureResultSet resultSet = featureDao.queryForAll();

        int size = 0;
        while (resultSet.moveToNext())
        {
            FeatureRow row = resultSet.getRow();
            assertTrue(row instanceof MockFeatureRow);
            for (int i = 0; i < ourKeys.size(); i++)
            {
                String columnName = ourExpectedKeys.get(i);
                Object value = ourValues.get(size).get(i);
                if (value instanceof Date)
                {
                    value = DateTimeUtilities.generateISO8601DateString((Date)value);
                }
                else if (value instanceof DoubleRange)
                {
                    value = ((DoubleRange)value).doubleValue();
                }

                if (i < ourKeys.size() - 1)
                {
                    assertEquals(value, row.getValue(columnName));
                }
                else
                {
                    assertNull(row.getValue(columnName));
                }
            }

            assertEquals(featureDao.getGeometryColumns().getSrsId(), row.getGeometry().getSrsId());
            Geometry geom = row.getGeometry().getGeometry();
            assertTrue(geom instanceof Point);
            Point point = (Point)geom;

            assertEquals(ourLocations.get(size).getLonD(), point.getX(), 0);
            assertEquals(ourLocations.get(size).getLatD(), point.getY(), 0);
            assertEquals(ourLocations.get(size).getAltM(), point.getZ(), 0);

            size++;
        }

        assertEquals(2, model.getCompletedCount());
        assertEquals(2, size);

        support.verifyAll();
    }

    /**
     * Creates a test {@link DataElement}.
     *
     * @param support Used to create mocks within the element.
     * @return The test {@link DataElement}.
     */
    private List<DataElement> createDataElements(EasyMockSupport support)
    {
        List<DataElement> elements = New.list();

        for (int i = 0; i < ourLocations.size(); i++)
        {
            DataTypeInfo dti = support.createMock(DataTypeInfo.class);

            MetaDataProvider mdp = support.createMock(MetaDataProvider.class);
            EasyMock.expect(mdp.getKeys()).andReturn(ourKeys).anyTimes();
            int index = 0;
            for (String key : ourKeys)
            {
                EasyMock.expect(mdp.getValue(EasyMock.cmpEq(key))).andReturn(ourValues.get(i).get(index)).anyTimes();
                index++;
            }

            DataElement dataElement = null;

            DefaultMapPointGeometrySupport mgs = new DefaultMapPointGeometrySupport(ourLocations.get(i));
            dataElement = new DefaultMapDataElement(1, TimeSpan.get((Date)ourValues.get(i).get(ourKeys.size() - 2)), dti, mdp,
                    mgs);

            elements.add(dataElement);
        }

        return elements;
    }

    /**
     * Creates an easy mocked {@link DataTypeInfo}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataTypeInfo}.
     */
    private DataTypeInfo createDataType(EasyMockSupport support)
    {
        MetaDataInfo metaInfo = support.createMock(MetaDataInfo.class);
        EasyMock.expect(metaInfo.getKeyNames()).andReturn(ourKeys);
        EasyMock.expect(metaInfo.getKeyClassType(EasyMock.isA(String.class))).andAnswer(this::getKeyClassAnswer).atLeastOnce();
        EasyMock.expect(metaInfo.getKeyForSpecialType(EasyMock.eq(TimeKey.DEFAULT))).andReturn("column7").atLeastOnce();
        EasyMock.expect(metaInfo.getGeometryColumn()).andReturn("column8").atLeastOnce();

        DataTypeInfo info = support.createMock(DataTypeInfo.class);
        EasyMock.expect(info.getDisplayName()).andReturn(ourLayerName).anyTimes();
        EasyMock.expect(info.getMetaDataInfo()).andReturn(metaInfo);

        return info;
    }

    /**
     * Creates a test {@link FeatureDao}.
     *
     * @param support Used to create mocks for objects within the dao.
     * @param ta The task activity to cancel, or null if this is not a cancel
     *            test.
     * @return The test dao.
     */
    private FeatureDao createFeatureDao(EasyMockSupport support, CancellableTaskActivity ta)
    {
        MockGeoPackageConnection db = new MockGeoPackageConnection(support.createMock(Connection.class));
        GeometryColumns geometry = new GeometryColumns();
        Contents contents = new Contents();
        contents.setDataType(ContentsDataType.FEATURES);

        geometry.setContents(contents);
        geometry.setSrs(new SpatialReferenceSystem());
        geometry.getSrs().setOrganization(ProjectionConstants.AUTHORITY_EPSG);
        geometry.getSrs().setOrganizationCoordsysId(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        MockFeatureDao featureDao = new MockFeatureDao(db, geometry, ta);

        return featureDao;
    }

    /**
     * Creates a mocked {@link GeoPackage}.
     *
     * @param support Used to create the mock.
     * @param featureDao The feature dao to return for the test table.
     * @param alreadyExists True if the table should already exist in the mocked
     *            geopackage, false if the table needs to be created.
     * @return The mocked {@link GeoPackage}.
     */
    @SuppressWarnings("unchecked")
    private GeoPackage createGeoPackage(EasyMockSupport support, FeatureDao featureDao, boolean alreadyExists)
    {
        GeoPackage geopackage = support.createMock(GeoPackage.class);

        if (!alreadyExists)
        {
            EasyMock.expect(
                    geopackage.createFeatureTableWithMetadata(EasyMock.isA(GeometryColumns.class), EasyMock.eq(new BoundingBox()),
                            EasyMock.eq((long)ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM), EasyMock.isA(List.class)))
                    .andAnswer(this::createTableAnswer);
        }
        EasyMock.expect(geopackage.getFeatureDao(EasyMock.cmpEq(ourLayerName.replace(' ', '_').replace('/', '_'))))
                .andReturn(featureDao);
        if (!alreadyExists)
        {
            EasyMock.expect(geopackage.getFeatureTables()).andReturn(New.list());
        }
        else
        {
            EasyMock.expect(geopackage.getFeatureTables()).andReturn(New.list(ourLayerName.replace(' ', '_').replace('/', '_')));
        }

        return geopackage;
    }

    /**
     * Creates a mocked {@link DataElementLookupUtils}.
     *
     * @param support Used to create the mock.
     * @param dataType The expected data type to be passed.
     * @return The mocked {@link DataElementLookupUtils}.
     * @throws DataElementLookupException Bad element.
     */
    private DataElementLookupUtils createLookupUtils(EasyMockSupport support, DataTypeInfo dataType)
        throws DataElementLookupException
    {
        DataElementLookupUtils lookup = support.createMock(DataElementLookupUtils.class);

        List<Long> elementIds = New.list(0L, 1L);
        EasyMock.expect(lookup.getDataElementCacheIds(EasyMock.eq(dataType))).andReturn(elementIds);
        List<DataElement> elements = createDataElements(support);
        EasyMock.expect(
                lookup.getDataElements(EasyMock.eq(elementIds), EasyMock.eq(dataType), EasyMock.isNull(), EasyMock.eq(false)))
                .andReturn(elements);

        return lookup;
    }

    /**
     * The answer used for the mocked createFeatueTable call.
     *
     * @return The passed in geometry columns.
     */
    @SuppressWarnings("unchecked")
    private GeometryColumns createTableAnswer()
    {
        GeometryColumns geometryColumns = (GeometryColumns)EasyMock.getCurrentArguments()[0];
        List<FeatureColumn> columns = (List<FeatureColumn>)EasyMock.getCurrentArguments()[3];

        assertEquals(ourLayerName.replace(' ', '_').replace('/', '_'), geometryColumns.getId().getTableName());
        assertEquals(GeoPackageColumns.GEOMETRY_COLUMN, geometryColumns.getId().getColumnName());
        assertEquals(GeometryType.GEOMETRY, geometryColumns.getGeometryType());
        assertEquals((byte)1, geometryColumns.getZ());
        assertEquals((byte)0, geometryColumns.getM());

        assertEquals(ourExpectedTypes.size() + 2, columns.size());

        for (int i = 0; i < columns.size(); i++)
        {
            FeatureColumn column = columns.get(i);

            if (i < ourKeys.size() - 1)
            {
                assertEquals(ourExpectedKeys.indexOf(column.getName()), column.getIndex());
                assertEquals(ourExpectedKeys.get(column.getIndex()), column.getName());
                assertEquals(ourExpectedTypes.get(column.getIndex()), column.getDataType());
                assertFalse(column.isNotNull());
                assertNull(column.getDefaultValue());
            }
            else if (i == ourKeys.size() - 1)
            {
                assertTrue(column.isPrimaryKey());
                assertEquals(GeoPackageColumns.ID_COLUMN, column.getName());
            }
            else
            {
                assertTrue(column.isGeometry());
                assertEquals(GeoPackageColumns.GEOMETRY_COLUMN, column.getName());
            }
        }

        return geometryColumns;
    }

    /**
     * The answer for the mocked getKeyClassType call.
     *
     * @return The key's class.
     */
    @SuppressWarnings("rawtypes")
    private Class getKeyClassAnswer()
    {
        int index = ourKeys.indexOf(EasyMock.getCurrentArguments()[0]);
        return ourValues.get(0).get(index).getClass();
    }
}
