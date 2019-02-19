package io.opensphere.core.animation.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.animation.ContinuousAnimationPlan;
import io.opensphere.core.animation.ContinuousAnimationState;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * Default implementation of {@link ContinuousAnimationPlan}.
 */
@SuppressWarnings("PMD.GodClass")
public class DefaultContinuousAnimationPlan extends DefaultAnimationPlan implements ContinuousAnimationPlan
{
    /** How much time is active for each animation step. */
    private final Duration myActiveWindowDuration;

    /** The time difference between animation steps. */
    private final Duration myAdvanceDuration;

    /** An optional time span that limits the animation within the sequence. */
    private final TimeSpan myLimitWindow;

    /**
     * Constructor.
     *
     * @param sequence The sequence of discrete time spans across which the
     *            animation is to be performed. These are <b>not</b> each step
     *            of the animation, but rather the ranges of time that the
     *            animation will traverse.
     * @param activeWindowDuration How much time is active for each animation
     *            step.
     * @param advanceDuration The time difference between animation steps.
     * @param endBehavior The behavior when the end of the sequence is reached.
     * @param limitWindow An optional time span that limits the animation within
     *            the sequence.
     */
    public DefaultContinuousAnimationPlan(List<? extends TimeSpan> sequence, Duration activeWindowDuration,
            Duration advanceDuration, EndBehavior endBehavior, TimeSpan limitWindow)
    {
        super(sequence, endBehavior);
        for (TimeSpan timeSpan : sequence)
        {
            if (!timeSpan.isBounded())
            {
                throw new IllegalArgumentException("Cannot create " + DefaultContinuousAnimationPlan.class.getSimpleName()
                        + " with an indefinite time span: " + timeSpan);
            }
        }
        myActiveWindowDuration = Utilities.checkNull(activeWindowDuration, "activeWindowDuration");
        myAdvanceDuration = Utilities.checkNull(advanceDuration, "advanceDuration");
        myLimitWindow = limitWindow == null ? TimeSpan.TIMELESS : limitWindow;
        setUsingProcessingTimeout(true);
    }

    @Override
    public DefaultAnimationState determineNextState(AnimationState state)
    {
        if (state instanceof DefaultContinuousAnimationState)
        {
            return determineNextState((DefaultContinuousAnimationState)state);
        }
        return super.determineNextState(state);
    }

    /**
     * Determine the next continuous animation state from a given state.
     *
     * @param state The given state.
     * @return The next state.
     */
    public DefaultContinuousAnimationState determineNextState(DefaultContinuousAnimationState state)
    {
        Duration advance = getAdvanceDuration();
        if (state.getDirection() == Direction.BACKWARD)
        {
            advance = advance.negate();
        }

        TimeSpan limitedParentSpan = getLimitedParentSpan(state);

        TimeSpan nextSpan;
        /* If the parent span doesn't contain the state's active span, reset the
         * span to the beginning or end of the limit window based on the
         * direction. */
        if (!limitedParentSpan.contains(state.getActiveTimeSpan()))
        {
            if (state.getDirection() == Direction.BACKWARD)
            {
                nextSpan = TimeSpan.get(getActiveWindowDuration(), getLimitWindow().getEnd());
            }
            else
            {
                nextSpan = TimeSpan.get(getLimitWindow().getStart(), getActiveWindowDuration());
            }
        }
        else
        {
            nextSpan = TimeSpan.get(state.getActiveTimeSpan().getStartInstant().plus(advance), getActiveWindowDuration());
        }
        int stepNumber = state.getStepNumber();
        if (limitedParentSpan.contains(nextSpan))
        {
            return new DefaultContinuousAnimationState(stepNumber, nextSpan, state.getDirection());
        }
        DefaultAnimationState nextParentState = state;
        do
        {
            nextParentState = super.determineNextState(nextParentState);
        }
        while (nextParentState != null && !getLimitWindow().overlaps(getTimeSpanForState(nextParentState)));

        if (nextParentState == null)
        {
            return null;
        }

        // If the direction reversed, the parent advanced a step, but we
        // don't want to advance yet.
        if (nextParentState.getDirection() != state.getDirection() && nextParentState.getStepNumber() != state.getStepNumber())
        {
            int correctedStepNumber = nextParentState.getStepNumber()
                    - (nextParentState.getDirection() == Direction.FORWARD ? 1 : -1);
            nextParentState = new DefaultAnimationState(correctedStepNumber, nextParentState.getDirection());
        }

        return createStateFromParentState(nextParentState, nextParentState.getDirection() == Direction.FORWARD);
    }

