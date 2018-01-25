package io.opensphere.core.control.ui;

import java.util.function.Supplier;

import javax.swing.JFrame;

import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.iconlegend.IconLegendRegistry;
import io.opensphere.core.options.OptionsRegistry;
import io.opensphere.core.timeline.TimelineRegistry;

/**
 * Registry for user interface components. Plug-ins may use this to register
 * their UIs so that the UIs will be made available to the users.
 */
public interface UIRegistry
{
    /**
     * Access the frame registry.
     *
     * @return the frame registry
     */
    InternalComponentRegistry getComponentRegistry();

    /**
     * Access the context menu and action manager.
     *
     * @return the context menu and action manager.
     */
    ContextActionManager getContextActionManager();

    /**
     * Gets the icon legend registry.
     *
     * @return the icon legend registry
     */
    IconLegendRegistry getIconLegendRegistry();

    /**
     * Get a provider for the mainFrame for the application.
     *
     * @return the mainFrame
     */
    Supplier<? extends JFrame> getMainFrameProvider();

    /**
     * Access the menu bar registry.
     *
     * @return The menu bar registry.
     */
    MenuBarRegistry getMenuBarRegistry();

    /**
     * Gets the {@link OptionsRegistry}.
     *
     * @return the {@link OptionsRegistry}
     */
    OptionsRegistry getOptionsRegistry();

    /**
     * Get the region selection manager.
     *
     * @return the region selection manager.
     */
    RegionSelectionManager getRegionSelectionManager();

    /**
     * Access the component registry.
     *
     * @return the component registry
     */
    SharedComponentRegistry getSharedComponentRegistry();

    /**
     * Gets the timeline registry.
     *
     * @return the timeline registry
     */
    TimelineRegistry getTimelineRegistry();

    /**
     * Gets the toolbar action registry.
     *
     * @return the toolbar action registry
     */
    ToolbarComponentRegistry getToolbarComponentRegistry();

    /**
     * Register the given manager as the region selection manager. There is only
     * ever one of these at a time.
     *
     * @param manager The manager which will manager region selection done by
     *            the user.
     */
    void registerAsRegionSelectionManager(RegionSelectionManager manager);

    /**
     * Set the size of the main application pane. This will be the size of the
     * main frame minus the size of the title bar, menu bar, and any other
     * extras.
     *
     * @param width The width of the pane.
     * @param height The height of the pane.
     */
    void setMainPaneSize(int width, int height);
}
