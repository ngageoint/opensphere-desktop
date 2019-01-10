package io.opensphere.core.util.fx.tabpane;

import javafx.beans.InvalidationListener;

/** A simple class in which an element's listener and counter are defined. */
public class ElementsMapElement
{
    /** The listener for the element. */
    private final InvalidationListener myListener;

    /** The counter for the element. */
    private int myCounter;

    /**
     * Creates a new element with the supplied invalidation listener.
     *
     * @param listener the listener for the element's invalidation.
     */
    public ElementsMapElement(InvalidationListener listener)
    {
        myListener = listener;
        myCounter = 1;
    }

    /** Increments the counter. */
    public void increment()
    {
        myCounter++;
    }

    /**
     * Decrement's the counter and returns it.
     *
     * @return the value of the counter after decrementing it.
     */
    public int decrement()
    {
        return --myCounter;
    }

    /**
     * Gets the value of the {@link #myListener} field.
     *
     * @return the value stored in the {@link #myListener} field.
     */
    public InvalidationListener getListener()
    {
        return myListener;
    }
}
