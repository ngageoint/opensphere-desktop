package io.opensphere.wps.streaming;

/**
 * Contains constants relevant to NRT streaming.
 */
public final class StreamingConstants
{
    /**
     * The filter identifier used in the subscribe request.
     */
    public static final String LAYER_FILTER = "layerFilter";

    /**
     * The layer name identifier used to subscribe to a stream.
     */
    public static final String LAYER_NAME = "layerName";

    /**
     * The output format identifier used in the subscribe request.
     */
    public static final String OUTPUT_FORMAT = "outputFormat";

    /**
     * The mime type for the output of NRT features.
     */
    public static final String OUTPUT_MIME_TYPE = "text/xml; subtype=gml/3.1.1";

    /**
     * The WPS process name for subscribing to a stream.
     */
    public static final String SUBSCRIBE_PROCESS = "SubscribeToNRTLayerProcess";

    /**
     * The output of the subscritpion request.
     */
    public static final String SUBSCRIPTION_OUTPUT = "subscriptionConfig";

    /**
     * The WPS process name for unsubscribing to a subscribed stream.
     */
    public static final String UNSUBSCRIBE_PROCESS = "UnsubscribeToNRTLayerProcess";

    /**
     * The output of the unsubscribe request.
     */
    public static final String UNSUBSCRIBE_OUTPUT = "unsubscribeResults";

    /**
     * Not constructible.
     */
    private StreamingConstants()
    {
    }
}
