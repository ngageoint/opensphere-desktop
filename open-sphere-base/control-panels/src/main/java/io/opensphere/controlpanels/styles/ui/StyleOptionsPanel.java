package io.opensphere.controlpanels.styles.ui;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

import io.opensphere.controlpanels.styles.model.StyleOptions;
import io.opensphere.controlpanels.styles.model.Styles;

/**
 * The panel that allows the user to change the bulls eye's style.
 */
public class StyleOptionsPanel extends GridPane implements StyleOptionsView
{
    /**
     * Synchronized the model values with the UI.
     */
    private final StyleOptionsBinder myBinder;

    /**
     * The color picker.
     */
    private ColorPicker myColorPicker;

    /**
     * The model.
     */
    private final StyleOptions myModel;

    /**
     * The size slider.
     */
    private Slider mySizeSlider;

    /**
     * The different styles picker.
     */
    private ComboBox<Styles> myStylePicker;

    /**
     * Creates the {@link StyleOptionsPanel}.
     *
     * @param model The model to edit.
     */
    public StyleOptionsPanel(StyleOptions model)
    {
        myModel = model;
        createUI();
        myBinder = new StyleOptionsBinder(this, myModel);
    }

    /**
     * Stops updating the model.
     */
    public void close()
    {
        myBinder.close();
    }

    @Override
    public ColorPicker getColorPicker()
    {
        return myColorPicker;
    }

    @Override
    public Slider getSize()
    {
        return mySizeSlider;
    }

    @Override
    public ComboBox<Styles> getStylePicker()
    {
        return myStylePicker;
    }

    /**
     * Creates the UI.
     */
    private void createUI()
    {
        setVgap(5);
        setHgap(5);

        Label styleLabel = new Label("Style:");
        myColorPicker = new ColorPicker();
        myColorPicker.setStyle("-fx-color-label-visible: false ;");
        myColorPicker.setTooltip(new Tooltip("Sets the icon color"));
        myStylePicker = new ComboBox<>();
        myStylePicker.setTooltip(new Tooltip("Sets the color/shape used to render the feature"));

        add(styleLabel, 0, 0);
        add(myColorPicker, 1, 0);
        add(myStylePicker, 2, 0);

        Label sizeLabel = new Label("Size:");
        mySizeSlider = new Slider(5, 50, 5);
        mySizeSlider.setTooltip(new Tooltip("Sets the size of the feature"));

        add(sizeLabel, 0, 1);
        add(mySizeSlider, 1, 1, 3, 1);
    }
}
