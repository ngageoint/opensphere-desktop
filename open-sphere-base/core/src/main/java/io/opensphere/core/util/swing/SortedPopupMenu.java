package io.opensphere.core.util.swing;

import java.util.Arrays;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import io.opensphere.core.util.collections.New;

/**
 * The popup menu will contain sorted JMenuItems.
 */
public class SortedPopupMenu extends JPopupMenu
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Menu items. */
    private final Map<String, JMenuItem> myMenuItemSet;

    /**
     * Instantiates a new sorted popup menu.
     */
    public SortedPopupMenu()
    {
        super();
        myMenuItemSet = New.map();
    }

    @Override
    public JMenuItem add(JMenuItem menuItem)
    {
        addMenuItem(menuItem);
        return menuItem;
    }

    @Override
    public JMenuItem add(String string)
    {
        JMenuItem item = new JMenuItem(string);
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
        if (!myMenuItemSet.containsKey(menuItem.getText()))
        {
            myMenuItemSet.put(menuItem.getText(), menuItem);
            Object[] keys = myMenuItemSet.keySet().toArray();
            Arrays.sort(keys);
            removeAll();
            for (int i = 0; i < keys.length; i++)
            {
                super.add(myMenuItemSet.get(keys[i]));
            }
        }
    }
}
