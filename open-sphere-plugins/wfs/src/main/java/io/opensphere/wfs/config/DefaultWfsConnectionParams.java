package io.opensphere.wfs.config;

import io.opensphere.core.common.connection.ServerConfiguration;
import io.opensphere.core.util.PausingTimeBudget;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.AuthenticationHelper;
import io.opensphere.server.source.OGCServerSource;

/**
 * The Default implementation of the WFSConnectionParams interface.
 */
public class DefaultWfsConnectionParams implements ServerConnectionParams
{
    /** The parameters needed to connect to the server. */
    private final ServerConfiguration myServerConfig;

    /** Special rules for how to configure/format requests to the server. */
    private final ServerCustomization myServerCustomization;

    /** The ID that uniquely identifies this server. */
    private final String myServerId;

    /** The Server title. */
    private final String myServerTitle;

    /** My WFS URL. */
    private final String myWfsUrl;

    /**
     * Copy constructor for a new default WFS connection params.
     *
     * @param other the object to copy from
     */
    public DefaultWfsConnectionParams(ServerConnectionParams other)
    {
        myWfsUrl = other.getWfsUrl();
        myServerId = other.getServerId(OGCServerSource.WFS_SERVICE);
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
        return myWfsUrl;
    }

    @Override
    public String getWmsGetMapOverride()
    {
        // Don't care about WMS in this plugin
        return null;
    }

    @Override
    public String getWmsUrl()
    {
        // Don't care about WMS in this plugin
        return null;
    }

    @Override
    public String getWpsUrl()
    {
        // Don't care about WPS in this plugin
        return null;
    }

    @Override
    public void setTimeBudget(PausingTimeBudget timeBudget)
    {
    }
}
