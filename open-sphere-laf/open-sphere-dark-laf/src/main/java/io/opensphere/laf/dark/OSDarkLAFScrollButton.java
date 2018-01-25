package io.opensphere.laf.dark;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalScrollButton;

@SuppressWarnings("serial")
public class OSDarkLAFScrollButton extends MetalScrollButton
{
    public OSDarkLAFScrollButton(int dir, int width, boolean isFreeStanding)
    {
        super(dir, width + 1, isFreeStanding);
    }

    @Override
    public void paint(Graphics graph)
    {
        final Rectangle area = new Rectangle(0, 0, getWidth(), getHeight());

        final Graphics2D graph2D = (Graphics2D)graph;
        GradientPaint gradienPaint = null;

        if (getDirection() == SwingConstants.WEST || getDirection() == SwingConstants.EAST)
        {
            if (getModel().isSelected() || getModel().isPressed())
            {
                gradienPaint = new GradientPaint(0, 0, OSDarkLAFUtils.getShadowColor(), 0, area.height,
                        OSDarkLAFUtils.getActiveColor());
            }
            else
            {
                gradienPaint = new GradientPaint(0, 0, OSDarkLAFUtils.getActiveColor(), 0, area.height,
                        OSDarkLAFUtils.getShadowColor());
            }
        }
        else
        {
            if (getModel().isSelected() || getModel().isPressed())
            {
                gradienPaint = new GradientPaint(0, 0, OSDarkLAFUtils.getShadowColor(), area.width, 0,
                        OSDarkLAFUtils.getActiveColor());
            }
            else
            {
                gradienPaint = new GradientPaint(0, 0, OSDarkLAFUtils.getActiveColor(), area.width, 0,
                        OSDarkLAFUtils.getShadowColor());
            }
        }

        graph2D.setColor(OpenSphereDarkLookAndFeel.getControl());
        graph2D.fillRect(area.x, area.y, area.width, area.height);
        graph2D.setPaint(gradienPaint);
        graph2D.fillRect(area.x, area.y, area.width, area.height);

        if (getModel().isRollover())
        {
            graph2D.setColor(OSDarkLAFUtils.getRolloverColor());
            graph2D.fillRect(area.x, area.y, area.width, area.height);
        }

        graph2D.setColor(OpenSphereDarkLookAndFeel.getControlDarkShadow());
        graph2D.drawRect(area.x, area.y, area.width - 1, area.height - 1);

        Icon directionalIcon = null;
        switch (getDirection())
        {
            case SwingConstants.EAST:
                directionalIcon = UIManager.getIcon("ScrollBar.eastButtonIcon");
                break;
            case SwingConstants.WEST:
                directionalIcon = UIManager.getIcon("ScrollBar.westButtonIcon");
                break;
            case SwingConstants.NORTH:
                directionalIcon = UIManager.getIcon("ScrollBar.northButtonIcon");
                break;
            case SwingConstants.SOUTH:
                directionalIcon = UIManager.getIcon("ScrollBar.southButtonIcon");
                break;
        }
        directionalIcon.paintIcon(this, graph2D, area.x, area.y);
    }
}
