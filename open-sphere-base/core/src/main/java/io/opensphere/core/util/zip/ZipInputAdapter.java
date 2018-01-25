package io.opensphere.core.util.zip;

import java.io.IOException;
import java.io.InputStream;

/**
 * The Class ZipInputAdapter.
 */
public abstract class ZipInputAdapter
{
    /** The Method. Was set to ZipEntry.DEFLATED in OpenSphere. */
    private final int myMethod;

    /**
     * Instantiates a new zip input adapter.
     *
     * @param method the method
     */
    public ZipInputAdapter(int method)
    {
        myMethod = method;
    }

    /**
     * Close input stream.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void closeInputStream() throws IOException
    {
        /* intentionally blank */
    }

    /**
     * Gets the input stream.
     *
     * @return the input stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Gets the location.
     *
     * @return the location
     */
    public abstract String getLocation();

    /**
     * Gets the method.
     *
     * @return the method
     */
    public int getMethod()
    {
        return myMethod;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public abstract String getName();

    /**
     * Gets the size.
     *
     * @return the size
     */
    public long getSize()
    {
        return 0;
    }
}
