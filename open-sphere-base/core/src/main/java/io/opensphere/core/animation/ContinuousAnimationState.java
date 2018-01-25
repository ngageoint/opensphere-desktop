package io.opensphere.core.animation;

import io.opensphere.core.model.time.TimeSpan;

/**
 * Animation state that keeps track of a time span as well as a step number.
 */
public interface ContinuousAnimationState extends AnimationState
{
    /**
     * Get the active time.
     *
     * @return The time.
     */
    TimeSpan getActiveTimeSpan();
}
