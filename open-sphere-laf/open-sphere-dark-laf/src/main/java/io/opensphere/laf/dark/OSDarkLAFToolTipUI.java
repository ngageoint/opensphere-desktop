package io.opensphere.laf.dark;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalToolTipUI;

public class OSDarkLAFToolTipUI extends MetalToolTipUI
{
    protected JToolTip aToolTip;

    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFToolTipUI(pComponent);
    }

    public OSDarkLAFToolTipUI(JComponent pComponent)
    {
        super();
        aToolTip = (JToolTip)pComponent;
        aToolTip.setOpaque(false);
    }

    @Override
    public void paint(Graphics graph, JComponent jComp)
    {
        int width = aToolTip.getWidth();
        int height = aToolTip.getHeight();
        final Border tipBorder = aToolTip.getBorder();
        if (null != tipBorder)
        {
            width -= tipBorder.getBorderInsets(aToolTip).right;
            height -= tipBorder.getBorderInsets(aToolTip).bottom;
        }
        graph.setColor(aToolTip.getBackground());
        graph.fillRect(0, 0, width, height);
        super.paint(graph, jComp);
    }
}
