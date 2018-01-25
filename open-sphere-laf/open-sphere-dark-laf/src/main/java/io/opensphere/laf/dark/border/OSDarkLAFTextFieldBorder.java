package io.opensphere.laf.dark.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.plaf.UIResource;

@SuppressWarnings("serial")
public class OSDarkLAFTextFieldBorder extends OSDarkLAFGenBorder implements UIResource
{
    protected static Insets defaultInsets = new Insets(5, 6, 5, 6);

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
        super.paintBorder(comp, graph, x + 2, y + 2, width - 4, height - 4);
    }
}
