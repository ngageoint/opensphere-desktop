package io.opensphere.core.control.action;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import io.opensphere.core.util.Utilities;

/**
 * The Class MenuOption.
 */
public class MenuOption
{
    /** The label. */
    private final String myLabel;

    /** The command. */
    private final String myCommand;

    /** The my tooltip. */
    private final String myTooltip;

    /** Optional icon to display with the menu item. */
    private final Icon myIcon;

    /** The sub options. */
    private List<MenuOption> mySubOptions;

    /** Optional accelerator for the menu option. */
    private final KeyStroke myAccelerator;

    /**
     * Instantiates a new selection menu option.
     *
     * @param label the label
     * @param command the command
     * @param toolTip the tool tip
     * @param accelerator the accelerator for the menu
     */
    public MenuOption(String label, String command, String toolTip, KeyStroke accelerator)
    {
        this(label, command, toolTip, accelerator, null);
    }

    /**
     * Instantiates a new selection menu option.
     *
     * @param label the label
     * @param command the command
     * @param toolTip the tool tip
     * @param accelerator the accelerator for the menu
     * @param icon Optional icon to display with the menu item.
     */
    public MenuOption(String label, String command, String toolTip, KeyStroke accelerator, Icon icon)
    {
        Utilities.checkNull(label, "label");
        myLabel = label;
        myCommand = command;
        myTooltip = toolTip;
        myAccelerator = accelerator;
        myIcon = icon;
    }

    /**
     * Instantiates a new selection menu option.
     *
     * @param label the label
     * @param command the command
     * @param toolTip the tool tip
     */
    public MenuOption(String label, String command, String toolTip)
    {
        this(label, command, toolTip, (KeyStroke)null);
    }

    /**
     * Instantiates a new selection menu option.
     *
     * @param label the label
     * @param command the command
     * @param toolTip the tool tip
     * @param icon Optional icon to display with the menu item.
     */
    public MenuOption(String label, String command, String toolTip, Icon icon)
    {
        this(label, command, toolTip, (KeyStroke)null, icon);
    }

    /**
     * Adds the sub option.
     *
     * @param option the option
     */
    public void addSubOption(MenuOption option)
    {
        if (mySubOptions == null)
        {
            mySubOptions = new ArrayList<>();
        }
        mySubOptions.add(option);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        MenuOption other = (MenuOption)obj;
        return Objects.equals(myCommand, other.myCommand) && Objects.equals(myLabel, other.myLabel);
    }

    /**
     * Gets the command for the menu option. Not used if this is a menu with
     * sub-menu.
     *
     * @return the string to be returned as a command.
     */
    public String getCommand()
    {
        return myCommand;
    }

    /**
     * Gets the label for the menu option.
     *
     * @return the label
     */
    public String getLabel()
    {
        return myLabel;
    }

    /**
     * Gets the value of the icon ({@link #myIcon}) field.
     *
     * @return the value stored in the {@link #myIcon} field.
     */
    public Icon getIcon()
    {
        return myIcon;
    }

    /**
     * Gets the sub options.
     *
     * @return the sub options
     */
    public List<MenuOption> getSubOptions()
    {
        return mySubOptions;
    }

    /**
     * Gets the tooltip.
     *
     * @return the tooltip
     */
    public String getTooltip()
    {
        return myTooltip;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myCommand == null ? 0 : myCommand.hashCode());
        result = prime * result + (myLabel == null ? 0 : myLabel.hashCode());
        return result;
    }

    /**
     * Checks for sub options.
     *
     * @return true, if successful
     */
    public boolean hasSubOptions()
    {
        return mySubOptions != null && !mySubOptions.isEmpty();
    }

    /**
     * Removes the sub option.
     *
     * @param option the option
     * @return true, if successful
     */
    public boolean removeSubOption(MenuOption option)
    {
        boolean removed = false;
        if (mySubOptions != null)
        {
            removed = mySubOptions.remove(option);
        }
        return removed;
    }

    /**
     * Converts this SelectionMenuOption to a JMenuItem. Builds menu with
     * sub-menus if there are sub options.
     *
     * ActionCommands for the JMenuItems are set to the command.
     *
     * @param listener the {@link ActionListener} to add to the menu item and
     *            its sub items.
     * @return the JMenuItem
     */
    public JMenuItem toJMenuItem(ActionListener listener)
    {
        JMenuItem item = null;
        if (hasSubOptions())
        {
            item = new JMenu(myLabel);
            for (MenuOption opt : mySubOptions)
            {
                item.add(opt.toJMenuItem(listener));
            }
        }
        else
        {
            item = new JMenuItem(myLabel);
            item.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            item.setActionCommand(myCommand);
            if (myAccelerator != null)
            {
                item.setAccelerator(myAccelerator);
            }
            if (myTooltip != null)
            {
                item.setToolTipText(myTooltip);
            }
            if (myIcon != null)
            {
                item.setIcon(myIcon);
            }
            if (listener != null)
            {
                item.addActionListener(listener);
            }
        }
        return item;
    }
}
