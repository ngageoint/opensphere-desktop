package io.opensphere.core.cache;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Default cache modification listener that puts the reports in a collection.
 */
public class DefaultCacheModificationListener implements CacheModificationListener
{
    /** The report collection. */
    private final Collection<CacheModificationReport> myReports = new ConcurrentLinkedQueue<>();

    @Override
    public void cacheModified(CacheModificationReport cacheModificationReport)
    {
        myReports.add(cacheModificationReport);
    }

    /**
     * Accessor for the reports.
     *
     * @return The reports.
     */
    public Collection<CacheModificationReport> getReports()
    {
        return myReports;
    }
}
