package io.opensphere.core.net;

import java.io.InputStream;

import com.bitsys.common.http.message.HttpResponse;

/**
 * A network event representing data received from a remote source. This event
 * requires a transaction ID to tie it to a corresponding request.
 */
public class NetworkReceiveEvent extends NetworkEvent
{
    /** The HTTP response received from the remote source. */
    private final HttpResponse myResponse;

    /** The content received from the remote server. */
    private InputStream myContent;

    /**
     * Creates a new event, using the supplied response and transaction ID.
     *
     * @param response the HTTP response object generated when the transaction
     *            was received.
     * @param transactionId the unique identifier applied to the transaction.
     */
    public NetworkReceiveEvent(HttpResponse response, String transactionId)
    {
        this(response, State.COMPLETED, transactionId);
    }

    /**
     * Creates a new event, using the supplied response and transaction ID.
     *
     * @param response the HTTP response object generated when the transaction
     *            was received.
     * @param state the state of the network event.
     * @param transactionId the unique identifier applied to the transaction.
     */
    public NetworkReceiveEvent(HttpResponse response, State state, String transactionId)
    {
        super(transactionId, state);
        myResponse = response;
    }

    /**
     * Sets the value of the {@link #myContent} field.
     *
     * @param content the value to store in the {@link #myContent} field.
     */
    public void setContent(InputStream content)
    {
        myContent = content;
    }

    /**
     * Gets the value of the {@link #myContent} field.
     *
     * @return the value stored in the {@link #myContent} field.
     */
    public InputStream getContent()
    {
        return myContent;
    }

    /**
     * Gets the value of the {@link #myResponse} field.
     *
     * @return the value of the myResponse field.
     */
    public HttpResponse getResponse()
    {
        return myResponse;
    }
}
