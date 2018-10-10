package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.ConnectionAppropriator.ConnectionUser;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;

/**
 * Database task that ensures that the data tables for some groups have the
 * correct columns. Adding columns copies all the data in the table for each
 * column added, so this should be avoided.
 */
public class EnsureColumnsTask extends DatabaseTask implements ConnectionUser<Void>
{
    /** The group ids. */
    private final int[] myGroupIds;

    /** The property descriptors. */
    private Collection<? extends PropertyDescriptor<?>> myPropertyDescriptors;

    /**
     * Constructor.
     *
     * @param groupIds The group ids.
     * @param propertyDescriptors The property descriptors.
     * @param databaseTaskFactory The database task factory.
     */
    public EnsureColumnsTask(int[] groupIds, Collection<? extends PropertyDescriptor<?>> propertyDescriptors,
            DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        Utilities.checkNull(groupIds, "groupIds");
        Utilities.checkNull(propertyDescriptors, "propertyDescriptors");
        myGroupIds = groupIds.clone();
        myPropertyDescriptors = propertyDescriptors;
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to ensure columns on " + getGroupIds().length + " groups: ";
    }

    @Override
    public Void run(Connection conn) throws CacheException
    {
        List<String> columnNames = getTypeMapper().getColumnNames(getPropertyDescriptors());

        if (!columnNames.isEmpty())
        {
            ensureColumns(conn, columnNames);
        }

        return null;
    }

    /**
     * Add a column to a table.
     *
     * @param dataTableName The table name.
     * @param columnName The column name.
     * @param columnType The SQL type for the column.
     * @param conn The database connection.
     * @throws CacheException If there is a database error.
     */
    protected void addColumn(String dataTableName, String columnName, String columnType, Connection conn) throws CacheException
    {
        String sql = getSQLGenerator().generateAddColumn(dataTableName, columnName, columnType);
        getCacheUtilities().execute(sql, conn);
    }

    /**
     * Ensure the correct columns are created.
     *
     * @param conn The database connection.
     * @param columnNames The column names.
     * @throws CacheException If there is a database error.
     */
    protected void ensureColumns(Connection conn, List<String> columnNames) throws CacheException
    {
        for (int groupId : getGroupIds())
        {
            Collection<String> missingColumns = getMissingColumns(groupId, columnNames, conn);
            if (!missingColumns.isEmpty())
            {
                String dataTableName = TableNames.getDataTableName(groupId);

                Map<String, String> columnNamesToTypes = getTypeMapper().getColumnNamesToTypes(getPropertyDescriptors());
                for (String columnName : missingColumns)
                {
                    String columnType = columnNamesToTypes.get(columnName);
                    addColumn(dataTableName, columnName, columnType, conn);
                }
            }
        }
    }

    /**
     * Accessor for the groupIds.
     *
     * @return The groupIds.
     */
    protected int[] getGroupIds()
    {
        return myGroupIds;
    }

    /**
     * Get the missing data table columns for a data group.
     *
     * @param groupId The group id.
     * @param neededColumns The needed column names.
     * @param conn An optional database connection.
     *
     * @return The missing column names.
     * @throws CacheException If there is a database error.
     */
    protected Collection<String> getMissingColumns(final int groupId, Collection<String> neededColumns, Connection conn)
            throws CacheException
    {
        Set<String> existingColumns = getDatabaseState().getExistingColumnsForGroup(groupId);

        if (existingColumns.containsAll(neededColumns))
        {
            return Collections.emptySet();
        }

        Set<String> columnsFromDb = getCacheUtilities().getColumns(conn, TableNames.getDataTableName(groupId), Nulls.STRING);

        Collection<String> result = New.set(neededColumns);
        synchronized (existingColumns)
        {
            existingColumns.addAll(columnsFromDb);
            result.removeAll(existingColumns);
        }

        return result;
    }

    /**
     * Accessor for the propertyDescriptors.
     *
     * @return The propertyDescriptors.
     */
    protected Collection<? extends PropertyDescriptor<?>> getPropertyDescriptors()
    {
        return myPropertyDescriptors;
    }

    /**
     * Mutator for the propertyDescriptors.
     *
     * @param propertyDescriptors The propertyDescriptors to set.
     */
    protected void setPropertyDescriptors(Collection<? extends PropertyDescriptor<?>> propertyDescriptors)
    {
        myPropertyDescriptors = propertyDescriptors;
    }
}
