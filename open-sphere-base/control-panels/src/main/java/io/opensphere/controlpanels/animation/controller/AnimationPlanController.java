package io.opensphere.controlpanels.animation.controller;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.swing.Timer;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.animation.model.PlayState;
import io.opensphere.core.AnimationChangeListener;
import io.opensphere.core.AnimationManager;
import io.opensphere.core.EDTAnimationChangeAdapter;
import io.opensphere.core.TimeManager;
import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.animation.ContinuousAnimationPlan;
import io.opensphere.core.animation.impl.AnimationPlanFactory;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanArrayList;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ObservableValueService;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.time.TimelineUtilities;

/**
 * Keeps track of the current {@link AnimationPlan} and updates it when
 * necessary.
 */
@SuppressWarnings("PMD.GodClass")
class AnimationPlanController extends ObservableValueService
{
    /** The animation manager. */
    private final AnimationManager myAnimationManager;

    /** Listener for changes to the animation plan. */
    private final AnimationChangeListener myAnimationManagerListener = new EDTAnimationChangeAdapter()
    {
        /** True when the FPS value is currently being updated. */
        private boolean myUpdatingFPS;

        @Override
        protected void animationPlanCancelledOnEDT()
        {
            assert EventQueue.isDispatchThread();
            if (!myAnimationManager.isPlaying())
            {
                myAnimationModel.setPlayState(PlayState.STOP);
            }
            myAnimationPlanDirty = true;
            createPlanAsync();
        }

        @Override
        protected void animationPlanEstablishedOnEDT(AnimationPlan ignore)
        {
            assert EventQueue.isDispatchThread();
            AnimationPlan plan = myAnimationManager.getCurrentPlan();
            if (plan == null || Utilities.sameInstance(myLastGeneratedAnimationPlan, plan))
            {
                return;
            }

            handleAnimationPlanEstablished(plan);
        }

        @Override
        protected void animationRateChangedOnEDT(Duration ignore)
        {
            assert EventQueue.isDispatchThread();
            /* Because this value is converted between FPS and duration, the
             * values are subject to rounding error. The values are also
             * adjusted to match the allowable values in the GUI. This creates
             * the potential for the values to oscillate causing an infinite
             * loop. Checking to make sure that we are not updating with a new
             * value because of an update we already made breaks this loop. */
            if (!myUpdatingFPS)
            {
                myUpdatingFPS = true;
                try
                {
                    double millis = Duration.create(ChronoUnit.MILLIS, myAnimationManager.getChangeRate()).doubleValue();
                    myAnimationModel.getFPS().set(Float.valueOf((float)(1. / millis * Constants.MILLI_PER_UNIT)));
                }
                finally
                {
                    myUpdatingFPS = false;
                }
            }
        }

        @Override
        protected void animationStartedOnEDT(Direction ignore)
        {
            assert EventQueue.isDispatchThread();
            if (myAnimationManager.isPlaying())
            {
                /* If we ever support backward animation in control panels then
                 * we should set this based on the animation state direction. */
                myAnimationModel.setPlayState(PlayState.FORWARD);
            }
        }

        @Override
        protected void animationStoppedOnEDT()
        {
            assert EventQueue.isDispatchThread();
            if (!myAnimationManager.isPlaying())
            {
                myAnimationModel.setPlayState(PlayState.STOP);
            }
        }
    };

    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** Flag indicating if the animation plan needs to be generated. */
    @ThreadConfined("EDT")
    private boolean myAnimationPlanDirty = true;

    /**
     * When true, skip generating a new animation plan as a result of changes to
     * the animation model. This can be useful when setting multiple elements in
     * the animation model to avoid having a new plan generated at for each
     * element which is set.
     */
    @ThreadConfined("EDT")
    private boolean myAnimationPlanGenerationSuspended;

    /**
     * Keep track of the last animation plan which was generated by this
     * controller. When handling conformance to new animation plans, the ones
     * generated by this controller can be skipped since conformance should
     * already be assured.
     */
    @ThreadConfined("EDT")
    private AnimationPlan myLastGeneratedAnimationPlan;

    /**
     * Use a Timer for plan updates to allow user actions to be completed before
     * the updates are committed.
     */
    private final Timer myAnimationPlanUpdateTimer;

    /** The time manager. */
    private final TimeManager myTimeManager;

