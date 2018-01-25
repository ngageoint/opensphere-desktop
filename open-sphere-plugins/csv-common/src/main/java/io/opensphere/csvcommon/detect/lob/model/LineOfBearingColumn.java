package io.opensphere.csvcommon.detect.lob.model;

import io.opensphere.importer.config.ColumnType;

/**
 * The Class LineOfBearingColumn. The name and index of a line of bearing
 * column.
 */
public class LineOfBearingColumn
{
    /** The Column name. */
    private final String myColumnName;

    /** The Column index. */
    private final int myColumnIndex;

    /**
     * Instantiates a new line of bearing column.
     *
     * @param colName the col name
     * @param columnIndex the column index
     */
    public LineOfBearingColumn(String colName, int columnIndex)
    {
        myColumnName = colName;
        myColumnIndex = columnIndex;
    }

    /**
     * Gets the column index.
     *
     * @return the column index
     */
    public int getColumnIndex()
    {
        return myColumnIndex;
    }

    /**
     * Gets the column name.
     *
     * @return the column name
     */
    public String getColumnName()
    {
        return myColumnName;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public ColumnType getType()
    {
        return ColumnType.LOB;
    }
}
