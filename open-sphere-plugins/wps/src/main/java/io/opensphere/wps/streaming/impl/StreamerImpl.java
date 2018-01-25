package io.opensphere.wps.streaming.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;

import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerCreator;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.server.StreamHandler;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.wps.streaming.Streamer;
import io.opensphere.wps.streaming.SubscriptionContext;

/**
 * This class is a runnable class that can be scheduled to retrieve new NRT data
 * at a given polling interval.
 */
public class StreamerImpl implements Streamer
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(StreamerImpl.class);

    /**
     * The default buffer size to use.
     */
    protected static final int ourDefaultBufferSize = 1530;

    /**
     * Contains the url to use to get new streaming data.
     */
    private final SubscriptionContext myContext;

    /**
     * The object to send the results to.
     */
    private final StreamHandler myHandler;

    /**
     * Indicates if this streamer should still keep running.
     */
    private boolean myIsRunning = true;

    /**
     * Used to get the server object.
     */
    private final ServerProviderRegistry myRegistry;

    /**
     * Constructs a new streamer class.
     *
     * @param context Contains the url to use to get new data.
     * @param handler The object to send the results to.
     * @param registry Used to get the server to make the polling requests.
     */
    public StreamerImpl(SubscriptionContext context, StreamHandler handler, ServerProviderRegistry registry)
    {
        myContext = context;
        myHandler = handler;
        myRegistry = registry;
    }

    @Override
    public SubscriptionContext getContext()
    {
        return myContext;
    }

    /**
     * Gets the handler.
     *
     * @return The handler.
     */
    public StreamHandler getHandler()
    {
        return myHandler;
    }

    @Override
    public void start() throws IOException
    {
        myIsRunning = true;
        doStreaming();
    }

    @Override
    public void stop()
    {
        myIsRunning = false;
    }

    /**
     * Streams the data.
     *
     * @throws IOException If an unrecoverable exception occurs during
     *             streaming.
     */
    @SuppressWarnings("unchecked")
    private void doStreaming() throws IOException
    {
        URL url = myContext.getStreamUrl();

        ServerProvider<HttpServer> provider = myRegistry.getProvider(HttpServer.class);
        HttpServer server = null;
        if (provider instanceof ServerCreator)
        {
            server = ((ServerCreator<HttpServer>)provider).createServer(url);
            int bufferSize = Integer.parseInt(System.getProperty("nrtBufferSize", String.valueOf(ourDefaultBufferSize)));
            server.setBufferSize(bufferSize);
        }
        else
        {
            server = provider.getServer(url);
        }

        int failureCount = 0;
        while (myIsRunning)
        {
            ResponseValues response = new ResponseValues();
            try
            {
                @SuppressWarnings("PMD.PrematureDeclaration")
                long t0 = System.nanoTime();
                long t1;

                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Requesting NRT features from " + url);
                }

                try (CancellableInputStream stream = server.sendGet(url, response))
                {
                    if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
                    {
                        myHandler.newData(myContext.getStreamId(), stream);
                        t1 = System.nanoTime();
                        failureCount = 0;
                    }
                    else
                    {
                        t1 = System.nanoTime();
                        failureCount++;
                        LOGGER.error("Server returned " + response.getResponseCode() + " " + response.getResponseMessage()
                                + " for url " + url);

                        if (failureCount >= 5)
                        {
                            throw new IOException("Stream was lost for " + url);
                        }
                        else
                        {
                            try
                            {
                                Thread.sleep(myContext.getPollInterval());
                            }
                            catch (InterruptedException e1)
                            {
                                if (LOGGER.isDebugEnabled())
                                {
                                    LOGGER.debug(e1.getMessage(), e1);
                                }
                            }
                        }
                    }
                }

                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(StringUtilities.formatTimingMessage("NRT features received from " + url + " in ", t1 - t0));
                }
            }
            catch (IOException | URISyntaxException e)
            {
                failureCount++;

                LOGGER.error("Error occurred for " + url + " " + e.getMessage(), e);

                if (failureCount >= 5)
                {
                    throw new IOException(e.getMessage(), e);
                }
                else
                {
                    try
                    {
                        Thread.sleep(myContext.getPollInterval());
                    }
                    catch (InterruptedException e1)
                    {
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug(e1.getMessage(), e1);
                        }
                    }
                }
            }
        }
    }
}
