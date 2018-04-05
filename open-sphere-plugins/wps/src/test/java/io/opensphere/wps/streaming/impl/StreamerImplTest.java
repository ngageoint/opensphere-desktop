package io.opensphere.wps.streaming.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerCreator;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.server.StreamHandler;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.test.core.matchers.EasyMockHelper;
import io.opensphere.wps.streaming.SubscriptionContext;

/**
 * Tests the {@link StreamerImpl} class.
 */
public class StreamerImplTest
{
    /**
     * The expected url.
     */
    private static final String ourUrl = "https://somehost/nrt/streamingServlet?filterId=29d25e04-6884-4ed5-88e9-1a93773a6743&pollInterval=1000";

    /**
     * The current streamer being tested.
     */
    private StreamerImpl myTestingStreamer;

    /**
     * Tests starting and stopping a streamer.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     * @throws InterruptedException Bad interupt.
     */
    @Test
    public void test() throws IOException, URISyntaxException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        CountDownLatch latch = new CountDownLatch(3);

        CancellableInputStream stream1 = new CancellableInputStream(new ByteArrayInputStream(new byte[0]), null);
        CancellableInputStream stream2 = new CancellableInputStream(new ByteArrayInputStream(new byte[0]), null);
        CancellableInputStream stream3 = new CancellableInputStream(new ByteArrayInputStream(new byte[0]), null);

        UUID streamId = UUID.randomUUID();
        StreamHandler handler = createHandler(streamId, support, latch, stream1, stream2, stream3);
        ServerProviderRegistry registry = createRegistry(support, stream1, stream2, stream3);

        SubscriptionContext context = new SubscriptionContext();
        context.setStreamId(streamId);
        context.setStreamUrl(new URL(ourUrl));

        support.replayAll();

        myTestingStreamer = new StreamerImpl(context, handler, registry);
        myTestingStreamer.start();

        latch.await();

        support.verifyAll();
    }

    /**
     * Creates an easy mocked handler.
     *
     * @param streamId The expected stream id.
     * @param support Used to create the mock.
     * @param latch The latch to countdown.
     * @param streams The streams to expect.
     * @return The {@link StreamHandler}.
     */
    private StreamHandler createHandler(UUID streamId, EasyMockSupport support, CountDownLatch latch, InputStream... streams)
    {
        StreamHandler handler = support.createMock(StreamHandler.class);

        for (InputStream stream : streams)
        {
            handler.newData(EasyMock.cmpEq(streamId), EasyMock.eq(stream));
            EasyMockHelper.expectLastCallAndCountDownLatch(latch);
        }

        return handler;
    }

    /**
     * Creates an easy mocked registry.
     *
     * @param support Used to create the mock.
     * @param streams The streams to return.
     * @return The easy mocked {@link ServerProviderRegistry}.
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad URI.
     */
    @SuppressWarnings("unchecked")
    private ServerProviderRegistry createRegistry(EasyMockSupport support, final CancellableInputStream... streams)
        throws IOException, URISyntaxException
    {
        IAnswer<CancellableInputStream> answerer = new IAnswer<CancellableInputStream>()
        {
            private int myCount;

            @Override
            public CancellableInputStream answer()
            {
                ResponseValues response = (ResponseValues)EasyMock.getCurrentArguments()[1];
                response.setResponseCode(HttpURLConnection.HTTP_OK);

                myCount++;
                if (myCount >= streams.length)
                {
                    myTestingStreamer.stop();
                }

                return streams[myCount - 1];
            }
        };

        HttpServer server = support.createMock(HttpServer.class);
        server.setBufferSize(EasyMock.eq(StreamerImpl.ourDefaultBufferSize));
        EasyMock.expect(server.sendGet(EasyMockHelper.eq(new URL(ourUrl)), EasyMock.isA(ResponseValues.class)))
                .andAnswer(answerer).times(streams.length);

        ServerProvider<HttpServer> provider = support.createMock(ServerProvider.class);

        ServerCreator<HttpServer> creator = support.createMock(ServerCreator.class);
        EasyMock.expect(creator.createServer(EasyMockHelper.eq(new URL(ourUrl)))).andReturn(server);

        ServerProviderRegistry registry = support.createMock(ServerProviderRegistry.class);
        EasyMock.expect(registry.getProvider(EasyMock.eq(HttpServer.class))).andReturn(new MockHttpProvider(provider, creator));

        return registry;
    }
}
