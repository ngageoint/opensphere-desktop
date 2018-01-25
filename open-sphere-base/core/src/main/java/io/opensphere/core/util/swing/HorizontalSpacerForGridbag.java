package io.opensphere.core.util.swing;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;

/**
 * The Class HorizontalSpacerForGridbag.
 */
@SuppressWarnings("serial")
public class HorizontalSpacerForGridbag extends JLabel
{
    /**
     * {@link GridBagConstraints} gbConst the GridBagConstraints for this
     * {@link HorizontalSpacerForGridbag}.
     */
    private final GridBagConstraints myGBConst;

    /**
     * Instantiates a new horizontal spacer for grid bag.
     */
    public HorizontalSpacerForGridbag()
    {
        myGBConst = new GridBagConstraints();
        setText("");
        this.setSize(5, 5);
        initialize();
    }

    /**
     * Instantiates a new horizontal spacer for grid bag.
     *
     * @param x the value used for the grid x value of the
     *            {@link GridBagConstraints}
     * @param y the value used for the grid y value of the
     *            {@link GridBagConstraints}
     */
    public HorizontalSpacerForGridbag(int x, int y)
    {
        myGBConst = new GridBagConstraints();
        setText("");
        this.setSize(5, 5);
        myGBConst.gridx = x;
        myGBConst.gridy = y;
        initialize();
    }

    /**
     * Gets the grid bag constraints.
     *
     * @return the grid bag constraints.
     */
    public GridBagConstraints getGbConst()
    {
        return myGBConst;
    }

    /**
     * Gets the grid x.
     *
     * @return the grid x
     */
    public int getGridX()
    {
        return myGBConst.gridx;
    }

    /**
     * Gets the grid y.
     *
     * @return the grid y
     */
    public int getGridY()
    {
        return myGBConst.gridy;
    }

    /**
     * Sets the x.
     *
     * @param pX the new x
     */
    public void setGridX(int pX)
    {
        myGBConst.gridx = pX;
    }

    /**
     * Sets the y.
     *
     * @param pY the new y
     */
    public void setGridY(int pY)
    {
        myGBConst.gridy = pY;
    }

    /**
     * Initialize.
     */
    private void initialize()
    {
        myGBConst.fill = GridBagConstraints.HORIZONTAL;
        myGBConst.weighty = 0;
        myGBConst.weightx = 1.0;
        myGBConst.insets = new Insets(0, 0, 0, 0);
    }
}
