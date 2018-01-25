package io.opensphere.core.control;

/**
 * The listener interface for receiving keyBindingChange events. The class that
 * is interested in processing a keyBindingChange event implements this
 * interface, and the object created with that class is registered with a
 * component using the component's <code>addKeyBindingChangeListener</code>
 * method. When the keyBindingChange event occurs, that object's appropriate
 * method is invoked.
 *
 * @see KeyBindingChangeEvent
 */
@FunctionalInterface
public interface KeyBindingChangeListener
{
    /**
     * Key binding changed.
     *
     * @param evt the evt
     */
    void keyBindingChanged(KeyBindingChangeEvent evt);
}
