package io.opensphere.mantle.plugin.queryregion;

import java.util.Collection;
import java.util.Map;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.model.time.TimeSpan;

/**
 * A region that data is requested.
 */
public interface QueryRegion
{
    /**
     * Determine if this query region applies to a data type.
     *
     * @param typeKey The key for the data type.
     * @return {@code true} if the region applies to the type.
     */
    boolean appliesToType(String typeKey);

    /**
     * Get the polygons that compose the query region.
     *
     * @return The polygons.
     */
    Collection<? extends PolygonGeometry> getGeometries();

    /**
     * Get the type keys associated with this region. If this is empty, this
     * region applies to all data types.
     *
     * @return The type keys.
     */
    Collection<? extends String> getTypeKeys();

    /**
     * Get a mapping of type keys for layers associated with the query to
     * filters for those layers.
     *
     * @return The type key to filter map.
     */
    Map<? extends String, ? extends DataFilter> getTypeKeyToFilterMap();

    /**
     * Get the times that this query region is valid.
     *
     * @return The valid times.
     */
    Collection<? extends TimeSpan> getValidTimes();

    /**
     * Gets the ID of this query region.
     *
     * @return the ID
     */
    String getId();
}
