package com.bitsys.common.http.client;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.protocol.HttpContext;

/**
 * Creates routes using the proxy server when the target server is unknown.
 */
public class ProxyRoutePlanner implements HttpRoutePlanner
{
    /**
     * The proxy to use when creating a proxying route.
     */
    private final HttpHost myProxy;

    /**
     * Constructs a new route planner.
     *
     * @param proxy The proxy to use when creating a proxying route.
     */
    public ProxyRoutePlanner(HttpHost proxy)
    {
        myProxy = proxy;
    }

    @Override
    public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException
    {
        return new HttpRoute(target, null, myProxy, "https".equalsIgnoreCase(target.getSchemeName()));
    }
}
