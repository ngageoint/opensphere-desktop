package io.opensphere.mantle.icon.impl.gui;

import static io.opensphere.core.util.fx.FXUtilities.toAwtColor;

import java.awt.Window;
import java.awt.image.BufferedImage;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import javax.swing.JOptionPane;

import io.opensphere.core.image.processor.RotateImageProcessor;
import io.opensphere.core.util.FontIconEnum;
import io.opensphere.core.util.swing.GenericFontIcon;

/** Panel for building custom icons. */
public class IconBuilderPane extends BorderPane
{
    /** The rotation value model. */
    private final IntegerProperty myRotation = new SimpleIntegerProperty();

    /** The size value model. */
    private final IntegerProperty mySize = new SimpleIntegerProperty(200);

    /** The X value model. */
    private final IntegerProperty myXPos = new SimpleIntegerProperty(0);

    /** The Y value model. */
    private final IntegerProperty myYPos = new SimpleIntegerProperty(0);

    /** The current icon. */
    private GenericFontIcon myCurrentIcon;

    /** The ColorPicker that determines the icon color. */
    private ColorPicker myColorPicker;

    /** The ImageView that renders the modified icon. */
    private ImageView myImageRenderView;

    /** The AWT Window that owns this pane. */
    private final Window myOwner;

    /** The spinner width. */
    private final double spinwidth = 40.0;

    /**
     * Constructs a new IconBuilderPane.
     *
     * @param owner the AWT Window
     */
    public IconBuilderPane(Window owner)
    {
        myOwner = owner;

        setTop(createTop());

        setCenter(createImageView());
        BorderPane.setAlignment(myImageRenderView, Pos.CENTER);
        myImageRenderView.autosize();

        myColorPicker.setOnAction((event) -> updateImageColor());

        setRight(createRight());

        VBox bottom = createBottom();
        BorderPane.setMargin(bottom, new Insets(10., 0., 0., 0.));
        setBottom(bottom);

        mySize.addListener((v, o, n) -> updateImageSize());
        myXPos.addListener((v, o, n) -> updateImagePosition());
        myYPos.addListener((v, o, n) -> updateImagePosition());
    }

    /**
     * Creates the top box, which contains the icon selection & color controls.
     *
     * @return the icon selection & color controls
     */
    private AnchorPane createTop()
    {
        AnchorPane box = new AnchorPane();

        Spinner<Number> sizeSpinner = new Spinner<>(12, 200, 200, 10);
        sizeSpinner.setPrefWidth(spinwidth);
        sizeSpinner.getValueFactory().valueProperty().bindBidirectional(mySize);
        sizeSpinner.setEditable(true);

        Label sizeLabel = new Label("Size: ", sizeSpinner);
        sizeLabel.setContentDisplay(ContentDisplay.RIGHT);
        AnchorPane.setRightAnchor(sizeLabel, 50.);

        Button button = new Button("Select an Icon");
        button.setOnAction((evt) ->
        {
            IconBuilderChoiceDialog iconChoice = new IconBuilderChoiceDialog();
            int result = JOptionPane.showConfirmDialog(myOwner, iconChoice, "Select an Icon",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION)
            {
                updateImageView(iconChoice.getValue());
            }
        });
        
        AnchorPane.setLeftAnchor(sizeSpinner, 10.);
        myColorPicker = new ColorPicker();

        box.getChildren().addAll(myColorPicker, button, sizeLabel, sizeSpinner);

        return box;
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

        Slider slider = new Slider(-180, 180, 0);
        slider.setOrientation(Orientation.VERTICAL);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(45);
        slider.valueProperty().bindBidirectional(myRotation);

        Spinner<Number> spinner = new Spinner<>(-180, 180, 0);
        spinner.setPrefWidth(spinwidth);
        spinner.getValueFactory().valueProperty().bindBidirectional(myRotation);
        spinner.setEditable(true);

        Label rotLabel = new Label("Rotation: ", spinner);
        rotLabel.setContentDisplay(ContentDisplay.BOTTOM);

        VBox.setVgrow(slider, Priority.ALWAYS);
        VBox.setVgrow(spinner, Priority.NEVER);
        box.getChildren().addAll(slider, rotLabel, spinner);

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

        Spinner<Number> xSpinner = new Spinner<>(-100, 100, 0, 10);
        xSpinner.setPrefWidth(spinwidth);
        xSpinner.getValueFactory().valueProperty().bindBidirectional(myXPos);
        xSpinner.setEditable(true);

        Spinner<Number> ySpinner = new Spinner<>(0, 200, 0, 10);
        ySpinner.setPrefWidth(spinwidth);
        ySpinner.getValueFactory().valueProperty().bindBidirectional(myYPos);
        ySpinner.setEditable(true);
        ySpinner.getStyleClass().clear();

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
        myImageRenderView = new ImageView();
        myImageRenderView.rotateProperty().bind(myRotation);

        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);

