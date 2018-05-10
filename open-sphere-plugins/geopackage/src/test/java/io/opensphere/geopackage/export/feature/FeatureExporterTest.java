package io.opensphere.geopackage.export.feature;

import java.util.Collections;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.export.model.ExportModel;
import io.opensphere.geopackage.model.ProgressModel;
import io.opensphere.geopackage.progress.ProgressReporter;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.mantle.data.util.DataElementLookupException;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.projection.ProjectionConstants;

/**
 * Unit test for {@link FeatureExporter}.
 */
@SuppressWarnings("boxing")
public class FeatureExporterTest
{
    /**
     * The test columns.
     */
    private static final List<String> ourKeys = New.list("column1", "column2", "column3");

    /**
     * The test layer name.
     */
    private static final String ourLayerName = "myNameIsLayer";

    /**
     * Tests exporting features from data types.
     *
     * @throws DataElementLookupException Bad element.
     */
    @Test
    public void testExportFeatures() throws DataElementLookupException
    {
        EasyMockSupport support = new EasyMockSupport();

        ProgressReporter reporter = new ProgressReporter(new ProgressModel(), Collections.emptyList(),
                new CancellableTaskActivity());

        DataTypeInfo dataType = createDataType(support);
        DataElementLookupUtils lookup = createLookupUtils(support, dataType);
        DataElementCache cache = support.createMock(DataElementCache.class);
        List<DataTypeInfo> dataTypes = createDataTypes(support, dataType, null);
        GeoPackage geopackage = createGeoPackage(support);

        ExportModel exportModel = new ExportModel(null);
        exportModel.setGeoPackage(geopackage);
        exportModel.setProgressReporter(reporter);

        support.replayAll();

        FeatureExporter exporter = new FeatureExporter(dataTypes, lookup, cache);
        exporter.export(exportModel);

        support.verifyAll();
    }

    /**
     * Tests export being cancelled.
     *
     * @throws DataElementLookupException Bad element.
     */
    @Test
    public void testExportFeaturesCancelled() throws DataElementLookupException
    {
        EasyMockSupport support = new EasyMockSupport();

        ProgressReporter reporter = new ProgressReporter(new ProgressModel(), Collections.emptyList(),
                new CancellableTaskActivity());

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        DataElementLookupUtils lookup = support.createMock(DataElementLookupUtils.class);
        DataElementCache cache = support.createMock(DataElementCache.class);
        List<DataTypeInfo> dataTypes = createDataTypes(support, dataType, reporter.getTaskActivity());
        GeoPackage geopackage = support.createMock(GeoPackage.class);

        ExportModel exportModel = new ExportModel(null);
        exportModel.setGeoPackage(geopackage);
        exportModel.setProgressReporter(reporter);

        support.replayAll();

        FeatureExporter exporter = new FeatureExporter(dataTypes, lookup, cache);
        exporter.export(exportModel);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataTypeInfo}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataTypeInfo}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private DataTypeInfo createDataType(EasyMockSupport support)
    {
        MetaDataInfo metaInfo = support.createMock(MetaDataInfo.class);
        EasyMock.expect(metaInfo.getKeyNames()).andReturn(ourKeys);
        EasyMock.expect(metaInfo.getKeyClassType(EasyMock.isA(String.class))).andReturn((Class)String.class).atLeastOnce();
        EasyMock.expect(metaInfo.getKeyForSpecialType(EasyMock.eq(TimeKey.DEFAULT))).andReturn(null).atLeastOnce();
        EasyMock.expect(metaInfo.getGeometryColumn()).andReturn(null).atLeastOnce();

        DataTypeInfo info = support.createMock(DataTypeInfo.class);
        EasyMock.expect(info.getDisplayName()).andReturn(ourLayerName).anyTimes();
        EasyMock.expect(info.getMetaDataInfo()).andReturn(metaInfo);

        BasicVisualizationInfo featureVisInfo = support.createMock(BasicVisualizationInfo.class);
        EasyMock.expect(featureVisInfo.usesDataElements()).andReturn(false);
        EasyMock.expect(info.getBasicVisualizationInfo()).andReturn(featureVisInfo).times(2);
        MapVisualizationInfo mapVisInfo = support.createMock(MapVisualizationInfo.class);
        EasyMock.expect(mapVisInfo.usesMapDataElements()).andReturn(true);
        EasyMock.expect(info.getMapVisualizationInfo()).andReturn(mapVisInfo).times(2);

        return info;
    }

    /**
     * Creates a list of {@link DataTypeInfo} to test with.
     *
     * @param support Used to create mocks.
     * @param featureType The {@link DataTypeInfo} that is the feature data
     *            type.
     * @param ta Used to set to cancel or null if not a cancel test.
     * @return The test data types.
     */
    private List<DataTypeInfo> createDataTypes(EasyMockSupport support, DataTypeInfo featureType, CancellableTaskActivity ta)
    {
        DataTypeInfo nullVisInfo = support.createMock(DataTypeInfo.class);
        EasyMock.expect(nullVisInfo.getBasicVisualizationInfo()).andAnswer(() ->
        {
            if (ta != null)
            {
                ta.setCancelled(true);
            }
            return null;
        });
        EasyMock.expect(nullVisInfo.getMapVisualizationInfo()).andReturn(null);
        List<DataTypeInfo> dataTypes = New.list();
        dataTypes.add(nullVisInfo);

        DataTypeInfo tileType = support.createMock(DataTypeInfo.class);

        if (ta == null)
        {
            BasicVisualizationInfo tileVisInfo = support.createMock(BasicVisualizationInfo.class);
            EasyMock.expect(tileVisInfo.usesDataElements()).andReturn(false);
            EasyMock.expect(tileType.getBasicVisualizationInfo()).andReturn(tileVisInfo).times(2);
            MapVisualizationInfo mapVisInfo = support.createMock(MapVisualizationInfo.class);
            EasyMock.expect(mapVisInfo.usesMapDataElements()).andReturn(false);
            EasyMock.expect(tileType.getMapVisualizationInfo()).andReturn(mapVisInfo).times(2);
        }

        dataTypes.add(tileType);
        dataTypes.add(featureType);

        return dataTypes;
    }

    /**
     * Creates a mocked {@link GeoPackage}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link GeoPackage}.
     */
    @SuppressWarnings("unchecked")
    private GeoPackage createGeoPackage(EasyMockSupport support)
    {
        GeoPackage geopackage = support.createMock(GeoPackage.class);

        EasyMock.expect(
                geopackage.createFeatureTableWithMetadata(EasyMock.isA(GeometryColumns.class), EasyMock.eq(new BoundingBox()),
                        EasyMock.eq((long)ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM), EasyMock.isA(List.class)))
                .andReturn(null);
        EasyMock.expect(geopackage.getFeatureTables()).andReturn(New.list());

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

        EasyMock.expect(lookup.getDataElementCacheIds(EasyMock.eq(dataType))).andReturn(New.list());

        return lookup;
    }
}
