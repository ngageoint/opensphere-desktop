package io.opensphere.mantle.iconproject.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class PanelModel
{

    private final ObjectProperty<ViewStyle> viewType = new SimpleObjectProperty<>(this, "viewtype", ViewStyle.GRID);

    public ObjectProperty<ViewStyle> getViewType()
    {
        return viewType;
    }



}
