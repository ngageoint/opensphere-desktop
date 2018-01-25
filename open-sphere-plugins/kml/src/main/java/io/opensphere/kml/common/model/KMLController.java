package io.opensphere.kml.common.model;

/**
 * An interface for a KML controller.
 */
public interface KMLController
{
    /**
     * Adds data to the controller.
     *
     * @param dataEvent The data event
     * @param reload Flag indicating if this is a reload
     */
    void addData(KMLDataEvent dataEvent, boolean reload);

    /**
     * Removes a data source from the controller.
     *
     * @param dataSource The data source
     */
    void removeData(KMLDataSource dataSource);
}
