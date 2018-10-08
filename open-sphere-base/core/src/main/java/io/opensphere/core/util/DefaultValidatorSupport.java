package io.opensphere.core.util;

import io.opensphere.core.util.ChangeSupport.Callback;

/**
 * Default implementation of {@link ValidatorSupport} that keeps track of the
 * validation object and the last validation result, as well as listeners
 * interested in changes to the validation state.
 */
public class DefaultValidatorSupport implements ValidatorSupport
{
    /**
     * Callback used to notify listeners.
     */
    private final Callback<ValidatorSupport.ValidationStatusChangeListener> myCallback = new Callback<>()
    {
        @Override
        public void notify(ValidationStatusChangeListener listener)
        {
            listener.statusChanged(myValidationObject, myValidationSuccessful, myValidationMessage);
        }
    };

    /** The change support. */
    private final transient ChangeSupport<ValidatorSupport.ValidationStatusChangeListener> myChangeSupport = StrongChangeSupport
            .create();

    /** The last validation message. */
    private String myValidationMessage;

    /** The validation object. */
    private final Object myValidationObject;

    /** The last validation result. */
    private ValidationStatus myValidationSuccessful;

    /**
     * Construct the validator support.
     *
     * @param validationObject The object being validated.
     */
    public DefaultValidatorSupport(Object validationObject)
    {
        myValidationObject = validationObject;
    }

    @Override
    public void addAndNotifyValidationListener(ValidationStatusChangeListener listener)
    {
        myChangeSupport.addListener(listener);
        myCallback.notify(listener);
    }

    @Override
    public String getValidationMessage()
    {
        return myValidationMessage;
    }

    @Override
    public ValidationStatus getValidationStatus()
    {
        return myValidationSuccessful;
    }

    /**
     * Notify validation listeners.
     *
     * @param callback The callback.
     */
    public void notifyListeners(Callback<ValidationStatusChangeListener> callback)
    {
        myChangeSupport.notifyListeners(callback);
    }

    @Override
    public void removeValidationListener(ValidationStatusChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Set the validation result and notify listeners.
     *
     * @param successful If the validation was successful.
     * @param message The validation message.
     */
    public void setValidationResult(ValidationStatus successful, String message)
    {
        if (!Utilities.sameInstance(successful, myValidationSuccessful) || !Utilities.sameInstance(message, myValidationMessage)
                && (message == null || !message.equals(myValidationMessage)))
        {
            myValidationSuccessful = successful;
            myValidationMessage = message;
            myChangeSupport.notifyListeners(myCallback);
        }
    }

    /**
     * Set the validation result from another validator and notify listeners.
     *
     * @param validator The other validator.
     */
    public void setValidationResult(ValidatorSupport validator)
    {
        setValidationResult(validator.getValidationStatus(), validator.getValidationMessage());
    }
}
