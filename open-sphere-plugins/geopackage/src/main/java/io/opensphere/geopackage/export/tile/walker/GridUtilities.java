package io.opensphere.geopackage.export.tile.walker;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.collections.CollectionUtilities;

/** Grid utilities. */
public final class GridUtilities
{
    /**
     * Creates a grid from the geometries.
     *
     * @param geoms the geometries
     * @return the grid
     */
    public static AbstractTileGeometry<?>[][] getGrid(Collection<? extends AbstractTileGeometry<?>> geoms)
    {
        List<Double> lats = CollectionUtilities.sort(geoms.stream().map(GridUtilities::getMaxLat).collect(Collectors.toSet()),
            (d1, d2) -> -Double.compare(d1.doubleValue(), d2.doubleValue()));
        List<Double> lons = CollectionUtilities.sort(geoms.stream().map(GridUtilities::getMaxLon).collect(Collectors.toSet()));
        AbstractTileGeometry<?>[][] grid = new AbstractTileGeometry<?>[lats.size()][lons.size()];
        for (AbstractTileGeometry<?> geom : geoms)
        {
            Double lat = getMaxLat(geom);
            Double lon = getMaxLon(geom);
            grid[lats.indexOf(lat)][lons.indexOf(lon)] = geom;
        }
        return grid;
    }

    /**
     * Gets the maximum latitude from a tile geometry.
     *
     * @param geom the geometry
     * @return the maximum latitude
     */
    private static Double getMaxLat(AbstractTileGeometry<?> geom)
    {
        return Double.valueOf(geom.getBounds().getVertices().stream()
                .mapToDouble(v -> ((GeographicPosition)v).getLatLonAlt().getLatD()).max().getAsDouble());
    }

    /**
     * Gets the maximum longitude from a tile geometry.
     *
     * @param geom the geometry
     * @return the maximum longitude
     */
    private static Double getMaxLon(AbstractTileGeometry<?> geom)
    {
        return Double.valueOf(geom.getBounds().getVertices().stream()
                .mapToDouble(v -> ((GeographicPosition)v).getLatLonAlt().getLonD()).max().getAsDouble());
    }

    /** Disallow instantiation. */
    private GridUtilities()
    {
    }
}
