package io.opensphere.core.control.ui.impl;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.SortedJMenu;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.core.util.taskactivity.TaskActivityPanel;

/**
 * Menu bar registry implementation.
 *
 * TODO: Change this to keep track of the source of the menu items so they can
 * be cleaned up if the source is deactivated.
 */
public class MenuBarRegistryImpl implements MenuBarRegistry
{
    /** Map of menu bar names to menu bars. */
    private final Map<String, JMenuBar> myMenuBars = new ConcurrentHashMap<>();

    /** Task Activity Panel. */
    private TaskActivityPanel myTaskActivityPanel;

    /**
     * Build the main menu bar.
     *
     * @return The main menu bar.
     */
    private static JMenuBar buildMainMenuBar()
    {
        assert SwingUtilities.isEventDispatchThread();

        JMenuBar toReturn = new JMenuBar();
        toReturn.setLayout(new BoxLayout(toReturn, BoxLayout.X_AXIS));

        JMenu fileMenu = new JMenu(FILE_MENU);
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenu editMenu = new SortedJMenu(EDIT_MENU);
        editMenu.setMnemonic(KeyEvent.VK_E);
        JMenu viewMenu = new JMenu(VIEW_MENU);
        viewMenu.setMnemonic(KeyEvent.VK_V);
        JMenu toolsMenu = new SortedJMenu(TOOLS_MENU);
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        JMenu dataControlMenu = new JMenu(DATA_CONTROL_MENU);
        dataControlMenu.setMnemonic(KeyEvent.VK_C);
        JMenu helpMenu = new JMenu(HELP_MENU);
        helpMenu.setMnemonic(KeyEvent.VK_H);

        toReturn.add(fileMenu);
        toReturn.add(editMenu);
        toReturn.add(viewMenu);
        toReturn.add(toolsMenu);
        toReturn.add(dataControlMenu);
        toReturn.add(helpMenu);

        return toReturn;
    }

    @Override
    public void addTaskActivity(TaskActivity ta)
    {
        getTaskActivityPanel().addTaskActivity(ta);
    }

    @Override
    public void addTaskActivity(String label, Runnable runnable)
    {
        TaskActivity activity = TaskActivity.createActive(label);

        addTaskActivity(activity);

        try
        {
            runnable.run();
        }
        finally
        {
            activity.setComplete(true);
        }
    }

    @Override
    public void deregisterMenuBar(String menuBarName)
    {
        myMenuBars.remove(menuBarName);
    }

    /**
     * Convenience method to get the main menu bar. This just calls
     * {@link #getMenuBar(String)} with {@link #MAIN_MENU_BAR} as the argument.
     *
     * @return The main menu bar.
     */
    @Override
    public JMenuBar getMainMenuBar()
    {
        assert SwingUtilities.isEventDispatchThread();

        return getMenuBar(MAIN_MENU_BAR);
    }

    @Override
    public JMenu getMenu(String menuBarName, String... names)
    {
        assert SwingUtilities.isEventDispatchThread();

        Container container = getMenuBar(menuBarName);
        if (container == null)
        {
            throw new IllegalArgumentException(
                    "No menu bar with the name [" + menuBarName + "] exists. Possible values are: " + myMenuBars.keySet());
        }
        JMenu component = null;
        for (String name : names)
        {
            Component[] components = container instanceof JMenu ? ((JMenu)container).getMenuComponents()
                    : container.getComponents();
            component = null;
            for (Component child : components)
            {
                if (child instanceof JMenu && ((AbstractButton)child).getText().equals(name))
                {
                    component = (JMenu)child;
                    break;
                }
            }
            if (component == null)
            {
                component = new JMenu(name);
                container.add(component);
            }
            container = component;
        }
        return component;
    }

    /**
     * Get the menu bar with the given name.
     *
     * @param menuBarName The name of the menu bar.
     * @return The menu bar with the given name, or <code>null</code> if it was
     *         not found.
     */
    @Override
    public JMenuBar getMenuBar(String menuBarName)
    {
        assert SwingUtilities.isEventDispatchThread();

        JMenuBar menuBar = myMenuBars.get(menuBarName);
        if (menuBar == null && MAIN_MENU_BAR.equals(menuBarName))
        {
            JMenuBar mainMenuBar = buildMainMenuBar();
            myMenuBars.put(MAIN_MENU_BAR, mainMenuBar);
            menuBar = mainMenuBar;
            menuBar.setMinimumSize(new Dimension(500, 24));
        }

        return menuBar;
    }

    @Override
    public void registerMenuBar(String menuBarName, JMenuBar menuBar)
    {
        myMenuBars.put(menuBarName, menuBar);
    }

    @Override
    public void removeTaskActivity(TaskActivity ta)
    {
        getTaskActivityPanel().removeTaskActivity(ta);
    }

    /**
     * Lazily create the TaskActivityPanel for the menu bar.
     *
     * @return the TaskActivityPanel
     */
    private synchronized TaskActivityPanel getTaskActivityPanel()
    {
        if (myTaskActivityPanel == null)
        {
            myTaskActivityPanel = new TaskActivityPanel();
            EventQueueUtilities.runOnEDT(() -> getMainMenuBar().add(myTaskActivityPanel));
        }
        return myTaskActivityPanel;
    }
}
