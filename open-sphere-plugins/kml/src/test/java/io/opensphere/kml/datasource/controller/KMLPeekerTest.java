package io.opensphere.kml.datasource.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.matchers.EasyMockHelper;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.ByteString;

/**
 * Tests the kml peeker class.
 *
 */
public class KMLPeekerTest
{
    /**
     * The test url.
     */
    private static final String ourUrl = "http://somehost/file";

    /**
     * Tests the is kml function expecting true.
     *
     * @throws IOException Bad IO.
     * @throws GeneralSecurityException Bad security.
     * @throws URISyntaxException Bad URI.
     */
    @Test
    public void testIsKmlDocument() throws GeneralSecurityException, IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        String xml = "<Document xmlns=\"http://earth.google.com/kml/2.1\"></Document>";

        Toolbox toolbox = createToolbox(support, xml);

        support.replayAll();

        KMLPeeker peeker = new KMLPeeker(toolbox);
        assertTrue(peeker.isKml(new URL(ourUrl)));

        support.verifyAll();
    }

    /**
     * Tests the is kml function expecting false.
     *
     * @throws IOException Bad io.
     * @throws GeneralSecurityException Bad security.
     * @throws MalformedURLException Bad url.
     * @throws URISyntaxException Bad URI.
     */
    @Test
    public void testIsKmlFalse() throws MalformedURLException, GeneralSecurityException, IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        String xml = "someplaintext";

        Toolbox toolbox = createToolbox(support, xml);

        support.replayAll();

        KMLPeeker peeker = new KMLPeeker(toolbox);
        assertFalse(peeker.isKml(new URL(ourUrl)));

        support.verifyAll();
    }

    /**
     * Tests the is kml function expecting true.
     *
     * @throws IOException Bad IO.
     * @throws GeneralSecurityException Bad security.
     * @throws URISyntaxException Bad URI.
     */
    @Test
    public void testIsKmlTrue() throws GeneralSecurityException, IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        String xml = "<kml></kml>";

        Toolbox toolbox = createToolbox(support, xml);

        support.replayAll();

        KMLPeeker peeker = new KMLPeeker(toolbox);
        assertTrue(peeker.isKml(new URL(ourUrl)));

        support.verifyAll();
    }

    /**
     * Creates an easy mocked url connection.
     *
     * @param support Used to create the mock.
     * @param xml The xml the url connection should return.
     * @return The easy mocked connection.
     * @throws MalformedURLException Bad url.
     * @throws GeneralSecurityException Bad security.
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad URI.
     */
    private Toolbox createToolbox(EasyMockSupport support, final String xml)
        throws MalformedURLException, GeneralSecurityException, IOException, URISyntaxException
    {
        HttpServer server = support.createMock(HttpServer.class);
        EasyMock.expect(server.sendGet(EasyMockHelper.eq(new URL(ourUrl)), EasyMock.isA(ResponseValues.class)))
                .andAnswer(new IAnswer<CancellableInputStream>()
                {
                    @Override
                    public CancellableInputStream answer()
                    {
                        ResponseValues values = (ResponseValues)EasyMock.getCurrentArguments()[1];
                        values.setResponseCode(HttpURLConnection.HTTP_OK);

                        return new CancellableInputStream(new ByteArrayInputStream(ByteString.getBytes(xml)), null);
                    }
                }).atLeastOnce();

        @SuppressWarnings("unchecked")
        ServerProvider<HttpServer> provider = support.createMock(ServerProvider.class);
        provider.getServer(EasyMockHelper.eq(new URL(ourUrl)));
        EasyMock.expectLastCall().andReturn(server);
        EasyMock.expectLastCall().atLeastOnce();

        ServerProviderRegistry registry = support.createMock(ServerProviderRegistry.class);
        registry.getProvider(EasyMock.eq(HttpServer.class));
        EasyMock.expectLastCall().andReturn(provider);
        EasyMock.expectLastCall().atLeastOnce();

        Toolbox toolbox = support.createMock(Toolbox.class);
        toolbox.getServerProviderRegistry();
        EasyMock.expectLastCall().andReturn(registry);
        EasyMock.expectLastCall().atLeastOnce();

        return toolbox;
    }
}
