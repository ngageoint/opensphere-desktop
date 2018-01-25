package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.ConnectionAppropriator.ConnectionUser;
import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Database task that ensures that the data tables for some groups have indices
 * for columns associated with some property matchers.
 */
public class EnsureIndicesTask extends DatabaseTask implements ConnectionUser<Void>
{
    /** The group ids. */
    private final int[] myGroupIds;

    /** The property matchers. */
    private final Collection<? extends PropertyMatcher<?>> myPropertyMatchers;

    /** The number of created indices. */
    private transient int myCreatedIndexCount;

    /**
     * Constructor.
     *
     * @param groupIds The group ids.
     * @param propertyMatchers The property matchers.
     * @param databaseTaskFactory The database task factory.
     */
    public EnsureIndicesTask(int[] groupIds, Collection<? extends PropertyMatcher<?>> propertyMatchers,
            DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        Utilities.checkNull(groupIds, "groupIds");
        Utilities.checkNull(propertyMatchers, "propertyMatchers");
        myGroupIds = groupIds.clone();
        myPropertyMatchers = propertyMatchers;
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to ensure indices on " + getGroupIds().length + " groups (created " + myCreatedIndexCount + " indices): ";
    }

    @Override
    public Void run(Connection conn) throws CacheException
    {
        // First determine what columns are to be indexed.
        List<String> columnsToBeIndexed = New.list();
        for (PropertyMatcher<?> matcher : getPropertyMatchers())
        {
            // Do not use an ordinary index for a geometry matcher.
            if (!(matcher instanceof GeometryMatcher))
            {
                List<String> columnNames = getTypeMapper().getColumnNames(matcher.getPropertyDescriptor());
                columnsToBeIndexed.addAll(columnNames);
            }
        }

        if (!columnsToBeIndexed.isEmpty())
        {
            if (columnsToBeIndexed.size() > 1)
            {
                Collections.sort(columnsToBeIndexed);
            }
            for (int groupId : getGroupIds())
            {
                String tableName = TableNames.getDataTableName(groupId);

                StringBuilder indexName = new StringBuilder(32).append("\"INDEX_").append(tableName).append('_');
                StringUtilities.join(indexName, "_", columnsToBeIndexed).append('"');

                StringBuilder columns = new StringBuilder(32).append('"');
                StringUtilities.join(columns, "\", \"", columnsToBeIndexed).append('"');

                String indexNameStr = indexName.toString();
                if (getDatabaseState().getCreatedIndices().add(indexNameStr))
                {
                    getCacheUtilities().execute(getSQLGenerator().generateCreateIndex(indexNameStr, tableName, false,
                            New.array(columnsToBeIndexed, String.class)), conn);
                    myCreatedIndexCount++;
                }
            }
        }

        return null;
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
     * Accessor for the propertyMatchers.
     *
     * @return The propertyMatchers.
     */
    protected Collection<? extends PropertyMatcher<?>> getPropertyMatchers()
    {
        return myPropertyMatchers;
    }
}
