package io.opensphere.core.util.callout;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;

import io.opensphere.core.geometry.GeoScreenBubbleGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.GeoScreenBoundingBox;
import io.opensphere.core.model.GeographicBoxAnchor;

/** The handler for callouts from the drag manager. */
public abstract class CalloutDragHandler
{
    /**
     * Get the anchor position for the new drag position. This only needs to be
     * overridden in the cases where the geographic anchor might change with the
     * position of the tile.
     *
     * @param mouseEvent the drag event.
     * @param mouseDownBox The position of the bounding box at the start of the
     *            drag.
     * @param tileDragMouseDown The position of the mouse at the start of the
     *            drag.
     * @return The anchor position.
     */
    public GeographicBoxAnchor getAnchor(MouseEvent mouseEvent, GeoScreenBoundingBox mouseDownBox, Point tileDragMouseDown)
    {
        GeographicBoxAnchor oldAnchor = mouseDownBox.getAnchor();
        Vector2i oldOffset = oldAnchor.getAnchorOffset() == null ? Vector2i.ORIGIN : oldAnchor.getAnchorOffset();
        Vector2i attachmentOffset = oldOffset.add(new Vector2i((int)(mouseEvent.getPoint().getX() - tileDragMouseDown.getX()),
                (int)(mouseEvent.getPoint().getY() - tileDragMouseDown.getY())));
        return new GeographicBoxAnchor(oldAnchor.getGeographicAnchor(), attachmentOffset, oldAnchor.getHorizontalAlignment(),
                oldAnchor.getVerticalAlignment());
    }

    /**
     * Get the bubble associated with a tile.
     *
     * @param tile The tile.
     * @return the bubble.
     */
    public abstract GeoScreenBubbleGeometry getAssociatedBubble(TileGeometry tile);

    /**
     * Get the line color to use for new bubbles.
     *
     * @return The line color.
     */
    public Color getLineColor()
    {
        return Color.WHITE;
    }

    /**
     * Get the line width to use for new bubbles.
     *
     * @return The line width.
     */
    public float getLineWidth()
    {
        return 1f;
    }

    /**
     * Tell whether this geometry should be handled by this handler.
     *
     * @param geom the tile which might be handled.
     * @return {@code true} when the geometry is owned by this handler.
     */
    public abstract boolean handles(TileGeometry geom);

    /**
     * Replace a callout with a new once created as a result of a drag.
     *
     * @param replace The tile to replace.
     * @param newTile The replacement tile.
     * @param newBubble The bubble for the replacement tile.
     */
    public abstract void replaceCallout(TileGeometry replace, TileGeometry newTile, GeoScreenBubbleGeometry newBubble);
}
