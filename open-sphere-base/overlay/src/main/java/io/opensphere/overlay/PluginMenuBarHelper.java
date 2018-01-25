package io.opensphere.overlay;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.overlay.arc.ArcTransformer;

/** Helper class for registration of items on the menu bar. */
public final class PluginMenuBarHelper
{
    /**
     * Add menu bar items that are related to Overlay.
     *
     * @param toolbox References to facilities that may be used by the plug-in
     *            to interact with the rest of the system.
     * @param mgrsTransformer The MGRS transformer.
     * @param arcTransformer The arc length transformer.
     * @param dotTransformer The random dot transformer.
     */
    static void initializeMenuBar(final Toolbox toolbox, final MGRSTransformer mgrsTransformer,
            final ArcTransformer arcTransformer, final RandomDotTransformer dotTransformer)
    {
        JMenu viewMenu = toolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR,
                MenuBarRegistry.VIEW_MENU);

        if (mgrsTransformer != null)
        {
            final JCheckBoxMenuItem mgrsMenuItem = new JCheckBoxMenuItem("MGRS Grid");
            mgrsMenuItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent event)
                {
                    if (mgrsMenuItem.isSelected())
                    {
                        mgrsTransformer.setEnabled(true);
                        mgrsTransformer.display(toolbox.getMapManager().getStandardViewer(),
                                toolbox.getMapManager().getProjection());
                    }
                    else
                    {
                        mgrsTransformer.remove();
                        mgrsTransformer.setEnabled(false);
                    }
                }
            });

            JMenu overlaysMenu = toolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR,
                    MenuBarRegistry.VIEW_MENU, MenuBarRegistry.OVERLAYS_MENU);

            overlaysMenu.add(mgrsMenuItem);
            viewMenu.add(overlaysMenu);
        }

        if (dotTransformer != null)
        {
            JMenu dotsMenu = new JMenu("Geometry Testing");
            toolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.DEBUG_MENU)
                    .add(dotsMenu);

            JMenuItem publishMenuItem = new JMenuItem("Toggle Dots Published");
            publishMenuItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    dotTransformer.publishRandomDots();
                }
            });
            dotsMenu.add(publishMenuItem);

            JMenuItem publishSetMenuItem = new JMenuItem("Toggle Point Set Published");
            publishSetMenuItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    dotTransformer.publishRandomSet();
                }
            });
            dotsMenu.add(publishSetMenuItem);

            JMenuItem manageProperties = new JMenuItem("Manage Dot Properties");
            manageProperties.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    dotTransformer.manageDotProperties();
                }
            });
            dotsMenu.add(manageProperties);

            JMenuItem publishTileMenuItem = new JMenuItem("Toggle Tile Published");
            publishTileMenuItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    dotTransformer.publishTile();
                }
            });
            dotsMenu.add(publishTileMenuItem);
        }
    }

    /** Disallow instantiation. */
    private PluginMenuBarHelper()
    {
    }
}