    @Override
    public DefaultAnimationState determinePreviousState(AnimationState state)
    {
        if (state instanceof DefaultContinuousAnimationState)
        {
            return determinePreviousState((DefaultContinuousAnimationState)state);
        }
        return super.determinePreviousState(state);
    }

    /**
     * Determine the previous continuous animation state from a given state.
     *
     * @param state The given state.
     * @return The next state.
     */
    public DefaultContinuousAnimationState determinePreviousState(DefaultContinuousAnimationState state)
    {
        Duration advance = getAdvanceDuration();
        if (state.getDirection() == Direction.FORWARD)
        {
            advance = advance.negate();
        }

        TimeSpan limitedParentSpan = getLimitedParentSpan(state);

        TimeSpan prevSpan;
        /* If the parent span doesn't contain the state's active span, reset the
         * span to the beginning or end of the limit window based on the
         * direction. */
        if (!limitedParentSpan.contains(state.getActiveTimeSpan()))
        {
            if (state.getDirection() == Direction.FORWARD)
            {
                prevSpan = TimeSpan.get(limitedParentSpan.getStart(), getActiveWindowDuration());
            }
            else
            {
                prevSpan = TimeSpan.get(getActiveWindowDuration(), limitedParentSpan.getEnd());
            }
        }
        else
        {
            prevSpan = TimeSpan.get(state.getActiveTimeSpan().getStartInstant().plus(advance), getActiveWindowDuration());
        }
        int stepNumber = state.getStepNumber();

        if (limitedParentSpan.contains(prevSpan))
        {
            return new DefaultContinuousAnimationState(stepNumber, prevSpan, state.getDirection());
        }
        DefaultAnimationState prevParentState = state;
        do
        {
            prevParentState = super.determinePreviousState(prevParentState);
        }
        while (prevParentState != null && !getLimitWindow().overlaps(getTimeSpanForState(prevParentState)));

        if (prevParentState == null)
        {
            return null;
        }

        // If the direction reversed, the parent advanced a step, but we
        // don't want to advance yet.
        if (prevParentState.getDirection() != state.getDirection())
        {
            int correctedStepNumber = prevParentState.getStepNumber()
                    - (prevParentState.getDirection() == Direction.FORWARD ? -1 : 1);
            prevParentState = new DefaultAnimationState(correctedStepNumber, prevParentState.getDirection());
        }

        return createStateFromParentState(prevParentState, prevParentState.getDirection() == Direction.BACKWARD);
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
        DefaultContinuousAnimationPlan other = (DefaultContinuousAnimationPlan)obj;
        return EqualsHelper.equals(myActiveWindowDuration, other.myActiveWindowDuration, myAdvanceDuration,
                other.myAdvanceDuration, myLimitWindow, other.myLimitWindow);
    }

    @Override
    @Nullable
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public DefaultContinuousAnimationState findState(Date time, Direction direction)
    {
        DefaultAnimationState parentState = super.findState(time, direction);
        if (parentState == null)
        {
            return null;
        }
        TimeSpan parentTime = getTimeSpanForState(parentState);
        TimeSpan timeSpan;
        if (direction == Direction.FORWARD)
        {
            timeSpan = TimeSpan.get(time, myActiveWindowDuration);
        }
        else
        {
            timeSpan = TimeSpan.get(myActiveWindowDuration, time.getTime());
        }
        if (timeSpan.getEnd() > parentTime.getEnd())
        {
            timeSpan = TimeSpan.get(myActiveWindowDuration, parentTime.getEnd());
        }
        else if (timeSpan.getStart() < parentTime.getStart())
        {
            timeSpan = TimeSpan.get(parentTime.getStart(), myActiveWindowDuration);
        }

        return new DefaultContinuousAnimationState(parentState.getStepNumber(), timeSpan, parentState.getDirection());
    }

