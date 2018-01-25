package io.opensphere.core.util;

/**
 * A service that keeps track of the validation state of an object and can
 * notify interested parties if the validation state changes.
 */
public interface ValidatorSupport
{
    /**
     * Add a validation listener to be notified if the validation status
     * changes, and notify the listener of the current status of the validator.
     *
     * @param listener The listener.
     */
    void addAndNotifyValidationListener(ValidationStatusChangeListener listener);

    /**
     * Get a message detailing why the object is valid or not.
     *
     * @return The message.
     */
    String getValidationMessage();

    /**
     * Get if this object is valid.
     *
     * @return {@code true} if the object is valid.
     */
    ValidationStatus getValidationStatus();

    /**
     * Remove a validation listener.
     *
     * @param listener The listener.
     */
    void removeValidationListener(ValidationStatusChangeListener listener);

    /**
     * Interface for listeners interested in validation status.
     */
    @FunctionalInterface
    public interface ValidationStatusChangeListener
    {
        /**
         * Method called when the validation status changes.
         *
         * @param object The object being validated.
         * @param valid If the object is valid.
         * @param message A message detailing why the object is valid or not.
         */
        void statusChanged(Object object, ValidationStatus valid, String message);
    }
}
