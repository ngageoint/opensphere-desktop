package io.opensphere.core.util.swing.input.model;

import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ValidationStatus;

/**
 * View model interface.
 *
 * @param <T> The type of the model
 */
public interface ViewModel<T> extends ObservableValue<T>
{
    /**
     * Adds the property change listener.
     *
     * @param listener the listener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Gets the description.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Returns whether the model has changed since the last mark.
     *
     * @return Whether the model has changed
     */
    boolean isChanged();

    /**
     * Returns whether the model is enabled.
     *
     * @return Whether the model is enabled
     */
    boolean isEnabled();

    /**
     * Returns whether the model is required.
     *
     * @return Whether the model is required
     */
    boolean isRequired();

    /**
     * Returns whether the model is valid.
     *
     * @return Whether the model is valid
     */
    ValidationStatus getValidationStatus();

    /**
     * Returns whether the model is validating.
     *
     * @return Whether the model is validating
     */
    boolean isValidating();

    /**
     * Returns whether the model is visible.
     *
     * @return Whether the model is visible
     */
    boolean isVisible();

    /**
     * Removes the property change listener.
     *
     * @param listener the listener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Marks the model's changed state so that it can be checked later for
     * changes.
     *
     * @param isChanged Whether the model is changed
     */
    void setChanged(boolean isChanged);

    /**
     * Sets whether the model is enabled.
     *
     * @param isEnabled Whether the model is enabled
     */
    void setEnabled(boolean isEnabled);

    /**
     * Sets whether the model is validating.
     *
     * @param isValidating Whether the model is validating
     */
    void setValidating(boolean isValidating);

    /**
     * Sets whether the model is visible.
     *
     * @param isVisible Whether the model is visible
     */
    void setVisible(boolean isVisible);
}
