package io.opensphere.controlpanels.styles.ui;

import java.util.Observable;
import java.util.Observer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

import io.opensphere.controlpanels.styles.model.LabelOptions;
import io.opensphere.core.util.fx.FXUtilities;

/**
 * Binds the {@link LabelOptionsView} to a {@link LabelOptions} model so that
 * the view and model's values are synchronized.
 */
public class LabelOptionsBinder implements Observer
{
    /**
     * The color binding property.
     */
    private ObjectProperty<Color> myColorProperty;

    /**
     * The model to bind to.
     */
    private final LabelOptions myModel;

    /**
     * The size binding property.
     */
    private ObjectProperty<Integer> mySizeProperty;

    /**
     * The view to bind to.
     */
    private final LabelOptionsView myView;

    /**
     * Constructs a new label options binder.
     *
     * @param view The view to bind to.
     * @param model The model to bind to.
     */
    public LabelOptionsBinder(LabelOptionsView view, LabelOptions model)
    {
        myView = view;
        myModel = model;
        bind();
    }

    /**
     * Stops synchronizing the view and model values.
     */
    public void close()
    {
        myModel.deleteObserver(this);
        myView.getColorPicker().valueProperty().unbindBidirectional(myColorProperty);
        myView.getSizePicker().getValueFactory().valueProperty().unbindBidirectional(mySizeProperty);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (LabelOptions.COLOR_PROP.equals(arg))
        {
            myColorProperty.set(FXUtilities.fromAwtColor(myModel.getColor()));
        }
        else if (LabelOptions.SIZE_PROP.equals(arg))
        {
            mySizeProperty.set(Integer.valueOf(myModel.getSize()));
        }
    }

    /**
     * Binds the view and the model together.
     */
    private void bind()
    {
        mySizeProperty = new SimpleObjectProperty<>(Integer.valueOf(myModel.getSize()));
        mySizeProperty.addListener(this::sizeToModel);
        myColorProperty = new SimpleObjectProperty<>(FXUtilities.fromAwtColor(myModel.getColor()));
        myColorProperty.addListener(this::colorToModel);

        myView.getColorPicker().valueProperty().bindBidirectional(myColorProperty);
        myView.getSizePicker().getValueFactory().valueProperty().bindBidirectional(mySizeProperty);

        myModel.addObserver(this);
    }

    /**
     * Sets the color in the model.
     *
     * @param observable The color.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void colorToModel(ObservableValue<? extends Color> observable, Color oldValue, Color newValue)
    {
        myModel.setColor(FXUtilities.toAwtColor(newValue));
    }

    /**
     * Sets the new size in the model.
     *
     * @param observable The size.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void sizeToModel(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue)
    {
        myModel.setSize(newValue.intValue());
    }
}
