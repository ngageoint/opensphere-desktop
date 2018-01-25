package io.opensphere.wps.streaming.factory;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.server.StreamHandler;
import io.opensphere.wps.streaming.MockWPSEnvoy;
import io.opensphere.wps.streaming.SubscriptionContext;
import io.opensphere.wps.streaming.impl.StreamSubscriber;
import io.opensphere.wps.streaming.impl.StreamUnsubscriber;
import io.opensphere.wps.streaming.impl.StreamerImpl;

/**
 * Tests the {@link ComponentsFactoryImpl} class.
 */
public class ComponentsFactoryImplTest
{
    /**
     * Tests creating all components.
     *
     * @throws MalformedURLException Bad url.
     */
    @Test
    public void test() throws MalformedURLException
    {
        EasyMockSupport support = new EasyMockSupport();

        ServerProviderRegistry registry = support.createNiceMock(ServerProviderRegistry.class);

        SubscriptionContext context = new SubscriptionContext();
        StreamHandler handler = support.createNiceMock(StreamHandler.class);

        support.replayAll();

        MockWPSEnvoy envoy = new MockWPSEnvoy(null, null, null);
        URL url = new URL("http://somehost");

        ComponentsFactoryImpl factory = new ComponentsFactoryImpl(url, registry, envoy);

        assertEquals(url, factory.getURL());

        StreamSubscriber subscriber = (StreamSubscriber)factory.buildSubscriber();
        assertEquals(registry, subscriber.getServerProviderRegistry());

        StreamUnsubscriber unsubscriber = (StreamUnsubscriber)factory.buildUnsubscriber();
        assertEquals(envoy, unsubscriber.getEnvoy());

        StreamerImpl streamer = (StreamerImpl)factory.buildStreamer(context, handler);
        assertEquals(context, streamer.getContext());
        assertEquals(handler, streamer.getHandler());

        support.verifyAll();
    }
}
