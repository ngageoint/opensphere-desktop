package io.opensphere.controlpanels.animation.view;

import java.util.Map;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.input.model.TimeInstantModel;

/**
 * Some crazy stuff to pretend that a span is an instant.
 */
public class TimeInstantSpanWrapper extends TimeInstantModel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The span inner model. */
    private final ObservableValue<TimeSpan> mySpan;

    /** Whether to use the start time (true for start, false for end). */
    private final boolean myIsStart;

    /** Map of instant listeners to span listeners. */
    private final Map<ChangeListener<? super TimeInstant>, ChangeListener<? super TimeSpan>> myListenerMap = New.weakMap();

    /**
     * Constructor.
     *
     * @param span The span inner model
     * @param isStart Whether to use the start time (true for start, false for
     *            end)
     */
    public TimeInstantSpanWrapper(ObservableValue<TimeSpan> span, boolean isStart)
    {
        super();
        mySpan = span;
        myIsStart = isStart;
    }

    @Override
    public TimeInstant get()
    {
        return getInstant(mySpan.get());
    }

    @Override
    public boolean set(TimeInstant value)
    {
        boolean changed = false;
        TimeSpan newSpan = getNewSpan(value);
        if (newSpan != null)
        {
            changed = mySpan.set(newSpan);
            if (changed)
            {
                setValid(true, this);
            }
        }
        return changed;
    }

    @Override
    public void addListener(final ChangeListener<? super TimeInstant> listener)
    {
        ChangeListener<? super TimeSpan> spanListener = (obs, old, newValue) -> listener.changed(this, getInstant(old),
                getInstant(newValue));
        myListenerMap.put(listener, spanListener);
        mySpan.addListener(spanListener);
    }

    @Override
    public void removeListener(ChangeListener<? super TimeInstant> listener)
    {
        ChangeListener<? super TimeSpan> removed = myListenerMap.remove(listener);
        if (removed != null)
        {
            mySpan.removeListener(removed);
        }
    }

    /**
     * Gets the span.
     *
     * @return the span
     */
    public ObservableValue<TimeSpan> getSpan()
    {
        return mySpan;
    }

    /**
     * Gets the isStart.
     *
     * @return the isStart
     */
    public boolean isStart()
    {
        return myIsStart;
    }

    /**
     * Gets the new time span for the given value.
     *
     * @param value the value
     * @return the new time span
     */
    protected TimeSpan getNewSpan(TimeInstant value)
    {
        TimeSpan span = null;
        if (value != null && !value.equals(get()))
        {
            span = myIsStart ? TimeSpan.get(value, mySpan.get().getEndInstant())
                    : TimeSpan.get(mySpan.get().getStartInstant(), value);
        }
        return span;
    }

    /**
     * Get the applicable instant from the span.
     *
     * @param span The span.
     * @return The instant.
     */
    private TimeInstant getInstant(TimeSpan span)
    {
        return myIsStart ? span.getStartInstant() : span.getEndInstant();
    }
}
