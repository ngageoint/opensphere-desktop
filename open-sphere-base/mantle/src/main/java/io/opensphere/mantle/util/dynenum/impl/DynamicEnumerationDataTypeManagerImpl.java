package io.opensphere.mantle.util.dynenum.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import gnu.trove.map.TObjectShortMap;
import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TObjectShortHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationConstants;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationDataTypeManager;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationMDIColumnData;
import io.opensphere.mantle.util.dynenum.KeyIdExhaustionException;

/**
 * The Class DynamicEnumerationTypeManagerImpl.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public class DynamicEnumerationDataTypeManagerImpl implements DynamicEnumerationDataTypeManager
{
    /** The Data type key. */
    private final String myDataTypeKey;

    /** The Type id counter. */
    private final AtomicInteger myMdiKeyIdCounter = new AtomicInteger();

    /** The Value id to value map. */
    private final TShortObjectMap<DynamicEnumerationMDIColumnData> myMdiKeyIdToDataSetMap;

    /** The Mdi key to mdi key id map. */
    private final TObjectShortMap<String> myMdiKeyToMdiKeyIdMap;

    /** The Read write lock. */
    private final ReentrantReadWriteLock myReadWriteLock;

    /** The Type id. */
    private final short myTypeId;

    /**
     * Instantiates a new dynamic enumeration type manager impl.
     *
     * @param dtiKey the dti key
     * @param typeId the type id
     */
    public DynamicEnumerationDataTypeManagerImpl(String dtiKey, short typeId)
    {
        myTypeId = typeId;
        myDataTypeKey = dtiKey;
        myReadWriteLock = new ReentrantReadWriteLock();
        myMdiKeyToMdiKeyIdMap = new TObjectShortHashMap<>();
        myMdiKeyIdToDataSetMap = new TShortObjectHashMap<>();
    }

    @Override
    public DynamicEnumerationKey addValue(String metaDataKeyName, Object value)
    {
        DynamicEnumerationMDIColumnData data = getEnumerationDataForMDIKeyName(metaDataKeyName);
        if (data != null)
        {
            return data.addValue(value);
        }
        else
        {
            throw new IllegalArgumentException("The meta data key \"" + metaDataKeyName + "\" for data type \"" + myDataTypeKey
                    + "\" currently is not managed.  First create the type.");
        }
    }

    @Override
    public DynamicEnumerationMDIColumnData createEnumerationDataForMDIKeyName(String keyName, Class<?> keyClass)
    {
        DynamicEnumerationMDIColumnData result = null;
        myReadWriteLock.writeLock().lock();
        try
        {
            short mdiKeyId = DynamicEnumerationConstants.NULL_VALUE_ID;
            if (!myMdiKeyToMdiKeyIdMap.containsKey(keyName))
            {
                mdiKeyId = getNextKeyId(keyName);
                myMdiKeyToMdiKeyIdMap.put(keyName, mdiKeyId);
            }
            result = myMdiKeyIdToDataSetMap.get(mdiKeyId);
            if (result == null)
            {
                result = new DynamicEnumerationMDIColumnDataImpl(myDataTypeKey, myTypeId, keyName, mdiKeyId, keyClass);
                myMdiKeyIdToDataSetMap.put(mdiKeyId, result);
            }
        }
        finally
        {
            myReadWriteLock.writeLock().unlock();
        }
        return result;
    }

    @Override
    public DynamicEnumerationMDIColumnData getEnumerationDataForMDIKeyId(short mdiKeyId)
    {
        DynamicEnumerationMDIColumnData result = null;
        myReadWriteLock.readLock().lock();
        try
        {
            result = myMdiKeyIdToDataSetMap.get(mdiKeyId);
        }
        finally
        {
            myReadWriteLock.readLock().unlock();
        }
        return result;
    }

    @Override
    public DynamicEnumerationMDIColumnData getEnumerationDataForMDIKeyName(String keyName)
    {
        if (myMdiKeyToMdiKeyIdMap.containsKey(keyName))
        {
            return getEnumerationDataForMDIKeyId(myMdiKeyToMdiKeyIdMap.get(keyName));
        }
        else
        {
            return null;
        }
    }

    @Override
    public Object getEnumerationValue(DynamicEnumerationKey key)
    {
        if (key != null && key.getTypeId() == myTypeId)
        {
            DynamicEnumerationMDIColumnData data = getEnumerationDataForMDIKeyId(key.getMetaDataKeyId());
            if (data != null)
            {
                return data.getEnumerationValue(key.getValueId());
            }
        }
        return null;
    }

    @Override
    public Object getEnumerationValue(short mdikeyId, short valueId)
    {
        DynamicEnumerationMDIColumnData data = getEnumerationDataForMDIKeyId(mdikeyId);
        if (data != null)
        {
            return data.getEnumerationValue(valueId);
        }
        return null;
    }

    @Override
    public void removeAllData()
    {
        myReadWriteLock.writeLock().lock();
        try
        {
            myMdiKeyIdToDataSetMap.clear();
        }
        finally
        {
            myReadWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void removeEnumerationDataForMdiKey(String keyName)
    {
        if (myMdiKeyToMdiKeyIdMap.containsKey(keyName))
        {
            removeEnumerationDataForMdiKeyId(myMdiKeyToMdiKeyIdMap.get(keyName));
        }
    }

    @Override
    public void removeEnumerationDataForMdiKeyId(short mdiKeyId)
    {
        myReadWriteLock.writeLock().lock();
        try
        {
            myMdiKeyIdToDataSetMap.remove(mdiKeyId);
        }
        finally
        {
            myReadWriteLock.writeLock().unlock();
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(64);
        sb.append(getClass().getSimpleName()).append(" DataType: ").append(myDataTypeKey).append(" ID: ").append(myTypeId)
                .append("\n" + "Total Keys: ").append(myMdiKeyIdCounter.get()).append("\n" + " ----- Content Report -----\n");
        myReadWriteLock.readLock().lock();
        try
        {
            List<String> mdiKeyList = New.list(myMdiKeyToMdiKeyIdMap.keySet());
            Collections.sort(mdiKeyList);
            for (String key : mdiKeyList)
            {
                short mdiKeyId = myMdiKeyToMdiKeyIdMap.get(key);
                DynamicEnumerationMDIColumnData keyData = myMdiKeyIdToDataSetMap.get(mdiKeyId);
                if (keyData != null)
                {
                    sb.append(keyData.toString());
                }
            }
        }
        finally
        {
            myReadWriteLock.readLock().unlock();
        }

        return sb.toString();
    }

    /**
     * Gets the next key id, if the number of keys exceeds the maximum,
     * Short.MAX_VALUE will throw a {@link KeyIdExhaustionException}.
     *
     * @param keyName the key name
     * @return the next key id
     */
    private short getNextKeyId(String keyName)
    {
        int nextKeyId = myMdiKeyIdCounter.incrementAndGet();
        if (nextKeyId > Short.MAX_VALUE)
        {
            nextKeyId = Short.MAX_VALUE - nextKeyId;
            if (nextKeyId < Short.MIN_VALUE)
            {
                throw new KeyIdExhaustionException("Maximum number of key ids created for data type " + myDataTypeKey
                        + ". Could not create id for key \"" + keyName);
            }
        }
        return (short)nextKeyId;
    }
}
