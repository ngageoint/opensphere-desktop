package io.opensphere.mantle.data.cache;

import java.util.List;

import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * The Interface LoadedElementDataView.
 */
public interface LoadedElementDataView
{
    /**
     * Gets the map geometry support.
     *
     * @return the map geometry support
     */
    MapGeometrySupport getMapGeometrySupport();

    /**
     * Gets the meta data.
     *
     * @return the meta data
     */
    List<Object> getMetaData();

    /**
     * Gets the origin id.
     *
     * @return the origin id
     */
    Long getOriginId();
}
