package io.opensphere.core.control.action;

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

/**
 * A DisabledMenuOption that allows a menu option to be displayed but not be
 * usable.
 */
public class DisabledMenuOption extends MenuOption
{
    /**
     * Instantiates a new disabled selection menu option.
     *
     * @param label the label
     * @param command the command
     * @param toolTip the tool tip
     */
    public DisabledMenuOption(String label, String command, String toolTip)
    {
        super(label, command, toolTip);
    }

    @Override
    public JMenuItem toJMenuItem(ActionListener listener)
    {
        JMenuItem item = super.toJMenuItem(listener);
        item.setEnabled(false);
        return item;
    }
}
