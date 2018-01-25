package io.opensphere.xyztile.transformer;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.AbstractTileGeometry.AbstractDivider;
import io.opensphere.core.geometry.ImageManager.RequestObserver;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Base class that divides XYZ tiles into smaller tiles.
 */
public abstract class XYZBaseDivider extends AbstractDivider<GeographicPosition>
{
    /**
     * The layer we are dividing.
     */
    private final XYZTileLayerInfo myLayer;

    /**
     * Notifies the user of tile downloads.
     */
    private final RequestObserver myRequestObserver;

    /**
     * Constructs a new tile divider for XYZ tiles.
     *
     * @param layer The layer to divide tiles for.
     * @param queryTracker Notifies the user of tile downloads.
     */
    public XYZBaseDivider(XYZTileLayerInfo layer, RequestObserver queryTracker)
    {
        super(layer.getName());
        myLayer = layer;
        myRequestObserver = queryTracker;
    }

    @Override
    public Collection<AbstractTileGeometry<?>> divide(AbstractTileGeometry<?> parent)
    {
        List<AbstractTileGeometry<?>> dividedTiles = New.list();

        Object imageKey = parent.getImageManager().getImageKey();
        if (imageKey instanceof ZYXImageKey)
        {
            ZYXImageKey zyx = (ZYXImageKey)imageKey;

            int currentZoomLevel = zyx.getZ();
            int newZoomLevel = currentZoomLevel + 1;

            if (newZoomLevel > myLayer.getMaxLevels())
            {
                throw new IllegalArgumentException("Not enough zoom levels to divide geometry: " + parent);
            }

            int y = zyx.getY();
            int x = zyx.getX();

            int[] newXs = calculateNewXorY(x);
            int[] newYs = calculateNewXorY(y);
            List<GeographicBoundingBox> newBoxes = calculateNewBoxes(newXs, newYs, newZoomLevel,
                    (GeographicBoundingBox)parent.getBounds());

            int index = 0;
            for (int newY : newYs)
            {
                for (int newX : newXs)
                {
                    GeographicBoundingBox newBounds = newBoxes.get(index);
                    ZYXImageKey newKey = new ZYXImageKey(newZoomLevel, newY, newX, newBounds);
                    XYZBaseDivider divider = this;
                    if (newZoomLevel + 1 > myLayer.getMaxLevels())
                    {
                        divider = null;
                    }
                    AbstractTileGeometry<?> subTile = parent.createSubTile(newBounds, newKey, divider);
                    subTile.getImageManager().addRequestObserver(myRequestObserver);
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
     * @param newXs The new x values.
     * @param newYs the new y values.
     * @param zoom The new zoom level.
     * @param overallBounds The overall bounding box of the tile we are
     *            dividing.
     * @return The new sub tile bounding boxes.
     */
    protected abstract List<GeographicBoundingBox> calculateNewBoxes(int[] newXs, int[] newYs, int zoom,
            GeographicBoundingBox overallBounds);

    /**
     * Gets the information about the layer we are dividing.
     *
     * @return The layer information.
     */
    protected XYZTileLayerInfo getLayer()
    {
        return myLayer;
    }

    /**
     * Gets the request observer. The guy that puts the tile downloads in the
     * upper right corner.
     *
     * @return The request observer.
     */
    protected RequestObserver getRequestObserver()
    {
        return myRequestObserver;
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
