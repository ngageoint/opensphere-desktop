package io.opensphere.laf.dark.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

@SuppressWarnings("serial")
public class OSDarkLAFToolBarBorder extends OSDarkLAFMenuBarBorder implements SwingConstants, UIResource
{
    private static int widthOfBump = 14;

    private static int vertThumbRepititions = 5;

    private static int insetDefault = 3;

    @Override
    public Insets getBorderInsets(Component comp)
    {
        return getBorderInsets(comp, new Insets(0, 0, 0, 0));
    }

    @Override
    public Insets getBorderInsets(Component comp, Insets in)
    {
        in.left = insetDefault;
        in.right = insetDefault;
        in.top = insetDefault;
        in.bottom = insetDefault;

        if (((JToolBar)comp).isFloatable())
        {
            if (((JToolBar)comp).getOrientation() != HORIZONTAL)
            {
                in.top += widthOfBump;
            }
            else
            {
                // Horizontal
                if (comp.getComponentOrientation().isLeftToRight())
                {
                    in.left += widthOfBump;
                }
                else
                {
                    in.right += widthOfBump;
                }
            }
        }

        final Insets toolBarMargin = ((JToolBar)comp).getMargin();

        if (null != toolBarMargin)
        {
            in.left += toolBarMargin.left;
            in.right += toolBarMargin.right;
            in.top += toolBarMargin.top;
            in.bottom += toolBarMargin.bottom;
        }

        return in;
    }

    @Override
    public void paintBorder(Component comp, Graphics graph, int x, int y, int width, int height)
    {
        Icon ic = null;
        int tmpDim = 0;

        if (((JToolBar)comp).isFloatable())
        {
            if (((JToolBar)comp).getOrientation() != HORIZONTAL)
            {
                ic = UIManager.getIcon("ScrollBar.horizontalThumbIconImage");
                tmpDim = ic.getIconWidth();
                for (int i = 0; i < vertThumbRepititions; i++)
                {
                    ic.paintIcon(comp, graph, x + 1 + tmpDim * i, y + 1);
                }
            }
            else
            {
                ic = UIManager.getIcon("ScrollBar.verticalThumbIconImage");
                tmpDim = ic.getIconHeight();
                for (int i = 0; i < vertThumbRepititions; i++)
                {
                    ic.paintIcon(comp, graph, x + 1, y + 1 + tmpDim * i);
                }
            }
        }
    }
}
