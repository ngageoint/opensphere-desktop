package io.opensphere.wps.streaming.impl;

import java.net.URL;

import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ServerChangedListener;
import io.opensphere.core.server.ServerCreator;
import io.opensphere.core.server.ServerProvider;

/**
 * A mock http server provider used for testing.
 */
public class MockHttpProvider implements ServerProvider<HttpServer>, ServerCreator<HttpServer>
{
    /**
     * The easy mocked creator calls.
     */
    private final ServerCreator<HttpServer> myCreator;

    /**
     * The easy mocked provider calls.
     */
    private final ServerProvider<HttpServer> myProvider;

    /**
     * Constructs a new mock provider.
     *
     * @param provider The easy mocked provider.
     * @param creator The easy mocked creator.
     */
    public MockHttpProvider(ServerProvider<HttpServer> provider, ServerCreator<HttpServer> creator)
    {
        myProvider = provider;
        myCreator = creator;
    }

    @Override
    public void addServerChangedListener(ServerChangedListener<HttpServer> listener)
    {
        myProvider.addServerChangedListener(listener);
    }

    @Override
    public void clearServers()
    {
    }

    @Override
    public HttpServer createServer(URL url)
    {
        return myCreator.createServer(url);
    }

    @Override
    public HttpServer getServer(String host, String protocol, int port)
    {
        return myProvider.getServer(host, protocol, port);
    }

    @Override
    public HttpServer getServer(URL url)
    {
        return myProvider.getServer(url);
    }

    @Override
    public void removeServerChangedListener(ServerChangedListener<HttpServer> listener)
    {
        myProvider.removeServerChangedListener(listener);
    }
}
