package io.opensphere.core.util.zip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * The Class ZipByteArrayInputAdapter.
 */
public class ZipByteArrayInputAdapter extends ZipInputAdapter
{
    /** The BAIS. */
    private ByteArrayInputStream myBAIS;

    /** The Byte array. */
    private final byte[] myByteArray;

    /** The Location. */
    private final String myLocation;

    /** The Name. */
    private final String myName;

    /**
     * Instantiates a new zip byte array input adapter.
     *
     * @param name the name
     * @param location the location
     * @param byteArray the byte array
     * @param method the method
     */
    public ZipByteArrayInputAdapter(String name, String location, byte[] byteArray, int method)
    {
        super(method);
        myName = name;
        myLocation = location;
        myByteArray = Arrays.copyOf(byteArray, byteArray.length);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.zip.ZipInputAdapter#closeInputStream()
     */
    @Override
    public void closeInputStream() throws IOException
    {
        if (myBAIS != null)
        {
            myBAIS.close();
            myBAIS = null;
        }
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.zip.ZipInputAdapter#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException
    {
        if (myBAIS == null)
        {
            myBAIS = new ByteArrayInputStream(myByteArray);
        }
        return myBAIS;
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.zip.ZipInputAdapter#getLocation()
     */
    @Override
    public String getLocation()
    {
        return myLocation;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.zip.ZipInputAdapter#getName()
     */
    @Override
    public String getName()
    {
        return myName;
    }
}
