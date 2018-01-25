package io.opensphere.core.appl;

import io.opensphere.core.AnimationChangeListener;
import io.opensphere.core.AnimationManager;
import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationPlanModificationException;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.PhasedChangeArbitrator;

/** Simple AnimationManager for testing purposes. */
public class TestAnimationManager implements AnimationManager
{
    /** The plan */
    private AnimationPlan myPlan;

    /** Whether playing. */
    private boolean myIsPlaying;

    /** The change rate. */
    private Duration myChangeRate;

    @Override
    public void abandonPlan()
    {
    }

    @Override
    public void addAnimationChangeListener(AnimationChangeListener listener)
    {
    }

    @Override
    public void addPhasedChangeArbitrator(PhasedChangeArbitrator arbitrator)
    {
    }

    @Override
    public AnimationState getAnimationState()
    {
        return null;
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
        return false;
    }

    @Override
    public boolean isPlaying()
    {
        return myIsPlaying;
    }

    @Override
    public boolean jumpToStep(AnimationPlan plan, TimeSpan step, boolean waitForListeners)
        throws AnimationPlanModificationException
    {
        return false;
    }

    @Override
    public void pause(AnimationPlan plan)
    {
        myIsPlaying = false;
    }

    @Override
    public void play(AnimationPlan plan, Direction direction)
    {
        myIsPlaying = true;
    }

    @Override
    public void removeAnimationChangeListener(AnimationChangeListener listener)
    {
    }

    @Override
    public void removePhasedChangeArbitrator(PhasedChangeArbitrator arbitrator)
    {
    }

    @Override
    public void setChangeRate(AnimationPlan plan, Duration changeRate)
    {
        myChangeRate = changeRate;
    }

    @Override
    public void setPlan(AnimationPlan plan)
    {
        myPlan = plan;
    }

    @Override
    public void setPlan(AnimationPlan plan, AnimationState initState)
    {
        myPlan = plan;
    }

    @Override
    public void setPlan(AnimationPlan plan, AnimationState initState, Direction animationDirection, Duration changeRate)
    {
        myPlan = plan;
        myChangeRate = changeRate;
        myIsPlaying = animationDirection != null;
    }

    @Override
    public void stepBackward(AnimationPlan plan, boolean waitForListeners) throws AnimationPlanModificationException
    {
    }

    @Override
    public void stepFirst(AnimationPlan plan, boolean waitForListeners) throws AnimationPlanModificationException
    {
    }

    @Override
    public void stepForward(AnimationPlan plan, boolean waitForListeners) throws AnimationPlanModificationException
    {
    }

    @Override
    public void stepLast(AnimationPlan plan, boolean waitForListeners) throws AnimationPlanModificationException
    {
    }

    @Override
    public void stepNext(AnimationPlan plan, boolean waitForListeners) throws AnimationPlanModificationException
    {
    }

    @Override
    public void stepPrevious(AnimationPlan plan, boolean waitForListeners) throws AnimationPlanModificationException
    {
    }
}
