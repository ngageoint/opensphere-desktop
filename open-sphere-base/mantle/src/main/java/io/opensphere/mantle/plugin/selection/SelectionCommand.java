package io.opensphere.mantle.plugin.selection;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.control.action.MenuOption;
import io.opensphere.core.util.collections.New;

/**
 * The Enum SelectionCommand.
 */
public enum SelectionCommand
{
    /** ADD_FEATURES. */
    ADD_FEATURES("Add Features", "Add features in region for active data types"),
    /** ADD_FEATURES_CURRENT_FRAME. */
    ADD_FEATURES_CURRENT_FRAME("Add Features Current Frame",
            "Add features in region for active data types current time frame only"),
    /** The CANCEL_QUERY. */
    CANCEL_QUERY("Cancel Query", "Cancel query from retrieving any more feature data"),
    /** Buffer Line. */
    CREATE_BUFFER_REGION("Create Buffer Region",
            "Create a polygon that buffers an item which can then be used for queries, selections, etc."),
    /** Buffer Line. */
    CREATE_BUFFER_REGION_FOR_SELECTED_SEGMENT("Create Buffer Region For Selected Segment",
            "Create a polygon that buffers the selected line segment, which can be used for queries, selections, etc."),
    /** DESELECT. */
    DESELECT("Deselect All Features", "Deselect all features in region"),
    /** The LOAD_FEATURES. */
    LOAD_FEATURES("Load Features", "Clear all loaded features and load new features in region for active data types"),
    /** The LOAD_FEATURES_CURRENT_FRAMEe. */
    LOAD_FEATURES_CURRENT_FRAME("Load Features Current Frame",
            "Clear all loaded features and load new features in region for active data types current time frame only"),
    /** PURGE. */
    PURGE("Purge area", "Purge all features in region"),
    /** SELECT. */
    SELECT("Select", "Select features in region"),
    /** SELECT_EXCLUSIVE. */
    SELECT_EXCLUSIVE("Select Exclusive", "Select features in region, deselect all other features");

    /** The Constant FILTERS_ACTIVE. */
    private static final String FILTERS_ACTIVE = " (Filters Active)";

    /** The my label. */
    private String myLabel;

    /** The my tooltip. */
    private String myTooltip;

    /**
     * Gets the no geometry menu items.
     *
     * @param al the {@link ActionListener}
     * @return the no geometry menu items
     */
    public static List<JMenuItem> getNoGeometryMenuItems(ActionListener al)
    {
        List<JMenuItem> menuItems = new ArrayList<>();
        menuItems.add(DESELECT.createMenuItem(al));
        menuItems.add(CREATE_BUFFER_REGION.createMenuItem(al));
        return menuItems;
    }

    /**
     * Gets the point menu items.
     *
     * @param al the {@link ActionListener}
     * @param hasFilters Whether there are filters associated with the menu.
     * @return the point menu items
     */
    public static List<JMenuItem> getPointMenuItems(ActionListener al, boolean hasFilters)
    {
        List<JMenuItem> menuItems = new ArrayList<>();
        menuItems.add(CREATE_BUFFER_REGION.createMenuItem(al));
        return menuItems;
    }

    /**
     * Gets the selection region menu items.
     *
     * @param listener A listener interested in actions on the menu items.
     * @param hasFilters Whether there are filters associated with the menu.
     * @param isFromCreateBuffer true when the menu should include the create
     *            buffer option.
     * @return the selection region menu items
     */
    public static List<JMenuItem> getPolygonMenuItems(ActionListener listener, boolean hasFilters, boolean isFromCreateBuffer)
    {
        List<JMenuItem> menuItems = new ArrayList<>();
        menuItems.add(ADD_FEATURES.createMenuItem(listener, hasFilters ? FILTERS_ACTIVE : null));
        menuItems.add(LOAD_FEATURES.createMenuItem(listener, hasFilters ? FILTERS_ACTIVE : null));
        JMenu selectionMU = new JMenu("Selection");
        selectionMU.add(SELECT.createMenuItem(listener));
        selectionMU.add(SELECT_EXCLUSIVE.createMenuItem(listener));
        selectionMU.add(DESELECT.createMenuItem(listener));
        menuItems.add(selectionMU);
        if (!isFromCreateBuffer)
        {
            menuItems.add(CREATE_BUFFER_REGION.createMenuItem(listener));
        }
        menuItems.add(PURGE.createMenuItem(listener));
        return menuItems;
    }

    /**
     * Gets the polyline menu items.
     *
     * @param al the {@link ActionListener}
     * @param hasFilters Whether there are filters associated with the menu.
     * @return the polyline menu items
     */
    public static List<JMenuItem> getPolylineMenuItems(ActionListener al, boolean hasFilters)
    {
        List<JMenuItem> menuItems = new ArrayList<>();
        menuItems.add(CREATE_BUFFER_REGION.createMenuItem(al));
        return menuItems;
    }

