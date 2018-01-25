package io.opensphere.controlpanels.styles.ui;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * Interface to the Ellipse editor UI.
 */
public interface EllipseView
{
    /**
     * Gets the orientation field.
     *
     * @return The orientation field.
     */
    TextField getOrientationField();

    /**
     * Gets the semi major input field.
     *
     * @return The semi major input field.
     */
    TextField getSemiMajorField();

    /**
     * Gets the semi major units picker.
     *
     * @return The semi major units picker.
     */
    ComboBox<String> getSemiMajorUnitsPicker();

    /**
     * Gets the semi minor field.
     *
     * @return The semi minor field.
     */
    TextField getSemiMinorField();

    /**
     * Gets the semi minor units picker.
     *
     * @return The semi minor units picker.
     */
    ComboBox<String> getSemiMinorUnitsPicker();
}
