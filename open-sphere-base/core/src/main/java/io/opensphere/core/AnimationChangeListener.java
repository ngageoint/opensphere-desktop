package io.opensphere.core;

import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.ThreePhaseChangeListener;

/** Interface for listeners for changes to the animation plan. */
public interface AnimationChangeListener extends ThreePhaseChangeListener<AnimationState>
{
    /**
     * Animation plan cancelled.
     */
    void animationPlanCancelled();

    /**
     * Animation plan established.
     *
     * @param plan The new animation plan.
     */
    void animationPlanEstablished(AnimationPlan plan);

    /**
     * Animation rate changed.
     *
     * @param changeRate the change rate
     */
    void animationRateChanged(Duration changeRate);

    /**
     * Method called when the animation is started.
     *
     * @param direction The direction in which the animation is now going.
     */
    void animationStarted(Direction direction);

    /**
     * Method called when the animation state changes.
     *
     * @param currentState The current animation state.
     */
    void animationStepChanged(AnimationState currentState);

    /**
     * Method called when the animation is stopped.
     */
    void animationStopped();
}
