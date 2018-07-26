package io.opensphere.mantle.iconproject.model;

import io.opensphere.mantle.icon.IconRegistry;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/** The model for the IconManagerFrame. */
public class PanelModel
{
    /** View set to default of Grid. */
    private final ObjectProperty<ViewStyle> viewType = new SimpleObjectProperty<>(this, "viewtype", ViewStyle.GRID);

    /** The Collection Name */
    private String myCollectionName = "Default";

    /**
     * gets the icon display view type.
     * 
     * @return viewType the chosen view.
     */
    public ObjectProperty<ViewStyle> getViewType()
    {
        return viewType;
    }

    public String getMyCollectionName()
    {
        return myCollectionName;
    }

    public void setMyCollectionName(String myCollectionName)
    {
        this.myCollectionName = myCollectionName;
    }
}
