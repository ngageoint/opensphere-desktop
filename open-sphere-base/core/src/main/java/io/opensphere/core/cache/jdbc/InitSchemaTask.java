package io.opensphere.core.cache.jdbc;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;

/**
 * A task for initializing the schema. This does not affect the version table.
 */
public class InitSchemaTask extends DatabaseTask implements StatementUser<Void>
{
    /**
     * Constructor.
     *
     * @param databaseTaskFactory The database task factory.
     */
    public InitSchemaTask(DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to initialize schema: ";
    }

    @Override
    public Void run(Connection conn, Statement stmt) throws CacheException
    {
        Collection<String> tableNames = getCacheUtilities().getTableNames(conn, Nulls.STRING, Nulls.STRING, "%DATA_GROUP");

        if (!tableNames.contains(TableNames.DATA_GROUP))
        {
            Map<String, String> columnNamesToTypes = New.insertionOrderMap(6);
            columnNamesToTypes.put(ColumnNames.GROUP_ID, getTypeMapper().getSqlColumnDefinition(Integer.class, false));
            String stringColumn = getTypeMapper().getSqlColumnDefinition(String.class, false);
            columnNamesToTypes.put(ColumnNames.SOURCE, stringColumn);
            columnNamesToTypes.put(ColumnNames.FAMILY, stringColumn);
            columnNamesToTypes.put(ColumnNames.CATEGORY, stringColumn);
            columnNamesToTypes.put(ColumnNames.CREATION_TIME, getTypeMapper().getSqlColumnDefinition(Long.class, false));
            columnNamesToTypes.put(ColumnNames.EXPIRATION_TIME, getTypeMapper().getSqlColumnDefinition(Long.class, true));
            columnNamesToTypes.put(ColumnNames.CRITICAL, getTypeMapper().getSqlColumnDefinition(Boolean.class, "FALSE", false));
            PrimaryKeyConstraint primaryKey = new PrimaryKeyConstraint(ColumnNames.GROUP_ID);
            String sql;
            sql = getSQLGenerator().generateCreateTable(TableNames.DATA_GROUP, columnNamesToTypes, primaryKey);
            getCacheUtilities().execute(sql, stmt);
        }
        else
        {
            if (getCacheUtilities().getColumns(conn, TableNames.DATA_GROUP, ColumnNames.CRITICAL).isEmpty())
            {
                String sql = getSQLGenerator().generateAddColumn(TableNames.DATA_GROUP, ColumnNames.CRITICAL,
                        getTypeMapper().getSqlColumnDefinition(Boolean.class, "FALSE", false));
                getCacheUtilities().execute(sql, conn);
            }
        }

        Collection<String> indexNames = getCacheUtilities().getIndexNames(conn, Nulls.STRING, Nulls.STRING,
                TableNames.DATA_GROUP);
        if (!indexNames.contains("DATA_GROUP_INDEX1"))
        {
            String sql = getSQLGenerator().generateCreateIndex("DATA_GROUP_INDEX1", TableNames.DATA_GROUP, false,
                    ColumnNames.FAMILY, ColumnNames.CATEGORY);
            getCacheUtilities().execute(sql, stmt);
        }
        if (!indexNames.contains("DATA_GROUP_INDEX2"))
        {
            String sql = getSQLGenerator().generateCreateIndex("DATA_GROUP_INDEX2", TableNames.DATA_GROUP, false,
                    ColumnNames.SOURCE, ColumnNames.FAMILY, ColumnNames.CATEGORY);
            getCacheUtilities().execute(sql, stmt);
        }

        String geometryGroupTableName = TableNames.getGroupTableName(Geometry.class);
        if (!tableNames.contains(geometryGroupTableName))
        {
            createGeometryGroupTable(geometryGroupTableName, conn, stmt);
        }
        String timespanGroupTableName = TableNames.getGroupTableName(TimeSpan.class);
        if (!tableNames.contains(timespanGroupTableName))
        {
            createTimeSpanGroupTable(timespanGroupTableName, conn, stmt);
        }
        String serializableGroupTableName = TableNames.getGroupTableName(Serializable.class);
        if (!tableNames.contains(serializableGroupTableName))
        {
            createSerializableGroupTable(serializableGroupTableName, conn, stmt);
        }

        getCacheUtilities().execute(getSQLGenerator().generateCreateSequence(SQL.GROUP_ID_SEQUENCE, 1, 1), stmt);

        return null;
    }

