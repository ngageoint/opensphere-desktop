package io.opensphere.wps.streaming.impl;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.wps.streaming.MockWPSEnvoy;
import io.opensphere.wps.streaming.SubscriptionContext;

/**
 * Tests the {@link StreamUnsubscriber} class.
 */
public class StreamUnsubscriberTest
{
    /**
     * The expected host url.
     */
    private static final String ourExpectedHostUrl = "https://somehost/ogc/wpsServer";

    /**
     * The expected url.
     */
    private static final String ourExpectedUrl = ourExpectedHostUrl
            + "?service=WPS&version=1.0.0&request=Execute&identifier=UnsubscribeToNRTLayerProcess"
            + "&rawdataoutput=unsubscribeResults" + "&datainputs=filterId%3D8ff3a736-21fa-44a2-90f9-a47cb4f80f2c%3B";

    /**
     * The xml response sent back from the server.
     */
    private static final String ourXmlResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<unsubscribedFilters><filterId>8ff3a736-21fa-44a2-90f9-a47cb4f80f2c</filterId></unsubscribedFilters>";

    /**
     * Tests unsubscribe a stream.
     */
    @Test
    public void testUnsubscribe()
    {
        EasyMockSupport support = new EasyMockSupport();

        support.replayAll();

        MockWPSEnvoy envoy = new MockWPSEnvoy(ourExpectedHostUrl, ourExpectedUrl, ourXmlResponse);

        SubscriptionContext context = new SubscriptionContext();
        context.setFilterIdParameterName("filterId");
        context.setStreamId(UUID.fromString("8ff3a736-21fa-44a2-90f9-a47cb4f80f2c"));

        StreamUnsubscriber unsubscriber = new StreamUnsubscriber(envoy);
        unsubscriber.unsubscribe(context);

        assertTrue(envoy.wasRequestedCalled());

        support.verifyAll();
    }
}
