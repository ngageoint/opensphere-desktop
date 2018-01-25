package io.opensphere.mantle.data.cache.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.security.CipherException;
import io.opensphere.mantle.data.cache.CacheQuery;
import io.opensphere.mantle.data.cache.Priority;
import io.opensphere.mantle.data.impl.encoder.DiskDecodeHelper;

/**
 * The Class RetrieveAndUpdateWorker.
 */
public class RetrieveAndUpdateWorker implements Priority
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(RetrieveAndUpdateWorker.class);

    /** The Constant ONE_MEGABYTE. */
    private static final int ONE_MEGABYTE = 1024 * 1000;

    /**
     * The cache assistant on which the worker is operating.
     */
    private final DiskCacheAssistant myDiskCacheAssistant;

    /** The decode buffer. */
    private byte[] myDecodeBuffer;

    /** The encode cipher. */
    private final Cipher myDecodeCipher;

    /** The entries. */
    private final List<Pair<Long, CacheEntry>> myEntries;

    /** The file. */
    private final File myFile;

    /** The insert number. */
    private final int myInsertNumber;

    /** The query. */
    private final CacheQuery myQuery;

    /** The read buffer. */
    private byte[] myReadBuffer;

    /** The type id. */
    private final int myTypeId;

    /** The update entries. */
    private final boolean myUpdateEntries;

    /**
     * Instantiates a new retrieve and update worker.
     *
     * @param typeId the type id
     * @param insertNum the insert num
     * @param query the query
     * @param entries the entries
     * @param updateEntries the update entries
     * @param diskCacheAssistant the cache assistant on which the worker will
     *            operate.
     */
    public RetrieveAndUpdateWorker(DiskCacheAssistant diskCacheAssistant, int typeId, int insertNum, CacheQuery query,
            List<Pair<Long, CacheEntry>> entries, boolean updateEntries)
    {
        myDiskCacheAssistant = diskCacheAssistant;
        myUpdateEntries = updateEntries;
        myTypeId = typeId;
        myInsertNumber = insertNum;
        myQuery = query;
        myEntries = entries;
        Cipher decodeCipher = null;
        if (myDiskCacheAssistant.isUsingEncryption())
        {
            try
            {
                // TODO: This needs to pass in the algorithm parameters.
                decodeCipher = myDiskCacheAssistant.getCipherFactory().initCipher(Cipher.DECRYPT_MODE);
            }
            catch (CipherException e)
            {
                LOG.error("Could not engage encryption for element disk cache!", e);
            }
        }
        myDecodeCipher = decodeCipher;

        myFile = DiskCacheAssistant.getCacheFile(myDiskCacheAssistant.getDiskCacheLocation(), myTypeId, myInsertNumber);
        if (!myFile.getParentFile().exists() && !myFile.getParentFile().mkdirs())
        {
            LOG.error("Failed to create data element type cache parent directory: " + myFile.getParentFile().getAbsolutePath());
        }
    }

    /**
     * Decode if necessary.
     *
     * @param useEncryption the use encryption
     * @param offset the offset
     * @param size the size
     * @return the byte array input stream
     * @throws BadPaddingException the bad padding exception
     * @throws IllegalBlockSizeException the illegal block size exception
     * @throws ShortBufferException the short buffer exception
     */
    public ByteArrayInputStream decodeIfNecessary(boolean useEncryption, int offset, int size)
        throws BadPaddingException, IllegalBlockSizeException, ShortBufferException
    {
        ByteArrayInputStream elementBAIS = null;
        if (useEncryption)
        {
            int decodeSize = myDecodeCipher.doFinal(myReadBuffer, offset, size, myDecodeBuffer, 0);

            // If the encryption decode buffer is too small for the current
            // record, grow it to match the current record.
            if (size > myDecodeBuffer.length)
            {
                myDecodeBuffer = new byte[size];
            }
            elementBAIS = new ByteArrayInputStream(myDecodeBuffer, 0, decodeSize);
        }
        else
        {
            elementBAIS = new ByteArrayInputStream(myReadBuffer, offset, size);
        }
        return elementBAIS;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.Priority#getPriority()
     */
    @Override
    public int getPriority()
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
        RandomAccessFile raf = null;
        try
        {
            long start = System.nanoTime();
            sortEntries();
            raf = new RandomAccessFile(myFile, "r");
            myDecodeBuffer = new byte[800];
            myReadBuffer = new byte[ONE_MEGABYTE];
            CacheEntryViewProxy proxy = new CacheEntryViewProxy(myDiskCacheAssistant.getDynamicColumnManager(),
                    myDiskCacheAssistant.getDynamicEnumerationRegistry());
            LoadedElementData led = new LoadedElementData();
            long totalBytesRead = 0;
            List<Pair<Long, CacheEntry>> entryList = new LinkedList<>(myEntries);

            ByteArrayInputStream elementBAIS = null;
            while (!myQuery.isComplete() && !entryList.isEmpty())
            {
                // Fill the read buffer with as many entries as it will fit.
                List<Pair<Long, CacheEntry>> retrieveList = fillReadBuffer(raf, myReadBuffer, entryList);
                if (retrieveList.isEmpty())
                {
                    resizeReadBuffer(entryList.iterator().next());
                }
                AbstractDiskCacheReference curRef = null;
                int retrieveBytesRead = 0;
                for (Pair<Long, CacheEntry> entry : retrieveList)
                {
                    Long id = entry.getFirstObject();
                    curRef = (AbstractDiskCacheReference)entry.getSecondObject().getCacheReference();
                    led.setAll(null, null, null);

                    if (myQuery.isRetrieveOriginId() && curRef.isOriginIdCached())
                    {
                        elementBAIS = decodeIfNecessary(false, retrieveBytesRead, curRef.getOriginIdSize());
                        led.setOriginId(DiskDecodeHelper.decodeOriginId(new ObjectInputStream(elementBAIS)));
                    }

                    if (myQuery.isRetrieveMetaDataProvider() && curRef.isMetaDataInfoCached())
                    {
                        elementBAIS = decodeIfNecessary(myDiskCacheAssistant.isUsingEncryption(),
                                retrieveBytesRead + curRef.getMDIOffset(), curRef.getMDISize());
                        led.setMetaData(DiskCacheDecodeHelper.decodeMetaDataList(new ObjectInputStream(elementBAIS)));
                    }

                    if (myQuery.isRetrieveMapGeometrySupport() && curRef.isMapGeometrySupportCached())
                    {
                        elementBAIS = decodeIfNecessary(myDiskCacheAssistant.isUsingEncryption(),
                                retrieveBytesRead + curRef.getMGSOffset(), curRef.getMGSSize());
                        led.setMapGeometrySupport(DiskDecodeHelper.decodeMapGeometrySupport(new ObjectInputStream(elementBAIS)));
                    }

                    proxy.setParts(id.longValue(), entry.getSecondObject(), led);
                    if (myQuery.acceptsInternal(proxy))
                    {
                        if (myUpdateEntries)
                        {
                            entry.getSecondObject().setLastUsedTime(System.currentTimeMillis());
                        }
                        myQuery.processInternal(entry.getFirstObject(), proxy);
                    }

                    if (myUpdateEntries)
                    {
                        updateCacheEntry(entry.getSecondObject(), led);
                    }
                    retrieveBytesRead += curRef.getSize();
                }
                totalBytesRead += retrieveBytesRead;
            }

            if (LOG.isTraceEnabled())
            {
                long end = System.nanoTime();
                LOG.trace(StringUtilities.formatTimingMessage("Read " + myEntries.size() + " records in ", end - start)
                        + StringUtilities.formatTimingMessage(" - average ", (end - start) / myEntries.size()) + " Total size: "
                        + totalBytesRead + " Ave Size: " + (double)totalBytesRead / (double)myEntries.size() + " From: "
                        + myFile.getAbsolutePath());
            }
        }
        catch (IOException | GeneralSecurityException e)
        {
            LOG.error(e);
        }
        finally
        {
            closeRandomAccessFileIfNotNull(raf);
        }
    }

    /**
     * Close if not null.
     *
     * @param raf the random access file.
     */
    protected void closeRandomAccessFileIfNotNull(RandomAccessFile raf)
    {
        if (raf != null)
        {
            try
            {
                raf.close();
            }
            catch (IOException e)
            {
                LOG.error(e);
            }
        }
    }

    /**
     * Fill read buffer from the file.
     *
     * @param raf the raf
     * @param readBuffer the read buffer
     * @param entryList the entry list
     * @return the list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected List<Pair<Long, CacheEntry>> fillReadBuffer(RandomAccessFile raf, byte[] readBuffer,
            List<Pair<Long, CacheEntry>> entryList) throws IOException
    {
        List<Pair<Long, CacheEntry>> readEntryList = new LinkedList<>();
        int bytesRead = 0;
        DiskCacheReference lastRef = null;
        DiskCacheReference curRef = null;
        while (!entryList.isEmpty())
        {
            Pair<Long, CacheEntry> entry = entryList.remove(0);
            curRef = (DiskCacheReference)entry.getSecondObject().getCacheReference();

            // Don't over read.
            if (bytesRead + curRef.getSize() > readBuffer.length)
            {
                // Put back the entry we didn't read because if we did we would
                // exceed our buffer size.
                entryList.add(0, entry);
                break;
            }
            // Don't seek unless we have to, it will be faster, detect if the
            // record we are going to read is sequential after the
            // record we just read. If it was we do not ( or this is the first
            // record to be read in this fill ) then we will have
            // to seek.
            boolean seek = false;
            if (lastRef == null || lastRef.getPosition() + lastRef.getSize() != curRef.getPosition())
            {
                seek = true;
            }
            if (seek)
            {
                raf.seek(curRef.getPosition());
            }
            // Read in the bytes of the record into our buffer.
            bytesRead += raf.read(readBuffer, bytesRead, curRef.getSize());
            readEntryList.add(entry);

            lastRef = curRef;
        }
        return readEntryList;
    }

    /**
     * Resize read buffer.
     *
     * @param next the next
     */
    protected void resizeReadBuffer(Pair<Long, CacheEntry> next)
    {
        CacheEntry entryNotRead = next.getSecondObject();
        DiskCacheReference ref = (DiskCacheReference)entryNotRead.getCacheReference();
        if (ref.getSize() > myReadBuffer.length)
        {
            int newSize = myReadBuffer.length;
            while (newSize < ref.getSize())
            {
                newSize = newSize + 1000;
            }
            LOG.info("Resizing Disk Cache Read Buffer to " + newSize + " bytes");
            myReadBuffer = Arrays.copyOf(myReadBuffer, newSize);
        }
    }

    /**
     * Sort entries into position order for retrieval. The hope is that the cost
     * of the sort in terms of time is made up for by the need not to seek
     * between sequential entries.
     */
    protected void sortEntries()
    {
        Collections.sort(myEntries, (o1, o2) -> compaireEntries(o1, o2));
    }

    /**
     * Compares the two entries for ordered retrieval.
     *
     * @param o1 the left hand side of the comparison.
     * @param o2 the right hand side of the comparison.
     * @return an integer result of the comparison.
     */
    protected int compaireEntries(Pair<Long, CacheEntry> o1, Pair<Long, CacheEntry> o2)
    {
        DiskCacheReference dcr1 = (DiskCacheReference)o1.getSecondObject().getCacheReference();
        DiskCacheReference dcr2 = (DiskCacheReference)o2.getSecondObject().getCacheReference();
        return Integer.compare(dcr1.getPosition(), dcr2.getPosition());
    }

    /**
     * Update cache entry.
     *
     * @param ece the ece
     * @param ledRetrieved the led retrieved
     */
    protected void updateCacheEntry(CacheEntry ece, LoadedElementData ledRetrieved)
    {
        LoadedElementData led = ece.getLoadedElementData();
        if (led == null)
        {
            led = new LoadedElementData();
            ece.setLoadedElementData(led);
        }

        if (ledRetrieved.getOriginId() != null)
        {
            led.setOriginId(ledRetrieved.getOriginId());
        }

        if (ledRetrieved.getMapGeometrySupport() != null)
        {
            led.setMapGeometrySupport(ledRetrieved.getMapGeometrySupport());
        }

        if (ledRetrieved.getMetaData() != null)
        {
            led.setMetaData(ledRetrieved.getMetaData());
        }
    }
}
