package io.opensphere.mantle.data.element.event.consolidators;

import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.event.Event;
import io.opensphere.core.event.EventConsolidator;
import io.opensphere.mantle.data.element.event.DataElementMapGeometrySupportChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementMapGeometrySupportChangeEvent;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * The Class DataElementMapGeometrySupportChangeConsolidator.
 */
public class DataElementMapGeometrySupportChangeConsolidator
        extends AbstractDataElementEventConsolidator<DataElementMapGeometrySupportChangeEvent>
{
    /** The colors. */
    private final TLongObjectHashMap<MapGeometrySupport> myIdtoMGSMap;

    /**
     * Instantiates a new data element map geometry support change consolidator.
     */
    public DataElementMapGeometrySupportChangeConsolidator()
    {
        super();
        myIdtoMGSMap = new TLongObjectHashMap<>();
    }

    @Override
    public void addEvent(DataElementMapGeometrySupportChangeEvent event)
    {
        super.addEvent(event);
        if (event != null)
        {
            myIdtoMGSMap.put(event.getRegistryId(), event.getMapGeometrySupport());
        }
    }

    @Override
    public Event createConsolidatedEvent()
    {
        return new ConsolidatedDataElementMapGeometrySupportChangeEvent(getIdSet(), getDataTypeSet(), myIdtoMGSMap, getSource());
    }

    @Override
    public EventConsolidator<DataElementMapGeometrySupportChangeEvent> newInstance()
    {
        return new DataElementMapGeometrySupportChangeConsolidator();
    }

    @Override
    public void reset()
    {
        super.reset();
        myIdtoMGSMap.clear();
    }
}
