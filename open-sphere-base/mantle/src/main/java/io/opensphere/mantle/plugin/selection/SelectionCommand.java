package io.opensphere.mantle.plugin.selection;

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import io.opensphere.core.control.action.MenuOption;
import io.opensphere.core.util.swing.GenericFontIcon;

/**
 * The Enum SelectionCommand.
 */
public interface SelectionCommand
{
    /**
     * Creates the selection menu option.
     *
     * @return the selection menu option
     */
    public MenuOption createMenuOption();

    /**
     * Creates the menu option.
     *
     * @param labelAppend the label append
     * @return the menu option
     */
    public MenuOption createMenuOption(String labelAppend);

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getName();

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel();

    /**
     * Gets the tooltip.
     *
     * @return the tooltip
     */
    public String getTooltip();

    /**
     * Gets the value of the icon field.
     *
     * @return the value stored in the icon field.
     */
    public GenericFontIcon getIcon();

    /**
     * Gets the value of the group field.
     *
     * @return the value stored in the group field.
     */
    public SelectionCommandGroup getGroup();

    /**
     * Creates the menu item.
     *
     * @param listener the the {@link ActionListener} to add to all the menu
     *            items that are created by this call.
     * @return the j menu item
     */
    public JMenuItem createMenuItem(ActionListener listener);

    /**
     * Creates the menu item.
     *
     * @param listener the the {@link ActionListener} to add to all the menu
     *            items that are created by this call.
     * @param labelAppend the label append
     * @return the j menu item
     */
    public JMenuItem createMenuItem(ActionListener listener, String labelAppend);
}
