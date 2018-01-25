package io.opensphere.laf.dark.border;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;

import io.opensphere.laf.dark.OSDarkLAFUtils;
import io.opensphere.laf.dark.OpenSphereDarkLookAndFeel;

@SuppressWarnings("serial")
public class OSDarkLAFInternalFrameBorder extends AbstractBorder implements UIResource
{
    private static final int borderThickness = 3;

    protected static Insets defaultInsets = new Insets(0, borderThickness, borderThickness + 5, borderThickness + 5);

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
        graph.translate(x, y);
        final Graphics2D graph2D = (Graphics2D)graph.create();

        graph2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OSDarkLAFUtils.getFrameOpacityFloat()));

        // Select the border colors
        Color colorOne;
        Color colorTwo;
        GradientPaint gradientPaint;
        if (!((JInternalFrame)comp).isSelected())
        {
            colorOne = OpenSphereDarkLookAndFeel.getControl();
            colorTwo = OpenSphereDarkLookAndFeel.getControlDarkShadow();
            gradientPaint = new GradientPaint(0, 0, OpenSphereDarkLookAndFeel.getControl(), width, 0,
                    OpenSphereDarkLookAndFeel.getControlDarkShadow());
        }
        else
        {
            colorOne = OpenSphereDarkLookAndFeel.getPrimaryControlDarkShadow();
            colorTwo = OpenSphereDarkLookAndFeel.getPrimaryControl();
            gradientPaint = new GradientPaint(0, 0, OpenSphereDarkLookAndFeel.getPrimaryControlDarkShadow(), width, 0,
                    OpenSphereDarkLookAndFeel.getPrimaryControl());
        }

        graph2D.setColor(colorOne);
        graph2D.fillRect(0, 0, borderThickness, height - defaultInsets.bottom);

        graph2D.setPaint(gradientPaint);
        graph2D.fillRect(0, height - defaultInsets.bottom, width - defaultInsets.right + borderThickness, borderThickness);

        graph2D.setColor(colorTwo);
        graph2D.fillRect(width - defaultInsets.right, 0, borderThickness, height - defaultInsets.bottom);

        graph2D.dispose();

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