    /**
     * Constructor.
     *
     * @param animationManager The animation manager.
     * @param animationModel The animation model.
     * @param timeManager The time manager.
     */
    public AnimationPlanController(AnimationManager animationManager, AnimationModel animationModel, TimeManager timeManager)
    {
        myAnimationManager = animationManager;
        myAnimationModel = animationModel;
        myTimeManager = timeManager;

        ChangeListener<Object> dirtyListener = new ChangeListener<Object>()
        {
            @Override
            public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue)
            {
                myAnimationPlanDirty = true;
            }
        };
        bindModel(myAnimationModel.getLoopSpan(), dirtyListener);
        bindModel(myAnimationModel.getActiveSpanDuration(), dirtyListener);
        bindModel(myAnimationModel.advanceDurationProperty(), dirtyListener);
        bindModel(myAnimationModel.getSnapToDataBoundaries(), dirtyListener);
        bindModel(myAnimationModel.getSkippedIntervals(), new ListDataListener<TimeSpan>()
        {
            @Override
            public void elementsAdded(ListDataEvent<TimeSpan> e)
            {
                myAnimationPlanDirty = true;
            }

            @Override
            public void elementsChanged(ListDataEvent<TimeSpan> e)
            {
            }

            @Override
            public void elementsRemoved(ListDataEvent<TimeSpan> e)
            {
                myAnimationPlanDirty = true;
            }
        });

        addService(new Service()
        {
            @Override
            public void close()
            {
                myAnimationManager.removeAnimationChangeListener(myAnimationManagerListener);
            }

            @Override
            public void open()
            {
                myAnimationManager.addAnimationChangeListener(myAnimationManagerListener);
            }
        });

