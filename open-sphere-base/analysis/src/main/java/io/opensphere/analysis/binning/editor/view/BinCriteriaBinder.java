package io.opensphere.analysis.binning.editor.view;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.analysis.binning.editor.model.BinCriteriaModel;

/**
 * Binds the criteria model to the criteria view.
 */
public class BinCriteriaBinder implements EventHandler<ActionEvent>
{
    /**
     * The property used to bind the ui and model.
     */
    private final SimpleListProperty<BinCriteriaElement> myBindProperty;

    /**
     * The model to bind to.
     */
    private final BinCriteriaModel myModel;

    /**
     * The view to bind to.
     */
    private final BinCriteriaView myView;

    /**
     * Constructs a new binder and binds the view to the model so there values
     * are in sync.
     *
     * @param view The view to bind.
     * @param model The model to bind.
     */
    public BinCriteriaBinder(BinCriteriaView view, BinCriteriaModel model)
    {
        myView = view;
        myModel = model;
        myBindProperty = new SimpleListProperty<>(myModel.getCriteria().getCriterias());
        bind();
    }

    /**
     * Unbinds the view from the model.
     */
    public void close()
    {
        myView.getAddButton().setOnAction(null);
        myView.getCriterionView().itemsProperty()
                .bindBidirectional(new SimpleListProperty<>(FXCollections.observableArrayList()));
        myView.getCriterionView().itemsProperty().unbindBidirectional(myBindProperty);
    }

    @Override
    public void handle(ActionEvent event)
    {
        myModel.getCriteria().getCriterias().add(new BinCriteriaElement());
    }

    /**
     * Fuses the view and model together.
     */
    private void bind()
    {
        myView.getCriterionView().itemsProperty().bindBidirectional(myBindProperty);
        myView.getAddButton().setOnAction(this);
    }
}
