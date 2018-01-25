package io.opensphere.controlpanels.columnlabels.ui;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

/**
 * Interface to a single row UI within the column labels editor.
 */
public interface ColumnLabelRowView
{
    /**
     * Gets the columns combo box.
     *
     * @return The columns combo box.
     */
    ComboBox<String> getColumns();

    /**
     * The move down button.
     *
     * @return The move down button.
     */
    Button getMoveDownButton();

    /**
     * Gets the move up button.
     *
     * @return The move up button.
     */
    Button getMoveUpButton();

    /**
     * Gets the remove button.
     *
     * @return The remove button.
     */
    Button getRemoveButton();

    /**
     * Gets the checkbox to turn column names on or off within the label.
     *
     * @return The show column name check box.
     */
    CheckBox getShowColumnName();
}
