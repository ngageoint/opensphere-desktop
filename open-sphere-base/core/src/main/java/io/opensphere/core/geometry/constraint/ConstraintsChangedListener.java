package io.opensphere.core.geometry.constraint;

/** Interface for listeners for changes to render constraints. */
@FunctionalInterface
public interface ConstraintsChangedListener
{
    /**
     * Callback to listeners when one or more constraints have changed.
     *
     * @param evt The constraints changed event.
     */
    void constraintsChanged(ConstraintsChangedEvent evt);
}
