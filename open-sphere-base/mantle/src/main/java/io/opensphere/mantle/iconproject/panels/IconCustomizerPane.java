package io.opensphere.mantle.iconproject.panels;

import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

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

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.iconproject.model.PanelModel;

/** Panel for building custom icons. */
public class IconCustomizerPane extends BorderPane
{
    /** The rotation value model. */
    private final DoubleProperty myRotation = new SimpleDoubleProperty(0.);

    /** The scale value model. */
    private final DoubleProperty myScale = new SimpleDoubleProperty(1.);

    /** The X value model. */
    private final DoubleProperty myXPos = new SimpleDoubleProperty(0.);

    /** The Y value model. */
    private final DoubleProperty myYPos = new SimpleDoubleProperty(0.);

    /** The save state selection. */
    private final BooleanProperty mySave = new SimpleBooleanProperty(false);

    /** The ColorPicker that determines the icon color. */
    private ColorPicker myColorPicker;

    /** The ImageView that renders the modified icon. */
    private ImageView myIconView;

    /** The spinner width. */
    private final double spinwidth = 59.0;

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
     * @param record the selected icon.
     */
    public IconCustomizerPane(Window owner, PanelModel thePanelModel)
    {
        myIconRecord = thePanelModel.getIconRecord();
        setTop(createTop());

        setCenter(myIconDisplay = createImageView());
        setRight(createRight());

        VBox bottom = createBottom();
        BorderPane.setMargin(bottom, new Insets(5., 0., 0., 0.));
        setBottom(bottom);
    }

    /**
     * Creates the top box, which contains the color and scale controls.
     *
     * @return an HBox containing icon scale & color controls.
     */
    private HBox createTop()
    {
        HBox myTopBar = new HBox();
        Spinner<Number> sizeSpin = new Spinner<>(0.0, 3.0, 1, .1);
        sizeSpin.setPrefWidth(55.);
        sizeSpin.getValueFactory().valueProperty().bindBidirectional(myScale);
        sizeSpin.setEditable(true);
        Label sizeLabel = new Label("Scale: ", sizeSpin);
        sizeLabel.setContentDisplay(ContentDisplay.RIGHT);
        AnchorPane.setRightAnchor(sizeLabel, 0.);

        myColorPicker = new ColorPicker();

        myColorPicker.setOnAction((event) ->
        {
            myColorPicker.show();
            myColor = myColorPicker.getValue();
            updateImageColor();
        });
        myTopBar.setSpacing(5.);
        myTopBar.getChildren().addAll(myColorPicker, sizeLabel, sizeSpin);
        return myTopBar;
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

        Slider rotSlider = new Slider(-180, 180, 0);
        rotSlider.setOrientation(Orientation.VERTICAL);
        rotSlider.setShowTickMarks(true);
        rotSlider.setShowTickLabels(true);
        rotSlider.setMajorTickUnit(45);
        rotSlider.valueProperty().bindBidirectional(myRotation);

        Spinner<Number> rotSpinner = new Spinner<>(-180., 180., 0.);
        rotSpinner.setPrefWidth(40.0);
        rotSpinner.getValueFactory().valueProperty().bindBidirectional(myRotation);
        rotSpinner.setEditable(true);
        rotSpinner.getStyleClass().clear();

        Label rotLabel = new Label("Rotation: ", rotSpinner);
        rotLabel.setContentDisplay(ContentDisplay.BOTTOM);

        VBox.setVgrow(rotSlider, Priority.ALWAYS);
        VBox.setVgrow(rotSpinner, Priority.NEVER);
        box.getChildren().addAll(rotSlider, rotLabel, rotSpinner);

        return box;
    }

    /**
     * Creates the bottom box, which contains the icon size & position controls.
     *
     * @return VBox containing the size & position controls.
     */
    private VBox createBottom()
    {
        VBox cnrlBox = new VBox();
        cnrlBox.setAlignment(Pos.TOP_LEFT);

        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.BASELINE_LEFT);

        Spinner<Number> xSpin = new Spinner<>(-100., 100., 0., 5.);
        xSpin.setPrefWidth(spinwidth);
        xSpin.getValueFactory().valueProperty().bindBidirectional(myXPos);
        xSpin.setEditable(true);

        Spinner<Number> ySpin = new Spinner<>(-125., 125., 0., 5.);
        ySpin.setPrefWidth(spinwidth);
        ySpin.getValueFactory().valueProperty().bindBidirectional(myYPos);
        ySpin.setEditable(true);

        Label xLabel = new Label("Position:  X: ", xSpin);
        xLabel.setContentDisplay(ContentDisplay.RIGHT);

        Label yLabel = new Label("Y: ", ySpin);
        yLabel.setContentDisplay(ContentDisplay.RIGHT);

        controlBox.getChildren().addAll(xLabel, xSpin, yLabel, ySpin);

        HBox SaveInfo = new HBox();

        Label helpInfo = new Label("Replace Existing Icon?");
        helpInfo.setFont(Font.font(helpInfo.getFont().getFamily(), FontPosture.ITALIC, 11));

        CheckBox saveState = new CheckBox();
        saveState.selectedProperty().set(false);
        saveState.selectedProperty().bindBidirectional(mySave);

        SaveInfo.getChildren().addAll(saveState, helpInfo);
        SaveInfo.setSpacing(5.);
        cnrlBox.getChildren().addAll(controlBox, SaveInfo);
        return cnrlBox;
    }

    /**
     * Creates the ImageView which renders the selected icon.
     *
     * @return the iconDisplayer - the Hbox containing the icon.
     */
    private HBox createImageView()
    {
        HBox iconDisplayer = new HBox();
        iconDisplayer.setAlignment(Pos.CENTER);
         iconDisplayer.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
         + "-fx-border-insets: 5;" + "-fx-border-radius: 5;" +
         "-fx-border-color: purple;");
        iconDisplayer.setId("BoxStyle");
        myIconView = new ImageView(myIconRecord.getImageURL().toString());
        myIconView.rotateProperty().bind(myRotation);
        myIconView.translateXProperty().bind(myXPos);
        myIconView.translateYProperty().bind(myYPos);

        BufferedImage iconActual = null;
        try
        {
            iconActual = ImageIO.read(myIconRecord.getImageURL());
        }
        catch (IOException e)
        {
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
     * @return the the image to be saved.
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
           /* if (myXPos.getValue() == 0 && myYPos.getValue() == 0)
            {
                iconOut = myIconView.snapshot(parameters, null);
            }
            else
            {
                iconOut = myIconDisplay.snapshot(parameters, null);
            }*/
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
                    + +myColor.getGreen() + "-" + myColor.getBlue() : null;
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

    public IconRecord getIconRecord()
    {
        return myIconRecord;
    }

    /**
     * Sends the X translation value to be used elsewhere.
     *
     * @return the current Icon's X Position.
     */

    public int getXPos()
    {
        return myXPos.getValue().intValue();
    }

    /**
     * Sends the Y translation value to be used elsewhere.
     *
     * @return the current Icon's Y Position.
     */
    public int getYPos()
    {
        return myYPos.getValue().intValue();
    }
}