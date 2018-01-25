package io.opensphere.laf.dark.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;

import io.opensphere.laf.dark.OpenSphereDarkLookAndFeel;

@SuppressWarnings("serial")
public class OSDarkLAFPopupMenuBorder extends AbstractBorder implements UIResource
{
    protected static Insets insets = new Insets(1, 1, 5, 5);

    @Override
    public Insets getBorderInsets(Component comp)
    {
        return insets;
    }

    @Override
    public Insets getBorderInsets(Component comp, Insets in)
    {
        final Insets tmp = getBorderInsets(comp);
        in.top = tmp.top;
        in.left = tmp.left;
        in.bottom = tmp.bottom;
        in.right = tmp.right;
        return in;
    }

    @Override
    public void paintBorder(Component comp, Graphics graph, int x, int y, int width, int height)
    {
        graph.translate(x, y);

        graph.setColor(OpenSphereDarkLookAndFeel.getControlDarkShadow());

        graph.drawRect(0, 0, width - 5, height - 5);

        Icon ic = UIManager.getIcon("BorderPopupMenu.MenuShadowTopLeft");
        ic.paintIcon(comp, graph, width - 5, height - 5);

        ic = UIManager.getIcon("BorderPopupMenu.MenuShadowUp");
        ic.paintIcon(comp, graph, width - 5, 0);

        ic = UIManager.getIcon("BorderPopupMenu.MenuShadowTopRight");
        ic.paintIcon(comp, graph, 0, height - 5);

        ic = UIManager.getIcon("BorderPopupMenu.MenuShadowBottomIcon");
        graph.drawImage(((ImageIcon)ic).getImage(), 5, height - 5, width - 10, ic.getIconHeight(), null);

        ic = UIManager.getIcon("BorderPopupMenu.MenuShadowRight");
        graph.drawImage(((ImageIcon)ic).getImage(), width - 5, 5, ic.getIconWidth(), height - 10, null);

        graph.translate(-x, -y);
    }
}
