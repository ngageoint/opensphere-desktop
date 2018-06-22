package io.opensphere.core.animation.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.Nullable;

import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanArrayList;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * Default implementation of {@link AnimationPlan}.
 */
@SuppressWarnings("PMD.GodClass")
public class DefaultAnimationPlan implements AnimationPlan
{
    /** The animation sequence. */
    private final List<? extends TimeSpan> myAnimationSequence;

    /** The end behavior. */
    private final EndBehavior myEndBehavior;

    /** Ignore the ThreePhaseChangeSupport update processing timer if false. */
    private boolean myIsUsingProcessingTimeout = true;

    /** The set of time spans in the sequence. */
    private final Set<? extends TimeSpan> myTimeSpanSet;

    /**
     * Instantiates a new default animation plan.
     *
     * @param sequence The sequence of discrete time spans across which the
     *            animation is to be performed.
     * @param endBehavior The behavior when the end of the sequence is reached.
     */
    public DefaultAnimationPlan(List<? extends TimeSpan> sequence, EndBehavior endBehavior)
    {
        Utilities.checkNull(sequence, "sequence");
        Utilities.checkNull(endBehavior, "endBehavior");

        // Allow an empty sequence(when all intervals have been de selected in
        // the UI)
        if (sequence.isEmpty())
        {
            myAnimationSequence = Collections.<TimeSpan>emptyList();
        }
        else
        {
            myAnimationSequence = New.unmodifiableList(sequence);
        }

        myTimeSpanSet = New.unmodifiableSet(myAnimationSequence);
        myEndBehavior = endBehavior;
    }

    @Override
    public int calculateDistance(AnimationState from, AnimationState to)
    {
        Utilities.checkNull(from, "from");
        Utilities.checkNull(to, "to");
        DefaultAnimationState fromState = getDefaultAnimationState(from);
        validateStepNumber(fromState.getStepNumber());
        DefaultAnimationState toState = getDefaultAnimationState(to);

        int delta = toState.getStepNumber() - fromState.getStepNumber();
        if (fromState.getDirection() == Direction.BACKWARD)
        {
            delta *= -1;
        }

        if (delta >= 0)
        {
            return delta;
        }
        else
        {
            switch (getEndBehavior())
            {
                case BOUNCE:
                    return (fromState.getDirection() == Direction.FORWARD
                            ? myAnimationSequence.size() - 1 - fromState.getStepNumber() : fromState.getStepNumber()) * 2 - delta;
                case STOP:
                    return Integer.MAX_VALUE;
                case WRAP:
                    return myAnimationSequence.size() + delta;
                default:
                    throw new UnexpectedEnumException(getEndBehavior());
            }
        }
    }

    @Override
    public DefaultAnimationState determineNextState(AnimationState state)
    {
        DefaultAnimationState animationState = getDefaultAnimationState(state);
        validateStepNumber(animationState.getStepNumber());
        int nextIndex;
        Direction nextDirection = animationState.getDirection();
        if (getAnimationSequence().size() > 1)
        {
            if (animationState.getDirection() == Direction.FORWARD)
            {
                nextIndex = animationState.getStepNumber() + 1;
                if (nextIndex >= getAnimationSequence().size())
                {
                    switch (getEndBehavior())
                    {
                        case BOUNCE:
                            nextDirection = animationState.getDirection().opposite();
                            nextIndex = animationState.getStepNumber() - 1;
                            break;
                        case STOP:
                            nextDirection = animationState.getDirection();
                            nextIndex = -1;
                            break;
                        case WRAP:
                            nextDirection = animationState.getDirection();
                            nextIndex = 0;
                            break;
                        default:
                            throw new UnexpectedEnumException(getEndBehavior());
                    }
                }
            }
            else
            {
                nextIndex = animationState.getStepNumber() - 1;
                if (nextIndex < 0)
                {
                    switch (getEndBehavior())
                    {
                        case BOUNCE:
                            nextDirection = animationState.getDirection().opposite();
                            nextIndex = animationState.getStepNumber() + 1;
                            break;
                        case STOP:
                            nextDirection = animationState.getDirection();
                            nextIndex = -1;
                            break;
                        case WRAP:
                            nextDirection = animationState.getDirection();
                            nextIndex = getAnimationSequence().size() - 1;
                            break;
                        default:
                            throw new UnexpectedEnumException(getEndBehavior());
                    }
                }
            }
        }
        else if (getEndBehavior() == EndBehavior.STOP)
        {
            return null;
        }
        else
        {
            nextDirection = getEndBehavior() == EndBehavior.WRAP ? state.getDirection() : state.getDirection().opposite();
            nextIndex = 0;
        }

        return nextIndex >= 0 ? new DefaultAnimationState(nextIndex, nextDirection) : null;
    }

