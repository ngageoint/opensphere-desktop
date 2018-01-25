package io.opensphere.mantle.data.element.event.consolidators;

import gnu.trove.map.hash.TLongFloatHashMap;
import io.opensphere.core.event.Event;
import io.opensphere.core.event.EventConsolidator;
import io.opensphere.mantle.data.element.event.DataElementAltitudeChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementAltitudeChangeEvent;

/**
 * The Class DataElementAltitudeChangeConsolidator.
 */
public class DataElementAltitudeChangeConsolidator extends AbstractDataElementEventConsolidator<DataElementAltitudeChangeEvent>
{
    /** The colors. */
    private final TLongFloatHashMap myIdToAltitudeMap;

    /**
     * Instantiates a new data element altitude change consolidator.
     */
    public DataElementAltitudeChangeConsolidator()
    {
        super();
        myIdToAltitudeMap = new TLongFloatHashMap();
    }

    @Override
    public void addEvent(DataElementAltitudeChangeEvent event)
    {
        super.addEvent(event);
        if (event != null)
        {
            myIdToAltitudeMap.put(event.getRegistryId(), event.getAltitude());
        }
    }

    @Override
    public Event createConsolidatedEvent()
    {
        return new ConsolidatedDataElementAltitudeChangeEvent(getIdSet(), getDataTypeSet(), myIdToAltitudeMap, getSource());
    }

    @Override
    public EventConsolidator<DataElementAltitudeChangeEvent> newInstance()
    {
        return new DataElementAltitudeChangeConsolidator();
    }

    @Override
    public void reset()
    {
        super.reset();
        myIdToAltitudeMap.clear();
    }
}
