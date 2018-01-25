package io.opensphere.core.util.javafx.input.view;

import java.time.LocalDate;
import java.time.chrono.IsoChronology;
import java.time.format.FormatStyle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;

/**
 * A date picker panel. This is currently intended to be used with TimeInstantTextFieldController but could be made more generic.
 */
public class DatePicker extends ComboBoxBase<LocalDate>
{
    /**
     * The name of the style class used to skin the component.
     */
    private static final String DEFAULT_STYLE_CLASS = "date-time-picker";

    /**
     * The editor for the DatePicker.
     *
     * @see javafx.scene.control.ComboBox#editorProperty
     */
    private ReadOnlyObjectWrapper<TextField> myEditor;

    /**
     * A converter used to change {@link LocalDate} objects to {@link String} instances with a known format.
     */
    private final ObjectProperty<StringConverter<LocalDate>> myConverter = new SimpleObjectProperty<>(this, "converter", null);

    /**
     * Create a symmetric (format/parse) converter with the default locale.
     */
    private final StringConverter<LocalDate> myDefaultConverter = new LocalDateStringConverter(FormatStyle.SHORT, null,
            IsoChronology.INSTANCE);

    /**
     * The content area in which the date picker is rendered.
     */
    private final DatePickerContent myDatePicker;

    /**
     * Creates a new date picker.
     */
    public DatePicker()
    {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        myDatePicker = new DatePickerContent(this);
        setEditable(true);
    }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin()
    {
        return new DatePickerSkin(this);
    }

    /**
     * Gets the property to which the internal converter is bound.
     *
     * @return the property to which the internal converter is bound.
     */
    public final ObjectProperty<StringConverter<LocalDate>> converterProperty()
    {
        return myConverter;
    }

    /**
     * Sets the value of the {@link #myConverter} field.
     *
     * @param pConverter the value to store in the {@link #myConverter} field.
     */
    public final void setConverter(StringConverter<LocalDate> pConverter)
    {
        converterProperty().set(pConverter);
    }

    /**
     * Gets the value of the {@link #myConverter} field.
     *
     * @return the value stored in the {@link #myConverter} field.
     */
    public final StringConverter<LocalDate> getConverter()
    {
        StringConverter<LocalDate> converter = converterProperty().get();
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
    public final TextField getEditor()
    {
        return editorProperty().get();
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

    /**
     * Gets the value of the {@link #myDatePicker} field.
     *
     * @return the value stored in the {@link #myDatePicker} field.
     */
    public DatePickerContent getDatePickerContent()
    {
        return myDatePicker;
    }
}
