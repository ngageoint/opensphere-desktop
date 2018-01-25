package io.opensphere.mantle.datasources;

/**
 * The listener interface for receiving validationDispostion events. The class
 * that is interested in processing a validationDispostion event implements this
 * interface, and the object created with that class is registered with a
 * component using the component's <code>addValidationDispostionListener</code>
 * method. When the validationDispostion event occurs, that object's appropriate
 * method is invoked.
 *
 * @see ValidationDispostionListener
 */
@FunctionalInterface
public interface ValidationDispostionListener
{
    /**
     * Validation complete.
     *
     * @param validationStatus the validation status
     */
    void validationComplete(boolean validationStatus);
}
