package io.opensphere.overlay.util;

import java.awt.Point;

import io.opensphere.core.Toolbox;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;

/**
 * Utility class relating to the mouse position on the screen.
 */
public class MousePositionUtils
{
    /** The current position of the mouse on the globe. */
    private static GeographicPosition currentPosition;

    /**
     * Set the mouse position.
     *
     * @param point the mouse position as a point on the screen
     * @param toolbox the toolbox
     */
    public static void setMousePosition(Point point, Toolbox toolbox)
    {
        if (point != null)
        {
            currentPosition = toolbox.getMapManager().convertToPosition(new Vector2i(point),
                    Altitude.ReferenceLevel.ELLIPSOID);
        }
    }

    /**
     * Get the mouse position
     *
     * @return the mouse position on the globe
     */
    public static GeographicPosition getMousePosition()
    {
        return currentPosition;
    }

    /**
     * Disallow instantiation.
     */
    private MousePositionUtils()
    {
    }
}
