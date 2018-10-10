package io.opensphere.core.util.javafx.input.view;

import java.util.regex.Pattern;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * A stylized spinner, in which the up and down arrows are presented above and
 * below the content area, respectively. This spinner was developed for use in
 * the time picker, to allow for each component of a clock to be spun
 * independently. The component was developed generically to allow for any
 * numeric values to be represented.
 */
public class BoundNumericSpinner extends VBox
{
    /**
     * The name of the style class used to skin the component.
     */
    private static final String DEFAULT_STYLE_CLASS = "time-spinner";

    /**
     * The pattern used for validation.
     */
    private static final Pattern VALIDATION_PATTERN = Pattern.compile("\\d{1,2}");

    /**
     * The button pressed to move the spinner's value up.
     */
    private final Button myUpButton;

    /**
     * The button pressed to move the spinner's value down.
     */
    private final Button myDownButton;

    /**
     * The component in which the content is rendered.
     */
    private final TextField myContent;

    /**
     * The maximum value of the spinner, expressed exclusively.
     */
    private final int myMaxValue;

    /**
     * The minimum value of the spinner expressed inclusively.
     */
    private final int myMinValue;

    /**
     * The total distance between the maximum value and the minimum value.
     */
    private final int myFieldSpan;

    /**
     * The property through which value changes are propagated.
     */
    private final IntegerProperty myValue;

    /**
     * Creates a new numeric spinner, with the a minimum value of zero, and a
     * maximum value of the supplied value.
     *
     * @param pMaxValue the maximum value of the spinner.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public BoundNumericSpinner(int pMaxValue)
    {
        myMaxValue = pMaxValue;
        myMinValue = 0;
        myFieldSpan = myMaxValue - myMinValue;
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        myUpButton = new Button();
        myUpButton.getStyleClass().add("up-button");

        StackPane upArrow = new StackPane();
        upArrow.getStyleClass().add("up-arrow");
        upArrow.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        myUpButton.setGraphic(upArrow);
        myUpButton.setOnAction(pEvent -> increment());

        myContent = new TextField("00");
        myContent.getStyleClass().add("value");
        myContent.textProperty().addListener((pObservable, pOldValue, pNewValue) -> validate(pObservable, pOldValue, pNewValue));

        myDownButton = new Button();
        myDownButton.getStyleClass().add("down-button");
        myDownButton.setOnAction(pEvent -> decrement());

        StackPane downArrow = new StackPane();
        downArrow.getStyleClass().add("down-arrow");
        downArrow.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        myDownButton.setGraphic(downArrow);

        getChildren().add(myUpButton);
        getChildren().add(myContent);
        getChildren().add(myDownButton);

        setOnScroll(pEvent ->
        {
            if (pEvent.getDeltaY() > 0)
            {
                increment();
            }
            else
            {
                decrement();
            }
        });

        myValue = new SimpleIntegerProperty(0);
    }

    /**
     * Validates the supplied value. This method is used as an event handler,
     * and is called when the {@link #myContent} field changes. This method has
     * a side-effect of resetting the value of the {@link #myContent} field to
     * the old value if the new value fails validation.
     *
     * @param pObservable the item that triggered the event.
     * @param pOldValue the original value of the field before it was changed.
     * @param pNewValue the new value of the field to validate.
     */
    protected void validate(ObservableValue<? extends String> pObservable, String pOldValue, String pNewValue)
    {
        if (!pObservable.equals(myContent.textProperty()))
        {
            // validate that only numbers are entered:
            if (!VALIDATION_PATTERN.matcher(pNewValue).matches())
            {
                myContent.textProperty().set(pOldValue);
            }
            else
            {
                // validate that only valid numbers are entered:
                int value = Integer.parseInt(pNewValue);
                if (value >= myMaxValue || value < myMinValue)
                {
                    myContent.textProperty().set(pOldValue);
                }
                else
                {
                    myContent.textProperty().set(String.format("%02d", Integer.valueOf(value)));
                }
            }
        }
    }

    /**
     * Gets the value of the {@link #myValue} field.
     *
     * @return the value stored in the {@link #myValue} field.
     */
    public ObservableIntegerValue value()
    {
        return myValue;
    }

    /**
     * Sets the value of the spinner to the supplied value.
     *
     * @param pValue the value to which to set the spinner.
     */
    public void set(int pValue)
    {
        myContent.textProperty().set(String.format("%02d", Integer.valueOf(pValue)));
        myValue.set(pValue);
    }

    /**
     * Adjusts the value of the spinner, and fires off notification through the
     * {@link #myValue} property.
     */
    public void increment()
    {
        int value = Integer.parseInt(myContent.textProperty().get()) + 1;
        if (value >= myMaxValue)
        {
            value -= myFieldSpan;
        }
        myContent.textProperty().set(String.format("%02d", Integer.valueOf(value)));
        myValue.set(value);
    }

    /**
     * Adjusts the value of the spinner, and fires off notification through the
     * {@link #myValue} property.
     */
    public void decrement()
    {
        int value = Integer.parseInt(myContent.textProperty().get()) - 1;
        if (value < myMinValue)
        {
            value += myFieldSpan;
        }
        myContent.textProperty().set(String.format("%02d", Integer.valueOf(value)));
        myValue.set(value);
    }
}
