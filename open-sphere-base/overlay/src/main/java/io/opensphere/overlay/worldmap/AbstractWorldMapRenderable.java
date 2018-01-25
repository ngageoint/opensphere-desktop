package io.opensphere.overlay.worldmap;

import java.awt.Point;

import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenBoundingBox;

/** Abstract superclass for the mini map components. */
public abstract class AbstractWorldMapRenderable extends Renderable
{
    /**
     * Construct me.
     *
     * @param parent parent component.
     */
    public AbstractWorldMapRenderable(Component parent)
    {
        super(parent);
    }

    /**
     * Given a point over the world map in screen coordinates, convert to
     * lat/lon.
     *
     * @param point the point to convert.
     * @return the point as GeographicPosition
     */
    protected final GeographicPosition convertToLatLon(Point point)
    {
        ScreenBoundingBox bbox = getDrawBounds();

        // We might want to scale this in the future
        double scaleFactor = 1;
        double scaledWidth = bbox.getWidth() * scaleFactor;
        double scaledHeight = bbox.getHeight() * scaleFactor;

        double lat = -1 * ((point.getY() - getAbsoluteLocation().getUpperLeft().getY()) / scaledHeight * 180 - 90);
        double lon = (point.getX() - getAbsoluteLocation().getLowerLeft().getX()) / scaledWidth * 360 - 180;

        LatLonAlt lla = LatLonAlt.createFromDegrees(lat, lon);
        return new GeographicPosition(lla);
    }
}
