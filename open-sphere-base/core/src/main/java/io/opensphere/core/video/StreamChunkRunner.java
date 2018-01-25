package io.opensphere.core.video;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.io.CancellableInputStream;

/**
 * An interface for a runnable which can break video up into smaller pieces and
 * provide the pieces to the receiver.
 */
public interface StreamChunkRunner extends Callable<Void>
{
    @Override
    Void call() throws ChunkException, CacheException;

    /**
     * Provide the items required for this runner to operate.
     *
     * @param inStream The stream which will be read to produce the video
     *            chunks.
     * @param streamSpan The time span of the stream (may be unbounded if the
     *            end time is unknown).
     * @param approxSizeMS The approximate chunk size in milliseconds, the
     *            actual size will extend past this size to the next frame which
     *            precedes a key frame or until the end of the stream is
     *            reached. A value of 0 or less will result in the entire stream
     *            being read as a single chunk.
     * @param category The category which is associated with the stream.
     * @param feedName The user friendly name of the feed the video belongs to.
     * @param queryReceiver The query receiver to which deposits will be
     *            provided.
     * @param expirationTime The amount of time to keep generated chunks in the
     *            cache.
     */
    void init(CancellableInputStream inStream, TimeSpan streamSpan, long approxSizeMS, DataModelCategory category,
            String feedName, CacheDepositReceiver queryReceiver, long expirationTime);

    /**
     * Registers an object interested in receiving non some sort of metadata
     * contained within the video data.
     *
     * @param contentHandler The object interested in non video data.
     */
    void registerNonVideoContentHandler(VideoContentHandler<ByteBuffer> contentHandler);
}
