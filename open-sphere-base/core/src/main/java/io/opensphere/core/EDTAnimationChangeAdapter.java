package io.opensphere.core;

import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * An adapter for AnimationChangeListener that puts all calls on the EDT.
 */
public abstract class EDTAnimationChangeAdapter extends AnimationChangeAdapter
{
    /**
     * Animation plan cancelled.
     */
    @Override
    public void animationPlanCancelled()
    {
        EventQueueUtilities.invokeLater(this::animationPlanCancelledOnEDT);
    }

    /**
     * Animation plan established.
     *
     * @param plan The plan.
     */
    @Override
    public void animationPlanEstablished(final AnimationPlan plan)
    {
        EventQueueUtilities.invokeLater(() -> animationPlanEstablishedOnEDT(plan));
    }

    /**
     * Animation rate changed.
     *
     * @param changeRate The change rate.
     */
    @Override
    public void animationRateChanged(final Duration changeRate)
    {
        EventQueueUtilities.invokeLater(() -> animationRateChangedOnEDT(changeRate));
    }

    /**
     * Animation started.
     *
     * @param direction The direction.
     */
    @Override
    public void animationStarted(final Direction direction)
    {
        EventQueueUtilities.invokeLater(() -> animationStartedOnEDT(direction));
    }

    /**
     * Animation step changed.
     *
     * @param currentState The current state.
     */
    @Override
    public void animationStepChanged(final AnimationState currentState)
    {
        EventQueueUtilities.invokeLater(() -> animationStepChangedOnEDT(currentState));
    }

    /**
     * Animation stopped.
     */
    @Override
    public void animationStopped()
    {
        EventQueueUtilities.invokeLater(this::animationStoppedOnEDT);
    }

    /** Hook method that will be called on the EDT. */
    protected void animationPlanCancelledOnEDT()
    {
    }

    /**
     * Hook method that will be called on the EDT.
     *
     * @param plan The plan.
     */
    protected void animationPlanEstablishedOnEDT(AnimationPlan plan)
    {
    }

    /**
     * Hook method that will be called on the EDT.
     *
     * @param changeRate The change rate.
     */
    protected void animationRateChangedOnEDT(Duration changeRate)
    {
    }

    /**
     * Hook method that will be called on the EDT.
     *
     * @param direction The direction.
     */
    protected void animationStartedOnEDT(Direction direction)
    {
    }

    /**
     * Hook method that will be called on the EDT.
     *
     * @param currentState The current state.
     */
    protected void animationStepChangedOnEDT(AnimationState currentState)
    {
    }

    /** Hook method that will be called on the EDT. */
    protected void animationStoppedOnEDT()
    {
    }
}