    @Override
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public DefaultContinuousAnimationState findState(TimeSpan span, Direction direction)
    {
        DefaultAnimationState parentState = super.findState(span, direction);
        if (parentState == null)
        {
            return null;
        }
        TimeSpan parentTime = getTimeSpanForState(parentState);
        TimeSpan timeSpan;
        if (direction == Direction.FORWARD)
        {
            timeSpan = TimeSpan.get(span.getStart(), myActiveWindowDuration);
        }
        else
        {
            timeSpan = TimeSpan.get(myActiveWindowDuration, span.getEnd());
        }
        if (timeSpan.getEnd() > parentTime.getEnd())
        {
            timeSpan = TimeSpan.get(myActiveWindowDuration, parentTime.getEnd());
        }
        else if (timeSpan.getStart() < parentTime.getStart())
        {
            timeSpan = TimeSpan.get(parentTime.getStart(), myActiveWindowDuration);
        }

        return new DefaultContinuousAnimationState(parentState.getStepNumber(), timeSpan, parentState.getDirection());
    }

    @Override
    public Duration getActiveWindowDuration()
    {
        return myActiveWindowDuration;
    }

    @Override
    public Duration getAdvanceDuration()
    {
        return myAdvanceDuration;
    }

    @Override
    public DefaultContinuousAnimationState getFinalState()
    {
        DefaultAnimationState finalState = super.getFinalState();
        return createStateFromParentState(finalState, false);
    }

    @Override
    public AnimationState getFinalState(AnimationState state)
    {
        if (state instanceof DefaultContinuousAnimationState)
        {
            return getFinalState((DefaultContinuousAnimationState)state);
        }
        else if (state.getDirection() == Direction.FORWARD)
        {
            return new DefaultAnimationState(getAnimationSequence().size() - 1, Direction.FORWARD);
        }
        else
        {
            return new DefaultAnimationState(0, Direction.BACKWARD);
        }
    }

    /**
     * Get the final state of the plan based on the given starting state,
     * ignoring any infinite behavior (e.g., looping).
     *
     * @param state The initial state.
     * @return The final state.
     */
    public AnimationState getFinalState(DefaultContinuousAnimationState state)
    {
        /* If step size is months/years, give up. Ideally we would calculate the final state in this case, but the math would be
         * hard and is probably not worth it. */
        if (!getAdvanceDuration().isConvertibleTo(Milliseconds.class))
        {
            return null;
        }

        boolean forward = state.getDirection() == Direction.FORWARD;
        int lastStep = forward ? getAnimationSequence().size() - 1 : 0;

        TimeSpan parentSpan = getTimeSpanForState(new DefaultAnimationState(lastStep, state.getDirection()));
        TimeSpan limitedParentSpan = getLimitWindow().getIntersection(parentSpan);
        TimeSpan startSpan;
        long end, start;
        if (limitedParentSpan != null)
        {
        	if (limitedParentSpan.contains(state.getActiveTimeSpan()))
        	{
        		startSpan = state.getActiveTimeSpan();
        	}
        	else
        	{
        		startSpan = forward ? TimeSpan.get(limitedParentSpan.getStart(), myActiveWindowDuration)
        				: TimeSpan.get(myActiveWindowDuration, limitedParentSpan.getEnd());
        	}
        	end = limitedParentSpan.getEnd();
        	start = limitedParentSpan.getStart();
        }
        else
        {
        	startSpan = state.getActiveTimeSpan();
        	end = getLimitWindow().getEnd();
        	start = getLimitWindow().getStart();
        }
        long gap = forward ? end - startSpan.getEnd() : startSpan.getStart() - start;
        long stepCount = gap / Milliseconds.get(getAdvanceDuration()).longValue();
        Duration adjustment = getAdvanceDuration().multiply(stepCount);
        TimeSpan lastSpan = forward ? startSpan.plus(adjustment) : startSpan.minus(adjustment);

        return new DefaultContinuousAnimationState(lastStep, lastSpan, state.getDirection());
    }

    @Override
    public DefaultContinuousAnimationState getInitialState()
    {
        DefaultAnimationState initialState = super.getInitialState();
        return createStateFromParentState(initialState, true);
    }

    @Override
    public TimeSpan getLimitWindow()
    {
        return myLimitWindow;
    }

