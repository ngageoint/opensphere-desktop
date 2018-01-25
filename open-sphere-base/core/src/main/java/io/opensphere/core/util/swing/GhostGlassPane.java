package io.opensphere.core.util.swing;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * The Class GhostGlassPane.
 */
public class GhostGlassPane extends JPanel
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The composite. */
    private final transient AlphaComposite myComposite;

    /** The dragged. */
    private transient BufferedImage myDragged;

    /** The location. */
    private Point myLocation = new Point(0, 0);

    /** The Is dragging. */
    private boolean myIsDragging;

    /**
     * Instantiates a new ghost glass pane.
     *
     * @param alpha the alpha
     */
    public GhostGlassPane(float alpha)
    {
        setOpaque(false);
        myComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }

    /**
     * Checks if is dragging.
     *
     * @return true, if is dragging
     */
    public boolean isDragging()
    {
        return myIsDragging;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        if (myDragged == null)
        {
            return;
        }

        Graphics2D g2 = (Graphics2D)g;

        g2.setComposite(myComposite);
        g2.drawImage(myDragged, (int)(myLocation.getX() - myDragged.getWidth(this) / 2.0),
                (int)(myLocation.getY() - myDragged.getHeight(this) / 2.0), null);
    }

    /**
     * Sets the dragging.
     *
     * @param dragging the new dragging
     */
    public void setDragging(boolean dragging)
    {
        myIsDragging = dragging;
    }

    /**
     * Sets the image.
     *
     * @param dragged the new image
     */
    public void setImage(BufferedImage dragged)
    {
        myDragged = dragged;
    }

    /**
     * Sets the point.
     *
     * @param location the new point
     */
    public void setPoint(Point location)
    {
        myLocation = location;
    }
}
