package io.opensphere.mantle.plugin.selection;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.plugin.selection.impl.AddFeaturesCommand;
import io.opensphere.mantle.plugin.selection.impl.AddFeaturesCurrentFrame;
import io.opensphere.mantle.plugin.selection.impl.CancelQueryCommand;
import io.opensphere.mantle.plugin.selection.impl.CreateBufferCommand;
import io.opensphere.mantle.plugin.selection.impl.CreateBufferSelectedCommand;
import io.opensphere.mantle.plugin.selection.impl.DeselectCommand;
import io.opensphere.mantle.plugin.selection.impl.LoadFeaturesCommand;
import io.opensphere.mantle.plugin.selection.impl.LoadFeaturesCurrentFrameCommand;
import io.opensphere.mantle.plugin.selection.impl.RemoveAllFeaturesCommand;
import io.opensphere.mantle.plugin.selection.impl.SelectCommand;
import io.opensphere.mantle.plugin.selection.impl.SelectExclusiveCommand;

/**
 * A factory implementation for selection command implementations.
 */
public class SelectionCommandFactory
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SelectionCommandFactory.class);

    /** The Constant FILTERS_ACTIVE. */
    public static final String FILTERS_ACTIVE = " (Filters Active)";

    /** ADD_FEATURES. */
    public static final SelectionCommand ADD_FEATURES = new AddFeaturesCommand();

    /** ADD_FEATURES_CURRENT_FRAME. */
    public static final SelectionCommand ADD_FEATURES_CURRENT_FRAME = new AddFeaturesCurrentFrame();

    /** The CANCEL_QUERY. */
    public static final SelectionCommand CANCEL_QUERY = new CancelQueryCommand();

    /** Buffer Line. */
    public static final SelectionCommand CREATE_BUFFER_REGION = new CreateBufferCommand();

    /** Buffer Line. */
    public static final SelectionCommand CREATE_BUFFER_REGION_FOR_SELECTED = new CreateBufferSelectedCommand();

    /** DESELECT. */
    public static final SelectionCommand DESELECT = new DeselectCommand();

    /** The LOAD_FEATURES. */
    public static final SelectionCommand LOAD_FEATURES = new LoadFeaturesCommand();

    /** The LOAD_FEATURES_CURRENT_FRAMEe. */
    public static final SelectionCommand LOAD_FEATURES_CURRENT_FRAME = new LoadFeaturesCurrentFrameCommand();

    /** PURGE. */
    public static final SelectionCommand REMOVE_ALL = new RemoveAllFeaturesCommand();

    /** SELECT. */
    public static final SelectionCommand SELECT = new SelectCommand();

    /** SELECT_EXCLUSIVE. */
    public static final SelectionCommand SELECT_EXCLUSIVE = new SelectExclusiveCommand();

    /**
     * Default set of commands.
     */
    private static final List<SelectionCommand> DEFAULT_COMMANDS = List.of(ADD_FEATURES, ADD_FEATURES_CURRENT_FRAME,
            LOAD_FEATURES, LOAD_FEATURES_CURRENT_FRAME, CANCEL_QUERY, CREATE_BUFFER_REGION,
            CREATE_BUFFER_REGION_FOR_SELECTED, DESELECT, REMOVE_ALL, SELECT, SELECT_EXCLUSIVE);

    /**
     * Default set of commands.
     */
    private static final Map<String, SelectionCommand> DEFAULT_COMMAND_MAP = DEFAULT_COMMANDS.stream()
            .collect(Collectors.toMap(c -> c.getName(), c -> c));

    /**
     * Gets the selection command from its string action command counterpart.
     *
     * @param name the command to convert to a {@link SelectionCommand}
     * @return the selection command or null if not valid.
     */
    public static SelectionCommand getSelectionCommand(String name)
    {
        if (DEFAULT_COMMAND_MAP.containsKey(name))
        {
            return DEFAULT_COMMAND_MAP.get(name);
        }
        // Unknown command returned.
        LOGGER.warn("Illegal Selection Command Recieved: " + name);
        return null;
    }

    /**
     * Gets the no geometry menu items.
     *
     * @param al the {@link ActionListener}
     * @return the no geometry menu items
     */
    public static List<Component> getNoGeometryMenuItems(ActionListener al)
    {
        List<Component> menuItems = new ArrayList<>();
        menuItems.add(DESELECT.createMenuItem(al));
        menuItems.add(CREATE_BUFFER_REGION.createMenuItem(al));
        return menuItems;
    }

    /**
     * Gets the point menu items.
     *
     * @param al the {@link ActionListener}
     * @return the point menu items
     */
    public static List<Component> getPointMenuItems(ActionListener al)
    {
        List<Component> menuItems = new ArrayList<>();
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
    public static List<Component> getPolygonMenuItems(ActionListener listener, boolean hasFilters, boolean isFromCreateBuffer)
    {
        List<Component> menuItems = new ArrayList<>();
        menuItems.add(createHeader("QUERY"));
        menuItems.add(ADD_FEATURES.createMenuItem(listener, hasFilters ? FILTERS_ACTIVE : null));
        menuItems.add(LOAD_FEATURES.createMenuItem(listener, hasFilters ? FILTERS_ACTIVE : null));
        menuItems.add(CANCEL_QUERY.createMenuItem(listener));
        menuItems.add(createHeader("FEATURES"));
        menuItems.add(DESELECT.createMenuItem(listener));
        menuItems.add(REMOVE_ALL.createMenuItem(listener));
        menuItems.add(SELECT.createMenuItem(listener));
        menuItems.add(SELECT_EXCLUSIVE.createMenuItem(listener));
        if (!isFromCreateBuffer)
        {
            menuItems.add(createHeader("TOOLS"));
            menuItems.add(CREATE_BUFFER_REGION.createMenuItem(listener));
            menuItems.add(CREATE_BUFFER_REGION_FOR_SELECTED.createMenuItem(listener));
        }
        return menuItems;
    }

    /**
     * Gets the polyline menu items.
     *
     * @param al the {@link ActionListener}
     * @return the polyline menu items
     */
    public static List<Component> getPolylineMenuItems(ActionListener al)
    {
        List<Component> menuItems = new ArrayList<>();
        menuItems.add(CREATE_BUFFER_REGION.createMenuItem(al));
        menuItems.add(CREATE_BUFFER_REGION_FOR_SELECTED.createMenuItem(al));
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
    public static List<Component> getQueryRegionMenuItems(ActionListener al, boolean hasFilters)
    {
        List<Component> menuItems = new ArrayList<>();
        menuItems.add(createHeader("QUERY"));
        menuItems.add(ADD_FEATURES.createMenuItem(al, hasFilters ? FILTERS_ACTIVE : null));
        menuItems.add(LOAD_FEATURES.createMenuItem(al, hasFilters ? FILTERS_ACTIVE : null));
        menuItems.add(CANCEL_QUERY.createMenuItem(al));
        menuItems.add(createHeader("FEATURES"));
        menuItems.add(DESELECT.createMenuItem(al));
        menuItems.add(REMOVE_ALL.createMenuItem(al));
        menuItems.add(SELECT.createMenuItem(al));
        menuItems.add(SELECT_EXCLUSIVE.createMenuItem(al));
        menuItems.add(createHeader("TOOLS"));
        menuItems.add(CREATE_BUFFER_REGION.createMenuItem(al));
        return menuItems;
    }

    /**
     * Gets the selection region menu items.
     *
     * @param listener A listener interested in actions on the menu items.
     * @param hasFilters Whether there are filters associated with the menu.
     * @return the selection region menu items
     */
    public static List<Component> getRoiMenuItems(ActionListener listener, boolean hasFilters)
    {
        List<Component> menuOpts = New.list();
        menuOpts.add(createHeader("QUERY"));
        menuOpts.add(LOAD_FEATURES.createMenuItem(listener, hasFilters ? FILTERS_ACTIVE : null));
        menuOpts.add(ADD_FEATURES.createMenuItem(listener, hasFilters ? FILTERS_ACTIVE : null));
        menuOpts.add(createHeader("FEATURES"));
        menuOpts.add(DESELECT.createMenuItem(listener));
        menuOpts.add(REMOVE_ALL.createMenuItem(listener));
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
    public static List<Component> getSelectionRegionMenuItems(ActionListener listener, boolean hasFilters)
    {
        List<Component> menuOpts = New.list();
        menuOpts.add(createHeader("QUERY"));
        menuOpts.add(ADD_FEATURES.createMenuItem(listener, hasFilters ? FILTERS_ACTIVE : null));
        menuOpts.add(LOAD_FEATURES.createMenuItem(listener, hasFilters ? FILTERS_ACTIVE : null));
        menuOpts.add(createHeader("FEATURES"));
        menuOpts.add(DESELECT.createMenuItem(listener));
        menuOpts.add(REMOVE_ALL.createMenuItem(listener));
        menuOpts.add(SELECT.createMenuItem(listener));
        menuOpts.add(SELECT_EXCLUSIVE.createMenuItem(listener));
        return menuOpts;
    }

    /**
     * Creates a menu header as a JLabel using the supplied text.
     *
     * @param title the title to apply to the header.
     * @return a {@link Component} in which the supplied text is rendered as a
     *         header.
     */
    private static JLabel createHeader(String title)
    {
        JLabel e = new JLabel(title);
        e.setFont(e.getFont().deriveFont(Font.BOLD));
        return e;
    }

    /**
     * Gets a collection of the known set of default commands.
     *
     * @return a collection of the known set of default commands.
     */
    public static Collection<SelectionCommand> getAllCommands()
    {
        return DEFAULT_COMMANDS;
    }
}
