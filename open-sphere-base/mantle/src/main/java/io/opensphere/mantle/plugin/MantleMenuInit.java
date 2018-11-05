package io.opensphere.mantle.plugin;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.mantle.data.geom.style.dialog.VisualizationStyleControlDialog;
import io.opensphere.mantle.iconproject.view.IconProjDialog;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.TextViewDialog;

/**
 * Initializer for Mantle menu items.
 */
class MantleMenuInit
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Construct the menu initializer.
     *
     * @param toolbox The toolbox.
     */
    public MantleMenuInit(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Creates and installs a data element cache summary menu item.
     */
    public void createAndInstallDataElementCacheSummaryMenuItem()
    {
        assert EventQueue.isDispatchThread();
        JMenuItem deCacheSummaryMI = new JMenuItem("Data Element Cache Summary");
        deCacheSummaryMI.addActionListener(e ->
        {
            String cacheSummary = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().toString();
            TextViewDialog dvd = new TextViewDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                    "Data Element Cache Summary", cacheSummary, false, myToolbox.getPreferencesRegistry());
            dvd.setLocationRelativeTo(myToolbox.getUIRegistry().getMainFrameProvider().get());
            dvd.setVisible(true);
        });
        myToolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.DEBUG_MENU)
                .add(deCacheSummaryMI);
    }

    /**
     * Creates the and install dynamic icon registry print debug menu item.
     */
    public void createAndInstallDynamicEnumDebugPrintMenuItem()
    {
        assert EventQueue.isDispatchThread();
        JMenuItem iconManagerMI = new JMenuItem("Dynamic Enum Registry Contents");
        iconManagerMI.addActionListener(e ->
        {
            TextViewDialog dvd = new TextViewDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                    "Dynamic Enumeration Registry Summary",
                    MantleToolboxUtils.getMantleToolbox(myToolbox).getDynamicEnumerationRegistry().toString(), false,
                    myToolbox.getPreferencesRegistry());
            dvd.setLocationRelativeTo(myToolbox.getUIRegistry().getMainFrameProvider().get());
            dvd.setVisible(true);
        });
        myToolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.DEBUG_MENU)
                .add(iconManagerMI);
    }

    /**
     * Creates the and install icon manager menu item.
     */
    public void createAndInstallIconManagerMenuItem()
    {
        assert EventQueue.isDispatchThread();
        JMenuItem iconManagerMI = new JMenuItem("Icon Manager");
        iconManagerMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
        iconManagerMI.addActionListener(e ->
        {
            Quantify.collectMetric("mist3d.menu-bar.tools.icon-manager");
            IconProjDialog internIconManager = new IconProjDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                    myToolbox, false, true, null);
            internIconManager.setVisible(true);
        });
        myToolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.TOOLS_MENU)
                .add(iconManagerMI);
    }

    /**
     * Creates the and install vis style control dialog.
     */
    public void createAndInstallVisStyleControlDialog()
    {
        assert EventQueue.isDispatchThread();
        final VisualizationStyleControlDialog visualizationStyleControlDialog = new VisualizationStyleControlDialog(
                myToolbox.getUIRegistry().getMainFrameProvider().get(), myToolbox);
        JMenuItem visStyleControlMI = new JMenuItem(VisualizationStyleControlDialog.TITLE);
        visStyleControlMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
        visStyleControlMI.addActionListener(e ->
        {
            visualizationStyleControlDialog.setLocationRelativeTo(myToolbox.getUIRegistry().getMainFrameProvider().get());
            visualizationStyleControlDialog.setVisible(true);
        });
        myToolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.TOOLS_MENU)
                .add(visStyleControlMI);
    }
}
