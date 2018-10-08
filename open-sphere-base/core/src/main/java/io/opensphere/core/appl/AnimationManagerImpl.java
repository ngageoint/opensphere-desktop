package io.opensphere.core.appl;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.AnimationChangeListener;
import io.opensphere.core.AnimationManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.PrimaryTimeSpanChangeListener;
import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationPlanModificationException;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.PhasedChangeArbitrator;
import io.opensphere.core.util.PropertyChangeException;
import io.opensphere.core.util.ThreePhaseChangeSupport;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakThreePhaseChangeSupport;
import io.opensphere.core.util.concurrent.ReportingScheduledExecutorService;
import io.opensphere.core.util.lang.ImpossibleException;
import io.opensphere.core.util.lang.StringUtilities;
import net.jcip.annotations.GuardedBy;

/**
 * The Implementation class of {@link AnimationManager}.
 */
@SuppressWarnings("PMD.GodClass")
public class AnimationManagerImpl implements AnimationManager
{
    /**
     * Default timeout to wait for listeners to prepare for the animation to be
     * stepped.
     */
    private static final Seconds DEFAULT_LISTENER_READY_TIMEOUT = Duration.create(Seconds.class, 5);

    /** The Logger. */
    private static final Logger LOGGER = Logger.getLogger(AnimationManagerImpl.class);

    /** Listener for time changes from the time manager. */
    private final PrimaryTimeSpanChangeListener myActiveTimeSpanChangeListener = new PrimaryTimeSpanChangeListener()
    {
        @Override
        public void primaryTimeSpansChanged(TimeSpanList active)
        {
            final TimeSpan timeSpan = active.get(0);
            myPlanLock.lock();
            try
            {
                if (myPlan != null)
                {
                    jumpToStep(myPlan, timeSpan, false);
                }
            }
            catch (AnimationPlanModificationException e)
            {
                throw new ImpossibleException(e);
            }
            finally
            {
                myPlanLock.unlock();
            }
        }

        @Override
        public void primaryTimeSpansCleared()
        {
        }
    };

    /** The current animation state. */
    @GuardedBy("myPlanLock")
    private AnimationState myAnimationState;

    /** The executor service for the animator. */
    private final ReportingScheduledExecutorService myAnimatorExecutor;

    /** The animator future. */
    private ScheduledFuture<?> myAnimatorFuture;

    /** The change rate. */
    private Duration myChangeRate = Seconds.ONE;

    /** The change support. */
    // @formatter:off
    private final ThreePhaseChangeSupport<AnimationState, AnimationChangeListener> myChangeSupport =
            new WeakThreePhaseChangeSupport<>();
    // @formatter:on

    /** When true, ignore any requested changed to the animation plan. */
    private boolean myIgnorePlanChanges;

    /** The current plan. */
    private volatile AnimationPlan myPlan;

    /** The plan lock. */
    private final ReentrantLock myPlanLock = new ReentrantLock();

    /** The preferences. */
    private final Preferences myPreferences;

    /** The time manager. */
    private final TimeManager myTimeManager;

    /**
     * Construct the animation manager.
     *
     * @param timeManager The time manager.
     * @param prefs The preferences.
     * @param executor The executor to use for animating.
     */
    public AnimationManagerImpl(TimeManager timeManager, Preferences prefs, ScheduledExecutorService executor)
    {
        myTimeManager = timeManager;
        myTimeManager.addPrimaryTimeSpanChangeListener(myActiveTimeSpanChangeListener);
        myAnimatorExecutor = new ReportingScheduledExecutorService(executor);
        myPreferences = prefs;
    }

    @Override
    public void abandonPlan()
    {
        myPlanLock.lock();
        try
        {
            cancelCurrentPlan(true);
        }
        finally
        {
            myPlanLock.unlock();
        }
    }

