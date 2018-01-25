package io.opensphere.mantle.util.proxypool;

import java.util.concurrent.locks.ReentrantLock;

import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TObjectShortHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;

/**
 * A utility class that stores the reference to an object and returns a code for
 * that object which can be used to look up the original object.
 *
 * Uses a short for the code.
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
@SuppressWarnings("PMD.AvoidUsingShortType")
public class ShortTypeProxyPool<T>
{
    /** The Constant NULL_TYPE_CODE. */
    public static final short NULL_TYPE_CODE = -1;

    /** The Code to type map. */
    private final TShortObjectMap<T> myCodeToTypeMap;

    /** The Read write lock. */
    private final ReentrantLock myLock;

    /** The Type counter. */
    private short myTypeCounter;

    /** The Type to type code map. */
    private final TObjectShortHashMap<T> myTypeToCodeMap;

    /**
     * Instantiates a new short type encode decode pool.
     */
    public ShortTypeProxyPool()
    {
        myLock = new ReentrantLock();
        myCodeToTypeMap = new TShortObjectHashMap<>();
        myTypeToCodeMap = new TObjectShortHashMap<>();
    }

    /**
     * Gets the code for type, if type is not already in the pool it will be
     * added to the pool unless the add will exceed the maximum pool size. 2^16
     * - 2 items. Code 0 is reserved, and -1 represents null.
     *
     * @param type the type
     * @return the code for type
     * @throws MaxPoolSizeExceededException If there are too many types in the
     *             pool.
     */
    public short getCodeForType(T type) throws MaxPoolSizeExceededException
    {
        if (type == null)
        {
            return NULL_TYPE_CODE;
        }
        short result = 0;
        myLock.lock();
        try
        {
            result = myTypeToCodeMap.get(type);
            if (result == 0) // Not found.
            {
                int nextCount = myTypeCounter + 1;
                if (nextCount > Short.MAX_VALUE)
                {
                    nextCount = Short.MIN_VALUE;
                }
                if (nextCount == NULL_TYPE_CODE)
                {
                    throw new MaxPoolSizeExceededException(
                            "Could not insert item, max pool size of " + myCodeToTypeMap.size() + " would be exceeded.");
                }
                myTypeCounter = (short)nextCount;
                result = myTypeCounter;
                myCodeToTypeMap.put(myTypeCounter, type);
                myTypeToCodeMap.put(type, myTypeCounter);
            }
        }
        finally
        {
            myLock.unlock();
        }
        return result;
    }

    /**
     * Gets the type from code, returns null if there is not type for the code
     * or the the code is the NULL_TYPE_CODE (-1).
     *
     * @param code the code
     * @return the type from code
     */
    public T getTypeFromCode(short code)
    {
        if (code == NULL_TYPE_CODE)
        {
            return null;
        }
        T result = null;
        if (code != NULL_TYPE_CODE)
        {
            myLock.lock();
            try
            {
                result = myCodeToTypeMap.get(code);
            }
            finally
            {
                myLock.unlock();
            }
        }
        return result;
    }

    /**
     * Reset pool.
     */
    public void resetPool()
    {
        myLock.lock();
        try
        {
            myTypeCounter = 0;
            myCodeToTypeMap.clear();
            myTypeToCodeMap.clear();
        }
        finally
        {
            myLock.unlock();
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
        myLock.lock();
        try
        {
            size = myTypeToCodeMap.size();
        }
        finally
        {
            myLock.unlock();
        }
        return size;
    }
}
