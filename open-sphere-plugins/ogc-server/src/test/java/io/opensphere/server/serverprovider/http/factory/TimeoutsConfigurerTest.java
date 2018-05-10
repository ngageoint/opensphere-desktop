package io.opensphere.server.serverprovider.http.factory;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.client.HttpClientOptions;

import io.opensphere.core.common.connection.ServerConfiguration;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.toolbox.ServerListManager;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.util.ServerConstants;

/**
 * Tests the TimeoutsConfigurer.
 *
 */
@SuppressWarnings("boxing")
public class TimeoutsConfigurerTest
{
    /**
     * The expected connect timeout from the host.
     */
    private static final int ourConnectTimeout = 400;

    /**
     * The expected default connect timeout.
     */
    private static final int ourDefaultConnect = 130;

    /**
     * The expected default read timeout.
     */
    private static final int ourDefaultRead = 120;

    /**
     * The host.
     */
    private static final String ourHost = "host";

    /**
     * The expected read timeout from the host.
     */
    private static final int ourReadTimeout = 360;

    /**
     * Tests configuring timeouts for a server who is active in the system.
     */
    @Test
    public void testConfigureActiveServer()
    {
        EasyMockSupport support = new EasyMockSupport();

        HttpClientOptions options = new HttpClientOptions();
        HttpClient client = createClient(support, options);
        ServerToolbox serverToolbox = createServerToolbox(support, true);
        PreferencesRegistry registry = EasyMock.createMock(PreferencesRegistry.class);

        support.replayAll();

        ConfigurerParameters parameters = new ConfigurerParameters();
        parameters.setClient(client);
        parameters.setHost(ourHost);

        TimeoutsConfigurer configurer = new TimeoutsConfigurer();
        configurer.configure(parameters, registry, serverToolbox);

        assertEquals(ourConnectTimeout, options.getConnectTimeout());
        assertEquals(ourReadTimeout, options.getReadTimeout());

        support.verifyAll();
    }

    /**
     * Tests configuring timeouts for a server who is not active in the system.
     */
    @Test
    public void testConfigureNoActiveServer()
    {
        EasyMockSupport support = new EasyMockSupport();

        HttpClientOptions options = new HttpClientOptions();
        HttpClient client = createClient(support, options);
        ServerToolbox serverToolbox = createServerToolbox(support, false);
        PreferencesRegistry registry = createPrefsRegistry(support);

        support.replayAll();

        ConfigurerParameters parameters = new ConfigurerParameters();
        parameters.setClient(client);
        parameters.setHost(ourHost);

        TimeoutsConfigurer configurer = new TimeoutsConfigurer();
        configurer.configure(parameters, registry, serverToolbox);

        assertEquals(ourDefaultConnect, options.getConnectTimeout());
        assertEquals(ourDefaultRead, options.getReadTimeout());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked HttpClient.
     *
     * @param support Used to creat mock.
     * @param options The options the client should return.
     * @return The HttpClient.
     */
    private HttpClient createClient(EasyMockSupport support, HttpClientOptions options)
    {
        HttpClient client = support.createMock(HttpClient.class);
        client.getOptions();
        EasyMock.expectLastCall().andReturn(options);
        EasyMock.expectLastCall().atLeastOnce();

        return client;
    }

    /**
     * Creates and easy mocked PreferencesRegistry.
     *
     * @param support Used to create the mock.
     * @return The PreferencesRegistry.
     */
    private PreferencesRegistry createPrefsRegistry(EasyMockSupport support)
    {
        Preferences prefs = support.createMock(Preferences.class);
        prefs.getInt(EasyMock.eq(ServerConstants.DEFAULT_SERVER_CONNECT_TIMEOUT_PREFERENCE_KEY),
                EasyMock.eq(ServerConstants.DEFAULT_SERVER_CONNECT_TIMEOUT));
        EasyMock.expectLastCall().andReturn(ourDefaultConnect * 1000);

        prefs.getInt(EasyMock.eq(ServerConstants.DEFAULT_SERVER_READ_TIMEOUT_PREFERENCE_KEY),
                EasyMock.eq(ServerConstants.DEFAULT_SERVER_READ_TIMEOUT));
        EasyMock.expectLastCall().andReturn(ourDefaultRead * 1000);

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        registry.getPreferences(EasyMock.eq(ServerConstants.class));
        EasyMock.expectLastCall().andReturn(prefs);
        EasyMock.expectLastCall().atLeastOnce();

        return registry;
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
    private ServerConnectionParams createServer(EasyMockSupport support, String host, int readTimeout, int connectTimeout)
    {
        ServerConfiguration randomConfig = new ServerConfiguration();
        randomConfig.setHost(host);
        randomConfig.setReadTimeout(readTimeout);
        randomConfig.setConnectTimeout(connectTimeout);

        ServerConnectionParams randomServer = support.createMock(ServerConnectionParams.class);
        randomServer.getServerConfiguration();
        EasyMock.expectLastCall().andReturn(randomConfig);

        return randomServer;
    }

    /**
     * Creates an easy mocked ServerToolbox.
     *
     * @param support Used to create the mock.
     * @param includeHost True if ourHost should be included in list of servers,
     *            false if not.
     * @return The ServerToolbox.
     */
    private ServerToolbox createServerToolbox(EasyMockSupport support, boolean includeHost)
    {
        List<ServerConnectionParams> servers = New.list();

        servers.add(createServer(support, "randomHost", 100, 100));

        if (includeHost)
        {
            ServerConnectionParams server = createServer(support, ourHost, ourReadTimeout * 1000, ourConnectTimeout * 1000);
            servers.add(server);

            servers.add(support.createMock(ServerConnectionParams.class));
        }

        ServerListManager serverManager = support.createMock(ServerListManager.class);
        serverManager.getActiveServers();
        EasyMock.expectLastCall().andReturn(servers);

        ServerToolbox serverToolbox = support.createMock(ServerToolbox.class);
        serverToolbox.getServerLayerListManager();
        EasyMock.expectLastCall().andReturn(serverManager);

        return serverToolbox;
    }
}
