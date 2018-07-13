package io.opensphere.subterrain.xraygoggles.ui;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.subterrain.xraygoggles.controller.XrayGogglesController;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Adds a menu item to the View menu so users can toggle the xray viewer on and
 * off.
 */
public class XrayGogglesMenuProvider
{
    /**
     * The current xray goggles controller.
     */
    private XrayGogglesController myCurrentController;

    /**
     * The xray goggles toggle menu item.
     */
    private JCheckBoxMenuItem myMenuItem;

    /**
     * The model used by the xray goggles components.
     */
    private final XrayGogglesModel myModel;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new menu provider.
     *
     * @param toolbox The system toolbox.
     * @param model The model used by the xray goggles components.
     */
    public XrayGogglesMenuProvider(Toolbox toolbox, XrayGogglesModel model)
    {
        myToolbox = toolbox;
        myModel = model;
        initializeMenu(toolbox.getUIRegistry());
    }

    /**
     * Removes the menu item and stops listening for any changes.
     */
    public void close()
    {
        if (myCurrentController != null)
        {
            myCurrentController.close();
            myCurrentController = null;
        }
    }

    /**
     * Either enables or disables the xray goggles based on the menu items
     * selection state.
     *
     * @param event The event.
     */
    private void actionPerformed(ActionEvent event)
    {
        Quantify.collectEnableDisableMetric("mist3d.menu-bar.view.underground", myMenuItem.isSelected());
        if (myMenuItem.isSelected())
        {
            myCurrentController = new XrayGogglesController(myToolbox.getMapManager(), myToolbox.getGeometryRegistry(),
                    myToolbox.getControlRegistry(), myModel);
        }
        else
        {
            myCurrentController.close();
            myCurrentController = null;
        }
    }

    /**
     * Initializes the menu item.
     *
     * @param uiRegistry Used to expose the menu to the user.
     */
    private void initializeMenu(UIRegistry uiRegistry)
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            JMenu viewMenu = uiRegistry.getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.VIEW_MENU);
            myMenuItem = new JCheckBoxMenuItem("Underground");
            myMenuItem.addActionListener(this::actionPerformed);
            viewMenu.add(myMenuItem);
        });
    }
}
