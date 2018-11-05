package io.opensphere.mantle.iconproject.panels;

import org.controlsfx.control.GridCell;

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

/**
 * A grid cell implementation used to render an {@link IconRecord}.
 */
public class IconGridCell extends GridCell<IconRecord>
{
    /** The panel in which items are displayed. */
    private final AnchorPane myContainer;

    /** The image view updated when the icon changes. */
    private final ImageView imageView;

    /** The model in which state is maintained. */
    private final PanelModel myModel;

    /**
     * Creates a default ImageGridCell instance, which will preserve image
     * properties.
     *
     * @param model The model in which state is maintained.
     */
    public IconGridCell(PanelModel model)
    {
        myModel = model;
        getStyleClass().add("image-grid-cell");

        imageView = new ImageView();
        imageView.fitHeightProperty().bind(heightProperty());
        imageView.fitWidthProperty().bind(widthProperty());
        imageView.setSmooth(true);
        imageView.setPreserveRatio(true);

        myContainer = new AnchorPane();
        myContainer.getChildren().add(imageView);

        AnchorPane.setRightAnchor(imageView, 0.0);
        AnchorPane.setLeftAnchor(imageView, 0.0);
        AnchorPane.setTopAnchor(imageView, 0.0);
        AnchorPane.setBottomAnchor(imageView, 0.0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateItem(IconRecord item, boolean empty)
    {
        super.updateItem(item, empty);

        if (myModel.getSelectedRecord().get() != null && myModel.getSelectedRecord().get().equals(item))
        {
            setStyle("-fx-effect: dropshadow(three-pass-box, lime, 15,.5, 0, 0);");
        }

        if (empty)
        {
            imageView.onMouseEnteredProperty().set(null);
            imageView.onMouseExitedProperty().set(null);
            imageView.onMouseClickedProperty().set(null);
            setGraphic(null);
        }
        else
        {
            myContainer.setOnMouseEntered(e ->
            {
                setStyle("-fx-effect: dropshadow(three-pass-box, aqua, 15,.5, 0, 0);");
                myModel.previewRecordProperty().set(item);
            });
            myContainer.setOnMouseExited(e ->
            {
                if (myModel.getSelectedRecord().get() != null && myModel.getSelectedRecord().get().equals(item))
                {
                    setStyle("-fx-effect: dropshadow(three-pass-box, lime, 15,.5, 0, 0);");
                }
                else
                {
                    setStyle(null);
                }
            });
            myContainer.setOnMouseClicked(e -> handleMouseClick(item, imageView));

            imageView.setImage(item.getImage());
            setGraphic(myContainer);
        }
    }

    /**
     * Handles a left mouse click.
     *
     * @param record The icon record subject to the click event.
     * @param source the source object that sent the click event.
     */
    private void handleMouseClick(IconRecord record, Node source)
    {
        myModel.getSelectedRecord().set(record);
        myModel.getSingleSelectedIcon().clear();
        myModel.getSingleSelectedIcon().put(record, source);
    }
}
