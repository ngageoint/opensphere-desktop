package io.opensphere.core.net;

import com.bitsys.common.http.message.HttpResponse;

public class NetworkReceiveEvent extends NetworkEvent
{
    private final HttpResponse myResponse;

    /**
     * 
     */
    public NetworkReceiveEvent(HttpResponse response, String transactionId)
    {
        this(response, State.COMPLETED, transactionId);
    }

    /**
     * 
     */
    public NetworkReceiveEvent(HttpResponse response, State state, String transactionId)
    {
        super(transactionId, state);
        myResponse = response;
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
