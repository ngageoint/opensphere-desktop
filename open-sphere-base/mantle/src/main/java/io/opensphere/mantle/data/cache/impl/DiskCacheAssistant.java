package io.opensphere.mantle.data.cache.impl;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongFunction;

import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.filesystem.FileUtilities;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.security.CipherFactory;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.CacheQuery;
import io.opensphere.mantle.data.cache.DirectAccessRetriever;
import io.opensphere.mantle.data.cache.Priority;
import io.opensphere.mantle.data.dynmeta.impl.DynamicMetadataManagerImpl;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * The Class DiskCacheAssistant.
 */
public class DiskCacheAssistant implements CacheAssistant
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DiskCacheAssistant.class);

    /** The Constant MAX_READ_THREADS. */
    private static final int MAX_READ_THREADS = Runtime.getRuntime().availableProcessors() > 4 ? 4
            : Runtime.getRuntime().availableProcessors();

    /** The Constant MAX_RECORDS_PER_FILE. */
    private static final int MAX_RECORDS_PER_FILE = 20000;

    /** The Constant MAX_WRITE_THREADS. */
    private static final int MAX_WRITE_THREADS = Runtime.getRuntime().availableProcessors() > 4 ? 4
            : Runtime.getRuntime().availableProcessors();

    /** The Constant ourDiskReadExecutorService. */
    protected static final ThreadPoolExecutor DISK_READ_EXECUTOR_SERVICE = new ThreadPoolExecutor(MAX_READ_THREADS,
            MAX_READ_THREADS, 20, TimeUnit.SECONDS, new PriorityBlockingQueue<>(10, new RunnablePriorityComparator()),
            new NamedThreadFactory("DataElementCache:DiskReadWorker"));

    /** The Constant ourDiskWriteExecutorService. */
    protected static final ThreadPoolExecutor DISK_WRITE_EXECUTOR_SERVICE = new ThreadPoolExecutor(MAX_WRITE_THREADS,
            MAX_WRITE_THREADS, 20, TimeUnit.SECONDS, new PriorityBlockingQueue<>(10, new RunnablePriorityComparator()),
            new NamedThreadFactory("DataElementCache:DiskWriteWorker"));

    /** Cipher factory for encryption/decryption. */
    private CipherFactory myCipherFactory;

    /** The data element cache. */
    private final DataElementCacheImpl myDataElementCache;

    /** The disk cache location. */
    private final File myDiskCacheLocation;

    /** The Dynamic column manager. */
    private final DynamicMetadataManagerImpl myDynamicColumnManager;

    /** The Dynamic enumeration registry. */
    private final DynamicEnumerationRegistry myDynamicEnumerationRegistry;

    /** The my type id counter. */
    private final AtomicInteger myTypeIdCounter;

    /** The my type id to type insert counter map. */
    private final Map<Integer, AtomicInteger> myTypeIdToTypeInsertCounterMap;

    /** The my type id to type map. */
    private final Map<Integer, String> myTypeIdToTypeMap;

    /** The my type to direct retriever map. */
    private final Map<String, Set<DiskCacheDirectAccessRetriever>> myTypeToDirectRetrieverMap;

    /** The my type to type id map. */
    private final Map<String, Integer> myTypeToTypeIdMap;

    /** The my use encryption. */
    private final boolean myUseEncryption;

    static
    {
        DISK_READ_EXECUTOR_SERVICE.allowCoreThreadTimeOut(true);
        DISK_WRITE_EXECUTOR_SERVICE.allowCoreThreadTimeOut(true);
    }

    /**
     * Instantiates a new disk cache assistant.
     *
     * @param tb the tb
     * @param dec the dec
     * @param dcMan the dc man
     * @param deReg the de reg
     * @param diskCacheLocation the disk cache location
     * @param useEncryption the use encryption
     */
    public DiskCacheAssistant(Toolbox tb, DataElementCacheImpl dec, DynamicMetadataManagerImpl dcMan,
            DynamicEnumerationRegistry deReg, File diskCacheLocation, boolean useEncryption)
    {
        myDynamicColumnManager = dcMan;
        myDynamicEnumerationRegistry = deReg;
        myTypeToDirectRetrieverMap = new HashMap<>();
        myDataElementCache = dec;
        myDiskCacheLocation = diskCacheLocation;
        FileUtilities.deleteDirRecursive(myDiskCacheLocation);
        if (!myDiskCacheLocation.mkdirs())
        {
            LOGGER.error("Failed to make data element disk cache location: " + myDiskCacheLocation.getAbsolutePath());
        }
        myUseEncryption = useEncryption;
        if (myUseEncryption)
        {
            myCipherFactory = tb.getSecurityManager().getCipherFactory();
        }

        myTypeIdToTypeInsertCounterMap = new HashMap<>();
        myTypeToTypeIdMap = new HashMap<>();
        myTypeIdToTypeMap = new HashMap<>();
        myTypeIdCounter = new AtomicInteger(0);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtilities.deleteDirRecursive(myDiskCacheLocation)));
    }

    /**
     * Gets the cache file.
     *
     * @param cacheLocation the cache location
     * @param typeId the type id
     * @param insertNumber the insert number
     * @return the cache file
     */
    static File getCacheFile(File cacheLocation, int typeId, int insertNumber)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(cacheLocation.getAbsolutePath()).append(File.separator).append(typeId).append(File.separator).append("data_")
                .append(insertNumber).append(".dat");
        return new File(sb.toString());
    }

    /**
     * Gets the cache file.
     *
     * @param cacheLocation the cache location
     * @param typeId the type id
     * @return the cache file
     */
    private static File getCacheTypeDir(File cacheLocation, int typeId)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(cacheLocation.getAbsolutePath()).append(File.separator).append(typeId);
        return new File(sb.toString());
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.impl.CacheAssistant#cacheElement(java.lang.String,
     *      java.lang.String, long, io.opensphere.mantle.data.DataTypeInfo,
     *      io.opensphere.mantle.data.cache.impl.CacheEntry)
     */
    @Override
    public void cacheElement(String source, String category, long id, DataTypeInfo type, CacheEntry ce)
    {
        int typeId = getTypeId(type);
        int insertNum = getNextInsertId(typeId);
        LinkedList<CacheEntry> itemList = new LinkedList<>();
        itemList.add(ce);
        DISK_WRITE_EXECUTOR_SERVICE
                .execute(new AddElementsWorker(this, typeId, insertNum, new TLongArrayList(new long[] { id }), itemList));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.impl.CacheAssistant#cacheElements(java.lang.String,
     *      java.lang.String, gnu.trove.list.TLongList,
     *      io.opensphere.mantle.data.DataTypeInfo, java.util.LinkedList)
     */
    @Override
    public void cacheElements(String source, String category, TLongList ids, DataTypeInfo type, LinkedList<CacheEntry> ceList)
    {
        int typeId = getTypeId(type);
        int insertNum = -1;
        TLongList idInsertList = new TLongArrayList();
        LinkedList<CacheEntry> deInsertList = new LinkedList<>();
        TLongIterator idItr = ids.iterator();
        Iterator<CacheEntry> ceItr = ceList.iterator();
        while (idItr.hasNext())
        {
            idInsertList.add(idItr.next());
            deInsertList.add(ceItr.next());
            ceItr.remove();
            if (idInsertList.size() == MAX_RECORDS_PER_FILE)
            {
                insertNum = getNextInsertId(typeId);
                DISK_WRITE_EXECUTOR_SERVICE.submit(new AddElementsWorker(this, typeId, insertNum, idInsertList, deInsertList));
                idInsertList = new TLongArrayList();
                deInsertList = new LinkedList<>();
            }
        }
        if (!idInsertList.isEmpty())
        {
            insertNum = getNextInsertId(typeId);
            DISK_WRITE_EXECUTOR_SERVICE.submit(new AddElementsWorker(this, typeId, insertNum, idInsertList, deInsertList));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.impl.CacheAssistant#dataTypeRemoved(io.opensphere.mantle.data.DataTypeInfo)
     */
    @Override
    public void dataTypeRemoved(final DataTypeInfo dti)
    {
        // First close out all the retrievers.
        synchronized (myTypeToDirectRetrieverMap)
        {
            Set<DiskCacheDirectAccessRetriever> retrieverSetForType = myTypeToDirectRetrieverMap.get(dti.getTypeKey());
            if (retrieverSetForType != null && !retrieverSetForType.isEmpty())
            {
                for (DiskCacheDirectAccessRetriever retriever : retrieverSetForType)
                {
                    retriever.close();
                }
                retrieverSetForType.clear();
            }
        }

        final int typeId = getTypeId(dti.getTypeKey());
        final File typeParentDir = getCacheTypeDir(myDiskCacheLocation, typeId);
        DISK_WRITE_EXECUTOR_SERVICE.execute(new Priority()
        {
            @Override
            public int getPriority()
            {
                return 5;
            }

            @Override
            public void run()
            {
                FileUtilities.deleteDirRecursive(typeParentDir);
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Data For Type " + dti.getTypeKey() + " Delete Complete at : " + System.currentTimeMillis());
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.impl.CacheAssistant#getDirectAccessRetriever(DataTypeInfo,
     *      LongFunction, DynamicMetadataManagerImpl)
     */
    @Override
    public DirectAccessRetriever getDirectAccessRetriever(DataTypeInfo dti, LongFunction<CacheEntry> cacheRefMap,
            DynamicMetadataManagerImpl dcm)
    {
        DiskCacheDirectAccessRetriever retriever = new DiskCacheDirectAccessRetriever(this, dti, cacheRefMap, dcm);
        synchronized (myTypeToDirectRetrieverMap)
        {
            Set<DiskCacheDirectAccessRetriever> retrieverSetForType = myTypeToDirectRetrieverMap.get(dti.getTypeKey());
            if (retrieverSetForType == null)
            {
                retrieverSetForType = new HashSet<>();
                myTypeToDirectRetrieverMap.put(dti.getTypeKey(), retrieverSetForType);
            }
            retrieverSetForType.add(retriever);
        }
        return retriever;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.impl.CacheAssistant#getPreferredInsertBlockSize()
     */
    @Override
    public int getPreferredInsertBlockSize()
    {
        return MAX_RECORDS_PER_FILE;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.impl.CacheAssistant#removeElement(long,
     *      io.opensphere.mantle.data.cache.impl.CacheReference)
     */
    @Override
    public void removeElement(long cacheId, CacheReference ref)
    {
        // For now don't do anything.
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.impl.CacheAssistant#removeElements(java.util.List,
     *      java.util.List)
     */
    @Override
    public void removeElements(List<Long> cacheIds, List<CacheReference> refs)
    {
        // For now don't do anything.
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.cache.impl.CacheAssistant#retrieveAndUpdateElementCacheEntries(io.opensphere.mantle.data.cache.CacheQuery,
     *      java.util.List, java.util.List, boolean)
     */
    @Override
    public void retrieveAndUpdateElementCacheEntries(CacheQuery query, List<Long> cacheIds, List<CacheEntry> entries,
            boolean updateEntries)
    {
        // Organize the retrieve by data type, insert number (file), and the
        // list to be retrieved.
        Map<Integer, Map<Integer, List<Pair<Long, CacheEntry>>>> retrieveMap = new HashMap<>();
        Iterator<Long> idItr = cacheIds.iterator();
        Long curId = null;
        DiskCacheReference dcr = null;
        for (CacheEntry ce : entries)
        {
            Integer typeId = Integer.valueOf(getTypeId(ce.getDataTypeKey()));
            curId = idItr.next();
            if (ce.getCacheReference() instanceof DiskCacheReference)
            {
                dcr = (DiskCacheReference)ce.getCacheReference();
                Integer insertNum = Integer.valueOf(dcr.getInsertNum());

                Map<Integer, List<Pair<Long, CacheEntry>>> insertMap = retrieveMap.get(typeId);
                if (insertMap == null)
                {
                    insertMap = new HashMap<>();
                    retrieveMap.put(typeId, insertMap);
                }
                List<Pair<Long, CacheEntry>> entryList = insertMap.get(insertNum);
                if (entryList == null)
                {
                    entryList = new LinkedList<>();
                    insertMap.put(insertNum, entryList);
                }

                entryList.add(new Pair<>(curId, ce));
            }
        }

        // Launch all the retrieve tasks by data type and insert. ( one task per
        // file )
        List<Future<?>> futureList = new LinkedList<>();
        for (Map.Entry<Integer, Map<Integer, List<Pair<Long, CacheEntry>>>> typeEntry : retrieveMap.entrySet())
        {
            for (Map.Entry<Integer, List<Pair<Long, CacheEntry>>> insertEntry : typeEntry.getValue().entrySet())
            {
                futureList.add(DISK_READ_EXECUTOR_SERVICE.submit(new RetrieveAndUpdateWorker(this, typeEntry.getKey().intValue(),
                        insertEntry.getKey().intValue(), query, insertEntry.getValue(), updateEntries)));
            }
        }
        retrieveMap.clear();
        retrieveMap = null;

        // Wait for all the retrieve tasks to complete.
        for (Future<?> aFuture : futureList)
        {
            try
            {
                aFuture.get();
            }
            catch (InterruptedException | ExecutionException e)
            {
                LOGGER.error(e);
            }
        }
    }

    /**
     * Gets the secret key spec.
     *
     * @param encryptionPassword the encryption password
     * @return the secret key spec
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    protected final SecretKeySpec getSecretKeySpec(char[] encryptionPassword) throws NoSuchAlgorithmException
    {
        byte[] encryptionPasswordBytes = new byte[encryptionPassword.length];
        for (int ii = 0; ii < encryptionPassword.length; ii++)
        {
            encryptionPasswordBytes[ii] = (byte)encryptionPassword[ii];
        }
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(encryptionPasswordBytes, 0, encryptionPasswordBytes.length);

        SecretKeySpec secretKeySpec = new SecretKeySpec(digest.digest(), "AES");
        return secretKeySpec;
    }

    /**
     * Gets the next insert id for a data type.
     *
     * @param typeId the type id
     * @return the next insert id
     */
    private int getNextInsertId(int typeId)
    {
        Integer insertId;
        synchronized (myTypeIdToTypeInsertCounterMap)
        {
            AtomicInteger ai = myTypeIdToTypeInsertCounterMap.get(Integer.valueOf(typeId));
            if (ai == null)
            {
                ai = new AtomicInteger(0);
                myTypeIdToTypeInsertCounterMap.put(Integer.valueOf(typeId), ai);
            }
            insertId = Integer.valueOf(ai.incrementAndGet());
        }
        return insertId.intValue();
    }

    /**
     * Gets the type id.
     *
     * @param dti the dti
     * @return the type id
     */
    int getTypeId(DataTypeInfo dti)
    {
        return getTypeId(dti.getTypeKey());
    }

    /**
     * Gets the type id.
     *
     * @param dtiKey the dti key
     * @return the type id
     */
    private int getTypeId(String dtiKey)
    {
        Integer id = null;
        synchronized (myTypeToTypeIdMap)
        {
            id = myTypeToTypeIdMap.get(dtiKey);
            if (id == null)
            {
                id = Integer.valueOf(myTypeIdCounter.incrementAndGet());
                myTypeToTypeIdMap.put(dtiKey, id);
                myTypeIdToTypeMap.put(id, dtiKey);
            }
        }
        return id.intValue();
    }

    /**
     * Gets the value of the {@link #myCipherFactory} field.
     *
     * @return the value stored in the {@link #myCipherFactory} field.
     */
    public CipherFactory getCipherFactory()
    {
        return myCipherFactory;
    }

    /**
     * Gets the value of the {@link #myDynamicEnumerationRegistry} field.
     *
     * @return the value stored in the {@link #myDynamicEnumerationRegistry}
     *         field.
     */
    public DynamicEnumerationRegistry getDynamicEnumerationRegistry()
    {
        return myDynamicEnumerationRegistry;
    }

    /**
     * Gets the value of the {@link #myDiskCacheLocation} field.
     *
     * @return the value stored in the {@link #myDiskCacheLocation} field.
     */
    public File getDiskCacheLocation()
    {
        return myDiskCacheLocation;
    }

    /**
     * Gets the value of the {@link #myDataElementCache} field.
     *
     * @return the value stored in the {@link #myDataElementCache} field.
     */
    public DataElementCacheImpl getDataElementCache()
    {
        return myDataElementCache;
    }

    /**
     * Gets the value of the {@link #myDynamicColumnManager} field.
     *
     * @return the value stored in the {@link #myDynamicColumnManager} field.
     */
    public DynamicMetadataManagerImpl getDynamicColumnManager()
    {
        return myDynamicColumnManager;
    }

    /**
     * Gets the value of the {@link #myUseEncryption} field.
     *
     * @return the value stored in the {@link #myUseEncryption} field.
     */
    public boolean isUsingEncryption()
    {
        return myUseEncryption;
    }
}
