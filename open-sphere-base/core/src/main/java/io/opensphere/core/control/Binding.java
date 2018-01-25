package io.opensphere.core.control;

/** Interface for action bindings. */
@FunctionalInterface
public interface Binding
{
    /**
     * Accessor for the listener.
     *
     * @return The listener.
     */
    BoundEventListener getListener();
}
