package io.opensphere.core.image;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

/**
 * Interface for a service that can encode an image into a Direct Draw Surface.
 */
public interface DDSEncoder
{
    /**
     * Encode a Java AWT image into a DDS image.
     *
     * @param input The input data.
     * @param compression DDS compression.
     * @return The output image data.
     * @throws EncodingException If input cannot be encoded.
     */
    ByteBuffer encode(BufferedImage input, Image.CompressionType compression) throws EncodingException;

    /**
     * Encode a Java AWT image into a DDS image.
     *
     * @param input The input data.
     * @param compression DDS compression.
     * @param executor An executor to use to encode the image.
     * @param encodingCompleteTask A task to be run when encoding is complete.
     * @return A stream that the image data can be read from.
     * @throws EncodingException If input cannot be encoded.
     */
    InputStream encodeStreaming(BufferedImage input, Image.CompressionType compression, Executor executor,
            Runnable encodingCompleteTask)
        throws EncodingException;

    /**
     * Exception indicating an encoding error.
     */
    class EncodingException extends Exception
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param message The error message.
         */
        public EncodingException(String message)
        {
            super(message);
        }

        /**
         * Constructor.
         *
         * @param message The error message.
         * @param e The cause.
         */
        public EncodingException(String message, Exception e)
        {
            super(message, e);
        }
    }
}
