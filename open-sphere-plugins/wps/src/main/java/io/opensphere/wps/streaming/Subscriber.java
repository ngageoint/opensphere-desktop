package io.opensphere.wps.streaming;

import java.net.URL;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.datafilter.DataFilter;

/**
 * Subscribes to the NRT stream and returns the stream's filter id used to get
 * new data.
 */
@FunctionalInterface
public interface Subscriber
{
    /**
     * Subscribes to the specified stream using the specified filter.
     *
     * @param wpsUrl The wps url to send WPS requests to.
     * @param stream The stream to subscribe to.
     * @param filter The filter to use or null if no filter needs to be applied.
     * @param spatialFilter The spatial filter to use or null if no spatial
     *            filter needs to be applied.
     * @return The filter id or stream id of the subscribed stream and the full
     *         url to use when polling the data. If an error occurred during
     *         subscription null is returned.
     */
    SubscriptionContext subscribeToStream(URL wpsUrl, String stream, DataFilter filter, Geometry spatialFilter);
}
