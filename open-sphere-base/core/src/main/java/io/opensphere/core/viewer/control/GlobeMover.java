package io.opensphere.core.viewer.control;

import io.opensphere.core.MapManager;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.GeographicPosition;

/**
 * Moves the globe so that the location the mouse was pointing at before zoom
 * will be at the same screen location after zoom.
 */
public class GlobeMover
{
    /**
     * Moves the globe so that the location the mouse was pointing at before
     * zoom will be at the same screen location after zoom.
     *
     * @param viewer Used to perform the zoom and the move of the globe.
     * @param mapManager Used to get the geographic position of the mouse
     *            position.
     * @param position The geographic position of the mouse before zoom.
     * @param mouseScreenPos The mouse screen position.
     */
    public void moveGlobe(AbstractViewerControlTranslator viewer, MapManager mapManager, GeographicPosition position,
            Vector2i mouseScreenPos)
    {
        if (position != null)
        {
            Vector2i previousMouseCoordinatesScreenPosition = mapManager.convertToPoint(position);
            viewer.moveView(previousMouseCoordinatesScreenPosition, mouseScreenPos);
        }
    }
}
