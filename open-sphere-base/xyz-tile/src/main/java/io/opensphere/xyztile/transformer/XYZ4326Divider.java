package io.opensphere.xyztile.transformer;

import java.util.List;

import io.opensphere.core.geometry.ImageManager.RequestObserver;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Divider used to divide WGS 84 XYZ tiles.
 */
public class XYZ4326Divider extends XYZBaseDivider
{
    /**
     * Constructs a new WGS 84 XYZ divider.
     *
     * @param layer The layer we are dividing tiles for.
     * @param queryTracker Used to notify the user of tile downloads.
     */
    public XYZ4326Divider(XYZTileLayerInfo layer, RequestObserver queryTracker)
    {
        super(layer, queryTracker);
    }

    @Override
    protected List<GeographicBoundingBox> calculateNewBoxes(int[] newXs, int[] newYs, int zoom,
            GeographicBoundingBox overallBounds)
    {
        GeographicPosition upperLeftPos = overallBounds.getUpperLeft();
        GeographicPosition upperRightPos = overallBounds.getUpperRight();
        GeographicPosition lowerLeftPos = overallBounds.getLowerLeft();
        GeographicPosition lowerRightPos = overallBounds.getLowerRight();
        GeographicPosition centerPos = overallBounds.getCenter();

        GeographicBoundingBox upperLeft = new GeographicBoundingBox(
                LatLonAlt.createFromDegrees(centerPos.getLat().getMagnitude(), upperLeftPos.getLon().getMagnitude()),
                LatLonAlt.createFromDegrees(upperLeftPos.getLat().getMagnitude(), centerPos.getLon().getMagnitude()));
        GeographicBoundingBox upperRight = new GeographicBoundingBox(centerPos, upperRightPos);
        GeographicBoundingBox lowerLeft = new GeographicBoundingBox(lowerLeftPos, centerPos);
        GeographicBoundingBox lowerRight = new GeographicBoundingBox(
                LatLonAlt.createFromDegrees(lowerLeftPos.getLat().getMagnitude(), centerPos.getLon().getMagnitude()),
                LatLonAlt.createFromDegrees(centerPos.getLat().getMagnitude(), lowerRightPos.getLon().getMagnitude()));

        List<GeographicBoundingBox> subBoxes = null;
        if (getLayer().isTms())
        {
            return New.list(lowerLeft, lowerRight, upperLeft, upperRight);
        }
        else
        {
            subBoxes = New.list(upperLeft, upperRight, lowerLeft, lowerRight);
        }

        return subBoxes;
    }
}
