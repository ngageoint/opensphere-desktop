package io.opensphere.server.serverprovider.http.factory;

import java.util.Collection;

import com.bitsys.common.http.client.HttpClient;

import io.opensphere.core.common.connection.ServerConfiguration;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.toolbox.ServerListManager;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.util.ServerConstants;

/**
 * Configures the timeouts for an HttpClient object.
 *
 */
public class TimeoutsConfigurer
{
    /**
     * Configures the timeouts for the specified client object.
     *
     * @param parameters The parameters to use to configure the HttpClient.
     * @param prefsRegistry The preferences registry.
     * @param serverTools The server toolbox used to get server timeouts.
     */
    public void configure(ConfigurerParameters parameters, PreferencesRegistry prefsRegistry, ServerToolbox serverTools)
    {
        HttpClient client = parameters.getClient();
        String host = parameters.getHost();

        ServerListManager serverManager = serverTools.getServerLayerListManager();
        Collection<ServerConnectionParams> activeServers = serverManager.getActiveServers();

        int readTimeout = -1;
        int connectTimeout = -1;
        for (ServerConnectionParams activeServer : activeServers)
        {
            ServerConfiguration serverConfig = activeServer.getServerConfiguration();
            if (host.equals(serverConfig.getHost()))
            {
                readTimeout = serverConfig.getReadTimeout();
                connectTimeout = serverConfig.getConnectTimeout();
                break;
            }
        }

        if (readTimeout <= 0)
        {
            readTimeout = ServerConstants.getDefaultServerReadTimeoutFromPrefs(prefsRegistry);
        }

        if (connectTimeout <= 0)
        {
            connectTimeout = ServerConstants.getDefaultServerConnectTimeoutFromPrefs(prefsRegistry);
        }

        client.getOptions().setReadTimeout(readTimeout / 1000);
        client.getOptions().setConnectTimeout(connectTimeout / 1000);
    }
}
