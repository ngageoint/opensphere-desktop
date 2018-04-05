package io.opensphere.featureactions.editor.ui;

import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.featureactions.editor.controller.FilterActionAdapter;
import io.opensphere.featureactions.editor.controller.SimpleFeatureActionController;
import io.opensphere.featureactions.editor.model.CriteriaOptions;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.mantle.controller.DataTypeController;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * Binds the {@link SimpleFeatureActionRowView} to the
 * {@link SimpleFeatureAction} model.
 */
public class SimpleFeatureActionRowBinder
{
    /** The controller that copies or removes the action. */
    private final SimpleFeatureActionController myController;

    /** Handles when the user clicks the copy button. */
    private final EventHandler<ActionEvent> myCopyHandler = e -> handleCopy();

    /** The model to bind to. */
    private final SimpleFeatureAction myModel;

    /** Handles when the user changes the {@link CriteriaOptions}. */
    private final ChangeListener<CriteriaOptions> myOptionsListener = (o, v0, v1) -> updateVisibleElements();

    /** Handles when the user clicks the remove button. */
    private final EventHandler<ActionEvent> myRemovedHandler = e -> handleRemove();

    /** The view to bind to. */
    private final SimpleFeatureActionRowView myView;

    /**
     * Constructs a new binder.
     *
     * @param view The view to bind.
     * @param typeController Used to get the available columns.
     * @param actions Contains all actions the layer.
     * @param group The group the action belongs to.
     * @param model The model to bind to.
     */
    public SimpleFeatureActionRowBinder(SimpleFeatureActionRowView view, DataTypeController typeController,
            SimpleFeatureActions actions, SimpleFeatureActionGroup group, SimpleFeatureAction model)
    {
        myView = view;
        myModel = model;
        myController = new SimpleFeatureActionController(typeController, actions, group, myModel);
        bindUI();
        FXUtilities.runOnFXThreadAndWait(() -> updateVisibleElements());
    }

    /** Unbinds the ui from the model. */
    public void close()
    {
        myView.getEnabled().selectedProperty().unbindBidirectional(myModel.getFeatureAction().enabledProperty());
        myView.getName().textProperty().unbindBidirectional(myModel.getFeatureAction().nameProperty());
        myView.getOptions().valueProperty().unbindBidirectional(myModel.getOption());
        myView.getField().valueProperty().unbindBidirectional(myModel.getColumn());
        myView.getValue().textProperty().unbindBidirectional(myModel.getValue());
        myView.getMinimumValue().textProperty().unbindBidirectional(myModel.getMinimumValue());
        myView.getMaximumValue().textProperty().unbindBidirectional(myModel.getMaximumValue());
        myView.getColorPicker().valueProperty().unbindBidirectional(myModel.colorProperty());
        myView.getCopyButton().setOnAction(null);
        myView.getRemoveButton().setOnAction(null);
        myModel.getOption().removeListener(myOptionsListener);
    }

    /**
     * Binds the ui to the model.
     */
    private void bindUI()
    {
        myView.getEnabled().selectedProperty().bindBidirectional(myModel.getFeatureAction().enabledProperty());
        myView.getName().textProperty().bindBidirectional(myModel.getFeatureAction().nameProperty());
        myView.getField().getItems().clear();
        myView.getField().getItems().addAll(myModel.getAvailableColumns());
        myView.getField().valueProperty().bindBidirectional(myModel.getColumn());
        myView.getOptions().setItems(FXCollections.observableArrayList(CriteriaOptions.values()));
        myView.getOptions().valueProperty().bindBidirectional(myModel.getOption());
        myView.getValue().textProperty().bindBidirectional(myModel.getValue());
        myView.getMinimumValue().textProperty().bindBidirectional(myModel.getMinimumValue());
        myView.getMaximumValue().textProperty().bindBidirectional(myModel.getMaximumValue());
        myView.getColorPicker().valueProperty().bindBidirectional(myModel.colorProperty());
        myView.getCopyButton().setOnAction(myCopyHandler);
        myView.getRemoveButton().setOnAction(myRemovedHandler);
        myView.setEditListener(() -> updateVisibleElements());
        myModel.getOption().addListener(myOptionsListener);
    }

    /** Has the controller copy the action as a new action within the group. */
    private void handleCopy()
    {
        myController.copy();
    }

    /** Decide what to show in the simple GUI based on current conditions. */
    private void updateVisibleElements()
    {
        boolean simple = FilterActionAdapter.isSimpleModel(myModel);
        boolean range = myModel.getOption().get() == CriteriaOptions.RANGE;
        myView.getValue().setVisible(simple && !range);
        myView.getMinimumValue().setVisible(simple && range);
        myView.getMaximumValue().setVisible(simple && range);
        myView.getField().setVisible(simple);
        myView.getOptions().setVisible(simple);
        myView.getComplexFilterMask().setVisible(!simple);

        boolean style = myModel.getFeatureAction().getActions().stream().anyMatch(a -> a instanceof StyleAction);
        myView.getColorPicker().setVisible(style);
        if (myView.getIconPicker() != null)
        {
            myView.getIconPicker().setVisible(style);
        }
        myView.getStyleAbsentMask().setVisible(!style);
    }

    /** Invokes the controller to remove the action from the group. */
    private void handleRemove()
    {
        myController.remove();
    }
}
