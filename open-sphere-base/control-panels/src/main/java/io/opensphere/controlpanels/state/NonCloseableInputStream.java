package io.opensphere.controlpanels.state;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A non closeable input stream.
 */
public class NonCloseableInputStream extends FilterInputStream
{
    /**
     * Constructs an input stream that can't be closed.
     *
     * @param in The input stream.
     */
    public NonCloseableInputStream(InputStream in)
    {
        super(in);
    }

    /* (non-Javadoc)
     *
     * @see java.io.FilterInputStream#close() */
    @Override
    public void close() throws IOException
    {
    }
}
