package io.opensphere.mantle.util.dynenum.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import gnu.trove.iterator.TObjectShortIterator;
import gnu.trove.map.TObjectShortMap;
import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TObjectShortHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationConstants;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationDataTypeManager;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;
import io.opensphere.mantle.util.dynenum.KeyIdExhaustionException;

/**
 * The Class DynamicEnumerationRegistryImpl.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public class DynamicEnumerationRegistryImpl implements DynamicEnumerationRegistry
{
    /** The Read write lock. */
    private final ReentrantReadWriteLock myReadWriteLock;

    /** The Type id counter. */
    private final AtomicInteger myTypeIdCounter;

    /** The Type id to data map. */
    private final TShortObjectMap<DynamicEnumerationDataTypeManager> myTypeIdToDataMap;

    /** The Type to type id map. */
    private final TObjectShortMap<String> myTypeToTypeIdMap;

    /** The Type to type id map lock. */
    private final ReentrantLock myTypeToTypeIdMapLock;

    /**
     * Instantiates a new dynamic enumeration registry impl.
     */
    public DynamicEnumerationRegistryImpl()
    {
        myTypeIdCounter = new AtomicInteger();
        myTypeToTypeIdMapLock = new ReentrantLock();
        myTypeToTypeIdMap = new TObjectShortHashMap<>();
        myTypeIdToDataMap = new TShortObjectHashMap<>();
        myReadWriteLock = new ReentrantReadWriteLock();
    }

    @Override
    public DynamicEnumerationKey addValue(String dtiKey, String metaDataKeyName, Object value)
    {
        DynamicEnumerationKey result = null;
        DynamicEnumerationDataTypeManager typeManager = getTypeManagerByDtiKey(dtiKey);
        if (typeManager != null)
        {
            result = typeManager.addValue(metaDataKeyName, value);
        }
        else
        {
            throw new IllegalArgumentException(
                    "The data type \"" + dtiKey + "\" is not managed. First create the enumeration for the type.");
        }
        return result;
    }

    @Override
    public void createEnumeration(String dtiKey, String metaDataKeyName, Class<?> keyClass)
    {
        short typeId = getTypeId(dtiKey, true);
        myReadWriteLock.writeLock().lock();
        try
        {
            DynamicEnumerationDataTypeManager typeManager = myTypeIdToDataMap.get(typeId);
            if (typeManager == null)
            {
                typeManager = new DynamicEnumerationDataTypeManagerImpl(dtiKey, typeId);
                myTypeIdToDataMap.put(typeId, typeManager);
            }
            typeManager.createEnumerationDataForMDIKeyName(metaDataKeyName, keyClass);
        }
        finally
        {
            myReadWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void destroyEnumeration(String dtiKey, String metaDataKeyName)
    {
        DynamicEnumerationDataTypeManager typeManager = getTypeManagerByDtiKey(dtiKey);
        if (typeManager != null)
        {
            typeManager.removeEnumerationDataForMdiKey(metaDataKeyName);
        }
    }

    @Override
    public void destroyEnumerations(String dtiKey)
    {
        short typeId = getTypeId(dtiKey, false);
        if (typeId != DynamicEnumerationConstants.NULL_VALUE_ID)
        {
            myReadWriteLock.writeLock().lock();
            try
            {
                myTypeIdToDataMap.remove(typeId);
            }
            finally
            {
                myReadWriteLock.writeLock().unlock();
            }
        }
    }

    @Override
    public Object getEnumerationValue(DynamicEnumerationKey key)
    {
        Utilities.checkNull(key, "key");
        return getEnumerationValue(key.getTypeId(), key.getMetaDataKeyId(), key.getValueId());
    }

    @Override
    public Object getEnumerationValue(int typeId, int mdiKeyId, int valueId)
    {
        return getEnumerationValue((short)typeId, (short)mdiKeyId, (short)valueId);
    }

    /**
     * Gets the enumeration value.
     *
     * @param typeId the type id
     * @param mdiKeyId the mdi key id
     * @param valueId the value id
     * @return the enumeration value
     */
    public Object getEnumerationValue(short typeId, short mdiKeyId, short valueId)
    {
        Object result = null;
        DynamicEnumerationDataTypeManager typeManager = getTypeManagerByTypeId(typeId);
        if (typeManager != null)
        {
            result = typeManager.getEnumerationValue(mdiKeyId, valueId);
        }
        else
        {
            throw new IllegalArgumentException(
                    "The data type id " + typeId + " is not managed. First create the enumeration for the type.");
        }
        return result;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(getClass().getSimpleName()).append("\n" + "Type to Type Id Assignments:\n");
        Map<String, Short> typeToIdMap = New.map();
        myTypeToTypeIdMapLock.lock();
        try
        {
            TObjectShortIterator<String> iter = myTypeToTypeIdMap.iterator();
            while (iter.hasNext())
            {
                iter.advance();
                typeToIdMap.put(iter.key(), iter.value());
            }
        }
        finally
        {
            myTypeToTypeIdMapLock.unlock();
        }

        List<String> dtList = New.list(typeToIdMap.keySet());
        Collections.sort(dtList);

        myReadWriteLock.readLock().lock();
        try
        {
            sb.append(String.format("  %-8s %-15s %s%n", "ACTIVE", "TYPE_ID", "DTI_KEY"));
            for (String dtKey : dtList)
            {
                Short typeId = typeToIdMap.get(dtKey);
                boolean active = myTypeIdToDataMap.containsKey(typeId.shortValue());
                sb.append(String.format("  %-8s %-15d %s%n", active ? "YES" : "", typeId, dtKey));
            }

            sb.append("\n ------------- DYNAMIC ENUMERATION TYPE CONTENTS -------------\n");
            DynamicEnumerationDataTypeManager typeManager = null;
            for (String dtKey : dtList)
            {
                Short typeId = typeToIdMap.get(dtKey);
                typeManager = myTypeIdToDataMap.get(typeId.shortValue());
                if (typeManager != null)
                {
                    sb.append(typeManager.toString());
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
     * @param dataTypeKey the data type key.
     * @return the next key id
     */
    private short getNextKeyId(String dataTypeKey)
    {
        int nextKeyId = myTypeIdCounter.incrementAndGet();
        if (nextKeyId > Short.MAX_VALUE)
        {
            nextKeyId = Short.MAX_VALUE - nextKeyId;
            if (nextKeyId < Short.MIN_VALUE)
            {
                throw new KeyIdExhaustionException(
                        "Maxmum number of key ids created for data types. Could not create id for data type \"" + dataTypeKey);
            }
        }
        return (short)nextKeyId;
    }

    /**
     * Gets the type id.
     *
     * @param dtiKey the dti key
     * @param addIfNotFound the add if not found
     * @return the type id ( or DynamicEnumerationConstants.NULL_VALUE_ID if not
     *         found)
     */
    private short getTypeId(String dtiKey, boolean addIfNotFound)
    {
        short id = DynamicEnumerationConstants.NULL_VALUE_ID;
        myTypeToTypeIdMapLock.lock();
        try
        {
            short qId = myTypeToTypeIdMap.get(dtiKey);
            if (qId == 0 && addIfNotFound)
            {
                qId = getNextKeyId(dtiKey);
                myTypeToTypeIdMap.put(dtiKey, qId);
            }
            if (qId != 0)
            {
                id = qId;
            }
        }
        finally
        {
            myTypeToTypeIdMapLock.unlock();
        }
        return id;
    }

    /**
     * Gets the type manager by dti key.
     *
     * @param dtiKey the dti key
     * @return the type manager by dti key
     */
    private DynamicEnumerationDataTypeManager getTypeManagerByDtiKey(String dtiKey)
    {
        return getTypeManagerByTypeId(getTypeId(dtiKey, false));
    }

    /**
     * Gets the type manager by type id.
     *
     * @param typeId the type id
     * @return the type manager by type id
     */
    private DynamicEnumerationDataTypeManager getTypeManagerByTypeId(short typeId)
    {
        DynamicEnumerationDataTypeManager typeManager = null;
        if (typeId != DynamicEnumerationConstants.NULL_VALUE_ID)
        {
            myReadWriteLock.readLock().lock();
            try
            {
                typeManager = myTypeIdToDataMap.get(typeId);
            }
            finally
            {
                myReadWriteLock.readLock().unlock();
            }
        }
        return typeManager;
    }
}
