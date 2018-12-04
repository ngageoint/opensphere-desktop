package io.opensphere.mantle.plugin.selection.impl;

import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.control.action.MenuOption;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.plugin.selection.SelectionCommand;
import io.opensphere.mantle.plugin.selection.SelectionCommandGroup;

/**
 * Abstract base implemention of a {@link SelectionCommand}.
 */
public abstract class AbstractSelectionCommand implements SelectionCommand
{
    /** The name of the command. */
    private final String myName;

    /** The Icon associated with the menu. */
    private final GenericFontIcon myIcon;

    /** The group to which the command belongs. */
    private final SelectionCommandGroup myGroup;

    /** The my label. */
    private final String myLabel;

    /** The my tooltip. */
    private final String myTooltip;

    /**
     * Instantiates a new command.
     *
     * @param name The name of the command.
     * @param label the label shown for the command.
     * @param toolTip the tool tip shown for the command.
     * @param group the group to which the command belongs.
     */
    public AbstractSelectionCommand(String name, String label, String toolTip, SelectionCommandGroup group)
    {
        this(name, label, toolTip, group, null);
    }

    /**
     * Instantiates a new command.
     *
     * @param name The name of the command.
     * @param label the label shown for the command.
     * @param toolTip the tool tip shown for the command.
     * @param group the group to which the command belongs.
     * @param icon the icon shown for the command (may be null).
     */
    public AbstractSelectionCommand(String name, String label, String toolTip, SelectionCommandGroup group, GenericFontIcon icon)
    {
        myName = name;
        myLabel = label;
        myTooltip = toolTip;
        myGroup = group;
        myIcon = icon;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.selection.SelectionCommand#getName()
     */
    @Override
    public String getName()
    {
        return myName;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.selection.SelectionCommand#createMenuOption()
     */
    @Override
    public MenuOption createMenuOption()
    {
        return new MenuOption(getLabel(), toString(), getTooltip(), getIcon());
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.selection.SelectionCommand#createMenuOption(java.lang.String)
     */
    @Override
    public MenuOption createMenuOption(String labelAppend)
    {
        StringBuilder lb = new StringBuilder();
        lb.append(getLabel());
        if (!StringUtils.isBlank(labelAppend))
        {
            lb.append(labelAppend);
        }
        MenuOption smo = new MenuOption(lb.toString(), toString(), getTooltip(), getIcon());
        return smo;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.selection.SelectionCommand#getLabel()
     */
    @Override
    public String getLabel()
    {
        return myLabel;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.selection.SelectionCommand#getTooltip()
     */
    @Override
    public String getTooltip()
    {
        return myTooltip;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.selection.SelectionCommand#getIcon()
     */
    @Override
    public GenericFontIcon getIcon()
    {
        return myIcon;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.selection.SelectionCommand#getGroup()
     */
    @Override
    public SelectionCommandGroup getGroup()
    {
        return myGroup;
    }

    /**
     * Creates the menu item.
     *
     * @param al the the {@link ActionListener} to add to all the menu items
     *            that are created by this call.
     * @return the j menu item
     */
    @Override
    public JMenuItem createMenuItem(ActionListener al)
    {
        return createMenuItem(al, null);
    }

    /**
     * Creates the menu item.
     *
     * @param al the the {@link ActionListener} to add to all the menu items
     *            that are created by this call.
     * @param labelAppend the label append
     * @return the j menu item
     */
    @Override
    public JMenuItem createMenuItem(ActionListener al, String labelAppend)
    {
        StringBuilder lb = new StringBuilder();
        lb.append(getLabel());
        if (!StringUtils.isBlank(labelAppend))
        {
            lb.append(labelAppend);
        }
        JMenuItem jmi = new JMenuItem(lb.toString());
        jmi.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        if (getIcon() != null)
        {
            jmi.setIcon(getIcon());
        }
        jmi.setToolTipText(getTooltip());
        jmi.setActionCommand(getName());
        if (al != null)
        {
            jmi.addActionListener(al);
        }
        return jmi;
    }
}
