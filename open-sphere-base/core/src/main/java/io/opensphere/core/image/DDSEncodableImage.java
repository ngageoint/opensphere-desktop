package io.opensphere.core.image;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

import io.opensphere.core.image.DDSEncoder.EncodingException;

/**
 * Interface for an image that can be converted to DDS.
 */
public interface DDSEncodableImage
{
    /**
     * Get this image as a DDS image.
     *
     * @return The DDS image.
     * @throws IOException If the image cannot be converted.
     */
    DDSImage asDDSImage() throws IOException;

    /**
     * Encode this image as a DDS on a separate thread provided by an executor.
     * Return an input stream from which the DDS can be read.
     *
     * @param executor An executor to provide the thread to do the encoding on.
     * @param dispose Indicates if the image should be closed once encoding is
     *            complete.
     * @return The input stream from which a {@link DDSImage} can be
     *         deserialized.
     * @throws EncodingException If there is an encoding error.
     */
    InputStream getDDSImageStream(Executor executor, boolean dispose) throws EncodingException;
}
