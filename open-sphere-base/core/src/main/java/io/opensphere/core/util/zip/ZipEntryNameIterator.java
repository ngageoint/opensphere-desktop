package io.opensphere.core.util.zip;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.jcip.annotations.NotThreadSafe;

import org.apache.log4j.Logger;

/** An iterator over the names of the entries in a {@link ZipInputStream}. */
@NotThreadSafe
public class ZipEntryNameIterator implements Iterator<String>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ZipEntryNameIterator.class);

    /** The input stream. */
    private final ZipInputStream myInputStream;

    /** The next entry name, if it has been retrieved. */
    private String myNextEntryName;

    /**
     * Constructor.
     *
     * @param inputStream The input stream.
     */
    public ZipEntryNameIterator(ZipInputStream inputStream)
    {
        myInputStream = inputStream;
    }

    @Override
    public boolean hasNext()
    {
        return getNextEntryName() != null;
    }

    @Override
    public String next()
    {
        String next = getNextEntryName();
        myNextEntryName = null;
        if (next == null)
        {
            throw new NoSuchElementException();
        }
        return next;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieve the next entry name, or return the one already retrieved.
     *
     * @return The next entry name, or {@code null} if there was an error or
     *         there are no more entries.
     */
    private String getNextEntryName()
    {
        if (myNextEntryName == null)
        {
            try
            {
                ZipEntry nextEntry = myInputStream.getNextEntry();
                myNextEntryName = nextEntry == null ? null : nextEntry.getName();
            }
            catch (IOException e)
            {
                LOGGER.warn("Failed to read from zip stream: " + e, e);
            }
        }
        return myNextEntryName;
    }
}
