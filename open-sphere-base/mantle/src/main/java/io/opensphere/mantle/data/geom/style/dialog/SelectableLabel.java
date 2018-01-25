package io.opensphere.mantle.data.geom.style.dialog;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.tree.DefaultTreeCellRenderer;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * The Class SelectableLabel.
 */
public class SelectableLabel extends JLabel
{
    /** The Constant ourDefaultListCellRenderer. */
    private static final DefaultTreeCellRenderer ourDefaultTreeCellRenderer = new DefaultTreeCellRenderer();

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Action listeners. */
    private final Set<ActionListener> myActionListeners;

    /** The Selection background color. */
    private Color myBackgroundColor;

    /** The Is selected. */
    private boolean myIsSelected;

    /**
     * Instantiates a new selectable label.
     *
     * @param selected the selected
     */
    public SelectableLabel(boolean selected)
    {
        super();
        myActionListeners = New.set();
        myIsSelected = selected;
        updateFromRenderer(true);
        addMouseClickListener();
    }

    /**
     * Instantiates a new selectable label.
     *
     * @param value the value
     * @param selected the selected
     */
    public SelectableLabel(String value, boolean selected)
    {
        super(value);
        myActionListeners = New.set();
        myIsSelected = selected;
        updateFromRenderer(true);
        addMouseClickListener();
    }

    /**
     * Adds the action listener.
     *
     * @param al the {@link ActionListener}
     */
    public void addActionListener(ActionListener al)
    {
        synchronized (myActionListeners)
        {
            myActionListeners.add(al);
        }
    }

    /**
     * Checks if is selected.
     *
     * @return true, if is selected
     */
    public boolean isSelected()
    {
        return myIsSelected;
    }

    @Override
    public void paint(Graphics g)
    {
        if (myIsSelected)
        {
            Color bgColor = myBackgroundColor;
            Graphics2D g2d = (Graphics2D)g;
            g2d.setColor(bgColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
        super.paint(g);
    }

    /**
     * Removes the action listener.
     *
     * @param al the {@link ActionListener}
     */
    public void removeActionListener(ActionListener al)
    {
        synchronized (myActionListeners)
        {
            myActionListeners.remove(al);
        }
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        updateFromRenderer(enabled);
    }

    /**
     * Sets the selected state of the label.
     *
     * @param selected the selected
     * @param event the event
     */
    public final void setSelected(boolean selected, boolean event)
    {
        if (selected != myIsSelected)
        {
            myIsSelected = selected;
            fireActionPerformed(myIsSelected);
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    updateFromRenderer(isEnabled());
                    repaint();
                }
            });
        }
    }

    /**
     * Adds the mouse click listener.
     */
    private void addMouseClickListener()
    {
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (isEnabled())
                {
                    setSelected(!myIsSelected, true);
                }
            }
        });
    }

    /**
     * Fire action performed.
     *
     * @param selected the selected
     */
    private void fireActionPerformed(boolean selected)
    {
        final ActionEvent ae = new ActionEvent(this, 0, selected ? "SELECTED" : "DESELECTED");
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (myActionListeners)
                {
                    for (ActionListener al : myActionListeners)
                    {
                        al.actionPerformed(ae);
                    }
                }
            }
        });
    }

    /**
     * Update foreground background colors.
     *
     * @param enabled the enabled
     */
    private void updateFromRenderer(boolean enabled)
    {
        ourDefaultTreeCellRenderer.setEnabled(enabled);
        setFont(ourDefaultTreeCellRenderer.getFont());
        setForeground(myIsSelected ? ourDefaultTreeCellRenderer.getTextSelectionColor()
                : ourDefaultTreeCellRenderer.getTextNonSelectionColor());
        myBackgroundColor = myIsSelected ? ourDefaultTreeCellRenderer.getBackgroundSelectionColor()
                : ourDefaultTreeCellRenderer.getBackgroundNonSelectionColor();
    }
}
