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
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
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
     * Constructor.
     *
     * @param record the icon record
     */
    public IconRotPane(IconRecord record)
    {
     //   setTop(createTop());
        setCenter(createImageView(record));
        setBottom(createControlPanel());
    }

    private AnchorPane createTop()
    {
        AnchorPane top = new AnchorPane();

        CheckBox saveState = new CheckBox();
        saveState.selectedProperty().set(false);
        saveState.selectedProperty().bindBidirectional(mySave);

        Label message = new Label("Replace existing icon?");
        message.setFont(Font.font(message.getFont().getFamily(), FontPosture.ITALIC, 11));
        message.setContentDisplay(ContentDisplay.RIGHT);
        AnchorPane.setRightAnchor(top, 0.);
        top.getChildren().addAll(message, saveState);
        return top;
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

//        BufferedImage iconActual = null;
//        try
//        {
//            iconActual = ImageIO.read(record.getImageURL());
//        }
//        catch (IOException e)
//        {
//        }
//        if (iconActual.getWidth() > 150)
//        {
//            imageView.setFitWidth(150);
//            imageView.setFitHeight(150);
//        }
//        else if (iconActual.getWidth() < 40)
//        {
//            imageView.setFitWidth(50);
//            imageView.setFitHeight(50);
//        }
//        else
//        {
//            imageView.setFitWidth(iconActual.getTileWidth());
//            imageView.setFitHeight(iconActual.getHeight());
//        }

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
        HBox box = new HBox(8);
        VBox box2 = new VBox();
        box.setAlignment(Pos.CENTER_LEFT);
        Slider slider = createSlider();
        Spinner<Number> spinner = createSpinner();
        HBox.setHgrow(slider, Priority.ALWAYS);
        HBox.setHgrow(spinner, Priority.NEVER);
        
        HBox Box3 = new HBox();
        CheckBox saveState = new CheckBox();
        saveState.selectedProperty().set(false);
        saveState.selectedProperty().bindBidirectional(mySave);

        Label message = new Label("Replace existing icon?");
        message.setFont(Font.font(message.getFont().getFamily(), FontPosture.ITALIC, 11));
        message.setPadding(new Insets(0,5.,0,0));
        message.setContentDisplay(ContentDisplay.RIGHT);
        Box3.getChildren().addAll(message,saveState);
        
        box.getChildren().addAll(slider, spinner);
        box2.getChildren().addAll(box,Box3);
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

    public boolean getSaveState()
    {
        return mySave.get();
    }
}
