package io.opensphere.core.video;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Interface to an object that can encode video and metadata in klv formatted
 * mpegts file.
 *
 */
public interface KLVVideoEncoder extends Closeable
{
    /**
     * Completes the encoding process.
     */
    void close();

    /**
     * Initializes the encoder.
     *
     * @param stream The stream to write to.
     */
    void init(OutputStream stream);

    /**
     * Encodes the metadata into the klv stream.
     *
     * @param metadata The metadata in MISB KLV format to add to the klv stream.
     * @param ptsMS The time fo the metadata.
     */
    void encodeMetadata(ByteBuffer metadata, long ptsMS);

    /**
     * Encodes the frame into the klv stream.
     *
     * @param image The image to encode.
     * @param ptsMS The presentation time of the image in milliseconds.
     * @throws VideoEncoderException Thrown if there were issues writing encoded
     *             video to the stream.
     */
    void encodeVideo(BufferedImage image, long ptsMS) throws VideoEncoderException;
}
