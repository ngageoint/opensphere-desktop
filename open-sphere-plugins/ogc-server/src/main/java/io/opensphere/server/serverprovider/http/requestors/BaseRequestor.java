package io.opensphere.server.serverprovider.http.requestors;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.entity.HttpEntity;
import com.bitsys.common.http.message.HttpRequest;
import com.bitsys.common.http.message.HttpResponse;

import io.opensphere.core.common.connection.HttpHeaders.HttpResponseHeader;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.net.NetworkReceiveEvent;
import io.opensphere.core.net.NetworkTransmitEvent;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.server.serverprovider.http.header.HeaderValues;

/**
 * A base abstract requestor class containing common functionality used by the
 * requestors.
 */
public abstract class BaseRequestor
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(BaseRequestor.class);

    /** Used to communicate to the server. */
    private final HttpClient myClient;

    /** Contains the header values. */
    private final HeaderValues myHeaderValues;

    /** The manager through which events are sent. */
    private EventManager myEventManager;

    /**
     * Constructs a base requestor.
     *
     * @param client The HttpClient object to use to communicate with the
     *            server.
     * @param headerValues Contains the header values.
     * @param eventManager The manager through which events are sent.
     */
    public BaseRequestor(HttpClient client, HeaderValues headerValues, EventManager eventManager)
    {
        myClient = client;
        myHeaderValues = headerValues;
        myEventManager = eventManager;
    }

    /**
     * Gets the client used to communicate with the server.
     *
     * @return The client.
     */
    public HttpClient getClient()
    {
        return myClient;
    }

    /**
     * Sends a post request to the server.
     *
     * @param request The request to send to the server.
     * @param responseValues The response code and message returned from the
     *            server.
     * @return The input stream containing the data returned by the post
     *         request.
     * @throws IOException Thrown if an error occurs while communicating with
     *             the server.
     */
    protected CancellableInputStream executeRequest(final HttpRequest request, ResponseValues responseValues) throws IOException
    {
        boolean success = false;

        if (request.getHeaders().get("Accept-Encoding") == null || request.getHeaders().get("Accept-Encoding").isEmpty())
        {
            request.getHeaders().put("Accept-Encoding", myHeaderValues.getEncoding());
        }

        request.getHeaders().put("User-Agent", myHeaderValues.getUserAgent());

        if (request.getHeaders().get("Accept") == null || request.getHeaders().get("Accept").isEmpty())
        {
            request.getHeaders().put("Accept", myHeaderValues.getAccept());
        }

        long t0 = 0L;
        if (LOGGER.isDebugEnabled())
        {
            t0 = System.nanoTime();
            try
            {
                InetAddress addr = InetAddress.getByName(request.getURI().getHost());
                LOGGER.debug("Got inet address " + addr + " for request host " + request.getURI().getHost());
            }
            catch (UnknownHostException e)
            {
                LOGGER.debug("Exception trying to get inet address for request host " + request.getURI().getHost() + ": " + e, e);
            }
            LOGGER.debug("Sending request: " + request);
        }

        String transactionId = UUID.randomUUID().toString();
        NetworkTransmitEvent transmitEvent = new NetworkTransmitEvent(request, myClient.getOptions(), transactionId);
        transmitEvent.setCookieStore(getClient().getCookieStore());
        myEventManager.publishEvent(transmitEvent);
        HttpResponse response = myClient.execute(request);

        NetworkReceiveEvent receiveEvent = new NetworkReceiveEvent(response, transactionId);

        try
        {
            if (LOGGER.isDebugEnabled())
            {
                long t1 = System.nanoTime();
                LOGGER.debug(StringUtilities.formatTimingMessage("Time to execute request: ", t1 - t0));
            }
            responseValues.setResponseCode(response.getStatusCode());
            responseValues.setResponseMessage(response.getStatusMessage());
            responseValues.setHeader(response.getHeaders().asMap());

            HttpEntity entity = response.getEntity();
            CancellableInputStream result;
            if (entity != null)
            {
                responseValues.setContentLength(entity.getContentLength());

                InputStream returnData = entity.getContent();

                String encoding = responseValues.getHeaderValue("Content-Encoding");
                if (encoding != null && encoding.contains(myHeaderValues.getZippedEncoding()))
                {
                    returnData = new GZIPInputStream(returnData);
                }

                result = new CancellableInputStream(request.getURI().toString(), returnData, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (!request.isAborted())
                        {
                            request.abort();
                        }
                    }
                });
            }
            else
            {
                result = null;
            }

            success = true;
            return result;
        }
        finally
        {
            myEventManager.publishEvent(receiveEvent);
            if (!success)
            {
                Utilities.close(response);
            }
        }
    }

    /**
     * Handles a redirect by requesting the new URL.
     *
     * @param stream The input stream returned by the initial request
     * @param responseValues The response code and message returned from the
     *            server.
     * @param requestCreator The http request creator
     * @return The input stream containing the data returned by the post
     *         request.
     * @throws IOException Thrown if an error occurs while communicating with
     *             the server.
     */
    protected CancellableInputStream handleRedirect(CancellableInputStream stream, ResponseValues responseValues,
            Function<String, HttpRequest> requestCreator)
        throws IOException
    {
        CancellableInputStream returnStream = stream;

        // Handle redirects. The Apache HTTP client does not automatically
        // handle POST redirects. It can be configured to, but when last tried
        // it didn't work, so we're doing it here.
        if (responseValues.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP
                || responseValues.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM)
        {
            String newUrlString = responseValues.getHeaderValue(HttpResponseHeader.LOCATION.getFieldName());
            if (newUrlString != null)
            {
                HttpRequest request = requestCreator.apply(newUrlString);
                if (request != null)
                {
                    try
                    {
                        stream.close();
                    }
                    catch (IOException e)
                    {
                        LOGGER.error(e.getMessage());
                    }

                    returnStream = executeRequest(request, responseValues);
                }
            }
        }

        return returnStream;
    }
}
