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
    private final SimpleStringProperty myTopic;

    /**
     * The action the shortcut executes.
     */
    private final SimpleStringProperty myControl;

    /**
     * The physical key the shortcut is mapped onto.
     */
    private final SimpleStringProperty myKey;

    /**
     * @param topic the category of the shortcut.
     * @param control the action the shortcut executes.
     * @param key the physical key mapped.
     */
    public MenuShortCut(String topic, String control, String key)
    {
        this.myTopic = new SimpleStringProperty(topic);
        this.myControl = new SimpleStringProperty(control);
        this.myKey = new SimpleStringProperty(key);
    }

    /**
     * @return myTopic gets the category of the shortcut.
     */
    public String getTopic()
    {
        return myTopic.get();
    }

    /**
     * @param topic sets the category of the shortcut.
     */
    public void setTopicName(String topic)
    {
        myTopic.set(topic);
    }

    /**
     * @return myControl gets the action performed.
     */
    public String getControl()
    {
        return myControl.get();
    }

    /**
     * @param control sets the action performed.
     */
    public void setControl(String control)
    {
        myControl.set(control);
    }

    /**
     * @return myKey the physical key mapped.
     */
    public String getKey()
    {
        return myKey.get();
    }

    /**
     * @param key sets the physical key mapped.
     */
    public void setKey(String key)
    {
        myKey.set(key);
    }

}