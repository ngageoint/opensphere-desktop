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
    private StringProperty myCollectionName = new SimpleStringProperty("Default");

    /** The SubCollection List. */
    private Set<String> mySubCollectionList;

    /** The SubCollection Name. */
    private StringProperty mySubCollectionName = new SimpleStringProperty("");

    /**
     * Gets the unique SubCollection chosen by the user during icon importation.
     *
     * @return mySubCollectionName the chosen SubCollection Name.
     */
    public StringProperty getSubCollectionName()
    {
        return mySubCollectionName;
    }

    /**
     * Gets the list of SubCollection names.
     *
     * @return mySubCollectionList the List of SubCollection Names that are
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
     * @param theSubCollectionList the List containing the sub collection names
     *            as strings.
     */
    public void setSubCollectionList(Set<String> theSubCollectionList)
    {
        mySubCollectionList = theSubCollectionList;
    }

    /**
     * Returns one unique collection name, chosen or created by the User.
     *
     * @return myCollectionName
     */
    public StringProperty getCollectionName()
    {
        return myCollectionName;
    }
}
