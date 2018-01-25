package io.opensphere.featureactions.editor.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A single group of feature actions.
 */
public class SimpleFeatureActionGroup
{
    /**
     * The actions that are contained in this group.
     */
    private final ObservableList<SimpleFeatureAction> myActions = FXCollections.observableArrayList();

    /**
     * The name of the group.
     */
    private final StringProperty myGroupName = new SimpleStringProperty("Feature Actions");

    /**
     * The actions that are contained in this group.
     *
     * @return The list of actions.
     */
    public ObservableList<SimpleFeatureAction> getActions()
    {
        return myActions;
    }

    /**
     * Gets the name of the group.
     *
     * @return The group name.
     */
    public String getGroupName()
    {
        return myGroupName.get();
    }

    /**
     * Gets the group name property.
     *
     * @return The group name property.
     */
    public StringProperty groupNameProperty()
    {
        return myGroupName;
    }

    /**
     * Sets the name of the group.
     *
     * @param groupName The new group name.
     */
    public void setGroupName(String groupName)
    {
        myGroupName.set(groupName);
    }
}
