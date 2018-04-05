package io.opensphere.wms.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.test.core.matchers.EasyMockHelper;
import io.opensphere.wms.capabilities.WMSServerCapabilities;

/**
 * Unit test for {@link WMSEnvoyHelper}.
 */
public class WMSEnvoyHelperTest
{
    /**
     * The test server url.
     */
    private static final String ourServerUrl = "http://somehost/wms";

    /**
     * Tests getting capabilities from a server we have issues connecting to.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     * @throws InterruptedException Don't interrupt.
     * @throws GeneralSecurityException Bad security.
     */
    @Test
    public void testRequestCapabilitiesFromServer()
        throws IOException, URISyntaxException, GeneralSecurityException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        ServerConnectionParams params = createParams(support);
        Toolbox toolbox = createToolbox(support);

        support.replayAll();

        WMSServerCapabilities capabilities = WMSEnvoyHelper.requestCapabilitiesFromServer(params, toolbox);
        assertNotNull(capabilities);
        assertEquals("1.1.1", capabilities.getVersion());

        support.verifyAll();
    }

    /**
     * Creates a mocked {@link ServerConnectionParams}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link ServerConnectionParams}.
     */
    private ServerConnectionParams createParams(EasyMockSupport support)
    {
        ServerConnectionParams params = support.createMock(ServerConnectionParams.class);

        EasyMock.expect(params.getWmsUrl()).andReturn(ourServerUrl);

        return params;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link Toolbox}.
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad URI.
     */
    private Toolbox createToolbox(EasyMockSupport support) throws IOException, URISyntaxException
    {
        URL expected = new URL(ourServerUrl + "?service=WMS&REQUEST=GetCapabilities&version=1.3.0");

        HttpServer server = support.createMock(HttpServer.class);
        EasyMock.expect(server.sendGet(EasyMockHelper.eq(expected), EasyMock.isA(ResponseValues.class))).andAnswer(() ->
        {
            ResponseValues response = (ResponseValues)EasyMock.getCurrentArguments()[1];
            response.setResponseCode(HttpURLConnection.HTTP_OK);
            return new CancellableInputStream(getClass().getResourceAsStream("/problemWMSCapabilities.xml"), null);
        });

        @SuppressWarnings("unchecked")
        ServerProvider<HttpServer> provider = support.createMock(ServerProvider.class);
        EasyMock.expect(provider.getServer(EasyMockHelper.eq(expected))).andReturn(server);

        ServerProviderRegistry providerRegistry = support.createMock(ServerProviderRegistry.class);
        EasyMock.expect(providerRegistry.getProvider(HttpServer.class)).andReturn(provider);

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getServerProviderRegistry()).andReturn(providerRegistry);

        return toolbox;
    }
}
