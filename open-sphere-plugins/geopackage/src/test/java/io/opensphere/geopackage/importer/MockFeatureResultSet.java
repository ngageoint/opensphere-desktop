package io.opensphere.geopackage.importer;

import java.util.List;

import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.MockFeatureRow;

/**
 * Mock's a {@link FeatureResultSet} used for testing.
 */
public class MockFeatureResultSet extends FeatureResultSet
{
    /**
     * The current index.
     */
    private int myCurrentIndex = -1;

    /**
     * The rows in the result set.
     */
    private final List<MockFeatureRow> myRows;

    /**
     * Used to test cancelling.
     */
    private final CancellableTaskActivity myTa;

    /**
     * Constructs a new mock result set.
     *
     * @param rows The data.
     * @param ta Used to test cancelling.
     */
    public MockFeatureResultSet(List<MockFeatureRow> rows, CancellableTaskActivity ta)
    {
        super(null, null, rows.size());
        myRows = rows;
        myTa = ta;
    }

    @Override
    public FeatureRow getRow()
    {
        FeatureRow row = null;
        if (myCurrentIndex < myRows.size())
        {
            row = myRows.get(myCurrentIndex);
        }

        return row;
    }

    @Override
    public boolean moveToNext()
    {
        myCurrentIndex++;

        if (myCurrentIndex == 1 && myTa != null)
        {
            myTa.setCancelled(true);
        }

        return myCurrentIndex < myRows.size();
    }
}
