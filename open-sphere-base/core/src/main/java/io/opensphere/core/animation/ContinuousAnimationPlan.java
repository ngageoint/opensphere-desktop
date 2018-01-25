package io.opensphere.core.animation;

import io.opensphere.core.units.duration.Duration;

/**
 * An animation plan that allows for the duration of the active window and the
 * size of the advance step to be independent of the animation sequence.
 */
public interface ContinuousAnimationPlan extends AnimationPlan
{
    /**
     * Get the amount of time in the active window.
     *
     * @return The active window duration.
     */
    Duration getActiveWindowDuration();
}
