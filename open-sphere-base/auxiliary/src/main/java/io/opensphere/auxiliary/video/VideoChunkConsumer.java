package io.opensphere.auxiliary.video;

import gnu.trove.list.TLongList;
import io.opensphere.core.util.io.ListOfBytesOutputStream;

/**
 * Interface to an object interested in video chunks produced by a video chunk
 * provider.
 */
public interface VideoChunkConsumer
{
    /**
     * Deposit everything that remains.
     *
     * @param startTimeEpoch The start time of the chunk (i.e., the end time of
     *            the last chunk).
     * @param endTimeEpoch The end time of the chunk.
     * @param previousChunkLastEpoch The last epoch of the previous chunk.
     * @param chunkStream The bytes of video data.
     * @param keyFrames The timestamps of the key frames.
     */
    void consumeLastChunk(long startTimeEpoch, long endTimeEpoch, long previousChunkLastEpoch,
            ListOfBytesOutputStream chunkStream, TLongList keyFrames);

    /**
     * Deposit the video into the registry.
     *
     * @param startTimeMS The start of the chunk in milliseconds since epoch.
     * @param endTimeMS The end of the chunk in milliseconds since epoch.
     * @param chunkStream The bytes of video data.
     * @param keyFrames The timestamps of the key frames.
     */
    void consumeVideoChunk(long startTimeMS, long endTimeMS, ListOfBytesOutputStream chunkStream, TLongList keyFrames);
}
