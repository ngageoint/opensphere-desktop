package io.opensphere.mantle.iconproject.panels.transform;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;

/**
 * An abstract base panel for transform operations.
 */
public class AbstractTransform extends VBox
{
    /**
     * Creates a spinner, bound to the supplied double property, with the
     * supplied minimum and maximum values.
     *
     * @param doubleProperty the property to use as the model of the spinner.
     * @param min the minimum value of the spinner.
     * @param max the maximum value of the spinner.
     * @param defaultValue the initial value of the spinner.
     * @return a spinner configured with the supplied parameters.
     */
    protected Spinner<Double> createSpinner(DoubleProperty doubleProperty, double min, double max, double defaultValue)
    {
        Spinner<Double> spinner = new Spinner<>(min, max, defaultValue);
        spinner.setMaxWidth(75);
        spinner.setOnScroll(e ->
        {
            if (e.getDeltaY() > 0)
            {
                spinner.increment();
            }
            else
            {
                spinner.decrement();
            }
        });
        SpinnerValueFactory<Double> factory = spinner.getValueFactory();
        factory.valueProperty().addListener((obs, ov, nv) -> doubleProperty.set(nv));
        doubleProperty.addListener((obs, ov, nv) -> factory.valueProperty().set(nv.doubleValue()));
//        factory.setConverter(new NoDecimalStringConverter());

        return spinner;
    }

    /**
     * Creates a slider, bound to the supplied double property, with the
     * supplied minimum and maximum values.
     *
     * @param property the property to use as the model of the slider.
     * @param min the minimum value of the slider.
     * @param max the maximum value of the slider.
     * @param initialValue the initial value of the slider
     * @return a slider configured with the supplied parameters.
     */
    protected Slider createSlider(DoubleProperty property, double min, double max, double initialValue)
    {
        Slider slider = new Slider(min, max, initialValue);
        slider.valueProperty().bindBidirectional(property);
        slider.setOnScroll(e ->
        {
            if (e.getDeltaY() > 0)
            {
                slider.increment();
            }
            else
            {
                slider.decrement();
            }
        });
        return slider;
    }
}