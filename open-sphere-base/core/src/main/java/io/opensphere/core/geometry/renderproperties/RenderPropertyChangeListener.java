package io.opensphere.core.geometry.renderproperties;

/** Interface for listeners for changes to render properties. */
@FunctionalInterface
public interface RenderPropertyChangeListener
{
    /**
     * Callback to listeners when one or more properties have changed.
     *
     * @param evt The properties changed event.
     */
    void propertyChanged(RenderPropertyChangedEvent evt);
}
