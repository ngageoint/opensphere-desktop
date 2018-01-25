package io.opensphere.core.options.impl;

import java.awt.Component;
import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;

import io.opensphere.core.util.swing.GridBagPanel;

/** A wrapper panel for an options panel. */
public class OptionsPanel extends GridBagPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param panel the panel
     */
    public OptionsPanel(Component panel)
    {
        this(panel, false);
    }

    /**
     * Constructor.
     *
     * @param panel the panel
     * @param fill whether to fill the panel to fit its parent
     */
    public OptionsPanel(Component panel, boolean fill)
    {
        this(panel, fill ? GridBagConstraints.BOTH : GridBagConstraints.NONE);
    }

    /**
     * Constructor.
     *
     * @param panel the panel
     * @param fill the GridBagConstraints fill option
     */
    public OptionsPanel(Component panel, int fill)
    {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setFill(fill);
        setAnchor(GridBagConstraints.NORTHWEST).setWeightx(1).setWeighty(1).addRow(panel);
    }
}
