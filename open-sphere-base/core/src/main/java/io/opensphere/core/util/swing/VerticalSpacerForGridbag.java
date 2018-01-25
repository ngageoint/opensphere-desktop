package io.opensphere.core.util.swing;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;

/**
 * The Class VerticalSpacerForGridbag.
 */
@SuppressWarnings("serial")
public class VerticalSpacerForGridbag extends JLabel
{
    /** The GB const. */
    private final GridBagConstraints myGBConst;

    /**
     * Instantiates a new vertical spacer for gridbag.
     */
    public VerticalSpacerForGridbag()
    {
        myGBConst = new GridBagConstraints();
        setText("");
        this.setSize(5, 5);
        initialize();
    }

    /**
     * Instantiates a new vertical spacer for gridbag.
     *
     * @param x the value used for the gridx value of the
     *            {@link GridBagConstraints}
     * @param y the value used for the gridy value of the
     *            {@link GridBagConstraints}
     */
    public VerticalSpacerForGridbag(int x, int y)
    {
        myGBConst = new GridBagConstraints();
        setText("");
        this.setSize(5, 5);
        myGBConst.gridx = x;
        myGBConst.gridy = y;
        initialize();
    }

    /**
     * Gets the gB const.
     *
     * @return the gB const
     */
    public GridBagConstraints getGBConst()
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
     * Sets the grid x.
     *
     * @param pGridX the new grid x
     */
    public void setGridX(int pGridX)
    {
        myGBConst.gridx = pGridX;
    }

    /**
     * Sets the grid y.
     *
     * @param pGridY the new grid y
     */
    public void setGridY(int pGridY)
    {
        myGBConst.gridy = pGridY;
    }

    /**
     * Initialize.
     */
    private void initialize()
    {
        myGBConst.fill = GridBagConstraints.VERTICAL;
        myGBConst.weighty = 1.0;
        myGBConst.weightx = 0;
        myGBConst.insets = new Insets(0, 0, 0, 0);
    }
}
