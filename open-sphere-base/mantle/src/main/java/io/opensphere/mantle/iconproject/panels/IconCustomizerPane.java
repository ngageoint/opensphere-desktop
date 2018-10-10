package io.opensphere.mantle.iconproject.panels;

import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.iconproject.model.PanelModel;

/** Panel for building custom icons. */
public class IconCustomizerPane extends BorderPane
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(IconCustomizerPane.class);

    /** The rotation value model. */
    private final DoubleProperty myRotation = new SimpleDoubleProperty(0.);

    /** The scale value model. */
    private final DoubleProperty myScale = new SimpleDoubleProperty(1.);

    /** The X value model. */
    private final DoubleProperty myXPosition = new SimpleDoubleProperty(0.);

    /** The Y value model. */
    private final DoubleProperty myYPosition = new SimpleDoubleProperty(0.);

    /** The save state selection. */
    private final BooleanProperty mySave = new SimpleBooleanProperty(false);

    /** The ColorPicker that determines the icon color. */
    private ColorPicker myColorPicker;

    /** The ImageView that renders the modified icon. */
    private ImageView myIconView;

    /** The spinner width. */
    private final double mySpinnerWidth = 59.0;

    /** The IconRecord of the displayed Icon. */
    private final IconRecord myIconRecord;

    /** The purple HBox containing the displayed icon. */
    private HBox myIconDisplay;

    /** The Color chosen for customized icon. */
    private Color myColor;

    /**
     * Constructs a new panel containing the icon customizer.
     *
     * @param owner the AWT Window.
     * @param panelModel the panel model
     */
    public IconCustomizerPane(Window owner, PanelModel panelModel)
    {
        myIconRecord = panelModel.getSelectedRecord().get();

        setCenter(myIconDisplay = createImageView());
        setRight(createRight());
        setTop(createTop());
        VBox bottom = createBottom();
        BorderPane.setMargin(bottom, new Insets(5, 0, 0, 0));
        setBottom(bottom);
    }

    /**
     * Creates the top box, which contains the color and scale controls.
     *
     * @return an HBox containing icon scale & color controls.
     */
    private HBox createTop()
    {
        HBox topMenuBar = new HBox();
        Slider sizeSlider = new Slider(0, 3, 1);
        sizeSlider.setOrientation(Orientation.HORIZONTAL);
        sizeSlider.setShowTickMarks(true);
        sizeSlider.setShowTickLabels(true);
        sizeSlider.setMajorTickUnit(.5);
        sizeSlider.valueProperty().bindBidirectional(myScale);

        double maxScale;
        if (myIconDisplay.getBoundsInLocal().getWidth() < 150)
        {
            maxScale = 4;
        }
        else
        {
            maxScale = 3;
        }
        Spinner<Number> sizeSpinner = new Spinner<>(0.0, maxScale, 1., 0.1);
        sizeSpinner.setPrefWidth(mySpinnerWidth);
        sizeSpinner.getValueFactory().valueProperty().bindBidirectional(myScale);
        sizeSpinner.setEditable(true);
        Label sizeLabel = new Label("Scale: ", sizeSpinner);
        sizeLabel.setContentDisplay(ContentDisplay.RIGHT);
        AnchorPane.setRightAnchor(sizeLabel, 0.);

        myColorPicker = new ColorPicker();

        myColorPicker.setOnAction(event ->
        {
            myColorPicker.show();
            myColor = myColorPicker.getValue();
            updateImageColor();
        });
        topMenuBar.setSpacing(5);
        topMenuBar.getChildren().addAll(myColorPicker, sizeLabel, sizeSpinner);
        return topMenuBar;
    }

    /**
     * Creates the right box, which contains the rotation controls.
     *
     * @return A VBox containing the rotation controls.
     */
    private VBox createRight()
    {
        VBox box = new VBox(8);
        box.setAlignment(Pos.TOP_CENTER);

        Slider rotationSlider = new Slider(-180, 180, 0);
        rotationSlider.setOrientation(Orientation.VERTICAL);
        rotationSlider.setShowTickMarks(true);
        rotationSlider.setShowTickLabels(true);
        rotationSlider.setMajorTickUnit(45);
        rotationSlider.valueProperty().bindBidirectional(myRotation);

        Spinner<Number> rotationSpinner = new Spinner<>(-180., 180., 0.);
        rotationSpinner.setPrefWidth(40.);
        rotationSpinner.getValueFactory().valueProperty().bindBidirectional(myRotation);
        rotationSpinner.setEditable(true);
        rotationSpinner.getStyleClass().clear();

        Label rotationLabel = new Label("Rotation: ", rotationSpinner);
        rotationLabel.setContentDisplay(ContentDisplay.BOTTOM);

        VBox.setVgrow(rotationSlider, Priority.ALWAYS);
        VBox.setVgrow(rotationSpinner, Priority.NEVER);
        box.getChildren().addAll(rotationSlider, rotationLabel, rotationSpinner);

        return box;
    }

    /**
     * Creates the bottom box, which contains the icon size & position controls.
     *
     * @return VBox containing the size & position controls.
     */
    private VBox createBottom()
    {
        VBox bottomBox = new VBox();
        bottomBox.setAlignment(Pos.TOP_LEFT);

        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.BASELINE_LEFT);
        final double spinnerStep = 5.;
        final double spinnerInitial = 0.;
        Spinner<Number> xSpinner = new Spinner<>(-100, 100, spinnerInitial, spinnerStep);
        xSpinner.setPrefWidth(mySpinnerWidth);
        xSpinner.getValueFactory().valueProperty().bindBidirectional(myXPosition);
        xSpinner.setEditable(true);

        Spinner<Number> ySpinner = new Spinner<>(-125, 125, spinnerInitial, spinnerStep);
        ySpinner.setPrefWidth(mySpinnerWidth);
        ySpinner.getValueFactory().valueProperty().bindBidirectional(myYPosition);
        ySpinner.setEditable(true);

        Label xLabel = new Label("Position:  X: ", xSpinner);
        xLabel.setContentDisplay(ContentDisplay.RIGHT);

        Label yLabel = new Label("Y: ", ySpinner);
        yLabel.setContentDisplay(ContentDisplay.RIGHT);

        controlBox.getChildren().addAll(xLabel, xSpinner, yLabel, ySpinner);

        HBox saveInfo = new HBox();

        Label helpInfo = new Label("Replace Existing Icon?");
        helpInfo.setFont(Font.font(helpInfo.getFont().getFamily(), FontPosture.ITALIC, 11));
        helpInfo.setPadding(new Insets(0, 3, 0, 0));

        CheckBox saveState = new CheckBox();
        saveState.selectedProperty().set(false);
        saveState.selectedProperty().bindBidirectional(mySave);

        saveInfo.getChildren().addAll(helpInfo, saveState);
        saveInfo.setSpacing(5);
        bottomBox.getChildren().addAll(controlBox, saveInfo);
        return bottomBox;
    }

    /**
     * Creates the ImageView which renders the selected icon.
     *
     * @return the Hbox containing the icon.
     */
    private HBox createImageView()
    {
        if (myIconRecord == null)
        {
    	    return null;
        }

        HBox iconDisplayer = new HBox();
        iconDisplayer.setAlignment(Pos.CENTER);
        iconDisplayer.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
                + "-fx-border-insets: 5;" + "-fx-border-radius: 5;" + "-fx-border-color: purple;");
        iconDisplayer.setId("BoxStyle");
        
        myIconView = new ImageView(myIconRecord.getImageURL().toString());
        myIconView.rotateProperty().bind(myRotation);
        myIconView.translateXProperty().bindBidirectional(myXPosition);
        myIconView.translateYProperty().bindBidirectional(myYPosition);

        BufferedImage iconActual = null;
        try
        {
            iconActual = ImageIO.read(myIconRecord.getImageURL());
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to read the icon.", e);
            return null;
        }

        if (iconActual.getWidth() > 150)
        {
            myIconView.setFitWidth(150);
            myIconView.setFitHeight(150);
        }
        else if (iconActual.getWidth() < 40)
        {
            myIconView.setFitWidth(50);
            myIconView.setFitHeight(50);
        }
        else
        {
            myIconView.setFitWidth(iconActual.getTileWidth());
            myIconView.setFitHeight(iconActual.getHeight());
        }

        myIconView.scaleXProperty().bind(myScale);
        myIconView.scaleYProperty().bind(myScale);

        iconDisplayer.getChildren().addAll(myIconView);
        return iconDisplayer;
    }

    /**
     * Updates the color of the selected icon when the ColorPicker selection
     * changes.
     */
    private void updateImageColor()
    {
        Lighting lighting = new Lighting();
        lighting.setDiffuseConstant(1.0);
        lighting.setSpecularConstant(0.75);
        lighting.setSpecularExponent(0.0);
        lighting.setSurfaceScale(0.0);
        lighting.setLight(new Light.Distant(45, 45, myColor));

        if (myIconView != null)
        {
            myIconView.setEffect(lighting);
        }
    }

    /**
     * Retrieves the final processed image as a BufferedImage.
     *
     * @return the image to be saved.
     */
    public WritableImage getFinalImage()
    {
        WritableImage iconOut = null;
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        myIconDisplay.setStyle(null);
        if (myIconView.getImage() != null)
        {
            iconOut = myIconDisplay.snapshot(parameters, null);
        }
        return iconOut;
    }

    /**
     * Generates and retrieves a name for the image, if it is not null.
     *
     * @return the image name.
     */
    public String getImageName()

    {
        if (myColor != null)
        {
            return myIconRecord != null ? myIconRecord.getName() + "_" + myIconView.getRotate() + "_" + myColor.getRed() + "-"
                    + myColor.getGreen() + "-" + myColor.getBlue() : null;
        }

        return myIconRecord != null ? myIconRecord.getName() + "_" + myIconView.getRotate() : null;
    }

    /**
     * Sends the Icon Record to be used elsewhere.
     *
     * @return the currently modified icon.
     */
    public boolean getSaveState()
    {
        return mySave.get();
    }

    /**
     * Gets the icon being modified.
     *
     * @return the current Icon being edited.
     */
    public IconRecord getIconRecord()
    {
        return myIconRecord;
    }

    /**
     * Sends the X translation value to be used elsewhere.
     *
     * @return the current Icon's X Position.
     */
    public int getXPosition()
    {
        return myXPosition.getValue().intValue();
    }

    /**
     * Sends the Y translation value to be used elsewhere.
     *
     * @return the current Icon's Y Position.
     */
    public int getYPosition()
    {
        return myYPosition.getValue().intValue();
    }
}
