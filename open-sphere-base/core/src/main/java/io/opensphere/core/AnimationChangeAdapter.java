package io.opensphere.core;

import java.util.concurrent.Phaser;

import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.units.duration.Duration;

/**
 * An adapter providing an empty implementation of the AnimationChangeListener.
 */
public abstract class AnimationChangeAdapter implements AnimationChangeListener
{
    @Override
    public void animationPlanCancelled()
    {
    }

    @Override
    public void animationPlanEstablished(AnimationPlan plan)
    {
    }

    @Override
    public void animationRateChanged(Duration changeRate)
    {
    }

    @Override
    public void animationStarted(Direction direction)
    {
    }

    @Override
    public void animationStepChanged(AnimationState currentState)
    {
    }

    @Override
    public void animationStopped()
    {
    }

    @Override
    public void commit(AnimationState state, Phaser phaser)
    {
        animationStepChanged(state);
    }

    @Override
    public boolean preCommit(AnimationState changeInfo, Phaser phaser)
    {
        return true;
    }

    @Override
    public boolean prepare(AnimationState changeInfo, Phaser phaser)
    {
        return true;
    }
}
