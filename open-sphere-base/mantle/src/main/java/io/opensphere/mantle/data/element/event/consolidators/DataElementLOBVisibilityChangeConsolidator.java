package io.opensphere.mantle.data.element.event.consolidators;

import java.util.LinkedList;
import java.util.List;

import io.opensphere.core.event.Event;
import io.opensphere.core.event.EventConsolidator;
import io.opensphere.mantle.data.element.event.DataElementLOBVsibilityChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementLOBVisibilityChangeEvent;

/**
 * The Class DataElementHighlightChangeConsolidator.
 */
public class DataElementLOBVisibilityChangeConsolidator
        extends AbstractDataElementEventConsolidator<DataElementLOBVsibilityChangeEvent>
{
    /** The de-select set. */
    private final List<Long> myLobInvisible;

    /** The select set. */
    private final List<Long> myLobVisible;

    /**
     * Instantiates a new data element lob visibility change consolidator.
     */
    public DataElementLOBVisibilityChangeConsolidator()
    {
        super();
        myLobVisible = new LinkedList<>();
        myLobInvisible = new LinkedList<>();
    }

    @Override
    public void addEvent(DataElementLOBVsibilityChangeEvent event)
    {
        super.addEvent(event);
        if (event != null)
        {
            if (event.isLOBVisible())
            {
                myLobVisible.add(event.getRegistryId());
            }
            else
            {
                myLobInvisible.add(event.getRegistryId());
            }
        }
    }

    @Override
    public Event createConsolidatedEvent()
    {
        return new ConsolidatedDataElementLOBVisibilityChangeEvent(getIdSet(), getDataTypeSet(), myLobVisible, myLobInvisible,
                getSource());
    }

    @Override
    public EventConsolidator<DataElementLOBVsibilityChangeEvent> newInstance()
    {
        return new DataElementLOBVisibilityChangeConsolidator();
    }

    @Override
    public void reset()
    {
        super.reset();
        myLobVisible.clear();
        myLobInvisible.clear();
    }
}
