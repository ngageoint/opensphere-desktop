package io.opensphere.mantle.data.cache;

import java.util.List;

import io.opensphere.core.util.Utilities;

/**
 * The Class CacheIdQuery.
 */
public abstract class CacheIdQuery extends CacheQuery
{
    /** The my filter ids. */
    private final List<Long> myFilterIds;

    /**
     * Instantiates a new cache id query with id's to query.
     *
     * @param filterIds the filter ids ( may be null for no filter )
     */
    public CacheIdQuery(List<Long> filterIds)
    {
        this(filterIds, null);
    }

    /**
     * Instantiates a new cache id query with id's to query and an access
     * constraint.
     *
     * @param filterIds the filter ids ( may be null for no filter )
     * @param accessConstraint the access constraint, if not provided all data
     *            can be accessed in the matches
     */
    public CacheIdQuery(List<Long> filterIds, QueryAccessConstraint accessConstraint)
    {
        super(accessConstraint);
        Utilities.checkNull(filterIds, "filterIds");
        myFilterIds = filterIds;
    }

    /**
     * Gets the filter ids.
     *
     * @return the filter ids
     */
    public List<Long> getFilterIds()
    {
        return myFilterIds;
    }
}
