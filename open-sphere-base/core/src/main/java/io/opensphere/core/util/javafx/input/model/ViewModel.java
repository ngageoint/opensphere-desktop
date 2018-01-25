package io.opensphere.core.util.javafx.input.model;

import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;

/**
 * View model interface.
 *
 * @param <T> The type of the model
 */
public interface ViewModel<T>
{
    /**
     * Sets the value of the model.
     *
     * @param pValue the value to store in the model.
     */
    void set(T pValue);

    /**
     * Gets the value of the model.
     *
     * @return the value of the model.
     */
    T get();

    /**
     * Gets the property used to manage the model.
     *
     * @return the property used to manage the model
     */
    ObservableValue<T> getProperty();

    /**
     * Sets the description.
     *
     * @param pDescription The description
     */
    void setDescription(String pDescription);

    /**
     * Gets the description.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Gets the value of the description property.
     *
     * @return the value stored in the description property.
     */
    ObservableStringValue getDescriptionProperty();

    /**
     * Sets the name.
     *
     * @param pName the name to store in the model.
     */
    void setName(String pName);

    /**
     * Gets the name.
     *
     * @return the name stored in the model.
     */
    String getName();

    /**
     * Gets the value of the name property.
     *
     * @return the value stored in the name property.
     */
    ObservableStringValue getNameProperty();

    /**
     * Returns whether the model is required.
     *
     * @param pRequired Whether the model is required
     */
    void setRequired(boolean pRequired);

    /**
     * Returns whether the model is required.
     *
     * @return Whether the model is required
     */
    boolean isRequired();

    /**
     * Gets the value of the required property.
     *
     * @return the value stored in the required property
     */
    ObservableBooleanValue getRequiredProperty();

    /**
     * Sets whether the model is visible.
     *
     * @param pVisible Whether the model is visible
     */
    void setVisible(boolean pVisible);

    /**
     * Tests to determine if the model is visible.
     *
     * @return true if the model is visible.
     */
    boolean isVisible();

    /**
     * Gets the value of the visible property.
     *
     * @return the value stored in the visible property
     */
    ObservableBooleanValue getVisibleProperty();

    /**
     * Sets whether the model is enabled.
     *
     * @param pEnabled Whether the model is enabled.
     */
    void setEnabled(boolean pEnabled);

    /**
     * Sets whether the model is enabled.
     *
     * @return Whether the model is enabled.
     */
    boolean isEnabled();

    /**
     * Sets whether the model is enabled.
     *
     * @return the value stored in the enabled property
     */
    ObservableBooleanValue getEnabledProperty();

    /**
     * Sets whether the model is valid.
     *
     * @param isValid Whether the model is valid
     */
    void setValid(boolean isValid);

    /**
     * Tests to determine if the model is valid.
     *
     * @return true if the model is valid.
     */
    boolean isValid();

    /**
     * Gets the value of the valid property.
     *
     * @return the value stored in the valid property
     */
    ObservableBooleanValue getValidProperty();

    /**
     * Sets whether the model performs validation.
     *
     * @param isValidating Whether the model performs validation
     */
    void setValidating(boolean isValidating);

    /**
     * Tests to determine if the model performs validation.
     *
     * @return true if the model performs validation.
     */
    boolean isValidating();

    /**
     * Gets the value of the validation property.
     *
     * @return the value stored in the validation property
     */
    ObservableBooleanValue getValidatingProperty();
}
