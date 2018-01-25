package io.opensphere.server.services;

import io.opensphere.core.common.connection.ServerConfiguration;
import io.opensphere.core.util.PausingTimeBudget;
import io.opensphere.server.customization.ServerCustomization;

/**
 * An Interface that provides connection parameters for a Server. This should
 * include methods to retrieve the URLs for each OGC Service and any
 * authentication, certificate, or network proxy information needed to connect
 * to those URLs.
 */
public interface ServerConnectionParams
{
    /**
     * Method called when authentication with the server fails.
     */
    void failedAuthentication();

    /**
     * Gets the {@link ServerConfiguration} that has all of the connection
     * parameters needed to connect to the server.
     *
     * @return the server configuration
     */
    ServerConfiguration getServerConfiguration();

    /**
     * Gets the {@link ServerCustomization} that helps manage the nuances of how
     * to communicate with the different server types.
     *
     * @return the server customization
     */
    ServerCustomization getServerCustomization();

    /**
     * Gets the unique server id. Normally, this will be the server's URL, but
     * there are cases where the URL cannot be displayed or saved for security
     * reasons, so something else will be provided in its place.
     *
     * @param service the OGC service whose ID should be retrieved
     * @return the unique server id
     */
    String getServerId(String service);

    /**
     * Gets the server title.
     *
     * @return the server title
     */
    String getServerTitle();

    /**
     * Gets the time budget which may be used to time out requests.
     *
     * @return The time budget.
     */
    PausingTimeBudget getTimeBudget();

    /**
     * Gets the WFS URL.
     *
     * @return the WFS URL
     */
    String getWfsUrl();

    /**
     * Gets the WMS GetMap override URL.
     *
     * @return the WMS GetMap override URL
     */
    String getWmsGetMapOverride();

    /**
     * Gets the WMS URL.
     *
     * @return the WMS URL
     */
    String getWmsUrl();

    /**
     * Gets the WPS URL.
     *
     * @return the WPS URL
     */
    String getWpsUrl();

    /**
     * Set the time budget which may be used to time out requests.
     *
     * @param timeBudget The time budget.
     */
    void setTimeBudget(PausingTimeBudget timeBudget);
}
