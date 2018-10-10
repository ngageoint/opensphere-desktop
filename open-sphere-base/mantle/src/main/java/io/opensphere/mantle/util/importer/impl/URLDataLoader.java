package io.opensphere.mantle.util.importer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.common.connection.HttpHeaders.HttpResponseHeader;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.net.URLEncodingUtilities;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.mantle.util.importer.FailureReason;
import io.opensphere.mantle.util.importer.URLDataSource;

/**
 * Takes a KML data source and loads it into an input stream, then passes it
 * onto the processor.
 */
public class URLDataLoader
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(URLDataLoader.class);

    /** The data source. */
    private final URLDataSource myURLDataSource;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param dataSource The data source.
     * @param tb the {@link Toolbox}
     */
    public URLDataLoader(URLDataSource dataSource, Toolbox tb)
    {
        myURLDataSource = dataSource;
        myToolbox = tb;
    }

    /**
     * Get a stream that will load KML data from a data source.
     *
     * @return The stream
     */
    public InputStream load()
    {
        if (myURLDataSource.getType() == URLDataSource.Type.FILE)
        {
            File file = new File(myURLDataSource.getPath());
            return loadLocalFile(file);
        }
        try
        {
            URL url = new URL(myURLDataSource.getPath());
            return loadURL(url);
        }
        catch (MalformedURLException e)
        {
            LOGGER.warn(e.getMessage());
            myURLDataSource.setLoadError(true);
            myURLDataSource.setErrorMessage("Malformed URL: " + e.getMessage());
            myURLDataSource.setFailureReason(FailureReason.MALFORMED_URL);
            return null;
        }
    }

    /**
     * Encode the URL parameters if it's an invalid URI This is not perfect, but
     * fixes some URLs.
     *
     * @param aURL The input URL.
     * @return The maybe fixed URL.
     */
    protected URL fixURL(URL aURL)
    {
        URL fixedURL = aURL;

        try
        {
            fixedURL.toURI();
        }
        catch (URISyntaxException e)
        {
            fixedURL = URLEncodingUtilities.encodeURL(fixedURL);

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Using encoded URL: " + fixedURL);
            }
        }
        return fixedURL;
    }

    /**
     * Handle an exception encountered during a load.
     *
     * @param aReason The failure reason.
     * @param aMessage The error message.
     */
    private void handleException(FailureReason aReason, String aMessage)
    {
        myURLDataSource.setFailureReason(aReason);
        myURLDataSource.setErrorMessage(aMessage);
        LOGGER.warn(aMessage);
    }

    /**
     * Extract KML data from a file and pass it to the KML processor.
     *
     * @param fileToLoad The file
     * @return The stream for the local file, or {@code null} if the file could
     *         not be found.
     */
    private InputStream loadLocalFile(File fileToLoad)
    {
        myURLDataSource.setActualPath(fileToLoad.toString());

        LOGGER.info(StringUtilities.concat("Loading ", myURLDataSource.getActualPath()));

        try
        {
            return new FileInputStream(fileToLoad);
        }
        catch (FileNotFoundException e)
        {
            LOGGER.error(e.getMessage());
            if (fileToLoad.exists())
            {
                myURLDataSource.setErrorMessage("Permission denied: " + fileToLoad.toString());
            }
            else
            {
                myURLDataSource.setErrorMessage("File not found: " + fileToLoad.toString());
            }
            myURLDataSource.setFailureReason(FailureReason.OTHER);
            return null;
        }
    }

    /**
     * Read KML data from a remote data source.
     *
     * @param aURL The URL
     * @return The stream for the remote url.
     */
    private InputStream loadRemoteURL(URL aURL)
    {
        URL url = fixURL(aURL);

        myURLDataSource.setActualPath(url.toString());

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(StringUtilities.concat("Creating connection to ", myURLDataSource.getActualPath()));
        }

        // Download the URL
        InputStream is = null;
        boolean anException = false;
        ResponseValues response = new ResponseValues();
        try
        {
            ServerProvider<HttpServer> provider = myToolbox.getServerProviderRegistry().getProvider(HttpServer.class);
            HttpServer server = provider.getServer(url);

            is = server.sendGet(url, response);
        }
        catch (SSLHandshakeException e)
        {
            anException = true;
            handleException(FailureReason.INVALID_CERTIFICATE, e.getMessage());
        }
        catch (UnknownHostException e)
        {
            anException = true;
            handleException(FailureReason.OTHER, "Unknown host: " + e.getMessage());
        }
        catch (IOException e)
        {
            anException = true;
            FailureReason reason;
            if (response.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED
                    || ("Failed to load URL " + url + ": " + e).contains("401 Unauthorized"))
            {
                reason = FailureReason.INVALID_BASIC_AUTH;
            }
            else
            {
                reason = FailureReason.OTHER;
            }
            handleException(reason, "Failed to load URL " + url + ": " + e);
        }
        catch (URISyntaxException e)
        {
            anException = true;
            handleException(FailureReason.OTHER, e.getMessage());
        }

        if (anException)
        {
            return null;
        }
        if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            logContentLength(response);
            setResponseHeaders(response);

            return is;
        }
        if (response.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
        {
            myURLDataSource.setFailureReason(FailureReason.INVALID_BASIC_AUTH);
        }
        else if (response.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN)
        {
            myURLDataSource.setFailureReason(FailureReason.INVALID_EITHER);
        }
        else
        {
            myURLDataSource.setFailureReason(FailureReason.OTHER);
        }
        StringBuilder msg = new StringBuilder().append(response.getResponseCode()).append(' ');
        msg.append(response.getResponseMessage()).append(": ").append(myURLDataSource.getPath());
        myURLDataSource.setErrorMessage(msg.toString());
        LOGGER.error(msg.toString());
        return null;
    }

    /**
     * Get a stream that will read KML data from a URL data source.
     *
     * @param aURL The URL
     * @return The input stream.
     */
    private InputStream loadURL(URL aURL)
    {
        // Get it from the local file system
        if (UrlUtilities.isFile(aURL))
        {
            try
            {
                File file = new File(aURL.toURI());
                return loadLocalFile(file);
            }
            catch (URISyntaxException e)
            {
                LOGGER.error(e.getMessage());
                return null;
            }
        }
        // Download it as a remote URL
        return loadRemoteURL(aURL);
    }

    /**
     * Log the content length reported by a response.
     *
     * @param response The response.
     */
    private void logContentLength(ResponseValues response)
    {
        if (LOGGER.isDebugEnabled())
        {
            int length = 0;
            try
            {
                length = Integer.parseInt(response.getHeaderValue("Content-Length"));
            }
            catch (NumberFormatException ex)
            {
                length = -1;
            }
            LOGGER.debug("Content-Length is " + length + " bytes");
        }
    }

    /**
     * Sets the relevant response headers in the data source.
     *
     * @param httpResponse The HTTP response
     */
    private void setResponseHeaders(ResponseValues httpResponse)
    {
        String cacheControlStr = httpResponse.getHeaderValue(HttpResponseHeader.CACHE_CONTROL.getFieldName());
        if (cacheControlStr != null)
        {
            myURLDataSource.getResponseHeaders().put(HttpResponseHeader.CACHE_CONTROL.getFieldName(), cacheControlStr);
        }
        String expiresStr = httpResponse.getHeaderValue(HttpResponseHeader.EXPIRES.getFieldName());
        if (expiresStr != null)
        {
            myURLDataSource.getResponseHeaders().put(HttpResponseHeader.EXPIRES.getFieldName(), expiresStr);
        }
        String contentLengthStr = httpResponse.getHeaderValue(HttpResponseHeader.CONTENT_LENGTH.getFieldName());
        if (contentLengthStr != null)
        {
            myURLDataSource.getResponseHeaders().put(HttpResponseHeader.CONTENT_LENGTH.getFieldName(), contentLengthStr);
        }
    }
}
