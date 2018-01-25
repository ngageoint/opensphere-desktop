package io.opensphere.analysis.binning.editor.view;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Interface to the UI the displays one row of bin criteria data.
 */
public interface CriterionCellView
{
    /**
     * Gets the different binning types combo box.
     *
     * @return The different types of binning.
     */
    ComboBox<String> getBinTypeBox();

    /**
     * Gets the data fields choices combo box.
     *
     * @return The data fields combo box.
     */
    ComboBox<String> getFieldBox();

    /**
     * Gets the remove button.
     *
     * @return The remove button.
     */
    Button getRemoveButton();

    /**
     * Gets the tolerance value text field.
     *
     * @return The tolerance text field.
     */
    TextField getTolerance();

    /**
     * Gets the tolerance label that goes with the text field.
     *
     * @return The tolerance label.
     */
    Label getToleranceLabel();
}
