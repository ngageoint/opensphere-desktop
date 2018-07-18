package io.opensphere.mantle.iconproject.impl;

import java.awt.Window;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
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
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import io.opensphere.mantle.icon.IconRecord;

/** Panel for building custom icons. */
public class IconBuilderProjPane extends BorderPane
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

//    private DoubleProperty myBright = new SimpleDoubleProperty(0.);
//    private double oldcolor;
//    private double newcolor;
//    private double transcolor;

    /**
     * Constructs a new IconBuilderPane.
     *
     * @param owner the AWT Window.
     * @param record the selected icon.
     */
    public IconBuilderProjPane(Window owner, IconRecord record)
    {
        myIconRecord = record;
        setTop(createTop());

        setCenter(myIconDisplay = createImageView());
        setRight(createRight());

        VBox bottom = createBottom();
        BorderPane.setMargin(bottom, new Insets(5., 0., 0., 0.));
        setBottom(bottom);
        
        myColorPicker.setOnAction((event) ->
        {
            myColorPicker.show();
            myColor = myColorPicker.getValue();
            updateImageColor();
        });
    }

    /**
     * Creates the top box, which contains the color and scale controls.
     *
     * @return the icon selection & color controls
     */
    private HBox createTop()
    {
        HBox TopBar = new HBox();
        Spinner<Number> sizeSpin = new Spinner<>(0.0, 3.0, 1, .1);
        sizeSpin.setPrefWidth(55.);
        sizeSpin.getValueFactory().valueProperty().bindBidirectional(myScale);
        sizeSpin.setEditable(true);
        Label sizeLabel = new Label("Scale: ", sizeSpin);
        sizeLabel.setContentDisplay(ContentDisplay.RIGHT);
        AnchorPane.setRightAnchor(sizeLabel, 0.);

        myColorPicker = new ColorPicker();
        
        // myColorPicker.getStyleClass().add("button");
        myColorPicker.setOnMouseEntered(event ->
        {
            myColorPicker.show();
        });
        /* myColorPicker.setOnMouseExited(event -> {
         * System.out.println("^^^^^^^^Accesible role prop time2:   " +

//        Slider brightSlider = new Slider(-1., 1., 0.);
//        brightSlider.setBlockIncrement(.1);
//        brightSlider.valueProperty().bindBidirectional(myBright);   
//        brightSlider.onMousePressedProperty().set(event -> { oldcolor =
//        brightSlider.getValue(); });
//        brightSlider.onMouseReleasedProperty().set(event -> { newcolor =
//        brightSlider.getValue(); updateImageBrightness(); }); */
//        brightSlider.setOnMouseClicked(event_ -> updateImageBrightness());
//        brightSlider.setMaxWidth(80.);
//        Label brightLabel = new Label("Brightness: ", brightSlider);
//        brightLabel.setContentDisplay(ContentDisplay.RIGHT);
//        AnchorPane.setLeftAnchor(brightLabel, 175.);
        
        TopBar.getChildren().addAll(myColorPicker, sizeLabel, sizeSpin);
        return TopBar;
    }

    /**
     * Creates the right box, which contains the rotation controls.
     *
     * @return the rotation controls
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
        rotSpinner.setPrefWidth(40.);
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
     * @return the size & position controls
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
        saveState.selectedProperty().addListener(event -> System.out.println(saveState.selectedProperty().get()));
        
        SaveInfo.getChildren().addAll(saveState,helpInfo);
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
                + "-fx-border-insets: 5;" + "-fx-border-radius: 5;" + "-fx-border-color: purple;");

        myIconView = new ImageView(myIconRecord.getImageURL().toString());
        myIconView.rotateProperty().bind(myRotation);
        myIconView.translateXProperty().bind(myXPos);
        myIconView.translateYProperty().bind(myYPos);
        myIconView.setFitWidth(150);
        myIconView.setFitHeight(150);
        myIconView.scaleXProperty().bind(myScale);
        myIconView.scaleYProperty().bind(myScale);

        iconDisplayer.getChildren().addAll(myIconView);

        return iconDisplayer;
        /* ColorAdjust monochrome = new ColorAdjust();
         * monochrome.setSaturation(-1.0); ColorAdjust color2 = new
         * ColorAdjust(); color2.setBrightness(5.0); Blend blush = new
         * Blend(BlendMode.MULTIPLY, monochrome, color2);
         *
         * myImageRenderView.effectProperty()
         * .bind(Bindings.when(myImageRenderView.hoverProperty()).then((Effect)
         * blush).otherwise((Effect)null)); */

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
     * Updates the brightness of the selected icon when the brightness slider
     * selection changes.
     */
//    private void updateImageBrightness()
//    {
//        if (newcolor > oldcolor)
//        {
//            myColor = myColor.brighter();
//        }
//        else
//        {
//            myColor = myColor.darker();
//        }
//        if (newcolor == oldcolor)
//            if (newcolor > transcolor && transcolor != 1. || transcolor != -1.)
//            {
//                myColor = myColor.brighter();
//            }
//            else
//            {
//                myColor = myColor.darker();
//
//            }
//        transcolor = newcolor;
//        updateImageColor();
//    }

    /**
     * Retrieves the final processed image as a BufferedImage.
     *
     * @return the the image to be saved.
     */
    public WritableImage getFinalImage()
    {
        WritableImage iconOut = null;

        if (myIconView.getImage() != null)
        {
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            myIconDisplay.setStyle(null);
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
                    + +myColor.getGreen() + "-" + myColor.getBlue() : null;
        }

        return myIconRecord != null ? myIconRecord.getName() + "_" + myIconView.getRotate() : null;
    }
    
    public boolean getSaveState()
    {
        return mySave.get();
    }

    public IconRecord getIconRecord()
    {
        return myIconRecord;
    }
    
}