package io.opensphere.core.cache.jdbc;

import io.opensphere.core.util.Utilities;

/**
 * A column in a table that is to be used for joining.
 */
public class JoinTableColumn
{
    /** The column name. */
    private final String myColumnName;

    /** The table name. */
    private final String myTableName;

    /**
     * Constructor that uses the standard column name (
     * {@link ColumnNames#JOIN_ID}).
     *
     * @param tableName The table name.
     */
    public JoinTableColumn(String tableName)
    {
        this(tableName, ColumnNames.JOIN_ID);
    }

    /**
     * Constructor.
     *
     * @param tableName The table name.
     * @param columnName The column name.
     */
    public JoinTableColumn(String tableName, String columnName)
    {
        myTableName = Utilities.checkNull(tableName, "tableName");
        myColumnName = Utilities.checkNull(columnName, "columnName");
    }

    /**
     * Get the column name.
     *
     * @return The column name.
     */
    public String getColumnName()
    {
        return myColumnName;
    }

    /**
     * Get the table name.
     *
     * @return The table name.
     */
    public String getTableName()
    {
        return myTableName;
    }
}
