package io.opensphere.laf.dark;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuItemUI;

public class OSDarkLAFMenuItemUI extends BasicMenuItemUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFMenuItemUI();
    }

    @Override
    protected void paintBackground(Graphics graph, JMenuItem muItem, Color backgroundColor)
    {
        OSDarkLAFUtils.paintMenuBar(graph, muItem, backgroundColor);
    }

    @Override
    protected void installDefaults()
    {
        super.installDefaults();
        menuItem.setOpaque(false);
        menuItem.setBorderPainted(false);
        defaultTextIconGap = 3;
    }

    @Override
    protected void uninstallDefaults()
    {
        super.uninstallDefaults();
        menuItem.setOpaque(true);
    }
}
