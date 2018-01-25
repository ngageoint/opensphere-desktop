package io.opensphere.core.util;

import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * A utility class that contains some common methods to access terrain data.
 */
public final class TerrainUtil
{
    /**
     * The instance of this class.
     */
    private static final TerrainUtil ourInstance = new TerrainUtil();

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static TerrainUtil getInstance()
    {
        return ourInstance;
    }

    /**
     * Not constructible.
     */
    private TerrainUtil()
    {
    }

    /**
     * Gets the elevation in meters at the specified latitude and longitude.
     *
     * @param mapContext The {@link MapContext} which can be retrieved from
     *            toolbox.getMapManager().
     * @param position The position to get the elevation at.
     * @return The elevation and the given location.
     */
    public double getElevationInMeters(MapContext<DynamicViewer> mapContext, GeographicPosition position)
    {
        double elevation = 0.;
        Projection proj = mapContext.getRawProjection();

        if (proj.getElevationManager() != null)
        {
            elevation = proj.getElevationManager().getElevationM(position, true);
            elevation = Math.max(0., elevation);
        }

        return elevation;
    }
}
