package io.opensphere.mantle.data.cache;

import java.util.List;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * High speed query bypassing interface into the DataElementCache, only for use
 * by select tools. Such as the ListTool where the retrieve does not need to
 * "warm" the cache, the fastest possible access to data is provided with the
 * minimal overhead. Note: May not work with the DataRegistry if enabled as the
 * baking store for the cache.
 */
public interface DirectAccessRetriever
{
    /**
     * Close the retriever, use this when the retriever is no longer needed so
     * that any resources utilized by the retriever can be closed properly.
     */
    void close();

    /**
     * Gets the data type.
     *
     * @return the data type
     */
    DataTypeInfo getDataType();

    /**
     * Gets the map geometry support for the specified entry.
     *
     * @param cacheId the cache id
     * @return the map geometry support
     */
    MapGeometrySupport getMapGeometrySupport(long cacheId);

    /**
     * Gets the meta data for the specified entry.
     *
     * @param cacheId the cache id
     * @return the meta data
     */
    List<Object> getMetaData(long cacheId);

    /**
     * Gets the origin id for the specified entry.
     *
     * @param cacheId the cache id
     * @return the origin id
     */
    Long getOriginId(long cacheId);

    /**
     * Gets the time span for the specified entry.
     *
     * @param cacheId the cache id
     * @return the time span
     */
    TimeSpan getTimeSpan(long cacheId);

    /**
     * Gets the visualization state for the specified entry.
     *
     * @param cacheId the cache id
     * @return the visualization state
     */
    VisualizationState getVisualizationState(long cacheId);
}
