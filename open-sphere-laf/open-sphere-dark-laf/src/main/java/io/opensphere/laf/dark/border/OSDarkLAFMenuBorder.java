package io.opensphere.laf.dark.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.ButtonModel;
import javax.swing.JMenuItem;
import javax.swing.plaf.UIResource;

@SuppressWarnings("serial")
public class OSDarkLAFMenuBorder extends OSDarkLAFGenBorder implements UIResource
{
    protected static Insets defaultInsets = new Insets(3, 3, 3, 3);

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
        final JMenuItem menuItem = (JMenuItem)comp;
        final ButtonModel buttonModel = menuItem.getModel();

        if (buttonModel.isSelected() || buttonModel.isArmed())
        {
            super.paintBorder(comp, graph, x, y, width, height - 2);
        }
    }
}
