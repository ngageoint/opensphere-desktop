package io.opensphere.controlpanels.animation.controller;

import io.opensphere.controlpanels.animation.model.Action;
import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.core.TimeManager;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.Service;

/**
 * Adapts the load times set in the animation model and applies them to the time
 * manager.
 */
public class LoadTimeAdapter implements Service, ListDataListener<TimeSpan>, ChangeListener<TimeSpan>
{
    /**
     * The animation model containing the true load times.
     */
    private final AnimationModel myAnimationModel;

    /**
     * The time manager to keep in sync with the animation model.
     */
    private final TimeManager myTimeManager;

    /**
     * Constructs a new adapter.
     *
     * @param timeManager The time manager to keep in sync with the animation
     *            model.
     * @param animationModel The animation model containing the true load times.
     */
    public LoadTimeAdapter(TimeManager timeManager, AnimationModel animationModel)
    {
        myTimeManager = timeManager;
        myAnimationModel = animationModel;
    }

    @Override
    public void changed(ObservableValue<? extends TimeSpan> observable, TimeSpan oldValue, TimeSpan newValue)
    {
        if (myAnimationModel.loadIntervalsProperty().isEmpty())
        {
            changed(oldValue, newValue);
        }
    }

    /**
     * Handles when the load span changes and applies the changes to the
     * {@link TimeManager}.
     *
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void changed(TimeSpan oldValue, TimeSpan newValue)
    {
        int index = myTimeManager.getLoadTimeSpans().indexOf(oldValue);
        if (index >= 0)
        {
            myTimeManager.getLoadTimeSpans().set(index, newValue);
        }
        else
        {
            myTimeManager.getLoadTimeSpans().add(newValue);
        }
    }

    @Override
    public void close()
    {
        myAnimationModel.loadIntervalsProperty().removeChangeListener(this);
        myAnimationModel.getLoopSpan().removeListener(this);
    }

    @Override
    public void elementsAdded(ListDataEvent<TimeSpan> e)
    {
        int indexOf = myTimeManager.getLoadTimeSpans().indexOf(myAnimationModel.getLoopSpan().get());
        TimeSpan oldValue = null;

        if (indexOf >= 0)
        {
            oldValue = myTimeManager.getLoadTimeSpans().get(indexOf);
        }

        if (myAnimationModel.getLoadIntervals().size() > 1 || e.getChangedElements().size() > 1)
        {
            if (indexOf >= 0 && myAnimationModel.lastActionProperty().get() == Action.LOAD)
            {
                myTimeManager.getLoadTimeSpans().remove(indexOf);
            }
            myTimeManager.getLoadTimeSpans().addAll(e.getChangedElements());
        }
        else
        {
            changed(oldValue, e.getChangedElements().get(0));
        }
    }

    @Override
    public void elementsChanged(ListDataEvent<TimeSpan> e)
    {
        int index = 0;
        for (TimeSpan span : e.getChangedElements())
        {
            TimeSpan previous = e.getPreviousElements().get(index);
            changed(previous, span);
            index++;
        }
    }

    @Override
    public void elementsRemoved(ListDataEvent<TimeSpan> e)
    {
        if (myAnimationModel.loadIntervalsProperty().isEmpty() && e.getChangedElements().size() == 1)
        {
            changed(e.getChangedElements().get(0), myAnimationModel.getLoadIntervals().get(0));
        }
        else
        {
            for (TimeSpan span : e.getChangedElements())
            {
                myTimeManager.getLoadTimeSpans().remove(span);
            }
        }
    }

    @Override
    public void open()
    {
        myTimeManager.getLoadTimeSpans().addAll(myAnimationModel.getLoadIntervals());
        myAnimationModel.loadIntervalsProperty().addChangeListener(this);
        myAnimationModel.getLoopSpan().addListener(this);
    }
}
