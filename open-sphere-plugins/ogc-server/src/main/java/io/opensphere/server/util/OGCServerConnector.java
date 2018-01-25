package io.opensphere.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.avro.data.TimeConversions;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import io.opensphere.core.common.services.ServiceException;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.io.CancellableInputStream;

/**
 * This handles establishing a connection with the OGC server.
 */
public class OGCServerConnector
{
    /** Error message for unsupported POST. */
    public static final String OBJECT_ERR_MSG = "Object retrieval not supported for HTTP POST requests with parameter map.";

    /** Error message for unsupported POST. */
    private static final String AVRO_ERR_MSG = "Avro retrieval not supported for HTTP POST requests with parameter map.";

    /** Parameter list for HTTP POST requests. */
    private Map<String, String> myParamList;

    /** InputStream for HTTP POST requests. */
    private InputStream postInput;

    /** The URL. */
    private final URL myUrl;

    /**
     * The registry of server providers.
     */
    private final ServerProviderRegistry myServerRegistry;

    /**
     * Construct the connector.
     *
     * @param url The server URL.
     * @param postParams the InputStream that gets added to the body of an HTTP
     *            post request.
     * @param serverProvider The registry of server providers.
     */
    public OGCServerConnector(URL url, InputStream postParams, ServerProviderRegistry serverProvider)
    {
        this(url, serverProvider);
        postInput = postParams;
    }

    /**
     * Construct the connector.
     *
     * @param url The server URL.
     * @param postParams the parameter list that gets added to the body of an
     *            HTTP post request.
     * @param serverProvider The registry of server providers.
     */
    public OGCServerConnector(URL url, Map<String, String> postParams, ServerProviderRegistry serverProvider)
    {
        this(url, serverProvider);
        myParamList = postParams;
    }

    /**
     * Construct the connector.
     *
     * @param url The server URL.
     * @param serverProvider The registry of server providers.
     */
    public OGCServerConnector(URL url, ServerProviderRegistry serverProvider)
    {
        myUrl = url;
        myServerRegistry = serverProvider;
    }

    /**
     * Gets the server.
     *
     * @return The server.
     * @throws IOException If the server could not be reached.
     * @throws GeneralSecurityException If the user's certs could not be read.
     */
    private HttpServer getServer() throws GeneralSecurityException, IOException
    {
        ServerProvider<HttpServer> provider = myServerRegistry.getProvider(HttpServer.class);
        HttpServer server = provider.getServer(myUrl);

        return server;
    }

    /**
     * Request an XML document from the server.
     *
     * @return The document.
     * @throws OGCServerException If communication with the server fails.
     */
    public Document requestDocument() throws OGCServerException
    {
        try
        {
            return XMLUtilities.docBuilder(true).parse(requestStream());
        }
        catch (ParserConfigurationException e)
        {
            throw new OGCServerException("Failed to create parser: " + e, e);
        }
        catch (SAXException e)
        {
            throw new OGCServerException("Failed to parse document: " + e, e);
        }
        catch (IOException e)
        {
            throw new OGCServerException("Failed to read document: " + e, e);
        }
    }

    /**
     * Construct an Avro DateFileStream, enabling conversion for timestamps.
     *
     * @param in InputStream
     * @return DataFileStream
     * @throws IOException if the InputStream breaks
     */
    public static DataFileStream<GenericRecord> avroDataStream(InputStream in) throws IOException
    {
        if (in == null)
        {
            return null;
        }
        GenericDatumReader<GenericRecord> gdr = new GenericDatumReader<>();
        gdr.getData().addLogicalTypeConversion(new TimeConversions.TimestampConversion());
        return new DataFileStream<>(in, gdr);
    }

