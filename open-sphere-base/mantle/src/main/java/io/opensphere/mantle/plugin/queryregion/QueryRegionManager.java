package io.opensphere.mantle.plugin.queryregion;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Service;

/**
 * Manager for query regions. Query regions are geographic areas where there are
 * data of interest.
 */
public interface QueryRegionManager
{
    /**
     * Add a query region to the manager. The provided polygons will be sent to
     * the geometry registry to be drawn if
     * {@link BaseRenderProperties#isDrawable()} or
     * {@link BaseRenderProperties#isPickable()} is {@code true}. Listeners will
     * be notified of the new query region. Queries added in this way have no
     * time constraint.
     *
     * @param polygons The polygons that make up the region.
     * @param typeKeys Type keys for layers associated with the query.
     * @return The created region.
     */
    QueryRegion addQueryRegion(Collection<? extends PolygonGeometry> polygons, Collection<? extends String> typeKeys);

    /**
     * Add a query region to the manager. The provided polygons will be sent to
     * the geometry registry to be drawn if
     * {@link BaseRenderProperties#isDrawable()} or
     * {@link BaseRenderProperties#isPickable()} is {@code true}. Listeners will
     * be notified of the new query region. Queries added in this way have no
     * time constraint.
     *
     * @param polygons The polygons that make up the region.
     * @param typeKeyToFilterMap Mapping of type keys for layers associated with
     *            the query to filters for those layers.
     * @return The created region.
     */
    QueryRegion addQueryRegion(Collection<? extends PolygonGeometry> polygons,
            Map<? extends String, ? extends DataFilter> typeKeyToFilterMap);

    /**
     * Add a query region to the manager. The provided polygons will be sent to
     * the geometry registry to be drawn if
     * {@link BaseRenderProperties#isDrawable()} or
     * {@link BaseRenderProperties#isPickable()} is {@code true}. Listeners will
     * be notified of the new query region.
     *
     * @param polygons The polygons that make up the region.
     * @param validTimes The times that the query is valid.
     * @param typeKeyToFilterMap Mapping of type keys for layers associated with
     *            the query to filters for those layers.
     * @return The created region.
     */
    QueryRegion addQueryRegion(Collection<? extends PolygonGeometry> polygons, Collection<? extends TimeSpan> validTimes,
            Map<? extends String, ? extends DataFilter> typeKeyToFilterMap);

    /**
     * Add a query region to the manager. The provided polygons will be sent to
     * the geometry registry to be drawn if
     * {@link BaseRenderProperties#isDrawable()} or
     * {@link BaseRenderProperties#isPickable()} is {@code true}. Listeners will
     * be notified of the new query region. Queries added in this way have no
     * time constraint.
     *
     * @param region The query region.
     */
    void addQueryRegion(QueryRegion region);

    /**
     * Adds the query region listener.
     *
     * @param listener the listener
     */
    void addQueryRegionListener(QueryRegionListener listener);

    /**
     * Derive some orange query polygons from the input polygons.
     *
     * @param input The input polygons.
     * @param dotted If the polygons' lines should be dotted.
     * @return The output polygons.
     */
    Collection<? extends PolygonGeometry> deriveQueryPolygons(Collection<? extends PolygonGeometry> input, boolean dotted);

    /**
     * Get a query region that has been added with the given geometry.
     *
     * @param geom The geometry.
     * @return The query region, or {@code null} if one was not found.
     */
    QueryRegion getQueryRegion(Geometry geom);

    /**
     * Gets the query regions.
     *
     * @return the query regions
     */
    List<? extends QueryRegion> getQueryRegions();

    /**
     * Removes all queries.
     */
    void removeAllQueryRegions();

    /**
     * Remove a query region associated with some geometries.
     *
     * @param polygons The geometries.
     */
    void removeQueryRegion(Collection<? extends Geometry> polygons);

    /**
     * Removes the query region.
     *
     * @param region The region.
     * @return {@code true} if the region was removed.
     */
    boolean removeQueryRegion(QueryRegion region);

    /**
     * Removes the query region listener.
     *
     * @param listener The listener.
     */
    void removeQueryRegionListener(QueryRegionListener listener);

    /**
     * Remove query regions.
     *
     * @param regions The regions to be removed.
     */
    void removeQueryRegions(Collection<? extends QueryRegion> regions);

    /**
     * Creates a service that can be used to add/remove the given query region
     * listener.
     *
     * @param listener the listener
     * @return the service
     */
    default Service getQueryRegionListenerService(final QueryRegionListener listener)
    {
        return new Service()
        {
            @Override
            public void open()
            {
                addQueryRegionListener(listener);
            }

            @Override
            public void close()
            {
                removeQueryRegionListener(listener);
            }
        };
    }
}
