package io.opensphere.mantle.util.proxypool;

import java.util.concurrent.locks.ReentrantLock;

import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;

/**
 * A utility class that stores the reference to an object and returns a code for
 * that object which can be used to look up the original object.
 *
 * Uses a byte for the code.
 *
 * This should be used if many (preferably immutable) instances of the same
 * object would need to be stored in a composite class, this can be used to
 * reduce the memory required.
 *
 * Note that a null value may be used, but that the code for a null is always
 * -1.
 *
 * Example would be for class associations.
 *
 * @param <T> the generic type
 */
public class ByteTypeProxyPool<T>
{
    /** The Constant NULL_TYPE_CODE. */
    public static final byte NULL_TYPE_CODE = -1;

    /** The Code to type map. */
    private final TByteObjectMap<T> myCodeToTypeMap;

    /** The Type counter. */
    private byte myCounter;

    /** The Read write lock. */
    private final ReentrantLock myPoolLock;

    /** The Type to type code map. */
    private final TObjectByteHashMap<T> myTypeToCodeMap;

    /**
     * Instantiates a new byte type encode decode pool.
     */
    public ByteTypeProxyPool()
    {
        myPoolLock = new ReentrantLock();
        myCodeToTypeMap = new TByteObjectHashMap<>();
        myTypeToCodeMap = new TObjectByteHashMap<>();
    }

    /**
     * Gets the code for type, if type is not already in the pool it will be
     * added to the pool unless the add will exceed the maximum pool size. 2^8 -
     * 2 items. Code 0 is reserved, and -1 represents null.
     *
     * @param type the type
     * @return the code for type
     * @throws MaxPoolSizeExceededException If there are too many types in the
     *             pool.
     */
    public byte getCodeForType(T type) throws MaxPoolSizeExceededException
    {
        if (type == null)
        {
            return NULL_TYPE_CODE;
        }
        byte result = 0;
        myPoolLock.lock();
        try
        {
            result = myTypeToCodeMap.get(type);
            if (result == 0) // Not found.
            {
                int nextCount = myCounter + 1;
                if (nextCount > Byte.MAX_VALUE)
                {
                    nextCount = Byte.MIN_VALUE;
                }
                if (nextCount == NULL_TYPE_CODE)
                {
                    throw new MaxPoolSizeExceededException(
                            "Could not insert item, max pool size of " + myCodeToTypeMap.size() + " would be exceeded.");
                }
                myCounter = (byte)nextCount;
                result = myCounter;
                myCodeToTypeMap.put(myCounter, type);
                myTypeToCodeMap.put(type, myCounter);
            }
        }
        finally
        {
            myPoolLock.unlock();
        }
        return result;
    }

    /**
     * Gets the type from the pool for the given code, null if not in pool.
     *
     * @param code the code
     * @return the type from code
     */
    public T getTypeFromCode(byte code)
    {
        if (code == NULL_TYPE_CODE)
        {
            return null;
        }
        T result = null;
        if (code != NULL_TYPE_CODE)
        {
            myPoolLock.lock();
            try
            {
                result = myCodeToTypeMap.get(code);
            }
            finally
            {
                myPoolLock.unlock();
            }
        }
        return result;
    }

    /**
     * Reset pool.
     */
    public void resetPool()
    {
        myPoolLock.lock();
        try
        {
            myCounter = 0;
            myCodeToTypeMap.clear();
            myTypeToCodeMap.clear();
        }
        finally
        {
            myPoolLock.unlock();
        }
    }

    /**
     * The number of types stored in the pool.
     *
     * @return the size.
     */
    public int size()
    {
        int size = 0;
        myPoolLock.lock();
        try
        {
            size = myTypeToCodeMap.size();
        }
        finally
        {
            myPoolLock.unlock();
        }
        return size;
    }
}