    @Override
    public void addAnimationChangeListener(AnimationChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public void addPhasedChangeArbitrator(PhasedChangeArbitrator arbitrator)
    {
        myChangeSupport.addPhasedChangeArbitrator(arbitrator);
    }

    @Override
    public AnimationState getAnimationState()
    {
        myPlanLock.lock();
        try
        {
            return myAnimationState;
        }
        finally
        {
            myPlanLock.unlock();
        }
    }

    @Override
    public Duration getChangeRate()
    {
        return myChangeRate;
    }

    @Override
    public AnimationPlan getCurrentPlan()
    {
        return myPlan;
    }

    @Override
    public boolean isPlanEstablished()
    {
        return myPlan != null;
    }

    @Override
    public boolean isPlaying()
    {
        return myAnimatorFuture != null;
    }

    @Override
    public boolean jumpToStep(AnimationPlan plan, TimeSpan step, boolean waitForListeners)
        throws AnimationPlanModificationException
    {
        Utilities.checkNull(step, "step");
        myPlanLock.lock();
        try
        {
            verifyPlanChange(plan);
            Direction direction = step.compareTo(myPlan.getTimeSpanForState(myAnimationState)) >= 0 ? Direction.FORWARD
                    : Direction.BACKWARD;
            AnimationState newState = myPlan.findState(step, direction);
            if (newState == null || !newState.equals(myAnimationState))
            {
                boolean wasPlaying = isPlaying();
                stopAnimator();

                if (newState != null)
                {
                    setState(newState, waitForListeners);
                    if (wasPlaying)
                    {
                        startAnimator();
                    }
                }
            }
        }
        finally
        {
            myPlanLock.unlock();
        }
        return false;
    }

    @Override
    public void pause(AnimationPlan plan) throws AnimationPlanModificationException
    {
        myPlanLock.lock();
        try
        {
            verifyPlanChange(plan);
            if (myAnimatorFuture != null)
            {
                myAnimatorFuture.cancel(false);
                myAnimatorFuture = null;
            }
        }
        finally
        {
            myPlanLock.unlock();
        }
        notifyAnimationChangeListeners(listener -> listener.animationStopped());
    }

    @Override
    public void play(AnimationPlan plan, final AnimationState.Direction direction) throws AnimationPlanModificationException
    {
        boolean notifyAnimationStarted = false;
        myPlanLock.lock();
        try
        {
            verifyPlanChange(plan);
            if (!myAnimationState.getDirection().equals(direction))
            {
                myAnimationState = myAnimationState.reverse();
            }
            if (myAnimatorFuture == null)
            {
                startAnimator();
                notifyAnimationStarted = true;
            }
        }
        finally
        {
            myPlanLock.unlock();
        }
        if (notifyAnimationStarted)
        {
            notifyAnimationChangeListeners(listener -> listener.animationStarted(direction));
        }
    }

    @Override
    public void removeAnimationChangeListener(AnimationChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void removePhasedChangeArbitrator(PhasedChangeArbitrator arbitrator)
    {
        myChangeSupport.removePhasedChangeArbitrator(arbitrator);
    }

    @Override
    public void setChangeRate(AnimationPlan plan, final Duration changeRate) throws AnimationPlanModificationException
    {
        if (myIgnorePlanChanges)
        {
            return;
        }

        if (myChangeRate.compareTo(changeRate) == 0)
        {
            return;
        }
        myPlanLock.lock();
        try
        {
            verifyPlanChange(plan);
            myChangeRate = changeRate;

            if (myAnimatorFuture != null)
            {
                myAnimatorFuture.cancel(false);
                startAnimator();
            }
        }
        finally
        {
            myPlanLock.unlock();
        }
        notifyAnimationChangeListeners(listener -> listener.animationRateChanged(changeRate));
    }

    @Override
    public void setPlan(AnimationPlan plan)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Animation plan set to " + plan);
        }
        setPlan(plan, null);
    }

    @Override
    public void setPlan(final AnimationPlan plan, final AnimationState initState)
    {
        setPlan(plan, initState, null, null);
    }

    @Override
    public void setPlan(final AnimationPlan plan, AnimationState initState, Direction animationDirection, Duration changeRate)
    {
        if (myIgnorePlanChanges)
        {
            return;
        }

        Utilities.checkNull(plan, "plan");
        myPlanLock.lock();
        try
        {
            myIgnorePlanChanges = true;
            doEstablishPlan(plan, initState);
        }
        finally
        {
            myPlanLock.unlock();
            myIgnorePlanChanges = false;
        }

        notifyAnimationChangeListeners(listener -> listener.animationPlanEstablished(plan));

        try
        {
            if (changeRate != null)
            {
                setChangeRate(myPlan, changeRate);
            }

            if (animationDirection != null)
            {
                play(myPlan, animationDirection);
            }
        }
        catch (AnimationPlanModificationException e)
        {
            LOGGER.error(e, e);
        }
    }

