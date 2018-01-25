package io.opensphere.mantle.data.cache;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.element.VisualizationState;

/**
 * The Class CacheEntryView.
 */
public interface CacheEntryView
{
    /**
     * Gets the data type key.
     *
     * @return the data type key
     */
    String getDataTypeKey();

    /**
     * Gets the loaded element data.
     *
     * @return the loaded element data
     */
    LoadedElementDataView getLoadedElementData();

    /**
     * Gets the time.
     *
     * @return the time
     */
    TimeSpan getTime();

    /**
     * Gets the vis state.
     *
     * @return the vis state
     */
    VisualizationState getVisState();

    /**
     * Checks if is cached.
     *
     * @return true, if is cached
     */
    boolean isCached();

    /**
     * Checks if is map geometry support cached.
     *
     * @return true, if is map geometry support cached
     */
    boolean isMapGeometrySupportCached();

    /**
     * Checks if is meta data info cached.
     *
     * @return true, if is meta data info cached
     */
    boolean isMetaDataInfoCached();

    /**
     * Checks if is origin id cached.
     *
     * @return true, if is origin id cached
     */
    boolean isOriginIdCached();
}
