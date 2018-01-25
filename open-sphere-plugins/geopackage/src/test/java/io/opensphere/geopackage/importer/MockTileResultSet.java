package io.opensphere.geopackage.importer;

import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.geopackage.tiles.user.TileTable;

/**
 * A mock {@link TileResultSet} used for testing.
 */
public class MockTileResultSet extends TileResultSet
{
    /**
     * The current row.
     */
    private TileRow myCurrentRow;

    /**
     * The rows in the result set.
     */
    private final Iterator<TileRow> myRows;

    /**
     * Constructs a new result set.
     *
     * @param table The table.
     * @param count The number of rows.
     * @param rows The rows to return.
     */
    public MockTileResultSet(TileTable table, int count, List<TileRow> rows)
    {
        super(table, null, count);
        myRows = rows.iterator();
    }

    @Override
    public TileRow getRow()
    {
        return myCurrentRow;
    }

    @Override
    public boolean moveToNext()
    {
        if (myRows.hasNext())
        {
            myCurrentRow = myRows.next();
        }
        else
        {
            myCurrentRow = null;
        }

        return myCurrentRow != null;
    }
}
