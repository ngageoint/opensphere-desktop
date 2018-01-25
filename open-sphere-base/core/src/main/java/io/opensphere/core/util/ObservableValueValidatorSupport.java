package io.opensphere.core.util;

import java.util.function.Predicate;

/**
 * A {@link DefaultValidatorSupport} for an {@link ObservableValue} that uses a
 * {@link Predicate} to determine if the value is valid.
 *
 * @param <T> The type of the value being validated.
 */
public class ObservableValueValidatorSupport<T> extends DefaultValidatorSupport
{
    /** The validator for the model value. */
    private final PredicateWithMessage<? super T> myValidator;

    /** The value being validated. */
    private final ObservableValue<T> myValue;

    /**
     * Construct the list validator.
     *
     * @param value The value being validated.
     * @param validator The predicate used to validate the model value.
     * @param validatorMessage The message to be used if the model value does
     *            not pass validation.
     */
    public ObservableValueValidatorSupport(ObservableValue<T> value, Predicate<? super T> validator, String validatorMessage)
    {
        super(value);
        myValue = Utilities.checkNull(value, "value");
        Utilities.checkNull(validator, "validator");
        Utilities.checkNull(validatorMessage, "validatorMessage");
        myValidator = new DefaultPredicateWithMessage<>(validator, null, validatorMessage);
    }

    /**
     * Construct the list validator.
     *
     * @param value The value being validated.
     * @param validator The predicate used to validate the model value.
     */
    public ObservableValueValidatorSupport(ObservableValue<T> value, PredicateWithMessage<? super T> validator)
    {
        super(value);
        myValue = Utilities.checkNull(value, "value");
        myValidator = Utilities.checkNull(validator, "validator");
    }

    @Override
    public ValidationStatus getValidationStatus()
    {
        ValidationStatus isValid;
        String message;

        T value = getValue();
        if (getValidator().test(value))
        {
            isValid = ValidationStatus.VALID;
            message = null;
        }
        else
        {
            isValid = ValidationStatus.ERROR;
            message = getValidator().getMessage();
        }

        setValidationResult(isValid, message);
        return isValid;
    }

    /**
     * Get the validator.
     *
     * @return The validator.
     */
    protected final PredicateWithMessage<? super T> getValidator()
    {
        return myValidator;
    }

    /**
     * Get the model value.
     *
     * @return The model value.
     */
    protected final T getValue()
    {
        return myValue.get();
    }
}
