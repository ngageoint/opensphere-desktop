package io.opensphere.core.util.swing;

import java.util.Arrays;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import io.opensphere.core.util.collections.New;

/**
 * The menu will contain sorted JMenuItems.
 */
public class SortedJMenu extends JMenu
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Menu items. */
    private final Map<String, JMenuItem> myMenuItems;

    /**
     * Instantiates a new sorted popup menu.
     */
    public SortedJMenu()
    {
        super();
        myMenuItems = New.map();
    }

    /**
     * Instantiates a new sorted popup menu.
     *
     * @param s the text for the menu label
     */
    public SortedJMenu(String s)
    {
        super(s);
        myMenuItems = New.map();
    }

    @Override
    public JMenuItem add(JMenuItem item)
    {
        addMenuItem(item);
        return item;
    }

    @Override
    public JMenuItem add(String s)
    {
        JMenuItem item = new JMenuItem(s);
        addMenuItem(item);
        return item;
    }

    /**
     * Adds the menu item.
     *
     * @param menuItem the menu item
     */
    public void addMenuItem(JMenuItem menuItem)
    {
        if (!myMenuItems.containsKey(menuItem.getText()))
        {
            myMenuItems.put(menuItem.getText(), menuItem);
            Object[] keys = myMenuItems.keySet().toArray();
            Arrays.sort(keys);
            removeAll();
            for (int i = 0; i < keys.length; i++)
            {
                super.add(myMenuItems.get(keys[i]));
            }
        }
    }
}
