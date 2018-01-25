package io.opensphere.core.util.swing;

/**
 * The listener interface for receiving ghostDrop events. The class that is
 * interested in processing a ghostDrop event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addGhostDropListener</code> method. When the ghostDrop
 * event occurs, that object's appropriate method is invoked.
 *
 * @see GhostDropEvent
 */
@FunctionalInterface
public interface GhostDropListener
{
    /**
     * Ghost dropped.
     *
     * @param e the e
     */
    void ghostDropped(GhostDropEvent e);
}
