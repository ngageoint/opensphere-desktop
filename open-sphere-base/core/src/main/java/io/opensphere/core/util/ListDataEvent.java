package io.opensphere.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

/**
 * Defines an event that encapsulates changes to a list.
 *
 * @param <E> the type of elements in the list
 */
public class ListDataEvent<E> extends EventObject
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The elements that changed. */
    private final List<? extends E> myChangedElements;

    /** The previous elements before the change. */
    private final List<? extends E> myPreviousElements;

    /**
     * Constructor.
     *
     * @param source the event source
     * @param changedElements the changed elements
     */
    public ListDataEvent(Object source, Collection<? extends E> changedElements)
    {
        super(source);
        myChangedElements = new ArrayList<>(changedElements);
        myPreviousElements = null;
    }

    /**
     * Constructor.
     *
     * @param source the event source
     * @param previousElements The element that were there before the change.
     * @param changedElements the changed elements
     */
    public ListDataEvent(Object source, Collection<? extends E> previousElements, Collection<? extends E> changedElements)
    {
        super(source);
        myChangedElements = new ArrayList<>(changedElements);
        myPreviousElements = new ArrayList<>(previousElements);
    }

    /**
     * Constructor.
     *
     * @param source the event source
     * @param changedElement the changed element
     */
    public ListDataEvent(Object source, E changedElement)
    {
        super(source);
        myChangedElements = Collections.singletonList(changedElement);
        myPreviousElements = null;
    }

    /**
     * Constructor.
     *
     * @param source the event source
     * @param previousElement The element that was there before the change.
     * @param changedElement the changed element
     */
    public ListDataEvent(Object source, E previousElement, E changedElement)
    {
        super(source);
        myChangedElements = Collections.singletonList(changedElement);
        myPreviousElements = Collections.singletonList(previousElement);
    }

    /**
     * Gets the changed elements.
     *
     * @return the changed elements
     */
    public List<? extends E> getChangedElements()
    {
        return myChangedElements;
    }

    /**
     * Gets the previous elements before the change.
     *
     * @return The previous elements before the change, or null if previous
     *         elements do not apply to the event.
     */
    public List<? extends E> getPreviousElements()
    {
        return myPreviousElements;
    }
}
