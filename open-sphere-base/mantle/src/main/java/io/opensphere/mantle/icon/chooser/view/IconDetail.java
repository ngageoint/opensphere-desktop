package io.opensphere.mantle.icon.chooser.view;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import io.opensphere.core.function.Procedure;
import io.opensphere.core.util.AwesomeIconRegular;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.fx.FxIcons;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.chooser.model.CustomizationModel;
import io.opensphere.mantle.icon.chooser.model.IconModel;
import io.opensphere.mantle.icon.chooser.model.TransformModel;
import io.opensphere.mantle.icon.chooser.view.transform.TransformPanel;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

/** A panel in which icon details are displayed. */
public class IconDetail extends AnchorPane
{
    /** The logger used to capture output from instances of this class. */
    private static final Logger LOG = Logger.getLogger(IconDetail.class);

    /** The label in which the name of the icon is displayed. */
    private final TextField myNameField;

    /** The label in which the source collection of the icon is displayed. */
    private final TextField mySourceField;

    /** The label in which the tags applied to the icon are displayed. */
    private final TextField myTagsField;

    /** The panel on which transformations occur. */
    private final TransformPanel myTransformPanel;

    /** The canvas on which the icon preview is drawn. */
    private final Canvas myCanvas;

    /** The icon chooser model backing the panel. */
    private IconModel myModel;

    /** the model backing customization operations. */
    private CustomizationModel myCustomizationModel;

    /**
     * The procedure called when part of the selected icon record's model
     * changes and the parent item needs to be refreshed.
     */
    private Procedure myRefreshProcedure;

    /** The button to toggle to set favorite state. */
    private Label myFavoriteToggleButton;

    /** The node to use when the icon is not marked as a favorite. */
    private Node myUnFavoriteIcon;

    /** The node to use when the icon is marked as a favorite. */
    private Node myFavoriteIcon;

    /**
     * Creates a new detail panel bound to the supplied model.
     *
     * @param model the model to which to bind the panel.
     * @param refreshProcedure The procedure called when part of the selected
     *            icon record's model changes and the parent item needs to be
     *            refreshed.
     */
    public IconDetail(IconModel model, Procedure refreshProcedure)
    {
        myModel = model;
        myRefreshProcedure = refreshProcedure;
        myCustomizationModel = model.getCustomizationModel();
        setMaxWidth(250);
        setMinWidth(250);

        myCanvas = new Canvas(246, 250);
        myCanvas.getGraphicsContext2D().drawImage(null, USE_COMPUTED_SIZE, BASELINE_OFFSET_SAME_AS_HEIGHT);

        myNameField = new TextField();
        myNameField.textProperty().bindBidirectional(myCustomizationModel.nameProperty());

        mySourceField = new TextField();
        mySourceField.textProperty().bindBidirectional(myCustomizationModel.sourceProperty());

        myTagsField = new TextField();
        myTagsField.textProperty().bindBidirectional(myCustomizationModel.tagsProperty());

        VBox box = new VBox(5);
        box.setAlignment(Pos.TOP_CENTER);

        StackPane stackPane = new StackPane();

        myUnFavoriteIcon = FxIcons.createClearIcon(AwesomeIconRegular.STAR, Color.GREY, 16);
        myFavoriteIcon = FxIcons.createClearIcon(AwesomeIconSolid.STAR, Color.GOLD, 16);

        myFavoriteToggleButton = new Label();
        myFavoriteToggleButton.setOnMouseClicked(e ->
        {
            if (myModel.selectedRecordProperty().get() != null && myModel.selectedRecordProperty().get().favoriteProperty().get())
            {
                myModel.selectedRecordProperty().get().favoriteProperty().set(false);
                myFavoriteToggleButton.setGraphic(myUnFavoriteIcon);
            }
            else
            {
                myModel.selectedRecordProperty().get().favoriteProperty().set(true);
                myFavoriteToggleButton.setGraphic(myFavoriteIcon);
            }
            myRefreshProcedure.invoke();
        });

        myFavoriteToggleButton.setPadding(new Insets(5));
        HBox buttonBox = new HBox(myFavoriteToggleButton);
        buttonBox.setAlignment(Pos.TOP_RIGHT);

        HBox canvasBox = new HBox(myCanvas);
        Border border = new Border(new BorderStroke(new Color(0.247058824, 0.247058824, 0.305882353, 1), BorderStrokeStyle.SOLID,
                null, BorderWidths.DEFAULT, new Insets(0, 0, 10, 2)));
        canvasBox.setBorder(border);
        stackPane.getChildren().addAll(canvasBox, buttonBox);

        getChildren().add(stackPane);
        setTopAnchor(stackPane, 0.0);
        setLeftAnchor(stackPane, 0.0);
        setRightAnchor(stackPane, 0.0);

        GridPane grid = new GridPane();
        grid.add(new Label("Name:"), 0, 0);
        grid.add(myNameField, 1, 0);
        grid.add(new Label("Source:"), 0, 1);
        grid.add(mySourceField, 1, 1);
        grid.add(new Label("Tags:"), 0, 2);
        grid.add(myTagsField, 1, 2);

        box.getChildren().add(grid);

        myTransformPanel = new TransformPanel(this::saveCanvas);

        TransformModel transformModel = myTransformPanel.getModel();
        transformModel.horizontalMoveProperty().addListener((obs, ov, nv) -> redrawPreview(model.selectedRecordProperty().get()));
        transformModel.verticalMoveProperty().addListener((obs, ov, nv) -> redrawPreview(model.selectedRecordProperty().get()));
        transformModel.rotationProperty().addListener((obs, ov, nv) -> redrawPreview(model.selectedRecordProperty().get()));
        transformModel.horizontalScaleProperty()
                .addListener((obs, ov, nv) -> redrawPreview(model.selectedRecordProperty().get()));
        transformModel.verticalScaleProperty().addListener((obs, ov, nv) -> redrawPreview(model.selectedRecordProperty().get()));

        HBox spacer = new HBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        box.getChildren().add(spacer);

        box.getChildren().add(myTransformPanel);

        getChildren().add(box);

        setTopAnchor(box, 275.0);
        setLeftAnchor(box, 2.0);
        setRightAnchor(box, 0.0);
        setBottomAnchor(box, 0.0);

        redrawPreview(model.selectedRecordProperty().get());
    }

