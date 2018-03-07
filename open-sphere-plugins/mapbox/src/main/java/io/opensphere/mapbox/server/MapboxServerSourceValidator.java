package io.opensphere.mapbox.server;

import java.net.MalformedURLException;
import java.net.URL;

import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.server.display.UrlServerSourceValidator;

/**
 * Validates that the server is a Mapbox server.
 */
public class MapboxServerSourceValidator extends UrlServerSourceValidator
{
    /**
     * Constructor.
     *
     * @param serverProviders the server provider registry
     */
    public MapboxServerSourceValidator(ServerProviderRegistry serverProviders)
    {
        super(serverProviders);
    }

    @Override
    protected URL getUrl(UrlDataSource source) throws MalformedURLException
    {
        return new URL(source.getURLString());
    }
}