    /**
     * Request Avro.
     *
     * @return Avro stuff.
     * @throws OGCServerException in case of any sort of failure
     */
    public DataFileStream<GenericRecord> requestAvro() throws OGCServerException
    {
        try
        {
            return avroDataStream(getHttp(AVRO_ERR_MSG));
        }
        catch (RuntimeException | IOException | URISyntaxException | GeneralSecurityException eek)
        {
            throw getServerException(eek);
        }
    }

    /**
     * Hit the server with an HTTP request and return the InputStream, if any,
     * from which the response may be read. The "param list" feature is not
     * supported for this type of operation.
     *
     * @param noSupp the error message to use in case of an unsupported request
     * @return the response stream
     * @throws GeneralSecurityException probably indicates certificate problems
     * @throws IOException probably indicates a broken connection
     * @throws URISyntaxException probably never happens
     */
    public InputStream getHttp(String noSupp) throws GeneralSecurityException, IOException, URISyntaxException
    {
        HttpServer svr = getServer();
        if (svr == null)
        {
            return null;
        }
        InputStream stream = null;
        ResponseValues response = new ResponseValues();
        if (postInput != null)
        {
            stream = svr.sendPost(myUrl, postInput, response);
        }
        else if (myParamList != null)
        {
            throw new UnsupportedOperationException(noSupp);
        }
        else
        {
            stream = svr.sendGet(myUrl, response);
        }

        if (response.getResponseCode() != HttpURLConnection.HTTP_OK)
        {
            return null;
        }
        return stream;
    }

    /**
     * Request a JAXB object from the OGC server.
     *
     * @param <T> The type of object being requested.
     * @param target The type of object being requested.
     * @return The returned object.
     * @throws OGCServerException If there is a problem communicating with the
     *             server.
     */
    public <T> T requestObject(Class<T> target) throws OGCServerException
    {
        try (InputStream stream = requestStream())
        {
            if (stream != null)
            {
                return XMLUtilities.readXMLObject(stream, target);
            }
        }
        catch (JAXBException e)
        {
            throw new OGCServerException("Failed unmarshal request " + myUrl, e);
        }
        catch (IOException e)
        {
            throw new OGCServerException("Failed to read stream at " + myUrl, e);
        }
        return null;
    }

    /**
     * Request a stream from the OGC server.
     *
     * @return The stream.
     * @throws OGCServerException If the server cannot be reached.
     */
    public CancellableInputStream requestStream() throws OGCServerException
    {
        try
        {
            ResponseValues response = new ResponseValues();
            ServerProvider<HttpServer> provider = myServerRegistry.getProvider(HttpServer.class);
            HttpServer server = provider.getServer(myUrl);

            CancellableInputStream stream = null;
            if (myParamList != null)
            {
                stream = server.sendPost(myUrl, myParamList, response);
            }
            else if (postInput != null)
            {
                stream = server.sendPost(myUrl, postInput, response);
            }
            else
            {
                stream = server.sendGet(myUrl, response);
            }

            String contentType = response.getHeaderValue("Content-Type");
            if (contentType != null && contentType.contains("application/vnd.ogc.se_xml"))
            {
                throw new OGCServerException("Exception received from server, check server URL", null);
            }

            if (response.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
            {
                throw new OGCServerException("HTTP Connection error (401): Unauthorized", null);
            }
            else if (response.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                StringBuilder codeString = new StringBuilder().append(response.getResponseCode());
                if (StringUtils.isNotEmpty(response.getResponseMessage()))
                {
                    codeString.append(" = ").append(response.getResponseMessage());
                }
                throw new OGCServerException("Error code [" + codeString.toString() + "] received from server", null);
            }
            return stream;
        }
        catch (ServiceException | IOException | URISyntaxException e)
        {
            throw new OGCServerException("Failed to get stream from server: " + e, e);
        }
    }

    /**
     * Get a server exception that wraps another exception.
     *
     * @param e The wrapped exception.
     * @return ServerException The server exception.
     */
    public OGCServerException getServerException(Exception e)
    {
        return new OGCServerException("Failed to retrieve data from server: " + e, e);
    }
}
