package io.opensphere.server.serverprovider.http.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.function.Supplier;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.bitsys.common.http.auth.AuthenticationScope;
import com.bitsys.common.http.auth.Credentials;
import com.bitsys.common.http.auth.CredentialsProvider;
import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.client.HttpClientOptions;
import com.bitsys.common.http.client.ProxyConfig;
import com.bitsys.common.http.proxy.ProxyHostConfig;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.SecurityManager;
import io.opensphere.core.SystemToolbox;
import io.opensphere.core.Toolbox;
import io.opensphere.core.common.connection.ServerConfiguration;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.RenderingCapabilities;
import io.opensphere.core.net.config.ConfigurationType;
import io.opensphere.core.net.config.ProxyConfigurations;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.server.serverprovider.ProxySelectorImpl;
import io.opensphere.server.serverprovider.SecurityComponentsProvider;
import io.opensphere.server.serverprovider.http.HttpServerImpl;
import io.opensphere.server.serverprovider.http.requestors.BaseRequestor;
import io.opensphere.server.serverprovider.http.requestors.RequestorProvider;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.toolbox.ServerListManager;
import io.opensphere.server.toolbox.ServerToolbox;

/**
 * Tests the HttpServerFactory class.
 *
 */
public class HttpServerFactoryTest
{
    /**
     * The expected connect timeout from the host.
     */
    private static final int ourConnectTimeout = 400;

    /**
     * The host.
     */
    private static final String ourHost = "host";

    /**
     * The expected read timeout from the host.
     */
    private static final int ourReadTimeout = 360;

    /**
     * The proxy host.
     */
    private static final String ourProxyHost = "proxyhost";

    /**
     * The proxy port.
     */
    private static final int ourProxyPort = 10;

    /**
     * The expected protocol.
     */
    private static final String ourProtocol = "http";

    /**
     * The expected port.
     */
    private static final int ourPort = 12;

    /**
     * The expected server key.
     */
    private static final String ourServerKey = "serverKey";

