package io.opensphere.featureactions.editor.ui;

import java.util.Observer;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import io.opensphere.controlpanels.styles.model.StyleOptions;
import io.opensphere.controlpanels.styles.model.Styles;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.featureactions.editor.controller.FilterActionAdapter;
import io.opensphere.featureactions.editor.controller.SimpleFeatureActionController;
import io.opensphere.featureactions.editor.model.CriteriaOptions;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.mantle.controller.DataTypeController;

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

    /** The use icon model observer. */
    private Observer myUseIconObserver;

    /** The use icon UI listener. */
    private ChangeListener<Boolean> myUseIconListener;

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
        Platform.runLater(() -> updateVisibleElements());
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
        unbindUseIconCheckBox();
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
        bindUseIconCheckBox();
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
        myView.getUseIconCheckBox().setVisible(style);
        StyleAction styleAction = getStyleAction();
        if (styleAction != null)
        {
            myView.getUseIconCheckBox().setSelected(styleAction.getStyleOptions().getStyle() == Styles.ICON);
        }
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

    /**
     * Bi-directionally binds the 'use icon' checkbox to the model.
     */
    private void bindUseIconCheckBox()
    {
        StyleAction styleAction = getStyleAction();
        if (styleAction != null)
        {
            myView.getUseIconCheckBox().setSelected(styleAction.getStyleOptions().getStyle() == Styles.ICON);
            myUseIconObserver = (o, arg) ->
            {
                if (StyleOptions.STYLE_PROP.equals(arg))
                {
                    myView.getUseIconCheckBox().setSelected(((StyleOptions)o).getStyle() == Styles.ICON);
                }
            };
            styleAction.getStyleOptions().addObserver(myUseIconObserver);
            if (myView.getUseIconCheckBox().isSelected())
            {
                myView.getColorPicker().valueProperty().unbindBidirectional(myModel.colorProperty());
                myView.getColorPicker().setDisable(true);
                myView.getColorPicker().setValue(FXUtilities.fromAwtColor(styleAction.getStyleOptions().getHoldColor()));
            }
        }
        myUseIconListener = (obs, o, n) ->
        {
            StyleAction action = getStyleAction();
            if (action != null)
            {
                action.getStyleOptions().setStyle(n.booleanValue() ? Styles.ICON : Styles.POINT);
                if (n.booleanValue())
                {
                    myView.getColorPicker().valueProperty().unbindBidirectional(myModel.colorProperty());
                    action.getStyleOptions().setHoldColor(FXUtilities.toAwtColor(myModel.getColor()));
                    myModel.setColor(Color.WHITE);
                    myView.getColorPicker().setDisable(true);
                }
                else
                {
                    myView.getColorPicker().valueProperty().bindBidirectional(myModel.colorProperty());
                    myView.getColorPicker().setValue(FXUtilities.fromAwtColor(action.getStyleOptions().getHoldColor()));
                    myView.getColorPicker().setDisable(false);
                }
            }
        };
        myView.getUseIconCheckBox().selectedProperty().addListener(myUseIconListener);
    }

    /**
     * Bi-directionally unbinds the 'use icon' checkbox from the model.
     */
    private void unbindUseIconCheckBox()
    {
        StyleAction styleAction = getStyleAction();
        if (styleAction != null && myUseIconObserver != null)
        {
            styleAction.getStyleOptions().deleteObserver(myUseIconObserver);
        }
        myView.getUseIconCheckBox().selectedProperty().removeListener(myUseIconListener);
    }

    /**
     * Gets the style action from the feature action.
     *
     * @return the style action, or null
     */
    private StyleAction getStyleAction()
    {
        return (StyleAction)myModel.getFeatureAction().getActions().stream().filter(a -> a instanceof StyleAction).findAny()
                .orElse(null);
    }
}