        Rectangle border = new Rectangle(0, 0, Color.TRANSPARENT);
      //  border.setStroke(Color.WHITE);
        border.setManaged(false);

        myImageRenderView.boundsInParentProperty().addListener((v, o, n) ->
        {
            border.setLayoutX(myImageRenderView.getBoundsInParent().getMinX());
            border.setLayoutY(myImageRenderView.getBoundsInParent().getMinY());
            border.setWidth(myImageRenderView.getBoundsInParent().getWidth());
            border.setHeight(myImageRenderView.getBoundsInParent().getHeight());
        });

        box.getChildren().addAll(myImageRenderView, border);

        return box;
    }

    /**
     * Updates the ImageView when the selected icon changes.
     *
     * @param item the selected icon
     */
    private void updateImageView(FontIconEnum item)
    {
        if (item != null)
        {
            myCurrentIcon = new GenericFontIcon(item, toAwtColor(myColorPicker.getValue()), mySize.get());
            myYPos.set(myCurrentIcon.getYPos());
            myXPos.set(myCurrentIcon.getXPos());

            myImageRenderView.setImage(SwingFXUtils.toFXImage(myCurrentIcon.getImage(), null));
        }
    }

    /**
     * Updates the color of the selected icon when the ColorPicker selection
     * changes.
     */
    private void updateImageColor()
    {
        if (myCurrentIcon != null)
        {
            myCurrentIcon.setColor(toAwtColor(myColorPicker.getValue()));
            myImageRenderView.setImage(SwingFXUtils.toFXImage(myCurrentIcon.getImage(), null));
        }
    }

    /**
     * Updates the size of the selected icon when the Size slider value changes.
     */
    private void updateImageSize()
    {
        if (myCurrentIcon != null)
        {
            myCurrentIcon.setSize(mySize.get());
            myXPos.set(myCurrentIcon.getXPos());
            myYPos.set(myCurrentIcon.getYPos());

            myImageRenderView.setImage(SwingFXUtils.toFXImage(myCurrentIcon.getImage(), null));
        }
    }

    /**
     * Updates the position of the selected icon when the X or Y slider values
     * change.
     */
    private void updateImagePosition()
    {
        if (myCurrentIcon != null)
        {
            myCurrentIcon.setXPos(myXPos.get());
            myCurrentIcon.setYPos(myYPos.get());

            myImageRenderView.setImage(SwingFXUtils.toFXImage(myCurrentIcon.getImage(), null));
        }
    }

    /**
     * Retrieves the final processed image as a BufferedImage.
     *
     * @return the image
     */
    public BufferedImage getFinalImage()
    {
        BufferedImage result = null;

        if (myImageRenderView.getImage() != null)
        {
            RotateImageProcessor processor = new RotateImageProcessor(myImageRenderView.getRotate(), false, null);
            result = SwingFXUtils.fromFXImage(myImageRenderView.getImage(), null);
            result = processor.process(result);
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
        return myCurrentIcon != null
                ? myCurrentIcon.getIcon() + "_" + myCurrentIcon.getColor() + "_" + myImageRenderView.getRotate() : null;
    }
}
