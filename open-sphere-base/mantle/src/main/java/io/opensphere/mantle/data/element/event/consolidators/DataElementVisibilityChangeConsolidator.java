package io.opensphere.mantle.data.element.event.consolidators;

import gnu.trove.set.hash.TLongHashSet;
import io.opensphere.core.event.Event;
import io.opensphere.core.event.EventConsolidator;
import io.opensphere.mantle.data.element.event.DataElementVisibilityChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementVisibilityChangeEvent;

/**
 * The Class DataElementVisibilityChangeConsolidator.
 */
public class DataElementVisibilityChangeConsolidator
        extends AbstractDataElementEventConsolidator<DataElementVisibilityChangeEvent>
{
    /** The de-select set. */
    private final TLongHashSet myInvisible;

    /** The select set. */
    private final TLongHashSet myVisible;

    /**
     * Instantiates a new data element visibility change consolidator.
     */
    public DataElementVisibilityChangeConsolidator()
    {
        super();
        myVisible = new TLongHashSet();
        myInvisible = new TLongHashSet();
    }

    @Override
    public void addEvent(DataElementVisibilityChangeEvent event)
    {
        super.addEvent(event);
        if (event != null)
        {
            if (event.isVisible())
            {
                myVisible.add(event.getRegistryId());
            }
            else
            {
                myInvisible.add(event.getRegistryId());
            }
        }
    }

    @Override
    public Event createConsolidatedEvent()
    {
        return new ConsolidatedDataElementVisibilityChangeEvent(getIdSet(), getDataTypeSet(), myVisible, myInvisible,
                getSource());
    }

    @Override
    public EventConsolidator<DataElementVisibilityChangeEvent> newInstance()
    {
        return new DataElementVisibilityChangeConsolidator();
    }

    @Override
    public void reset()
    {
        super.reset();
        myVisible.clear();
        myInvisible.clear();
    }
}
