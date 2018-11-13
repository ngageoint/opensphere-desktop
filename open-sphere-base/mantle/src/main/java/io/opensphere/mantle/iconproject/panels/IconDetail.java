package io.opensphere.mantle.iconproject.panels;

import org.apache.log4j.Logger;

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.panels.transform.TransformModel;
import io.opensphere.mantle.iconproject.panels.transform.TransformPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
    private final Label myNameLabel;

    /** The label in which the source collection of the icon is displayed. */
    private final Label mySourceLabel;

    /** The label in which the tags applied to the icon are displayed. */
    private final Label myTagsLabel;

    /** The panel on which transformations occur. */
    private final TransformPanel myTransformPanel;

    /**
     * Creates a new detail panel bound to the supplied model.
     *
     * @param model the model to which to bind the panel.
     */
    public IconDetail(PanelModel model)
    {
        setMaxWidth(250);
        setMinWidth(250);

        Canvas canvas = new Canvas(246, 250);
        canvas.getGraphicsContext2D().drawImage(null, USE_COMPUTED_SIZE, BASELINE_OFFSET_SAME_AS_HEIGHT);

        model.previewRecordProperty().addListener((obs, ov, nv) ->
        {
            redrawPreview(canvas, nv);
        });

        myNameLabel = new Label();
        mySourceLabel = new Label();
        myTagsLabel = new Label();

        model.previewRecordProperty().addListener((obs, ov, nv) ->
        {
            if (nv != null)
            {
                myNameLabel.setText(nv.getName());
                mySourceLabel.setText(nv.collectionNameProperty().get());
            }
        });

        VBox box = new VBox(5);
        box.setAlignment(Pos.TOP_CENTER);

        HBox canvasBox = new HBox(canvas);
        Border border = new Border(new BorderStroke(new Color(0.247058824, 0.247058824, 0.305882353, 1), BorderStrokeStyle.SOLID,
                null, BorderWidths.DEFAULT, new Insets(0, 0, 10, 2)));
        canvasBox.setBorder(border);
        getChildren().add(canvasBox);
        setTopAnchor(canvasBox, 0.0);
        setLeftAnchor(canvasBox, 0.0);
        setRightAnchor(canvasBox, 0.0);
        setBottomAnchor(canvasBox, 255.0);

        GridPane grid = new GridPane();
        grid.add(new Label("Name:"), 0, 0);
        grid.add(myNameLabel, 1, 0);
        grid.add(new Label("Source:"), 0, 1);
        grid.add(mySourceLabel, 1, 1);
        grid.add(new Label("Tags:"), 0, 2);
        grid.add(myTagsLabel, 1, 2);

        box.getChildren().add(grid);

        myTransformPanel = new TransformPanel();

        TransformModel transformModel = myTransformPanel.getModel();
        transformModel.horizontalMoveProperty()
                .addListener((obs, ov, nv) -> redrawPreview(canvas, model.previewRecordProperty().get()));
        transformModel.verticalMoveProperty()
                .addListener((obs, ov, nv) -> redrawPreview(canvas, model.previewRecordProperty().get()));
        transformModel.rotationProperty()
                .addListener((obs, ov, nv) -> redrawPreview(canvas, model.previewRecordProperty().get()));
        transformModel.horizontalScaleProperty()
                .addListener((obs, ov, nv) -> redrawPreview(canvas, model.previewRecordProperty().get()));
        transformModel.verticalScaleProperty()
                .addListener((obs, ov, nv) -> redrawPreview(canvas, model.previewRecordProperty().get()));

        box.getChildren().add(myTransformPanel);

        getChildren().add(box);

        setTopAnchor(box, 275.0);
        setLeftAnchor(box, 2.0);
        setRightAnchor(box, 0.0);
    }

    /**
     * Draws the preview of the supplied icon on the supplied canvas, applying
     * any transforms specified by the user.
     *
     * @param canvas the canvas on which to draw the preview.
     * @param icon the icon from which to extract the image for the preview.
     */
    private void redrawPreview(Canvas canvas, IconRecord icon)
    {
        TransformModel model = myTransformPanel.getModel();
        if (icon == null)
        {
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
        else
        {
            Image image = icon.imageProperty().get();
            double xOrigin = canvas.getWidth() / 2;
            double yOrigin = canvas.getHeight() / 2;

            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            double drawX = 0 - (image.getWidth() * model.horizontalScaleProperty().get()) / 2;
            double drawY = 0 - (image.getHeight() * model.verticalScaleProperty().get()) / 2;

            GraphicsContext graphicsContext2D = canvas.getGraphicsContext2D();

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
