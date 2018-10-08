package io.opensphere.core.animation;

import java.util.Date;
import java.util.List;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;

/**
 * A plan that describes how an animation will be performed.
 */
public interface AnimationPlan
{
    /**
     * Calculate the distance in terms of animation sequence elements from the
     * element that contains the "from" time to the element that contains the
     * "to" time. If the "to" time can never be reached,
     * {@link Integer#MAX_VALUE} is returned.
     *
     * @param from The animation state from which the distance is calculated.
     * @param to The animation state to which the distance is calculated.
     * @return The number of steps between the times.
     */
    int calculateDistance(AnimationState from, AnimationState to);

    /**
     * Determine the next animation state from a given state.
     *
     * @param state The given state.
     * @return The next state.
     */
    AnimationState determineNextState(AnimationState state);

    /**
     * Determine the previous animation state from a given state.
     *
     * @param state The given state.
     * @return The previous state.
     */
    AnimationState determinePreviousState(AnimationState state);

    /**
     * Find the nearest state in the plan for the given time.
     *
     * @param time The time of interest.
     * @param direction The direction of the animation.
     * @return The animation state or {@code null} if one could not be found.
     */
    @Nullable
    AnimationState findState(Date time, Direction direction);

    /**
     * Find the nearest state in the plan for the given time span.
     *
     * @param span The time span of interest.
     * @param direction The direction of the animation.
     * @return The animation state or {@code null} if one could not be found.
     */
    AnimationState findState(TimeSpan span, Direction direction);

    /**
     * Get the amount of time between the start times of consecutive animation
     * steps. In other words, when the animation advances a step, this is how
     * much the time window moves.
     *
     * @return The advance duration.
     */
    Duration getAdvanceDuration();

    /**
     * Get the animation sequence, which defines what time spans are included in
     * the animation and their order, which may not be chronological.
     *
     * @return The time spans.
     */
    List<? extends TimeSpan> getAnimationSequence();

    /**
     * Get a subset of the spans which follow the given state. If the number of
     * steps requested is not available, the steps returned will be all of those
     * which were available.
     *
     * @param state The start state.
     * @param number The number of plan steps following the given steps which
     *            are desired.
     * @param direction The direction of the animation at the start time.
     * @return A set of steps in the plan.
     */
    List<? extends TimeSpan> getAnimationSequence(AnimationState state, int number, Direction direction);

    /**
     * Gets the step behavior once the end has been reached.
     *
     * @return The end behavior.
     */
    EndBehavior getEndBehavior();

    /**
     * Get the final possible state of the plan without regard to the starting
     * state, ignoring any infinite behavior (e.g., looping).
     *
     * @return The final state.
     */
    AnimationState getFinalState();

    /**
     * Get the final state of the plan based on the given starting state,
     * ignoring any infinite behavior (e.g., looping).
     *
     * @param state The initial state.
     * @return The final state.
     */
    AnimationState getFinalState(AnimationState state);

    /**
     * Get the initial state of the plan.
     *
     * @return The initial state.
     */
    AnimationState getInitialState();

    /**
     * Get the limits on the animation within the current animation sequence.
     * <p>
     * The animation runs across the intersection of this time span and the
     * animation sequence.
     *
     * @return The limit window.
     */
    TimeSpan getLimitWindow();

    /**
     * Get the set of all time spans covered by this plan.
     *
     * @return The time coverage.
     */
    Set<? extends TimeSpan> getTimeCoverage();

    /**
     * Get the time span for an animation state.
     *
     * @param state The animation state.
     * @return The time span.
     */
    TimeSpan getTimeSpanForState(AnimationState state);

    /**
     * Checks if is empty plan.
     *
     * @return true, if is empty plan
     */
    boolean isEmptyPlan();

    /**
     * Waits during ThreePhaseChangeSupport processing if true.
     *
     * @return true, if successful
     */
    boolean isUsingProcessingTimeout();

    /**
     * The behavior that describes how the animation determines the next step
     * once the end has been reached.
     */
    enum EndBehavior
    {
        /** The animation reverses direction at each end. */
        BOUNCE,

        /** The animation stops when it reaches the end. */
        STOP,

        /** The animation starts from the other end when it reaches the end. */
        WRAP,
    }
}
