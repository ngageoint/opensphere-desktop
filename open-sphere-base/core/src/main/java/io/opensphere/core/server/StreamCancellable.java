package io.opensphere.core.server;

import java.util.UUID;

import io.opensphere.core.util.lang.Cancellable;

/**
 * A {@link Cancellable} object that will cancel an NRT stream.
 */
public class StreamCancellable implements Cancellable
{
    /**
     * Indicates if the stream is cancelled.
     */
    private boolean myIsCancelled;

    /**
     * The server object containing the stream.
     */
    private final StreamingServer myServer;

    /**
     * The id of the stream.
     */
    private final UUID myStreamId;

    /**
     * Constructs a new stream cancellable.
     *
     * @param server The {@link StreamingServer} containing the running stream.
     * @param streamId The unique id of the stream.
     */
    public StreamCancellable(StreamingServer server, UUID streamId)
    {
        myServer = server;
        myStreamId = streamId;
    }

    @Override
    public void cancel()
    {
        myServer.stop(myStreamId);
        myIsCancelled = true;
    }

    @Override
    public boolean isCancelled()
    {
        return myIsCancelled;
    }
}
