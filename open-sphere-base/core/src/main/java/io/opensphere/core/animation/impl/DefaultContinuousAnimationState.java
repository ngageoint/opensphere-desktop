package io.opensphere.core.animation.impl;

import java.util.Objects;

import io.opensphere.core.animation.ContinuousAnimationState;
import io.opensphere.core.model.time.TimeSpan;

/**
 * Animation state that keeps track of a time span as well as a step number.
 */
public class DefaultContinuousAnimationState extends DefaultAnimationState implements ContinuousAnimationState
{
    /** The active time span. */
    private final TimeSpan myActiveTimeSpan;

    /**
     * Constructor.
     *
     * @param stepNumber The step number in the animation sequence.
     * @param activeTimeSpan The active time within the step.
     * @param direction The direction of the animation.
     */
    public DefaultContinuousAnimationState(int stepNumber, TimeSpan activeTimeSpan, Direction direction)
    {
        super(stepNumber, direction);
        myActiveTimeSpan = activeTimeSpan;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultContinuousAnimationState other = (DefaultContinuousAnimationState)obj;
        return Objects.equals(myActiveTimeSpan, other.myActiveTimeSpan);
    }

    /**
     * Get the active time.
     *
     * @return The time.
     */
    @Override
    public TimeSpan getActiveTimeSpan()
    {
        return myActiveTimeSpan;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myActiveTimeSpan == null ? 0 : myActiveTimeSpan.hashCode());
        return result;
    }

    @Override
    public DefaultContinuousAnimationState reverse()
    {
        return new DefaultContinuousAnimationState(getStepNumber(), getActiveTimeSpan(), getDirection().opposite());
    }

    @Override
    public String toString()
    {
        return new StringBuilder(64).append(getClass().getSimpleName()).append(" [dir=").append(getDirection()).append(", step=")
                .append(getStepNumber()).append(", timeSpan=").append(getActiveTimeSpan()).append(']').toString();
    }
}
