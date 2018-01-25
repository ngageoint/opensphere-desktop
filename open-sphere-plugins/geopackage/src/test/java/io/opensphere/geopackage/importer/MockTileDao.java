package io.opensphere.geopackage.importer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import io.opensphere.core.util.collections.New;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileColumn;
import mil.nga.geopackage.tiles.user.TileConnection;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.geopackage.tiles.user.TileTable;

/**
 * Used for tests.
 */
public class MockTileDao extends TileDao
{
    /**
     * The results to return in query.
     */
    private TileResultSet myResults;

    /**
     * The rows to return in a query.
     */
    private List<TileRow> myRows;

    /**
     * Constructs a new mock.
     *
     * @param db The db.
     * @param tileMatrixSet The matrix.
     */
    public MockTileDao(GeoPackageConnection db, TileMatrixSet tileMatrixSet)
    {
        super("database", db, new TileConnection(db), tileMatrixSet, New.list(),
                new TileTable("tile",
                        New.list(TileColumn.createIdColumn(0), TileColumn.createZoomLevelColumn(1),
                                TileColumn.createTileColumnColumn(2), TileColumn.createTileRowColumn(3),
                                TileColumn.createTileDataColumn(4))));
    }

    /**
     * Constructs a new mock.
     *
     * @param db The db.
     * @param tileMatrixSet The matrix.
     * @param matrices The tile matrices.
     * @param count The number of results to return.
     */
    public MockTileDao(GeoPackageConnection db, TileMatrixSet tileMatrixSet, List<TileMatrix> matrices, int count)
    {
        super("database", db, new TileConnection(db), tileMatrixSet, matrices,
                new TileTable("tile",
                        New.list(TileColumn.createIdColumn(0), TileColumn.createZoomLevelColumn(1),
                                TileColumn.createTileColumnColumn(2), TileColumn.createTileRowColumn(3),
                                TileColumn.createTileDataColumn(4))));
        myRows = New.list();
        for (int i = 0; i < count; i++)
        {
            TileRow row = newRow();
            row.setZoomLevel(i);
            myRows.add(row);
        }

        myResults = new MockTileResultSet(getTable(), myRows.size(), myRows);
    }

    /**
     * Constructs a new mock.
     *
     * @param db The db.
     * @param tileMatrixSet The matrix.
     * @param matrices Test tile matrices.
     */
    public MockTileDao(GeoPackageConnection db, TileMatrixSet tileMatrixSet, List<TileMatrix> matrices)
    {
        super("database", db, new TileConnection(db), tileMatrixSet, matrices,
                new TileTable("tile",
                        New.list(TileColumn.createIdColumn(0), TileColumn.createZoomLevelColumn(1),
                                TileColumn.createTileColumnColumn(2), TileColumn.createTileRowColumn(3),
                                TileColumn.createTileDataColumn(4))));
    }

    @Override
    public BoundingBox getBoundingBox()
    {
        return new BoundingBox(10, 11, 10, 11);
    }

    @Override
    public int count(long zoomLevel)
    {
        assertEquals(8, zoomLevel);
        return 100;
    }

    /**
     * Gets the rows that will be returned in a query call.
     *
     * @return The rows that will be returned in a query.
     */
    public List<TileRow> getRows()
    {
        return myRows;
    }

    @Override
    public TileResultSet queryForAll()
    {
        return myResults;
    }

    @Override
    public TileResultSet queryForTile(long zoomLevel)
    {
        TileResultSet resultSet = myResults;

        if (zoomLevel == 8)
        {
            resultSet = new MockTileResultSet(getTable(), 1, New.list(myRows.get(0), myRows.get(1)));
        }

        return resultSet;
    }

    @Override
    public TileRow queryForTile(long column, long row, long zoomLevel)
    {
        TileRow tileRow = null;
        if (column == 0 && row == 1)
        {
            tileRow = newRow();
            tileRow.setTileData(new byte[] { 1, 2, 3, 4 });
        }

        return tileRow;
    }
}
