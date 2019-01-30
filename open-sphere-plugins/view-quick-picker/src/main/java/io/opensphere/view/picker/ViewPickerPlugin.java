package io.opensphere.view.picker;

import java.awt.EventQueue;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.hud.awt.HUDJInternalFrame;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.view.picker.controller.ViewPickerController;
import io.opensphere.view.picker.view.ViewPickerFrame;

/** A plugin implementation to show a small box for saved views. */
public class ViewPickerPlugin extends PluginAdapter
{
    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.PluginAdapter#initialize(io.opensphere.core.PluginLoaderData,
     *      io.opensphere.core.Toolbox)
     */
    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        super.initialize(plugindata, toolbox);

        ViewPickerController controller = new ViewPickerController(toolbox);

        EventQueueUtilities.runOnEDTAndWait(() -> createAndInstallFrame(toolbox, controller));
    }

    /**
     * Creates a new frame, and installs it to the UI registry. Also adds a menu
     * item to display or hide the frame.
     * 
     * @param toolbox the toolbox through which application state is accessed.
     * @param controller the controller managing the picker.
     */
    private void createAndInstallFrame(Toolbox toolbox, ViewPickerController controller)
    {
        assert EventQueue.isDispatchThread();

        final ViewPickerFrame frame = new ViewPickerFrame(controller);

        HUDJInternalFrame hudFrame = new HUDJInternalFrame(new HUDJInternalFrame.Builder().setInternalFrame(frame));
        toolbox.getUIRegistry().getComponentRegistry().addObjectsForSource(this, Collections.singleton(hudFrame));

        JMenuItem menuItem = new JMenuItem("Saved View Picker");
        menuItem.addActionListener(e ->
        {
            JFrame jFrame = toolbox.getUIRegistry().getMainFrameProvider().get();
            int width = (int)jFrame.getSize().getWidth();
            int height = (int)jFrame.getSize().getHeight();

            int frameHeight = frame.getHeight();
            frame.setLocation(width - 30, height / 2 - (int)(frameHeight / 1.5));

            frame.setVisible(!frame.isVisible());
        });

        toolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.TOOLS_MENU)
                .add(menuItem);
    }
}
