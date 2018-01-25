package io.opensphere.featureactions.editor.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import io.opensphere.controlpanels.iconpicker.ui.IconPickerButton;
import io.opensphere.featureactions.editor.model.CriteriaOptions;
import io.opensphere.featureactions.model.FeatureAction;

/**
 * Interface to the UI that represents a single {@link FeatureAction}.
 * This abstraction is completely unnecessary.
 */
public interface SimpleFeatureActionRowView
{
    /**
     * Gets the color picker.
     *
     * @return The color picker.
     */
    ColorPicker getColorPicker();

    /**
     * Gets the icon picker.
     *
     * @return the icon picker
     */
    IconPickerButton getIconPicker();

    /**
     * Gets the copy button.
     *
     * @return The copy button.
     */
    Button getCopyButton();

    /**
     * Gets the enabled checkbox.
     *
     * @return The enabled checkbox.
     */
    CheckBox getEnabled();

    /**
     * Gets the field picker.
     *
     * @return The fields combobox.
     */
    ComboBox<String> getField();

    /**
     * Gets the maximum value text field.
     *
     * @return The maximum value text field.
     */
    TextField getMaximumValue();

    /**
     * Gets the minimum value text field.
     *
     * @return The minimum value text field.
     */
    TextField getMinimumValue();

    /**
     * Gets the name text field.
     *
     * @return The name text field.
     */
    TextField getName();

    /**
     * Gets the {@link CriteriaOptions} picker.
     *
     * @return The {@link CriteriaOptions} picker.
     */
    ComboBox<CriteriaOptions> getOptions();

    /**
     * Gets the remove button.
     *
     * @return The remove button.
     */
    Button getRemoveButton();

    /**
     * Gets the value text field.
     *
     * @return The value text field.
     */
    TextField getValue();

    /**
     * Gets the message shown when the filter is complex.
     *
     * @return the message mask
     */
    Node getComplexFilterMask();

    /**
     * Gets the message shown when the style options are missing.
     *
     * @return the message mask
     */
    Node getStyleAbsentMask();

    /**
     * Invoked when the data have been edited.
     *
     * @param listener the listener
     */
    void setEditListener(Runnable listener);
}
