package io.opensphere.analysis.baseball;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The data to be displayed in a row of cells of the data panel in
 * the baseball card.
 */
public class BaseballDataRow
{
    /** The data displayed under the field heading. */
    private final StringProperty myFieldProperty;

    /** The data displayed under the value heading. */
    private final ObjectProperty<Object> myValueProperty;

    /**
     * Creates a new set of values for the data panel.
     *
     * @param field the data for the field column
     * @param value the data for the value column
     */
    public BaseballDataRow(String field, Object value)
    {
        myFieldProperty = new SimpleStringProperty(field);
        myValueProperty = new SimpleObjectProperty<Object>();
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
    public ObjectProperty<Object> valueProperty()
    {
        return myValueProperty;
    }

    /**
     * Gets the data for the value column.
     *
     * @return the value data
     */
    public Object getValue()
    {
        return myValueProperty.get();
    }
}
