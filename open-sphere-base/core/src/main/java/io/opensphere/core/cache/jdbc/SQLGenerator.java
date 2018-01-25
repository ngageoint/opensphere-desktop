package io.opensphere.core.cache.jdbc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.matcher.MultiPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.model.time.TimeSpan;

/**
 * An SQL generator.
 */
public interface SQLGenerator
{
    /**
     * Generate SQL for adding a column to a table.
     *
     * @param tableName The table name.
     * @param columnName The column name.
     * @param columnType The SQL type of the column.
     * @return The SQL.
     */
    String generateAddColumn(String tableName, String columnName, String columnType);

    /**
     * Generate SQL to create an index on a table.
     *
     * @param indexName The name for the index.
     * @param tableName The table name.
     * @param unique Flag indicating if the values in the column shall be
     *            unique.
     * @param columnNames The column names that are part of the index.
     * @return The SQL.
     */
    String generateCreateIndex(String indexName, String tableName, boolean unique, String... columnNames);

    /**
     * Generate SQL to create a database sequence.
     *
     * @param sequenceName The sequence name.
     * @param startWith The first value the sequence should return.
     * @param incrementBy The increment for the sequence.
     * @return The SQL.
     */
    String generateCreateSequence(String sequenceName, long startWith, long incrementBy);

    /**
     * Generate SQL to create a table.
     *
     * @param tableName The table name.
     * @param columnNamesToTypes A map of column names to SQL types.
     * @return The SQL.
     */
    String generateCreateTable(String tableName, Map<String, String> columnNamesToTypes);

    /**
     * Generate SQL to create a table.
     *
     * @param tableName The table name.
     * @param columnNamesToTypes A map of column names to SQL types.
     * @param primaryKeyConstraint The primary key constraint on the table.
     * @param foreignKeyConstraints The foreign key constraints on the table.
     * @return The SQL.
     */
    String generateCreateTable(String tableName, Map<String, String> columnNamesToTypes,
            PrimaryKeyConstraint primaryKeyConstraint, ForeignKeyConstraint... foreignKeyConstraints);

    /**
     * Generate SQL to create a temporary table.
     *
     * @param tempTableName The table name.
     * @param columnNamesToTypes A map of column names to SQL types.
     * @param primaryKeyConstraint The primary key constraint on the table.
     * @return The SQL.
     */
    String generateCreateTemporaryTable(String tempTableName, Map<String, String> columnNamesToTypes,
            PrimaryKeyConstraint primaryKeyConstraint);

    /**
     * Build the SQL for deleting rows from the database.
     *
     * @param tableName The table name.
     * @param ids The ids to be deleted.
     * @return The SQL string.
     */
    String generateDelete(String tableName, int[] ids);

    /**
     * Build the SQL for deleting rows from the database.
     *
     * @param tableName The table name.
     * @param joinTableColumn The name of the join table containing the ids.
     * @return The SQL string.
     */
    String generateDelete(String tableName, JoinTableColumn joinTableColumn);

    /**
     * Generate SQL to delete a group from the data group table.
     *
     * @param groupId The id for the group.
     * @return The SQL.
     */
    String generateDeleteGroup(int groupId);

    /**
     * Generate SQL to drop all database objects.
     *
     * @return The SQL.
     */
    String generateDropAllObjects();

    /**
     * Generate SQL to drop a database table.
     *
     * @param tableName The table name.
     * @return The SQL.
     */
    String generateDropTable(String tableName);

    /**
     * Generate SQL to drop a database trigger.
     *
     * @param trigger The trigger name.
     * @return The SQL.
     */
    String generateDropTrigger(String trigger);

    /**
     * Generate SQL that will make some data groups expired.
     *
     * @param groupIds The group ids.
     * @return The SQL.
     */
    String generateExpireGroups(int[] groupIds);

    /**
     * Generate SQL that selects groups that have expired.
     *
     * @param thresholdMilliseconds How many milliseconds a group must be
     *            expired to be selected.
     * @return The SQL.
     */
    String generateGetExpiredGroups(long thresholdMilliseconds);

    /**
     * Generate SQL to get the next value from a sequence.
     *
     * @param sequenceName The name of the sequence.
     * @return The SQL.
     */
    String generateGetNextSequenceValue(String sequenceName);

    /**
     * Generate SQL for a parameterized insert.
     *
     * @param tableName The table name.
     * @param columnNames The column names.
     * @return The SQL.
     */
    String generateInsert(String tableName, String... columnNames);

    /**
     * Generate SQL for a parameterized merge.
     *
     * @param tableName The table name.
     * @param keyColumnNames The columns to use to match the rows in the merge.
     * @param columnNames The column names.
     * @return The SQL.
     */
    String generateMerge(String tableName, String[] keyColumnNames, String[] columnNames);

    /**
     * Generate SQL that selects nothing from a table <i>for update</i> which
     * will lock the table for the transaction.
     *
     * @param tableName The table name.
     * @return The SQL.
     */
    String generateNullSelectForUpdate(String tableName);

