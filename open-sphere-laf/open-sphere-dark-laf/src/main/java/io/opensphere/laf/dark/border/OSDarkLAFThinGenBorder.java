package io.opensphere.laf.dark.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.plaf.UIResource;

import io.opensphere.laf.dark.OpenSphereDarkLookAndFeel;

@SuppressWarnings("serial")
public class OSDarkLAFThinGenBorder extends OSDarkLAFGenBorder implements UIResource
{
    protected static Insets defaultInsets = new Insets(1, 1, 1, 1);

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
        graph.setColor(OpenSphereDarkLookAndFeel.getControlDarkShadow());
        graph.drawRect(x, y, width - 1, height - 1);
    }
}
