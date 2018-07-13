package io.opensphere.core.appl;

import javax.swing.JMenuItem;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.input.DontShowDialog;

/**
 * Plugin that adds a reset menu button for the {@link DontShowDialog}.
 */
public class DontShowDialogPlugin extends PluginAdapter
{
    @Override
    public void initialize(PluginLoaderData plugindata, final Toolbox toolbox)
    {
        EventQueueUtilities.invokeLater(() ->
        {
            JMenuItem menuButton = new JMenuItem("Reset all \"Don't show this again\" dialogs");
            menuButton.addActionListener(e ->
            {
                QuantifyToolboxUtils.collectMetric(toolbox, "mist3d.menu-bar.help.reset-all-dialogs");
                DontShowDialog.resetPreferences(toolbox.getPreferencesRegistry());
            });
            toolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.HELP_MENU)
                    .add(menuButton);
        });
    }
}
