package io.opensphere.mantle.data.cache.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongFunction;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import org.apache.log4j.Logger;

import io.opensphere.core.util.security.CipherException;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.dynmeta.impl.DynamicMetaDataListViewProxy;
import io.opensphere.mantle.data.dynmeta.impl.DynamicMetadataManagerImpl;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.impl.encoder.DiskDecodeHelper;

/**
 * The Class DiskCacheDirectAccessRetriever.
 */
public class DiskCacheDirectAccessRetriever extends DefaultDirectAccessRetriever
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(DiskCacheDirectAccessRetriever.class);

    /**
     * The cache assistant that the retriever is operating on.
     */
    private final DiskCacheAssistant myDiskCacheAssistant;

    /** The my decode buffer. */
    private byte[] myDecodeBuffer;

    /** The my encode cipher. */
    private final Cipher myDecodeCipher;

    /** The my insert to raf map. */
    private final Map<Integer, RandomAccessFile> myInsertToRAFMap;

    /** The is closed. */
    private boolean myIsClosed;

    /** The my read buffer. */
    private byte[] myReadBuffer;

    /** The my retrieve lock. */
    private final ReentrantLock myRetrieveLock;

    /** The my type id. */
    private final int myTypeId;

    /**
     * Instantiates a new disk cache direct access retriever.
     *
     * @param dti the dti
     * @param cacheRefMap the cache ref map
     * @param dcm the dcm
     * @param diskCacheAssistant the cache assistant on which the worker will
     *            operate.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public DiskCacheDirectAccessRetriever(DiskCacheAssistant diskCacheAssistant, DataTypeInfo dti,
            LongFunction<CacheEntry> cacheRefMap, DynamicMetadataManagerImpl dcm)
    {
        super(dti, cacheRefMap, dcm);
        myDiskCacheAssistant = diskCacheAssistant;
        myTypeId = myDiskCacheAssistant.getTypeId(dti);
        Cipher decodeCipher;
        try
        {
            // TODO: This needs to pass in the algorithm parameters.
            decodeCipher = myDiskCacheAssistant.isUsingEncryption()
                    ? myDiskCacheAssistant.getCipherFactory().initCipher(Cipher.DECRYPT_MODE) : null;
        }
        catch (CipherException e)
        {
            LOG.error("Could not engage encryption for element disk cache!", e);
            decodeCipher = null;
        }
        myDecodeCipher = decodeCipher;
        myDecodeBuffer = new byte[1000];
        myReadBuffer = new byte[1000];
        myInsertToRAFMap = new HashMap<>();
        myRetrieveLock = new ReentrantLock();

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.impl.DefaultDirectAccessRetriever#close()
     */
    @Override
    public void close()
    {
        myRetrieveLock.lock();
        try
        {
            myIsClosed = true;
            for (Map.Entry<Integer, RandomAccessFile> entry : myInsertToRAFMap.entrySet())
            {
                try
                {
                    entry.getValue().close();
                }
                catch (IOException e)
                {
                    LOG.error("Failed to close random access file ", e);
                }
            }
            myInsertToRAFMap.clear();
        }
        finally
        {
            myRetrieveLock.unlock();
        }
    }

    /**
     * Decode if necessary.
     *
     * @param useEncryption the use encryption
     * @param size the size
     * @return the byte array input stream
     * @throws BadPaddingException the bad padding exception
     * @throws IllegalBlockSizeException the illegal block size exception
     * @throws ShortBufferException the short buffer exception
     */
    public ByteArrayInputStream decodeIfNecessary(boolean useEncryption, int size)
        throws BadPaddingException, IllegalBlockSizeException, ShortBufferException
    {
        ByteArrayInputStream elementBAIS = null;
        if (useEncryption)
        {
            int decodeSize = myDecodeCipher.doFinal(myReadBuffer, 0, size, myDecodeBuffer, 0);
            if (size > myDecodeBuffer.length)
            {
                myDecodeBuffer = new byte[size];
            }
            elementBAIS = new ByteArrayInputStream(myDecodeBuffer, 0, decodeSize);
        }
        else
        {
            elementBAIS = new ByteArrayInputStream(myReadBuffer, 0, size);
        }
        return elementBAIS;
    }

    /**
     * Gets the disk cache reference.
     *
     * @param cr the cr
     * @return the disk cache reference
     */
    public DiskCacheReference getDiskCacheReference(CacheEntry cr)
    {
        DiskCacheReference dcr = null;
        if (cr.getCacheReference() instanceof DiskCacheReference)
        {
            dcr = (DiskCacheReference)cr.getCacheReference();
        }
        return dcr;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.impl.DefaultDirectAccessRetriever#getMapGeometrySupport(long)
     */
    @Override
    public MapGeometrySupport getMapGeometrySupport(long cacheId)
    {
        MapGeometrySupport result = null;
        CacheEntry ce = getCacheEntry(cacheId);
        if (ce != null)
        {
            result = extractMGSFromEntryIfAvailable(ce);
            if (result == null && ce.isMapGeometrySupportCached() && !myIsClosed)
            {
                result = (MapGeometrySupport)retrieve(getDiskCacheReference(ce), DiskCacheFetchType.MAP_GEOMETRY_SUPPORT);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.impl.DefaultDirectAccessRetriever#getMetaData(long)
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Object> getMetaData(long cacheId)
    {
        List<Object> result = null;
        CacheEntry ce = getCacheEntry(cacheId);
        if (ce != null)
        {
            result = extractMetaDataFromEntryIfAvailable(ce);
            if (result == null && ce.isMetaDataInfoCached() && !myIsClosed)
            {
                result = (List<Object>)retrieve(getDiskCacheReference(ce), DiskCacheFetchType.META_DATA);
            }
            result = result == null ? null
                    : DynamicEnumDecoder.decode(myDiskCacheAssistant.getDynamicEnumerationRegistry(), result);
            result = new DynamicMetaDataListViewProxy(cacheId, result, getDynamicColumnCoordinator());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.impl.DefaultDirectAccessRetriever#getOriginId(long)
     */
    @Override
    public Long getOriginId(long cacheId)
    {
        Long result = null;
        CacheEntry ce = getCacheEntry(cacheId);
        if (ce != null)
        {
            result = extractOriginIdFromEntryIfAvailable(ce);
            if (result == null && ce.isOriginIdCached() && !myIsClosed)
            {
                result = (Long)retrieve(getDiskCacheReference(ce), DiskCacheFetchType.ORIGIN_ID);
            }
        }
        return result;
    }

    /**
     * Retrieve.
     *
     * @param dcr the dcr
     * @param ft the ft
     * @return the object
     */
    public Object retrieve(DiskCacheReference dcr, DiskCacheFetchType ft)
    {
        if (myIsClosed)
        {
            return null;
        }

        Object result = null;
        myRetrieveLock.lock();
        try
        {
            RandomAccessFile raf = myInsertToRAFMap.get(Integer.valueOf(dcr.getInsertNum()));
            if (raf == null)
            {
                File cacheFile = DiskCacheAssistant.getCacheFile(myDiskCacheAssistant.getDiskCacheLocation(), myTypeId,
                        dcr.getInsertNum());
                raf = new RandomAccessFile(cacheFile, "r");
                myInsertToRAFMap.put(Integer.valueOf(dcr.getInsertNum()), raf);
            }

            if (ft == DiskCacheFetchType.META_DATA)
            {
                result = decodeMetaData(raf, dcr);
            }
            else if (ft == DiskCacheFetchType.MAP_GEOMETRY_SUPPORT)
            {
                result = decodeMGS(raf, dcr);
            }
            else if (ft == DiskCacheFetchType.ORIGIN_ID)
            {
                result = decodeOriginId(raf, dcr);
            }
        }
        catch (IOException | BadPaddingException | IllegalBlockSizeException | ShortBufferException e)
        {
            LOG.error(e);
        }
        finally
        {
            myRetrieveLock.unlock();
        }
        return result;
    }

    /**
     * Decode meta data.
     *
     * @param raf the raf
     * @param dcr the dcr
     * @return the object
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws BadPaddingException the bad padding exception
     * @throws IllegalBlockSizeException the illegal block size exception
     * @throws ShortBufferException the short buffer exception
     */
    protected Object decodeMetaData(RandomAccessFile raf, DiskCacheReference dcr)
        throws IOException, BadPaddingException, IllegalBlockSizeException, ShortBufferException
    {
        int size = dcr.getMDISize();
        int offset = dcr.getMDIOffset();
        if (myReadBuffer.length < size)
        {
            myReadBuffer = new byte[size];
        }

        raf.seek(dcr.getPosition() + offset);
        int read = raf.read(myReadBuffer, 0, size);
        if (read != size)
        {
            LOG.error("Read fewer bytes than expected, expected " + size + " got " + read);
        }

        ByteArrayInputStream elementBAIS = decodeIfNecessary(myDiskCacheAssistant.isUsingEncryption(), size);
        ObjectInputStream ois = new ObjectInputStream(elementBAIS);

        return DiskCacheDecodeHelper.decodeMetaDataList(ois);
    }

    /**
     * Decode mgs.
     *
     * @param raf the random access file on which to operate.
     * @param dcr the dcr
     * @return the object
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws BadPaddingException the bad padding exception
     * @throws IllegalBlockSizeException the illegal block size exception
     * @throws ShortBufferException the short buffer exception
     */
    protected Object decodeMGS(RandomAccessFile raf, DiskCacheReference dcr)
        throws IOException, BadPaddingException, IllegalBlockSizeException, ShortBufferException
    {
        int size = dcr.getMGSSize();
        int offset = dcr.getMGSOffset();

        if (myReadBuffer.length < size)
        {
            myReadBuffer = new byte[size];
        }

        raf.seek(dcr.getPosition() + offset);
        int read = raf.read(myReadBuffer, 0, size);
        if (read != size)
        {
            LOG.error("Read fewer bytes than expected, expected " + size + " got " + read);
        }

        ByteArrayInputStream elementBAIS = decodeIfNecessary(myDiskCacheAssistant.isUsingEncryption(), size);
        ObjectInputStream ois = new ObjectInputStream(elementBAIS);
        return DiskDecodeHelper.decodeMapGeometrySupport(ois);
    }

    /**
     * Decode origin id.
     *
     * @param raf the random access file on which to operate.
     * @param dcr the dcr
     * @return the object
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws BadPaddingException the bad padding exception
     * @throws IllegalBlockSizeException the illegal block size exception
     * @throws ShortBufferException the short buffer exception
     */
    protected Object decodeOriginId(RandomAccessFile raf, DiskCacheReference dcr)
        throws IOException, BadPaddingException, IllegalBlockSizeException, ShortBufferException
    {
        int size = dcr.getOriginIdSize();

        if (myReadBuffer.length < size)
        {
            myReadBuffer = new byte[size];
        }

        raf.seek(dcr.getPosition());
        int read = raf.read(myReadBuffer, 0, size);
        if (read != size)
        {
            LOG.error("Read fewer bytes than expected, expected " + size + " got " + read);
        }

        ByteArrayInputStream elementBAIS = decodeIfNecessary(false, size);
        ObjectInputStream ois = new ObjectInputStream(elementBAIS);
        return DiskDecodeHelper.decodeOriginId(ois);
    }

    /**
     * Gets the value of the {@link #myDiskCacheAssistant} field.
     *
     * @return the value stored in the {@link #myDiskCacheAssistant} field.
     */
    public DiskCacheAssistant getDiskCacheAssistant()
    {
        return myDiskCacheAssistant;
    }
}
