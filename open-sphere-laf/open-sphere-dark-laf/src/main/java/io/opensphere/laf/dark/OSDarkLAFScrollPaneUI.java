package io.opensphere.laf.dark;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollPaneUI;
import javax.swing.plaf.metal.MetalScrollBarUI;

import io.opensphere.laf.dark.border.OSDarkLAFGenBorder;

public class OSDarkLAFScrollPaneUI extends BasicScrollPaneUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFScrollPaneUI();
    }

    /**
     * A flag used to track the opaqueness of the original paint job.
     */
    protected boolean wasOriginalOpaque;

    @Override
    public void paint(Graphics graph, JComponent jComp)
    {
        final JScrollPane jSP = (JScrollPane)jComp;
        final Dimension dim = jSP.getSize();

        if (null != jSP.getViewportBorder())
        {
            Component comp = jComp.getParent();
            while (null != comp)
            {
                if (comp.toString().startsWith("javax.swing.plaf.basic.BasicComboPopup"))
                {
                    jSP.setViewportBorder(null);
                    break;
                }
                comp = comp.getParent();
            }

            comp = jSP.getViewport().getView();
            if (null != comp)
            {
                try
                {
                    final JComponent comp2 = (JComponent)comp;

                    final Border viewPortBorder = jSP.getViewportBorder();
                    if (null != viewPortBorder && viewPortBorder instanceof OSDarkLAFGenBorder)
                    {
                        final int x = viewPortBorder.getBorderInsets(jSP).left + viewPortBorder.getBorderInsets(jSP).right - 1;

                        graph.setColor(comp2.getBackground());
                        final Graphics2D graph2D = (Graphics2D)graph;
                        graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        graph.fillRoundRect(0, 0, x + comp.getWidth(), jComp.getHeight(), 7, 7);
                        graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
                        jSP.setPreferredSize(dim);
                    }

                    if (null != comp2.getBorder() && comp2.getBorder() instanceof OSDarkLAFGenBorder)
                    {
                        comp2.setBorder(null);
                    }
                }
                catch (final Exception ex)
                {
                    // Intentionally do nothing
                }
            }
        }

        super.paint(graph, jComp);
    }

    @Override
    public void installUI(JComponent jComp)
    {
        super.installUI(jComp);

        final JScrollPane aScrollPane = (JScrollPane)jComp;

        if (null != aScrollPane.getVerticalScrollBar())
        {
            aScrollPane.getVerticalScrollBar().putClientProperty(MetalScrollBarUI.FREE_STANDING_PROP, Boolean.FALSE);
        }

        if (null != aScrollPane.getHorizontalScrollBar())
        {
            aScrollPane.getHorizontalScrollBar().putClientProperty(MetalScrollBarUI.FREE_STANDING_PROP, Boolean.FALSE);
        }

        wasOriginalOpaque = aScrollPane.isOpaque();
        aScrollPane.setOpaque(false);

        final Component viewPortViewComp = aScrollPane.getViewport().getView();
        if (null != viewPortViewComp)
        {
            try
            {
                final JComponent vpComp2 = (JComponent)viewPortViewComp;

                if (vpComp2.getBorder() != null && vpComp2.getBorder() instanceof OSDarkLAFGenBorder)
                {
                    vpComp2.setBorder(null);
                }
            }
            catch (final Exception e)
            {
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void uninstallUI(JComponent jComp)
    {
        super.uninstallUI(jComp);
        jComp.setOpaque(wasOriginalOpaque);
    }
}
