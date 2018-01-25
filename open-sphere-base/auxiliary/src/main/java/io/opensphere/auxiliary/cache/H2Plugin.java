package io.opensphere.auxiliary.cache;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.h2.tools.Console;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * A plug-in that adds the H2 console to the menu.
 */
public class H2Plugin extends PluginAdapter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(H2Plugin.class);

    @Override
    public void initialize(PluginLoaderData plugindata, final Toolbox toolbox)
    {
        EventQueueUtilities.invokeLater(() -> addMenu(toolbox));
    }

    /**
     * Adds menu(s) to the menu bar.
     *
     * @param toolbox The toolbox.
     */
    private void addMenu(Toolbox toolbox)
    {
        JMenu subMenu = new JMenu("Cache");
        addCacheMenuItems(subMenu, toolbox);
        JMenu toolsMenu = toolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR,
                MenuBarRegistry.DEBUG_MENU);
        toolsMenu.add(subMenu);
    }

    /**
     * Add the cache-related menu items.
     *
     * @param menu The menu to which to add.
     * @param toolbox The toolbox.
     */
    protected void addCacheMenuItems(JMenu menu, final Toolbox toolbox)
    {
        final JMenuItem openH2Console = new JMenuItem("Open H2 Console...");
        openH2Console.addActionListener(new ActionListener()
        {
            private Console myConsole;

            @Override
            public synchronized void actionPerformed(ActionEvent event)
            {
                if (myConsole == null)
                {
                    try
                    {
                        myConsole = new Console();
                        myConsole.runTool();
                        openH2Console.setEnabled(false);
                    }
                    catch (SQLException e)
                    {
                        LOGGER.error("Failed to open H2 Console: " + e, e);
                    }
                }
            }
        });
        menu.add(openH2Console);

        JMenuItem clearCacheMenuItem = new JMenuItem("Clear Cache");
        clearCacheMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                toolbox.getDataRegistry().removeModels((DataModelCategory)null, false);
            }
        });
        menu.add(clearCacheMenuItem);
    }
}
