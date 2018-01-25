package io.opensphere.controlpanels.columnlabels.ui;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;

/**
 * Interface to the Columns Label editor.
 */
public interface ColumnLabelsView
{
    /**
     * Gets the add button that will add a new label.
     *
     * @return The add button.
     */
    Button getAddButton();

    /**
     * Gets the check box indicating if the user wants labels to show all the
     * time.
     *
     * @return The show always check box.
     */
    CheckBox getAlwaysShowLabels();

    /**
     * Gets the list view shoing the columns configured in the label.
     *
     * @return The columns configured for the label.
     */
    ListView<ColumnLabel> getColumnLabels();
}
