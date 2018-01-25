package io.opensphere.core.util.javafx.input.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * A combined component that extends the {@link Control} class, allowing instances to be used for input in place of a single
 * component.
 */
public class CombinedDateTimePicker extends Control
{
    /**
     * The picker in which the user selects date values.
     */
    private final DatePicker myDatePicker;

    /**
     * The picker in which the user selects time values.
     */
    private final TimePicker myTimePicker;

    /**
     * The button used to change the control's date and time to the current value.
     */
    private final Button myNowButton;

    /**
     * The node in which the components are rendered.
     */
    private final HBox myNode;

    /**
     * The property used to propagate value changes.
     */
    private final ObjectProperty<Date> myValue = new SimpleObjectProperty<>();

    /**
     * Creates a new combined picker, in which a date picker and time picker are displayed on the same line.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public CombinedDateTimePicker()
    {
        myDatePicker = new DatePicker();
        myTimePicker = new TimePicker();
        myNowButton = new Button("Now");
        myNowButton.setOnAction(event -> setToNow());

        myNode = new HBox(myDatePicker, myTimePicker, myNowButton);
        HBox.setHgrow(myDatePicker, Priority.ALWAYS);
        HBox.setHgrow(myTimePicker, Priority.ALWAYS);
        HBox.setHgrow(myNowButton, Priority.NEVER);

        HBox.setMargin(myDatePicker, new Insets(0, 2, 0, 0));
        HBox.setMargin(myTimePicker, new Insets(0, 2, 0, 2));
        HBox.setMargin(myNowButton, new Insets(0, 0, 0, 2));

        myDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> dateChanged(newValue));
        myTimePicker.valueProperty().addListener((observable, oldValue, newValue) -> timeChanged(newValue));

        getChildren().add(myNode);
    }

    /**
     * Sets the value of the picker to the current date and time.
     */
    protected void setToNow()
    {
        myTimePicker.setValue(LocalTime.now());
        myDatePicker.setValue(LocalDate.now());
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.Control#createDefaultSkin()
     */
    @Override
    protected Skin<?> createDefaultSkin()
    {
        return new CombinedDateTimePickerSkin(this, myNode);
    }

    /**
     * An event handler method used to react to a date selection change event in the {@link #myDatePicker} field. When the
     * selected date is changed, the value is combined with the selected time, and converted to a legacy {@link Date} object. This
     * value is then propagated through the {@link #myValue} property.
     *
     * @param pSelectedDate the date selected by the user on the date picker.
     */
    protected void dateChanged(LocalDate pSelectedDate)
    {
        LocalTime time = myTimePicker.getValue();
        if (time == null)
        {
            time = LocalTime.MIDNIGHT;
        }
        LocalDateTime localDateTime = pSelectedDate.atTime(time);
        OffsetDateTime offsetDateTime = localDateTime.atOffset(ZoneOffset.UTC);

        myValue.set(Date.from(offsetDateTime.toInstant()));
    }

    /**
     * An event handler method used to react to a time selection change event in the {@link #myTimePicker} field. When the
     * selected time is changed, the value is combined with the selected date, and converted to a legacy {@link Date} object. This
     * value is then propagated through the {@link #myValue} property.
     *
     * @param pSelectedTime the time selected by the user on the time picker.
     */
    protected void timeChanged(LocalTime pSelectedTime)
    {
        LocalTime time = pSelectedTime;
        if (time == null)
        {
            time = LocalTime.MIDNIGHT;
        }

        LocalDate date = myDatePicker.getValue();
        if (date == null)
        {
            date = LocalDate.now();
        }
        LocalDateTime localDateTime = time.atDate(date);
        OffsetDateTime offsetDateTime = localDateTime.atOffset(ZoneOffset.UTC);

        myValue.set(Date.from(offsetDateTime.toInstant()));
    }

    /**
     * Sets the value of the {@link #myValue} field.
     *
     * @param pValue the value to store in the {@link #myValue} field.
     */
    public void setValue(LocalDateTime pValue)
    {
        LocalTime time = pValue.toLocalTime();
        LocalDate date = pValue.toLocalDate();

        myTimePicker.setValue(time);
        myDatePicker.setValue(date);
    }

    /**
     * Gets the value of the {@link #myValue} field.
     *
     * @return the value stored in the {@link #myValue} field.
     */
    public ObjectProperty<Date> valueProperty()
    {
        return myValue;
    }
}