        myAnimationPlanUpdateTimer = new Timer(750, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                createPlan();
            }
        });
        myAnimationPlanUpdateTimer.setRepeats(false);
    }

    /**
     * Abandon the current animation plan if it's dirty.
     */
    public void abandonPlanIfDirty()
    {
        assert EventQueue.isDispatchThread();

        if (myAnimationPlanDirty)
        {
            myAnimationManager.abandonPlan();
        }
    }

    /**
     * If the plan is continuous and the new active time span is in the last
     * section of the plan, make the loop span end match the final step in the
     * animation.
     *
     * @param activeTimeSpan The active time span.
     * @param dir The animation direction.
     */
    public void conformLoopSpanToActiveSpan(TimeSpan activeTimeSpan, Direction dir)
    {
        assert EventQueue.isDispatchThread();
        AnimationPlan plan = getPlan(activeTimeSpan);
        if (plan instanceof ContinuousAnimationPlan)
        {
            AnimationState finalState = plan.getFinalState(plan.findState(activeTimeSpan, dir));
            if (finalState != null)
            {
                TimeSpan loopSpan1;
                TimeSpan loopSpan2;
                TimeSpan delta;
                if (dir == Direction.FORWARD)
                {
                    TimeInstant loopEnd = plan.getTimeSpanForState(finalState).getEndInstant();
                    loopSpan1 = TimeSpan.get(myAnimationModel.getLoopSpan().get().getStartInstant(), loopEnd);
                    loopSpan2 = TimeSpan.get(myAnimationModel.getLoopSpan().get().getStartInstant(),
                            loopEnd.plus(myAnimationModel.getAdvanceDuration()));
                    delta = TimeSpan.get(loopSpan1.getEndInstant(), loopSpan2.getEndInstant());
                }
                else
                {
                    TimeInstant loopStart = plan.getTimeSpanForState(finalState).getStartInstant();
                    loopSpan1 = TimeSpan.get(loopStart, myAnimationModel.getLoopSpan().get().getEndInstant());
                    loopSpan2 = TimeSpan.get(loopStart.minus(myAnimationModel.getAdvanceDuration()),
                            myAnimationModel.getLoopSpan().get().getEndInstant());
                    delta = TimeSpan.get(loopSpan2.getStartInstant(), loopSpan1.getStartInstant());
                }
                if (myAnimationModel.getLoopSpan().set(loopSpan1))
                {
                    // The loop span was not on a step boundary, and has now
                    // been changed to be shorter. If it can be extended one
                    // step without hitting any skipped intervals, do so.
                    if (myAnimationModel.getSkippedIntervals().isEmpty()
                            || !new TimeSpanArrayList(myAnimationModel.getSkippedIntervals()).intersects(delta))
                    {
                        myAnimationModel.getLoopSpan().set(loopSpan2);
                    }
                    generateAnimationPlan(activeTimeSpan, null);
                }
            }
        }
    }

    /**
     * Generate a new animation plan using the values in the animation model. A
     * new plan is only generated when it will be different from the existing
     * plan.
     *
     * @param initialSpan The initial state for the plan.
     * @param animationDirection The direction of the animation.
     */
    public void generateAnimationPlan(@Nullable TimeSpan initialSpan, @Nullable Direction animationDirection)
    {
        assert EventQueue.isDispatchThread();
        if (myAnimationPlanGenerationSuspended || !myAnimationPlanDirty)
        {
            return;
        }

        myAnimationPlanDirty = false;

        TimeSpan loopSpan = myAnimationModel.getLoopSpan().get();

        AnimationPlan plan = generateAnimationPlanInternal(myAnimationModel.getActiveSpanDuration().get(), loopSpan);

        // If a plan couldn't be generated, try getting rid of skipped
        // intervals.
        while (plan.isEmptyPlan() && !myAnimationModel.getSkippedIntervals().isEmpty())
        {
            myAnimationModel.getSkippedIntervals().remove(0);
            plan = generateAnimationPlanInternal(myAnimationModel.getActiveSpanDuration().get(), loopSpan);
        }

        if (!Objects.equals(myAnimationManager.getCurrentPlan(), plan))
        {
            myLastGeneratedAnimationPlan = plan;
            if (plan.isEmptyPlan())
            {
                myAnimationManager.abandonPlan();
            }
            else
            {
                AnimationState initialState = initialSpan == null || initialSpan.isZero() ? null
                        : plan.findState(initialSpan, Direction.FORWARD);
                myAnimationManager.setPlan(plan, initialState, animationDirection, null);
            }
        }
    }

    /**
     * Get the current plan, generating a new one if necessary.
     *
     * @param initialSpan The initial time span for the plan if one is
     *            generated.
     * @return The current plan.
     */
    public AnimationPlan getPlan(@Nullable TimeSpan initialSpan)
    {
        generateAnimationPlan(initialSpan, null);
        return myAnimationManager.getCurrentPlan();
    }

    /**
     * Creates an animation plan soon.
     */
    public void createPlanAsync()
    {
        myAnimationPlanUpdateTimer.restart();
    }

    /**
     * Creates an animation plan, synchronizing the animation model if
     * necessary.
     */
    public void createPlan()
    {
        assert EventQueue.isDispatchThread();

        /* Cause AnimationController to snap to data boundaries if necessary
         * before creating the plan. */
        if (myAnimationModel.getSnapToDataBoundaries().isEnabled())
        {
            myAnimationModel.getSnapToDataBoundaries().fireChangeEvent();
        }

        generateAnimationPlan(myTimeManager.getPrimaryActiveTimeSpans().get(0), null);
    }

    /**
     * Generates an animation plan with the given parameters plus some animation
     * model state.
     *
     * @param activeDuration the active duration
     * @param loopSpan the loop span
     * @return the animation plan
     */
    private AnimationPlan generateAnimationPlanInternal(Duration activeDuration, TimeSpan loopSpan)
    {
        assert EventQueue.isDispatchThread();
        AnimationPlan plan = null;

        AnimationPlanFactory planFactory = new AnimationPlanFactory();
        Duration advance = myAnimationModel.getAdvanceDuration();
        if (myAnimationModel.getSnapToDataBoundaries().get().booleanValue()
                && myAnimationModel.getSelectedDataLoadDuration().get() != null && advance.equalsIgnoreUnits(activeDuration)
                && myAnimationModel.getFade().get().intValue() == 0)
        {
            plan = planFactory.createDefaultAnimationPlan(loopSpan, activeDuration, myAnimationModel.getSkippedIntervals());
            if (plan.getAnimationSequence().size() > 500)
            {
                plan = null;
            }
        }

        if (plan == null)
        {
            /* Round the loop span to the nearest day boundary so that there
             * doesn't need to be a new sequence for every little loop span
             * change. */
            Date sequenceStart = TimelineUtilities.roundDown(loopSpan.getStartDate(), Days.ONE).getTime();
            Date sequenceEnd = TimelineUtilities.roundUp(loopSpan.getEndDate(), Days.ONE).getTime();
            TimeSpan sequenceSpan = TimeSpan.get(sequenceStart, sequenceEnd);

            plan = planFactory.createDefaultContinuousAnimationPlan(loopSpan, sequenceSpan, activeDuration, advance,
                    myAnimationModel.getSkippedIntervals());
        }

        return plan;
    }

    /**
     * React to a new animation plan which has just been established.
     *
     * @param plan The new animation plan.
     */
    private void handleAnimationPlanEstablished(AnimationPlan plan)
    {
        assert EventQueue.isDispatchThread();

        TimeSpanList sequence = new TimeSpanArrayList(plan.getAnimationSequence());
        if (sequence.isEmpty())
        {
            return;
        }

        myAnimationPlanDirty = true;
        myAnimationPlanGenerationSuspended = true;
        try
        {
            if (plan instanceof ContinuousAnimationPlan)
            {
                ContinuousAnimationPlan continuousPlan = (ContinuousAnimationPlan)plan;

                myAnimationModel.getLoopSpan().set(continuousPlan.getLimitWindow());
                myAnimationModel.getActiveSpanDuration().set(continuousPlan.getActiveWindowDuration());
                myAnimationModel.setAdvanceDuration(plan.getAdvanceDuration());
            }
            else
            {
                myAnimationModel.getLoopSpan().set(sequence.getExtent());
                AnimationState state = myAnimationManager.getAnimationState();
                Duration dur = plan.getTimeSpanForState(state).getDuration();
                myAnimationModel.getActiveSpanDuration().set(dur);
                myAnimationModel.setAdvanceDuration(plan.getAdvanceDuration());
            }
        }
        finally
        {
            myAnimationPlanGenerationSuspended = false;
            Direction animationDirection = myAnimationManager.getAnimationState() != null && myAnimationManager.isPlaying()
                    ? myAnimationManager.getAnimationState().getDirection() : null;
            generateAnimationPlan((TimeSpan)null, animationDirection);
        }
    }
}
