package io.opensphere.controlpanels.styles.ui;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.Spinner;

/**
 * Interface to the label options UI.
 */
public interface LabelOptionsView
{
    /**
     * Gets the label color picker.
     *
     * @return The color picker.
     */
    ColorPicker getColorPicker();

    /**
     * Gets the label size picker.
     *
     * @return The size picker.
     */
    Spinner<Integer> getSizePicker();
}
