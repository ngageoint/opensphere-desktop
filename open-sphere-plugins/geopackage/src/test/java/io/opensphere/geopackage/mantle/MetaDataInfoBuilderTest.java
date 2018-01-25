package io.opensphere.geopackage.mantle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageColumns;
import io.opensphere.geopackage.model.GeoPackageFeatureLayer;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;

/**
 * Tests the {@link MetaDataInfoBuilder} class.
 */
public class MetaDataInfoBuilderTest
{
    /**
     * A test column name.
     */
    private static final String ourColumn1 = "column1";

    /**
     * A test column name.
     */
    private static final String ourColumn2 = "column2";

    /**
     * Another test column name.
     */
    private static final String ourColumn3 = "column3";

    /**
     * The time test column.
     */
    private static final String ourColumn4 = "column4";

    /**
     * The test layer name.
     */
    private static final String ourLayerName = "layer";

    /**
     * The test package file.
     */
    private static final String ourPackageFile = "c:\\somefile.gpkg";

    /**
     * The test package name.
     */
    private static final String ourPackageName = "package";

    /**
     * Tests building meta data info.
     */
    @Test
    public void testBuildMetaDataInfo()
    {
        GeoPackageFeatureLayer layer = new GeoPackageFeatureLayer(ourPackageName, ourPackageFile, ourLayerName, 1);

        Map<String, Serializable> row = New.map();

        row.put(ourColumn1, "value1");
        row.put(ourColumn2, null);
        row.put(ourColumn3, null);
        row.put(ourColumn4, "N_A");
        row.put(GeoPackageColumns.GEOMETRY_COLUMN, new GeometryFactory().createPoint(new Coordinate(10, 11)));

        layer.getData().add(row);

        row = New.map();

        Date time = new Date();
        row.put(ourColumn1, "value2");
        row.put(ourColumn2, 10d);
        row.put(ourColumn3, null);
        row.put(ourColumn4, DateTimeUtilities.generateISO8601DateString(time));
        row.put(GeoPackageColumns.GEOMETRY_COLUMN, new GeometryFactory().createPoint(new Coordinate(10, 11)));

        layer.getData().add(row);

        MetaDataInfoBuilder builder = new MetaDataInfoBuilder();
        MetaDataInfo metadataInfo = builder.buildMetaDataInfo(layer);

        assertTrue(metadataInfo.getKeyNames().contains(ourColumn1));
        assertTrue(metadataInfo.getKeyNames().contains(ourColumn2));
        assertTrue(metadataInfo.getKeyNames().contains(ourColumn3));
        assertTrue(metadataInfo.getKeyNames().contains(GeoPackageColumns.GEOMETRY_COLUMN));
        assertEquals(GeoPackageColumns.GEOMETRY_COLUMN, metadataInfo.getGeometryColumn());
        assertTrue(metadataInfo.getKeyNames().contains(ourColumn4));
        assertEquals(ourColumn4, metadataInfo.getKeyForSpecialType(TimeKey.DEFAULT));

        assertEquals(String.class, metadataInfo.getKeyClassType(ourColumn1));
        assertEquals(Double.class, metadataInfo.getKeyClassType(ourColumn2));
        assertEquals(Serializable.class, metadataInfo.getKeyClassType(ourColumn3));
        assertEquals(Geometry.class, metadataInfo.getKeyClassType(GeoPackageColumns.GEOMETRY_COLUMN));
        assertEquals(String.class, metadataInfo.getKeyClassType(ourColumn4));
    }

