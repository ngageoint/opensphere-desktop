package io.opensphere.core.control.keybinding;

import javafx.beans.property.SimpleStringProperty;

/**
 * Holds the information for entries within a JavaFX tableview used in the "Menu
 * Shortcuts" tab of the "Help" -> "Key Map" Menu.
 */
public class MenuShortCut
{

    /**
     * The category of the shortcut.
     */
    private final SimpleStringProperty myMenu;

    /**
     * The action the shortcut executes.
     */
    private final SimpleStringProperty myItem;

    /**
     * The physical key the shortcut is mapped onto.
     */
    private final SimpleStringProperty myShortcut;

    /**
     * Instantiates a MenuShortCut object and sets its default properties.
     * 
     * @param menu the category of the shortcut.
     * @param item the action the shortcut executes.
     * @param shortcut the physical key mapped.
     */
    public MenuShortCut(String menu, String item, String shortcut)
    {
        this.myMenu = new SimpleStringProperty(menu);
        this.myItem = new SimpleStringProperty(item);
        this.myShortcut = new SimpleStringProperty(shortcut);
    }

    /**
     * Gets the menu property.
     * 
     * @return category of the shortcut.
     */
    public String getMenu()
    {
        return myMenu.get();
    }

    /**
     * Sets the menu property.
     * 
     * @param sets the category of the shortcut.
     */
    public void setMenuName(String menu)
    {
        myMenu.set(menu);
    }

    /**
     * Gets the item property.
     * 
     * @return the action performed.
     */
    public String getItem()
    {
        return myItem.get();
    }

    /**
     * Sets the item property.
     * 
     * @param Item sets the action performed.
     */
    public void setItem(String item)
    {
        myItem.set(item);
    }

    /**
     * Gets the key mapped property.
     * 
     * @return the physical key mapped.
     */
    public String getShortcut()
    {
        return myShortcut.get();
    }

    /**
     * Sets the shortcut key property.
     * 
     * @param Shortcut sets the physical key mapped.
     */
    public void setShortcut(String shortcut)
    {
        myShortcut.set(shortcut);
    }
}
