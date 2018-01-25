package io.opensphere.stkterrain.server;

import java.net.MalformedURLException;
import java.net.URL;

import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.server.display.UrlServerSourceValidator;
import io.opensphere.stkterrain.util.Constants;

/**
 * Validates that the server is a STK terrain server.
 */
public class STKServerSourceValidator extends UrlServerSourceValidator
{
    /**
     * Constructor.
     *
     * @param serverProviders the server provider registry
     */
    public STKServerSourceValidator(ServerProviderRegistry serverProviders)
    {
        super(serverProviders);
    }

    @Override
    protected URL getUrl(UrlDataSource source) throws MalformedURLException
    {
        return new URL(source.getBaseUrl() + Constants.TILE_SETS_URL);
    }
}
