package io.opensphere.mantle.data.element.event.consolidators;

import io.opensphere.core.event.Event;
import io.opensphere.core.event.EventConsolidator;
import io.opensphere.mantle.data.element.event.DataElementMetaDataValueChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementMetadataValueChangeEvent;

/**
 * The Class DataElementAltitudeChangeConsolidator.
 */
public class DataElementMetaDataValueChangeConsolidator
        extends AbstractDataElementEventConsolidator<DataElementMetaDataValueChangeEvent>
{
    /**
     * Instantiates a new consolidator.
     */
    public DataElementMetaDataValueChangeConsolidator()
    {
        super();
    }

    @Override
    public Event createConsolidatedEvent()
    {
        return new ConsolidatedDataElementMetadataValueChangeEvent(getIdSet(), getDataTypeSet(), getSource());
    }

    @Override
    public EventConsolidator<DataElementMetaDataValueChangeEvent> newInstance()
    {
        return new DataElementMetaDataValueChangeConsolidator();
    }
}
