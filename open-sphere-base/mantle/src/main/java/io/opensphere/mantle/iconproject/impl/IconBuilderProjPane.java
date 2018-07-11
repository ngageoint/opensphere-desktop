package io.opensphere.mantle.iconproject.impl;

import java.awt.Window;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
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

/** Panel for building custom icons. */
public class IconBuilderProjPane extends BorderPane
{
    /** The rotation value model. */
    private final DoubleProperty myRotation = new SimpleDoubleProperty(0.);

    /** The size value model. */
    private final DoubleProperty mySize = new SimpleDoubleProperty(1.);

    /** The X value model. */
    private final DoubleProperty myXPos = new SimpleDoubleProperty(0.);

    /** The Y value model. */
    private final DoubleProperty myYPos = new SimpleDoubleProperty(0.);

    /** The current icon. */
    private IconProjProps myCurrentIcon;

    /** The ColorPicker that determines the icon color. */
    private ColorPicker myColorPicker;

    /** The ImageView that renders the modified icon. */
    private ImageView myImageRenderView;

    /** The AWT Window that owns this pane. */
    private final Window myOwner;

    /** The spinner width. */
    private final double spinwidth = 59.0;

    private final IconRecord myRecord;

    private HBox myHbox;

    private Color theColor;

    /**
     * Constructs a new IconBuilderPane.
     *
     * @param owner the AWT Window
     * @return
     */
    public IconBuilderProjPane(Window owner, IconRecord record)
    {
        myOwner = owner;
        myRecord = record;
        setTop(createTop());

        setCenter(myHbox = createImageView());
        setRight(createRight());

        VBox bottom = createBottom();
        BorderPane.setMargin(bottom, new Insets(10., 0., 0., 0.));
        setBottom(bottom);




        myColorPicker.setOnAction((event) -> {
            theColor = myColorPicker.getValue();
            updateImageColor(theColor);
        });

    }

    /**
     * Creates the top box, which contains the icon selection & color controls.
     *
     * @return the icon selection & color controls
     */
    private AnchorPane createTop()
    {
        AnchorPane TopBar = new AnchorPane();
        Spinner<Number> sizeSpinner = new Spinner<>(0.0, 3.0, 1, .1);
        sizeSpinner.setPrefWidth(55.);
        sizeSpinner.getValueFactory().valueProperty().bindBidirectional(mySize);
        sizeSpinner.setEditable(true);
        Label sizeLabel = new Label("Scale: ", sizeSpinner);
        sizeLabel.setContentDisplay(ContentDisplay.RIGHT);
        AnchorPane.setRightAnchor(sizeLabel, 0.);

        myColorPicker = new ColorPicker();
        myColorPicker.setOnMouseEntered(event ->
        {
            myColorPicker.show();
            System.out.println("Accesible role prop:   " + myColorPicker.accessibleRoleProperty());
            System.out.println("Get children unmodifiable:  " + myColorPicker.getChildrenUnmodifiable());
        });
        myColorPicker.setOnMouseExited(event -> System.out.println("Accesible role prop time2:   " + myColorPicker.accessibleRoleProperty()));

        TopBar.getChildren().addAll(myColorPicker, sizeLabel, sizeSpinner);
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
        VBox box = new VBox();
        box.setAlignment(Pos.TOP_LEFT);

        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.BASELINE_LEFT);

        Spinner<Number> xSpinner = new Spinner<>(-100., 100., 0., 5.);
        xSpinner.setPrefWidth(spinwidth);
        xSpinner.getValueFactory().valueProperty().bindBidirectional(myXPos);
        xSpinner.setEditable(true);

        Spinner<Number> ySpinner = new Spinner<>(-125., 125., 0., 5.);
        ySpinner.setPrefWidth(spinwidth);
        ySpinner.getValueFactory().valueProperty().bindBidirectional(myYPos);
        ySpinner.setEditable(true);

        Label xLabel = new Label("Position:  X: ", xSpinner);
        xLabel.setContentDisplay(ContentDisplay.RIGHT);

        Label yLabel = new Label("Y: ", ySpinner);
        yLabel.setContentDisplay(ContentDisplay.RIGHT);

        controlBox.getChildren().addAll(xLabel, xSpinner, yLabel, ySpinner);

        Label helpInfo = new Label("Saved icons will appear under the 'User Added' menu.");
        helpInfo.setFont(Font.font(helpInfo.getFont().getFamily(), FontPosture.ITALIC, 11));

        box.getChildren().addAll(controlBox, helpInfo);
        return box;
    }

    /**
     * Creates the ImageView which renders the selected icon.
     *
     * @return the image view
     */
    private HBox createImageView()
    {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;" + "-fx-border-color: purple;");

        myImageRenderView = new ImageView(myRecord.getImageURL().toString());
        myImageRenderView.rotateProperty().bind(myRotation);
        myImageRenderView.translateXProperty().bind(myXPos);
        myImageRenderView.translateYProperty().bind(myYPos);
        myImageRenderView.setFitWidth(150);
        myImageRenderView.setFitHeight(150);
        myImageRenderView.scaleXProperty().bind(mySize);
        myImageRenderView.scaleYProperty().bind(mySize);

        /*ColorAdjust monochrome = new ColorAdjust();
        monochrome.setSaturation(-1.0);
        ColorAdjust color2 = new ColorAdjust();
        color2.setBrightness(5.0);
        Blend blush = new Blend(BlendMode.MULTIPLY, monochrome, color2);

        myImageRenderView.effectProperty()
                .bind(Bindings.when(myImageRenderView.hoverProperty()).then((Effect)blush).otherwise((Effect)null));
        */

        box.getChildren().addAll(myImageRenderView);

        return box;
    }

    /**
     * Updates the color of the selected icon when the ColorPicker selection
     * changes.
     */
    private void updateImageColor(Color color)
    {
        Lighting lighting = new Lighting();
        lighting.setDiffuseConstant(1.0);
        lighting.setSpecularConstant(1.0);
        lighting.setSpecularExponent(0.0);
        lighting.setSurfaceScale(0.0);
        lighting.setLight(new Light.Distant(45, 45, theColor));

        if (myImageRenderView != null)
        {
           myImageRenderView.setEffect(lighting);
        }
    }

    /**
     * Retrieves the final processed image as a BufferedImage.
     *
     * @return the image
     */
    public WritableImage getFinalImage()
    {
        WritableImage result = null;

        if (myImageRenderView.getImage() != null)
        {
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            myHbox.setStyle(null);
            result = myHbox.snapshot(parameters, null);
        }
        return result;
    }

    /**
     * Generates and retrieves a name for the image, if it is not null.
     *
     * @return the image name
     */
    public String getImageName()
    {
        return myRecord != null ? myRecord.getName() + "_" + myImageRenderView.getRotate() : null;
    }
}