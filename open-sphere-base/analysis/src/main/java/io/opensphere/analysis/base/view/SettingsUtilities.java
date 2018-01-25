package io.opensphere.analysis.base.view;

import javafx.beans.property.Property;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;

/** Settings UI utilities. */
public final class SettingsUtilities
{
    /**
     * Creates a row HBox.
     *
     * @param children the children
     * @return the HBox
     */
    public static HBox createRow(Node... children)
    {
        HBox box = new HBox(10, children);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /**
     * Creates a check box.
     *
     * @param text the text
     * @param property the selected property to bind to
     * @return the check box
     */
    public static CheckBox createCheckBox(String text, Property<Boolean> property)
    {
        CheckBox checkBox = new CheckBox(text);
        checkBox.selectedProperty().bindBidirectional(property);
        return checkBox;
    }

    /** Disallow instantiation. */
    private SettingsUtilities()
    {
    }
}
