package io.opensphere.laf.dark.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;

import io.opensphere.laf.dark.OSDarkLAFUtils;
import io.opensphere.laf.dark.OpenSphereDarkLookAndFeel;

@SuppressWarnings("serial")
public class OSDarkLAFCellFocusBorder extends AbstractBorder implements UIResource
{
    protected static Insets insetsForBorder = new Insets(1, 1, 1, 1);

    @Override
    public Insets getBorderInsets(Component c)
    {
        return insetsForBorder;
    }

    @Override
    public Insets getBorderInsets(Component comp, Insets in)
    {
        final Insets tmp = getBorderInsets(comp);
        in.right = tmp.right;
        in.left = tmp.left;
        in.bottom = tmp.bottom;
        in.top = tmp.top;
        return in;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h)
    {
        final Color col = OSDarkLAFUtils.getThirdColor(OpenSphereDarkLookAndFeel.getControlTextColor(),
                OpenSphereDarkLookAndFeel.getFocusColor());
        g.setColor(col);
        g.drawRect(x, y, w - 1, h - 1);
    }
}
