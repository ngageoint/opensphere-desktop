package io.opensphere.laf.dark.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;

import io.opensphere.laf.dark.OpenSphereDarkLookAndFeel;

@SuppressWarnings("serial")
public class OSDarkLAFButtonBorder extends AbstractBorder implements UIResource
{
    protected static Insets insets = new Insets(0, 0, 0, 0);

    /**
     * Gets the border insets.
     */
    @Override
    public Insets getBorderInsets(Component comp)
    {
        return insets;
    }

    /**
     * Gets the border insets, given a set of insets.
     */
    @Override
    public Insets getBorderInsets(Component comp, Insets in)
    {
        final Insets tmp = getBorderInsets(comp);
        in.left = tmp.left;
        in.right = tmp.right;
        in.top = tmp.top;
        in.bottom = tmp.bottom;
        return in;
    }

    /**
     * Paints the border.
     */
    @Override
    public void paintBorder(Component comp, Graphics graph, int x, int y, int w, int h)
    {
        if (!((AbstractButton)comp).isBorderPainted())
        {
            return;
        }

        graph.translate(x, y);
        final Graphics2D g2D = (Graphics2D)graph;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2D.setColor(OpenSphereDarkLookAndFeel.getControlDarkShadow());
        g2D.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

        if (comp instanceof JButton)
        {
            final JButton b = (JButton)comp;

            if (b.isDefaultButton())
            {
                g2D.setColor(OpenSphereDarkLookAndFeel.getControlDarkShadow().darker());
                g2D.drawRoundRect(1, 1, w - 3, h - 3, 7, 7);
            }
        }

        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }
}
