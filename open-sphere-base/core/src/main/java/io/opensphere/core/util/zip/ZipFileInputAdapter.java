package io.opensphere.core.util.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Class ZipFileInputAdapter.
 */
public class ZipFileInputAdapter extends ZipInputAdapter
{
    /** The File. */
    private final File myFile;

    /** The FIS. */
    private FileInputStream myFIS;

    /** The Location. */
    private final String myLocation;

    /**
     * Instantiates a new zip file input adapter.
     *
     * @param location the location
     * @param aFile the a file
     * @param method the method
     */
    public ZipFileInputAdapter(String location, File aFile, int method)
    {
        super(method);
        myFile = aFile;
        myLocation = location;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.zip.ZipInputAdapter#closeInputStream()
     */
    @Override
    public void closeInputStream() throws IOException
    {
        if (myFIS != null)
        {
            myFIS.close();
            myFIS = null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.zip.ZipInputAdapter#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException
    {
        if (myFIS == null)
        {
            myFIS = new FileInputStream(myFile);
        }
        return myFIS;
    }

    /**
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
        return myFile.getName();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.zip.ZipInputAdapter#getSize()
     */
    @Override
    public long getSize()
    {
        return myFile.length();
    }
}