    @Override
    public void stepBackward(AnimationPlan plan, boolean waitForReady) throws AnimationPlanModificationException
    {
        myPlanLock.lock();
        try
        {
            verifyPlanChange(plan);
            doStep(myAnimationState.getDirection() == AnimationState.Direction.BACKWARD ? myAnimationState
                    : myAnimationState.reverse(), waitForReady);
        }
        finally
        {
            myPlanLock.unlock();
        }
    }

    @Override
    public void stepFirst(AnimationPlan plan, boolean waitForListeners) throws AnimationPlanModificationException
    {
        myPlanLock.lock();
        try
        {
            verifyPlanChange(plan);
            AnimationState initial = plan.getInitialState();
            if (initial != null)
            {
                setState(initial, waitForListeners);
            }
        }
        finally
        {
            myPlanLock.unlock();
        }
    }

    @Override
    public void stepForward(AnimationPlan plan, boolean waitForReady) throws AnimationPlanModificationException
    {
        myPlanLock.lock();
        try
        {
            verifyPlanChange(plan);
            doStep(myAnimationState.getDirection() == AnimationState.Direction.FORWARD ? myAnimationState
                    : myAnimationState.reverse(), waitForReady);
        }
        finally
        {
            myPlanLock.unlock();
        }
    }

    @Override
    public void stepLast(AnimationPlan plan, boolean waitForListeners) throws AnimationPlanModificationException
    {
        myPlanLock.lock();
        try
        {
            verifyPlanChange(plan);
            AnimationState finalState = plan.getFinalState();
            if (finalState != null)
            {
                setState(finalState, waitForListeners);
            }
        }
        finally
        {
            myPlanLock.unlock();
        }
    }

    @Override
    public void stepNext(AnimationPlan plan, boolean waitForReady) throws AnimationPlanModificationException
    {
        myPlanLock.lock();
        try
        {
            verifyPlanChange(plan);
            doStep(myAnimationState, waitForReady);
        }
        finally
        {
            myPlanLock.unlock();
        }
    }

    @Override
    public void stepPrevious(AnimationPlan plan, boolean waitForReady) throws AnimationPlanModificationException
    {
        myPlanLock.lock();
        try
        {
            verifyPlanChange(plan);
            doStep(myAnimationState.reverse(), waitForReady);
        }
        finally
        {
            myPlanLock.unlock();
        }
    }

    /**
     * Cancel the current plan.
     *
     * @param sendNotification the send notification
     */
    private void cancelCurrentPlan(boolean sendNotification)
    {
        myPlanLock.lock();
        try
        {
            if (myPlan == null)
            {
                return;
            }

            stopAnimator();
            myPlan = null;
        }
        finally
        {
            myPlanLock.unlock();
        }
        if (sendNotification)
        {
            notifyAnimationChangeListeners(listener ->
            {
                listener.animationStopped();
                listener.animationPlanCancelled();
            });
        }
    }

    /**
     * Establish a new (or alter an existing) animation plan. Caller provides a
     * plan and a key to be used to control execution of and changes to the
     * plan.
     *
     * @param plan the {@link AnimationPlan}
     * @param initState the init state, if null uses animation plan's default
     *            state
     */
    private void doEstablishPlan(final AnimationPlan plan, AnimationState initState)
    {
        if (myPlan != null)
        {
            cancelCurrentPlan(true);
        }

        myPlan = plan;
        if (myAnimationState == null)
        {
            setState(initState == null ? myPlan.getFinalState() : initState, false);
        }
        else
        {
            AnimationState state = myPlan.findState(myTimeManager.getPrimaryActiveTimeSpans().get(0),
                    myAnimationState.getDirection());
            if (state == null)
            {
                // Exact state not found; find the nearest state.
                state = myPlan.findState(myTimeManager.getPrimaryActiveTimeSpans().get(0).getStartDate(),
                        myAnimationState.getDirection());

                if (state == null)
                {
                    // Still no state; use the initial state.
                    state = myPlan.getInitialState();
                }
            }
            setState(state, false);
        }
    }

