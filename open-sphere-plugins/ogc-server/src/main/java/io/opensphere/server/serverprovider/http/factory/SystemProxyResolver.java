package io.opensphere.server.serverprovider.http.factory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;

import com.bitsys.common.http.proxy.ProxyHostConfig;
import com.bitsys.common.http.proxy.ProxyResolver;
import com.bitsys.common.http.proxy.ProxyHostConfig.ProxyType;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * {@link ProxyResolver} implementation that lets the system
 * {@link ProxySelector} determine the proxy.
 */
public class SystemProxyResolver implements ProxyResolver
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SystemProxyResolver.class);

    @Override
    public List<ProxyHostConfig> getProxyServer(URL destination) throws IOException
    {
        List<ProxyHostConfig> results = New.list(1);
        try
        {
            for (Proxy prox : ProxySelector.getDefault().select(destination.toURI()))
            {
                ProxyType proxyType;
                switch (prox.type())
                {
                    case DIRECT:
                        proxyType = ProxyType.DIRECT;
                        break;
                    case HTTP:
                        proxyType = ProxyType.PROXY;
                        break;
                    case SOCKS:
                        proxyType = ProxyType.SOCKS;
                        break;
                    default:
                        throw new UnexpectedEnumException(prox.type());
                }
                SocketAddress addr = prox.address();
                if (addr instanceof InetSocketAddress)
                {
                    results.add(new ProxyHostConfig(proxyType, ((InetSocketAddress)addr).getHostString(),
                            ((InetSocketAddress)addr).getPort()));
                }
            }
        }
        catch (URISyntaxException e)
        {
            LOGGER.error("Failed to create URI for URL: [" + destination + "]: " + e, e);
        }
        return results;
    }
}
