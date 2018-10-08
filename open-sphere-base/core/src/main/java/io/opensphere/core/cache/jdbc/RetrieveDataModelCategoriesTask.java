package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.util.SharedObjectPool;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;

/**
 * Database task for retrieving data model categories.
 */
public class RetrieveDataModelCategoriesTask extends DatabaseTask implements StatementUser<List<DataModelCategory>>
{
    /**
     * Flag indicating if only distinct categories should be returned.
     */
    private final boolean myDistinct;

    /** The group ids. */
    private final int[] myGroupIds;

    /** The result count. */
    private transient int myResultCount;

    /**
     * Flag indicating that the category of the data model categories needs to
     * be populated.
     */
    private final boolean mySelectCategory;

    /**
     * Flag indicating that the family of the data model categories needs to be
     * populated.
     */
    private final boolean mySelectFamily;

    /**
     * Flag indicating that the source of the data model categories needs to be
     * populated.
     */
    private final boolean mySelectSource;

    /**
     * Constructor.
     *
     * @param groupIds The group ids.
     * @param selectSource Flag indicating that the source of the data model
     *            categories needs to be populated.
     * @param selectFamily Flag indicating that the family of the data model
     *            categories needs to be populated.
     * @param selectCategory Flag indicating that the category of the data model
     *            categories needs to be populated.
     * @param distinct Flag indicating if only distinct categories should be
     *            returned.
     * @param databaseTaskFactory The database task factory.
     */
    protected RetrieveDataModelCategoriesTask(int[] groupIds, boolean selectSource, boolean selectFamily, boolean selectCategory,
            boolean distinct, DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        Utilities.checkNull(groupIds, "groupIds");
        myGroupIds = groupIds.clone();
        mySelectSource = selectSource;
        mySelectFamily = selectFamily;
        mySelectCategory = selectCategory;
        myDistinct = distinct;
    }

    @Override
    public String getTimingMessage()
    {
        return myResultCount + " data model categories retrieved from cache in ";
    }

    @Override
    public List<DataModelCategory> run(Connection conn, Statement stmt) throws CacheException
    {
        try
        {
            JoinTableColumn joinTableColumn;
            String whereExpression;
            if (getGroupIds().length == 1)
            {
                joinTableColumn = null;
                whereExpression = new StringBuilder().append(SQL.DATA_GROUP_GROUP_ID).append(SQL.EQUALS).append(getGroupIds()[0])
                        .toString();
            }
            else
            {
                whereExpression = null;
                joinTableColumn = new JoinTableColumn(getDatabaseTaskFactory().getCreateIdJoinTableTask(getGroupIds(),
                        ColumnNames.JOIN_ID, isDistinct() ? Nulls.STRING : ColumnNames.SEQUENCE).run(conn, stmt));
            }

            String sql = getSQLGenerator().generateRetrieveDataModelCategories(isSelectSource(), isSelectFamily(),
                    isSelectCategory(), isDistinct(), joinTableColumn, whereExpression);
            ResultSet rs = getCacheUtilities().executeQuery(stmt, sql);
            try
            {
                return convertResultSetToDataModelCategories(rs);
            }
            finally
            {
                rs.close();
            }
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to read values from cache: " + e, e);
        }
    }

    /**
     * Extract {@link DataModelCategory}s from a result set.
     *
     * @param rs The result set.
     * @return The {@link DataModelCategory}s.
     * @throws SQLException If a database error occurs.
     */
    protected List<DataModelCategory> convertResultSetToDataModelCategories(ResultSet rs) throws SQLException
    {
        List<DataModelCategory> results;
        if (rs.next())
        {
            SharedObjectPool<DataModelCategory> dmcPool = getGroupIds().length > 1 ? new SharedObjectPool<>()
                    : null;
            results = New.list();
            int index;
            do
            {
                index = 1;
                String sourceResult = isSelectSource() ? rs.getString(index++) : null;
                String familyResult = isSelectFamily() ? rs.getString(index++) : null;
                String categoryResult = isSelectCategory() ? rs.getString(index++) : null;
                if (dmcPool == null)
                {
                    results.add(new DataModelCategory(sourceResult, familyResult, categoryResult));
                }
                else
                {
                    results.add(dmcPool.get(new DataModelCategory(sourceResult, familyResult, categoryResult)));
                }
            }
            while (rs.next());
        }
        else
        {
            results = Collections.emptyList();
        }
        myResultCount = results.size();
        return results;
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
     * Accessor for the distinct flag.
     *
     * @return The distinct flag.
     */
    protected final boolean isDistinct()
    {
        return myDistinct;
    }

    /**
     * Accessor for the selectCategory.
     *
     * @return The selectCategory.
     */
    protected boolean isSelectCategory()
    {
        return mySelectCategory;
    }

    /**
     * Accessor for the selectFamily.
     *
     * @return The selectFamily.
     */
    protected boolean isSelectFamily()
    {
        return mySelectFamily;
    }

    /**
     * Accessor for the selectSource.
     *
     * @return The selectSource.
     */
    protected boolean isSelectSource()
    {
        return mySelectSource;
    }
}
