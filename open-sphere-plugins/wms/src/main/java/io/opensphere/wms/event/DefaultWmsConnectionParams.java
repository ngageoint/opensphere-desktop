package io.opensphere.wms.event;

import io.opensphere.core.common.connection.ServerConfiguration;
import io.opensphere.core.util.PausingTimeBudget;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.AuthenticationHelper;
import io.opensphere.server.source.OGCServerSource;

/**
 * The Default implementation of the WMSConnectionParams interface.
 */
public class DefaultWmsConnectionParams implements ServerConnectionParams
{
    /** The parameters needed to connect to the server. */
    private final ServerConfiguration myServerConfig;

    /** Special rules for how to configure/format requests to the server. */
    private final ServerCustomization myServerCustomization;

    /** The ID that uniquely identifies this server. */
    private final String myServerId;

    /** The Server title. */
    private final String myServerTitle;

    /** My WMS GetMap override URL. */
    private final String myWmsGetMapOverride;

    /** My WMS URL. */
    private final String myWmsUrl;

    /**
     * Instantiates a new default WMS connection params.
     *
     * @param other the other
     */
    public DefaultWmsConnectionParams(ServerConnectionParams other)
    {
        myWmsUrl = other.getWmsUrl();
        myWmsGetMapOverride = other.getWmsGetMapOverride();
        myServerId = other.getServerId(OGCServerSource.WMS_SERVICE);
        myServerTitle = other.getServerTitle();
        myServerConfig = other.getServerConfiguration();
        myServerCustomization = other.getServerCustomization();
    }

    @Override
    public void failedAuthentication()
    {
        AuthenticationHelper.failedAuthentication(myServerConfig);
    }

    @Override
    public ServerConfiguration getServerConfiguration()
    {
        return myServerConfig;
    }

    @Override
    public ServerCustomization getServerCustomization()
    {
        return myServerCustomization;
    }

    @Override
    public String getServerId(String service)
    {
        return myServerId;
    }

    @Override
    public String getServerTitle()
    {
        return myServerTitle;
    }

    @Override
    public PausingTimeBudget getTimeBudget()
    {
        return null;
    }

    @Override
    public String getWfsUrl()
    {
        // Don't care about WFS connection info
        return null;
    }

    @Override
    public String getWmsGetMapOverride()
    {
        return myWmsGetMapOverride;
    }

    @Override
    public String getWmsUrl()
    {
        return myWmsUrl;
    }

    @Override
    public String getWpsUrl()
    {
        // Don't care about WPS connection info
        return null;
    }

    @Override
    public void setTimeBudget(PausingTimeBudget timeBudget)
    {
    }
}
