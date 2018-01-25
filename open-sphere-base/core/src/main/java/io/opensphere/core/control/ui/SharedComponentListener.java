package io.opensphere.core.control.ui;

/** Interface for shared component listeners. */
public interface SharedComponentListener
{
    /**
     * A component has been added.
     *
     * @param name The name of the added component.
     */
    void componentAdded(String name);

    /**
     * A component has been removed.
     *
     * @param name The name of the removed component.
     */
    void componentRemoved(String name);
}