    /**
     * Tests building meta data info.
     */
    @Test
    public void testBuildMetaDataInfoNoDate()
    {
        GeoPackageFeatureLayer layer = new GeoPackageFeatureLayer(ourPackageName, ourPackageFile, ourLayerName, 1);

        Map<String, Serializable> row = New.map();

        row.put(ourColumn1, "value1");
        row.put(ourColumn2, null);
        row.put(ourColumn3, null);
        row.put(GeoPackageColumns.GEOMETRY_COLUMN, new GeometryFactory().createPoint(new Coordinate(10, 11)));

        layer.getData().add(row);

        row = New.map();

        row.put(ourColumn1, "value2");
        row.put(ourColumn2, 10d);
        row.put(ourColumn3, null);
        row.put(GeoPackageColumns.GEOMETRY_COLUMN, new GeometryFactory().createPoint(new Coordinate(10, 11)));

        layer.getData().add(row);

        MetaDataInfoBuilder builder = new MetaDataInfoBuilder();
        MetaDataInfo metadataInfo = builder.buildMetaDataInfo(layer);

        assertTrue(metadataInfo.getKeyNames().contains(ourColumn1));
        assertTrue(metadataInfo.getKeyNames().contains(ourColumn2));
        assertTrue(metadataInfo.getKeyNames().contains(ourColumn3));
        assertTrue(metadataInfo.getKeyNames().contains(GeoPackageColumns.GEOMETRY_COLUMN));
        assertEquals(GeoPackageColumns.GEOMETRY_COLUMN, metadataInfo.getGeometryColumn());
        assertNull(metadataInfo.getKeyForSpecialType(TimeKey.DEFAULT));

        assertEquals(String.class, metadataInfo.getKeyClassType(ourColumn1));
        assertEquals(Double.class, metadataInfo.getKeyClassType(ourColumn2));
        assertEquals(Serializable.class, metadataInfo.getKeyClassType(ourColumn3));
        assertEquals(Geometry.class, metadataInfo.getKeyClassType(GeoPackageColumns.GEOMETRY_COLUMN));
    }

    /**
     * Tests building {@link MetaDataInfo} with empty columns.
     */
    @Test
    public void testBuildMetaDataInfoEmptyColumns()
    {
        GeoPackageFeatureLayer layer = new GeoPackageFeatureLayer(ourPackageName, ourPackageFile, ourLayerName, 1);

        Map<String, Serializable> row = New.map();

        layer.getData().add(row);

        MetaDataInfoBuilder builder = new MetaDataInfoBuilder();
        MetaDataInfo metadataInfo = builder.buildMetaDataInfo(layer);

        assertTrue(metadataInfo.getKeyNames().contains(GeoPackageColumns.GEOMETRY_COLUMN));
        assertEquals(GeoPackageColumns.GEOMETRY_COLUMN, metadataInfo.getGeometryColumn());
        assertEquals(Geometry.class, metadataInfo.getKeyClassType(GeoPackageColumns.GEOMETRY_COLUMN));
    }

    /**
     * Tests building metadata info without a geometry column.
     */
    @Test
    public void testBuildMetaDataInfoNoGeom()
    {
        GeoPackageFeatureLayer layer = new GeoPackageFeatureLayer(ourPackageName, ourPackageFile, ourLayerName, 1);

        Map<String, Serializable> row = New.map();

        row.put("column1", "value1");
        row.put("column2", "value2");

        layer.getData().add(row);

        MetaDataInfoBuilder builder = new MetaDataInfoBuilder();
        MetaDataInfo metadataInfo = builder.buildMetaDataInfo(layer);

        assertTrue(metadataInfo.getKeyNames().contains("column1"));
        assertTrue(metadataInfo.getKeyNames().contains("column2"));
        assertTrue(metadataInfo.getKeyNames().contains(GeoPackageColumns.GEOMETRY_COLUMN));
        assertEquals(GeoPackageColumns.GEOMETRY_COLUMN, metadataInfo.getGeometryColumn());

        assertEquals(String.class, metadataInfo.getKeyClassType(ourColumn1));
        assertEquals(String.class, metadataInfo.getKeyClassType(ourColumn2));
        assertEquals(Geometry.class, metadataInfo.getKeyClassType(GeoPackageColumns.GEOMETRY_COLUMN));
    }

    /**
     * Tests building metadata info without data.
     */
    @Test
    public void testBuildMetaDataInfoNoRows()
    {
        GeoPackageFeatureLayer layer = new GeoPackageFeatureLayer(ourPackageName, ourPackageFile, ourLayerName, 1);

        MetaDataInfoBuilder builder = new MetaDataInfoBuilder();
        MetaDataInfo metadataInfo = builder.buildMetaDataInfo(layer);

        assertNull(metadataInfo);
    }
}