    @Override
    public DefaultAnimationState determinePreviousState(AnimationState state)
    {
        DefaultAnimationState animationState = getDefaultAnimationState(state);
        validateStepNumber(animationState.getStepNumber());
        int prevIndex;
        Direction prevDirection = animationState.getDirection();
        if (getAnimationSequence().size() > 1)
        {
            if (animationState.getDirection() == Direction.BACKWARD)
            {
                prevIndex = animationState.getStepNumber() + 1;
                if (prevIndex >= getAnimationSequence().size())
                {
                    switch (getEndBehavior())
                    {
                        case BOUNCE:
                            prevDirection = animationState.getDirection().opposite();
                            prevIndex = animationState.getStepNumber() - 1;
                            break;
                        case STOP:
                            prevDirection = animationState.getDirection();
                            prevIndex = -1;
                            break;
                        case WRAP:
                            prevDirection = animationState.getDirection();
                            prevIndex = 0;
                            break;
                        default:
                            throw new UnexpectedEnumException(getEndBehavior());
                    }
                }
            }
            else
            {
                prevIndex = animationState.getStepNumber() - 1;
                if (prevIndex < 0)
                {
                    switch (getEndBehavior())
                    {
                        case BOUNCE:
                            prevDirection = animationState.getDirection().opposite();
                            prevIndex = animationState.getStepNumber() + 1;
                            break;
                        case STOP:
                            prevDirection = animationState.getDirection();
                            prevIndex = -1;
                            break;
                        case WRAP:
                            prevDirection = animationState.getDirection();
                            prevIndex = getAnimationSequence().size() - 1;
                            break;
                        default:
                            throw new UnexpectedEnumException(getEndBehavior());
                    }
                }
            }
        }
        else if (getEndBehavior() == EndBehavior.STOP)
        {
            return null;
        }
        else
        {
            prevDirection = Direction.FORWARD;
            prevIndex = 0;
        }

        return prevIndex >= 0 ? new DefaultAnimationState(prevIndex, prevDirection) : null;
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
        DefaultAnimationPlan other = (DefaultAnimationPlan)obj;
        return EqualsHelper.equals(myAnimationSequence, other.myAnimationSequence, myEndBehavior, other.myEndBehavior);
    }

    @Override
    @Nullable
    public DefaultAnimationState findState(Date time, Direction direction)
    {
        Utilities.checkNull(time, "time");
        Utilities.checkNull(direction, "direction");
        int resultIndex = 0;
        long minDistance = Long.MAX_VALUE;
        for (int index = 0; index < myAnimationSequence.size(); ++index)
        {
            TimeSpan span = myAnimationSequence.get(index);
            if (span.overlaps(time))
            {
                resultIndex = index;
                break;
            }
            long dist = Math.abs(span.getStart() + span.getEnd() - time.getTime() * 2);
            if (dist < minDistance)
            {
                minDistance = dist;
                resultIndex = index;
            }
        }

        return new DefaultAnimationState(resultIndex, direction);
    }

    @Override
    public DefaultAnimationState findState(TimeSpan span, Direction direction)
    {
        Utilities.checkNull(span, "span");
        Utilities.checkNull(direction, "direction");

        if (isEmptyPlan())
        {
            return null;
        }

        int resultIndex = 0;
        long minDistance = Long.MAX_VALUE;
        for (int index = 0; index < myAnimationSequence.size(); ++index)
        {
            TimeSpan sequenceSpan = myAnimationSequence.get(index);
            if (sequenceSpan.equals(span) || sequenceSpan.contains(span))
            {
                resultIndex = index;
                break;
            }
            long dist = Math.abs(sequenceSpan.getStart() - span.getStart() + sequenceSpan.getEnd() - span.getEnd());
            if (dist < minDistance)
            {
                minDistance = dist;
                resultIndex = index;
            }
        }

        return new DefaultAnimationState(resultIndex, direction);
    }

    @Override
    public Duration getAdvanceDuration()
    {
        TimeSpan first = myAnimationSequence.get(0);
        if (myAnimationSequence.size() > 1)
        {
            TimeSpan second = myAnimationSequence.get(1);
            return new Milliseconds(second.getStart() - first.getStart());
        }
        else
        {
            return first.getDuration();
        }
    }

    @Override
    public List<? extends TimeSpan> getAnimationSequence()
    {
        return myAnimationSequence;
    }

    @Override
    public TimeSpanList getAnimationSequence(AnimationState state, int number, Direction direction)
    {
        List<TimeSpan> steps = New.list();

        for (int i = 1; i <= number; ++i)
        {
            int relativeIndex = getInrangeIndex(getDefaultAnimationState(state), i);
            steps.add(myAnimationSequence.get(relativeIndex));
        }

        return new TimeSpanArrayList(steps);
    }

