package io.opensphere.osh.server;

import java.net.MalformedURLException;
import java.net.URL;

import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.server.display.UrlServerSourceValidator;

/**
 * Validates that the server is a OpenSensorHub server.
 */
public class OSHServerSourceValidator extends UrlServerSourceValidator
{
    /**
     * Constructor.
     *
     * @param serverProviders the server provider registry
     */
    public OSHServerSourceValidator(ServerProviderRegistry serverProviders)
    {
        super(serverProviders);
    }

    @Override
    protected URL getUrl(UrlDataSource source) throws MalformedURLException
    {
        return new URL(source.getURL() + "?service=SOS&version=2.0&request=GetCapabilities");
    }
}
