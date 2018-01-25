package io.opensphere.geopackage.export.tile.walker;

import java.util.List;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.util.collections.New;

/**
 * Gets the top level {@link TileGeometry} for a given layer.
 */
public class TopLevelGeometryProvider
{
    /**
     * Contains all published geometries.
     */
    private final GeometryRegistry myGeometryRegistry;

    /**
     * Constructs a new {@link TopLevelGeometryProvider}.
     *
     * @param geometryRegistry Contains all published geometries.
     */
    public TopLevelGeometryProvider(GeometryRegistry geometryRegistry)
    {
        myGeometryRegistry = geometryRegistry;
    }

    /**
     * Gets the top level geometries.
     *
     * @param layerId The id of the layer to get its geometries for.
     * @return The cloned top level geometries.
     */
    public List<AbstractTileGeometry<?>> getTopLevelGeometries(String layerId)
    {
        List<AbstractTileGeometry<?>> geometries = New.list();

        for (Geometry geometry : myGeometryRegistry.getGeometries())
        {
            if (geometry instanceof AbstractTileGeometry)
            {
                AbstractTileGeometry<?> tile = (AbstractTileGeometry<?>)geometry;
                if (layerId.equals(tile.getLayerId()))
                {
                    geometries.add(tile.clone());
                }
            }
        }

        return geometries;
    }
}
