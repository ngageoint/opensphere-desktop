package io.opensphere.core.util;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * An object that knows how to read its content stream into a byte buffer.
 */
public interface Reader
{
    /**
     * Read the stream into a new byte buffer.
     *
     * @return The byte buffer.
     * @throws IOException If there is an exception reading the stream.
     */
    ByteBuffer readStreamIntoBuffer() throws IOException;

    /**
     * Read the stream into an existing byte buffer if it has enough capacity.
     * If the existing buffer does not have enough capacity, a new buffer will
     * be created and the contents of the existing buffer will be copied to the
     * new buffer, along with the contents of the stream.
     *
     * @param buffer The byte buffer to use if possible.
     * @return The byte buffer containing the data from the stream.
     * @throws IOException If there is an exception reading the stream.
     */
    ByteBuffer readStreamIntoBuffer(ByteBuffer buffer) throws IOException;
}
