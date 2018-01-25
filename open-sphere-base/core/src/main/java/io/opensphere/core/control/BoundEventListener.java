package io.opensphere.core.control;

/**
 * A listener for a control event.
 */
public interface BoundEventListener
{
    /**
     * The listener's category.
     *
     * @return The category.
     */
    String getCategory();

    /**
     * The description for the listener.
     *
     * @return The description.
     */
    String getDescription();

    /**
     * Determine whether I am a viable target of mouse events.
     *
     * @return A priority number greater than or equal to 0 indicating a
     *         relative importance. Higher priorities will have precedence in
     *         receiving the event.
     */
    int getTargetPriority();

    /**
     * The title for the listener.
     *
     * @return The title.
     */
    String getTitle();

    /**
     * This indicates if the listener can be reassigned using the UI.
     *
     * @return <code>true</code> if the listener can be reassigned.
     */
    boolean isReassignable();

    /**
     * Determine whether I am a viable target for events.
     *
     * @return True when I am allowed and able to use the event.
     */
    boolean isTargeted();

    /**
     * Determine whether I need to be targeted in order to receive events.
     *
     * @return True when I must be targeted to receive events.
     */
    boolean mustBeTargeted();
}
