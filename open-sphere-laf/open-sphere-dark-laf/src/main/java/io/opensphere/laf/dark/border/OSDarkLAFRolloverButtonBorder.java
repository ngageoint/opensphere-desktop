package io.opensphere.laf.dark.border;

import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.RoundRectangle2D;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;

import io.opensphere.laf.dark.OSDarkLAFUtils;
import io.opensphere.laf.dark.OpenSphereDarkLookAndFeel;

@SuppressWarnings("serial")
public class OSDarkLAFRolloverButtonBorder extends AbstractBorder implements UIResource
{
    protected static int ARC_WIDTH = 8;

    protected static Insets insets = new Insets(3, 3, 3, 3);

    @Override
    public Insets getBorderInsets(Component comp)
    {
        return insets;
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
        if (!((AbstractButton)comp).isBorderPainted())
        {
            return;
        }

        final ButtonModel model = ((AbstractButton)comp).getModel();

        if (model.isRollover())
        {
            graph.setColor(OpenSphereDarkLookAndFeel.getControlDarkShadow());
            graph.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);

            final RoundRectangle2D.Float bt = new RoundRectangle2D.Float();
            bt.arcwidth = ARC_WIDTH;
            bt.archeight = ARC_WIDTH;
            bt.x = 0;
            bt.y = 0;
            bt.width = comp.getWidth();
            bt.height = comp.getHeight();

            final GradientPaint gradientPt = model.isPressed()
                    ? new GradientPaint(0, 0, OSDarkLAFUtils.getShadowColor(), 0, comp.getHeight() / 2,
                            OSDarkLAFUtils.getActiveColor())
                    : new GradientPaint(0, 0, OSDarkLAFUtils.getActiveColor(), 0, comp.getHeight(),
                            OSDarkLAFUtils.getShadowColor());

            final Graphics2D g2D = (Graphics2D)graph;
            g2D.setPaint(gradientPt);
            g2D.fill(bt);
        }
    }
}
