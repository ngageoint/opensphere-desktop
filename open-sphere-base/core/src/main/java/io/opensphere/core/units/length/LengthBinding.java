package io.opensphere.core.units.length;

import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.opensphere.core.util.collections.New;

/** A binding to a Length property, that can notify listeners if changed. */
public class LengthBinding
{
    /** The set of listeners. Not a list, because we don't want duplicates. */
    private final Set<ChangeListener> myListeners = New.set();

    /** The Length the listeners are bound to. */
    private Length myLength;

    /**
     * Constructs a LengthBinding.
     * 
     * @param length {@link #myLength}
     */
    public LengthBinding(Length length)
    {
        myLength = length;
    }

    /**
     * Changes the {@link #myLength} property and notifies listeners.
     * 
     * @param length the length
     */
    public void changeLength(Length length)
    {
        myLength = length;
        notifyListeners();
    }

    /**
     * Retrieves {@link #myLength}.
     * 
     * @return the length
     */
    public Length getLength()
    {
        return myLength;
    }

    /**
     * Adds a new listener to {@link #myListeners}.
     * 
     * @param listener the listener to add
     * @return true if successful; false if already in the set
     */
    public boolean bind(ChangeListener listener)
    {
        return myListeners.add(listener);
    }

    /**
     * Removes a listener from {@link #myListeners}.
     * 
     * @param listener the listener to remove
     * @return true if successful; false if not in the set
     */
    public boolean unbind(ChangeListener listener)
    {
        return myListeners.remove(listener);
    }

    /**
     * Notifies each {@link ChangeListener} in {@link #myListeners} that a
     * change has occurred in the binding.
     */
    private void notifyListeners()
    {
        for (ChangeListener listener : myListeners)
        {
            listener.stateChanged(new ChangeEvent(this));
        }
    }
}
