package io.opensphere.core.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import io.opensphere.core.util.BufferUtilities;
import io.opensphere.core.util.Reader;

/**
 * Reads content from a URL into a byte buffer.
 */
public class HttpReader implements Reader
{
    /** The content type returned from the URL connection. */
    private String myContentType = "UNKNOWN";

    /** The URL. */
    private final URL myURL;

    /** The URL connection. */
    private final URLConnection myURLConnection;

    /**
     * Create the reader with a URL.
     *
     * @param url The URL.
     */
    public HttpReader(URL url)
    {
        myURL = url;
        myURLConnection = null;
    }

    /**
     * Create the reader with a <code>URLConnection</code>.
     *
     * @param urlc The URL connection.
     */
    public HttpReader(URLConnection urlc)
    {
        myURL = urlc.getURL();
        myURLConnection = urlc;
    }

    /**
     * Get the content type.
     *
     * @return The content type.
     */
    public String getContentType()
    {
        return myContentType;
    }

    /**
     * Read the stream into a byte array.
     *
     * @return A byte array containing the data from the stream.
     * @throws IOException If there is an exception reading the stream.
     */
    public byte[] readStreamIntoArray() throws IOException
    {
        return BufferUtilities.toByteArray(readStreamIntoBuffer());
    }

    @Override
    public ByteBuffer readStreamIntoBuffer() throws IOException
    {
        return readStreamIntoBuffer((ByteBuffer)null);
    }

    @Override
    public ByteBuffer readStreamIntoBuffer(ByteBuffer buffer) throws IOException
    {
        URLConnection conn = myURLConnection == null ? myURL.openConnection() : myURLConnection;
        setContentType(conn);
        InputStream inputStream = conn.getInputStream();
        try
        {
            return new StreamReader(inputStream, conn.getContentLength()).readStreamIntoBuffer(buffer);
        }
        finally
        {
            inputStream.close();
        }
    }

    /**
     * Set the content type based on the established connection.
     *
     * @param conn The connection.
     */
    protected void setContentType(URLConnection conn)
    {
        myContentType = conn.getContentType();
    }
}
