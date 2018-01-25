package mil.nga.geopackage.features.user;

import java.util.Map;

import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageColumns;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryType;

/**
 * Mocks a {@link FeatureRow} and is used for testing.
 */
public class MockFeatureRow extends FeatureRow
{
    /**
     * The test data fro the row.
     */
    private final Map<String, Object> myData;

    /**
     * The test geometry.
     */
    private GeoPackageGeometryData myGeometry;

    /**
     * Constructs a new mock feature row.
     *
     * @param table The test table.
     * @param geometry The test geomerty to return.
     */
    public MockFeatureRow(FeatureTable table, Geometry geometry)
    {
        this(table, geometry, New.map());
    }

    /**
     * Constructs a new mock feature row.
     *
     * @param table The test table.
     * @param geometry The test geometry to return.
     * @param data The test data for this row.
     */
    public MockFeatureRow(FeatureTable table, Geometry geometry, Map<String, Object> data)
    {
        super(table);
        if (geometry != null)
        {
            myGeometry = new GeoPackageGeometryData(1L);
            myGeometry.setGeometry(geometry);
        }
        else
        {
            myGeometry = null;
        }
        myData = data;
    }

    @Override
    public String[] getColumnNames()
    {
        String[] columnNames = new String[myData.keySet().size()];
        return myData.keySet().toArray(columnNames);
    }

    @Override
    public GeoPackageGeometryData getGeometry()
    {
        return myGeometry;
    }

    @Override
    public FeatureColumn getGeometryColumn()
    {
        return new FeatureColumn(0, "geom", GeoPackageDataType.BLOB, null, true, null, false, GeometryType.POINT);
    }

    @Override
    public Object getValue(String columnName)
    {
        return myData.get(columnName);
    }

    @Override
    public void setGeometry(GeoPackageGeometryData geometryData)
    {
        myGeometry = geometryData;
    }

    @Override
    public void setValue(String columnName, Object value)
    {
        assert !GeoPackageColumns.ID_COLUMN.equals(columnName);
        myData.put(columnName, value);
    }
}
