package io.opensphere.kml.envoy;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.common.connection.HttpHeaders.HttpResponseHeader;
import io.opensphere.core.Toolbox;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.net.URLEncodingUtilities;
import io.opensphere.core.util.net.UrlBuilder;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLDataSource.FailureReason;
import io.opensphere.kml.common.model.KMLDataSource.Type;
import io.opensphere.kml.common.util.KMLLinkHelper;

/**
 * Takes a KML data source and loads it into an input stream, then passes it
 * onto the processor.
 */
@SuppressWarnings("PMD.GodClass")
public class KMLDataLoader
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(KMLDataLoader.class);

    /** The data source. */
    private final KMLDataSource myDataSource;

    /** The length of the loaded content. */
    private long myContentLength = -1;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param dataSource The data source.
     * @param toolbox The system toolbox.
     */
    public KMLDataLoader(KMLDataSource dataSource, Toolbox toolbox)
    {
        myDataSource = dataSource;
        myToolbox = toolbox;
    }

    /**
     * Get the content length, which will be populated during the
     * {@link #load()} call.
     *
     * @return The content length in bytes, or -1 if the content length is
     *         unknown.
     */
    public long getContentLength()
    {
        return myContentLength;
    }

    /**
     * Get a stream that will load KML data from a data source.
     *
     * @return The stream
     */
    public InputStream load()
    {
        InputStream inputStream;
        if (myDataSource.getType() == Type.FILE)
        {
            File file = new File(myDataSource.getPath());
            inputStream = loadLocalFile(file);
        }
        else
        {
            try
            {
                URL url = new URL(myDataSource.getPath());
                inputStream = loadURL(url);
            }
            catch (MalformedURLException e)
            {
                inputStream = loadURLFragment();
            }
        }

        // Create a marked buffer in order to do some checks and processing
        // on it
        if (inputStream != null)
        {
            inputStream = StreamUtilities.bufferifyInputStream(inputStream);
        }

        return inputStream;
    }

    /**
     * Step 1: Identifies the problem. Step 2: Fixes it!! Repeats as necessary
     * until everything is fixed!!
     *
     * @param pUrl The input URL.
     * @return The fixed URL.
     */
    private URL fixURL(URL pUrl)
    {
        URL url = pUrl;

        // Encode the URL parameters if it's an invalid URI. This is not
        // perfect, but fixes some URLs.
        try
        {
            url.toURI();
        }
        catch (URISyntaxException e)
        {
            url = URLEncodingUtilities.encodeURL(url);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Using encoded URL: " + url);
            }
        }

        // Fix double slashes in the path
        if (url.getPath().contains("//"))
        {
            UrlBuilder builder = new UrlBuilder(url);
            builder.setPath(builder.getPath().replaceAll("/{2,}", "/"));
            try
            {
                url = builder.toURL();
            }
            catch (MalformedURLException e)
            {
                LOGGER.error(e, e);
            }
        }

        return url;
    }

    /**
     * Handle an exception encountered during a load.
     *
     * @param reason The failure reason.
     * @param message The error message.
     */
    private void handleException(FailureReason reason, String message)
    {
        myDataSource.setFailureReason(reason);
        myDataSource.setErrorMessage(message);
        Notify.error(myDataSource.getErrorMessage(), Method.ALERT_HIDDEN);
        LOGGER.warn(message);
    }

    /**
     * Extract KML data from a file and pass it to the KML processor.
     *
     * @param input The file
     * @return The stream for the local file, or {@code null} if the file could
     *         not be found.
     */
    private InputStream loadLocalFile(File input)
    {
        File file = input;
        if (!file.exists())
        {
            // Check for Windows drive letters
            String parentPath;
            try
            {
                parentPath = myDataSource.getParentDataSource() == null ? null
                        : new File(KMLLinkHelper.toBaseURL(myDataSource.getParentDataSource()).toURI()).getPath();
                if (parentPath != null && file.getPath().contains(parentPath))
                {
                    Matcher matcher = Pattern.compile(Pattern.quote(parentPath) + "[/\\\\]?([A-z]:.+)").matcher(file.getPath());
                    if (matcher.matches())
                    {
                        file = new File(matcher.group(1));
                    }
                }
            }
            catch (URISyntaxException e)
            {
                LOGGER.error(e, e);
            }
        }
        myDataSource.setActualPath(file.toString());

        LOGGER.info(StringUtilities.concat("Loading ", myDataSource.getActualPath()));

        myContentLength = file.length();

        try
        {
            return new FileInputStream(file);
        }
        catch (FileNotFoundException e)
        {
            LOGGER.error(e.getMessage());
            if (file.exists())
            {
                myDataSource.setErrorMessage("Permission denied: " + file.toString());
            }
            else
            {
                myDataSource.setErrorMessage("File not found: " + file.toString());
            }
            myDataSource.setFailureReason(FailureReason.OTHER);
            Notify.error(myDataSource.getErrorMessage().toString(), Method.ALERT_HIDDEN);
            return null;
        }
    }

    /**
     * Read KML data from a remote data source.
     *
     * @param pUrl The URL
     * @return The stream for the remote url.
     */
    private InputStream loadRemoteURL(URL pUrl)
    {
        URL url = fixURL(pUrl);

        myDataSource.setActualPath(url.toString());

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(StringUtilities.concat("Creating connection to ", myDataSource.getActualPath()));
        }

        // Download the URL
        InputStream inputStream = null;
        boolean wasException = false;
        ResponseValues httpResponse = new ResponseValues();
        try
        {
            inputStream = sendGet(url, httpResponse);
            myContentLength = httpResponse.getContentLength();
        }
        catch (SSLHandshakeException e)
        {
            wasException = true;
            handleException(FailureReason.INVALID_CERTIFICATE, e.getMessage());
        }
        catch (UnknownHostException e)
        {
            wasException = true;
            handleException(FailureReason.OTHER, "Unknown host: " + e.getMessage());
        }
        catch (IOException e)
        {
            wasException = true;
            FailureReason reason;
            if (httpResponse.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED
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
            wasException = true;
            handleException(FailureReason.OTHER, e.getMessage());
        }

        if (wasException)
        {
            return null;
        }
        else
        {
            if (httpResponse.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                logContentLength(httpResponse);
                setResponseHeaders(httpResponse);

                return inputStream;
            }
            else
            {
                if (httpResponse.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
                {
                    myDataSource.setFailureReason(FailureReason.INVALID_BASIC_AUTH);
                }
                else if (httpResponse.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN)
                {
                    myDataSource.setFailureReason(FailureReason.INVALID_EITHER);
                }
                else
                {
                    myDataSource.setFailureReason(FailureReason.OTHER);
                }
                StringBuilder message = new StringBuilder().append("Failed to load KML ").append(myDataSource.getName())
                        .append(": ").append(httpResponse.getResponseCode()).append(' ');
                message.append(httpResponse.getResponseMessage()).append(": ").append(myDataSource.getPath());
                myDataSource.setErrorMessage(message.toString());
                return null;
            }
        }
    }

    /**
     * Uses the ServerProviderRegistry to send a GET request.
     *
     * @param url the URL
     * @param httpResponse the response object
     * @return the input stream
     * @throws IOException if something went wrong
     * @throws URISyntaxException if the URL is no good
     */
    private InputStream sendGet(URL url, ResponseValues httpResponse) throws IOException, URISyntaxException
    {
        ServerProvider<HttpServer> provider = myToolbox.getServerProviderRegistry().getProvider(HttpServer.class);
        HttpServer server = provider.getServer(url);
        InputStream inputStream = server.sendGet(url, httpResponse);
        return inputStream;
    }

    /**
     * Get a stream that will read KML data from a URL data source.
     *
     * @param url The URL
     * @return The input stream.
     */
    private InputStream loadURL(URL url)
    {
        // Get it from the local file system
        if (UrlUtilities.isFile(url))
        {
            String pathname = url.toExternalForm();
            pathname = StringUtilities.removePrefix(pathname, "file://");
            pathname = StringUtilities.removePrefix(pathname, "file:");
            File file = new File(pathname);
            return loadLocalFile(file);
        }
        // Download it as a remote URL
        else
        {
            URL populatedURL = KMLLinkHelper.addURLParameters(myDataSource, url);
            return loadRemoteURL(populatedURL);
        }
    }

    /**
     * Get a stream that will read KML data from a URL fragment data source.
     *
     * @return The stream
     */
    private InputStream loadURLFragment()
    {
        // Prepend the base path of the parent data source to the URL
        // fragment
        URL url = KMLLinkHelper.toBaseURL(myDataSource.getParentDataSource());
        if (url != null)
        {
            url = KMLLinkHelper.appendPath(url, myDataSource.getPath());
        }
        if (url == null)
        {
            myDataSource.setErrorMessage("Null URL for " + myDataSource);
            myDataSource.setFailureReason(FailureReason.OTHER);
            Notify.error(myDataSource.getErrorMessage().toString(), Method.ALERT_HIDDEN);
            return null;
        }
        return loadURL(url);
    }

    /**
     * Log the content length reported by a response.
     *
     * @param httpResponse The response.
     */
    private void logContentLength(ResponseValues httpResponse)
    {
        if (LOGGER.isDebugEnabled())
        {
            long contentLength = 0;
            try
            {
                contentLength = httpResponse.getContentLength();
            }
            catch (NumberFormatException ex)
            {
                contentLength = -1;
            }
            LOGGER.debug("Content-Length is " + contentLength + " bytes");
        }
    }

    /**
     * Sets the relevant response headers in the data source.
     *
     * @param httpResponse The HTTP response
     */
    private void setResponseHeaders(ResponseValues httpResponse)
    {
        String cacheControl = httpResponse.getHeaderValue(HttpResponseHeader.CACHE_CONTROL.getFieldName());
        if (cacheControl != null)
        {
            myDataSource.getResponseHeaders().put(HttpResponseHeader.CACHE_CONTROL.getFieldName(), cacheControl);
        }
        String expires = httpResponse.getHeaderValue(HttpResponseHeader.EXPIRES.getFieldName());
        if (expires != null)
        {
            myDataSource.getResponseHeaders().put(HttpResponseHeader.EXPIRES.getFieldName(), expires);
        }
    }
}
