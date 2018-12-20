package io.opensphere.server.serverprovider.http.factory;

import java.awt.Component;
import java.util.function.Supplier;

import com.bitsys.common.http.client.DefaultHttpClient;
import com.bitsys.common.http.client.HttpClient;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.SecurityManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.server.HttpServer;
import io.opensphere.server.serverprovider.SecurityComponentsProvider;
import io.opensphere.server.serverprovider.ServerFactory;
import io.opensphere.server.serverprovider.http.HttpServerImpl;
import io.opensphere.server.serverprovider.http.header.HeaderValuesImpl;
import io.opensphere.server.serverprovider.http.requestors.RequestorProviderImpl;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.ServerToolboxUtils;

/**
 * Creates an HttpServer either based on a url or based on host name and port
 * and configures all necessary security and proxy settings for the server.
 *
 */
public class HttpServerFactory implements ServerFactory
{
    /**
     * Creates an HttpServer connection based on the specified protocol and
     * host.
     *
     * @param securityProvider Provides different security configurations for
     *            the given server.
     * @param protocol The protocol to use (e.g. http or https).
     * @param host The server name or ip.
     * @param port The port to create the connection for.
     * @param toolbox The system toolbox.
     * @param serverKey The server key to pass to the security components.
     * @return The newly create HttpServer.
     */
    @Override
    public HttpServer createServer(SecurityComponentsProvider securityProvider, String protocol, String host, int port,
            String serverKey, Toolbox toolbox)
    {
        Supplier<? extends Component> parentComponent = toolbox.getUIRegistry().getMainFrameProvider();
        SecurityManager securityManager = toolbox.getSecurityManager();
        PreferencesRegistry prefsRegistry = toolbox.getPreferencesRegistry();
        NetworkConfigurationManager networkConfigurationManager = toolbox.getSystemToolbox().getNetworkConfigurationManager();
        ServerToolbox serverTools = ServerToolboxUtils.getServerToolbox(toolbox);

        HttpClient httpClient = new DefaultHttpClient();

        ConfigurerParameters parameters = new ConfigurerParameters();
        parameters.setClient(httpClient);
        parameters.setHost(host);
        parameters.setParentComponent(parentComponent);
        parameters.setPort(port);
        parameters.setPrefsRegistry(prefsRegistry);
        parameters.setProvider(securityProvider);
        parameters.setSecurityManager(securityManager);
        parameters.setServerKey(serverKey);
        parameters.setNetworkConfigurationManager(networkConfigurationManager);

        new ProxyConfigurer().configure(parameters);
        new BasicAuthConfigurer().configure(parameters);
        new CertificateConfigurer().configure(parameters);
        new TimeoutsConfigurer().configure(parameters, prefsRegistry, serverTools);
        new ConnectionPoolConfigurer().configure(httpClient);

        RequestorProviderImpl provider = new RequestorProviderImpl(httpClient,
                new HeaderValuesImpl(toolbox.getGeometryRegistry().getRenderingCapabilities().getRendererIdentifier()),
                toolbox.getEventManager());
        HttpServerImpl server = new HttpServerImpl(host, protocol, provider);

        return server;
    }
}
