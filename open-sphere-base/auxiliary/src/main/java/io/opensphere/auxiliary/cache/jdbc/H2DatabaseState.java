package io.opensphere.auxiliary.cache.jdbc;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.opensphere.core.cache.jdbc.DatabaseState;
import io.opensphere.core.util.collections.CollectionUtilities;

/**
 * H2 extensions for {@link DatabaseState}.
 */
public class H2DatabaseState extends DatabaseState
{
    /** The set of triggers that have been created. */
    private final Set<String> myCreatedTriggers = CollectionUtilities.toSetView(new ConcurrentHashMap<String, Object>(),
            new Object());

    @Override
    public void clearCreated()
    {
        super.clearCreated();
        myCreatedTriggers.clear();
    }

    /**
     * Access the in-memory cache of the triggers that have been created in the
     * database. The returned set is read/write and thread-safe.
     *
     * @return The created triggers.
     */
    public Set<String> getCreatedTriggers()
    {
        return myCreatedTriggers;
    }
}
