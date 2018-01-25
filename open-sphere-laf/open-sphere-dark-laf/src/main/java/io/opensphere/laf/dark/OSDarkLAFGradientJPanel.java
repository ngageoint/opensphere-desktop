package io.opensphere.laf.dark;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * The OpenSphere Dark Look and feel for gradient panels.
 */
public class OSDarkLAFGradientJPanel extends JPanel implements SwingConstants
{
    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = 4628299207314460260L;

    protected int orientation;

    protected Color beginColor;

    protected Color endColor;

    public OSDarkLAFGradientJPanel()
    {
        super();
        initialize();
    }

    public OSDarkLAFGradientJPanel(boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
        initialize();
    }

    public OSDarkLAFGradientJPanel(LayoutManager layout, boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
        initialize();
    }

    public OSDarkLAFGradientJPanel(LayoutManager layout)
    {
        super(layout);
        initialize();
    }

    protected void initialize()
    {
        orientation = VERTICAL;
        beginColor = OpenSphereDarkLookAndFeel.getControl();
        endColor = beginColor.darker();
    }

    @Override
    protected void paintComponent(Graphics graph)
    {
        final Graphics2D graph2D = (Graphics2D)graph;
        final GradientPaint gradientPaint = orientation == HORIZONTAL ? new GradientPaint(0, 0, beginColor, getWidth(), 0, endColor)
                : new GradientPaint(0, 0, beginColor, 0, getHeight(), endColor);
        graph2D.setPaint(gradientPaint);
        graph2D.fillRect(0, 0, getWidth(), getHeight());
    }

    public void setGradientDirection(int oreint)
    {
        orientation = oreint;
    }

    public void setGradientColors(Color pColorBegin, Color pColorEnd)
    {
        beginColor = pColorBegin;
        endColor = pColorEnd;
    }
}
