package io.opensphere.mantle.icon.impl.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import io.opensphere.mantle.icon.IconRecord;
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

/** Icon rotation pane. */
public class IconRotationPane extends BorderPane
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(IconRotationPane.class);

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
        ImageView imageView = new ImageView(record.imageURLProperty().toString());
        imageView.rotateProperty().bind(myRotation);

        BufferedImage iconActual = null;
        try
        {
            iconActual = ImageIO.read(record.imageURLProperty().get());

            if (iconActual.getWidth() > 150)
            {
                imageView.setFitWidth(150);
                imageView.setFitHeight(150);
            }
            else if (iconActual.getWidth() < 40)
            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
            }
            else
            {
                imageView.setFitWidth(iconActual.getTileWidth());
                imageView.setFitHeight(iconActual.getHeight());
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to read the icon.", e);
        }

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
