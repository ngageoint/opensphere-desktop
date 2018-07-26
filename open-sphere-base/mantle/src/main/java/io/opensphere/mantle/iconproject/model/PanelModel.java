package io.opensphere.mantle.iconproject.model;

import java.util.Set;

import io.opensphere.mantle.icon.IconRegistry;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** The model for the IconManagerFrame. */
public class PanelModel
{
    /** View set to default of Grid. */
    private final ObjectProperty<ViewStyle> viewType = new SimpleObjectProperty<>(this, "viewtype", ViewStyle.GRID);

    /** The Collection Name */
    private StringProperty myCollectionName = new SimpleStringProperty("Default");

    /** The SubCollection Name */
    private Set<String> mySubCollectionNames = null;

    private IconRegistry myIconRegistry;

    /**
     * gets the icon display view type.
     * 
     * @return viewType the chosen view.
     */
    public ObjectProperty<ViewStyle> getViewType()
    {
        return viewType;
    }

    public StringProperty getMyCollectionName()
    {
        return myCollectionName;
    }

    public void setMyCollectionName(StringProperty myCollectionName)
    {
        this.myCollectionName = myCollectionName;
    }

    public Set<String> getMySubCollectionNames()
    {
        return mySubCollectionNames;
    }

    public void setMySubCollectionNames(Set<String> mySubCollectionName)
    {
        this.mySubCollectionNames = mySubCollectionName;
    }

    public IconRegistry getMyIconRegistry()
    {
        return myIconRegistry;
    }
    
    public void setMyIconRegistry(IconRegistry iconRegistry)
    {
        this.myIconRegistry = iconRegistry;
    }
}
