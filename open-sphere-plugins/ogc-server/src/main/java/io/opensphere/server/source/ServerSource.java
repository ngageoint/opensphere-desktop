package io.opensphere.server.source;

import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.UrlSource;

/** Interface for data sources that connect to external servers. */
public interface ServerSource extends IDataSource, UrlSource
{
    /**
     * Creates a near copy of this data source suitable for export.
     *
     * @return An exportable copy of this data source
     */
    ServerSource createExportDataSource();

    /**
     * Gets the URL from the source for the given service.
     *
     * @param service the service
     * @return the URL
     */
    String getURL(String service);

    /**
     * Sets the URL in the source for the given service.
     *
     * @param service the service
     * @param url the URL
     */
    void setURL(String service, String url);
}
