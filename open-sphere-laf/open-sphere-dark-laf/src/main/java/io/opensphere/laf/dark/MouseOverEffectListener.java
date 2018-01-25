package io.opensphere.laf.dark;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.event.MouseInputAdapter;

/**
 * A listener used to respond to mouse-over events, and force repainting of the
 * parent button.
 */
public class MouseOverEffectListener extends MouseInputAdapter implements PropertyChangeListener, FocusListener
{
    /**
     * The parent to which the listener is bound.
     */
    private final AbstractButton parent;

    /**
     * Creates a new listener bound to the supplied parent.
     *
     * @param pParent the parent to which the listener is bound.
     */
    public MouseOverEffectListener(AbstractButton pParent)
    {
        parent = pParent;
    }

    @Override
    public void focusGained(FocusEvent e)
    {
        repaint();
    }

    @Override
    public void focusLost(FocusEvent e)
    {
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        parent.getModel().setRollover(true);
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        parent.getModel().setRollover(false);
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        parent.getModel().setRollover(false);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        parent.getModel().setRollover(false);
        repaint();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals("enabled"))
        {
            repaint();
        }
    }

    /**
     * A repaint method in which the parent button is repainted in response to
     * mouseover events.
     */
    public void repaint()
    {
        if (null != parent && null != parent.getParent())
        {
            parent.getParent().repaint(parent.getX() - 5, parent.getY() - 5, parent.getWidth() + 10, parent.getHeight() + 10);
        }
    }
}
