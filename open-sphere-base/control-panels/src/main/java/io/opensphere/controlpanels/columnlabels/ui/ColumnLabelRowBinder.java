package io.opensphere.controlpanels.columnlabels.ui;

import java.util.Observable;
import java.util.Observer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.controlpanels.columnlabels.model.ColumnLabels;

/**
 * Binds the {@link ColumnLabelRowView} and the {@link ColumnLabel} model so
 * that there values are synchronized.
 */
public class ColumnLabelRowBinder implements Observer, EventHandler<ActionEvent>
{
    /**
     * Contains all column configurations.
     */
    private final ColumnLabels myContainerModel;

    /**
     * The model to bind.
     */
    private final ColumnLabel myModel;

    /**
     * The selected column binding property.
     */
    private StringProperty mySelectedColumn;

    /**
     * The show column name binding property.
     */
    private BooleanProperty myShowColumnName;

    /**
     * The view to bind.
     */
    private final ColumnLabelRowView myView;

    /**
     * Constructs a new column label binder.
     *
     * @param view The view to bind to.
     * @param containerModel The model that contains all column label
     *            configurations.
     * @param model The model to bind to.
     */
    public ColumnLabelRowBinder(ColumnLabelRowView view, ColumnLabels containerModel, ColumnLabel model)
    {
        myView = view;
        myContainerModel = containerModel;
        myModel = model;
        bind();
        checkButtons();
    }

    /**
     * Stops synchronizing the view and model.
     */
    public void close()
    {
        myModel.deleteObserver(this);

        myView.getColumns().valueProperty().unbindBidirectional(mySelectedColumn);
        myView.getShowColumnName().selectedProperty().unbindBidirectional(myShowColumnName);
        myView.getColumns().setItems(FXCollections.observableArrayList());
    }

    @Override
    public void handle(ActionEvent event)
    {
        if (event.getSource().equals(myView.getRemoveButton()))
        {
            myContainerModel.getColumnsInLabel().remove(myModel);
        }
        else if (event.getSource().equals(myView.getMoveDownButton()))
        {
            moveLabel(1);
            checkButtons();
        }
        else if (event.getSource().equals(myView.getMoveUpButton()))
        {
            moveLabel(-1);
            checkButtons();
        }
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (ColumnLabel.COLUMN_PROP.equals(arg))
        {
            mySelectedColumn.set(myModel.getColumn());
        }
        else if (ColumnLabel.SHOW_COLUMN_NAME_PROP.equals(arg))
        {
            myShowColumnName.set(myModel.isShowColumnName());
        }
    }

    /**
     * Binds the view and model.
     */
    private void bind()
    {
        mySelectedColumn = new SimpleStringProperty(myModel.getColumn());
        mySelectedColumn.addListener(this::columnToModel);
        myShowColumnName = new SimpleBooleanProperty(myModel.isShowColumnName());
        myShowColumnName.addListener(this::showColumnNameToModel);

        myView.getColumns().setItems(myModel.getAvailableColumns());
        myView.getColumns().valueProperty().bindBidirectional(mySelectedColumn);
        myView.getShowColumnName().selectedProperty().bindBidirectional(myShowColumnName);

        myModel.addObserver(this);

        myView.getRemoveButton().setOnAction(this);
        myView.getMoveDownButton().setOnAction(this);
        myView.getMoveUpButton().setOnAction(this);
    }

    /**
     * Tests to see if any of the move buttons need to be disabled.
     */
    private void checkButtons()
    {
        int indexOf = myContainerModel.getColumnsInLabel().indexOf(myModel);

        if (indexOf == 0)
        {
            myView.getMoveUpButton().setDisable(true);
        }
        else
        {
            myView.getMoveUpButton().setDisable(false);
        }

        if (indexOf == myContainerModel.getColumnsInLabel().size() - 1)
        {
            myView.getMoveDownButton().setDisable(true);
        }
        else
        {
            myView.getMoveDownButton().setDisable(false);
        }
    }

    /**
     * Sets the selected column on the model.
     *
     * @param observable The selected column.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void columnToModel(ObservableValue<? extends String> observable, String oldValue, String newValue)
    {
        myModel.setColumn(newValue);
    }

    /**
     * Moves the model within the list of column labels the specified number of
     * positions.
     *
     * @param delta A positive or negative number of positions to move the
     *            model.
     */
    private void moveLabel(int delta)
    {
        int indexOf = myContainerModel.getColumnsInLabel().indexOf(myModel);
        myContainerModel.getColumnsInLabel().remove(indexOf);
        myContainerModel.getColumnsInLabel().add(indexOf + delta, myModel);
    }

    /**
     * Sets show column name on the model.
     *
     * @param observable The selected column.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void showColumnNameToModel(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
    {
        myModel.setShowColumnName(newValue.booleanValue());
    }
}
