package io.opensphere.core.cache.jdbc;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.ConcurrentLazyMap;
import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.New;

/**
 * This keeps track of the current database state.
 */
public class DatabaseState
{
    /** The set of indices that have been created. */
    private final Set<String> myCreatedIndices = CollectionUtilities.toSetView(new ConcurrentHashMap<String, Object>(),
            new Object());

    /** The set of tables that have been created. */
    private final Map<Integer, Set<String>> myCreatedTables = ConcurrentLazyMap
            .create(new ConcurrentHashMap<Integer, Set<String>>(), Integer.class, new LazyMap.Factory<Integer, Set<String>>()
            {
                @Override
                public Set<String> create(Integer key)
                {
                    return Collections.synchronizedSet(New.<String>set());
                }
            });

    /** Counter used to create unique temporary table names. */
    private final AtomicInteger myTemporaryTableCounter = new AtomicInteger();

    /**
     * Clear the cache of created tables and indices.
     */
    public void clearCreated()
    {
        myCreatedTables.clear();
        myCreatedIndices.clear();
    }

    /**
     * Access the in-memory cache of the indices that have been created in the
     * database. The returned set is read/write and thread-safe.
     *
     * @return The createdIndices.
     */
    public Set<String> getCreatedIndices()
    {
        return myCreatedIndices;
    }

    /**
     * Access the in-memory cache of the columns that have been created in the
     * data table associated with a model group. The returned set is thread-safe
     * for put/get but must be externally synchronized for iteration.
     *
     * @param groupId The group id.
     * @return The set of column names.
     */
    public Set<String> getExistingColumnsForGroup(int groupId)
    {
        return myCreatedTables.get(Integer.valueOf(groupId));
    }

    /**
     * Get the next unique name for a temporary table. Temporary tables only
     * exist with the scope of the database connection, so this doesn't have to
     * be very sophisticated. This is thread-safe.
     *
     * @return A new temporary table name.
     */
    public String getNextTempTableName()
    {
        return "JOIN_TABLE_" + myTemporaryTableCounter.getAndIncrement();
    }

    /**
     * Remove database state for a group.
     *
     * @param groupId The group id.
     */
    public void removeGroup(int groupId)
    {
        myCreatedTables.remove(Integer.valueOf(groupId));

        String indexPrefix = "INDEX_" + TableNames.getDataTableName(groupId) + "_";

        for (Iterator<String> iter = myCreatedIndices.iterator(); iter.hasNext();)
        {
            if (iter.next().startsWith(indexPrefix))
            {
                iter.remove();
            }
        }
    }
}
