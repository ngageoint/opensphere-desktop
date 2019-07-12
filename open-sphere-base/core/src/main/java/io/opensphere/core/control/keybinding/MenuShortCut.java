package io.opensphere.core.control.keybinding;

import javafx.beans.property.SimpleStringProperty;

public class MenuShortCut
{

    private final SimpleStringProperty myTopic;

    private final SimpleStringProperty myControl;

    private final SimpleStringProperty myKey;

    public MenuShortCut(String topic, String control, String key)
    {
        this.myTopic = new SimpleStringProperty(topic);
        this.myControl = new SimpleStringProperty(control);
        this.myKey = new SimpleStringProperty(key);
    }

    public String getTopic()
    {
        return myTopic.get();
    }

    public void setTopicName(String topic)
    {
        myTopic.set(topic);
    }

    public String getControl()
    {
        return myControl.get();
    }

    public void setControl(String control)
    {
        myControl.set(control);
    }

    public String getKey()
    {
        return myKey.get();
    }

    public void setKey(String key)
    {
        myKey.set(key);
    }

}