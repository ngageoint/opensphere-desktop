package io.opensphere.wps.ui.detail;

import jidefx.scene.control.validation.ValidationGroup;

/**
 * An interface defining the methods needed to perform validation on a given form.
 */
public interface ValidatableForm
{
    /**
     * Gets the validation group associated with the form.
     *
     * @return the validation group associated with the form.
     */
    ValidationGroup getValidationGroup();

    /**
     * Forces the form to validate all registered inputs.
     */
    void performValidation();
}
