package io.opensphere.wps.streaming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.MalformedURLException;
import java.net.URL;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.server.StreamingServer;
import net.opengis.ows._110.CodeType;
import net.opengis.wps._100.ProcessBriefType;
import net.opengis.wps._100.ProcessOfferings;
import net.opengis.wps._100.WPSCapabilitiesType;

/**
 * Tests the {@link WPSStreamingServerHandler} class.
 */
public class WPSStreamingServerHandlerTest
{
    /**
     * The expected url.
     */
    private static final String ourUrl = "https://somehost/wps";

    /**
     * Tests activating a streaming server and closing it.
     *
     * @throws MalformedURLException Bad url.
     */
    @Test
    public void test() throws MalformedURLException
    {
        EasyMockSupport support = new EasyMockSupport();

        WPSCapabilitiesType capabilities = createStreamingCapabilities();
        MockServerProvider provider = new MockServerProvider();

        ServerProviderRegistry registry = createProviderRegistry(support, provider);

        ComponentsFactory factory = createFactory(support);

        support.replayAll();

        WPSStreamingServerHandler handler = new WPSStreamingServerHandler(capabilities, registry, factory);

        WPSStreamingServer server = (WPSStreamingServer)provider.getAddedServer();
        assertEquals(ourUrl, server.getURL().toString());

        handler.close();

        assertNull(provider.getAddedServer());

        support.verifyAll();
    }

    /**
     * Tests activating a non streaming server and closing it.
     */
    @Test
    public void testNonStreamingServer()
    {
        EasyMockSupport support = new EasyMockSupport();

        WPSCapabilitiesType capabilities = createNonStreamingCapabilities();
        MockServerProvider provider = new MockServerProvider();

        ServerProviderRegistry registry = support.createMock(ServerProviderRegistry.class);

        ComponentsFactory factory = support.createNiceMock(ComponentsFactory.class);

        support.replayAll();

        WPSStreamingServerHandler handler = new WPSStreamingServerHandler(capabilities, registry, factory);

        assertNull(provider.getAddedServer());

        handler.close();

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link ComponentsFactory}.
     *
     * @param support Used to create the mock.
     * @return The {@link ComponentsFactory}.
     * @throws MalformedURLException Bad URL.
     */
    private ComponentsFactory createFactory(EasyMockSupport support) throws MalformedURLException
    {
        ComponentsFactory factory = support.createNiceMock(ComponentsFactory.class);

        EasyMock.expect(factory.getURL()).andReturn(new URL(ourUrl));

        return factory;
    }

    /**
     * Creates wps capabilities without streaming capabilities.
     *
     * @return The non streaming wps capabilities.
     */
    private WPSCapabilitiesType createNonStreamingCapabilities()
    {
        WPSCapabilitiesType capabilities = createStreamingCapabilities();

        capabilities.getProcessOfferings().getProcess().remove(0);

        return capabilities;
    }

    /**
     * Creates an easy mocked {@link ServerProviderRegistry}.
     *
     * @param support Used to create the mock.
     * @param provider The provider the mock should return.
     * @return The {@link ServerProviderRegistry}.
     */
    private ServerProviderRegistry createProviderRegistry(EasyMockSupport support, MockServerProvider provider)
    {
        ServerProviderRegistry registry = support.createMock(ServerProviderRegistry.class);
        EasyMock.expect(registry.getProvider(EasyMock.eq(StreamingServer.class))).andReturn(provider).atLeastOnce();

        return registry;
    }

    /**
     * Creates wps capabilities that has streaming capabilities.
     *
     * @return The stream wps capabilities.
     */
    private WPSCapabilitiesType createStreamingCapabilities()
    {
        WPSCapabilitiesType capabilities = new WPSCapabilitiesType();

        ProcessOfferings offerings = new ProcessOfferings();

        ProcessBriefType brief;
        CodeType codeType;

        brief = new ProcessBriefType();
        codeType = new CodeType();
        codeType.setValue("SubscribeToNRTLayerProcess");
        brief.setIdentifier(codeType);

        offerings.getProcess().add(brief);

        brief = new ProcessBriefType();
        codeType = new CodeType();
        codeType.setValue("UnsubscribeToNRTLayerProcess");
        brief.setIdentifier(codeType);

        offerings.getProcess().add(brief);

        capabilities.setProcessOfferings(offerings);

        return capabilities;
    }
}
