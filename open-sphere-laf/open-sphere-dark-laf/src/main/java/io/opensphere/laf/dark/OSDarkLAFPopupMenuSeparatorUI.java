package io.opensphere.laf.dark;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalPopupMenuSeparatorUI;

public class OSDarkLAFPopupMenuSeparatorUI extends MetalPopupMenuSeparatorUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFPopupMenuSeparatorUI();
    }

    @Override
    public Dimension getPreferredSize(JComponent pComponent)
    {
        return new Dimension(0, 2);
    }

    @Override
    public void paint(Graphics graph, JComponent jComp)
    {
        final Dimension s = jComp.getSize();
        graph.setColor(OSDarkLAFUtils.getShadowColor());
        graph.drawLine(1, 0, s.width - 1, 0);
        graph.setColor(OSDarkLAFUtils.getActiveColor());
        graph.drawLine(1, 1, s.width - 1, 1);
    }
}
