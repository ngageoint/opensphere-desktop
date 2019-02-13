package io.opensphere.analysis.baseball;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.text.Text;

/**
 * The data to be displayed in a row of cells of the data panel in
 * the baseball card.
 */
public class BaseballDataRow
{
    /** The data displayed under the field heading. */
    private final StringProperty myFieldProperty;

    /** The data displayed under the value heading. */
    private final ObjectProperty<Text> myValueProperty;

    /**
     * Creates a new set of values for the data panel.
     *
     * @param field the data for the field column
     * @param value the data for the value column
     */
    public BaseballDataRow(String field, Text value)
    {
        myFieldProperty = new SimpleStringProperty(field);
        myValueProperty = new SimpleObjectProperty<Text>();
        myValueProperty.setValue(value);
    }

    /**
     * Gets the property containing the field information.
     *
     * @return the field property
     */
    public StringProperty fieldProperty()
    {
        return myFieldProperty;
    }

    /**
     * Gets the data for the field column.
     *
     * @return the field data
     */
    public String getField()
    {
        return myFieldProperty.get();
    }

    /**
     * Gets the property containing the value information.
     *
     * @return the value property
     */
    public ObjectProperty<Text> valueProperty()
    {
        return myValueProperty;
    }

    /**
     * Gets the data for the value column.
     *
     * @return the value data
     */
    public Text getValue()
    {
        return myValueProperty.get();
    }
}
