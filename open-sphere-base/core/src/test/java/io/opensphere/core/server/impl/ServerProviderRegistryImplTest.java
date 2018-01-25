package io.opensphere.core.server.impl;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ServerProvider;

/**
 * Tests the ServerProviderRegistryImpl class.
 */
public class ServerProviderRegistryImplTest
{
    /**
     * Tests the ServerProviderRegistryImpl class.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        ServerProvider<HttpServer> serverProvider = support.createMock(ServerProvider.class);

        support.replayAll();

        ServerProviderRegistryImpl registry = new ServerProviderRegistryImpl();
        registry.registerProvider(HttpServer.class, serverProvider);

        ServerProvider<HttpServer> actualProvider = registry.getProvider(HttpServer.class);

        assertEquals(serverProvider, actualProvider);

        support.verifyAll();
    }
}