    /**
     * Generate SQL for getting data model categories from the database.
     *
     * @param selectSource Flag indicating if <tt>source</tt> should be
     *            selected.
     * @param selectFamily Flag indicating if <tt>family</tt> should be
     *            selected.
     * @param selectCategory Flag indicating if <tt>category</tt> should be
     *            selected.
     * @param distinct Flag indicating if only distinct categories should be
     *            returned.
     * @param joinTableColumn The optional join table name.
     * @param whereExpression The optional where expression.
     * @return The SQL string.
     */
    String generateRetrieveDataModelCategories(boolean selectSource, boolean selectFamily, boolean selectCategory,
            boolean distinct, JoinTableColumn joinTableColumn, String whereExpression);

    /**
     * Build the SQL for a group query.
     *
     * @param groupIds Optional array of group ids that may be used to restrict
     *            the query.
     * @param joinTableColumn Optional name of a temporary table that contains
     *            the group ids.
     * @param category Optional data model category filter. This may be
     *            {@code null} or any of its components may be {@code null} to
     *            indicate wildcards.
     * @param parameters The optional query parameters.
     * @param selectProperties Other properties that should be selected.
     * @param expirationRange If not {@code null}, the range that the groups'
     *            expiration times must lie within. If {@code null}, the groups'
     *            expiration times must be {@code null}.
     * @param critical If not {@code null}, the required criticality of the
     *            groups.
     * @return The SQL.
     * @throws CacheException If an unsupported parameter type is encountered.
     */
    String generateRetrieveGroupValuesSql(int[] groupIds, JoinTableColumn joinTableColumn, DataModelCategory category,
            Collection<? extends PropertyMatcher<?>> parameters, Collection<? extends PropertyDescriptor<?>> selectProperties,
            TimeSpan expirationRange, Boolean critical) throws CacheException;

    /**
     * Build the SQL for a model ids query.
     *
     * @param groupIds The ids of the groups to query.
     * @param parameters The optional query parameters.
     * @param joinTableNames Join tables containing values for
     *            {@link MultiPropertyMatcher}s contained in the
     *            <tt>parameters</tt> collection, in the same order.
     * @param orderSpecifiers The optional order specifiers.
     * @param startIndex The index of the first id to be returned.
     * @param limit The limit on the number of ids to be returned.
     * @param typeMapper The type mapper used to produce the column names for
     *            the parameters and order specifiers.
     * @return The SQL.
     */
    String generateRetrieveIds(int[] groupIds, Collection<? extends PropertyMatcher<?>> parameters, List<String> joinTableNames,
            List<? extends OrderSpecifier> orderSpecifiers, int startIndex, int limit, TypeMapper typeMapper);

    /**
     * Generate the SQL for retrieving the current schema version.
     *
     * @return The SQL.
     */
    String generateRetrieveSchemaVersion();

    /**
     * Generate the SQL for retrieving property values from the database for a
     * single data model.
     *
     * @param dataId The data id.
     * @param tableName The data table name.
     * @param columnNames The column names.
     * @return The SQL.
     */
    String generateRetrieveValues(int dataId, String tableName, Collection<String> columnNames);

    /**
     * Generate the query for retrieving property values from the database.
     *
     * @param joinTableName A temporary table containing the data ids in its
     *            {@link ColumnNames#JOIN_ID} column and the order in its
     *            {@link ColumnNames#SEQUENCE} column.
     * @param tableName The data table name.
     * @param columnNames The column names.
     * @return The SQL.
     */
    String generateRetrieveValues(String joinTableName, String tableName, Collection<String> columnNames);

    /**
     * Generate the SQL for retrieving property value sizes from the database
     * for a single data model.
     *
     * @param dataId The data id.
     * @param tableName The data table name.
     * @param columnNames The column names.
     * @return The SQL.
     */
    String generateRetrieveValueSizes(int dataId, String tableName, Collection<String> columnNames);

    /**
     * Generate the query for retrieving property value sizes from the database.
     *
     * @param joinTableName A temporary table containing the data ids in its
     *            {@link ColumnNames#JOIN_ID} column and the order in its
     *            {@link ColumnNames#SEQUENCE} column.
     * @param tableName The data table name.
     * @param columnNames The column names.
     * @return The SQL.
     */
    String generateRetrieveValueSizes(String joinTableName, String tableName, Collection<String> columnNames);

    /**
     * Generate SQL for an update.
     *
     * @param tableName The table name.
     * @param columnNamesToValues A map of column names to their new values.
     * @param whereExpression An optional where expression.
     * @return The SQL.
     */
    String generateUpdate(String tableName, Map<String, String> columnNamesToValues, String whereExpression);

    /**
     * Generate an SQL fragment to be inserted into a <i>where</i> clause that
     * specifies that the values from <tt>columnName</tt> must be one of the
     * specified values.
     *
     * @param columnName The name of the column being tested.
     * @param values The possible values.
     * @return The SQL.
     */
    String generateWhereExpression(String columnName, int[] values);

    /**
     * Generate an SQL fragment to be inserted into a <i>where</i> clause that
     * specifies that the values from <tt>columnName</tt> must be in the
     * specified column of the specified join table.
     *
     * @param columnName The name of the column being tested.
     * @param joinTableColumn The name of the join table.
     * @return The SQL.
     */
    String generateWhereExpression(String columnName, JoinTableColumn joinTableColumn);
}
