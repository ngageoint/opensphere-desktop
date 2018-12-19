package io.opensphere.core.net;

import org.apache.http.client.CookieStore;

import com.bitsys.common.http.client.HttpClientOptions;
import com.bitsys.common.http.message.HttpRequest;

/**
 * An event representing the transmission of data from the local instance to a
 * remote source. This transmission may be a single stand alone event, or the
 * sender may synchronously wait for a response. If a response is pending, the
 * event's sender should populate the transaction ID so that the event framework
 * can tie the send and receive events together.
 */
public class NetworkTransmitEvent extends NetworkEvent
{
    /** A flag used to inform the framework that a response is expected. */
    private final boolean myExpectingResponse;

    /** The HTTP request sent to the remote endpoint. */
    private final HttpRequest myRequest;

    private HttpClientOptions myOptions;

    private CookieStore myCookieStore;

    /**
     * Creates a new transmission event with the supplied information.
     *
     * @param request the HTTP request sent to the remote endpoint.
     * @param transactionId the unique identifier applied to the transaction.
     */
    public NetworkTransmitEvent(HttpRequest request, HttpClientOptions options, String transactionId)
    {
        this(request, options, transactionId, State.COMPLETED);
    }

    /**
     * Creates a new transmission event with the supplied information.
     *
     * @param request the HTTP request sent to the remote endpoint.
     * @param transactionId the unique identifier applied to the transaction.
     * @param state the state of the event.
     */
    public NetworkTransmitEvent(HttpRequest request, HttpClientOptions options, String transactionId, State state)
    {
        this(request, options, transactionId, state, false);
    }

    /**
     * Creates a new transmission event with the supplied information.
     *
     * @param request the HTTP request sent to the remote endpoint.
     * @param transactionId the unique identifier applied to the transaction.
     * @param state the state of the event.
     * @param expectingResponse a flag used to indicate that a response is
     *            pending and that the event framework should adjust
     *            accordingly.
     */
    public NetworkTransmitEvent(HttpRequest request, HttpClientOptions options, String transactionId, State state,
            boolean expectingResponse)
    {
        super(transactionId, state);
        myRequest = request;
        myOptions = options;
        myExpectingResponse = false;
    }

    /**
     * Gets the value of the {@link #myExpectingResponse} field.
     *
     * @return the value of the myExpectingResponse field.
     */
    public boolean isExpectingResponse()
    {
        return myExpectingResponse;
    }

    /**
     * Gets the value of the {@link #myRequest} field.
     *
     * @return the value of the myRequest field.
     */
    public HttpRequest getRequest()
    {
        return myRequest;
    }

    /**
     * Gets the value of the {@link #myOptions} field.
     *
     * @return the value stored in the {@link #myOptions} field.
     */
    public HttpClientOptions getOptions()
    {
        return myOptions;
    }

    /**
     * Sets the value of the {@link #myCookieStore} field.
     *
     * @param cookieStore the value to store in the {@link #myCookieStore}
     *            field.
     */
    public void setCookieStore(CookieStore cookieStore)
    {
        myCookieStore = cookieStore;
    }

    /**
     * Gets the value of the {@link #myCookieStore} field.
     *
     * @return the value stored in the {@link #myCookieStore} field.
     */
    public CookieStore getCookieStore()
    {
        return myCookieStore;
    }
}
