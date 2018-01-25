package io.opensphere.analysis.binning.editor.controller;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.analysis.binning.criteria.CriteriaTypeFactory;
import io.opensphere.analysis.binning.criteria.RangeCriteria;
import io.opensphere.analysis.binning.editor.model.BinCriteriaModel;
import io.opensphere.analysis.binning.editor.model.CriterionModel;
import io.opensphere.core.util.lang.Pair;

/**
 * Controller used for editing a single bin criteria row.
 */
public class CriterionController implements Observer
{
    /**
     * The top level model containing the criteria we are editing.
     */
    private final BinCriteriaModel myMainModel;

    /**
     * The criterion model.
     */
    private final CriterionModel myModel;

    /**
     * Constructs a new controller.
     *
     * @param mainModel The top level model containing the criteria we are
     *            editing.
     * @param model The model the UI is using.
     */
    public CriterionController(BinCriteriaModel mainModel, CriterionModel model)
    {
        myMainModel = mainModel;
        myModel = model;
        myModel.getElement().addObserver(this);
        updateBinTypes();
    }

    /**
     * Stops listening for model changes.
     */
    public void close()
    {
        myModel.getElement().deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (BinCriteriaElement.FIELD_PROP.equals(arg))
        {
            updateBinTypes();
        }
    }

    /**
     * Updates the bin types so there aren't binning options that do not make
     * sense for certain fields.
     */
    private void updateBinTypes()
    {
        List<String> availableTypes = CriteriaTypeFactory.getInstance().getAvailableTypes();

        Optional<Pair<String, Class<?>>> optional = myMainModel.getLayersFields().stream().filter((item) ->
        {
            return item.getFirstObject().equals(myModel.getElement().getField());
        }).findFirst();

        if (optional.isPresent() && !Number.class.isAssignableFrom(optional.get().getSecondObject()))
        {
            {
                availableTypes.remove(RangeCriteria.CRITERIA_TYPE);
            }
        }

        if (!myModel.getBinTypes().equals(availableTypes))
        {
            myModel.getBinTypes().clear();
            myModel.getBinTypes().addAll(availableTypes);

            if (myModel.getElement().getCriteriaType() == null)
            {
                myModel.getElement().setCriteriaType(CriteriaTypeFactory.getInstance().newCriteriaType(availableTypes.get(0)));
            }
        }
    }
}