    /**
     * Create the geometry group table.
     *
     * @param groupTableName The name of the table.
     * @param conn A database connection.
     * @param stmt A database statement.
     *
     * @throws CacheException If there is a database error.
     */
    protected void createGeometryGroupTable(String groupTableName, Connection conn, Statement stmt) throws CacheException
    {
        Map<String, String> columnNamesToTypes = New.insertionOrderMap();
        columnNamesToTypes.put(ColumnNames.GROUP_ID, getTypeMapper().getSqlColumnDefinition(Integer.class, false));
        columnNamesToTypes.put(ColumnNames.PROPERTY, getTypeMapper().getSqlColumnDefinition(String.class, false));
        columnNamesToTypes.put(ColumnNames.VALUE, getTypeMapper().getSqlColumnDefinition(Geometry.class, false));

        PrimaryKeyConstraint primaryKey = new PrimaryKeyConstraint(ColumnNames.GROUP_ID);
        ForeignKeyConstraint foreignKey = new ForeignKeyConstraint(ColumnNames.GROUP_ID, TableNames.DATA_GROUP,
                ColumnNames.GROUP_ID);

        String sql = getSQLGenerator().generateCreateTable(groupTableName, columnNamesToTypes, primaryKey, foreignKey);
        getCacheUtilities().execute(sql, stmt);

        createGroupTableIndex(groupTableName, conn, stmt);
    }

    /**
     * Create an index on the group_id of a group table.
     *
     * @param groupTableName The group table name.
     * @param conn A database connection.
     * @param stmt The database statement.
     * @throws CacheException If there is a database error.
     */
    protected void createGroupTableIndex(String groupTableName, Connection conn, Statement stmt) throws CacheException
    {
        String sql = getSQLGenerator().generateCreateIndex(groupTableName + "_INDEX", groupTableName, true, ColumnNames.GROUP_ID,
                ColumnNames.PROPERTY);
        getCacheUtilities().execute(sql, stmt);
    }

    /**
     * Create the geometry group table.
     *
     * @param groupTableName The name of the table.
     * @param conn A database connection.
     * @param stmt A database statement.
     *
     * @throws CacheException If there is a database error.
     */
    protected void createSerializableGroupTable(String groupTableName, Connection conn, Statement stmt) throws CacheException
    {
        Map<String, String> columnNamesToTypes = New.insertionOrderMap();
        columnNamesToTypes.put(ColumnNames.GROUP_ID, getTypeMapper().getSqlColumnDefinition(Integer.class, false));
        columnNamesToTypes.put(ColumnNames.PROPERTY, getTypeMapper().getSqlColumnDefinition(String.class, false));
        columnNamesToTypes.put(ColumnNames.VALUE, getTypeMapper().getSqlColumnDefinition(Serializable.class, true));

        PrimaryKeyConstraint primaryKey = new PrimaryKeyConstraint(ColumnNames.GROUP_ID);
        ForeignKeyConstraint foreignKey = new ForeignKeyConstraint(ColumnNames.GROUP_ID, TableNames.DATA_GROUP,
                ColumnNames.GROUP_ID);

        String sql = getSQLGenerator().generateCreateTable(groupTableName, columnNamesToTypes, primaryKey, foreignKey);
        getCacheUtilities().execute(sql, stmt);

        createGroupTableIndex(groupTableName, conn, stmt);
    }

    /**
     * Create the time span group table.
     *
     * @param groupTableName The group table name.
     * @param conn A database connection.
     * @param stmt A database statement.
     *
     * @throws CacheException If there is a database error.
     */
    protected void createTimeSpanGroupTable(String groupTableName, Connection conn, Statement stmt) throws CacheException
    {
        Map<String, String> columnNamesToTypes = New.insertionOrderMap();
        columnNamesToTypes.put(ColumnNames.GROUP_ID, getTypeMapper().getSqlColumnDefinition(Integer.class, false));
        columnNamesToTypes.put(ColumnNames.PROPERTY, getTypeMapper().getSqlColumnDefinition(String.class, false));
        columnNamesToTypes.put(ColumnNames.VALUE_START, getTypeMapper().getSqlColumnDefinition(Long.class, false));
        columnNamesToTypes.put(ColumnNames.VALUE_END, getTypeMapper().getSqlColumnDefinition(Long.class, false));

        PrimaryKeyConstraint primaryKey = new PrimaryKeyConstraint(ColumnNames.GROUP_ID, ColumnNames.PROPERTY);
        ForeignKeyConstraint foreignKey = new ForeignKeyConstraint(ColumnNames.GROUP_ID, TableNames.DATA_GROUP,
                ColumnNames.GROUP_ID);

        String sql = getSQLGenerator().generateCreateTable(groupTableName, columnNamesToTypes, primaryKey, foreignKey);
        getCacheUtilities().execute(sql, stmt);

        createGroupTableIndex(groupTableName, conn, stmt);
    }
}
