package io.opensphere.laf.dark;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSeparatorUI;

public class OSDarkLAFSeparatorUI extends BasicSeparatorUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFSeparatorUI();
    }

    @Override
    public void paint(Graphics graph, JComponent jComp)
    {
        final Dimension compSize = jComp.getSize();

        if (((JSeparator)jComp).getOrientation() == JSeparator.HORIZONTAL)
        {
            graph.setColor(OSDarkLAFUtils.getShadowColor());
            graph.drawLine(0, 0, compSize.width, 0);
            graph.setColor(OSDarkLAFUtils.getActiveColor());
            graph.drawLine(0, 1, compSize.width, 1);
        }
        else
        {
            // VERTICAL:
            graph.setColor(OSDarkLAFUtils.getShadowColor());
            graph.drawLine(0, 0, 0, compSize.height);
            graph.setColor(OSDarkLAFUtils.getActiveColor());
            graph.drawLine(1, 0, 1, compSize.height);
        }
    }
}
