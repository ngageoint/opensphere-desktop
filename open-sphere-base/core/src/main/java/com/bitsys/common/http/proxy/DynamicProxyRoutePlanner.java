package com.bitsys.common.http.proxy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.protocol.HttpContext;

/**
 * This is a proof-of-concept class for performing HTTP route planning in the
 * new HTTP Client API.
 * <p>
 * <b>WARNING</b>: This class will likely change in the future or go away so use
 * it at your own risk!
 */
public class DynamicProxyRoutePlanner implements HttpRoutePlanner
{
    /** The proxy resolver. */
    private final ProxyResolver resolver;

    /**
     * Constructor.
     *
     * @param resolver the proxy resolver to use
     */
    public DynamicProxyRoutePlanner(final ProxyResolver resolver)
    {
        this.resolver = resolver;
    }

    /**
     * @see HttpRoutePlanner#determineRoute(HttpHost, HttpRequest, HttpContext)
     */
    @Override
    public HttpRoute determineRoute(final HttpHost target, final HttpRequest request, final HttpContext context)
        throws HttpException
    {
        List<ProxyHostConfig> list;
        try
        {
            final URI destination = new URI(target.toURI());
            list = resolver.getProxyServer(destination.toURL());
        }
        catch (final URISyntaxException e)
        {
            throw new HttpException("Failed to convert the target to a well-formed URI: " + target, e);
        }
        catch (final IOException e)
        {
            throw new HttpException(
                    "Failed to determine the proxy server for target=" + target + ", request=" + request + ", context=" + context,
                    e);
        }

        final boolean isSecure = "https".equalsIgnoreCase(target.getSchemeName());
        final HttpRoute httpRoute;
        if (list.isEmpty())
        {
            httpRoute = new HttpRoute(target, null, isSecure);
        }
        else
        {
            final ProxyHostConfig config = list.get(0);
            switch (config.getProxyType())
            {
                case PROXY:
                    final HttpHost proxy = new HttpHost(config.getHost(), config.getPort());
                    httpRoute = new HttpRoute(target, null, proxy, isSecure);
                    break;
                case SOCKS:
                    throw new HttpException(
                            "The first recommended proxy configuration is SOCKS which is not supported: " + config);
                case DIRECT:
                default:
                    httpRoute = new HttpRoute(target, null, isSecure);
            }
        }
        return httpRoute;
    }
}
