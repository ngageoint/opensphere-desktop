package io.opensphere.wfs.envoy;

import io.opensphere.core.cache.accessor.IntervalPropertyAccessor;
import io.opensphere.core.cache.accessor.PersistentPropertyAccessor;
import io.opensphere.core.cache.matcher.GeneralIntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.datafilter.DataFilter;

/**
 * Singleton accessor for a data filter.
 */
public class DataFilterAccessor
        implements IntervalPropertyAccessor<Object, DataFilter>, PersistentPropertyAccessor<Object, DataFilter>
{
    /** The data filter. */
    private final DataFilter myDataFilter;

    /**
     * Constructor.
     *
     * @param dataFilter The data filter.
     */
    public DataFilterAccessor(DataFilter dataFilter)
    {
        myDataFilter = dataFilter;
    }

    @Override
    public DataFilter access(Object input)
    {
        return myDataFilter;
    }

    @Override
    public IntervalPropertyMatcher<DataFilter> createMatcher()
    {
        return new GeneralIntervalPropertyMatcher<DataFilter>(getPropertyDescriptor(), getExtent());
    }

    @Override
    public DataFilter getExtent()
    {
        return myDataFilter;
    }

    @Override
    public PropertyDescriptor<DataFilter> getPropertyDescriptor()
    {
        return WFSDataRegistryHelper.DATA_FILTER_PROPERTY_DESCRIPTOR;
    }
}
