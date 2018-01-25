package io.opensphere.wps.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.XMLUtilities;

/**
 * This handles establishing a connection with the OGC server.
 */
public class WpsServerConnectionHelper
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(WpsServerConnectionHelper.class);

    /** The URL. */
    private final URL myURL;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Construct the connector.
     *
     * @param url The server URL.
     * @param toolbox The system toolbox.
     */
    public WpsServerConnectionHelper(URL url, Toolbox toolbox)
    {
        myURL = url;
        myToolbox = toolbox;
    }

    /**
     * Get an OGC server connection.
     *
     * @return The connection reference.
     * @throws IOException If an error occurs connecting to the server.
     * @throws GeneralSecurityException If unable to read the user's cert.
     */
    private HttpServer getServerConnection() throws GeneralSecurityException, IOException
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Getting connection for URL [" + myURL + "]");
        }

        ServerProvider<HttpServer> provider = myToolbox.getServerProviderRegistry().getProvider(HttpServer.class);

        return provider.getServer(myURL);
    }

    /**
     * Request a JAXB object from the OGC server using a POST request, suppling the provided parameters as the body of the
     * request.
     *
     * @param <T> The type of object being requested.
     * @param pTarget The type of object being requested.
     * @param pBodyStream the stream from which the body will be read.
     * @return The returned object.
     * @throws ServerException If there is a problem communicating with the server.
     */
    public <T> T requestObjectViaPost(Class<T> pTarget, InputStream pBodyStream) throws ServerException
    {
        T result = null;
        try (InputStream stream = requestStreamAsPost(pBodyStream))
        {
            if (stream != null)
            {
                if (LOGGER.isDebugEnabled())
                {
                    StringBuilder responseBuilder = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8"))))
                    {
                        String line = null;
                        while ((line = reader.readLine()) != null)
                        {
                            responseBuilder.append(line);
                        }
                    }
                    LOGGER.info("Response:\n" + responseBuilder.toString());

                    byte[] bytes = responseBuilder.toString().getBytes(Charset.forName("UTF-8"));
                    result = XMLUtilities.readXMLObject(new ByteArrayInputStream(bytes), pTarget);
                }
                else
                {
                    // TODO: error messages come through in a different XML format:
                    result = XMLUtilities.readXMLObject(stream, pTarget);
                }
            }
        }
        catch (JAXBException e)
        {
            throw new ServerException("Failed unmarshal request " + myURL, e);
        }
        catch (IOException e)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Failed to close stream.", e);
            }
        }
        return result;
    }

    /**
     * Request a JAXB object from the OGC server.
     *
     * @param <T> The type of object being requested.
     * @param target The type of object being requested.
     * @return The returned object.
     * @throws ServerException If there is a problem communicating with the server.
     */
    public <T> T requestObject(Class<T> target) throws ServerException
    {
        T result = null;
        try (InputStream stream = requestStream())
        {
            parseStream(target, stream);
        }
        catch (IOException e)
        {
            LOGGER.info("Failed to close stream.", e);
        }
        return result;
    }

    /**
     * Request a JAXB object from the OGC server.
     *
     * @param <T> The type of object being requested.
     * @param pTarget The type of object being requested.
     * @param pStream the input stream from which to parse the result.
     * @return The returned object.
     * @throws ServerException If there is a problem parsing the results.
     */
    public <T> T parseStream(Class<T> pTarget, InputStream pStream) throws ServerException
    {
        T result = null;
        try
        {
            result = XMLUtilities.readXMLObject(pStream, pTarget);
        }
        catch (JAXBException e)
        {
            throw new ServerException("Failed unmarshal request " + myURL, e);
        }
        return result;
    }

    /**
     * Request a stream from the OGC server as an HTTP POST request.
     *
     * @param pBodyStream the stream from which the body will be read.
     * @return The stream.
     * @throws ServerException If the server cannot be reached.
     */
    public InputStream requestStreamAsPost(InputStream pBodyStream) throws ServerException
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Invoking POST request to " + myURL.toString());
        }

        try
        {
            HttpServer serverConnection = getServerConnection();

            ResponseValues response = new ResponseValues();
            InputStream stream = null;
            stream = serverConnection == null ? null : serverConnection.sendPost(myURL, pBodyStream, response);

            String contentType = response.getHeaderValue("Content-Type");
            if (contentType.contains("application/vnd.ogc.se_xml"))
            {
                throw new ServerException("Exception received from WPS server, check server URL", null);
            }

            if (response.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
            {
                throw new ServerException("HTTP Connection error (401): Unauthorized", null);
            }
            else if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                return stream;
            }
            else
            {
                throw new ServerException(
                        "HTTP Connection error " + response.getResponseCode() + ": " + response.getResponseMessage(), null);
            }
        }
        catch (IOException | URISyntaxException | GeneralSecurityException e)
        {
            throw new ServerException("Failed to get stream from server: " + e, e);
        }
    }

    /**
     * Request a stream from the OGC server.
     *
     * @return The stream.
     * @throws ServerException If the server cannot be reached.
     */
    public InputStream requestStream() throws ServerException
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Invoking GET request to " + myURL.toString());
        }

        try
        {
            HttpServer serverConnection = getServerConnection();

            ResponseValues response = new ResponseValues();
            InputStream stream = null;
            stream = serverConnection == null ? null : serverConnection.sendGet(myURL, response);

            String contentType = response.getHeaderValue("Content-Type");
            if (contentType.contains("application/vnd.ogc.se_xml"))
            {
                throw new ServerException("Exception received from WPS server, check server URL", null);
            }

            if (response.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
            {
                throw new ServerException("HTTP Connection error (401): Unauthorized", null);
            }
            else if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                return stream;
            }
            else
            {
                throw new ServerException(
                        "HTTP Connection error " + response.getResponseCode() + ": " + response.getResponseMessage(), null);
            }
        }
        catch (IOException | URISyntaxException | GeneralSecurityException e)
        {
            throw new ServerException("Failed to get stream from server: " + e, e);
        }
    }

    /**
     * Request a stream from the OGC server.
     *
     * @param httpResponse The HTTP response to be populated.
     * @return The stream.
     * @throws ServerException If the server cannot be reached.
     */
    public InputStream requestStream(ResponseValues httpResponse) throws ServerException
    {
        try
        {
            HttpServer serverConnection = getServerConnection();
            return serverConnection == null ? null : serverConnection.sendGet(myURL, httpResponse);
        }
        catch (IOException | GeneralSecurityException | URISyntaxException e)
        {
            throw new ServerException("Failed to get stream from server: " + e, e);
        }
    }
}
