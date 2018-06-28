package io.opensphere.core.server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.StreamHandler;
import io.opensphere.core.server.StreamingServer;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.net.HttpUtilities;

/** Abstract {@link StreamingServer} that deals with {@link InputStream}s. */
@ThreadSafe
public abstract class AbstractStreamingServer implements StreamingServer, AutoCloseable
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(AbstractStreamingServer.class);

    /** The base URL. */
    private final URL myBaseUrl;

    /**
     * The delay between the termination of one query and the commencement of
     * the next.
     */
    private volatile Duration myDelay;

    /** The HTTP server connection. */
    private final HttpServer myServerConnection;

    /** The scheduled executor. */
    private final ScheduledExecutorService myScheduledExecutor;

    /** Map of stream ID to future. */
    private final Map<UUID, ScheduledFuture<?>> myStreamIdToFutureMap = Collections.synchronizedMap(New.map());

    /** Map of stream ID to ResponseValues. */
    private final Map<UUID, ResponseValues> myStreamIdToResponseMap = Collections.synchronizedMap(New.map());

    /** The error handler. */
    @GuardedBy("this")
    private BiConsumer<UUID, Throwable> myErrorHandler;

    /** Whether the executor is internal. */
    private boolean myIsInternalExecutor;

    /** The count of successful queries. */
    private final AtomicInteger mySuccessCount = new AtomicInteger();

    /** The count of failed queries. */
    private final AtomicInteger myFailureCount = new AtomicInteger();

    /**
     * Constructor.
     *
     * @param baseUrl The base URL
     * @param delay The delay between the termination of one query and the
     *            commencement of the next
     * @param provider The server provider
     */
    public AbstractStreamingServer(URL baseUrl, Duration delay, ServerProvider<HttpServer> provider)
    {
        this(baseUrl, delay, provider, Executors.newScheduledThreadPool(1, new NamedThreadFactory("AbstractStreamingServer")));
        myIsInternalExecutor = true;
    }

    /**
     * Constructor.
     *
     * @param baseUrl The base URL
     * @param delay The delay between the termination of one query and the
     *            commencement of the next
     * @param provider The server provider
     * @param executor The executor
     */
    public AbstractStreamingServer(URL baseUrl, Duration delay, ServerProvider<HttpServer> provider,
            ScheduledExecutorService executor)
    {
        myBaseUrl = Utilities.checkNull(baseUrl, "baseUrl");
        myDelay = Utilities.checkNull(delay, "delay");
        myServerConnection = provider.getServer(myBaseUrl);
        myScheduledExecutor = Utilities.checkNull(executor, "executor");
    }

    @Override
    public String getURL()
    {
        return myBaseUrl.toExternalForm();
    }

    @Override
    public UUID start(String stream, final StreamHandler handler, ExecutorService executor) throws IOException
    {
        if (executor != null)
        {
            LOGGER.warn("Ignoring executor");
        }

        final URL url = getUrl(stream);
        final UUID streamId = UUID.randomUUID();
        ScheduledFuture<?> future = myScheduledExecutor.scheduleWithFixedDelay(() -> queryAndNotify(url, handler, streamId), 0,
                myDelay.toMillis(), TimeUnit.MILLISECONDS);
        myStreamIdToFutureMap.put(streamId, future);
        return streamId;
    }

    @Override
    public UUID start(String stream, StreamHandler handler, ExecutorService executor, DataFilter filter, Geometry spatialFilter)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stop(UUID streamId)
    {
        ScheduledFuture<?> future = myStreamIdToFutureMap.remove(streamId);
        if (future != null)
        {
            future.cancel(true);
        }
        myStreamIdToResponseMap.remove(streamId);
    }

    @Override
    public void close()
    {
        for (UUID streamId : New.list(myStreamIdToFutureMap.keySet()))
        {
            stop(streamId);
        }
        if (myIsInternalExecutor)
        {
            myScheduledExecutor.shutdown();
        }
    }

    /**
     * Sets the error handler.
     *
     * @param errorHandler the error handler
     */
    public synchronized void setErrorHandler(BiConsumer<UUID, Throwable> errorHandler)
    {
        myErrorHandler = errorHandler;
    }

    /**
     * Gets the success count.
     *
     * @return the success count
     */
    public AtomicInteger getSuccessCount()
    {
        return mySuccessCount;
    }

    /**
     * Gets the failure count.
     *
     * @return the failure count
     */
    public AtomicInteger getFailureCount()
    {
        return myFailureCount;
    }

    /**
     * Sets the delay. The delay will only apply to subsequently started
     * queries.
     *
     * @param delay the delay
     */
    public void setDelay(Duration delay)
    {
        myDelay = delay;
    }

    /**
     * Handles an error.
     *
     * @param streamId the stream ID
     * @param e the exception
     */
    protected synchronized void error(UUID streamId, Throwable e)
    {
        myFailureCount.incrementAndGet();
        if (myErrorHandler != null)
        {
            myErrorHandler.accept(streamId, e);
        }
        else
        {
            LOGGER.error(e);
        }
    }

    /**
     * Gets the URL for the given stream.
     *
     * @param stream A stream returned from getAvailableStream
     * @return the stream's URL
     * @throws MalformedURLException If the URL is malformed
     */
    protected URL getUrl(String stream) throws MalformedURLException
    {
        return new URL(getURL() + stream);
    }

    /**
     * Gets the server connection.
     *
     * @return the server connection
     */
    protected final HttpServer getServerConnection()
    {
        return myServerConnection;
    }

    /**
     * Gets the ResponseValues for the stream id.
     *
     * @param streamId the stream ID
     * @return the ResponseValues
     */
    protected final ResponseValues getResponse(UUID streamId)
    {
        return myStreamIdToResponseMap.get(streamId);
    }

    /**
     * Queries the server for the given URL and notifies the handler when
     * results are received.
     *
     * @param url the URL
     * @param handler the handler
     * @param streamId the stream ID
     */
    private void queryAndNotify(URL url, StreamHandler handler, UUID streamId)
    {
        ResponseValues response = new ResponseValues();
        try
        {
            InputStream inputStream = sendGet(myServerConnection, url, response);
            myStreamIdToResponseMap.put(streamId, response);
            mySuccessCount.incrementAndGet();
            handler.newData(streamId, inputStream);
        }
        catch (IOException e)
        {
            error(streamId, e);
        }
    }

    /**
     * Performs a GET request to the given URL and returns the response as an
     * {@link InputStream}.
     *
     * @param server the HttpServer
     * @param url the URL
     * @param response the response
     * @return The input stream
     * @throws IOException If something went wrong
     */
    protected InputStream sendGet(HttpServer server, URL url, ResponseValues response) throws IOException
    {
        CancellableInputStream inputStream;
        try
        {
            inputStream = server.sendGet(url, response);
            if (response.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                inputStream.close();
                throw new IOException(HttpUtilities.formatResponse(url, response));
            }
        }
        catch (URISyntaxException e)
        {
            throw new IOException(e);
        }
        return inputStream;
    }
}
