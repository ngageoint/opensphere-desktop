package io.opensphere.mantle.data.element.event.consolidators;

import java.util.LinkedList;
import java.util.List;

import io.opensphere.core.event.Event;
import io.opensphere.core.event.EventConsolidator;
import io.opensphere.mantle.data.element.event.DataElementSelectionChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementSelectionChangeEvent;

/**
 * The Class DataElementSelectionChangeConsolidator.
 */
public class DataElementSelectionChangeConsolidator extends AbstractDataElementEventConsolidator<DataElementSelectionChangeEvent>
{
    /** The de-select set. */
    private final List<Long> myDeselects;

    /** The select set. */
    private final List<Long> mySelects;

    /**
     * Instantiates a new data element selection change consolidator.
     */
    public DataElementSelectionChangeConsolidator()
    {
        super();
        mySelects = new LinkedList<>();
        myDeselects = new LinkedList<>();
    }

    @Override
    public void addEvent(DataElementSelectionChangeEvent event)
    {
        super.addEvent(event);
        if (event != null)
        {
            if (event.isSelected())
            {
                mySelects.add(Long.valueOf(event.getRegistryId()));
            }
            else
            {
                myDeselects.add(Long.valueOf(event.getRegistryId()));
            }
        }
    }

    @Override
    public Event createConsolidatedEvent()
    {
        return new ConsolidatedDataElementSelectionChangeEvent(getIdSet(), getDataTypeSet(), mySelects, myDeselects, getSource());
    }

    @Override
    public EventConsolidator<DataElementSelectionChangeEvent> newInstance()
    {
        return new DataElementSelectionChangeConsolidator();
    }

    @Override
    public void reset()
    {
        super.reset();
        mySelects.clear();
        myDeselects.clear();
    }
}
