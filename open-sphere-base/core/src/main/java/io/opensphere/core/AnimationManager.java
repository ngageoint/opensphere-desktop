package io.opensphere.core;

import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationPlanModificationException;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.PhasedChangeArbitrator;

/**
 * Interface for the animation management facility. This manager owns the
 * current animation plan and decides what time spans are currently active based
 * on the plan.
 */
public interface AnimationManager
{
    /**
     * The preference key for the amount of time to wait for an animation step
     * to render.
     */
    String TIMEOUT_PREFERENCE_KEY = "StepTimeout";

    /**
     * Abandons any current animation plan.
     */
    void abandonPlan();

    /**
     * Add a animation change listener.
     *
     * @param listener The listener.
     */
    void addAnimationChangeListener(AnimationChangeListener listener);

    /**
     * Add an arbitrator which can control whether animation plan changes
     * require phased commits.
     *
     * @param arbitrator The arbitrator.
     */
    void addPhasedChangeArbitrator(PhasedChangeArbitrator arbitrator);

    /**
     * Get the current animation state.
     *
     * @return The current animation state.
     */
    AnimationState getAnimationState();

    /**
     * Gets the reciprocal of the change rate for the animation.
     *
     * @return The reciprocal of the change rate.
     */
    Duration getChangeRate();

    /**
     * Gets the current plan.
     *
     * @return The current plan, or {@code null} if none.
     */
    AnimationPlan getCurrentPlan();

    /**
     * Checks if there is currently a plan established.
     *
     * @return {@code true} if a plan is established.
     */
    boolean isPlanEstablished();

    /**
     * Checks if an animation is playing.
     *
     * @return {@code true} if a plan is playing.
     */
    boolean isPlaying();

    /**
     * Jumps to the specified step, if it is in the plan, otherwise does
     * nothing.
     *
     * @param plan The plan that expected to be the current plan.
     * @param step The step to jump to.
     * @param waitForListeners When true, wait for listeners to be ready before
     *            changing states.
     * @return {@code true} if jumped, {@code false} if step was not in plan.
     * @throws AnimationPlanModificationException If the expected plan does not
     *             match the current plan.
     */
    boolean jumpToStep(AnimationPlan plan, TimeSpan step, boolean waitForListeners) throws AnimationPlanModificationException;

    /**
     * Instructs the animation manager to pause playing the animation.
     *
     * @param plan The plan that expected to be the current plan.
     * @throws AnimationPlanModificationException If the expected plan does not
     *             match the current plan.
     */
    void pause(AnimationPlan plan) throws AnimationPlanModificationException;

    /**
     * Instructs the animation manager to begin playing the animation.
     *
     * @param plan The plan that expected to be the current plan.
     * @param direction the playback direction, forward or rewind
     * @throws AnimationPlanModificationException If the expected plan does not
     *             match the current plan.
     */
    void play(AnimationPlan plan, AnimationState.Direction direction) throws AnimationPlanModificationException;

    /**
     * Remove a animation change listener.
     *
     * @param listener The listener.
     */
    void removeAnimationChangeListener(AnimationChangeListener listener);

    /**
     * Remove an arbitrator which can control whether animation plan changes
     * require phased commits.
     *
     * @param arbitrator The arbitrator.
     */
    void removePhasedChangeArbitrator(PhasedChangeArbitrator arbitrator);

    /**
     * Sets the rate at which the animation will change steps.
     *
     * @param plan The plan that expected to be the current plan.
     * @param changeRate The change rate.
     * @throws AnimationPlanModificationException If the expected plan does not
     *             match the current plan.
     */
    void setChangeRate(AnimationPlan plan, Duration changeRate) throws AnimationPlanModificationException;

    /**
     * Establish a new (or alter an existing) animation plan. Caller provides a
     * plan and a key to be used to control execution of and changes to the
     * plan.
     *
     * @param plan the {@link AnimationPlan}
     */
    void setPlan(AnimationPlan plan);

    /**
     * Establish a new (or alter an existing) animation plan. Caller provides a
     * plan and a key to be used to control execution of and changes to the
     * plan.
     *
     * @param plan the {@link AnimationPlan}
     * @param initState the init state, if null uses animation plan's default
     *            state
     */
    void setPlan(AnimationPlan plan, AnimationState initState);

    /**
     * Establish a new (or alter an existing) animation plan. Caller provides a
     * plan and a key to be used to control execution of and changes to the
     * plan.
     *
     * @param plan the {@link AnimationPlan}
     * @param initState the init state, if null uses animation plan's default
     *            state
     * @param animationDirection When not null, play the animation in the
     *            direction indicated.
     * @param changeRate If provided, set the change rate immediately after
     *            establishing the plan.
     */
    void setPlan(AnimationPlan plan, AnimationState initState, Direction animationDirection, Duration changeRate);

    /**
     * Move to the next step backward in the plan.
     *
     * @param plan The plan that expected to be the current plan.
     * @param waitForListeners When true, wait for listeners to be ready before
     *            changing states.
     * @throws AnimationPlanModificationException If the expected plan does not
     *             match the current plan.
     */
    void stepBackward(AnimationPlan plan, boolean waitForListeners) throws AnimationPlanModificationException;

    /**
     * Move to the first step in the plan.
     *
     * @param plan The plan that expected to be the current plan.
     * @param waitForListeners When true, wait for listeners to be ready before
     *            changing states.
     * @throws AnimationPlanModificationException If the expected plan does not
     *             match the current plan.
     */
    void stepFirst(AnimationPlan plan, boolean waitForListeners) throws AnimationPlanModificationException;

    /**
     * Move to the next step forward in the plan.
     *
     * @param plan The plan that expected to be the current plan.
     * @param waitForListeners When true, wait for listeners to be ready before
     *            changing states.
     * @throws AnimationPlanModificationException If the expected plan does not
     *             match the current plan.
     */
    void stepForward(AnimationPlan plan, boolean waitForListeners) throws AnimationPlanModificationException;

    /**
     * Move to the last step forward in the plan.
     *
     * @param plan The plan that expected to be the current plan.
     * @param waitForListeners When true, wait for listeners to be ready before
     *            changing states.
     * @throws AnimationPlanModificationException If the expected plan does not
     *             match the current plan.
     */
    void stepLast(AnimationPlan plan, boolean waitForListeners) throws AnimationPlanModificationException;

    /**
     * Moves to the next step in the plan, which may be forward or backward in
     * time depending on the animation state.
     *
     * @param plan The plan that expected to be the current plan.
     * @param waitForListeners When true, wait for listeners to be ready before
     *            changing states.
     * @throws AnimationPlanModificationException If the expected plan does not
     *             match the current plan.
     */
    void stepNext(AnimationPlan plan, boolean waitForListeners) throws AnimationPlanModificationException;

    /**
     * Moves to the previous step in the plan, which may be forward or backward
     * in time depending on the animation state.
     *
     * @param plan The plan that expected to be the current plan.
     * @param waitForListeners When true, wait for listeners to be ready before
     *            changing states.
     * @throws AnimationPlanModificationException If the expected plan does not
     *             match the current plan.
     */
    void stepPrevious(AnimationPlan plan, boolean waitForListeners) throws AnimationPlanModificationException;
}
