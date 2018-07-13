package io.opensphere.core.appl;

import javax.swing.JMenuItem;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.ui.MenuBarRegistry;
<<<<<<< HEAD
import io.opensphere.core.quantify.QuantifyToolboxUtils;
=======
import io.opensphere.core.quantify.Quantify;
>>>>>>> snapshot_5.2.5
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
<<<<<<< HEAD
                QuantifyToolboxUtils.collectMetric(toolbox, "mist3d.menu-bar.help.reset-all-dialogs");
=======
                Quantify.collectMetric("mist3d.help.reset-all-dont-show-dialogs");
>>>>>>> snapshot_5.2.5
                DontShowDialog.resetPreferences(toolbox.getPreferencesRegistry());
            });
            toolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.HELP_MENU)
                    .add(menuButton);
        });
    }
}
