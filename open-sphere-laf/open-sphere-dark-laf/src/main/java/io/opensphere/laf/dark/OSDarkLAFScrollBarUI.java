package io.opensphere.laf.dark;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalScrollBarUI;

public class OSDarkLAFScrollBarUI extends MetalScrollBarUI
{
    private boolean isClicked;

    private boolean isRolledOver;

    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFScrollBarUI();
    }

    @Override
    protected void paintTrack(Graphics graph, JComponent jComp, Rectangle trackBounds)
    {
        final Graphics2D graph2D = (Graphics2D)graph;
        GradientPaint gradientPainter = null;

        if (scrollbar.getOrientation() != JScrollBar.HORIZONTAL)
        {
            gradientPainter = new GradientPaint(trackBounds.x, trackBounds.y, OSDarkLAFUtils.getShadowColor(),
                    trackBounds.x + trackBounds.width, trackBounds.y, OSDarkLAFUtils.getActiveColor());
        }
        else
        {
            gradientPainter = new GradientPaint(trackBounds.x, trackBounds.y, OSDarkLAFUtils.getShadowColor(), trackBounds.x,
                    trackBounds.y + trackBounds.height, OSDarkLAFUtils.getActiveColor());
        }

        graph2D.setPaint(gradientPainter);
        graph2D.fill(trackBounds);
    }

    @Override
    protected void paintThumb(Graphics graph, JComponent jComp, Rectangle thumbBounds)
    {
        final Color tColor = UIManager.getColor("ScrollBar.thumb");
        final Color tShadow = UIManager.getColor("ScrollBar.thumbShadow");

        graph.translate(thumbBounds.x, thumbBounds.y);
        graph.setColor(tColor);
        graph.fillRect(0, 0, thumbBounds.width - 1, thumbBounds.height - 1);
        graph.setColor(isRolledOver ? tShadow.darker() : tShadow);
        graph.drawRect(0, 0, thumbBounds.width - 1, thumbBounds.height - 1);

        final Icon iconDecoration = scrollbar.getOrientation() == JScrollBar.HORIZONTAL
                ? UIManager.getIcon("ScrollBar.horizontalThumbIconImage")
                : UIManager.getIcon("ScrollBar.verticalThumbIconImage");

        final int width = iconDecoration.getIconWidth();
        final int height = iconDecoration.getIconHeight();
        final int x = (thumbBounds.width - width) / 2;
        final int y = (thumbBounds.height - height) / 2;

        if (scrollbar.getOrientation() == JScrollBar.HORIZONTAL && thumbBounds.width >= width
                || scrollbar.getOrientation() == JScrollBar.VERTICAL && thumbBounds.height >= height)

        {
            iconDecoration.paintIcon(jComp, graph, x, y);
        }

        graph.translate(-thumbBounds.x, -thumbBounds.y);

        final Graphics2D graph2D = (Graphics2D)graph;
        GradientPaint gradientPaint = null;

        Color color1;
        Color color2;
        if (isClicked)
        {
            color1 = OSDarkLAFUtils.getShadowColor();
            color2 = OSDarkLAFUtils.getActiveColor();
        }
        else
        {
            color1 = OSDarkLAFUtils.getActiveColor();
            color2 = OSDarkLAFUtils.getShadowColor();
        }

        gradientPaint = scrollbar.getOrientation() == JScrollBar.HORIZONTAL
                ? new GradientPaint(thumbBounds.x, thumbBounds.y, color1, thumbBounds.x, thumbBounds.height, color2)
                : new GradientPaint(thumbBounds.x, thumbBounds.y, color1, thumbBounds.width, thumbBounds.y, color2);

        graph2D.setPaint(gradientPaint);
        graph2D.fill(thumbBounds);
    }

    @Override
    protected TrackListener createTrackListener()
    {
        return new SBCustomMouseListener(this);
    }

    @Override
    protected JButton createDecreaseButton(int orientation)
    {
        decreaseButton = new OSDarkLAFScrollButton(orientation, scrollBarWidth, isFreeStanding);
        return decreaseButton;
    }

    @Override
    protected JButton createIncreaseButton(int orientation)
    {
        increaseButton = new OSDarkLAFScrollButton(orientation, scrollBarWidth, isFreeStanding);
        return increaseButton;
    }

    public class SBCustomMouseListener extends MetalScrollBarUI.TrackListener
    {
        OSDarkLAFScrollBarUI parent;

        public SBCustomMouseListener(OSDarkLAFScrollBarUI parent)
        {
            this.parent = parent;
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            super.mouseEntered(e);
            parent.isRolledOver = true;
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            super.mouseExited(e);
            parent.isRolledOver = false;
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            super.mousePressed(e);
            parent.isClicked = true;
            scrollbar.repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            super.mouseReleased(e);
            parent.isClicked = false;
            scrollbar.repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
            super.mouseMoved(e);

            if (parent.isRolledOver && !thumbRect.contains(e.getX(), e.getY()))
            {
                isRolledOver = false;
                scrollbar.repaint();
            }
            else if (!parent.isRolledOver && thumbRect.contains(e.getX(), e.getY()))
            {
                parent.isRolledOver = true;
                scrollbar.repaint();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            super.mouseDragged(e);

            if (parent.isRolledOver && !thumbRect.contains(e.getX(), e.getY()))
            {
                isRolledOver = false;
                scrollbar.repaint();
            }
            else if (!parent.isRolledOver && thumbRect.contains(e.getX(), e.getY()))
            {
                parent.isRolledOver = true;
                scrollbar.repaint();
            }
        }
    }
}
