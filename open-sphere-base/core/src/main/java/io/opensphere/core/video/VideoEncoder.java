package io.opensphere.core.video;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;

/**
 * A service that encodes video frames to a file.
 */
public interface VideoEncoder extends Closeable
{
    @Override
    void close();

    /**
     * Encode a frame of video.
     *
     * @param frame The image to use for the frame.
     * @throws VideoEncoderException If there is an error encoding the video.
     */
    void encode(BufferedImage frame) throws VideoEncoderException;

    /**
     * Encode a frame of video.
     *
     * @param frame The image to use for the frame.
     * @param timeStampMS The time stamp of the frame within it's stream in
     *            milliseconds.
     */
    void encode(BufferedImage frame, long timeStampMS);

    /**
     * Get if this encoder is open.
     *
     * @return If the encoder is open.
     */
    boolean isOpen();

    /**
     * Open the encoder.
     *
     * @param output The output file.
     * @param width The width of the video in pixels.
     * @param height The height of the video in pixels.
     * @throws VideoEncoderException If there is an error.
     */
    void open(File output, int width, int height) throws VideoEncoderException;
}
