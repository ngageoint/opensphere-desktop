package io.opensphere.mantle.data.cache.query;

import java.util.List;
import java.util.stream.Collectors;

import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.mantle.data.cache.CacheIdQuery;
import io.opensphere.mantle.data.cache.QueryAccessConstraint;

/**
 * The Class SimpleListResultCacheIdQuery.
 *
 * @param <R> the generic type
 */
public abstract class SimpleListResultCacheIdQuery<R> extends CacheIdQuery
{
    /** The my id to result map. */
    private final TLongObjectHashMap<R> myIdToResultMap;

    /**
     * Instantiates a new simple cache id query.
     *
     * @param filterIds the filter ids
     */
    public SimpleListResultCacheIdQuery(List<Long> filterIds)
    {
        super(filterIds);
        myIdToResultMap = new TLongObjectHashMap<>();
    }

    /**
     * Instantiates a new simple cache id query.
     *
     * @param filterIds the filter ids
     * @param constraints the constraints
     */
    public SimpleListResultCacheIdQuery(List<Long> filterIds, QueryAccessConstraint constraints)
    {
        super(filterIds, constraints);
        myIdToResultMap = new TLongObjectHashMap<>();
    }

    @Override
    public void finalizeQuery()
    {
    }

    /**
     * Gets the filter id to result map. This assumes that the user of the
     * query's process function extracts a result per id and puts it in this
     * map.
     *
     * @return the id to result map
     */
    public TLongObjectHashMap<R> getIdToResultMap()
    {
        return myIdToResultMap;
    }

    /**
     * Gets the results as a list from the internal IdToResultMap in the order
     * of the ids in the provided filterIds list. If an entry is not found in
     * the map for a particular id, null will be inserted as the value.
     *
     * @return the results
     */
    public List<R> getResults()
    {
        return getFilterIds().stream().map(id -> myIdToResultMap.get(id.longValue())).collect(Collectors.toList());
    }
}
