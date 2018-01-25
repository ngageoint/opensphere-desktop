package io.opensphere.mantle.data.element.event.consolidators;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.opensphere.core.event.EventConsolidator;
import io.opensphere.mantle.data.element.event.AbstractDataElementChangeEvent;

/**
 * An abstract consolidator.
 *
 * @param <E> the element type
 */
public abstract class AbstractDataElementEventConsolidator<E extends AbstractDataElementChangeEvent>
        implements EventConsolidator<E>
{
    /** The data type set. */
    private final Set<String> myDataTypeSet;

    /** The id set. */
    private final List<Long> myIdSet;

    /** The source. */
    private Object mySource;

    /**
     * Instantiates a new abstract data element event consolidator.
     */
    public AbstractDataElementEventConsolidator()
    {
        myIdSet = new LinkedList<>();
        myDataTypeSet = new HashSet<>();
    }

    @Override
    public void addEvent(E event)
    {
        if (event != null)
        {
            myIdSet.add(event.getRegistryId());
            if (event.getDataTypeKey() != null)
            {
                myDataTypeSet.add(event.getDataTypeKey());
            }
            mySource = event.getSource();
        }
    }

    @Override
    public void addEvents(Collection<E> events)
    {
        if (events != null)
        {
            for (E evt : events)
            {
                addEvent(evt);
            }
        }
    }

    @Override
    public boolean hadEvents()
    {
        return !myIdSet.isEmpty();
    }

    @Override
    public void reset()
    {
        myIdSet.clear();
        myDataTypeSet.clear();
        mySource = null;
    }

    /**
     * Gets the data type set.
     *
     * @return the data type set
     */
    protected Set<String> getDataTypeSet()
    {
        return myDataTypeSet;
    }

    /**
     * Gets the id set.
     *
     * @return the id set
     */
    protected List<Long> getIdSet()
    {
        return myIdSet;
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    protected Object getSource()
    {
        return mySource;
    }
}
