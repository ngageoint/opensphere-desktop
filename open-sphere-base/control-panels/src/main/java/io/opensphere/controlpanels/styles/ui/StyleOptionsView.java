package io.opensphere.controlpanels.styles.ui;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;

import io.opensphere.controlpanels.styles.model.Styles;

/**
 * The interface to the style options UI.
 */
public interface StyleOptionsView
{
    /**
     * Gets the color picker component in the UI.
     *
     * @return The color picker.
     */
    ColorPicker getColorPicker();

    /**
     * Gets the size slider.
     *
     * @return The size of the bulls eye.
     */
    Slider getSize();

    /**
     * Gets the style picker combo box.
     *
     * @return The style picker.
     */
    ComboBox<Styles> getStylePicker();
}
