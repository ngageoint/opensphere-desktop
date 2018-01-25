package io.opensphere.kml.envoy;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

import io.opensphere.core.util.io.StreamReader;

/**
 * Stream utilities.
 */
public final class StreamUtilities
{
    /** Zip local file header signature. */
    private static final byte[] ZIP_HEADER = { 0x50, 0x4b, 0x03, 0x04 };

    /** The XML declaration encoding pattern. */
    private static final Pattern ENCODING_PATTERN = Pattern.compile(".*?encoding=\"(.+?)\".*", Pattern.DOTALL);

    /** UTF-8 byte-order-mark. */
    private static final byte[] UTF8_BOM = { (byte)0xEF, (byte)0xBB, (byte)0xBF };

    /** UTF-16 big-endian byte-order-mark. */
    private static final byte[] UTF16_BE_BOM = { (byte)0xFE, (byte)0xFF };

    /** UTF-16 little-endian byte-order-mark. */
    private static final byte[] UTF16_LE_BOM = { (byte)0xFF, (byte)0xFE };

    /**
     * Converts an input stream that doesn't support mark/reset into a marked
     * BufferedInputStream.
     *
     * @param is The original input stream
     * @return A wrapped version of the original input stream that supports
     *         mark/reset.
     */
    public static InputStream bufferifyInputStream(InputStream is)
    {
        return is.markSupported() ? is : new BufferedInputStream(is);
    }

    /**
     * Gets the encoding of the input stream.
     *
     * @param is The input stream
     * @return The encoding
     * @throws IOException If an IOException occurs on the input stream
     */
    public static String getEncoding(InputStream is) throws IOException
    {
        if (!is.markSupported())
        {
            throw new IOException("mark/reset not supported");
        }

        String encoding = null;

        // Read the first part of the input stream
        byte[] bytes = new byte[64];
        is.mark(bytes.length);
        new DataInputStream(is).readFully(bytes);
        is.reset();

        // Try to get the encoding from the XML declaration
        String declaration = new String(bytes, "UTF-8");
        Matcher m = ENCODING_PATTERN.matcher(declaration);
        if (m.matches())
        {
            encoding = m.group(1).trim();
        }

        // Get the encoding from the BOM if it wasn't found in the declaration
        if (encoding == null)
        {
            if (startsWith(bytes, UTF8_BOM))
            {
                encoding = "UTF-8";
            }
            else if (startsWith(bytes, UTF16_BE_BOM))
            {
                encoding = "UTF-16BE";
            }
            else if (startsWith(bytes, UTF16_LE_BOM))
            {
                encoding = "UTF-16LE";
            }
            else
            {
                encoding = "UTF-8";
            }
        }

        return encoding;
    }

    /**
     * Determines if the given input stream is a zip input stream.
     *
     * @param is The input stream
     * @return Whether it is a zip input stream
     * @throws IOException If an IOException occurs on the input stream
     */
    public static boolean isZipInputStream(InputStream is) throws IOException
    {
        if (!is.markSupported())
        {
            throw new IOException("mark/reset not supported");
        }

        byte[] bytes = new byte[ZIP_HEADER.length];
        is.mark(bytes.length);
        new DataInputStream(is).readFully(bytes);
        is.reset();

        return Arrays.equals(bytes, ZIP_HEADER);
    }

    /**
     * Determines if the given input stream is a zip input stream by reading the
     * header and not reseting the stream.
     *
     * @param is The input stream
     * @return Whether it is a zip input stream
     * @throws IOException If an IOException occurs on the input stream
     */
    public static boolean isZipInputStreamNoReset(InputStream is) throws IOException
    {
        byte[] bytes = new byte[ZIP_HEADER.length];
        new DataInputStream(is).readFully(bytes);

        return Arrays.equals(bytes, ZIP_HEADER);
    }

    /**
     * Decodes the base64 encoded stream.
     *
     * @param is the input stream
     * @param from the starting byte
     * @param to the ending byte, exclusive
     * @return the decoded stream
     * @throws IOException if something went wrong
     */
    public static InputStream decodeBase64(InputStream is, int from, int to) throws IOException
    {
        byte[] bytes = new StreamReader(is).readStreamIntoBuffer().array();
        byte[] decodedBytes = Base64.decodeBase64(Arrays.copyOfRange(bytes, from, to));
        return new ByteArrayInputStream(decodedBytes);
    }

    /**
     * Tests if one array starts with the other, up to the length of the smaller
     * array.
     *
     * @param a The first array
     * @param a2 The second array
     * @return Whether one array starts with the other
     */
    private static boolean startsWith(byte[] a, byte[] a2)
    {
        int minLength = Math.min(a.length, a2.length);
        for (int i = 0; i < minLength; i++)
        {
            if (a[i] != a2[i])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Private constructor.
     */
    private StreamUtilities()
    {
    }
}
