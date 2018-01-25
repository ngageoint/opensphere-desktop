package io.opensphere.core.util.zip;

import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.ZipInputStream;

/**
 * An iterable that provides a {@link ZipEntryNameIterator}. The iteration can
 * only be performed once. The same iterator is always returned by
 * {@link #iterator()}.
 */
public class ZipEntryNameIterable implements Iterable<String>
{
    /** The iterator. */
    private final ZipEntryNameIterator myZipEntryNameIterator;

    /**
     * This constructor will create a {@link ZipInputStream} if necessary.
     *
     * @param inStream The input stream to be iterated.
     */
    public ZipEntryNameIterable(InputStream inStream)
    {
        this(inStream instanceof ZipInputStream ? (ZipInputStream)inStream : new ZipInputStream(inStream));
    }

    /**
     * Constructor.
     *
     * @param inputStream The input stream to be iterated.
     */
    public ZipEntryNameIterable(ZipInputStream inputStream)
    {
        myZipEntryNameIterator = new ZipEntryNameIterator(inputStream);
    }

    @Override
    public Iterator<String> iterator()
    {
        return myZipEntryNameIterator;
    }
}
