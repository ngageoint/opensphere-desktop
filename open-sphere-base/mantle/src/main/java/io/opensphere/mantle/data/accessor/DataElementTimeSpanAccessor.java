package io.opensphere.mantle.data.accessor;

import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.element.DataElement;

/**
 * The Class DataElementTimeSpanAccessor.
 */
public class DataElementTimeSpanAccessor extends TimeSpanAccessor<DataElement>
{
    /**
     * Instantiates a new data element time span accessor.
     *
     * @param extent the extent
     */
    public DataElementTimeSpanAccessor(TimeSpan extent)
    {
        super(extent);
    }

    @Override
    public TimeSpan access(DataElement input)
    {
        return input.getTimeSpan();
    }
}
