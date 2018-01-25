package io.opensphere.laf.dark.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

@SuppressWarnings("serial")
public class OSDarkLAFComboButtonBorder extends OSDarkLAFButtonBorder
{
    protected static Insets defaultInsets = new Insets(2, 2, 2, 2);

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
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
        super.paintBorder(c, g, x + 2, y + 2, width - 4, height - 4);
    }
}
