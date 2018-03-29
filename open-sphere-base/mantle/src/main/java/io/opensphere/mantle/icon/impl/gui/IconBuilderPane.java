package io.opensphere.mantle.icon.impl.gui;

import java.awt.image.BufferedImage;

import io.opensphere.core.image.processor.RotateImageProcessor;
import io.opensphere.core.util.AwesomeIcon;
import io.opensphere.core.util.FontIconEnum;
import io.opensphere.core.util.GovIcon;
import io.opensphere.core.util.MilitaryRankIcon;
import io.opensphere.core.util.swing.GenericFontIcon;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.util.Callback;

/** Panel for building custom icons. */
@SuppressWarnings("restriction")
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

    /** The ComboBox containing all icon choices. */
    private ComboBox<FontIconEnum> myComboBox;

    /** The ColorPicker that determines the icon color. */
    private ColorPicker myColorPicker;

    /** The ImageView that renders the modified icon. */
    private ImageView myImageRenderView;

    /** Constructs a new IconBuilderPane. */
    public IconBuilderPane()
    {
        setTop(createTop());

        setCenter(createImageView());
        BorderPane.setAlignment(myImageRenderView, Pos.CENTER);
        myImageRenderView.autosize();

        myComboBox.setOnAction((event) -> updateImageView());
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
    private HBox createTop()
    {
        HBox box = new HBox();

        myComboBox = new ComboBox<>();
        myComboBox.getItems().addAll(AwesomeIcon.values());
        myComboBox.getItems().addAll(GovIcon.values());
        myComboBox.getItems().addAll(MilitaryRankIcon.values());

        myComboBox.setCellFactory(new Callback<ListView<FontIconEnum>, ListCell<FontIconEnum>>()
        {
            @Override
            public ListCell<FontIconEnum> call(ListView<FontIconEnum> param)
            {
                return new ListCell<FontIconEnum>()
                {
                    ImageView view;
                    {
                        setContentDisplay(ContentDisplay.LEFT);
                        view = new ImageView();
                    }

                    @Override
                    protected void updateItem(FontIconEnum item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        setGraphic(view);

                        if (item != null && !empty)
                        {
                            GenericFontIcon icon = new GenericFontIcon(item);

                            setText(item.toString());
                            view.setImage(SwingFXUtils.toFXImage(icon.getImage(), null));
                        }
                    }
                };
            }

        });

        myColorPicker = new ColorPicker();

        box.getChildren().addAll(myComboBox, myColorPicker);

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
        spinner.setPrefWidth(80);
        spinner.getValueFactory().valueProperty().bindBidirectional(myRotation);

        VBox.setVgrow(slider, Priority.ALWAYS);
        VBox.setVgrow(spinner, Priority.NEVER);
        box.getChildren().addAll(slider, spinner);

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
        box.setAlignment(Pos.TOP_CENTER);

        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.BASELINE_CENTER);

        Spinner<Number> sizeSpinner = new Spinner<>(12, 200, 200);
        sizeSpinner.setPrefWidth(70);
        sizeSpinner.getValueFactory().valueProperty().bindBidirectional(mySize);

        Spinner<Number> xSpinner = new Spinner<>(-100, 100, 0);
        xSpinner.setPrefWidth(70);
        xSpinner.getValueFactory().valueProperty().bindBidirectional(myXPos);

        Spinner<Number> ySpinner = new Spinner<>(0, 200, 0);
        ySpinner.setPrefWidth(70);
        ySpinner.getValueFactory().valueProperty().bindBidirectional(myYPos);

        Label sizeLabel = new Label("Size: ", sizeSpinner);
        sizeLabel.setContentDisplay(ContentDisplay.RIGHT);

        Label xLabel = new Label("X: ", xSpinner);
        xLabel.setContentDisplay(ContentDisplay.RIGHT);

        Label yLabel = new Label("Y: ", ySpinner);
        yLabel.setContentDisplay(ContentDisplay.RIGHT);

        controlBox.getChildren().addAll(sizeLabel, sizeSpinner, xLabel, xSpinner, yLabel, ySpinner);

        Label helpInfo = new Label("Saved icons will appear under the 'User Added' menu.");
        helpInfo.setFont(Font.font(helpInfo.getFont().getFamily(), FontPosture.ITALIC, 10));

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
        border.setStroke(Color.WHITE);

        border.setManaged(false);
        myImageRenderView.boundsInParentProperty().addListener((v, o, n) ->
        {
            Bounds ivBounds = myImageRenderView.getBoundsInParent();
            border.setLayoutX(ivBounds.getMinX());
            border.setLayoutY(ivBounds.getMinY());
            border.setWidth(ivBounds.getWidth());
            border.setHeight(ivBounds.getHeight());
        });

        box.getChildren().addAll(myImageRenderView, border);

        return box;
    }

    /** Updates the ImageView when the selected icon changes. */
    private void updateImageView()
    {
        FontIconEnum item = myComboBox.getSelectionModel().getSelectedItem();
        if (item != null)
        {
            Color fxColor = myColorPicker.getValue();
            int r = (int)(fxColor.getRed() * 255);
            int g = (int)(fxColor.getGreen() * 255);
            int b = (int)(fxColor.getBlue() * 255);

            myCurrentIcon = new GenericFontIcon(item, new java.awt.Color(r, g, b), mySize.get());
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
            Color fxColor = myColorPicker.getValue();
            int r = (int)(fxColor.getRed() * 255);
            int g = (int)(fxColor.getGreen() * 255);
            int b = (int)(fxColor.getBlue() * 255);

            myCurrentIcon.setColor(new java.awt.Color(r, g, b));
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
