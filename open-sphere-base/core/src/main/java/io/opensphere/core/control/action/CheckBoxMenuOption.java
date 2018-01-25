package io.opensphere.core.control.action;

import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

/**
 * The Class CheckBoxMenuOption.
 */
public class CheckBoxMenuOption extends MenuOption
{
    /** The Default checkbox state. */
    private final boolean myDefaultState;

    /**
     * Instantiates a new check box menu option.
     *
     * @param label the label
     * @param command the command
     * @param toolTip the tool tip
     * @param defaultSelectionState the selected
     */
    public CheckBoxMenuOption(String label, String command, String toolTip, boolean defaultSelectionState)
    {
        super(label, command, toolTip);
        myDefaultState = defaultSelectionState;
    }

    @Override
    public void addSubOption(MenuOption option)
    {
        throw new UnsupportedOperationException("Cannot add sub menu options to a CheckBoxMenuOption!");
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        CheckBoxMenuOption other = (CheckBoxMenuOption)obj;
        return myDefaultState == other.myDefaultState;
    }

    @Override
    public List<MenuOption> getSubOptions()
    {
        return null;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myDefaultState ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean hasSubOptions()
    {
        return false;
    }

    @Override
    public boolean removeSubOption(MenuOption option)
    {
        return false;
    }

    @Override
    public JMenuItem toJMenuItem(ActionListener listener)
    {
        JMenuItem item = null;
        if (hasSubOptions())
        {
            item = super.toJMenuItem(listener);
        }
        else
        {
            item = new JCheckBoxMenuItem(getLabel(), myDefaultState);
            item.setActionCommand(getCommand());
            if (getTooltip() != null)
            {
                item.setToolTipText(getTooltip());
            }
            if (listener != null)
            {
                item.addActionListener(listener);
            }
        }
        return item;
    }
}
