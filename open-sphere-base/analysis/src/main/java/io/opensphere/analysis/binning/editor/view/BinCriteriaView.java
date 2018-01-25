package io.opensphere.analysis.binning.editor.view;

import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import io.opensphere.analysis.binning.criteria.BinCriteriaElement;

/**
 * Interface to the bin criteria editor.
 */
public interface BinCriteriaView
{
    /**
     * Gets the add button the user can click to create new bin criteria
     * elements.
     *
     * @return The add button.
     */
    Button getAddButton();

    /**
     * Gets the view showing the list of criterion.
     *
     * @return The criterion view.
     */
    ListView<BinCriteriaElement> getCriterionView();
}
