package io.opensphere.core.common.collapsablepanel;

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
     * The gb const. {@link GridBagConstraints} gbConst the GridBagConstraints
     * for this {@link HorizontalSpacerForGridbag}.
     */
    private final GridBagConstraints myGBConst;

    /**
     * Instantiates a new horizontal spacer for gridbag.
     */
    public HorizontalSpacerForGridbag()
    {
        this.myGBConst = new GridBagConstraints();
        this.setText("");
        this.setSize(5, 5);
        initialize();
    }

    /**
     * Instantiates a new horizontal spacer for gridbag.
     *
     * @param x the value used for the gridx value of the
     * @param y the value used for the gridy value of the
     *            {@link GridBagConstraints} {@link GridBagConstraints}
     */
    public HorizontalSpacerForGridbag(int x, int y)
    {
        this.myGBConst = new GridBagConstraints();
        this.setText("");
        this.setSize(5, 5);
        myGBConst.gridx = x;
        myGBConst.gridy = y;
        initialize();
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

    /**
     * Gets the gB const.
     *
     * @return the gB const
     */
    public GridBagConstraints getGBConst()
    {
        return myGBConst;
    }

}
