package io.opensphere.analysis.binning.editor.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import io.opensphere.analysis.binning.criteria.BinCriteriaElement;

/**
 * Model containing the values for a single criteria row.
 */
public class CriterionModel
{
    /**
     * The available list of binning types.
     */
    private final ObservableList<String> myBinTypes = FXCollections.observableArrayList();

    /**
     * The criteria element.
     */
    private final BinCriteriaElement myElement;

    /**
     * Constructs a new model.
     *
     * @param element The element being edited.
     */
    public CriterionModel(BinCriteriaElement element)
    {
        myElement = element;
    }

    /**
     * Gets the available bin types.
     *
     * @return The available binning types.
     */
    public ObservableList<String> getBinTypes()
    {
        return myBinTypes;
    }

    /**
     * Gets the criteria element.
     *
     * @return The criteria element.
     */
    public BinCriteriaElement getElement()
    {
        return myElement;
    }
}
