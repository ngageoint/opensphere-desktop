package io.opensphere.laf.dark;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalCheckBoxUI;
import javax.swing.plaf.metal.MetalIconFactory;

/**
 * CheckboxUI implementation for OpenSphere Dark Look and Feel.
 */
public class OSDarkLAFCheckBoxUI extends MetalCheckBoxUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFCheckBoxUI();
    }

    /**
     * A flag used to track the opaqueness of the original paint job.
     */
    protected boolean wasOriginalOpaque;

    @Override
    public synchronized void paint(Graphics graph, JComponent jComp)
    {
        if (wasOriginalOpaque)
        {
            final Dimension dim = jComp.getSize();
            final Object parent = jComp.getParent();

            if (parent != null)
            {
                if (parent.getClass() == CellRendererPane.class)
                {
                    graph.setColor(jComp.getBackground());
                    graph.fillRect(0, 0, dim.width, dim.height);
                }
                else if (parent instanceof JTable)
                {
                    graph.setColor(((JTable)parent).getSelectionBackground());
                    graph.fillRect(0, 0, dim.width, dim.height);
                }
                else if (parent instanceof JList)
                {
                    graph.setColor(((JList)parent).getSelectionBackground());
                    graph.fillRect(0, 0, dim.width, dim.height);
                }
            }
        }

        super.paint(graph, jComp);
    }

    @Override
    protected void paintFocus(Graphics graph, Rectangle rect, Dimension dim)
    {
        OSDarkLAFUtils.focusPaint(graph, 1, 1, dim.width - 2, dim.height - 2, 8, 8, OpenSphereDarkLookAndFeel.getFocusColor());
    }

    @Override
    public void installDefaults(AbstractButton bt)
    {
        super.installDefaults(bt);
        wasOriginalOpaque = bt.isOpaque();
        bt.setOpaque(false);
        icon = OSDarkLAFIconFactory.getCheckBoxIcon();
    }

    @Override
    public void uninstallDefaults(AbstractButton bt)
    {
        super.uninstallDefaults(bt);
        bt.setOpaque(wasOriginalOpaque);
        icon = MetalIconFactory.getCheckBoxIcon();
    }
}
