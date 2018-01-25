package io.opensphere.laf.dark.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;

import io.opensphere.laf.dark.OSDarkLAFUtils;

@SuppressWarnings("serial")
public class OSDarkLAFMenuBarBorder extends AbstractBorder implements UIResource
{
    protected static Insets defaultInsets = new Insets(0, 2, 0, 10);

    @Override
    public Insets getBorderInsets(Component comp)
    {
        return defaultInsets;
    }

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

    @Override
    public void paintBorder(Component comp, Graphics graph, int x, int y, int width, int height)
    {
        graph.setColor(OSDarkLAFUtils.getShadowColor());
        graph.drawLine(0, height - 2, width, height - 2);
        graph.setColor(OSDarkLAFUtils.getActiveColor());
        graph.drawLine(0, height - 1, width, height - 1);
    }
}
