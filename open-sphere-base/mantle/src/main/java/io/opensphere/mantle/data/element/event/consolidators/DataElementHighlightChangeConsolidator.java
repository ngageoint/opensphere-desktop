package io.opensphere.mantle.data.element.event.consolidators;

import java.util.LinkedList;
import java.util.List;

import io.opensphere.core.event.Event;
import io.opensphere.core.event.EventConsolidator;
import io.opensphere.mantle.data.element.event.DataElementHighlightChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementHighlightChangeEvent;

/**
 * The Class DataElementHighlightChangeConsolidator.
 */
public class DataElementHighlightChangeConsolidator extends AbstractDataElementEventConsolidator<DataElementHighlightChangeEvent>
{
    /** The highlighted set. */
    private final List<Long> myHighlighted;

    /** The un-highlighted set. */
    private final List<Long> myUnHighlighted;

    /**
     * Instantiates a new data element highlight change consolidator.
     */
    public DataElementHighlightChangeConsolidator()
    {
        super();
        myHighlighted = new LinkedList<>();
        myUnHighlighted = new LinkedList<>();
    }

    @Override
    public void addEvent(DataElementHighlightChangeEvent event)
    {
        super.addEvent(event);
        if (event != null)
        {
            if (event.isHighlighted())
            {
                myHighlighted.add(Long.valueOf(event.getRegistryId()));
                myUnHighlighted.clear();
            }
            else
            {
                myHighlighted.clear();
                myUnHighlighted.add(Long.valueOf(event.getRegistryId()));
            }
        }
    }

    @Override
    public Event createConsolidatedEvent()
    {
        return new ConsolidatedDataElementHighlightChangeEvent(getIdSet(), getDataTypeSet(), myHighlighted, myUnHighlighted,
                getSource());
    }

    @Override
    public EventConsolidator<DataElementHighlightChangeEvent> newInstance()
    {
        return new DataElementHighlightChangeConsolidator();
    }

    @Override
    public void reset()
    {
        super.reset();
        myHighlighted.clear();
        myUnHighlighted.clear();
    }
}
