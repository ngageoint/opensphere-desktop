package io.opensphere.controlpanels.timeline;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.StrongObservableValue;

/**
 * A class that contains an observable time span and separate observable start
 * and end instants. They are all kept consistent internally.
 */
public class ObservableTimeSpan
{
    /** The end instant. */
    private final ObservableValue<TimeInstant> myEnd;

    /** The time span. */
    private final ObservableValue<TimeSpan> mySpan;

    /** The start instant. */
    private final ObservableValue<TimeInstant> myStart;

    /**
     * Constructor.
     *
     * @param span the time span
     */
    public ObservableTimeSpan(ObservableValue<TimeSpan> span)
    {
        mySpan = span;
        myStart = new StrongObservableValue<>();
        myEnd = new StrongObservableValue<>();
        coupleTimeSpanToInstants();
    }

    /**
     * Gets the end.
     *
     * @return the end
     */
    public ObservableValue<TimeInstant> getEnd()
    {
        return myEnd;
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
     * Gets the start.
     *
     * @return the start
     */
    public ObservableValue<TimeInstant> getStart()
    {
        return myStart;
    }

    /**
     * Add listeners to the time span and two time instants so that they stay
     * synchronized.
     */
    private void coupleTimeSpanToInstants()
    {
        mySpan.addListener(new ChangeListener<TimeSpan>()
        {
            @Override
            public void changed(ObservableValue<? extends TimeSpan> observable, TimeSpan oldValue, TimeSpan newValue)
            {
                myStart.set(mySpan.get().getStartInstant());
                myEnd.set(mySpan.get().getEndInstant());
            }
        });
        myStart.addListener(new ChangeListener<TimeInstant>()
        {
            @Override
            public void changed(ObservableValue<? extends TimeInstant> observable, TimeInstant oldValue, TimeInstant newValue)
            {
                mySpan.set(TimeSpan.get(myStart.get(), mySpan.get().getEndInstant()));

                // In case the start time didn't actually change in mySpan.
                myStart.set(mySpan.get().getStartInstant());
            }
        });
        myEnd.addListener(new ChangeListener<TimeInstant>()
        {
            @Override
            public void changed(ObservableValue<? extends TimeInstant> observable, TimeInstant oldValue, TimeInstant newValue)
            {
                mySpan.set(TimeSpan.get(mySpan.get().getStartInstant(), myEnd.get()));

                // In case the end time didn't actually change in mySpan.
                myEnd.set(mySpan.get().getEndInstant());
            }
        });
        TimeSpan value = mySpan.get();
        if (value != null)
        {
            myStart.set(value.getStartInstant());
            myEnd.set(value.getEndInstant());
        }
    }

    @Override
    public String toString()
    {
        return "ObservableTimeSpan [" + mySpan.get() + "]";
    }
}
