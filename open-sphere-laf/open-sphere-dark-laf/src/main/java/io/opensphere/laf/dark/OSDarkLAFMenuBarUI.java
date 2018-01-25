package io.opensphere.laf.dark;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuBarUI;

public class OSDarkLAFMenuBarUI extends BasicMenuBarUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFMenuBarUI();
    }

    @Override
    public void paint(Graphics graph, JComponent pComponent)
    {
        super.paint(graph, pComponent);
    }
}
