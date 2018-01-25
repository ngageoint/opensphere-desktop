package io.opensphere.core.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * An association between a listener and its control bindings.
 */
public class BindingsToListener
{
    /** Message used when the maximum concurrent bindings limit is exceeded. */
    private static final String CONCURRENT_BINDINGS_MESSAGE = "Maximum number of concurrent bindings per listener is: ";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(BindingsToListener.class);

    /**
     * The allowed number of bindings. This is simply to give the configuration
     * dialog some layout help.
     */
    private static final int TOTAL_ALLOWABLE_BINDINGS = 2;

    /** The bindings. */
    private final List<Binding> myBindings;

    /** The control context. */
    private final ControlContextImpl myContext;

    /** The listener. */
    private final BoundEventListener myListener;

    /**
     * The original bindings, before runtime modification. This is
     * <code>null</code> until modifications are made.
     */
    private List<Binding> myOriginalBindings;

    /**
     * This list maintains the bindings that have been added by the user, (at
     * this point, from the KeyBinding frame). If they select 'ok' from that
     * frame, these will be copied/moved over to the myBindings list. If the
     * user cancels the frame, this list is simply nulled.
     */
    private List<Binding> myStagedBindings;

    /**
     * Construct the association.
     *
     * @param context The control context.
     * @param listener The listener.
     * @param bindings The bindings.
     */
    BindingsToListener(ControlContextImpl context, BoundEventListener listener, Binding... bindings)
    {
        if (bindings.length > TOTAL_ALLOWABLE_BINDINGS && listener.isReassignable() && LOGGER.isDebugEnabled())
        {
            LOGGER.debug(
                    CONCURRENT_BINDINGS_MESSAGE + TOTAL_ALLOWABLE_BINDINGS + ".  Attempting to create with: " + bindings.length);
        }

        myContext = context;
        myBindings = new ArrayList<>(bindings.length);
        for (int index = 0; index < bindings.length || index < TOTAL_ALLOWABLE_BINDINGS; index++)
        {
            if (index < bindings.length)
            {
                myBindings.add(bindings[index]);
            }
            else
            {
                myBindings.add(null);
            }
        }
        myListener = listener;
    }

    /**
     * Commit the pending changes.
     */
    public void commitBindingChanges()
    {
        if (myStagedBindings != null)
        {
            // if this is the first reassignment of this item, then store
            // the original default
            if (myOriginalBindings == null)
            {
                myOriginalBindings = Collections.unmodifiableList(new ArrayList<Binding>(myBindings));
            }

            for (int index = 0; index < myStagedBindings.size(); index++)
            {
                Binding newBinding = myStagedBindings.get(index);
                Binding oldBinding = myBindings.get(index);

                if (newBinding == null ? oldBinding != null : !newBinding.equals(oldBinding))
                {
                    if (oldBinding != null)
                    {
                        /* the old binding may be in one of these sets, so
                         * dispose of it... */
                        removeBindingFromAllSets(oldBinding);
                    }
                    if (newBinding != null)
                    {
                        addBindingToProperSet(newBinding);
                    }
                    myBindings.set(index, newBinding);
                }
            }
            myStagedBindings = null;
        }
    }

    /**
     * Get the committed binding at a particular position.
     *
     * @param index The position.
     * @return The binding, or <code>null</code> if there is none.
     */
    public Binding getBinding(int index)
    {
        return myBindings.get(index);
    }

    /**
     * Get my listener.
     *
     * @return The listener.
     */
    public BoundEventListener getListener()
    {
        return myListener;
    }

    /**
     * Set a binding for this listener at a particular index.
     *
     * @param binding The binding to set.
     * @param index The position for the binding.
     */
    void associatePendingBinding(Binding binding, int index)
    {
        if (index >= TOTAL_ALLOWABLE_BINDINGS)
        {
            throw new IllegalArgumentException(CONCURRENT_BINDINGS_MESSAGE + TOTAL_ALLOWABLE_BINDINGS
                    + ".  Attempting to create more, using index: " + index + ".  (Binding indices are zero based)");
        }
        if (myStagedBindings == null)
        {
            myStagedBindings = new ArrayList<>(myBindings);
        }
        myStagedBindings.set(index, binding);
    }

    /**
     * Cancel all staged changes.
     */
    void cancelBindingChanges()
    {
        myStagedBindings = null;
    }

    /**
     * Clear the binding at a position.
     *
     * @param index The position of the binding.
     */
    void clearBinding(int index)
    {
        if (index >= TOTAL_ALLOWABLE_BINDINGS)
        {
            throw new IllegalArgumentException(CONCURRENT_BINDINGS_MESSAGE + TOTAL_ALLOWABLE_BINDINGS
                    + ".  Attempting to clear an invalid index: " + index + ".  (Binding indices are zero based)");
        }
        if (myStagedBindings == null)
        {
            myStagedBindings = new ArrayList<>(myBindings);
        }
        myStagedBindings.set(index, null);
    }

    /**
     * Get a copy of the committed bindings.
     *
     * @return The bindings.
     */
    Collection<Binding> getBindings()
    {
        return new ArrayList<Binding>(myBindings);
    }

    /**
     * Get the staged binding at a particular position.
     *
     * @param index The position.
     * @return The binding, or <code>null</code>.
     */
    Binding getStagedBinding(int index)
    {
        return myStagedBindings == null ? myBindings.get(index) : myStagedBindings.get(index);
    }

    /**
     * Get a copy of the staged bindings.
     *
     * @return The staged bindings.
     */
    Collection<Binding> getStagedBindings()
    {
        return myStagedBindings == null ? null : new ArrayList<Binding>(myStagedBindings);
    }

    /**
     * Clear a pending binding at a particular position.
     *
     * @param index The position.
     */
    void setClearBindingPending(int index)
    {
        associatePendingBinding(null, index);
    }

    /**
     * Reset the pending binding at a particular position to the original value.
     *
     * @param assignmentIndex The position.
     */
    void setResetBindingPending(int assignmentIndex)
    {
        if (assignmentIndex >= TOTAL_ALLOWABLE_BINDINGS)
        {
            throw new IllegalArgumentException(CONCURRENT_BINDINGS_MESSAGE + TOTAL_ALLOWABLE_BINDINGS
                    + ".  Attempting to create more, using index: " + assignmentIndex + ".  (Binding indices are zero based)");
        }
        List<Binding> source = myOriginalBindings == null ? myBindings : myOriginalBindings;
        associatePendingBinding(source.get(assignmentIndex), assignmentIndex);
    }

    /**
     * Add a committed binding to the control context.
     *
     * @param value The binding.
     */
    private void addBindingToProperSet(Binding value)
    {
        if (value == null)
        {
            return;
        }

        if (value instanceof PressedReleasedKeyBindAbs)
        {
            myContext.getPressedReleasedKeyBindings().add((PressedReleasedKeyBindAbs)value);
        }
        else if (value instanceof KeyTypedBind)
        {
            myContext.getTypedKeyBindings().add((KeyTypedBind)value);
        }
        else if (value instanceof MouseBindingAbs)
        {
            myContext.addMouseBinding((MouseBindingAbs)value);
        }
    }

    /**
     * Remove a binding from the control context.
     *
     * @param oldBinding The old binding.
     */
    private void removeBindingFromAllSets(Binding oldBinding)
    {
        myContext.getPressedReleasedKeyBindings().remove(oldBinding);
        myContext.getTypedKeyBindings().remove(oldBinding);
        myContext.removeMouseBinding(oldBinding);
    }
}
