package io.opensphere.mantle.icon.impl.gui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import io.opensphere.mantle.icon.IconRecord;

/** Icon rotation pane. */
public class IconRotationPane extends BorderPane
{
    /** The rotation value model. */
    private final IntegerProperty myRotation = new SimpleIntegerProperty();

    /**
     * Constructor.
     *
     * @param record the icon record
     */
    public IconRotationPane(IconRecord record)
    {
        setTop(new Label("Note: The rotated icon will be saved as a new icon."));
        setCenter(createImageView(record));
        setBottom(createControlPanel());
    }

    /**
     * Gets the rotation value.
     *
     * @return the rotation value
     */
    public int getRotation()
    {
        return myRotation.get();
    }

    /**
     * Creates the image view.
     *
     * @param record the icon record
     * @return the image view
     */
    private ImageView createImageView(IconRecord record)
    {
        ImageView imageView = new ImageView(record.getImageURL().toString());
        imageView.rotateProperty().bind(myRotation);
        return imageView;
    }

    /**
     * Creates the rotation control panel.
     *
     * @return the panel
     */
    private HBox createControlPanel()
    {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        Slider slider = createSlider();
        Spinner<Number> spinner = createSpinner();
        HBox.setHgrow(slider, Priority.ALWAYS);
        HBox.setHgrow(spinner, Priority.NEVER);
        box.getChildren().addAll(slider, spinner);
        return box;
    }

    /**
     * Creates the rotation slider.
     *
     * @return the slider
     */
    private Slider createSlider()
    {
        Slider slider = new Slider(-180, 180, 0);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(45);
        slider.valueProperty().bindBidirectional(myRotation);
        return slider;
    }

    /**
     * Creates the rotation spinner.
     *
     * @return the spinner
     */
    private Spinner<Number> createSpinner()
    {
        Spinner<Number> spinner = new Spinner<>(-180, 180, 0);
        spinner.setPrefWidth(64);
        spinner.getValueFactory().valueProperty().bindBidirectional(myRotation);
        return spinner;
    }
}
