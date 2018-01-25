package io.opensphere.mantle.data.cache;

import java.util.Set;

import io.opensphere.core.util.Utilities;

/**
 * The Class CacheDataTypeQuery.
 */
public abstract class CacheDataTypeQuery extends CacheQuery
{
    /** The data type key filter. */
    private final Set<String> myDataTypeKeyFilter;

    /**
     * Instantiates a new cache data type query with data types to process.
     *
     * @param dataTypeKeys the data type key filter ( may be null for no filter
     *            )
     */
    public CacheDataTypeQuery(Set<String> dataTypeKeys)
    {
        this(dataTypeKeys, null);
    }

    /**
     * Instantiates a new cache data type query with data types to process and
     * an access constraint.
     *
     * @param dataTypeKeys the data type key filter ( may be null for no filter
     *            )
     * @param accessConstraint the access constraint, if not provided all data
     *            can be accessed in the matches
     */
    public CacheDataTypeQuery(Set<String> dataTypeKeys, QueryAccessConstraint accessConstraint)
    {
        super(accessConstraint);
        Utilities.checkNull(dataTypeKeys, "dataTypeKeys");
        myDataTypeKeyFilter = dataTypeKeys;
    }

    /**
     * Gets the data type key filters.
     *
     * @return the data type key filters
     */
    public Set<String> getDataTypeKeyFilters()
    {
        return myDataTypeKeyFilter;
    }
}
