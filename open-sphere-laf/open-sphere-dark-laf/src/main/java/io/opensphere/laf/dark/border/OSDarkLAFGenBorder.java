package io.opensphere.laf.dark.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;

@SuppressWarnings("serial")
public class OSDarkLAFGenBorder extends AbstractBorder implements UIResource
{
    protected static Insets defaultInsets = new Insets(3, 3, 2, 2);

    @Override
    public Insets getBorderInsets(Component c)
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
        final int wl = width - 8;
        final int hl = height - 8;

        ImageIcon icon = (ImageIcon)UIManager.getIcon("BorderGeneralTop");
        if (null == icon)
        {
            return;
        }

        graph.translate(x, y);

        graph.drawImage(icon.getImage(), 4, 0, wl, icon.getIconHeight(), null);

        icon = (ImageIcon)UIManager.getIcon("BorderGeneralBottom");
        graph.drawImage(icon.getImage(), 4, height - icon.getIconHeight(), wl, icon.getIconHeight(), null);

        icon = (ImageIcon)UIManager.getIcon("BorderGeneralRight");
        graph.drawImage(icon.getImage(), width - icon.getIconWidth(), 4, icon.getIconWidth(), hl, null);

        icon = (ImageIcon)UIManager.getIcon("BorderGeneralLeft");
        graph.drawImage(icon.getImage(), 0, 4, icon.getIconWidth(), hl, null);

        icon = (ImageIcon)UIManager.getIcon("BorderGeneralUpperLeft");
        icon.paintIcon(comp, graph, 0, 0);

        icon = (ImageIcon)UIManager.getIcon("BorderGeneralBottomLeft");
        icon.paintIcon(comp, graph, 0, height - icon.getIconHeight());

        icon = (ImageIcon)UIManager.getIcon("BorderGeneralUpperRight");
        icon.paintIcon(comp, graph, width - icon.getIconWidth(), 0);

        icon = (ImageIcon)UIManager.getIcon("BorderGeneralBottomRight");
        icon.paintIcon(comp, graph, width - icon.getIconWidth(), height - icon.getIconHeight());

        graph.translate(-x, -y);
    }
}