    @Override
    public EndBehavior getEndBehavior()
    {
        return myEndBehavior;
    }

    @Override
    public DefaultAnimationState getFinalState()
    {
        return new DefaultAnimationState(getAnimationSequence().size() - 1, Direction.FORWARD);
    }

    @Override
    public AnimationState getFinalState(AnimationState state)
    {
        return getFinalState();
    }

    @Override
    public DefaultAnimationState getInitialState()
    {
        return new DefaultAnimationState(0, Direction.FORWARD);
    }

    @Override
    public Set<? extends TimeSpan> getTimeCoverage()
    {
        return myTimeSpanSet;
    }

    @Override
    public TimeSpan getTimeSpanForState(AnimationState state)
    {
        if (isEmptyPlan())
        {
            return TimeSpan.ZERO;
        }
        else
        {
            int stepNumber = getDefaultAnimationState(state).getStepNumber();
            validateStepNumber(stepNumber);
            return getAnimationSequence().get(stepNumber);
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(myAnimationSequence, myEndBehavior);
    }

    @Override
    public boolean isEmptyPlan()
    {
        return myAnimationSequence == null || myAnimationSequence.isEmpty();
    }

    @Override
    public boolean isUsingProcessingTimeout()
    {
        return myIsUsingProcessingTimeout;
    }

    /**
     * Sets the ignores timeout.
     *
     * @param ignoresTimeout the new ignores timeout
     */
    public void setUsingProcessingTimeout(boolean ignoresTimeout)
    {
        myIsUsingProcessingTimeout = ignoresTimeout;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128).append("DefaultAnimationPlan :\n    Sequence : ").append(myAnimationSequence).toString();
    }

    /**
     * Validate a step number.
     *
     * @param stepNumber The step number.
     * @throws IllegalArgumentException If the step number is invalid.
     */
    protected void validateStepNumber(int stepNumber) throws IllegalArgumentException
    {
        if (!isEmptyPlan() && (stepNumber < 0 || stepNumber >= getAnimationSequence().size()))
        {
            throw new IllegalArgumentException("Invalid animation state (index " + stepNumber + " out of bounds).");
        }
    }

    /**
     * Calculate a position after "bouncing" off of one of the ends. This should
     * only be called when the index is either less than 0 or greater than the
     * max index.
     *
     * @param index The out of range index.
     * @param maxIndex The maximum allowable index.
     * @return the index after bouncing one time.
     */
    private int calculateBounce(int index, int maxIndex)
    {
        if (index < 0)
        {
            return -index;
        }
        else
        {
            return maxIndex - (index - maxIndex);
        }
    }

    /**
     * Cast the argument to a {@link DefaultAnimationState}.
     *
     * @param state The state.
     * @return The {@link DefaultAnimationState}.
     */
    private DefaultAnimationState getDefaultAnimationState(AnimationState state)
    {
        if (!(state instanceof DefaultAnimationState))
        {
            throw new IllegalArgumentException("State is not a " + DefaultAnimationState.class);
        }
        return (DefaultAnimationState)state;
    }

    /**
     * Get the index of the animation plan after stepping by the number of steps
     * specified by the given offset and ensure it is in range using the plan's
     * end behavior.
     *
     * @param state The origin state.
     * @param offset The number of steps for which the index is desired.
     * @return The index of the animation plan after stepping by the number of
     *         steps specified by the given offset.
     */
    private int getInrangeIndex(DefaultAnimationState state, int offset)
    {
        int offsetToUse = state.getDirection() == Direction.FORWARD ? offset : -offset;
        int maxIndex = myAnimationSequence.size() - 1;
        int relativeIndex = state.getStepNumber() + offsetToUse;
        if (relativeIndex >= 0 && relativeIndex < maxIndex)
        {
            return relativeIndex;
        }
        if (myEndBehavior == EndBehavior.WRAP)
        {
            return relativeIndex % maxIndex;
        }
        if (myEndBehavior == EndBehavior.BOUNCE)
        {
            // If the sequence size is small compared to the relative index, it
            // may bounce several times before it is settles.
            while (relativeIndex < 0 || relativeIndex > maxIndex)
            {
                relativeIndex = calculateBounce(relativeIndex, maxIndex);
            }
            return relativeIndex;
        }
        else
        {
            return MathUtil.clamp(relativeIndex, 0, maxIndex);
        }
    }

    @Override
    public TimeSpan getLimitWindow()
    {
        return myAnimationSequence.isEmpty() ? TimeSpan.ZERO : TimeSpan.get(myAnimationSequence.get(0).getStartInstant(),
                myAnimationSequence.get(myAnimationSequence.size() - 1).getEndInstant());
    }
}
