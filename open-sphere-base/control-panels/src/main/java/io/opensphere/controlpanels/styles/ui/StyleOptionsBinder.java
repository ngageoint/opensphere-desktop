package io.opensphere.controlpanels.styles.ui;

import java.util.Observable;
import java.util.Observer;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;

import io.opensphere.controlpanels.styles.model.StyleOptions;
import io.opensphere.controlpanels.styles.model.Styles;
import io.opensphere.core.util.fx.FXUtilities;

/**
 * Binds the UI components to the appropriate model components and keeps both
 * model and UI synchronized.
 */
public class StyleOptionsBinder implements Observer
{
    /**
     * The color property for binding.
     */
    private ObjectProperty<Color> myColorProperty;

    /**
     * The model to bind to.
     */
    private final StyleOptions myModel;

    /**
     * The selected style property for binding.
     */
    private ObjectProperty<Styles> mySelectedStyleProperty;

    /**
     * The size property for binding.
     */
    private IntegerProperty mySizeProperty;

    /**
     * The view to bind to.
     */
    private final StyleOptionsView myView;

    /**
     * Constructs a new binder for the style options UI.
     *
     * @param view The view to bind to.
     * @param model The model to bind to.
     */
    public StyleOptionsBinder(StyleOptionsView view, StyleOptions model)
    {
        myView = view;
        myModel = model;
        bind();
    }

    /**
     * Stops synchronizing the view and model.
     */
    public void close()
    {
        myModel.deleteObserver(this);

        myView.getSize().valueProperty().unbindBidirectional(mySizeProperty);
        myView.getColorPicker().valueProperty().unbindBidirectional(myColorProperty);
        myView.getStylePicker().valueProperty().unbindBidirectional(mySelectedStyleProperty);
        myView.getStylePicker().setItems(FXCollections.observableArrayList());
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (StyleOptions.COLOR_PROP.equals(arg))
        {
            colorFromModel();
        }
        else if (StyleOptions.SIZE_PROP.equals(arg))
        {
            mySizeProperty.set(myModel.getSize());
        }
        else if (StyleOptions.STYLE_PROP.equals(arg))
        {
            mySelectedStyleProperty.set(myModel.getStyle());
        }
    }

    /**
     * Binds the view to the model.
     */
    private void bind()
    {
        mySizeProperty = new SimpleIntegerProperty(myModel.getSize());
        mySizeProperty.addListener(this::sizeToModel);
        mySelectedStyleProperty = new SimpleObjectProperty<Styles>(myModel.getStyle());
        mySelectedStyleProperty.addListener(this::styleToModel);
        myColorProperty = new SimpleObjectProperty<>();
        myColorProperty.addListener(this::colorToModel);

        myView.getSize().valueProperty().bindBidirectional(mySizeProperty);
        myView.getColorPicker().valueProperty().bindBidirectional(myColorProperty);
        myView.getStylePicker().valueProperty().bindBidirectional(mySelectedStyleProperty);
        myView.getStylePicker().setItems(myModel.getStyles());

        colorFromModel();

        myModel.addObserver(this);
    }

    /**
     * Sets the color on the view.
     */
    private void colorFromModel()
    {
        java.awt.Color awtColor = myModel.getColor();
        if (awtColor != null)
        {
            Color fxColor = FXUtilities.fromAwtColor(awtColor);
            myColorProperty.set(fxColor);
        }
    }

    /**
     * Sets the color on the model.
     *
     * @param observable The color.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void colorToModel(ObservableValue<? extends Color> observable, Color oldValue, Color newValue)
    {
        java.awt.Color color = FXUtilities.toAwtColor(newValue);
        myModel.setColor(color);
    }

    /**
     * Sets the size on the model.
     *
     * @param observable The size.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void sizeToModel(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
    {
        myModel.setSize(newValue.intValue());
    }

    /**
     * Sets the selected style on the model.
     *
     * @param observable The selected style.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void styleToModel(ObservableValue<? extends Styles> observable, Styles oldValue, Styles newValue)
    {
        myModel.setStyle(newValue);
    }
}
