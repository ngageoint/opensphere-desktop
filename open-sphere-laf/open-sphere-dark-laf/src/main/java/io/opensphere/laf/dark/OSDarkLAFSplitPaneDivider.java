package io.opensphere.laf.dark;

import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

@SuppressWarnings("serial")
public class OSDarkLAFSplitPaneDivider extends BasicSplitPaneDivider
{
    public OSDarkLAFSplitPaneDivider(BasicSplitPaneUI p)
    {
        super(p);
    }

    public void paint(Graphics graph)
    {
        super.paint(graph);

        Graphics2D graph2D = (Graphics2D)graph;
        GradientPaint gradientPaint = super.splitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT
                ? new GradientPaint(0, 0, OSDarkLAFUtils.getActiveColor(), 0, getHeight(), OSDarkLAFUtils.getShadowColor())
                : new GradientPaint(0, 0, OSDarkLAFUtils.getActiveColor(), getWidth(), 0, OSDarkLAFUtils.getShadowColor());

        graph2D.setPaint(gradientPaint);
        graph2D.fillRect(0, 0, getWidth(), getHeight());
    }

    protected JButton createRightOneTouchButton()
    {
        JButton button = new Button(Button.LOWER_RIGHT, super.splitPane, BasicSplitPaneDivider.ONE_TOUCH_SIZE);
        Boolean buttonOpaque = ((Boolean)UIManager.get("SplitPane.oneTouchButtonsOpaque"));
        if (null != buttonOpaque)
        {
            button.setOpaque(buttonOpaque.booleanValue());
        }
        return button;
    }

    protected JButton createLeftOneTouchButton()
    {
        JButton button = new Button(Button.UPER_LEFT, super.splitPane, BasicSplitPaneDivider.ONE_TOUCH_SIZE);
        Boolean buttonIsOpaque = ((Boolean)UIManager.get("SplitPane.oneTouchButtonsOpaque"));
        if (null != buttonIsOpaque)
        {
            button.setOpaque(buttonIsOpaque.booleanValue());
        }
        return button;
    }

    protected class Button extends JButton
    {
        public static final int LOWER_RIGHT = 1;

        public static final int UPER_LEFT = 0;

        private JSplitPane aSplitPane;

        private int direction;

        private int ots;

        public Button(int pDirection, JSplitPane splitPane, int ots)
        {
            this.direction = pDirection;
            aSplitPane = splitPane;
            this.ots = ots;

            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            setRequestFocusEnabled(false);
            setOpaque(false);
        }

        public void paint(Graphics graph)
        {
            if (null != aSplitPane)
            {
                int bSize = Math.min(getDividerSize(), ots);
                graph.setColor(OpenSphereDarkLookAndFeel.getFocusColor());

                int[] xsa = new int[3];
                int[] ysa = new int[3];

                if (direction == LOWER_RIGHT)
                {
                    if (orientation == JSplitPane.VERTICAL_SPLIT)
                    {
                        xsa = new int[] { 0, bSize / 2, bSize };
                        ysa = new int[] { 0, bSize, 0 };
                    }
                    else if (orientation == JSplitPane.HORIZONTAL_SPLIT)
                    {
                        xsa = new int[] { 0, 0, bSize };
                        ysa = new int[] { 0, bSize, bSize / 2 };
                    }
                }
                else
                {
                    if (orientation == JSplitPane.VERTICAL_SPLIT)
                    {
                        xsa = new int[] { 0, bSize / 2, bSize };
                        ysa = new int[] { bSize, 0, bSize };
                    }
                    else if (orientation == JSplitPane.HORIZONTAL_SPLIT)
                    {
                        xsa = new int[] { 0, bSize, bSize };
                        ysa = new int[] { bSize / 2, 0, bSize };
                    }
                }

                graph.fillPolygon(xsa, ysa, 3);
                graph.setColor(OpenSphereDarkLookAndFeel.getFocusColor().darker());
                graph.drawPolygon(xsa, ysa, 3);
            }
        }

        public boolean isFocusable()
        {
            return false;
        }

        public void setBorder(Border b)
        {
        }
    }
}
