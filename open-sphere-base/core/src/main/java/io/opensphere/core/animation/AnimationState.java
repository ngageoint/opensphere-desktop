package io.opensphere.core.animation;

/**
 * The state of the animation.
 */
public interface AnimationState
{
    /**
     * The direction to follow after this state.
     *
     * @return The next direction.
     */
    Direction getDirection();

    /**
     * Get a copy of this state, but pointing in the reverse direction.
     *
     * @return The copy.
     */
    AnimationState reverse();

    /**
     * The Direction of animation.
     */
    enum Direction
    {
        /** Indicates the animation runs backward. */
        BACKWARD,

        /** Indicates the animation runs forward. */
        FORWARD,

        ;

        /**
         * Get the opposite direction.
         *
         * @return The opposite direction.
         */
        public Direction opposite()
        {
            return equals(FORWARD) ? BACKWARD : FORWARD;
        }
    }
}
