package io.opensphere.mantle.data.cache;

import java.util.Arrays;

import io.opensphere.core.model.time.TimeSpan;

/**
 * The Class CacheQuery.
 */
public abstract class CacheQuery
{
    /** The my complete. */
    private boolean myComplete;

    /** The constraint. */
    private final QueryAccessConstraint myConstraint;

    /** The retrieve map geometry support. */
    private boolean myRetrieveMapGeometrySupport;

    /** The retrieve meta data provider. */
    private boolean myRetrieveMetaData;

    /** The retrieve origin id. */
    private boolean myRetrieveOriginId;

    /** The times of interest. */
    private TimeSpan[] myTimesOfInterest;

    /**
     * Instantiates a new cache query.
     */
    public CacheQuery()
    {
        this(null);
    }

    /**
     * Instantiates a new cache query with an access constraint.
     *
     * @param accessConstraint the access constraint, if not provided all data
     *            can be accessed in the matches
     */
    public CacheQuery(QueryAccessConstraint accessConstraint)
    {
        myConstraint = accessConstraint;
    }

    /**
     * Returns true if this query will accept the entry for inclusion into its
     * result set.
     *
     * @param entry the entry
     * @return true, if successful
     * @throws CacheQueryException the cache query exception
     */
    public boolean accepts(CacheEntryView entry) throws CacheQueryException
    {
        return true;
    }

    /**
     * A synchronized call to accepts, used internally by the query engine.
     *
     * @param entry the entry
     * @return true, if successful
     * @throws CacheQueryException the cache query exception
     */
    public final synchronized boolean acceptsInternal(CacheEntryView entry) throws CacheQueryException
    {
        return accepts(entry);
    }

    /**
     * Finalizes the query result at the end of the query process.
     */
    public abstract void finalizeQuery();

    /**
     * Same as the finalizeQuery function used internally by the query engine to
     * ensure synchronized access.
     */
    public final synchronized void finalizeQueryInternal()
    {
        finalizeQuery();
    }

    /**
     * Gets the constraint.
     *
     * @return the constraint
     */
    public QueryAccessConstraint getConstraint()
    {
        return myConstraint;
    }

    /**
     * Intersects times of interest.
     *
     * @param entry the entry
     * @return true, if successful
     */
    public final boolean intersectsTimesOfInterest(CacheEntryView entry)
    {
        boolean passes = true;
        if (myTimesOfInterest != null)
        {
            passes = Arrays.stream(myTimesOfInterest)
                    .filter(t -> t != null && entry.getTime() != null && t.overlaps(entry.getTime())).findAny().isPresent();
        }
        return passes;
    }

    /**
     * Checks if the query has determined that it has seen enough data to be
     * finalized and ended.
     *
     * @return true, if is complete
     */
    public final boolean isComplete()
    {
        return myComplete;
    }

    /**
     * Checks if is retrieve map geometry support.
     *
     * @return true, if is retrieve map geometry support
     */
    public boolean isRetrieveMapGeometrySupport()
    {
        return myRetrieveMapGeometrySupport;
    }

    /**
     * Checks if is retrieve meta data provider.
     *
     * @return true, if is retrieve meta data provider
     */
    public boolean isRetrieveMetaDataProvider()
    {
        return myRetrieveMetaData;
    }

    /**
     * Checks if is retrieve origin id.
     *
     * @return true, if is retrieve origin id
     */
    public boolean isRetrieveOriginId()
    {
        return myRetrieveOriginId;
    }

