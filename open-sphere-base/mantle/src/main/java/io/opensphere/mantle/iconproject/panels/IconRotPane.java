package io.opensphere.mantle.iconproject.panels;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import io.opensphere.mantle.icon.IconRecord;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

/** Icon rotation pane. */
public class IconRotPane extends BorderPane
{
    /** The rotation value model. */
    private final IntegerProperty myRotation = new SimpleIntegerProperty();

    /** The save state selection. */
    private final BooleanProperty mySave = new SimpleBooleanProperty(false);

    /**
     * Creates the Collection Name and Sub Collection Name selection panes
     * containing controls for user input.
     *
     * @param displayIcon the icon record
     */
    public IconRotPane(IconRecord displayIcon)
    {
        setCenter(createImageView(displayIcon));
        setBottom(createControlPanel());
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

        BufferedImage iconActual = null;
        try
        {
            iconActual = ImageIO.read(record.getImageURL());
        }
        catch (IOException e)
        {
        }
        if (iconActual.getWidth() > 300)
        {
            imageView.setFitWidth(300);
            imageView.setFitHeight(300);
        }

        imageView.rotateProperty().bind(myRotation);
        return imageView;
    }

    /**
     * Creates the rotation control panel.
     *
     * @return the panel
     */
    private VBox createControlPanel()
    {
        HBox rotateControls = new HBox(8);
        VBox box2 = new VBox();
        rotateControls.setAlignment(Pos.CENTER_LEFT);
        Slider slider = createSlider();
        Spinner<Number> spinner = createSpinner();
        HBox.setHgrow(slider, Priority.ALWAYS);
        HBox.setHgrow(spinner, Priority.NEVER);

        HBox saveControls = new HBox();
        CheckBox saveState = new CheckBox();
        saveState.selectedProperty().set(false);
        saveState.selectedProperty().bindBidirectional(mySave);

        Label message = new Label("Replace existing icon?");
        message.setFont(Font.font(message.getFont().getFamily(), FontPosture.ITALIC, 11));
        message.setPadding(new Insets(0, 5, 0, 0));
        message.setContentDisplay(ContentDisplay.RIGHT);
        saveControls.getChildren().addAll(message, saveState);

        rotateControls.getChildren().addAll(slider, spinner);
        box2.getChildren().addAll(rotateControls, saveControls);
        return box2;
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

    /**
     * Gets the save state.
     *
     * @return The users choice to replace the existing icon or save as a new
     *         icon.
     */
    public boolean getSaveState()
    {
        return mySave.get();
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
}
