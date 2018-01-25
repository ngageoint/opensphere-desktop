package io.opensphere.server.display;

import java.awt.Component;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;

import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.net.HttpUtilities;
import io.opensphere.mantle.datasources.impl.UrlDataSource;

/**
 * Validates a UrlDataSource.
 */
public abstract class UrlServerSourceValidator extends ServiceValidator<UrlDataSource>
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(UrlServerSourceValidator.class);

    /**
     * Used to query the server in order to see if it is valid.
     */
    private final ServerProviderRegistry myServerProviders;

    /**
     * The server to validate.
     */
    private UrlDataSource mySource;

    /**
     * Constructs a new validator.
     *
     * @param serverProviders Used to query the server in order to check its
     *            validity.
     */
    public UrlServerSourceValidator(ServerProviderRegistry serverProviders)
    {
        myServerProviders = serverProviders;
    }

    @Override
    public void setParent(Component parent)
    {
    }

    @Override
    public void setService(String service)
    {
    }

    @Override
    public void setSource(UrlDataSource source)
    {
        mySource = source;
    }

    @Override
    public ValidationStatus getValidationStatus()
    {
        ValidationStatus status = ValidationStatus.VALID;
        String displayError = null;

        if (mySource != null)
        {
            ServerProvider<HttpServer> serverProvider = myServerProviders.getProvider(HttpServer.class);
            try
            {
                URL url = getUrl(mySource);
                ResponseValues response = new ResponseValues();
                try (CancellableInputStream stream = serverProvider.getServer(url).sendGet(url, response))
                {
                    if (response.getResponseCode() != HttpURLConnection.HTTP_OK)
                    {
                        status = ValidationStatus.ERROR;
                        displayError = HttpUtilities.formatResponse(url, response);
                    }
                }
            }
            catch (IOException | URISyntaxException e)
            {
                status = ValidationStatus.ERROR;
                displayError = e.toString();
                LOGGER.error(e, e);
            }
        }
        else
        {
            status = ValidationStatus.ERROR;
            displayError = "Server has not been set.";
        }

        setValidationResult(status, displayError);

        return super.getValidationStatus();
    }

    /**
     * Gets the URL to validate.
     *
     * @param source the data source
     * @return the URL
     * @throws MalformedURLException if the URL is malformed
     */
    protected abstract URL getUrl(UrlDataSource source) throws MalformedURLException;
}
