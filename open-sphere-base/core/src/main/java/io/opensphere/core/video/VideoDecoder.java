package io.opensphere.core.video;

import java.io.Closeable;
import java.nio.ByteBuffer;

import edu.umd.cs.findbugs.annotations.Nullable;

import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.util.io.CancellableInputStream;

/**
 * A service that takes a stream, decodes it, and forwards the packets to its
 * handlers.
 */
public interface VideoDecoder extends Closeable
{
    /**
     * Decode the current stream, producing images, until the stream is
     * exhausted or suspended or closed.
     *
     * @return {@code true} if the whole stream was decoded, {@code false} if it
     *         was suspended.
     *
     * @throws VideoDecoderException If there is an error.
     */
    boolean decode() throws VideoDecoderException;

    /**
     * Decode frames as necessary to display the first possible frame following
     * the desired time.
     *
     * @param keyframes The time stamps at which the key frames occur, these
     *            values are in the streams native start time, but converted to
     *            milliseconds.
     * @param time The desired time for the last decoded frame.
     * @param startOnKeyFrame when true, do not decode packets until we reach
     *            the preceding key frame. It may be beneficial in some cases to
     *            ignore key frame boundaries if we have already decoded part of
     *            the current video stream.
     * @return If a frame was produced.
     * @throws VideoDecoderException If there is an error.
     */
    boolean decodeToTime(long[] keyframes, TimeInstant time, boolean startOnKeyFrame) throws VideoDecoderException;

    /**
     * De-register a content handler.
     *
     * @param handler The handler.
     */
    void deregisterNonVideoContentHandler(VideoContentHandler<? super ByteBuffer> handler);

    /**
     * De-register a content handler.
     *
     * @param handler The handler.
     */
    void deregisterVideoContentHandler(VideoContentHandler<? super ImageIOImage> handler);

    /**
     * Get the time that was passed in to
     * {@link #setInputStream(CancellableInputStream, TimeInstant)}.
     *
     * @return The start time.
     */
    TimeInstant getStartTime();

    /**
     * Indicates if the produced image will keep its aspect ratio, if a size was
     * passed when registering a video content handler.
     *
     * @return True if the aspect ratio will be kept, false otherwise.
     */
    boolean isKeepAspectRatio();

    /**
     * Get if the decoder has been suspended.
     *
     * @return {@code true} if suspended.
     */
    boolean isSuspended();

    /**
     * Register a content handler for non-video packets.
     *
     * @param handler The handler.
     */
    void registerNonVideoContentHandler(VideoContentHandler<? super ByteBuffer> handler);

    /**
     * Register a content handler for packets that belong to a video stream.
     *
     * @param handler The handler.
     * @param dimensions The desired dimensions of the video content, or
     *            {@code null} if the native dimensions should be used.
     */
    void registerVideoContentHandler(VideoContentHandler<? super ImageIOImage> handler, @Nullable Vector2i dimensions);

    /**
     * Replace the input stream but keep using the same decoder.
     *
     * @param is The input stream.
     * @param startTime The time corresponding to the beginning of the stream.
     * @throws VideoDecoderException If there's an error initializing with the
     *             given stream.
     */
    void replaceInputStream(CancellableInputStream is, TimeInstant startTime) throws VideoDecoderException;

    /**
     * Initialize the set of items required for decoding the given stream.
     *
     * @param is The stream to be decoded.
     * @param startTime The time corresponding to the beginning of the stream.
     * @throws VideoDecoderException When problems occur initializing the
     *             container or the stream coder.
     */
    void setInputStream(CancellableInputStream is, TimeInstant startTime) throws VideoDecoderException;

    /**
     * Initialize the set of items required for decoding the given file.
     *
     * @param file The file to be decoded.
     * @param startTime The time corresponding to the beginning of the file.
     * @throws VideoDecoderException When problems occur initializing the
     *             container or the stream coder.
     */
    void setInputFile(String file, TimeInstant startTime) throws VideoDecoderException;

    /**
     * Sets if the aspect ratio should be kept when resizing decoded images.
     *
     * @param keepAspectRatio True if aspect ratio should be kept, false
     *            otherwise.
     */
    void setKeepAspectRatio(boolean keepAspectRatio);

    /**
     * Suspend decoding, but do not clean up resources.
     *
     * @param suspend If decoding should be suspended or resumed.
     */
    void suspendDecoding(boolean suspend);

    /**
     * The length of the video in milliseconds.
     *
     * @return The length of the video in milliseconds.
     */
    long videoDuration();
}
