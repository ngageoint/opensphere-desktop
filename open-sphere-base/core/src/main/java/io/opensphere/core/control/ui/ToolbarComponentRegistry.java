package io.opensphere.core.control.ui;

import java.awt.Insets;

import javax.swing.JComponent;

import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;

/**
 * The ToolbarActionRegistry interface allows plugins to register components to
 * be used in one of the 2 system toolbars.
 */
public interface ToolbarComponentRegistry
{
    /**
     * Deregister toolbar component.
     *
     * @param location the toolbar location, either NORTH or SOUTH
     * @param componentName the component name
     */
    void deregisterToolbarComponent(ToolbarManager.ToolbarLocation location, String componentName);

    /**
     * Gets the toolbar manager.
     *
     * @return the toolbar manager
     */
    ToolbarManager getToolbarManager();

    /**
     * Register toolbar component.
     *
     * @param location the toolbar location, either NORTH or SOUTH
     * @param componentName the component name
     * @param comp the comp
     * @param relativeLoc the relative loc
     * @param separatorLoc the separator loc
     */
    void registerToolbarComponent(ToolbarManager.ToolbarLocation location, String componentName, JComponent comp, int relativeLoc,
            SeparatorLocation separatorLoc);

    /**
     * Register toolbar component.
     *
     * @param location the toolbar location, either NORTH or SOUTH
     * @param componentName the component name
     * @param comp the comp
     * @param relativeLoc the relative loc
     * @param separatorLoc the separator loc
     * @param insets Optional insets to be used when laying out the component in
     *            the toolbar.
     */
    void registerToolbarComponent(ToolbarManager.ToolbarLocation location, String componentName, JComponent comp, int relativeLoc,
            SeparatorLocation separatorLoc, Insets insets);
}
