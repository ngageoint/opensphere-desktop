package com.bitsys.common.http.proxy;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * This interfaces defines the methods for classes that provide HTTP proxy
 * configuration based upon an request URL.
 */
public interface ProxyResolver
{
    /**
     * Determines the proxy host configuration(s) from the given destination
     * URL.
     *
     * @param destination the destination URL.
     * @return the list of proxy host configurations.
     * @throws IOException if an error occurs generating the list of proxy host
     *             configuration.
     */
    List<ProxyHostConfig> getProxyServer(URL destination) throws IOException;
}
