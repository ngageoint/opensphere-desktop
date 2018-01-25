package io.opensphere.controlpanels.columnlabels.ui;

import java.util.Observable;
import java.util.Observer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.controlpanels.columnlabels.model.ColumnLabels;
import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;

/**
 * Binds the {@link ColumnLabelsView} to the {@link ColumnLabels} model so that
 * their values are synchronized between the two.
 */
public class ColumnLabelsBinder implements ListDataListener<ColumnLabel>, EventHandler<ActionEvent>, Observer
{
    /**
     * The always show labels binding property.
     */
    private BooleanProperty myAlwaysShowProperty;

    /**
     * The observable list used for binding.
     */
    private ObservableList<ColumnLabel> myBindProperty;

    /**
     * The model to bind to.
     */
    private final ColumnLabels myModel;

    /**
     * The view to bind to.
     */
    private final ColumnLabelsView myView;

    /**
     * Constructs a new binder.
     *
     * @param view The view to bind to.
     * @param model The model to bind to.
     */
    public ColumnLabelsBinder(ColumnLabelsView view, ColumnLabels model)
    {
        myView = view;
        myModel = model;
        bind();
    }

    /**
     * Stops synchronizing the UI and the model.
     */
    public void close()
    {
        myModel.deleteObserver(this);
        myView.getAddButton().setOnAction(null);
        myView.getAlwaysShowLabels().selectedProperty().unbindBidirectional(myAlwaysShowProperty);
        myModel.getColumnsInLabel().removeChangeListener(this);
        myView.getColumnLabels().setItems(FXCollections.observableArrayList());
    }

    @Override
    public void elementsAdded(ListDataEvent<ColumnLabel> e)
    {
        myBindProperty.clear();
        myBindProperty.addAll(myModel.getColumnsInLabel());
    }

    @Override
    public void elementsChanged(ListDataEvent<ColumnLabel> e)
    {
        //No need to handle.
    }

    @Override
    public void elementsRemoved(ListDataEvent<ColumnLabel> e)
    {
        myBindProperty.clear();
        myBindProperty.addAll(myModel.getColumnsInLabel());
    }

    @Override
    public void handle(ActionEvent event)
    {
        myModel.getColumnsInLabel().add(new ColumnLabel());
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (ColumnLabels.ALWAYS_SHOW_LABELS_PROP.equals(arg))
        {
            myAlwaysShowProperty.set(myModel.isAlwaysShowLabels());
        }
    }

    /**
     * Sets the always show label in the model.
     *
     * @param observable The always show label.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void alwaysShowToModel(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
    {
        myModel.setAlwaysShowLabels(newValue.booleanValue());
    }

    /**
     * Binds the ui and the model.
     */
    private void bind()
    {
        myBindProperty = FXCollections.observableArrayList(myModel.getColumnsInLabel());

        myView.getColumnLabels().setItems(myBindProperty);

        myModel.getColumnsInLabel().addChangeListener(this);

        myView.getAddButton().setOnAction(this);

        myAlwaysShowProperty = new SimpleBooleanProperty(myModel.isAlwaysShowLabels());
        myAlwaysShowProperty.addListener(this::alwaysShowToModel);
        myView.getAlwaysShowLabels().selectedProperty().bindBidirectional(myAlwaysShowProperty);

        myModel.addObserver(this);
    }
}
