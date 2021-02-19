package io.opensphere.core.util.javafx.input;

import org.apache.commons.lang3.StringUtils;

import javafx.application.Platform;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

/**
 * Ensures that only integer input is entered into a integer spinner.
 */
public class IntegerSpinnerInputValidator extends StringConverter<Integer>
{
    /** The maximum allowable value in the spinner. */
    private final int myMaximum;

    /** The minimum allowable value in the spinner. */
    private final int myMinimum;

    /**
     * Creates a new input validator for the given Spinner TextField.
     *
     * @param input the TextField analyzed for validation
     * @param min the minimum value allowed
     * @param max the maximum value allowed
     */
    public IntegerSpinnerInputValidator(TextField input, int min, int max)
    {
        myMinimum = min;
        myMaximum = max;

        // invalid input immediately resets the spinner to its last value
        input.textProperty().addListener((obs, o, n) ->
        {
            if (!checkInput(n))
            {
                Platform.runLater(() -> input.setText("" + o));
            }
        });
    }

    /**
     * Checks if the given string is allowed in the Spinner TextField.
     * Valid input is empty strings and integers between the maximum
     * and minimum (inclusive).
     *
     * @param input the text to validate
     * @return true if the text is allowed in the TextField
     */
    private boolean checkInput(String input)
    {
        if (input != null && StringUtils.isEmpty(input))
        {
            return true;
        }
        else if (input == null || StringUtils.isWhitespace(input))
        {
            return false;
        }

        try
        {
            Integer parsedInput = Integer.parseInt(input);
            if (parsedInput <= myMaximum && parsedInput >= myMinimum)
            {
                return true;
            }
            return false;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    /**
     * Attaches a new validator to the Spinner, with the given boundaries.
     *
     * @param spinner the Spinner
     * @param min the minimum value allowed in the Spinner
     * @param max the maximum value allowed in the Spinner
     */
    public static void setupValidator(Spinner<Integer> spinner, int min, int max)
    {
        IntegerSpinnerValueFactory factory = (IntegerSpinnerValueFactory) spinner.getValueFactory();
        IntegerSpinnerInputValidator validator = new IntegerSpinnerInputValidator(spinner.getEditor(), min, max);
        factory.setConverter(validator);
    }

    @Override
    public String toString(Integer object)
    {
        return object.toString();
    }

    @Override
    public Integer fromString(String string)
    {
        if (StringUtils.isBlank(string))
        {
            return Integer.valueOf(myMinimum);
        }
        return Integer.parseInt(string);
    }
}
