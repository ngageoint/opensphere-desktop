package io.opensphere.mantle.iconproject.model;

import java.util.Set;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * This model is used to track the collection and subcollection choices the user
 * makes when importing icons from file or folder.
 */
public class ImportProp
{
    /** The Collection Name. */
    private final StringProperty myCollectionName = new SimpleStringProperty("Default");

    /** The SubCollection List. */
    private Set<String> mySubCollectionList;

    /** The SubCollection Name. */
    private final StringProperty mySubCollectionName = new SimpleStringProperty("");

    /**
     * Gets the unique SubCollection chosen by the user during icon importation.
     *
     * @return the chosen SubCollection Name.
     */
    public StringProperty getSubCollectionName()
    {
        return mySubCollectionName;
    }

    /**
     * Gets the list of SubCollection names.
     *
     * @return the List of SubCollection names that are
     *         attached to {@link #myCollectionName}.
     */
    public Set<String> getSubCollectionList()
    {
        return mySubCollectionList;
    }

    /**
     * Sets the SubCollection Names that come from the parent.
     * {@link #myCollectionName.}
     *
     * @param the List containing the sub collection names
     */
    public void setSubCollectionList(Set<String> subCollectionList)
    {
        mySubCollectionList = subCollectionList;
    }

    /**
     * Returns one unique collection name, chosen or created by the User.
     *
     * @return the collection name
     */
    public StringProperty getCollectionName()
    {
        return myCollectionName;
    }
}
