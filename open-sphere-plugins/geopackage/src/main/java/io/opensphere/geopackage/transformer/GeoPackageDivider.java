package io.opensphere.geopackage.transformer;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.AbstractTileGeometry.AbstractDivider;
import io.opensphere.core.geometry.AbstractTileGeometry.Divider;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageTileLayer;

/**
 * A {@link Divider} that knows how to divide a given geopackage tile into
 * smaller more zoomed in ones.
 */
public class GeoPackageDivider extends AbstractDivider<GeographicPosition>
{
    /**
     * The layer to divide tiles for.
     */
    private final GeoPackageTileLayer myLayer;

    /**
     * Constructs a new tile divider geopackage tiles.
     *
     * @param layer The layer to divide tiles for.
     */
    public GeoPackageDivider(GeoPackageTileLayer layer)
    {
        super(layer.getId());
        myLayer = layer;
    }

    @Override
    public Collection<AbstractTileGeometry<?>> divide(AbstractTileGeometry<?> parent)
    {
        long currentZoomLevel = myLayer.getMinZoomLevel() + parent.getGeneration();
        long newZoomLevel = currentZoomLevel + 1;

        if (newZoomLevel > myLayer.getMaxZoomLevel())
        {
            throw new IllegalArgumentException("Not enough zoom levels to divide geometry: " + parent);
        }

        List<AbstractTileGeometry<?>> dividedTiles = New.list();

        Object imageKey = parent.getImageManager().getImageKey();
        if (imageKey instanceof ZYXImageKey)
        {
            ZYXImageKey zyx = (ZYXImageKey)imageKey;
            int y = zyx.getY();
            int x = zyx.getX();

            int[] newXs = calculateNewXorY(x);
            int[] newYs = calculateNewXorY(y);
            List<GeographicBoundingBox> newBoxes = calculateNewBoxes((GeographicBoundingBox)parent.getBounds());

            int index = 0;
            for (int newY : newYs)
            {
                for (int newX : newXs)
                {
                    GeographicBoundingBox newBounds = newBoxes.get(index);
                    ZYXImageKey newKey = new ZYXImageKey((int)newZoomLevel, newY, newX, newBounds);
                    GeoPackageDivider divider = this;
                    if (newZoomLevel + 1 > myLayer.getMaxZoomLevel())
                    {
                        divider = null;
                    }
                    AbstractTileGeometry<?> subTile = parent.createSubTile(newBounds, newKey, divider);
                    dividedTiles.add(subTile);

                    index++;
                }
            }
        }

        return dividedTiles;
    }

    /**
     * Calculates the new bounding boxes of the divided tile.
     *
     * @param bbox The bounding box of the tile we are dividing.
     * @return The new sub tile bounding boxes.
     */
    private List<GeographicBoundingBox> calculateNewBoxes(GeographicBoundingBox bbox)
    {
        GeographicPosition upperLeftPos = bbox.getUpperLeft();
        GeographicPosition upperRightPos = bbox.getUpperRight();
        GeographicPosition lowerLeftPos = bbox.getLowerLeft();
        GeographicPosition lowerRightPos = bbox.getLowerRight();
        GeographicPosition centerPos = bbox.getCenter();

        GeographicBoundingBox upperLeft = new GeographicBoundingBox(
                LatLonAlt.createFromDegrees(centerPos.getLat().getMagnitude(), upperLeftPos.getLon().getMagnitude()),
                LatLonAlt.createFromDegrees(upperLeftPos.getLat().getMagnitude(), centerPos.getLon().getMagnitude()));
        GeographicBoundingBox upperRight = new GeographicBoundingBox(centerPos, upperRightPos);
        GeographicBoundingBox lowerLeft = new GeographicBoundingBox(lowerLeftPos, centerPos);
        GeographicBoundingBox lowerRight = new GeographicBoundingBox(
                LatLonAlt.createFromDegrees(lowerLeftPos.getLat().getMagnitude(), centerPos.getLon().getMagnitude()),
                LatLonAlt.createFromDegrees(centerPos.getLat().getMagnitude(), lowerRightPos.getLon().getMagnitude()));

        return New.list(upperLeft, upperRight, lowerLeft, lowerRight);
    }

    /**
     * Calculates the new x or y coordinates for the divided tiles.
     *
     * @param currentXorY The current x or y coordinate of the tile we are
     *            dividing.
     * @return The new x or y coordinates of the sub tiles.
     */
    private int[] calculateNewXorY(int currentXorY)
    {
        int count = currentXorY + 1;
        int newCount = count * 2;

        return new int[] { newCount - 2, newCount - 1 };
    }
}
