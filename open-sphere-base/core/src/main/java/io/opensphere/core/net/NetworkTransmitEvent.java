package io.opensphere.core.net;

import com.bitsys.common.http.message.HttpRequest;

public class NetworkTransmitEvent extends NetworkEvent
{
    private final boolean myExpectingResponse;

    private final HttpRequest myRequest;

    /**
     * 
     */
    public NetworkTransmitEvent(HttpRequest request)
    {
        this(request, null);
    }

    /**
     * @param transactionId
     */
    public NetworkTransmitEvent(HttpRequest request, String transactionId)
    {
        this(request, null, State.COMPLETED);
    }

    /**
     * @param transactionId
     * @param state
     */
    public NetworkTransmitEvent(HttpRequest request, String transactionId, State state)
    {
        this(request, transactionId, state, false);
    }

    /**
     * @param transactionId
     * @param state
     */
    public NetworkTransmitEvent(HttpRequest request, String transactionId, State state, boolean expectingResponse)
    {
        super(transactionId, state);
        myRequest = request;
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
}
