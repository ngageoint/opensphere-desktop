package io.opensphere.wms.display;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Base class for the gridbag sub panels.
 */
public final class WMSGridBagUtil
{
    /**
     * Add the given component in the current position in the grid bag.
     *
     * @param parent The parent of the component.
     * @param gbc The constraints to give to the layout manager.
     * @param comp The component being added.
     */
    public static void addComponent(JPanel parent, GridBagConstraints gbc, Component comp)
    {
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 7, 2, 7);
        parent.add(comp, gbc);
    }

    /**
     * Add a label in the current position for the grid bag.
     *
     * @param parent The parent component of the label.
     * @param gbc The grid bag constraints.
     * @param labelText The text for the label.
     */
    public static void addLabel(JPanel parent, GridBagConstraints gbc, String labelText)
    {
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        JLabel label = new JLabel();
        label.setText(labelText);
        parent.add(label, gbc);
    }

    /** Do not allow construction. */
    private WMSGridBagUtil()
    {
    }
}
