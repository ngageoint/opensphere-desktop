package io.opensphere.core.util.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.swing.Box;
import javax.swing.JPanel;

import io.opensphere.core.util.StrongChangeSupport;

/**
 * A panel that supports easier use of a GridBagLayout.
 */
public class GridBagPanel extends JPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The default GridBagConstraints. */
    private final GridBagConstraints myDefaultGBC = new GridBagConstraints();

    /** The current GridBagConstraints. */
    private GridBagConstraints myGBC = myDefaultGBC;

    /** Change support for invalidation. */
    private final StrongChangeSupport<Runnable> myInvalidationChangeSupport = new StrongChangeSupport<>();

    /** The style map. */
    private final Map<String, GridBagConstraints> myStyleMap = new HashMap<>();

    /** The queue of next styles to be used. */
    private final Queue<String> myStyleQueue = new LinkedList<>();

    /**
     * Constructor.
     */
    public GridBagPanel()
    {
        super(new GridBagLayout());
    }

    @Override
    public Component add(Component comp)
    {
        if (comp != null)
        {
            super.add(comp, myGBC);
        }
        useNextStyle();
        return comp;
    }

    /**
     * Adds the invalidation listener.
     *
     * @param listener The listener.
     */
    public void addInvalidationListener(Runnable listener)
    {
        myInvalidationChangeSupport.addListener(listener);
    }

    /**
     * Adds component(s) and then increments gridy.
     *
     * @param components the components
     * @return the grid bag panel
     */
    public GridBagPanel addRow(Component... components)
    {
        if (myGBC.gridx == GridBagConstraints.RELATIVE)
        {
            setGridx(0);
        }
        if (myGBC.gridy == GridBagConstraints.RELATIVE)
        {
            setGridy(0);
        }

        for (Component component : components)
        {
            add(component);
            incrementGridx();
        }
        setGridx(0);
        incrementGridy();
        return this;
    }

    /**
     * Sets the anchor to center.
     *
     * @return the grid bag panel
     */
    public GridBagPanel anchorCenter()
    {
        myGBC.anchor = GridBagConstraints.CENTER;
        return this;
    }

    /**
     * Sets the anchor to east.
     *
     * @return the grid bag panel
     */
    public GridBagPanel anchorEast()
    {
        myGBC.anchor = GridBagConstraints.EAST;
        return this;
    }

    /**
     * Sets the anchor to north.
     *
     * @return the grid bag panel
     */
    public GridBagPanel anchorNorth()
    {
        myGBC.anchor = GridBagConstraints.NORTH;
        return this;
    }

    /**
     * Sets the anchor to west.
     *
     * @return the grid bag panel
     */
    public GridBagPanel anchorWest()
    {
        myGBC.anchor = GridBagConstraints.WEST;
        return this;
    }

    /**
     * Sets the fill to both.
     *
     * @return the grid bag panel
     */
    public GridBagPanel fillBoth()
    {
        myGBC.fill = GridBagConstraints.BOTH;
        myGBC.weightx = 1.0;
        myGBC.weighty = 1.0;
        return this;
    }

    /**
     * Sets the fill to horizontal.
     *
     * @return the grid bag panel
     */
    public GridBagPanel fillHorizontal()
    {
        myGBC.fill = GridBagConstraints.HORIZONTAL;
        myGBC.weightx = 1.0;
        myGBC.weighty = 0.0;
        return this;
    }

    /**
     * Fill horizontal space.
     *
     * @return the grid bag panel
     */
    public GridBagPanel fillHorizontalSpace()
    {
        fillHorizontal();
        add(Box.createHorizontalGlue(), myGBC);
        return this;
    }

    /**
     * Sets the fill to vertical.
     *
     * @return the grid bag panel
     */
    public GridBagPanel fillNone()
    {
        myGBC.fill = GridBagConstraints.NONE;
        myGBC.weightx = 0.0;
        myGBC.weighty = 0.0;
        return this;
    }

    /**
     * Sets the fill to vertical.
     *
     * @return the grid bag panel
     */
    public GridBagPanel fillVertical()
    {
        myGBC.fill = GridBagConstraints.VERTICAL;
        myGBC.weightx = 0.0;
        myGBC.weighty = 1.0;
        return this;
    }

    /**
     * Fill vertical space.
     *
     * @return the grid bag panel
     */
    public GridBagPanel fillVerticalSpace()
    {
        fillVertical();
        add(Box.createVerticalGlue(), myGBC);
        return this;
    }

    /**
     * Gets the GridBagConstraints.
     *
     * @return the GridBagConstraints
     */
    public GridBagConstraints getGBC()
    {
        return myGBC;
    }

    /**
     * Increments gridx by 1.
     *
     * @return the grid bag panel
     */
    public GridBagPanel incrementGridx()
    {
        myGBC.gridx++;
        return this;
    }

    /**
     * Increments gridy by 1.
     *
     * @return the grid bag panel
     */
    public GridBagPanel incrementGridy()
    {
        myGBC.gridy++;
        return this;
    }

    /**
     * Initializes gridx and gridy to 0.
     *
     * @return the grid bag panel
     */
    public GridBagPanel init0()
    {
        myGBC.gridx = myGBC.gridy = 0;
        return this;
    }

    @Override
    public void invalidate()
    {
        super.invalidate();

        if (!myInvalidationChangeSupport.isEmpty())
        {
            myInvalidationChangeSupport.notifyListeners(l -> l.run());
        }
    }

    /**
     * Removes the invalidation listener.
     *
     * @param listener The listener.
     */
    public void removeInvalidationListener(Runnable listener)
    {
        myInvalidationChangeSupport.removeListener(listener);
    }

    /**
     * Reset insets.
     *
     * @return the grid bag panel
     */
    public GridBagPanel resetInsets()
    {
        setInsets(0, 0, 0, 0);
        return this;
    }

    /**
     * Sets the anchor.
     *
     * @param anchor the anchor
     * @return the grid bag panel
     */
    public GridBagPanel setAnchor(int anchor)
    {
        myGBC.anchor = anchor;
        return this;
    }

    /**
     * Sets the fill.
     *
     * @param fill the fill
     * @return the grid bag panel
     */
    public GridBagPanel setFill(int fill)
    {
        myGBC.fill = fill;
        return this;
    }

    /**
     * Sets the gridheight.
     *
     * @param gridheight the gridheight
     * @return the grid bag panel
     */
    public GridBagPanel setGridheight(int gridheight)
    {
        myGBC.gridheight = gridheight;
        return this;
    }

    /**
     * Sets the gridwidth.
     *
     * @param gridwidth the gridwidth
     * @return the grid bag panel
     */
    public GridBagPanel setGridwidth(int gridwidth)
    {
        myGBC.gridwidth = gridwidth;
        return this;
    }

    /**
     * Sets the gridx.
     *
     * @param gridx the gridx
     * @return the grid bag panel
     */
    public GridBagPanel setGridx(int gridx)
    {
        myGBC.gridx = gridx;
        return this;
    }

    /**
     * Sets the gridy.
     *
     * @param gridy the gridy
     * @return the grid bag panel
     */
    public GridBagPanel setGridy(int gridy)
    {
        myGBC.gridy = gridy;
        return this;
    }

    /**
     * Sets the insets.
     *
     * @param insets the insets
     * @return the grid bag panel
     */
    public GridBagPanel setInsets(Insets insets)
    {
        myGBC.insets.set(insets.top, insets.left, insets.bottom, insets.right);
        return this;
    }

    /**
     * Sets the insets.
     *
     * @param value the value for all sides
     * @return the grid bag panel
     */
    public GridBagPanel setInsets(int value)
    {
        myGBC.insets.set(value, value, value, value);
        return this;
    }

    /**
     * Sets the insets.
     *
     * @param top the inset from the top.
     * @param left the inset from the left.
     * @param bottom the inset from the bottom.
     * @param right the inset from the right.
     * @return the grid bag panel
     */
    public GridBagPanel setInsets(int top, int left, int bottom, int right)
    {
        myGBC.insets.set(top, left, bottom, right);
        return this;
    }

    /**
     * Sets the ipadx.
     *
     * @param ipadx the ipadx
     * @return the grid bag panel
     */
    public GridBagPanel setIpadx(int ipadx)
    {
        myGBC.ipadx = ipadx;
        return this;
    }

    /**
     * Sets the ipady.
     *
     * @param ipady the ipady
     * @return the grid bag panel
     */
    public GridBagPanel setIpady(int ipady)
    {
        myGBC.ipady = ipady;
        return this;
    }

    /**
     * Sets the weightx.
     *
     * @param weightx the weightx
     * @return the grid bag panel
     */
    public GridBagPanel setWeightx(double weightx)
    {
        myGBC.weightx = weightx;
        return this;
    }

    /**
     * Sets the weighty.
     *
     * @param weighty the weighty
     * @return the grid bag panel
     */
    public GridBagPanel setWeighty(double weighty)
    {
        myGBC.weighty = weighty;
        return this;
    }

    /**
     * Sets the next styles to use when adding components.
     *
     * @param styles The style names
     * @return the grid bag panel
     */
    public GridBagPanel style(String... styles)
    {
        myStyleQueue.clear();
        for (String style : styles)
        {
            myStyleQueue.add(style);
        }
        useNextStyle();
        return this;
    }

    /**
     * Uses the next style in the queue.
     */
    private void useNextStyle()
    {
        GridBagConstraints styleConstraints;
        String style = myStyleQueue.poll();
        if (style == null)
        {
            styleConstraints = myDefaultGBC;
        }
        else
        {
            styleConstraints = myStyleMap.get(style);
            if (styleConstraints == null)
            {
                styleConstraints = new GridBagConstraints();
                myStyleMap.put(style, styleConstraints);
            }
        }

        int gridx = myGBC.gridx;
        int gridy = myGBC.gridy;
        myGBC = styleConstraints;
        myGBC.gridx = gridx;
        myGBC.gridy = gridy;
    }
}
