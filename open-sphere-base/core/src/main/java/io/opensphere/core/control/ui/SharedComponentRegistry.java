package io.opensphere.core.control.ui;

import java.awt.Component;

/**
 * Registry for AWT or swing components in the application. This allows a plugin
 * to share GUI components potentially modifying or sharing an existing
 * component from another plugin. It is recommended that the component name be
 * prefaced with an identifier for the owner. For example, the layer manager
 * name might be "WMS-Plugin.LayerManager".
 */
public interface SharedComponentRegistry
{
    /**
     * Add listener.
     *
     * @param listener The listener to add.
     */
    void addListener(SharedComponentListener listener);

    /**
     * Deregister a shared component.
     *
     * @param compName name of the component to deregister.
     */
    void deregisterComponent(String compName);

    /**
     * Get the Component with the given name.
     *
     * @param compName The name of the component.
     * @return The component with the given name, or <code>null</code> if it was
     *         not found.
     */
    Component getComponent(String compName);

    /**
     * Register a shared component.
     *
     * @param compName component name.
     * @param component component to register.
     */
    void registerComponent(String compName, Component component);

    /**
     * Remove listener.
     *
     * @param listener The listener to remove.
     */
    void removeListener(SharedComponentListener listener);
}