    /**
     * Saves the current content of the canvas to a new icon record.
     */
    private void saveCanvas()
    {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        WritableImage image = myCanvas.snapshot(parameters, null);

        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(image, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(),
                Transparency.TRANSLUCENT);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);
        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufImageRGB, "png", outputStream);

            URL imageURL = myModel.getIconRegistry().getIconCache().cacheIcon(outputStream.toByteArray(),
                    myNameField.textProperty().get() + " User Edited Icon", true);
            IconProvider provider = new DefaultIconProvider(imageURL, IconRecord.USER_ADDED_COLLECTION, "User");
            myModel.getIconRegistry().addIcon(provider, this);
        }
        catch (IOException e)
        {
            LOG.error("Failed to write image.", e);
        }
    }

    /**
     * Draws the preview of the supplied icon on the supplied canvas, applying
     * any transforms specified by the user.
     *
     * @param icon the icon from which to extract the image for the preview.
     */
    public void redrawPreview(IconRecord icon)
    {
        if (icon != null && icon.favoriteProperty().get())
        {
            myFavoriteToggleButton.setGraphic(myFavoriteIcon);
        }
        else
        {
            myFavoriteToggleButton.setGraphic(myUnFavoriteIcon);
        }

        TransformModel model = myTransformPanel.getModel();
        if (icon == null)
        {
            myCanvas.getGraphicsContext2D().clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
        }
        else
        {
            Image image = icon.imageProperty().get();
            double xOrigin = myCanvas.getWidth() / 2;
            double yOrigin = myCanvas.getHeight() / 2;

            myCanvas.getGraphicsContext2D().clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
            double drawX = 0 - (image.getWidth() * model.horizontalScaleProperty().get()) / 2;
            double drawY = 0 - (image.getHeight() * model.verticalScaleProperty().get()) / 2;

            GraphicsContext graphicsContext2D = myCanvas.getGraphicsContext2D();

            double horizontalTranslate = xOrigin + drawX + model.horizontalMoveProperty().get();
            double mxx = model.horizontalScaleProperty().get();
            double mxy = 0;
            double tx = horizontalTranslate;

            double verticalTranslate = yOrigin + drawY + model.verticalMoveProperty().get();
            double myx = 0;
            double myy = model.verticalScaleProperty().get();
            double ty = verticalTranslate;

            Affine affine = new Affine(mxx, mxy, tx, myx, myy, ty);
            affine.appendRotation(model.rotationProperty().get(), image.getWidth() / 2, image.getHeight() / 2);

            if (LOG.isTraceEnabled())
            {
                LOG.trace("Affine: " + affine.toString());
            }

            try
            {
                graphicsContext2D.setTransform(affine);
                graphicsContext2D.drawImage(image, 0, 0);
                Affine inverseAffine = affine.createInverse();
                graphicsContext2D.transform(inverseAffine);
            }
            catch (NonInvertibleTransformException e)
            {
                LOG.error("Unable to invert scale transform", e);
            }
        }
    }
}
