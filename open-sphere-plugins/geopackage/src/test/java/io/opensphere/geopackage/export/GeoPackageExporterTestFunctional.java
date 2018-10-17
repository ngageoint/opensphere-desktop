package io.opensphere.geopackage.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.DoubleRange;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.mantle.data.util.DataElementLookupException;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.sf.Geometry;
import mil.nga.sf.Point;

/**
 * Functional test for {@link GeoPackageExporter}.
 */
public class GeoPackageExporterTestFunctional
{
    /**
     * The expected columns in the geopackage file.
     */
    private static final List<String> ourExpectedKeys = New.list("column_1", "column2", "column3", "column4_m", "column5",
            "column5_1", "column6", "column7");

    /**
     * The test columns.
     */
    private static final List<String> ourKeys = New.list("column 1", "column2", "column3", "column4 (m)", "column5", "column5.1",
            "column6", "column7");

    /**
     * The test layer name.
     */
    private static final String ourLayerName = "my Name Is Layer";

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
            New.list("value1", Double.valueOf(10.1), Boolean.FALSE, Integer.valueOf(9), Float.valueOf(12.3f),
                    new DoubleRange(12.3), Long.valueOf(1000L), new Date(1000)),
            New.list("value2", Double.valueOf(11.1), Boolean.TRUE, Integer.valueOf(10), Float.valueOf(13.3f),
                    new DoubleRange(13.4), Long.valueOf(1001L), new Date(1001)));

    /**
     * Tests exporting features to a geopackage file.
     *
     * @throws DataElementLookupException Bad element.
     * @throws IOException Bad IO.
     * @throws ExportException Bad export.
     */
    @Test
    public void testExportFile() throws DataElementLookupException, IOException, ExportException
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo dataType = createDataType(support);
        DataElementLookupUtils lookupUtils = createLookupUtils(support, dataType);
        Toolbox toolbox = createToolbox(support, lookupUtils);

        support.replayAll();

        GeoPackageExporter exporter = new GeoPackageExporter();
        exporter.setToolbox(toolbox);
        File temp = File.createTempFile("test", ".gpk");
        temp.deleteOnExit();
        File geopackageFile = new File(temp.toString() + "g");
        geopackageFile.deleteOnExit();

        exporter.setObjects(New.list(dataType));
        assertTrue(exporter.canExport(File.class));

        exporter.export(geopackageFile);

        GeoPackage geopackage = GeoPackageManager.open(geopackageFile);

        FeatureDao featureDao = geopackage.getFeatureDao(ourLayerName.replace(' ', '_'));

        FeatureResultSet resultSet = featureDao.queryForAll();

        int size = 0;
        while (resultSet.moveToNext())
        {
            FeatureRow row = resultSet.getRow();
            for (int i = 0; i < ourExpectedKeys.size(); i++)
            {
                String columnName = ourExpectedKeys.get(i);
                Object value = ourValues.get(size).get(i);
                if (value instanceof Date)
                {
                    value = DateTimeUtilities.generateISO8601DateString((Date)value);
                }
                else if (value instanceof DoubleRange)
                {
                    value = Double.valueOf(((DoubleRange)value).doubleValue());
                }

                assertEquals(value, row.getValue(columnName));
            }

            assertEquals(featureDao.getGeometryColumns().getSrsId(), row.getGeometry().getSrsId());
            Geometry geom = row.getGeometry().getGeometry();
            assertTrue(geom instanceof Point);
            Point point = (Point)geom;

            assertEquals(ourLocations.get(size).getLonD(), point.getX(), 0);
            assertEquals(ourLocations.get(size).getLatD(), point.getY(), 0);
            assertEquals(ourLocations.get(size).getAltM(), point.getZ().doubleValue(), 0);

            size++;
        }

        assertEquals(2, size);

        support.verifyAll();
    }

    /**
     * Tests exporting features to a geopackage file.
     *
     * @throws DataElementLookupException Bad element.
     * @throws IOException Bad IO.
     * @throws ExportException Bad export.
     */
    @Test
    public void testExportFileExists() throws DataElementLookupException, IOException, ExportException
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo dataType = createDataType(support);
        DataElementLookupUtils lookupUtils = createLookupUtils(support, dataType);
        Toolbox toolbox = createToolbox(support, lookupUtils);

        support.replayAll();

        GeoPackageExporter exporter = new GeoPackageExporter();
        exporter.setToolbox(toolbox);
        File temp = File.createTempFile("test", ".gpk");
        temp.deleteOnExit();
        File geopackageFile = new File(temp.toString() + "g");
        geopackageFile.deleteOnExit();

        exporter.setObjects(New.list(dataType));
        assertTrue(exporter.canExport(File.class));

        exporter.export(geopackageFile);
        exporter.export(geopackageFile);

        GeoPackage geopackage = GeoPackageManager.open(geopackageFile);

        FeatureDao featureDao = geopackage.getFeatureDao(ourLayerName.replace(' ', '_'));

        FeatureResultSet resultSet = featureDao.queryForAll();

        int size = 0;
        while (resultSet.moveToNext())
        {
            FeatureRow row = resultSet.getRow();
            for (int i = 0; i < ourExpectedKeys.size(); i++)
            {
                String columnName = ourExpectedKeys.get(i);
                Object value = ourValues.get(size % 2).get(i);
                if (value instanceof Date)
                {
                    value = DateTimeUtilities.generateISO8601DateString((Date)value);
                }
                else if (value instanceof DoubleRange)
                {
                    value = Double.valueOf(((DoubleRange)value).doubleValue());
                }
                assertEquals(value, row.getValue(columnName));
            }

            assertEquals(featureDao.getGeometryColumns().getSrsId(), row.getGeometry().getSrsId());
            Geometry geom = row.getGeometry().getGeometry();
            assertTrue(geom instanceof Point);
            Point point = (Point)geom;

            assertEquals(ourLocations.get(size % 2).getLonD(), point.getX(), 0);
            assertEquals(ourLocations.get(size % 2).getLatD(), point.getY(), 0);
            assertEquals(ourLocations.get(size % 2).getAltM(), point.getZ().doubleValue(), 0);

            size++;
        }

        assertEquals(4, size);
    }

    /**
     * Tests exporting features to a geopackage file.
     *
     * @throws DataElementLookupException Bad element.
     * @throws IOException Bad IO.
     * @throws ExportException Bad export.
     */
    @Test
    public void testExportFileNoExtension() throws DataElementLookupException, IOException, ExportException
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo dataType = createDataType(support);
        DataElementLookupUtils lookupUtils = createLookupUtils(support, dataType);
        Toolbox toolbox = createToolbox(support, lookupUtils);

        support.replayAll();

        GeoPackageExporter exporter = new GeoPackageExporter();
        exporter.setToolbox(toolbox);
        File temp = File.createTempFile("test", "somethin");
        temp.deleteOnExit();
        File geopackageFile = new File(temp.toString() + "g");
        geopackageFile.deleteOnExit();

        exporter.setObjects(New.list(dataType));
        assertTrue(exporter.canExport(File.class));

        exporter.export(geopackageFile);

        GeoPackage geopackage = GeoPackageManager.open(new File(geopackageFile + "." + GeoPackageConstants.GEOPACKAGE_EXTENSION));

        FeatureDao featureDao = geopackage.getFeatureDao(ourLayerName.replace(' ', '_'));

        FeatureResultSet resultSet = featureDao.queryForAll();

        int size = 0;
        while (resultSet.moveToNext())
        {
            FeatureRow row = resultSet.getRow();
            for (int i = 0; i < ourExpectedKeys.size(); i++)
            {
                String columnName = ourExpectedKeys.get(i);
                Object value = ourValues.get(size).get(i);
                if (value instanceof Date)
                {
                    value = DateTimeUtilities.generateISO8601DateString((Date)value);
                }
                else if (value instanceof DoubleRange)
                {
                    value = Double.valueOf(((DoubleRange)value).doubleValue());
                }

                assertEquals(value, row.getValue(columnName));
            }

            assertEquals(featureDao.getGeometryColumns().getSrsId(), row.getGeometry().getSrsId());
            Geometry geom = row.getGeometry().getGeometry();
            assertTrue(geom instanceof Point);
            Point point = (Point)geom;

            assertEquals(ourLocations.get(size).getLonD(), point.getX(), 0);
            assertEquals(ourLocations.get(size).getLatD(), point.getY(), 0);
            assertEquals(ourLocations.get(size).getAltM(), point.getZ().doubleValue(), 0);

            size++;
        }

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
            dataElement = new DefaultMapDataElement(1, TimeSpan.get((Date)ourValues.get(i).get(ourKeys.size() - 1)), dti, mdp,
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
        Map<String, Class<?>> keysToTypes = New.map();
        for (int i = 0; i < ourKeys.size(); i++)
        {
            keysToTypes.put(ourKeys.get(i), ourValues.get(0).get(i).getClass());
        }
        MetaDataInfo metaInfo = support.createMock(MetaDataInfo.class);
        EasyMock.expect(metaInfo.getKeyNames()).andReturn(ourKeys).atLeastOnce();
        EasyMock.expect(metaInfo.getKeyClassType(EasyMock.isA(String.class))).andAnswer(this::getKeyClassAnswer).atLeastOnce();
        EasyMock.expect(metaInfo.getKeyForSpecialType(EasyMock.eq(TimeKey.DEFAULT))).andReturn("column7").atLeastOnce();
        EasyMock.expect(metaInfo.getGeometryColumn()).andReturn(null).atLeastOnce();

        DataTypeInfo info = support.createMock(DataTypeInfo.class);
        EasyMock.expect(info.getDisplayName()).andReturn(ourLayerName).anyTimes();
        EasyMock.expect(info.getMetaDataInfo()).andReturn(metaInfo).atLeastOnce();

        BasicVisualizationInfo featureVisInfo = support.createMock(BasicVisualizationInfo.class);
        EasyMock.expect(Boolean.valueOf(featureVisInfo.usesDataElements())).andReturn(Boolean.TRUE).atLeastOnce();
        EasyMock.expect(info.getBasicVisualizationInfo()).andReturn(featureVisInfo).atLeastOnce();
        EasyMock.expect(info.getMapVisualizationInfo()).andReturn(null).anyTimes();

        return info;
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

        List<Long> elementIds = New.list(Long.valueOf(0L), Long.valueOf(1L));
        EasyMock.expect(lookup.getDataElementCacheIds(EasyMock.eq(dataType))).andReturn(elementIds).atLeastOnce();
        List<DataElement> elements = createDataElements(support);
        EasyMock.expect(
                lookup.getDataElements(EasyMock.eq(elementIds), EasyMock.eq(dataType), EasyMock.isNull(), EasyMock.eq(false)))
                .andReturn(elements).atLeastOnce();

        return lookup;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param lookupUtils The lookupUtils to return.
     * @return The mocked {@link Toolbox}.
     */
    private Toolbox createToolbox(EasyMockSupport support, DataElementLookupUtils lookupUtils)
    {
        DataElementCache elementCache = support.createMock(DataElementCache.class);
        EasyMock.expect(Integer.valueOf(elementCache.getElementCountForType(EasyMock.isA(DataTypeInfo.class)))).andReturn(Integer.valueOf(ourValues.size()))
                .atLeastOnce();

        MantleToolbox mantleToolbox = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantleToolbox.getDataElementCache()).andReturn(elementCache).atLeastOnce();
        EasyMock.expect(mantleToolbox.getDataElementLookupUtils()).andReturn(lookupUtils).atLeastOnce();

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantleToolbox)
                .atLeastOnce();

        MenuBarRegistry menuBarRegistry = support.createMock(MenuBarRegistry.class);
        menuBarRegistry.addTaskActivity(EasyMock.isA(CancellableTaskActivity.class));
        EasyMock.expectLastCall().atLeastOnce();

        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        EasyMock.expect(uiRegistry.getMenuBarRegistry()).andReturn(menuBarRegistry).atLeastOnce();

        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();

        return toolbox;
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
