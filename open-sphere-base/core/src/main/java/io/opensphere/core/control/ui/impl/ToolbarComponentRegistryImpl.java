package io.opensphere.core.control.ui.impl;

import java.awt.Insets;

import javax.swing.JComponent;

import io.opensphere.core.control.ui.ToolbarComponentRegistry;
import io.opensphere.core.control.ui.ToolbarManager;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * The Class ToolbarActionRegistryImpl. This is the registry used by plugins to
 * add components to one of the 2 system toolbars.
 */
public class ToolbarComponentRegistryImpl implements ToolbarComponentRegistry
{
    /** The North toolbar manager. */
    private final ToolbarManager myToolbarManager;

    /**
     * Instantiates a new toolbar component registry impl.
     *
     * @param preferencesRegistry The preferences registry.
     */
    public ToolbarComponentRegistryImpl(PreferencesRegistry preferencesRegistry)
    {
        myToolbarManager = new ToolbarManager(preferencesRegistry);
    }

    @Override
    public void deregisterToolbarComponent(final ToolbarManager.ToolbarLocation loc, final String componentName)
    {
        EventQueueUtilities.runOnEDT(() -> myToolbarManager.removeToolbarComponent(loc, componentName));
    }

    @Override
    public ToolbarManager getToolbarManager()
    {
        return myToolbarManager;
    }

    @Override
    public void registerToolbarComponent(final ToolbarManager.ToolbarLocation loc, final String componentName,
            final JComponent comp, final int relativeLoc, final SeparatorLocation separatorLocation)
    {
        registerToolbarComponent(loc, componentName, comp, relativeLoc, separatorLocation, (Insets)null);
    }

    @Override
    public void registerToolbarComponent(final ToolbarManager.ToolbarLocation loc, final String componentName,
            final JComponent comp, final int relativeLoc, final SeparatorLocation separatorLocation, final Insets insets)
    {
        EventQueueUtilities.runOnEDT(
                () -> myToolbarManager.addToolbarComponent(loc, componentName, comp, relativeLoc, separatorLocation, insets));
    }
}