    /**
     * Gets the selection region menu items.
     *
     * @param al the {@link ActionListener} to add to all the menu items that
     *            are created by this call.
     * @param hasFilters Whether there are filters associated with the menu.
     * @return the selection region menu items
     */
    public static List<JMenuItem> getQueryRegionMenuItems(ActionListener al, boolean hasFilters)
    {
        List<JMenuItem> menuItems = new ArrayList<>();
        menuItems.add(ADD_FEATURES.createMenuItem(al, hasFilters ? FILTERS_ACTIVE : null));
        menuItems.add(LOAD_FEATURES.createMenuItem(al, hasFilters ? FILTERS_ACTIVE : null));
        JMenu selectionMU = new JMenu("Selection");
        selectionMU.add(SELECT.createMenuItem(al));
        selectionMU.add(SELECT_EXCLUSIVE.createMenuItem(al));
        selectionMU.add(DESELECT.createMenuItem(al));
        menuItems.add(selectionMU);
        menuItems.add(CREATE_BUFFER_REGION.createMenuItem(al));
        menuItems.add(PURGE.createMenuItem(al));
        menuItems.add(CANCEL_QUERY.createMenuItem(al));
        return menuItems;
    }

    /**
     * Gets the selection region menu items.
     *
     * @param listener A listener interested in actions on the menu items.
     * @param hasFilters Whether there are filters associated with the menu.
     * @return the selection region menu items
     */
    public static List<JMenuItem> getRoiMenuItems(ActionListener listener, boolean hasFilters)
    {
        List<JMenuItem> menuOpts = New.list();
        menuOpts.add(LOAD_FEATURES.createMenuItem(listener, hasFilters ? FILTERS_ACTIVE : null));
        menuOpts.add(ADD_FEATURES.createMenuItem(listener, hasFilters ? FILTERS_ACTIVE : null));
        menuOpts.add(PURGE.createMenuItem(listener));
        menuOpts.add(SELECT.createMenuItem(listener));
        menuOpts.add(DESELECT.createMenuItem(listener));
        return menuOpts;
    }

    /**
     * Gets the selection region menu items.
     *
     * @param listener A listener interested in actions on the menu items.
     * @param hasFilters Whether there are filters associated with the menu.
     * @return the selection region menu items
     */
    public static List<JMenuItem> getSelectionRegionMenuItems(ActionListener listener, boolean hasFilters)
    {
        List<JMenuItem> menuOpts = New.list();
        menuOpts.add(ADD_FEATURES.createMenuItem(listener, hasFilters ? FILTERS_ACTIVE : null));
        menuOpts.add(LOAD_FEATURES.createMenuItem(listener, hasFilters ? FILTERS_ACTIVE : null));
        JMenu selectionMU = new JMenu("Selection");
        selectionMU.add(SELECT.createMenuItem(listener));
        selectionMU.add(SELECT_EXCLUSIVE.createMenuItem(listener));
        selectionMU.add(DESELECT.createMenuItem(listener));
        menuOpts.add(selectionMU);
        menuOpts.add(PURGE.createMenuItem(listener));
        return menuOpts;
    }

    /**
     * Instantiates a new command.
     *
     * @param label the label
     * @param toolTip the tool tip
     */
    SelectionCommand(String label, String toolTip)
    {
        myLabel = label;
        myTooltip = toolTip;
    }

    /**
     * Creates the selection menu option.
     *
     * @return the selection menu option
     */
    public MenuOption createMenuOption()
    {
        MenuOption smo = new MenuOption(getLabel(), toString(), getTooltip());
        return smo;
    }

    /**
     * Creates the menu option.
     *
     * @param labelAppend the label append
     * @return the menu option
     */
    public MenuOption createMenuOption(String labelAppend)
    {
        StringBuilder lb = new StringBuilder();
        lb.append(getLabel());
        if (!StringUtils.isBlank(labelAppend))
        {
            lb.append(labelAppend);
        }
        MenuOption smo = new MenuOption(lb.toString(), toString(), getTooltip());
        return smo;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel()
    {
        return myLabel;
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

    /**
     * Creates the menu item.
     *
     * @param al the the {@link ActionListener} to add to all the menu items
     *            that are created by this call.
     * @return the j menu item
     */
    private JMenuItem createMenuItem(ActionListener al)
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
    private JMenuItem createMenuItem(ActionListener al, String labelAppend)
    {
        StringBuilder lb = new StringBuilder();
        lb.append(getLabel());
        if (!StringUtils.isBlank(labelAppend))
        {
            lb.append(labelAppend);
        }
        JMenuItem jmi = new JMenuItem(lb.toString());
        jmi.setToolTipText(getTooltip());
        jmi.setActionCommand(toString());
        if (al != null)
        {
            jmi.addActionListener(al);
        }
        return jmi;
    }
}
