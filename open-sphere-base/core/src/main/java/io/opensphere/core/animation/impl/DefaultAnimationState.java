package io.opensphere.core.animation.impl;

import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.util.Utilities;

/**
 * Default implementation of {@link AnimationState}.
 */
public class DefaultAnimationState implements AnimationState
{
    /** The direction. */
    private final Direction myDirection;

    /** The step number. */
    private final int myStepNumber;

    /**
     * Constructor.
     *
     * @param stepNumber The step number in the animation sequence.
     * @param direction The direction.
     */
    public DefaultAnimationState(int stepNumber, Direction direction)
    {
        myStepNumber = stepNumber;
        myDirection = Utilities.checkNull(direction, "direction");
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultAnimationState other = (DefaultAnimationState)obj;
        return myDirection == other.myDirection && myStepNumber == other.myStepNumber;
    }

    @Override
    public Direction getDirection()
    {
        return myDirection;
    }

    /**
     * The index into the animation sequence, zero-based.
     *
     * @return The index.
     */
    public int getStepNumber()
    {
        return myStepNumber;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myDirection == null ? 0 : myDirection.hashCode());
        result = prime * result + myStepNumber;
        return result;
    }

    @Override
    public DefaultAnimationState reverse()
    {
        return new DefaultAnimationState(getStepNumber(), getDirection().opposite());
    }

    @Override
    public String toString()
    {
        return new StringBuilder(64).append(getClass().getSimpleName()).append(" [dir=").append(getDirection()).append(", step=")
                .append(getStepNumber()).append(']').toString();
    }
}
