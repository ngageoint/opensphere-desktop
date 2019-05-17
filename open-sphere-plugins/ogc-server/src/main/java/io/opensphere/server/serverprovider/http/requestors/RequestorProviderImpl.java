package io.opensphere.server.serverprovider.http.requestors;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;

import com.bitsys.common.http.client.DefaultHttpClient;
import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.client.HttpClientOptions;
import com.bitsys.common.http.client.ProxyConfig;
import com.bitsys.common.http.proxy.ProxyHostConfig;
import com.bitsys.common.http.proxy.ProxyResolver;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.server.serverprovider.http.header.HeaderValues;

/**
 * Provides the different requestors available to use to communicate with an
 * Http server.
 *
 */
public class RequestorProviderImpl implements RequestorProvider
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(RequestorProviderImpl.class);

    /**
     * Used by the requestors to communicate with the server.
     */
    private HttpClient myClient;

    /** The deleter. It deletes stuff. */
    private final DeleteRequestorImpl myDeleter;

    /**
     * The requestor that posts a file to the server.
     */
    private FilePostRequestorImpl myFilePoster;

    /**
     * The requestor that requests gets from the server.
     */
    private GetRequestorImpl myGetter;

    /** The requestor that makes HEAD requests to the server. */
    private final HeadRequestorImpl myHeadRequestor;

    /**
     * Contains the header values.
     */
    private final HeaderValues myHeaderValues;

    /**
     * The requestor that posts data to the server.
     */
    private PostRequestorImpl myPoster;

    /** The manager through which events are sent. */
    private final EventManager myEventManager;

    /** The network configuration manager. */
    private final NetworkConfigurationManager myNetworkConfigurationManager;

    /**
     * Constructs a new requestor provider.
     *
     * @param client The client the requestors should use.
     * @param headerValues Contains the header values.
     * @param eventManager The manager through which events are sent.
     * @param networkConfigurationManager The network configuration manager.
     */
    public RequestorProviderImpl(HttpClient client, HeaderValues headerValues, EventManager eventManager, NetworkConfigurationManager networkConfigurationManager)
    {
        myClient = client;
        myHeaderValues = headerValues;
        myEventManager = eventManager;
        myNetworkConfigurationManager = networkConfigurationManager;
        myFilePoster = new FilePostRequestorImpl(client, headerValues, eventManager, networkConfigurationManager);
        myPoster = new PostRequestorImpl(client, headerValues, eventManager, networkConfigurationManager);
        myGetter = new GetRequestorImpl(client, headerValues, eventManager, networkConfigurationManager);
        myHeadRequestor = new HeadRequestorImpl(client, headerValues, eventManager, networkConfigurationManager);
        myDeleter = new DeleteRequestorImpl(client, headerValues, eventManager, networkConfigurationManager);
    }

    @Override
    public DeleteRequestor getDeleteRequestor()
    {
        return myDeleter;
    }

    @Override
    public FilePostRequestor getFilePoster()
    {
        return myFilePoster;
    }

    @Override
    public PostRequestor getPostRequestor()
    {
        return myPoster;
    }

    @Override
    public GetRequestor getRequestor()
    {
        return myGetter;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.serverprovider.http.requestors.RequestorProvider#getHeadRequestor()
     */
    @Override
    public HeadRequestor getHeadRequestor()
    {
        return myHeadRequestor;
    }

    @Override
    public Pair<String, Integer> resolveProxy(URL destination)
    {
        Pair<String, Integer> hostAndPort = null;

        ProxyHostConfig proxyHost = resolveProxyConfig(destination);
        if (proxyHost != null)
        {
            hostAndPort = new Pair<>(proxyHost.getHost(), Integer.valueOf(proxyHost.getPort()));
        }

        return hostAndPort;
    }

    @Override
    public ProxyHostConfig resolveProxyConfig(URL destination)
    {
        ProxyHostConfig proxyHost = null;

        ProxyConfig proxyConfig = myClient.getOptions().getProxyConfig();
        if (proxyConfig != null)
        {
            ProxyResolver resolver = proxyConfig.getProxyResolver();
            if (resolver != null)
            {
                try
                {
                    List<ProxyHostConfig> proxyHosts = resolver.getProxyServer(destination);
                    if (proxyHosts != null && !proxyHosts.isEmpty())
                    {
                        proxyHost = proxyHosts.get(0);
                    }
                }
                catch (IOException e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        return proxyHost;
    }

    @Override
    public void setBufferSize(int bufferSize)
    {
        createNewClient();

        myClient.getOptions().setSocketBufferSize(bufferSize);

        createNewRequestors();
    }

    @Override
    public void setTimeouts(int readTimeout, int connectTimeout)
    {
        createNewClient();

        myClient.getOptions().setConnectTimeout(connectTimeout / 1000);
        myClient.getOptions().setReadTimeout(readTimeout / 1000);

        createNewRequestors();
    }

    @Override
    public HttpClient getClient()
    {
        return myClient;
    }

    /**
     * Creates a new {@link HttpClient} with the same options as myClient.
     */
    private void createNewClient()
    {
        HttpClientOptions options = myClient.getOptions();

        myClient = new DefaultHttpClient();
        myClient.getOptions().setConnectTimeout(options.getConnectTimeout());
        myClient.getOptions().setCredentialsProvider(options.getCredentialsProvider());
        myClient.getOptions().setMaxConnections(options.getMaxConnections());
        myClient.getOptions().setMaxConnectionsPerRoute(options.getMaxConnectionsPerRoute());
        myClient.getOptions().setProxyConfig(options.getProxyConfig());
        myClient.getOptions().setReadTimeout(options.getReadTimeout());
        myClient.getOptions().setSslConfig(options.getSslConfig());
    }

    /**
     * Creates new requestors.
     */
    private void createNewRequestors()
    {
        myFilePoster = new FilePostRequestorImpl(myClient, myHeaderValues, myEventManager, myNetworkConfigurationManager);
        myPoster = new PostRequestorImpl(myClient, myHeaderValues, myEventManager, myNetworkConfigurationManager);
        myGetter = new GetRequestorImpl(myClient, myHeaderValues, myEventManager, myNetworkConfigurationManager);
    }
}
