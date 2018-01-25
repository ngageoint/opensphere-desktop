package io.opensphere.core.control.ui;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import io.opensphere.core.util.taskactivity.TaskActivity;

/**
 * Registry for menu bars in the application. This allows components to get
 * references to the main menu bar or to menu bars potentially created by other
 * components in the system.
 */
public interface MenuBarRegistry
{
    /** The main menu bar on the primary window. */
    String MAIN_MENU_BAR = "Main Menu Bar";

    /** Name for the file menu on the main menu bar. */
    String FILE_MENU = "File";

    /** Name for the help menu on the main menu bar. */
    String HELP_MENU = "Help";

    /** Name for the edit menu on the main menu bar. */
    String EDIT_MENU = "Edit";

    /** Name for the view menu on the main menu bar. */
    String VIEW_MENU = "View";

    /** Name for the tools menu on the main menu bar. */
    String TOOLS_MENU = "Tools";

    /** Name for the data control menu on the main menu bar. */
    String DATA_CONTROL_MENU = "Data Control";

    /** Name for the debug menu on the main menu bar. */
    String DEBUG_MENU = "Debug";

    /** Name for the quit menu item. */
    String QUIT_MENU_ITEM = "Quit";

    /** The name of the projection menu. */
    String PROJECTION_MENU = "Projection";

    /** Name for the map overlays menu item. */
    String OVERLAYS_MENU = "Map Overlays";

    /** Name for the controls menu item. */
    String CONTROLS_MENU = "Controls";

    /**
     * Add a {@link TaskActivity} to the main menu bar. A message with a busy
     * spinner the the upper right hand corner of the display. Any number of
     * TaskActivity messages may be active at one time, eventually the label
     * will marquis scroll.
     *
     * Note that this task activity will be removed automatically as soon as the
     * TaskActivity.isComplete() returns true.
     *
     * @param ta - the task activity to add to the menu bar
     */
    void addTaskActivity(TaskActivity ta);

    /**
     * Add a {@link TaskActivity} to the main menu bar and runs the given
     * runnable. When the runnable completes the task activity is cancelled. See
     * {@link #addTaskActivity(TaskActivity)}.
     *
     * @param label the label to display
     * @param runnable the runnable to run while displaying the task activity
     */
    void addTaskActivity(String label, Runnable runnable);

    /**
     * De-register the menu bar for the given name.
     *
     * @param menuBarName The name of the menu bar.
     */
    void deregisterMenuBar(String menuBarName);

    /**
     * Convenience method to get the main menu bar. This just calls
     * {@link #getMenuBar(String)} with {@link #MAIN_MENU_BAR} as the argument.
     *
     * @return The main menu bar.
     */
    JMenuBar getMainMenuBar();

    /**
     * Get a reference to a menu. If the menu does not exist, it will be
     * created.
     * <p>
     * For example, to get the main tools menu:
     *
     * <pre>
     * <code>
     *    menuBarRegistry.getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.TOOLS_MENU)
     * </code>
     * </pre>
     *
     * @param menuBarName The name of the menu bar that contains the menu, e.g.,
     *            {@link #MAIN_MENU_BAR}.
     * @param names The path to the menu starting with the top-level menu name
     *            and ending with the desired menu name.
     * @return The named menu.
     */
    JMenu getMenu(String menuBarName, String... names);

    /**
     * Get the menu bar with the given name.
     *
     * @param menuBarName The name of the menu bar.
     * @return The menu bar with the given name, or <code>null</code> if it was
     *         not found.
     */
    JMenuBar getMenuBar(String menuBarName);

    /**
     * Register the menu bar for the given name.
     *
     * @param menuBarName The name of the menu bar.
     * @param menuBar The menu bar to be associated with menuBarName.
     */
    void registerMenuBar(String menuBarName, JMenuBar menuBar);

    /**
     * Removes a task activity message from the main menu bar.
     *
     * @param ta - the TaskActivity to remove.
     */
    void removeTaskActivity(TaskActivity ta);
}
