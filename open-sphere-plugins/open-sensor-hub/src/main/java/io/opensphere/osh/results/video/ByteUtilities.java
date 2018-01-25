package io.opensphere.osh.results.video;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;

/** Byte utilities. */
public final class ByteUtilities
{
    /** The buffer size. */
    private static final int BUFFER_SIZE = 8192;

    /**
     * Reads a given number of bytes from the input stream to the output stream.
     *
     * @param in the input stream
     * @param out the output stream
     * @param byteCount the number of bytes to read
     * @return the number of bytes read
     * @throws IOException if a problem occurs reading the stream
     */
    public static int readNBytes(InputStream in, OutputStream out, int byteCount) throws IOException
    {
        int totalBytesRead = 0;
        int bytesRead;
        byte[] bytes = new byte[BUFFER_SIZE];
        while (totalBytesRead < byteCount)
        {
            bytesRead = in.read(bytes, 0, Math.min(bytes.length, byteCount - totalBytesRead));
            if (bytesRead > 0)
            {
                out.write(bytes, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
            else if (bytesRead == -1)
            {
                break;
            }
        }
        return totalBytesRead;
    }

    /**
     * Reads the input stream into the output stream until the marker is found.
     * The marker is included in the output stream.
     *
     * @param in the input stream
     * @param out the output stream
     * @param marker the marker to search for
     * @return whether EOF was hit
     * @throws IOException if a problem occurs reading the stream
     */
    public static boolean readUntilInclusive(PushbackInputStream in, OutputStream out, byte[] marker) throws IOException
    {
        int bytesRead;
        byte[] bytes = new byte[BUFFER_SIZE];
        byte[] mostRecentBytes = new byte[marker.length - 1];
        while ((bytesRead = in.read(bytes)) != -1)
        {
            if (bytesRead > 0)
            {
                int index = indexOf(bytes, bytesRead, marker);
                // Found the marker
                if (index != -1)
                {
                    int writeLength = index + marker.length;
                    out.write(bytes, 0, writeLength);
                    in.unread(bytes, writeLength, bytesRead - writeLength);
                    break;
                }
                else
                {
                    byte[] combinedBytes = concat(mostRecentBytes, bytes, Math.min(bytesRead, marker.length - 1));
                    index = indexOf(combinedBytes, combinedBytes.length, marker);
                    // Found the marker across chunks
                    if (index != -1)
                    {
                        out.write(bytes, 0, index + 1);
                        break;
                    }
                    else
                    {
                        out.write(bytes, 0, bytesRead);
                    }
                }

                shift(mostRecentBytes, bytes, bytesRead);
            }
        }
        return bytesRead == -1;
    }

    /**
     * Returns the index of the marker in the given bytes, or -1 if it could not
     * be found.
     *
     * @param bytes the bytes to search through
     * @param byteLength the number of bytes to use from the byte array
     * @param marker the marker to search for
     * @return the index of the marker
     */
    static int indexOf(byte[] bytes, int byteLength, byte[] marker)
    {
        int index = -1;
        int length = byteLength - (marker.length - 1);
        for (int b = 0; b < length; b++)
        {
            boolean equals = true;
            for (int m = 0; m < marker.length; m++)
            {
                if (bytes[b + m] != marker[m])
                {
                    equals = false;
                    break;
                }
            }

            if (equals)
            {
                return b;
            }
        }
        return index;
    }

    /**
     * Shifts the new bytes into the bytes from the right.
     *
     * @param bytes the bytes
     * @param newBytes the new bytes
     * @param newByteLength the number of new bytes to shift in
     */
    static void shift(byte[] bytes, byte[] newBytes, int newByteLength)
    {
        if (newBytes.length >= bytes.length)
        {
            System.arraycopy(newBytes, newByteLength - bytes.length, bytes, 0, bytes.length);
        }
        else
        {
            System.arraycopy(bytes, newByteLength, bytes, 0, bytes.length - newByteLength);
            System.arraycopy(newBytes, 0, bytes, bytes.length - newByteLength, newByteLength);
        }
    }

    /**
     * Concatenates two byte arrays.
     *
     * @param b1 the first array
     * @param b2 the second array
     * @param b2Length the number of bytes from the second array to use
     * @return the concatenated array
     */
    static byte[] concat(byte[] b1, byte[] b2, int b2Length)
    {
        byte[] concat = new byte[b1.length + b2Length];
        System.arraycopy(b1, 0, concat, 0, b1.length);
        System.arraycopy(b2, 0, concat, b1.length, b2Length);
        return concat;
    }

    /** Disallow instantiation. */
    private ByteUtilities()
    {
    }
}
