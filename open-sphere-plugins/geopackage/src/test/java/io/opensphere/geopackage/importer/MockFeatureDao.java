package io.opensphere.geopackage.importer;

import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureConnection;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.features.user.MockFeatureRow;
import mil.nga.sf.GeometryType;

/**
 * Used for tests.
 */
public class MockFeatureDao extends FeatureDao
{
    /**
     * The test data for the dao.
     */
    private final List<MockFeatureRow> myData = New.list();

    /**
     * Used to test cancelling.
     */
    private final CancellableTaskActivity myTa;

    /**
     * Constructs a new mock.
     *
     * @param db The db.
     * @param geometryColumns The {@link GeometryColumns}.
     * @param ta The cancellable task activity to cancel when a row is inserted,
     *            or null if there is nothing to cancel.
     */
    public MockFeatureDao(GeoPackageConnection db, GeometryColumns geometryColumns, CancellableTaskActivity ta)
    {
        super("database", db, new FeatureConnection(db), geometryColumns,
                new FeatureTable("feature", New.list(FeatureColumn.createPrimaryKeyColumn(0, "key"),
                        FeatureColumn.createGeometryColumn(1, "geom", GeometryType.POINT, false, null))));
        myTa = ta;
    }

    /**
     * Constructs a new mock.
     *
     * @param db The db.
     * @param geometryColumns The {@link GeometryColumns}.
     * @param data The data to test with.
     * @param ta Used to test cancelling.
     */
    public MockFeatureDao(GeoPackageConnection db, GeometryColumns geometryColumns, List<MockFeatureRow> data,
            CancellableTaskActivity ta)
    {
        super("database", db, new FeatureConnection(db), geometryColumns,
                new FeatureTable("feature", New.list(FeatureColumn.createPrimaryKeyColumn(0, "key"),
                        FeatureColumn.createGeometryColumn(1, "geom", GeometryType.POINT, false, null))));
        myData.addAll(data);
        myTa = ta;
    }

    @Override
    public int count()
    {
        return 10000;
    }

    @Override
    public long insert(FeatureRow row)
    {
        if (row instanceof MockFeatureRow)
        {
            myData.add((MockFeatureRow)row);
        }

        if (myTa != null)
        {
            myTa.setCancelled(true);
        }

        return 0;
    }

    @Override
    public FeatureRow newRow()
    {
        return new MockFeatureRow(getTable(), null);
    }

    @Override
    public FeatureResultSet queryForAll()
    {
        return new MockFeatureResultSet(myData, myTa);
    }
}
