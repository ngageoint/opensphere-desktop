package io.opensphere.mantle.iconproject.model;

import java.util.Set;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ImportProp
{

    /** The Collection Name */
    private StringProperty myCollectionName = new SimpleStringProperty("Default");

    /** The SubCollection Name */
    private Set<String> mySubCollectionList;

    private StringProperty mySubCollectionName= new SimpleStringProperty("");

    public StringProperty getSubCollectionName()
    {
        return mySubCollectionName;
    }

    public Set<String> getSubCollectionList()
    {
        return mySubCollectionList;
    }

    public void setSubCollectionList(Set<String> theSubCollectionList)
    {
        mySubCollectionList = theSubCollectionList;
    }

    public StringProperty getCollectionName()
    {
        return myCollectionName;
    }

}
