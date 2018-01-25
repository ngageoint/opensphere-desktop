package io.opensphere.mantle.data.cache.impl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import org.apache.log4j.Logger;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TLongList;
import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.security.CipherException;
import io.opensphere.mantle.data.cache.Priority;
import io.opensphere.mantle.data.impl.encoder.DiskEncodeHelper;

/**
 * The Class AddMapDataElementWorker.
 */
class AddElementsWorker implements Priority
{
    /** Logger reference. */
    private static final Logger LOG = Logger.getLogger(AddElementsWorker.class);

    /**
     * The cache assistant on which the worker will operate.
     */
    private final DiskCacheAssistant myDiskCacheAssistant;

    /** The my cypher buffer. */
    private byte[] myCypherBuffer;

    /** The my element cache ids. */
    private final TLongList myElementCacheIds;

    /** The my data elements. */
    private final Collection<CacheEntry> myElements;

    /** The my encode cipher. */
    private Cipher myEncodeCipher;

    /** The my file. */
    private final File myFile;

    /** The my insert number. */
    private final int myInsertNumber;

    /** The my type id. */
    private final int myTypeId;

    /**
     * Instantiates a new adds the data element worker.
     *
     * @param typeId the type id
     * @param insertNumber the insert number
     * @param elementIds the element ids
     * @param dataElements the data elements
     * @param diskCacheAssistant The cache assistant on which the worker will
     *            operate.
     */
    public AddElementsWorker(DiskCacheAssistant diskCacheAssistant, int typeId, int insertNumber, TLongList elementIds,
            Collection<CacheEntry> dataElements)
    {
        myDiskCacheAssistant = diskCacheAssistant;
        myTypeId = typeId;
        myInsertNumber = insertNumber;
        myElements = dataElements;
        myElementCacheIds = elementIds;

        if (myDiskCacheAssistant.isUsingEncryption())
        {
            try
            {
                myEncodeCipher = myDiskCacheAssistant.getCipherFactory().initCipher(Cipher.ENCRYPT_MODE);
            }
            catch (CipherException e)
            {
                LOG.error("Could not engage encryption for element disk cache!", e);
            }
        }

        myFile = DiskCacheAssistant.getCacheFile(myDiskCacheAssistant.getDiskCacheLocation(), myTypeId, myInsertNumber);
        if (!myFile.getParentFile().exists() && !myFile.getParentFile().mkdirs())
        {
            LOG.error("Failed to create data element type cache parent directory: " + myFile.getParentFile().getAbsolutePath());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.Priority#getPriority()
     */
    @Override
    public int getPriority()
    {
        return 2;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    @Override
    public void run()
    {
        TLongObjectHashMap<CacheReference> idToCRMap = new TLongObjectHashMap<>();
        try
        {
            FileOutputStream fos = new FileOutputStream(myFile, true);
            BufferedOutputStream bos = new BufferedOutputStream(fos, 1024 * 4000);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(2000);
            int totalBytes = 0;
            long start = System.nanoTime();
            myCypherBuffer = new byte[2000];
            TLongIterator cacheIdItr = myElementCacheIds.iterator();
            long currCacheId;
            LoadedElementData led = null;
            int totalElements = myElements.size();
            Iterator<CacheEntry> ceList = myElements.iterator();
            CacheEntry de = null;
            while (ceList.hasNext())
            {
                de = ceList.next();
                ceList.remove();
                currCacheId = cacheIdItr.next();
                try
                {
                    led = de.getLoadedElementData();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    DiskEncodeHelper.encodeOriginId(oos, led.getOriginId());
                    oos.flush();

                    byte[] bytes = baos.toByteArray();
                    int totalLength = write(false, bos, bytes);
                    byte originIdLength = (byte)totalLength;

                    totalLength += writeMetaDataIfAvailable(bos, baos, led);
                    short mgsOffset = (short)totalLength;

                    totalLength += writeMapGeometrySupportIfAvailable(bos, baos, led);

                    CacheReference dcr = DiskCacheReferenceFactory.createDiskCacheReference((short)myInsertNumber, totalBytes,
                            totalLength, originIdLength, mgsOffset);
                    dcr.setOriginIdCached(led.getOriginId() != null);
                    dcr.setMapGeometrySupportCached(led.getMapGeometrySupport() != null);
                    dcr.setMetaDataInfoCached(led.getMetaData() != null);

                    idToCRMap.put(currCacheId, dcr);
                    totalBytes += totalLength;
                    baos.reset();
                }
                catch (IOException e)
                {
                    LOG.error("Failed to write record", e);
                }
                catch (IllegalBlockSizeException | BadPaddingException | ShortBufferException e)
                {
                    LOG.error(e);
                }
            }

            bos.flush();
            fos.close();

            if (LOG.isTraceEnabled())
            {
                long end = System.nanoTime();
                LOG.trace(StringUtilities.formatTimingMessage("Serialized " + totalElements + " records in ", end - start)
                        + StringUtilities.formatTimingMessage(" - average ", (end - start) / totalElements) + " Total size: "
                        + totalBytes + " Ave Size: " + totalBytes / (double)totalElements);
            }
        }
        catch (IOException e)
        {
            LOG.error(e);
        }

        myDiskCacheAssistant.getDataElementCache().cacheAssistantStoreComplete(idToCRMap);
    }

    /**
     * Write.
     *
     * @param useEncryption the use encryption
     * @param bos the bos
     * @param bytes the bytes
     * @return the int
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws BadPaddingException the bad padding exception
     * @throws IllegalBlockSizeException the illegal block size exception
     * @throws ShortBufferException the short buffer exception
     */
    private int write(boolean useEncryption, BufferedOutputStream bos, byte[] bytes)
        throws IOException, BadPaddingException, IllegalBlockSizeException, ShortBufferException
    {
        int length = bytes.length;
        if (useEncryption)
        {
            if (bytes.length > myCypherBuffer.length)
            {
                myCypherBuffer = new byte[bytes.length + 100];
            }
            length = myEncodeCipher.doFinal(bytes, 0, bytes.length, myCypherBuffer, 0);
        }
        bos.write(useEncryption ? myCypherBuffer : bytes, 0, length);
        return length;
    }

    /**
     * Write map geometry support if available.
     *
     * @param bos the bos
     * @param baos the baos
     * @param led the led
     * @return the int
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws BadPaddingException the bad padding exception
     * @throws IllegalBlockSizeException the illegal block size exception
     * @throws ShortBufferException the short buffer exception
     */
    private int writeMapGeometrySupportIfAvailable(BufferedOutputStream bos, ByteArrayOutputStream baos, LoadedElementData led)
        throws IOException, BadPaddingException, IllegalBlockSizeException, ShortBufferException
    {
        int numWritten = 0;
        if (led.getMapGeometrySupport() != null)
        {
            baos.reset();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            DiskEncodeHelper.encodeMapGeometrySupport(oos, led.getMapGeometrySupport());
            oos.flush();
            numWritten = write(myDiskCacheAssistant.isUsingEncryption(), bos, baos.toByteArray());
        }
        return numWritten;
    }

    /**
     * Write meta data if available.
     *
     * @param bos the bos
     * @param baos the baos
     * @param led the led
     * @return the int
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws BadPaddingException the bad padding exception
     * @throws IllegalBlockSizeException the illegal block size exception
     * @throws ShortBufferException the short buffer exception
     */
    private int writeMetaDataIfAvailable(BufferedOutputStream bos, ByteArrayOutputStream baos, LoadedElementData led)
        throws IOException, BadPaddingException, IllegalBlockSizeException, ShortBufferException
    {
        int numWritten = 0;
        if (led.getMetaData() != null)
        {
            baos.reset();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            DiskCacheEncodeHelper.encodeMetaDataList(oos, led.getMetaData());
            oos.flush();
            numWritten = write(myDiskCacheAssistant.isUsingEncryption(), bos, baos.toByteArray());
        }
        return numWritten;
    }
}
