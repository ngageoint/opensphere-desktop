package io.opensphere.wps.streaming.impl;

import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.wps.source.HashMapEntryType;
import io.opensphere.wps.source.WPSRequest;
import io.opensphere.wps.source.WPSRequestExecuter;
import io.opensphere.wps.streaming.StreamingConstants;
import io.opensphere.wps.streaming.SubscriptionContext;
import io.opensphere.wps.streaming.Unsubscriber;

/**
 * Unsubscribes from a specified stream.
 */
public class StreamUnsubscriber implements Unsubscriber
{
    /**
     * The envoy used to make the request.
     */
    private final WPSRequestExecuter myRequestExecuter;

    /**
     * Constructs a new stream {@link Unsubscriber}.
     *
     * @param envoy Used to make the unsubscribe request.
     */
    public StreamUnsubscriber(WPSRequestExecuter envoy)
    {
        myRequestExecuter = envoy;
    }

    /**
     * Gets the envoy used to make the request.
     *
     * @return The envoy used to make the request.
     */
    public WPSRequestExecuter getEnvoy()
    {
        return myRequestExecuter;
    }

    @Override
    public void unsubscribe(SubscriptionContext context)
    {
        WPSRequest request = new WPSRequest();
        request.setIdentifier(StreamingConstants.UNSUBSCRIBE_PROCESS);
        request.setRawDataOutput(StreamingConstants.UNSUBSCRIBE_OUTPUT);

        List<HashMapEntryType> dataInputs = New.list();

        HashMapEntryType filterId = new HashMapEntryType();
        filterId.setKey(context.getFilterIdParameterName());
        filterId.setValue(context.getStreamId().toString());
        dataInputs.add(filterId);

        request.setDataInput(dataInputs);

        myRequestExecuter.execute(request);
    }
}