    /**
     * Tests creating an HttpServer.
     *
     * @throws IOException Bad IO.
     * @throws GeneralSecurityException Bad security.
     */
    @Test
    public void testCreateServer() throws GeneralSecurityException, IOException
    {
        final EasyMockSupport support = new EasyMockSupport();

        final PreferencesRegistry prefsRegistry = createPrefsRegistry(support);
        @SuppressWarnings("unchecked")
        final Supplier<? extends Component> parentProvider = support.createMock(Supplier.class);
        final SecurityManager securityManager = createSecurityManager(support);
        final Credentials hostCreds = support.createMock(Credentials.class);
        final Credentials proxyCreds = support.createMock(Credentials.class);
        final Toolbox toolbox = createToolbox(support, prefsRegistry, parentProvider, securityManager);

        final SecurityComponentsProvider provider = createSecurityProvider(support, parentProvider, securityManager,
                prefsRegistry, hostCreds, proxyCreds);

        support.replayAll();

        final HttpServerFactory factory = new HttpServerFactory();
        final HttpServerImpl server = (HttpServerImpl)factory.createServer(provider, ourProtocol, ourHost, ourPort, ourServerKey,
                toolbox);

        assertEquals(ourHost, server.getHost());
        assertEquals(ourProtocol, server.getProtocol());

        final RequestorProvider requestorProvider = server.getRequestProvider();
        final BaseRequestor baseRequestor = (BaseRequestor)requestorProvider.getFilePoster();

        final HttpClient client = baseRequestor.getClient();

        assertEquals(client, ((BaseRequestor)requestorProvider.getPostRequestor()).getClient());
        assertEquals(client, ((BaseRequestor)requestorProvider.getRequestor()).getClient());

        final HttpClientOptions actualOptions = client.getOptions();

        assertEquals(ourConnectTimeout, actualOptions.getConnectTimeout());
        assertEquals(ourReadTimeout, actualOptions.getReadTimeout());

        final CredentialsProvider credsProvider = actualOptions.getCredentialsProvider();
        final Credentials credentials = credsProvider.getCredentials(new AuthenticationScope(ourHost, ourPort));
        assertEquals(hostCreds, credentials);

        assertEquals(ConnectionPoolConfigurer.ourMaxConnections, actualOptions.getMaxConnections());
        assertEquals(ConnectionPoolConfigurer.ourConnectionsPerRoute, actualOptions.getMaxConnectionsPerRoute());

        final ProxyConfig proxyConfig = actualOptions.getProxyConfig();
        final List<ProxyHostConfig> servers = proxyConfig.getProxyResolver()
                .getProxyServer(new URL(ourProtocol, ourHost, ourPort, ""));
        assertEquals(1, servers.size());
        assertEquals(ourProxyHost, servers.get(0).getHost());
        assertEquals(ourProxyPort, servers.get(0).getPort());
        assertEquals(proxyCreds, proxyConfig.getCredentials());

        assertTrue(client.getOptions().isAllowCircularRedirects());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked security provider.
     *
     * @param support The easy mock support.
     * @param parentProvider The parent provider to return.
     * @param securityManager The security manager to return.
     * @param prefsRegistry The preferences registry to return.
     * @param creds The credentials for the provider to return.
     * @param proxyCreds The credentials for the provider to return for the
     *            proxy.
     * @return The easy mocked SecurityComponentsProvider.
     */
    private SecurityComponentsProvider createSecurityProvider(final EasyMockSupport support,
            final Supplier<? extends Component> parentProvider, final SecurityManager securityManager,
            final PreferencesRegistry prefsRegistry, final Credentials creds, final Credentials proxyCreds)
    {
        final SecurityComponentsProvider provider = support.createMock(SecurityComponentsProvider.class);

        provider.getUserCredentials(EasyMock.cmpEq("proxy"), EasyMock.cmpEq("proxy"), EasyMock.eq(parentProvider),
                EasyMock.eq(securityManager));
        EasyMock.expectLastCall().andReturn(proxyCreds);

        provider.getKeyManager(EasyMock.cmpEq(ourHost), EasyMock.cmpEq(ourServerKey), EasyMock.eq(parentProvider),
                EasyMock.eq(prefsRegistry), EasyMock.eq(securityManager));
        EasyMock.expectLastCall().andReturn(null);

        provider.getTrustManager(EasyMock.cmpEq(ourHost), EasyMock.cmpEq(ourServerKey), EasyMock.eq(parentProvider),
                EasyMock.eq(securityManager));
        EasyMock.expectLastCall().andReturn(null);

        provider.getUserCredentials(EasyMock.cmpEq(ourHost), EasyMock.cmpEq(ourServerKey), EasyMock.eq(parentProvider),
                EasyMock.eq(securityManager));
        EasyMock.expectLastCall().andReturn(creds);

        return provider;
    }

    /**
     * Creates an easy mocked plugin toolbox registry.
     *
     * @param support The easy mock support object.
     * @return The PluginToolboxRegistry.
     */
    private PluginToolboxRegistry createPluginToolboxRegistry(final EasyMockSupport support)
    {
        final ServerToolbox serverToolbox = createServerToolbox(support);

        final PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        toolboxRegistry.getPluginToolbox(EasyMock.eq(ServerToolbox.class));
        EasyMock.expectLastCall().andReturn(serverToolbox);

        return toolboxRegistry;
    }

    /**
     * Creates an easy mocked SecurityManager.
     *
     * @param support The easy mock support object.
     * @return The security manager.
     */
    private SecurityManager createSecurityManager(final EasyMockSupport support)
    {
        final SecurityManager securityManager = support.createMock(SecurityManager.class);

        return securityManager;
    }

    /**
     * Creates an easy mocked system toolbox.
     *
     * @param support The easy mock support object.
     * @return The system toolbox.
     */
    private SystemToolbox createSystemToolbox(final EasyMockSupport support)
    {
        final NetworkConfigurationManager networkManager = createNetworkConfigurationManager(support);

        final SystemToolbox systemToolbox = support.createMock(SystemToolbox.class);
        systemToolbox.getNetworkConfigurationManager();
        EasyMock.expectLastCall().andReturn(networkManager);

        return systemToolbox;
    }

    /**
     * Creates an easy mocked preferences registry.
     *
     * @param support The easy mock support object.
     * @return The preferences registry.
     */
    private PreferencesRegistry createPrefsRegistry(final EasyMockSupport support)
    {
        final PreferencesRegistry prefsRegistry = support.createMock(PreferencesRegistry.class);

        return prefsRegistry;
    }

    /**
     * Creates an easy mocked UI registry.
     *
     * @param support The easy mock support object.
     * @param supplier The supplier to return.
     * @return The UI registry.
     */
    private UIRegistry createUIRegistry(final EasyMockSupport support, final Supplier<? extends Component> supplier)
    {
        final UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        uiRegistry.getMainFrameProvider();
        EasyMock.expectLastCall().andReturn(supplier);

        return uiRegistry;
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support The easy mock support object.
     * @param prefsRegistry The preferences registry to return.
     * @param parentProvider The parent provider to return.
     * @param securityManager The security manager to return.
     * @return The toolbox.
     */
    private Toolbox createToolbox(final EasyMockSupport support, final PreferencesRegistry prefsRegistry,
            final Supplier<? extends Component> parentProvider, final SecurityManager securityManager)
    {
        final UIRegistry uiRegistry = createUIRegistry(support, parentProvider);
        final SystemToolbox systemToolbox = createSystemToolbox(support);
        final PluginToolboxRegistry pluginToolbox = createPluginToolboxRegistry(support);

        final Toolbox toolbox = support.createMock(Toolbox.class);
        toolbox.getUIRegistry();
        EasyMock.expectLastCall().andReturn(uiRegistry);

        toolbox.getSecurityManager();
        EasyMock.expectLastCall().andReturn(securityManager);

        toolbox.getPreferencesRegistry();
        EasyMock.expectLastCall().andReturn(prefsRegistry);

        toolbox.getSystemToolbox();
        EasyMock.expectLastCall().andReturn(systemToolbox);

        toolbox.getPluginToolboxRegistry();
        EasyMock.expectLastCall().andReturn(pluginToolbox).anyTimes();

        final RenderingCapabilities rendering = support.createMock(RenderingCapabilities.class);
        rendering.getRendererIdentifier();
        EasyMock.expectLastCall().andReturn("videocard");

        final GeometryRegistry geometryRegistry = support.createMock(GeometryRegistry.class);
        geometryRegistry.getRenderingCapabilities();
        EasyMock.expectLastCall().andReturn(rendering);

        toolbox.getGeometryRegistry();
        EasyMock.expectLastCall().andReturn(geometryRegistry);

        final EventManager eventManager = support.createMock(EventManager.class);
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager);

        return toolbox;
    }

    /**
     * Creates an easy mocked ServerConnectionParams.
     *
     * @param support Used to create the mock.
     * @param host The host the params should return.
     * @param readTimeout The read timeout the params should return.
     * @param connectTimeout The connect timeout the params should return.
     * @return The ServerConnectionParams.
     */
    private ServerConnectionParams createServer(final EasyMockSupport support, final String host, final int readTimeout,
            final int connectTimeout)
    {
        final ServerConfiguration randomConfig = new ServerConfiguration();
        randomConfig.setHost(host);
        randomConfig.setReadTimeout(readTimeout);
        randomConfig.setConnectTimeout(connectTimeout);

        final ServerConnectionParams randomServer = support.createMock(ServerConnectionParams.class);
        randomServer.getServerConfiguration();
        EasyMock.expectLastCall().andReturn(randomConfig);

        return randomServer;
    }

    /**
     * Creates an easy mocked ServerToolbox.
     *
     * @param support Used to create the mock.
     * @return The ServerToolbox.
     */
    private ServerToolbox createServerToolbox(final EasyMockSupport support)
    {
        final List<ServerConnectionParams> servers = New.list();

        servers.add(createServer(support, "randomHost", 100, 100));

        final ServerConnectionParams server = createServer(support, ourHost, ourReadTimeout * 1000, ourConnectTimeout * 1000);
        servers.add(server);

        final ServerListManager serverManager = support.createMock(ServerListManager.class);
        serverManager.getActiveServers();
        EasyMock.expectLastCall().andReturn(servers);

        final ServerToolbox serverToolbox = support.createMock(ServerToolbox.class);
        serverToolbox.getServerLayerListManager();
        EasyMock.expectLastCall().andReturn(serverManager);

        return serverToolbox;
    }

    /**
     * Creates an easy mocked network configuration manager.
     *
     * @param support The support.
     * @return The easy mocked network configuration manager.
     */
    private NetworkConfigurationManager createNetworkConfigurationManager(final EasyMockSupport support)
    {
        final NetworkConfigurationManager manager = support.createMock(NetworkConfigurationManager.class);

        final ProxyConfigurations configurations = new ProxyConfigurations();
        configurations.getManualProxyConfiguration().setHost(ourProxyHost);
        configurations.getManualProxyConfiguration().setPort(ourProxyPort);

        EasyMock.expect(manager.getSelectedProxyType()).andReturn(ConfigurationType.MANUAL).anyTimes();
        EasyMock.expect(manager.getManualConfiguration()).andReturn(configurations.getManualProxyConfiguration());

        EasyMock.expect(Boolean.valueOf(manager.isExcludedFromProxy(ourHost))).andReturn(Boolean.FALSE);
        configurations.setSelectedConfigurationType(ConfigurationType.MANUAL);

        ProxySelector.setDefault(new ProxySelectorImpl(manager));

        return manager;
    }
}
