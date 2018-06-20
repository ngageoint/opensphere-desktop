package io.opensphere.controlpanels.layers.availabledata.detail;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import io.opensphere.controlpanels.DetailPane;
import io.opensphere.controlpanels.component.map.background.BackgroundOverlay;
import io.opensphere.controlpanels.component.map.boundingbox.BoundingBoxOverlay;
import io.opensphere.controlpanels.component.map.controller.ZoomController;
import io.opensphere.controlpanels.component.map.model.MapModel;
import io.opensphere.controlpanels.component.map.overlay.Overlay;
import io.opensphere.core.Toolbox;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Quadrilateral;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.StrongObservableValue;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.GroupCategorizationUtilities;

/**
 * A panel on which an image and map preview are displayed in addition to the
 * standard text description.
 */
public class ImagePreviewPane extends DetailPane
{
    /**
     * A default image used when no image could be loaded.
     */
    private static final Image BROKEN_IMAGE = SwingFXUtils.toFXImage(ImageUtil.BROKEN_IMAGE, null);

    /**
     * A default image used when no image could be loaded.
     */
    private static final Image NO_IMAGE = SwingFXUtils.toFXImage(ImageUtil.NO_IMAGE, null);

    /**
     * A default image used when no image could be loaded.
     */
    private static final Image LOADING_IMAGE = SwingFXUtils.toFXImage(ImageUtil.LOADING_IMAGE, null);

    /**
     * The panel in which the preview is rendered.
     */
    private final ThreeElementPreviewPane myRenderer;

    /**
     * The model.
     */
    private final MapModel myModel = new MapModel();

    /**
     * The background overlay.
     */
    private final Overlay[] myOverlays;

    /**
     * The zoom controller.
     */
    private final ZoomController myZoomController;

    /**
     * A container in which an error message is stored, if needed.
     */
    private String myMapErrorMessage;

    /**
     * Creates a new image preview pane.
     *
     * @param pToolbox The toolbox through which system interactions occur.
     */
    public ImagePreviewPane(Toolbox pToolbox)
    {
        super(pToolbox);

        Dimension size = new Dimension(450, 225);
        myModel.setHeightWidth(size.height, size.width);
        myZoomController = new ZoomController(myModel);
        myOverlays = new Overlay[] { new BackgroundOverlay(myModel), new BoundingBoxOverlay(myModel) };

        ImageView image = new ImageView(NO_IMAGE);
        image.setPreserveRatio(true);

        ImageView mapNode = new ImageView(NO_IMAGE);
        mapNode.setPreserveRatio(true);

        TextArea textArea = new TextArea();
        textArea.setStyle("-fx-background-color: transparent; ");
        textArea.setWrapText(true);

        myRenderer = new ThreeElementPreviewPane(image, mapNode, textArea);
        myRenderer.loadingMapProperty().set(Boolean.TRUE);
        myRenderer.loadingImageProperty().set(Boolean.TRUE);

        setCenter(myRenderer);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.DetailPane#populate(io.opensphere.mantle.data.DataGroupInfo)
     */
    @Override
    public void populate(DataGroupInfo pDataGroup)
    {
        myRenderer.imageProperty().set(LOADING_IMAGE);
        myRenderer.mapProperty().set(LOADING_IMAGE);

        myRenderer.loadingMapProperty().set(Boolean.TRUE);
        myRenderer.loadingImageProperty().set(Boolean.TRUE);

        // populate the image:
        final ObservableValue<BufferedImage> observableImage = new StrongObservableValue<>();
        observableImage.addListener((obs, old, newValue) -> EventQueueUtilities.runOnEDT(() -> updateImage(observableImage)));
        pDataGroup.getPreviewImage(observableImage);

        // populate the map:
        final ObservableValue<Quadrilateral<GeographicPosition>> observableRegion = new StrongObservableValue<>();
        observableRegion
                .addListener((obs, old, newValue) -> EventQueueUtilities.runOnEDT(() -> updateMapPreview(observableRegion)));
        pDataGroup.getRegion(observableRegion);

        // populate the text:
        String provider = pDataGroup.getTopParentDisplayName();

        Collection<String> categories = StreamUtilities.map(GroupCategorizationUtilities.getGroupCategories(pDataGroup, false),
                input -> StringUtilities.trim(input, 's'));

        String type = StringUtilities.join(", ", categories);

        String summary = pDataGroup.getSummaryDescription();

        String value = StringUtilities.concat("Provider: ", provider, "\n", "Type: ", type, "\n\n", summary, "\n");

        myRenderer.textProperty().setValue(value);
    }

    /**
     * Updates the contents of the map based on the supplied region.
     *
     * @param observableRegion the region in which the update took place.
     */
    protected void updateMapPreview(final ObservableValue<Quadrilateral<GeographicPosition>> observableRegion)
    {
        if (observableRegion.getErrorMessage() == null)
        {
            myMapErrorMessage = null;
            setRegion(observableRegion.get());
            BufferedImage image = new BufferedImage(450, 255, BufferedImage.TYPE_INT_ARGB);
            paint(image.getGraphics());

            myRenderer.mapProperty().set(SwingFXUtils.toFXImage(image, null));
        }
        else
        {
            myMapErrorMessage = observableRegion.getErrorMessage();
            myRenderer.mapProperty().set(NO_IMAGE);
            UserMessageEvent.error(getToolbox().getEventManager(), observableRegion.getErrorMessage(), false, null,
                    observableRegion.getErrorCause(), true);
        }

        Platform.runLater(() -> myRenderer.loadingMapProperty().set(Boolean.FALSE));
    }

    /**
     * Paints the map overlay onto the supplied {@link Graphics} object.
     *
     * <p>
     * TODO: this is an AWT / Swing Paint operation, which is then translated
     * back into JavaFX for rendering. Better to do this on a native JavaFX
     * rendering to avoid all of the conversions.
     * </p>
     *
     * @param g the graphics object onto which the map overlay will be painted.
     */
    public void paint(Graphics g)
    {
        if (myMapErrorMessage == null)
        {
            for (Overlay overlay : myOverlays)
            {
                overlay.draw(g);
            }
        }
        else
        {
            Graphics2D g2d = (Graphics2D)g.create();
            g2d.setPaint(Colors.QUERY_REGION);
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(myMapErrorMessage, g2d);
            g2d.drawString(myMapErrorMessage, (int)(getWidth() - (int)bounds.getWidth()) / 2,
                    (int)(getHeight() - (int)bounds.getHeight()) / 2);
            g2d.dispose();
        }
    }

    /**
     * Sets the region to display on the map.
     *
     * @param region The region to display on the map.
     */
    public void setRegion(Quadrilateral<GeographicPosition> region)
    {
        myZoomController.calculateViewPort(region);
        myModel.setRegion(region);
    }

    /**
     * Updates the image based on the supplied value.
     *
     * @param observableImage the image to update.
     */
    protected void updateImage(final ObservableValue<BufferedImage> observableImage)
    {
        if (observableImage.getErrorMessage() == null)
        {
            WritableImage image = SwingFXUtils.toFXImage(observableImage.get(), null);
            myRenderer.imageProperty().set(image);
        }
        else
        {
            myRenderer.imageProperty().set(BROKEN_IMAGE);
        }

        Platform.runLater(() -> myRenderer.loadingImageProperty().set(Boolean.FALSE));
    }
}