    /**
     * Determines if the entry requires retrieval from the store or if it is
     * fully available in memory.
     *
     * Should not under any circumstance modify any of the data within the
     * entry.
     *
     * @param entry the entry to test
     * @return the match type
     */
    public final boolean needsRetrieve(CacheEntryView entry)
    {
        boolean needsRetrieve = false;
        boolean needsOriginIdRetrieved = false;
        boolean needsMetaDataRetrieved = false;
        boolean needsMGSRetrieved = false;
        if (myConstraint != null && myConstraint.requiresCachedContent())
        {
            if (entry.getLoadedElementData() == null)
            {
                if (entry.isCached())
                {
                    needsOriginIdRetrieved = myConstraint.isOriginIdRequired() && entry.isOriginIdCached();
                    needsMetaDataRetrieved = myConstraint.isMetaDataProviderRequired() && entry.isMetaDataInfoCached();
                    needsMGSRetrieved = myConstraint.isMapGeometrySupportRequired() && entry.isMapGeometrySupportCached();
                    needsRetrieve = true;
                }
            }
            else
            {
                needsOriginIdRetrieved = myConstraint.isOriginIdRequired() && entry.getLoadedElementData().getOriginId() == null
                        && entry.isOriginIdCached();

                needsMetaDataRetrieved = myConstraint.isMetaDataProviderRequired()
                        && entry.getLoadedElementData().getMetaData() == null && entry.isMetaDataInfoCached();

                needsMGSRetrieved = myConstraint.isMapGeometrySupportRequired()
                        && entry.getLoadedElementData().getMapGeometrySupport() == null && entry.isMapGeometrySupportCached();
            }
        }
        else
        {
            if (entry.getLoadedElementData() == null)
            {
                if (entry.isCached())
                {
                    needsOriginIdRetrieved = entry.isOriginIdCached();
                    needsMetaDataRetrieved = entry.isMetaDataInfoCached();
                    needsMGSRetrieved = entry.isMapGeometrySupportCached();
                    needsRetrieve = true;
                }
            }
            else
            {
                needsMGSRetrieved = entry.getVisState().isMapDataElement()
                        && entry.getLoadedElementData().getMapGeometrySupport() == null && entry.isMapGeometrySupportCached();
                needsOriginIdRetrieved = entry.getLoadedElementData().getOriginId() == null && entry.isOriginIdCached();
                needsMetaDataRetrieved = entry.getLoadedElementData().getMetaData() == null && entry.isOriginIdCached();
            }
        }

        // If the needs retrieve flag is not yet set but we need some sub-part
        // set the flag.
        if (!needsRetrieve && (needsOriginIdRetrieved || needsMGSRetrieved || needsMetaDataRetrieved))
        {
            needsRetrieve = true;
        }

        // Set the overall query retrieve flags if they have not yet been set.
        if (!myRetrieveOriginId && needsOriginIdRetrieved)
        {
            myRetrieveOriginId = true;
        }

        if (!myRetrieveMapGeometrySupport && needsMGSRetrieved)
        {
            myRetrieveMapGeometrySupport = true;
        }

        if (!myRetrieveMetaData && needsMetaDataRetrieved)
        {
            myRetrieveMetaData = true;
        }

        return needsRetrieve;
    }

    /**
     * Called if a cache entry is not found for an id selected by the query.
     *
     * @param id the id
     */
    public void notFound(Long id)
    {
    }

    /**
     * Should extract any desired results from the entry.
     *
     * Should not under any circumstance modify any of the data within the
     * entry. Cannot guarantee that entries will be presented in any particular
     * id order.
     *
     * @param id the cache id of the entry
     * @param entry the entry to extract results from.
     * @throws CacheQueryException the cache query exception
     */
    public abstract void process(Long id, CacheEntryView entry) throws CacheQueryException;

    /**
     * Same as process but used by the query engine to ensure synchronized
     * access to this function.
     *
     * @param id the id
     * @param entry the entry
     * @throws CacheQueryException the cache query exception
     */
    public final synchronized void processInternal(Long id, CacheEntryView entry) throws CacheQueryException
    {
        process(id, entry);
    }

    /**
     * Sets the query as complete complete. This allows for queries being
     * processed to tell the query processor when they have acquired sufficient
     * results to be complete, however they want to determine that. The complete
     * flag will never be set by the query processor.
     */
    public final void setComplete()
    {
        myComplete = true;
    }

    /**
     * Sets the times of interest for the query, any element not intersecting
     * with the provided timespans will not be provided to the process method.
     *
     * @param ts the new times of interest
     */
    public final void setTimesOfInterest(TimeSpan... ts)
    {
        myTimesOfInterest = ts;
    }
}
