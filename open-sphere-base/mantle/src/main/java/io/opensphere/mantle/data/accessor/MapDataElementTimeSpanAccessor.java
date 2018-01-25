package io.opensphere.mantle.data.accessor;

import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.element.MapDataElement;

/**
 * The Class DataElementTimeSpanAccessor.
 */
public class MapDataElementTimeSpanAccessor extends TimeSpanAccessor<MapDataElement>
{
    /**
     * Instantiates a new data element time span accessor.
     *
     * @param extent the extent
     */
    public MapDataElementTimeSpanAccessor(TimeSpan extent)
    {
        super(extent);
    }

    @Override
    public TimeSpan access(MapDataElement input)
    {
        return input.getTimeSpan() == null ? TimeSpan.TIMELESS : input.getTimeSpan();
    }
}
