package io.opensphere.osm.server;

import java.net.MalformedURLException;
import java.net.URL;

import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.osm.util.OSMUtil;
import io.opensphere.server.display.UrlServerSourceValidator;

/**
 * Validates that the server is an Open Street Map server.
 */
public class OSMServerSourceValidator extends UrlServerSourceValidator
{
    /**
     * Constructor.
     *
     * @param serverProviders the server provider registry
     */
    public OSMServerSourceValidator(ServerProviderRegistry serverProviders)
    {
        super(serverProviders);
    }

    @Override
    protected URL getUrl(UrlDataSource source) throws MalformedURLException
    {
        return new URL(OSMUtil.getInstance().buildImageUrlString(source.getBaseUrl(), new ZYXImageKey(0, 0, 0, null)));
    }
}
