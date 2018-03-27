package io.opensphere.server.serverprovider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.bitsys.common.http.proxy.AutomaticProxyResolver;
import com.bitsys.common.http.proxy.ProxyHostConfig;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.net.config.ConfigurationType;
import io.opensphere.core.net.config.ManualProxyConfiguration;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * Implementation of {@link ProxySelector} that uses the
 * {@link NetworkConfigurationManager}.
 */
public final class ProxySelectorImpl extends ProxySelector
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ProxySelectorImpl.class);

    /** The proxy selector provided by the JRE. */
    private static final ProxySelector SYSTEM_DEFAULT_PROXY_SELECTOR = ProxySelector.getDefault();

    /** The network configuration manager. */
    private final NetworkConfigurationManager myNetworkConfigurationManager;

    /**
     * Gets the Proxy for the given ProxyHostConfig.
     *
     * @param proxyHostConfig the ProxyHostConfig
     * @return the equivalent Proxy object
     */
    public static Proxy getProxy(ProxyHostConfig proxyHostConfig)
    {
        Proxy proxy;
        switch (proxyHostConfig.getProxyType())
        {
            case DIRECT:
                proxy = Proxy.NO_PROXY;
                break;
            case PROXY:
                proxy = new Proxy(Type.HTTP, new InetSocketAddress(proxyHostConfig.getHost(), proxyHostConfig.getPort()));
                break;
            case SOCKS:
                proxy = new Proxy(Type.SOCKS, new InetSocketAddress(proxyHostConfig.getHost(), proxyHostConfig.getPort()));
                break;
            default:
                throw new UnexpectedEnumException(proxyHostConfig.getProxyType());
        }
        return proxy;
    }

    /**
     * Constructor.
     *
     * @param networkConfigurationManager The network configuration manager,
     *            used to get the application proxy preferences.
     */
    public ProxySelectorImpl(NetworkConfigurationManager networkConfigurationManager)
    {
        myNetworkConfigurationManager = networkConfigurationManager;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe)
    {
        LOGGER.error("Failed to connect to proxy [" + uri + "] [" + sa + "]: " + ioe, ioe);
    }

    @Override
    public List<Proxy> select(URI uri)
    {
        List<Proxy> results = New.list(1);
        if ("socket".equals(uri.getScheme()))
        {
            results.add(Proxy.NO_PROXY);
        }
        else if (myNetworkConfigurationManager.getSelectedProxyType() == ConfigurationType.SYSTEM)
        {
            if (myNetworkConfigurationManager.isExcludedFromProxy(uri.getHost()))
            {
                results.add(Proxy.NO_PROXY);
            }
            else
            {
                results.addAll(SYSTEM_DEFAULT_PROXY_SELECTOR.select(uri));
            }
        }
        else if (myNetworkConfigurationManager.getSelectedProxyType() == ConfigurationType.MANUAL)
        {
            results.add(selectWithProxyHostPort(uri));
        }
        else if (myNetworkConfigurationManager.getSelectedProxyType() == ConfigurationType.URL)
        {
            String proxyConfigUrl = myNetworkConfigurationManager.getUrlConfiguration().getProxyUrl();
            if (!StringUtils.isBlank(proxyConfigUrl))
            {
                selectWithConfigUrl(uri, proxyConfigUrl, results);
            }
            else
            {
                results.add(Proxy.NO_PROXY);
            }
        }
        else
        {
            results.add(Proxy.NO_PROXY);
        }
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Proxy selection for " + uri + " is " + results);
        }
        return results;
    }

    /**
     * Select proxy(s) to use for the given uri when the system is configured to
     * use a proxy configuration script URL.
     *
     * @param uri The URI.
     * @param proxyConfigUrl The proxy config url.
     * @param results The results.
     */
    private void selectWithConfigUrl(URI uri, String proxyConfigUrl, List<Proxy> results)
    {
        try
        {
            URL scriptUrl = new URL(proxyConfigUrl);
            if (scriptUrl.getHost().equals(uri.getHost()))
            {
                results.add(Proxy.NO_PROXY);
            }
            else
            {
                AutomaticProxyResolver resolver = new AutomaticProxyResolver(scriptUrl);
                List<ProxyHostConfig> proxyServer = resolver.getProxyServer(uri.toURL());
                for (ProxyHostConfig proxyHostConfig : proxyServer)
                {
                    Proxy proxy = getProxy(proxyHostConfig);
                    results.add(proxy);
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to get proxies for [" + uri + "]: " + e, e);
            results.add(Proxy.NO_PROXY);
        }
    }

    /**
     * Select a proxy to use for the given uri when the system is configured to
     * use a specific host/port for the proxy.
     *
     * @param uri The URI.
     * @return the proxy.
     */
    private Proxy selectWithProxyHostPort(URI uri)
    {
        ManualProxyConfiguration manualConfiguration = myNetworkConfigurationManager.getManualConfiguration();
        String proxyHost = manualConfiguration.getHost();
        if (myNetworkConfigurationManager.isExcludedFromProxy(uri.getHost()) || proxyHost.equals(uri.getHost()))
        {
            return Proxy.NO_PROXY;
        }
        return new Proxy(Type.HTTP, new InetSocketAddress(proxyHost, manualConfiguration.getPort()));
    }
}
