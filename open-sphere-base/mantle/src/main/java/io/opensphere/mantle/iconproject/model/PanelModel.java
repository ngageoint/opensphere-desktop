package io.opensphere.mantle.iconproject.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/** The model for the IconManagerFrame. */
public class PanelModel
{
    /** View set to default of Grid. */
    private final ObjectProperty<ViewStyle> viewType = new SimpleObjectProperty<>(this, "viewtype", ViewStyle.GRID);

    /** gets the icon display view type.
     * @return viewType the chosen view.*/
    public ObjectProperty<ViewStyle> getViewType()
    {
        return viewType;
    }
}
