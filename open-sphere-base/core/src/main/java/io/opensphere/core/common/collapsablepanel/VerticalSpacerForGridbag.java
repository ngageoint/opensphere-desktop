package io.opensphere.core.common.collapsablepanel;

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
