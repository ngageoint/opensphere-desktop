package io.opensphere.server.util;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * Builder class for {@link GridBagConstraints}.
 */
public class GridBagConstraintsBuilder
{
    /** The x grid position. */
    private int myGridx = GridBagConstraints.RELATIVE;

    /** The y grid position. */
    private int myGridy = GridBagConstraints.RELATIVE;

    /** The horizontal grid span. */
    private int myGridWidth = 1;

    /** The vertical grid span. */
    private int myGridHeight = 1;

    /** The x weight. */
    private double myWeightx;

    /** The y weight. */
    private double myWeighty;

    /** The anchor position of components. */
    private int myAnchor = GridBagConstraints.CENTER;

    /** The cell's fill type. */
    private int myFill = GridBagConstraints.NONE;

    /** The cell insets (i.e. margins). */
    private Insets myInsets = new Insets(0, 0, 0, 0);

    /** The internal x padding. */
    private int myIPadx;

    /** The internal y padding. */
    private int myIPady;

    /**
     * Set the anchor position of components.
     *
     * @param anchor The anchor position of components
     * @return This {@link GridBagConstraints} Builder
     */
    public GridBagConstraintsBuilder anchor(int anchor)
    {
        myAnchor = anchor;
        return this;
    }

    /**
     * Builds the {@link GridBagConstraints}.
     *
     * @return the {@link GridBagConstraints}
     */
    public GridBagConstraints build()
    {
        return new GridBagConstraints(myGridx, myGridy, myGridWidth, myGridHeight, myWeightx, myWeighty, myAnchor, myFill,
                myInsets, myIPadx, myIPady);
    }

    /**
     * Set the cell's fill type.
     *
     * @param fill The cell's fill type
     * @return This {@link GridBagConstraints} Builder
     */
    public GridBagConstraintsBuilder fill(int fill)
    {
        myFill = fill;
        return this;
    }

    /**
     * Set the vertical grid span.
     *
     * @param gridheight The vertical grid span
     * @return This {@link GridBagConstraints} Builder
     */
    public GridBagConstraintsBuilder gridheight(int gridheight)
    {
        myGridHeight = gridheight;
        return this;
    }

    /**
     * Set the horizontal grid span.
     *
     * @param gridwidth The horizontal grid span
     * @return This {@link GridBagConstraints} Builder
     */
    public GridBagConstraintsBuilder gridwidth(int gridwidth)
    {
        myGridWidth = gridwidth;
        return this;
    }

    /**
     * Set the x grid position.
     *
     * @param gridx The x grid position
     * @return This {@link GridBagConstraints} Builder
     */
    public GridBagConstraintsBuilder gridx(int gridx)
    {
        myGridx = gridx;
        return this;
    }

    /**
     * Set the y grid position.
     *
     * @param gridy The y grid position
     * @return This {@link GridBagConstraints} Builder
     */
    public GridBagConstraintsBuilder gridy(int gridy)
    {
        myGridy = gridy;
        return this;
    }

    /**
     * Set the cell insets (i.e. margins).
     *
     * @param insets The cell insets
     * @return This {@link GridBagConstraints} Builder
     */
    public GridBagConstraintsBuilder insets(Insets insets)
    {
        myInsets = insets;
        return this;
    }

    /**
     * Set the cell insets (i.e. margins).
     *
     * @param top the inset from the top
     * @param left the inset from the left
     * @param bottom the inset from the bottom
     * @param right the inset from the right
     * @return This {@link GridBagConstraints} Builder
     */
    public GridBagConstraintsBuilder insets(int top, int left, int bottom, int right)
    {
        myInsets = new Insets(top, left, bottom, right);
        return this;
    }

    /**
     * Set the internal x padding.
     *
     * @param ipadx The internal x padding
     * @return This {@link GridBagConstraints} Builder
     */
    public GridBagConstraintsBuilder ipadx(int ipadx)
    {
        myIPadx = ipadx;
        return this;
    }

    /**
     * Set the internal y padding.
     *
     * @param ipady The internal y padding
     * @return This {@link GridBagConstraints} Builder
     */
    public GridBagConstraintsBuilder ipady(int ipady)
    {
        myIPady = ipady;
        return this;
    }

    /**
     * Set the x weight.
     *
     * @param weightx The x weight
     * @return This {@link GridBagConstraints} Builder
     */
    public GridBagConstraintsBuilder weightx(double weightx)
    {
        myWeightx = weightx;
        return this;
    }

    /**
     * Set the y weight.
     *
     * @param weighty The y weight
     * @return This {@link GridBagConstraints} Builder
     */
    public GridBagConstraintsBuilder weighty(double weighty)
    {
        myWeighty = weighty;
        return this;
    }
}
