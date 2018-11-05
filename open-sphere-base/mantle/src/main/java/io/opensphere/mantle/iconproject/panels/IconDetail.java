package io.opensphere.mantle.iconproject.panels;

import org.apache.log4j.Logger;

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.panels.transform.TransformModel;
import io.opensphere.mantle.iconproject.panels.transform.TransformPanel;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * A panel in which icon details are displayed.
 */
public class IconDetail extends AnchorPane
{
    /** The logger used to capture output from instances of this class. */
    private static final Logger LOG = Logger.getLogger(IconDetail.class);

    /** A zoomed in view of the selected icon. */
    private final ImageView myImagePreview;

    /** The label in which the name of the icon is displayed. */
    private final Label myNameLabel;

    /** The label in which the source collection of the icon is displayed. */
    private final Label mySourceLabel;

    /** The label in which the tags applied to the icon are displayed. */
    private final Label myTagsLabel;

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

        Canvas canvas = new Canvas(250, 250);
        canvas.getGraphicsContext2D().drawImage(null, USE_COMPUTED_SIZE, BASELINE_OFFSET_SAME_AS_HEIGHT);

        myImagePreview = new ImageView();
//        myImagePreview.fitHeightProperty().bind(this.widthProperty());
//        myImagePreview.fitWidthProperty().bind(this.widthProperty());
        myImagePreview.setPreserveRatio(true);

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
                mySourceLabel.setText(nv.getCollectionName());
            }
        });

        VBox box = new VBox();
        box.setAlignment(Pos.TOP_CENTER);

//        Node hbox = Borders.wrap(myImagePreview).lineBorder().color(Color.GREY).innerPadding(1).outerPadding(0).buildAll();
//        Node hbox = Borders.wrap(canvas).lineBorder().color(Color.GREY).innerPadding(1).outerPadding(0).buildAll();

        getChildren().add(canvas);
        setTopAnchor(canvas, 0.0);
        setLeftAnchor(canvas, 0.0);
        setRightAnchor(canvas, 0.0);
        setBottomAnchor(canvas, 275.0);

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

        setTopAnchor(box, 250.0);
        setLeftAnchor(box, 0.0);
        setRightAnchor(box, 0.0);
    }

    private void redrawPreview(Canvas canvas, IconRecord icon)
    {
        TransformModel model = myTransformPanel.getModel();
        if (icon == null)
        {
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
        else
        {
            Image image = icon.getImage();
            double xOrigin = canvas.getWidth() / 2;
            double xOffset = 0;
            if (model.horizontalMoveProperty().get() != 0)
            {
                xOffset = xOrigin * (model.horizontalMoveProperty().get() / 100);
            }
            double yOrigin = canvas.getHeight() / 2;
            double yOffset = 0;
            if (model.verticalMoveProperty().get() != 0)
            {
                yOffset = yOrigin * (model.verticalMoveProperty().get() / 100);
            }

            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            double drawX = 0 - image.getWidth() / 2;
            double drawY = 0 - image.getHeight() / 2;

            LOG.info("     X Origin: " + xOrigin);
            LOG.info("     Y Origin: " + yOrigin);
            LOG.info("       H Move: " + model.horizontalMoveProperty().get());
            LOG.info("       V Move: " + model.verticalMoveProperty().get());
            LOG.info("     X Offset: " + xOffset);
            LOG.info("     Y Offset: " + yOffset);
            LOG.info(" Canvas Width: " + canvas.getWidth());
            LOG.info("Canvas Height: " + canvas.getHeight());
            LOG.info("   Icon Width: " + image.getWidth());
            LOG.info("  Icon Height: " + image.getHeight());
            LOG.info("       X Draw: " + xOffset);
            LOG.info("       Y Draw: " + yOffset);

            GraphicsContext graphicsContext2D = canvas.getGraphicsContext2D();

            // translate to the middle:

            graphicsContext2D.translate(xOrigin, yOrigin);
            graphicsContext2D.translate(xOffset, yOffset);
            graphicsContext2D.rotate(model.rotationProperty().get());
            graphicsContext2D.translate(drawX, drawY);
//            graphicsContext2D.scale(model.horizontalScaleProperty().get(), model.verticalScaleProperty().get());
            graphicsContext2D.drawImage(image, 0, 0);
            canvas.getGraphicsContext2D().strokeRect(0, 0, image.getWidth(), image.getHeight());
//            graphicsContext2D.scale(-model.horizontalScaleProperty().get(), -model.verticalScaleProperty().get());
            graphicsContext2D.translate(-drawX, -drawY);
            graphicsContext2D.rotate(-model.rotationProperty().get());
            graphicsContext2D.translate(-xOffset, -yOffset);
            graphicsContext2D.translate(-xOrigin, -yOrigin);
            canvas.getGraphicsContext2D().strokeRect(1, 1, canvas.getWidth() - 1, canvas.getHeight() - 1);
        }
    }

}
