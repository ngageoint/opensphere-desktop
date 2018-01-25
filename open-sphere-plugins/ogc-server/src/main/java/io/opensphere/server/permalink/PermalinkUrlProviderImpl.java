package io.opensphere.server.permalink;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.common.connection.ServerConfiguration;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.toolbox.PermalinkUrlProvider;
import io.opensphere.server.toolbox.ServerListManager;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;

/**
 * Gets the permalink url for a specified file. The url returned is not the full
 * url, just permalink url to be appended to the server url.
 *
 */
public class PermalinkUrlProviderImpl implements PermalinkUrlProvider
{
    /**
     * Used to get the specific server configuration.
     */
    private final ServerSourceControllerManager myControllerManager;

    /**
     * Gets the list of active servers.
     */
    private final ServerListManager myServerListManager;

    /**
     * Constructs a new permalink Url provider.
     *
     * @param controllerManager Used to get the server configurations.
     * @param serverListManager Used to get the list of active servers.
     */
    public PermalinkUrlProviderImpl(ServerSourceControllerManager controllerManager, ServerListManager serverListManager)
    {
        myControllerManager = controllerManager;
        myServerListManager = serverListManager;
    }

    @Override
    public String getPermalinkUrl(String host)
    {
        String permalinkUrl = null;

        String serverName = null;

        for (ServerConnectionParams params : myServerListManager.getActiveServers())
        {
            ServerConfiguration configuration = params.getServerConfiguration();
            if (configuration != null && host.equals(configuration.getHost()))
            {
                serverName = params.getServerTitle();
                break;
            }
        }

        if (StringUtils.isNotEmpty(serverName))
        {
            for (ServerSourceController controller : myControllerManager.getControllers())
            {
                for (IDataSource server : controller.getSourceList())
                {
                    if (server instanceof OGCServerSource)
                    {
                        OGCServerSource ogcServer = (OGCServerSource)server;
                        if (StringUtils.equals(serverName, ogcServer.getName()))
                        {
                            permalinkUrl = ogcServer.getPermalinkUrl();
                            break;
                        }
                    }
                }

                if (StringUtils.isNotEmpty(permalinkUrl))
                {
                    break;
                }
            }
        }

        return permalinkUrl;
    }
}
