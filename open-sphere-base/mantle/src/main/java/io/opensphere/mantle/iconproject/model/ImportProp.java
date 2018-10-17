package io.opensphere.mantle.iconproject.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * This model is used to track the Collection and SubCollection choices the user
 * makes when importing icons from file or folder.
 */
public class ImportProp
{
    /** The Collection Name. */
    private StringProperty myCollectionProperty = new SimpleStringProperty("Default");

    /** The SubCollection Name. */
    private String mySubCollectionProperty = "";

    /**
     * Gets the unique SubCollection name.
     *
     * @return the SubCollection Name.
     */
    public String getSubCollectionName()
    {
        return mySubCollectionProperty;
    }

    /**
     * Sets the unique SubCollection name.
     *
     * @param name the SubCollection name
     */
    public void setSubCollectionName(String name)
    {
        mySubCollectionProperty = name;
    }

    /**
     * Gets the property containing the collection name.
     *
     * @return the collection property
     */
    public StringProperty collectionProperty()
    {
        return myCollectionProperty;
    }

    /**
     * Gets the unique Collection name.
     *
     * @return the Collection name
     */
    public String getCollectionName()
    {
        return myCollectionProperty.get();
    }

    /**
     * Sets the unique Collection name.
     *
     * @param name the Collection name
     */
    public void setCollectionName(String name)
    {
        myCollectionProperty.set(name);
    }
}
