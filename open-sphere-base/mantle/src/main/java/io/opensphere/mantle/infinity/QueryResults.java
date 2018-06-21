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

    /** The data model category family. */
    public static final String FAMILY = "Infinity.Search";

    /** The total result count. */
    private final long myCount;

    /** The bins. */
    private List<ValueWithCount<Object>> myBins;

    /**
     * Constructor.
     *
     * @param count the count
     */
    public QueryResults(long count)
    {
        myCount = count;
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
     * Sets the bins.
     *
     * @param bins the bins
     */
    public void setBins(List<ValueWithCount<Object>> bins)
    {
        myBins = bins;
    }

    /**
     * Gets the bins.
     *
     * @return the bins
     */
    public List<ValueWithCount<Object>> getBins()
    {
        return myBins;
    }
}