    @Override
    public TimeSpan getTimeSpanForState(AnimationState state)
    {
        if (state instanceof ContinuousAnimationState)
        {
            return ((ContinuousAnimationState)state).getActiveTimeSpan();
        }
        return super.getTimeSpanForState(state);
    }

    @Override
    public int hashCode()
    {
        return 31 * super.hashCode() + Objects.hash(myActiveWindowDuration, myAdvanceDuration, myLimitWindow);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(128);
        builder.append("DefaultContinuousAnimationPlan :\n    Active window Duration : ").append(myActiveWindowDuration);
        builder.append("\n    Advance duration : ").append(myAdvanceDuration);
        builder.append("\n    Limit window : ").append(myLimitWindow);
        builder.append("\n    Sequence : ").append(getAnimationSequence());
        return builder.toString();
    }

    /**
     * Get the next state from the parent and create a continuous state from it.
     *
     * @param state The state to start from.
     * @param beginning Indicates the active time span of the result should be
     *            at the beginning of the parent time span.
     * @return The next state.
     */
    private DefaultContinuousAnimationState createStateFromParentState(DefaultAnimationState state, boolean beginning)
    {
        if (state == null)
        {
            return null;
        }

        TimeSpan ts = getTimeSpanForState(state);
        TimeInstant nextStart;
        TimeInstant nextEnd;
        if (beginning)
        {
            nextStart = ts.getStartInstant();
            if (!getLimitWindow().isUnboundedStart() && nextStart.compareTo(getLimitWindow().getStartInstant()) < 0)
            {
                nextStart = getLimitWindow().getStartInstant();
            }
            nextEnd = nextStart.plus(getActiveWindowDuration());
            if (!getLimitWindow().isUnboundedEnd() && nextEnd.compareTo(getLimitWindow().getEndInstant()) > 0)
            {
                nextEnd = getLimitWindow().getEndInstant();
            }
        }
        else
        {
            nextEnd = ts.getEndInstant();
            if (!getLimitWindow().isUnboundedEnd() && nextEnd.compareTo(getLimitWindow().getEndInstant()) > 0)
            {
                nextEnd = getLimitWindow().getEndInstant();
            }

            /* Because of different month lengths, we can end up with the case
             * that nextStart.plus(getActiveWindowDuration).equals(nextEnd) is
             * not true, so this logic is here in an attempt to make that true. */
            nextStart = nextEnd.minus(getActiveWindowDuration());
            TimeInstant possibleEnd = nextStart.plus(myActiveWindowDuration);
            if (possibleEnd.isAfter(ts.getEndInstant()))
            {
                nextStart = ts.getEndInstant().minus(possibleEnd.minus(nextStart));
            }
            else
            {
                nextEnd = possibleEnd;
            }

            if (!getLimitWindow().isUnboundedStart() && nextStart.compareTo(getLimitWindow().getStartInstant()) < 0)
            {
                nextStart = getLimitWindow().getStartInstant();
            }
        }

        return new DefaultContinuousAnimationState(state.getStepNumber(), TimeSpan.get(nextStart, nextEnd), state.getDirection());
    }

    /**
     * Get the parent span for the given state, limited by the limit window.
     *
     * @param state The state.
     * @return The limited time span.
     * @throws IllegalArgumentException If the parent state of the given state
     *             does not contain the limit window.
     */
    private TimeSpan getLimitedParentSpan(DefaultContinuousAnimationState state) throws IllegalArgumentException
    {
        // Get the time span from the parent sequence.
        DefaultAnimationState parentState = new DefaultAnimationState(state.getStepNumber(), state.getDirection());
        TimeSpan parentSpan = getTimeSpanForState(parentState);
        if (!parentSpan.contains(state.getActiveTimeSpan()))
        {
            throw new IllegalArgumentException("Parent span [" + parentSpan + "] does not contain the state [" + state + "]");
        }

        TimeSpan limitedParentSpan = getLimitWindow().getIntersection(parentSpan);
        if (limitedParentSpan == null)
        {
            throw new IllegalArgumentException(
                    "Parent span [" + parentSpan + "] does not contain the limit window [" + getLimitWindow() + "]");
        }
        return limitedParentSpan;
    }
}
