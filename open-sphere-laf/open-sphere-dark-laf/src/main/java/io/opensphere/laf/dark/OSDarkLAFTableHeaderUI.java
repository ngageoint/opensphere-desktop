package io.opensphere.laf.dark;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableHeaderUI;

public class OSDarkLAFTableHeaderUI extends BasicTableHeaderUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFTableHeaderUI();
    }

    public OSDarkLAFTableHeaderUI()
    {
        /* intentionally blank */
    }

    @Override
    public void paint(Graphics graph, JComponent jComp)
    {
        graph.translate(3, 0);
        super.paint(graph, jComp);
        graph.translate(-3, 0);

        if (!jComp.isOpaque())
        {
            return;
        }

        final Graphics2D graph2D = (Graphics2D)graph;
        final GradientPaint gradientPaint = new GradientPaint(0, 0, OSDarkLAFUtils.getActiveColor(), 0, jComp.getHeight(),
                OSDarkLAFUtils.getShadowColor());

        graph2D.setPaint(gradientPaint);
        graph2D.fillRect(0, 0, jComp.getWidth(), jComp.getHeight());
    }
}
