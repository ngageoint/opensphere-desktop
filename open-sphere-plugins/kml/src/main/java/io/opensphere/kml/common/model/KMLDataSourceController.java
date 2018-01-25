package io.opensphere.kml.common.model;

import io.opensphere.mantle.data.DataGroupInfoAssistant;

/**
 * Interface for KML data source controller.
 */
public interface KMLDataSourceController
{
    /**
     * Adds the source using the assistant in the controller.
     *
     * @param dataSource the data source
     */
    void addSource(KMLDataSource dataSource);

    /**
     * Adds the source and adds the provided DataGroupInfoAssistant.
     *
     * @param dataSource the data source
     * @param dgiAssistant the dgi assistant
     */
    void addSource(KMLDataSource dataSource, DataGroupInfoAssistant dgiAssistant);

    /**
     * Removes a data source from the controller.
     *
     * @param dataSource The data source
     */
    void removeSource(KMLDataSource dataSource);
}
