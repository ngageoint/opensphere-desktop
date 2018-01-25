package io.opensphere.controlpanels.styles.ui;

import java.util.Observable;
import java.util.Observer;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.util.converter.NumberStringConverter;

import io.opensphere.controlpanels.styles.model.EllipseModel;

/**
 * Binds the {@link EllipseView} to the {@link EllipseModel} so that the values
 * of the view and model are synchronized.
 */
public class EllipseBinder implements Observer
{
    /**
     * The model to bind to.
     */
    private final EllipseModel myModel;

    /**
     * The orientation binding property.
     */
    private DoubleProperty myOrientationProp;

    /**
     * The semi major binding property.
     */
    private DoubleProperty mySemiMajorProp;

    /**
     * The semi major unit binding property.
     */
    private StringProperty mySemiMajorUnitProp;

    /**
     * The semi minor binding property.
     */
    private DoubleProperty mySemiMinorProp;

    /**
     * The semi minor unit binding property.
     */
    private StringProperty mySemiMinorUnitProp;

    /**
     * The view to bind to.
     */
    private final EllipseView myView;

    /**
     * Constructs a new ellipse binder.
     *
     * @param view The view to bind to.
     * @param model The model to bind to.
     */
    public EllipseBinder(EllipseView view, EllipseModel model)
    {
        myView = view;
        myModel = model;
        bind();
    }

    /**
     * Stops synchronizing the view and the model.
     */
    public void close()
    {
        myModel.deleteObserver(this);

        Bindings.unbindBidirectional(myView.getOrientationField().textProperty(), myOrientationProp);
        Bindings.unbindBidirectional(myView.getSemiMajorField().textProperty(), mySemiMajorProp);
        Bindings.unbindBidirectional(myView.getSemiMinorField().textProperty(), mySemiMinorProp);

        myView.getSemiMajorUnitsPicker().setItems(FXCollections.observableArrayList());
        myView.getSemiMajorUnitsPicker().valueProperty().unbindBidirectional(mySemiMajorUnitProp);

        myView.getSemiMinorUnitsPicker().setItems(FXCollections.observableArrayList());
        myView.getSemiMinorUnitsPicker().valueProperty().unbindBidirectional(mySemiMinorUnitProp);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (EllipseModel.ORIENTATION_PROP.equals(arg))
        {
            myOrientationProp.set(myModel.getOrientation());
        }
        else if (EllipseModel.SEMI_MAJOR_PROP.equals(arg))
        {
            mySemiMajorProp.set(myModel.getSemiMajor());
        }
        else if (EllipseModel.SEMI_MAJOR_UNITS_PROP.equals(arg))
        {
            mySemiMajorUnitProp.set(myModel.getSemiMajorUnits());
        }
        else if (EllipseModel.SEMI_MINOR_PROP.equals(arg))
        {
            mySemiMinorProp.set(myModel.getSemiMinor());
        }
        else if (EllipseModel.SEMI_MINOR_UNITS_PROP.equals(arg))
        {
            mySemiMinorUnitProp.set(myModel.getSemiMinorUnits());
        }
    }

    /**
     * Binds the view and model.
     */
    private void bind()
    {
        mySemiMajorProp = new SimpleDoubleProperty(myModel.getSemiMajor());
        mySemiMajorProp.addListener(this::semiMajorToModel);
        mySemiMinorProp = new SimpleDoubleProperty(myModel.getSemiMinor());
        mySemiMinorProp.addListener(this::semiMinorToModel);
        myOrientationProp = new SimpleDoubleProperty(myModel.getOrientation());
        myOrientationProp.addListener(this::orientationToModel);
        mySemiMajorUnitProp = new SimpleStringProperty(myModel.getSemiMajorUnits());
        mySemiMajorUnitProp.addListener(this::semiMajorUnitsToModel);
        mySemiMinorUnitProp = new SimpleStringProperty(myModel.getSemiMinorUnits());
        mySemiMinorUnitProp.addListener(this::semiMinorUnitsToModel);

        Bindings.bindBidirectional(myView.getOrientationField().textProperty(), myOrientationProp, new NumberStringConverter());
        Bindings.bindBidirectional(myView.getSemiMajorField().textProperty(), mySemiMajorProp, new NumberStringConverter());
        Bindings.bindBidirectional(myView.getSemiMinorField().textProperty(), mySemiMinorProp, new NumberStringConverter());

        myView.getSemiMajorUnitsPicker().valueProperty().bindBidirectional(mySemiMajorUnitProp);
        myView.getSemiMajorUnitsPicker().setItems(myModel.getAvailableUnits());

        myView.getSemiMinorUnitsPicker().valueProperty().bindBidirectional(mySemiMinorUnitProp);
        myView.getSemiMinorUnitsPicker().setItems(myModel.getAvailableUnits());

        myModel.addObserver(this);
    }

    /**
     * Updates the model with the new value.
     *
     * @param observable The observable.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void orientationToModel(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
    {
        myModel.setOrientation(newValue.doubleValue());
    }

    /**
     * Updates the model with the new value.
     *
     * @param observable The observable.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void semiMajorToModel(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
    {
        myModel.setSemiMajor(newValue.doubleValue());
    }

    /**
     * Updates the model with the new value.
     *
     * @param observable The observable.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void semiMajorUnitsToModel(ObservableValue<? extends String> observable, String oldValue, String newValue)
    {
        myModel.setSemiMajorUnits(newValue);
    }

    /**
     * Updates the model with the new value.
     *
     * @param observable The observable.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void semiMinorToModel(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
    {
        myModel.setSemiMinor(newValue.doubleValue());
    }

    /**
     * Updates the model with the new value.
     *
     * @param observable The observable.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void semiMinorUnitsToModel(ObservableValue<? extends String> observable, String oldValue, String newValue)
    {
        myModel.setSemiMinorUnits(newValue);
    }
}
