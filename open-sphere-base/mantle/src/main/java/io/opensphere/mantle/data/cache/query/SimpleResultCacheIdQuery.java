package io.opensphere.mantle.data.cache.query;

import java.util.List;

import io.opensphere.mantle.data.cache.CacheIdQuery;
import io.opensphere.mantle.data.cache.QueryAccessConstraint;

/**
 * The Class SimpleResultCacheIdQuery.
 *
 * @param <R> the result type
 */
public abstract class SimpleResultCacheIdQuery<R> extends CacheIdQuery
{
    /** The result. */
    private R myResult;

    /**
     * Instantiates a new simple cache id query.
     *
     * @param filterIds the filter ids
     */
    public SimpleResultCacheIdQuery(List<Long> filterIds)
    {
        super(filterIds);
    }

    /**
     * Instantiates a new simple cache id query.
     *
     * @param filterIds the filter ids
     * @param constraints the constraints
     */
    public SimpleResultCacheIdQuery(List<Long> filterIds, QueryAccessConstraint constraints)
    {
        super(filterIds, constraints);
    }

    /**
     * Instantiates a new simple cache id query.
     *
     * @param filterIds the filter ids
     * @param constraints the constraints
     * @param initialResult the initial result
     */
    public SimpleResultCacheIdQuery(List<Long> filterIds, QueryAccessConstraint constraints, R initialResult)
    {
        super(filterIds, constraints);
        myResult = initialResult;
    }

    /**
     * Instantiates a new simple cache id query.
     *
     * @param filterIds the filter ids
     * @param initialResult the initial result
     */
    public SimpleResultCacheIdQuery(List<Long> filterIds, R initialResult)
    {
        super(filterIds);
        myResult = initialResult;
    }

    @Override
    public void finalizeQuery()
    {
    }

    /**
     * Gets the result.
     *
     * @return the result
     */
    public R getResult()
    {
        return myResult;
    }

    /**
     * Sets the result.
     *
     * @param result the new result
     */
    protected void setResult(R result)
    {
        myResult = result;
    }
}
