package io.opensphere.controlpanels.animation.model;

import io.opensphere.core.animation.AnimationState;

/** State of animation playback. */
public enum PlayState
{
    /** Playback backward. */
    BACKWARD,

    /** Playback forward. */
    FORWARD,

    /** Step backward. */
    STEP_BACKWARD,

    /** Step to the first frame. */
    STEP_FIRST,

    /** Step forward. */
    STEP_FORWARD,

    /** Step to the last frame. */
    STEP_LAST,

    /** Playback stopped. */
    STOP,

    ;

    /**
     * Gets the direction.
     *
     * @return the direction
     */
    public AnimationState.Direction getDirection()
    {
        if (this == STOP)
        {
            return null;
        }

        if (this == BACKWARD || this == STEP_BACKWARD || this == STEP_FIRST)
        {
            return AnimationState.Direction.BACKWARD;
        }

        return AnimationState.Direction.FORWARD;
    }

    /**
     * Returns whether the state is a playing state.
     *
     * @return whether the state is a playing state
     */
    public boolean isPlaying()
    {
        return this == FORWARD || this == BACKWARD;
    }
}
