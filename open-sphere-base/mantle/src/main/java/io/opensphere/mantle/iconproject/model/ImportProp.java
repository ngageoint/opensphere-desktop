package io.opensphere.mantle.iconproject.model;

import java.util.Set;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ImportProp
{

    /** The Collection Name */
    private StringProperty myCollectionName = new SimpleStringProperty("Default");

    /** The SubCollection Name */
    private Set<String> mySubCollectionList = null;

    private StringProperty mySubCollectionName;

    public StringProperty getCollectionName()
    {
        return myCollectionName;
    }

    public void setMyCollectionName(StringProperty myCollectionName)
    {
        this.myCollectionName = myCollectionName;
    }

    public Set<String> getMySubCollectionNames()
    {
        return mySubCollectionList;
    }

    public void setSubCollectionName(StringProperty CollectionName)
    {
        this.mySubCollectionName = CollectionName;
    }

    public void setMySubCollectionNames(Set<String> mySubCollectionName)
    {
        this.mySubCollectionList = mySubCollectionName;
    }

    public StringProperty getSubCollectionName()
    {
        return null;
    }

}
