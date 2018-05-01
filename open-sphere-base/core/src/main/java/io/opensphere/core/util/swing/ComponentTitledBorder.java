package io.opensphere.core.util.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * A Border sub-class that puts a generic Component into the Border Title (vice
 * text).
 */
public class ComponentTitledBorder implements Border, MouseListener, SwingConstants
{
    /** The Component's offset from the left edge. */
    public static final int LEFT_OFFSET = 5;

    /** The Component to add into the Border. */
    private final Component myComp;

    /** The Container this Border surrounds. */
    private final JComponent myContainer;

    /** A Rectangle that forms the Border. */
    private Rectangle myRectangle;

    /** The Border. */
    private final Border myBorder;

    /**
     * Constructor.
     *
     * @param comp The component that gets added to the Border
     * @param container The container this Border surrounds
     * @param border The Border type to render around the Container
     */
    public ComponentTitledBorder(Component comp, JComponent container, Border border)
    {
        myComp = comp;
        myContainer = container;
        myBorder = border;
        container.addMouseListener(this);
    }

    @Override
    public Insets getBorderInsets(Component c)
    {
        Dimension size = myComp.getPreferredSize();
        Insets insets = myBorder.getBorderInsets(c);
        insets.top = Math.max(insets.top, size.height);
        return insets;
    }

    @Override
    public boolean isBorderOpaque()
    {
        return true;
    }

    @Override
    public void mouseClicked(MouseEvent me)
    {
        dispatchEvent(me);
    }

    @Override
    public void mouseEntered(MouseEvent me)
    {
        dispatchEvent(me);
    }

    @Override
    public void mouseExited(MouseEvent me)
    {
        dispatchEvent(me);
    }

    @Override
    public void mousePressed(MouseEvent me)
    {
        dispatchEvent(me);
    }

    @Override
    public void mouseReleased(MouseEvent me)
    {
        dispatchEvent(me);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
        Insets borderInsets = myBorder.getBorderInsets(c);
        Insets insets = getBorderInsets(c);
        int temp = (insets.top - borderInsets.top) / 2;
        Dimension size = myComp.getPreferredSize();
        myRectangle = new Rectangle(LEFT_OFFSET, 0, size.width, size.height);
        myBorder.paintBorder(c, g, x, y + temp, width, height - temp);
        SwingUtilities.paintComponent(g, myComp, (Container)c, myRectangle);
    }

    /**
     * Dispatch mouse events to child component and repaint contained objects.
     *
     * @param me The Mouse event to dispatch
     */
    private void dispatchEvent(MouseEvent me)
    {
        if (myRectangle != null && myRectangle.contains(me.getX(), me.getY()))
        {
            Point pt = me.getPoint();
            pt.translate(-LEFT_OFFSET, 0);
            myComp.setBounds(myRectangle);
            myComp.dispatchEvent(new MouseEvent(myComp, me.getID(), me.getWhen(), me.getModifiersEx(), pt.x, pt.y,
                    me.getClickCount(), me.isPopupTrigger(), me.getButton()));
            if (!myComp.isValid())
            {
                myContainer.repaint();
            }
        }
    }
}
