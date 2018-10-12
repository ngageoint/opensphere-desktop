package io.opensphere.core.util.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.ClosedByInterruptException;

import org.apache.log4j.Logger;

import io.opensphere.core.server.ContentType;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.io.CancellableInputStream;

/**
 * HTTP Utilities.
 */
public final class HttpUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(HttpUtilities.class);

    /**
     * Performs a GET request to the given URL and returns the response as an
     * {@link InputStream}.
     *
     * @param url the URL
     * @param serverProviderRegistry the server provider registry
     * @return The input stream
     * @throws IOException If something went wrong
     */
    public static CancellableInputStream sendGet(URL url, ServerProviderRegistry serverProviderRegistry) throws IOException
    {
        return sendGet(url, serverProviderRegistry.getProvider(HttpServer.class));
    }

    /**
     * Performs a GET request to the given URL and returns the response as an
     * {@link InputStream}.
     *
     * @param url the URL
     * @param serverProvider the server provider
     * @return The input stream
     * @throws IOException If something went wrong
     */
    public static CancellableInputStream sendGet(URL url, ServerProvider<HttpServer> serverProvider) throws IOException
    {
        ResponseValues response = new ResponseValues();
        CancellableInputStream inputStream = sendGet(url, response, serverProvider);
        if (inputStream != null && response.getResponseCode() != HttpURLConnection.HTTP_OK)
        {
            inputStream.close();
            throw new IOException(formatResponse(url, response));
        }
        return inputStream;
    }

    /**
     * Performs a GET request to the given URL and returns the response as an
     * {@link InputStream}.
     *
     * @param url the URL
     * @param response the response
     * @param serverProvider the server provider
     * @return The input stream
     * @throws IOException If something went wrong
     */
    public static CancellableInputStream sendGet(URL url, ResponseValues response, ServerProvider<HttpServer> serverProvider)
            throws IOException
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("GET " + url);
        }

        CancellableInputStream inputStream = null;
        HttpServer server = serverProvider.getServer(url);
        try
        {
            inputStream = server.sendGet(url, response);
        }
        catch (ClosedByInterruptException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e, e);
            }
        }
        catch (URISyntaxException e)
        {
            throw new IOException(e);
        }
        return inputStream;
    }

    /**
     * Performs a POST request to the given URL and returns the response as a {@link CancellableInputStream}.
     *
     * @param url the URL
     * @param postData the post data
     * @param response the response
     * @param contentType the content type
     * @param serverProvider the server provider
     * @return The input stream
     * @throws IOException If something went wrong
     */
    public static CancellableInputStream sendPost(URL url, InputStream postData, ResponseValues response, ContentType contentType,
            ServerProvider<HttpServer> serverProvider)
                    throws IOException
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("POST " + url);
        }

        CancellableInputStream inputStream = null;
        HttpServer server = serverProvider.getServer(url);
        try
        {
            inputStream = server.sendPost(url, postData, response, contentType);

            String error = null;
            if (response.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                error = formatResponse(url, response);
            }

            if (error != null)
            {
                inputStream.close();
                throw new IOException(error);
            }
        }
        catch (ClosedByInterruptException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e, e);
            }
        }
        catch (ConnectException e)
        {
            throw new IOException(e.getMessage() + " to " + url, e);
        }
        catch (URISyntaxException e)
        {
            throw new IOException(e);
        }
        return inputStream;
    }

    /**
     * Validates whether the URL can be requested.
     *
     * @param url the URL
     * @param serverProviderRegistry the server provider registry
     * @return the validation status
     */
    public static ValidatorSupport validateUrl(URL url, ServerProviderRegistry serverProviderRegistry)
    {
        DefaultValidatorSupport validator = new DefaultValidatorSupport(url);
        ResponseValues responseValue = new ResponseValues();
        InputStream stream = null;
        try
        {
            stream = serverProviderRegistry.getProvider(HttpServer.class).getServer(url).sendGet(url, responseValue);
            if (responseValue.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                validator.setValidationResult(ValidationStatus.VALID, null);
            }
            else
            {
                validator.setValidationResult(ValidationStatus.ERROR, formatResponse(url, responseValue));
            }
        }
        catch (IOException | URISyntaxException e)
        {
            validator.setValidationResult(ValidationStatus.ERROR, formatException(e));
        }
        finally
        {
            Utilities.close(stream);
        }
        return validator;
    }

    /**
     * Formats an response value suitable for display to the user.
     *
     * @param url The URL
     * @param responseValue the response value
     * @return the formatted exception text
     */
    public static String formatResponse(URL url, ResponseValues responseValue)
    {
        StringBuilder message = new StringBuilder();
        message.append(responseValue.getResponseCode()).append(' ').append(responseValue.getResponseMessage());
        message.append(": ").append(url);
        return message.toString();
    }

    /**
     * Formats an exception suitable for display to the user.
     *
     * @param e the exception
     * @return the formatted exception text
     */
    private static String formatException(Exception e)
    {
        return e instanceof UnknownHostException ? "Unknown host: " + e.getMessage() : e.toString();
    }

    /** Private constructor. */
    private HttpUtilities()
    {
    }
}
