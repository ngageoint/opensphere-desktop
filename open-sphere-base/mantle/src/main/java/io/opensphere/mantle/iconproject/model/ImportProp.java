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

    public StringProperty getSubCollectionName()
    {
        return mySubCollectionName;
    }

    public Set<String> getSubCollectionList()
    {
        return mySubCollectionList;
    }

    public void setSubCollectionList(Set<String> mySubCollectionList)
    {
        this.mySubCollectionList = mySubCollectionList;
    }

    public StringProperty getCollectionName()
    {
        return myCollectionName;
    }

}
