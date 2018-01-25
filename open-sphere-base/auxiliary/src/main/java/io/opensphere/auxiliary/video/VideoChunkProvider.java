package io.opensphere.auxiliary.video;

import java.nio.ByteBuffer;

import com.xuggle.xuggler.IContainer;

import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.video.ChunkException;
import io.opensphere.core.video.VideoContentHandler;

/**
 * Provides video chunks to a chunk consumer.
 */
public interface VideoChunkProvider
{
    /**
     * The approximate size to make the new video chunks.
     *
     * @return The approximate size of the video chunks, usually 5 seconds.
     */
    long getApproxSizeMS();

    /**
     * Gets the {@link IContainer} to decode video from.
     *
     * @return The {@link IContainer}.
     */
    IContainer getInputContainer();

    /**
     * The start time of the stream.
     *
     * @return The start time.
     */
    long getStreamStart();

    /**
     * Gets the stream containing the video data.
     *
     * @return The video stream.
     */
    CancellableInputStream getVideoStream();

    /**
     * Reads the video input stream and breaks up the video data into
     * approximately 5 second chunks of video data.
     *
     * @param chunkConsumer The object wanting the video chunks.
     * @param contentHandler The object wanting some sort of metadata from the
     *            video, or null if there isn't one.
     * @return True if the chunk provider was successful at chunking the full
     *         video stream, false otherwise.
     * @throws ChunkException If an exception occurred chunking the video data.
     */
    boolean provideChunks(VideoChunkConsumer chunkConsumer, VideoContentHandler<ByteBuffer> contentHandler) throws ChunkException;
}