    /**
     * Step the animation.
     *
     * @param state The current state.
     * @param waitForListeners When true, wait for listeners to be ready before
     *            changing states.
     */
    private void doStep(AnimationState state, boolean waitForListeners)
    {
        boolean notifyStopped = false;
        myPlanLock.lock();
        try
        {
            AnimationState nextState = myPlan.determineNextState(state);
            if (nextState != null)
            {
                setState(nextState, waitForListeners);
            }
            else
            {
                // Stop playing if we don't have a next step.
                if (isPlaying())
                {
                    stopAnimator();
                    notifyStopped = true;
                }
            }
        }
        finally
        {
            myPlanLock.unlock();
        }
        if (notifyStopped)
        {
            notifyAnimationChangeListeners(listener -> listener.animationStopped());
        }
    }

    /**
     * Notify animation change listeners.
     *
     * @param callback The callback.
     */
    private void notifyAnimationChangeListeners(final Callback<AnimationChangeListener> callback)
    {
        long t0 = System.nanoTime();
        myChangeSupport.notifyListeners(callback, (Executor)null);
        if (LOGGER.isTraceEnabled())
        {
            long t1 = System.nanoTime();
            LOGGER.trace(StringUtilities.formatTimingMessage("Time to notify animation change listeners: ", t1 - t0));
        }
    }

    /**
     * Set an animation state to be current.
     *
     * @param state The new state.
     * @param waitForListeners When true, wait for listeners to be ready before
     *            changing states.
     */
    private void setState(final AnimationState state, boolean waitForListeners)
    {
        myPlanLock.lock();
        try
        {
            // If the plan is empty, all spans have been removed so set the
            // primary to the ZERO span.
            if (myPlan.isEmptyPlan())
            {
                myTimeManager.setPrimaryActiveTimeSpan(TimeSpan.ZERO);
                return;
            }

            boolean stateChanged;
            Duration timeout = myPreferences.getJAXBableObject(Duration.class, TIMEOUT_PREFERENCE_KEY,
                    DEFAULT_LISTENER_READY_TIMEOUT);
            long timeoutMillis = Milliseconds.get(timeout).longValue();
            if (waitForListeners)
            {
                stateChanged = myChangeSupport.updateState(state, timeoutMillis, false);
            }
            else
            {
                stateChanged = myChangeSupport.commit(state, timeoutMillis);
            }

            if (stateChanged)
            {
                final TimeSpan ts = myPlan.getTimeSpanForState(state);
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Animation step changed to " + ts);
                }
                myAnimationState = state;
                myTimeManager.setPrimaryActiveTimeSpan(ts);
            }
        }
        catch (PropertyChangeException | InterruptedException e)
        {
            LOGGER.error("Failed to change animation state: " + e, e);
        }
        finally
        {
            myPlanLock.unlock();
        }
    }

    /**
     * Start the animator task.
     */
    private void startAnimator()
    {
        long rateMillis = Duration.create(Milliseconds.class, myChangeRate).longValue();
        myAnimatorFuture = myAnimatorExecutor.scheduleWithFixedDelay(this::step, rateMillis, rateMillis, TimeUnit.MILLISECONDS);
    }

    /** Steps to the next step in the plan. */
    private void step()
    {
        try
        {
            stepNext(myPlan, myPlan.isUsingProcessingTimeout());
        }
        catch (AnimationPlanModificationException e)
        {
            LOGGER.error("Cannot change plan time to next step.", e);
            stopAnimator();
        }
    }

    /**
     * Stop the animator task.
     */
    private void stopAnimator()
    {
        myPlanLock.lock();
        try
        {
            if (myAnimatorFuture != null)
            {
                myAnimatorFuture.cancel(false);
                myAnimatorFuture = null;
            }
        }
        finally
        {
            myPlanLock.unlock();
        }
    }

    /**
     * Verify that the plan may be changed.
     *
     * @param plan The plan that expected to be the current plan.
     * @throws AnimationPlanModificationException when the plan cannot be
     *             modified.
     */
    private void verifyPlanChange(AnimationPlan plan) throws AnimationPlanModificationException
    {
        if (!Utilities.sameInstance(myPlan, plan))
        {
            throw new AnimationPlanModificationException(AnimationPlanModificationException.PLAN_MISMATCH);
        }
    }
}
