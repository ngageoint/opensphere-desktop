package io.opensphere.server.services;

import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultMapTileVisualizationInfo;

/**
 * The Class ServerMapVisualizationInfo.
 */
public class ServerMapVisualizationInfo extends DefaultMapTileVisualizationInfo
{
    /**
     * Instantiates a new map visualization info for server layers.
     *
     * @param visType the {@link MapVisualizationType}
     * @param props the props
     */
    public ServerMapVisualizationInfo(MapVisualizationType visType, TileRenderProperties props)
    {
        super(visType, props, true);
    }
}
