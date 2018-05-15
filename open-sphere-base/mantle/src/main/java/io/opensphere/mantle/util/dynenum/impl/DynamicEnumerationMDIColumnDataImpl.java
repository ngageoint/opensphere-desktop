package io.opensphere.mantle.util.dynenum.impl;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import gnu.trove.procedure.TShortObjectProcedure;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationConstants;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationMDIColumnData;
import io.opensphere.mantle.util.dynenum.KeyIdExhaustionException;

/**
 * The Class DynamicEnumerationMDIColumnDataImpl.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public class DynamicEnumerationMDIColumnDataImpl implements DynamicEnumerationMDIColumnData
{
    /** The Data type key. */
    private final String myDataTypeKey;

    /** The Key class. */
    private final Class<?> myKeyClass;

    /** The Meta data key id. */
    private final short myMetaDataKeyId;

    /** The Meta data key name. */
    private final String myMetaDataKeyName;

    /** The Read write lock. */
    private final ReentrantReadWriteLock myReadWriteLock;

    /** The Type id. */
    private final short myTypeId;

    /** The Value id counter. */
    private AtomicInteger myValueIdCounter;

    /** The Value id to value map. */
    private final TShortObjectMap<Object> myValueIdToValueMap;

    /**
     * Instantiates a new dynamic enumeration data impl.
     *
     * @param dtiKey the dti key
     * @param typeId the type id
     * @param metaDataKeyName the meta data key name
     * @param metaDataKeyId the meta data key id
     * @param keyClass the key class
     */
    public DynamicEnumerationMDIColumnDataImpl(String dtiKey, short typeId, String metaDataKeyName, short metaDataKeyId,
            Class<?> keyClass)
    {
        myTypeId = typeId;
        myDataTypeKey = dtiKey;
        myMetaDataKeyName = metaDataKeyName;
        myMetaDataKeyId = metaDataKeyId;
        myKeyClass = keyClass;
        myReadWriteLock = new ReentrantReadWriteLock();
        myValueIdToValueMap = new TShortObjectHashMap<>();
        myValueIdCounter = new AtomicInteger();
    }

    @Override
    public DynamicEnumerationKey addValue(Object value)
    {
        DynamicEnumerationKey result = null;

        if (value != null)
        {
            myReadWriteLock.writeLock().lock();
            try
            {
                FindIdByValueProcedure<Object> procedure = new FindIdByValueProcedure<>(value);
                myValueIdToValueMap.forEachEntry(procedure);

                if (procedure.found())
                {
                    result = DynamicEnumerationKeyFactory.createKey(myTypeId, myMetaDataKeyId, procedure.valueId());
                }
                else
                {
                    short newId = getNextKeyId(value);
                    myValueIdToValueMap.put(newId, value);
                    result = DynamicEnumerationKeyFactory.createKey(myTypeId, myMetaDataKeyId, newId);
                }
            }
            finally
            {
                myReadWriteLock.writeLock().unlock();
            }
        }
        else
        {
            result = DynamicEnumerationKeyFactory.createKey(myTypeId, myMetaDataKeyId, DynamicEnumerationConstants.NULL_VALUE_ID);
        }
        return result;
    }

    @Override
    public void clearAll()
    {
        myReadWriteLock.writeLock().lock();
        try
        {
            myValueIdToValueMap.clear();
            myValueIdCounter = new AtomicInteger(Short.MIN_VALUE);
        }
        finally
        {
            myReadWriteLock.writeLock().unlock();
        }
    }

    @Override
    public String getDataTypeKey()
    {
        return myDataTypeKey;
    }

    @Override
    public Object getEnumerationValue(DynamicEnumerationKey key)
    {
        Object result = null;
        if (key != null && key.getTypeId() == myTypeId && key.getMetaDataKeyId() == myMetaDataKeyId)
        {
            result = getEnumerationValue(key.getValueId());
        }
        return result;
    }

    @Override
    public Object getEnumerationValue(short valueId)
    {
        Object result = null;
        myReadWriteLock.readLock().lock();
        try
        {
            result = myValueIdToValueMap.get(valueId);
        }
        finally
        {
            myReadWriteLock.readLock().unlock();
        }
        return result;
    }

    @Override
    public DynamicEnumerationKey getKey(short valueId)
    {
        int current = myValueIdCounter.get();
        if (valueId < 1 || valueId > current)
        {
            return null;
        }
        else
        {
            return DynamicEnumerationKeyFactory.createKey(myTypeId, myMetaDataKeyId, valueId);
        }
    }

    @Override
    public Class<?> getKeyClass()
    {
        return myKeyClass;
    }

    @Override
    public int getMetaDataKeyId()
    {
        return myMetaDataKeyId;
    }

    @Override
    public String getMetaDataKeyName()
    {
        return myMetaDataKeyName;
    }

    @Override
    public int getTypeId()
    {
        return myTypeId;
    }

    @Override
    public int getValueCount()
    {
        return myValueIdCounter.get();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(getClass().getSimpleName()).append(" MDIKeyName: ").append(myMetaDataKeyName).append(" ID: ")
                .append(myMetaDataKeyId).append("\n" + "    Value Class: ")
                .append(myKeyClass == null ? "?" : myKeyClass.getSimpleName()).append("\n" + "    Value Set: Count: ")
                .append(getValueCount()).append('\n');
        sb.append(String.format("          %-15s %-30s Value%n", "Id", "Class"));
        myReadWriteLock.readLock().lock();
        try
        {
            short[] keys = myValueIdToValueMap.keys();
            Arrays.sort(keys);
            for (short key : keys)
            {
                Object value = myValueIdToValueMap.get(key);
                String classStr = value == null ? "?" : value.getClass().getSimpleName();
                String valueStr = value == null ? "NULL" : value.toString();
                sb.append(String.format("          %-15s %-30s ", Short.valueOf(key), classStr)).append(valueStr).append('\n');
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
     * @param value the value for which a key id is to be created.
     * @return the next key id
     */
    private short getNextKeyId(Object value)
    {
        int nextKeyId = myValueIdCounter.incrementAndGet();
        if (nextKeyId > Short.MAX_VALUE)
        {
            nextKeyId = Short.MAX_VALUE - nextKeyId;
            if (nextKeyId < Short.MIN_VALUE)
            {
                throw new KeyIdExhaustionException("Maximum number of key ids created for data type " + myDataTypeKey + " Column "
                        + myMetaDataKeyName + ". Could not create id for value \"" + value);
            }
        }
        return (short)nextKeyId;
    }

    /**
     * The Class FindIdByValueProcedure.
     *
     * @param <T> the generic type
     */
    private static class FindIdByValueProcedure<T> implements TShortObjectProcedure<T>
    {
        /** The Found. */
        private boolean myFound;

        /** The Search value. */
        private final T mySearchValue;

        /** The Value id. */
        private short myValueId;

        /**
         * Instantiates a new find id by value procedure.
         *
         * @param searchValue the search value
         */
        public FindIdByValueProcedure(T searchValue)
        {
            mySearchValue = searchValue;
        }

        @Override
        public boolean execute(short id, T value)
        {
            if (EqualsHelper.equals(mySearchValue, value))
            {
                myFound = true;
                myValueId = id;
                return false;
            }
            return true;
        }

        /**
         * Found.
         *
         * @return true, if successful
         */
        public boolean found()
        {
            return myFound;
        }

        /**
         * Value id.
         *
         * @return the short
         */
        public short valueId()
        {
            return myValueId;
        }
    }
}
