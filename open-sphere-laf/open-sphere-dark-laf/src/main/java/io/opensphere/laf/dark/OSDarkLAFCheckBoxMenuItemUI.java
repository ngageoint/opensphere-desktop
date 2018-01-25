package io.opensphere.laf.dark;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;

/**
 * A pluggable look and feel interface for JMenuItem.
 */
public class OSDarkLAFCheckBoxMenuItemUI extends BasicCheckBoxMenuItemUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFCheckBoxMenuItemUI();
    }

    @Override
    protected void paintBackground(Graphics graph, JMenuItem menuItem, Color backgroundColor)
    {
        OSDarkLAFUtils.paintMenuBar(graph, menuItem, backgroundColor);
    }

    @Override
    protected void installDefaults()
    {
        super.installDefaults();
        menuItem.setBorderPainted(false);
        menuItem.setOpaque(false);
        defaultTextIconGap = 3;
    }

    @Override
    protected void uninstallDefaults()
    {
        super.uninstallDefaults();
        menuItem.setOpaque(true);
    }
}
