package io.opensphere.mantle.infinity;

import java.util.List;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.util.ValueWithCount;

/** Infinity query results. */
public class QueryResults
{
    /** The {@link PropertyDescriptor} for query results. */
    public static final PropertyDescriptor<QueryResults> PROPERTY_DESCRIPTOR = new PropertyDescriptor<>("QueryResults",
            QueryResults.class);

    /** The total result count. */
    private final long myCount;

    /** The bins. */
    private final List<ValueWithCount<String>> myBins;

    /**
     * Constructor.
     *
     * @param count the count
     * @param bins the bins
     */
    public QueryResults(long count, List<ValueWithCount<String>> bins)
    {
        myCount = count;
        myBins = bins;
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public long getCount()
    {
        return myCount;
    }

    /**
     * Gets the bins.
     *
     * @return the bins
     */
    public List<ValueWithCount<String>> getBins()
    {
        return myBins;
    }
}
