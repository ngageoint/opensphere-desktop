package io.opensphere.core.util.javafx.input.view;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.regex.Pattern;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.util.converter.LocalTimeStringConverter;

/**
 * A picker used to enter time in HH:mm:ss format.
 */
public class TimePicker extends ComboBoxBase<LocalTime>
{
    /**
     * The name of the style class used to skin the component.
     */
    private static final String DEFAULT_STYLE_CLASS = "time-picker";

    /**
     * The regular expression pattern used to validate user-typed time strings.
     */
    private static final Pattern TIME_PATTERN = Pattern
            .compile("(([01]{1}\\d{1})|(2{1}[0-3]{1})):([0-5]{1}\\d{1}):([0-5]{1}\\d{1})");

    /**
     * The editor for the DatePicker.
     *
     * @see javafx.scene.control.ComboBox#editorProperty
     */
    private ReadOnlyObjectWrapper<TextField> myEditor;

    /**
     * A converter used to change {@link LocalTime} objects to {@link String}
     * instances with a known format.
     */
    private final ObjectProperty<StringConverter<LocalTime>> myConverter = new SimpleObjectProperty<>(this, "converter", null);

    /**
     * Create a symmetric (format/parse) converter with the default locale.
     */
    private final StringConverter<LocalTime> myDefaultConverter = new LocalTimeStringConverter(
            new DateTimeFormatterBuilder().appendValue(HOUR_OF_DAY, 2).appendLiteral(':').appendValue(MINUTE_OF_HOUR, 2)
                    .optionalStart().appendLiteral(':').appendValue(SECOND_OF_MINUTE, 2).toFormatter(),
            DateTimeFormatter.ISO_LOCAL_TIME);

    /**
     * The content of the time picker.
     */
    private final TimePickerContent myTimePickerContent;

    /**
     * Creates a new time picker.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public TimePicker()
    {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        myTimePickerContent = new TimePickerContent(this);
        setEditable(true);
        editorProperty().get().textProperty().addListener((pObservable, pOldValue, pNewValue) -> processTimeChange(pNewValue));
    }

    /**
     * Processes a change, populating the underlying picker if and only if the
     * time pattern matches the input.
     *
     * @param pNewValue the value to process.
     */
    protected void processTimeChange(String pNewValue)
    {
        if (TIME_PATTERN.matcher(pNewValue).matches())
        {
            myTimePickerContent.populateValue(getConverter().fromString(pNewValue));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.Control#createDefaultSkin()
     */
    @Override
    protected Skin<?> createDefaultSkin()
    {
        return new TimePickerSkin(this);
    }

    /**
     * Gets the property to which the internal converter is bound.
     *
     * @return the property to which the internal converter is bound.
     */
    public final ObjectProperty<StringConverter<LocalTime>> converterProperty()
    {
        return myConverter;
    }

    /**
     * Sets the value of the {@link #myConverter} field.
     *
     * @param pConverter the value to store in the {@link #myConverter} field.
     */
    public final void setConverter(StringConverter<LocalTime> pConverter)
    {
        converterProperty().set(pConverter);
    }

    /**
     * Gets the value of the myConverter property.
     *
     * @return the value stored in the myConverter property.
     */
    public StringConverter<LocalTime> getConverter()
    {
        StringConverter<LocalTime> converter = converterProperty().get();
        if (converter != null)
        {
            return converter;
        }
        return myDefaultConverter;
    }

    /**
     * Gets the value of the {@link #myEditor} field.
     *
     * @return the value stored in the {@link #myEditor} field.
     */
    public TextField getEditor()
    {
        return editorProperty().getValue();
    }

    /**
     * Gets the property to which the internal editor is bound.
     *
     * @return the property to which the internal editor is bound.
     */
    public final ReadOnlyObjectProperty<TextField> editorProperty()
    {
        if (myEditor == null)
        {
            myEditor = new ReadOnlyObjectWrapper<>(this, "editor");
            myEditor.set(new FakeFocusTextField());
        }
        return myEditor.getReadOnlyProperty();
    }
}
