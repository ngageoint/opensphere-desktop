package io.opensphere.mantle.iconproject.impl;

import javafx.beans.property.IntegerProperty;
import javafx.scene.control.Spinner;

public class SpinnerBuilder
{

    public static Spinner<Number> createSpinner(boolean editable, int min, int max, double spinwidth, IntegerProperty binder)
    {

        Spinner<Number> Spinner = new Spinner<>(min, max, 0);

        Spinner.setPrefWidth(spinwidth);
        Spinner.getValueFactory().valueProperty().bindBidirectional(binder);

        if (editable)
            Spinner.getStyleClass().clear();
        Spinner.setEditable(true);
        return Spinner;

    }

}
