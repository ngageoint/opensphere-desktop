package io.opensphere.controlpanels.animation.controller;

import java.awt.EventQueue;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.animation.model.PlayState;
import io.opensphere.controlpanels.timeline.TimelineUIModel;
import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.DataLoadDurationChangeListener;
import io.opensphere.core.TimeManager.PrimaryTimeSpanChangeListener;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanArrayList;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.time.TimelineUtilities;

/**
 * ObservableValue wrapper for the time manager.
 */
class TimeManagerWrapper implements ObservableValue<TimeSpan>, Service
{
    /** The desired location of the active span on screen. */
    @ThreadConfined("EDT")
    private int myActiveSpanScreenPosition = -1;

    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** The animation plan manager. */
    private final AnimationPlanController myAnimationPlanController;

    /** Listener for changes to the data load duration. */
    private final DataLoadDurationChangeListener myDataLoadDurationChangeListener = new DataLoadDurationChangeListener()
    {
        @Override
        public void dataLoadDurationChanged(final Duration dataLoadDuration)
        {
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    myAnimationModel.getSelectedDataLoadDuration().set(dataLoadDuration);
                }
            });
        }
    };

    /** The listeners that are added. */
    private final List<ChangeListener<? super TimeSpan>> myListeners = New.list();

    /** The time listener. */
    private final PrimaryTimeSpanChangeListener myTimeListener;

    /** The time manager. */
    private final TimeManager myTimeManager;

    /** The UI model. */
    private final TimelineUIModel myUIModel;

    /** Listener for changes to the UI span. */
    private final ChangeListener<TimeSpan> myUISpanListener = new ChangeListener<TimeSpan>()
    {
        @Override
        public void changed(ObservableValue<? extends TimeSpan> observable, TimeSpan oldValue, TimeSpan newValue)
        {
            setActiveSpanScreenPosition(get());
        }
    };

    /**
     * Constructor.
     *
     * @param timeManager the time manager
     * @param animationPlanController the animation plan controller
     * @param animationModel the animation model
     * @param uiModel the UI model
     */
    public TimeManagerWrapper(TimeManager timeManager, AnimationPlanController animationPlanController,
            AnimationModel animationModel, TimelineUIModel uiModel)
    {
        myTimeManager = timeManager;
        myAnimationPlanController = animationPlanController;
        myAnimationModel = animationModel;
        myUIModel = uiModel;

        myTimeListener = new PrimaryTimeSpanChangeListener()
        {
            @Override
            public void primaryTimeSpansChanged(TimeSpanList ignore)
            {
                EventQueueUtilities.runOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        TimeSpan activeExtent = myTimeManager.getPrimaryActiveTimeSpans().getExtent();

                        if (myUIModel.getUISpan().get() != null && myActiveSpanScreenPosition != -1)
                        {
                            if (myAnimationModel.getPlayState() == PlayState.STOP)
                            {
                                double millis = MathUtil.subtractSafe(myUIModel.timeToX(activeExtent.getStartInstant()),
                                        myActiveSpanScreenPosition) / myUIModel.getPixelsPerMilli();
                                if (millis > 0.)
                                {
                                    myUIModel.getUISpan().set(myUIModel.getUISpan().get().plus(new Milliseconds(millis)));
                                }
                            }
                            else
                            {
                                setActiveSpanScreenPosition(null);
                            }
                        }

                        conformLoopSpanToActiveSpan(activeExtent);

                        // The fade and the active duration should be in the
                        // same units, but check to be sure.
                        Duration fadeOut = myTimeManager.getFade().getFadeOut();
                        Duration activeDuration = myAnimationModel.getActiveSpanDuration().get();
                        int pct = !activeDuration.isZero() && fadeOut.isConvertibleTo(activeDuration)
                                ? MathUtil.clamp((int)(fadeOut.divide(activeDuration).doubleValue() * 100), 0, 100) : 0;
                        myAnimationModel.getFade().set(Integer.valueOf(pct));

                        // If the active span is no longer on a data boundary,
                        // turn off snap-to.
                        if (myAnimationModel.getSnapToDataBoundaries().get().booleanValue())
                        {
                            Duration selectedDataLoadDuration = myAnimationModel.getSelectedDataLoadDuration().get();
                            if (selectedDataLoadDuration != null)
                            {
                                boolean turnOffSnap = !activeExtent.getDuration().isConvertibleTo(selectedDataLoadDuration)
                                        || activeExtent.getDuration().compareTo(selectedDataLoadDuration) != 0;
                                if (!turnOffSnap)
                                {
                                    turnOffSnap = activeExtent.getStart() != TimelineUtilities
                                            .roundDown(activeExtent.getStartDate(), selectedDataLoadDuration).getTimeInMillis();
                                }
                                if (turnOffSnap)
                                {
                                    myAnimationModel.getSnapToDataBoundaries().set(Boolean.FALSE);
                                }
                            }
                        }

                        TimeSpan value = get();
                        for (ChangeListener<? super TimeSpan> listener : myListeners)
                        {
                            listener.changed(TimeManagerWrapper.this, value, value);
                        }

//                        // Make sure there's a plan.
//                        myAnimationPlanController.generateAnimationPlan(activeExtent);
                    }
                });
            }

            @Override
            public void primaryTimeSpansCleared()
            {
            }
        };
    }

    @Override
    public void accept(TimeSpan t)
    {
        set(t);
    }

    @Override
    public void addListener(ChangeListener<? super TimeSpan> listener)
    {
        myListeners.add(listener);
    }

    @Override
    public void bindBidirectional(ObservableValue<TimeSpan> other, Collection<? super ChangeListener<? super TimeSpan>> listeners)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close()
    {
        myTimeManager.removeDataLoadDurationChangeListener(myDataLoadDurationChangeListener);
        myTimeManager.removePrimaryTimeSpanChangeListener(myTimeListener);
        myUIModel.getUISpan().removeListener(myUISpanListener);
    }

    @Override
    public TimeSpan get()
    {
        return myTimeManager.getPrimaryActiveTimeSpans().get(0);
    }

    @Override
    public Throwable getErrorCause()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getErrorMessage()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the time manager.
     *
     * @return the time manager
     */
    public TimeManager getTimeManager()
    {
        return myTimeManager;
    }

    @Override
    public void open()
    {
        myUIModel.getUISpan().addListener(myUISpanListener);
        myTimeManager.addPrimaryTimeSpanChangeListener(myTimeListener);

        myTimeManager.addDataLoadDurationChangeListener(myDataLoadDurationChangeListener);

        myAnimationModel.getSelectedDataLoadDuration().set(myTimeManager.getDataLoadDuration());
    }

    @Override
    public void removeListener(ChangeListener<? super TimeSpan> listener)
    {
        myListeners.remove(listener);
    }

    @Override
    public boolean set(TimeSpan activeTime)
    {
        assert EventQueue.isDispatchThread();

        TimeSpan timeToUse;
        if (myAnimationModel.getLoopSpanLocked().get().booleanValue()
                && !myAnimationModel.getLoopSpan().get().contains(activeTime))
        {
            timeToUse = myAnimationModel.getLoopSpan().get().clamp(activeTime);
        }
        else
        {
            timeToUse = activeTime;
        }
        if (Objects.equals(myTimeManager.getPrimaryActiveTimeSpans().getExtent(), activeTime))
        {
            return false;
        }
        else
        {
            // Make sure the time does not overlap a skipped interval. Mostly
            // the snap function should take care of this, but if the loop span
            // is locked there may not be any viable interval.
            if (!myAnimationModel.getSkippedIntervals().isEmpty()
                    && new TimeSpanArrayList(myAnimationModel.getSkippedIntervals()).intersects(timeToUse))
            {
                return false;
            }

            setActiveSpanScreenPosition(timeToUse);

            myUIModel.getTemporaryMessage().set(null);

            // Call this first because we need to trigger an update to the
            // animation plan before setting the new time.
            if (!myAnimationModel.getActiveSpanDuration().get().equalsIgnoreUnits(timeToUse.getDuration()))
            {
                myAnimationModel.getActiveSpanDuration().set(timeToUse.getDuration());
            }

            conformLoopSpanToActiveSpan(timeToUse);

            myAnimationPlanController.abandonPlanIfDirty();
            myTimeManager.setPrimaryActiveTimeSpan(timeToUse);

            return true;
        }
    }

    @Override
    public boolean set(TimeSpan value, boolean forceFire)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setError(String message)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setError(String message, Throwable cause)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Expand the loop span to include the new active span.
     *
     * @param activeSpan The active time span.
     */
    private void conformLoopSpanToActiveSpan(TimeSpan activeSpan)
    {
        if (!myAnimationModel.getLoopSpan().get().contains(activeSpan))
        {
            myAnimationModel.getLoopSpan().set(activeSpan.simpleUnion(myAnimationModel.getLoopSpan().get()));
        }
    }

    /**
     * Remember the screen position of the given active span.
     *
     * @param activeSpan The active span.
     */
    private void setActiveSpanScreenPosition(TimeSpan activeSpan)
    {
        assert EventQueue.isDispatchThread();
        if (activeSpan == null || !myUIModel.getUISpan().get().overlaps(activeSpan))
        {
            myActiveSpanScreenPosition = -1;
        }
        else if (myUIModel.getComponent() != null)
        {
            myUIModel.calculateRatios();
            myActiveSpanScreenPosition = myUIModel.timeToX(activeSpan.getStartInstant());
        }
    }
}
