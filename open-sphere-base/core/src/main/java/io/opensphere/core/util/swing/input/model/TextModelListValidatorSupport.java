package io.opensphere.core.util.swing.input.model;

import java.util.function.Predicate;

import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ObservableValueValidatorSupport;
import io.opensphere.core.util.PredicateWithMessage;
import io.opensphere.core.util.ValidationStatus;

/**
 * A {@link DefaultValidatorSupport} for a {@link TextModel} that uses a
 * {@link Predicate} to determine if the items in the list are valid.
 */
public class TextModelListValidatorSupport extends ObservableValueValidatorSupport<String>
{
    /**
     * Construct the list validator.
     *
     * @param model The model being validated.
     * @param validator The predicate used to validate each element in the list.
     */
    public TextModelListValidatorSupport(TextModel model, PredicateWithMessage<String> validator)
    {
        super(model, validator);
    }

    @Override
    public ValidationStatus getValidationStatus()
    {
        ValidationStatus isValid = ValidationStatus.ERROR;
        String message = "List is empty.";

        String value = getValue();
        String[] tokens = value == null ? null : value.split("\\s*,\\s*");
        if (tokens != null && tokens.length > 0)
        {
            isValid = ValidationStatus.VALID;
            message = null;
            for (String token : tokens)
            {
                if (!getValidator().test(token))
                {
                    isValid = ValidationStatus.ERROR;
                    message = "List value '" + token + "' is not formatted correctly: " + getValidator().getMessage();
                    break;
                }
            }
        }

        setValidationResult(isValid, message);
        return isValid;
    }
}
