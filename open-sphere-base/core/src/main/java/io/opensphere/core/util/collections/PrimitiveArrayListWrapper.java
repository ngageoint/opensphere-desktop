package io.opensphere.core.util.collections;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.RandomAccess;

import io.opensphere.core.util.Utilities;

/**
 * Wraps a primitive array in a list interface. Changes to the array are not
 * permitted.
 *
 * @param <E> The type of object in the List.
 */
@io.opensphere.core.util.Immutable
@net.jcip.annotations.Immutable
public class PrimitiveArrayListWrapper<E> extends AbstractList<E> implements Serializable, RandomAccess
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** The values array. */
    private final Object myValues;

    /**
     * Instantiates a new list wrapper.
     *
     * @param array The array to be wrapped.
     * @param type The type of the list.
     * @throws IllegalArgumentException If the type of the array is not
     *             compatible with the type of the list.
     */
    public PrimitiveArrayListWrapper(Object array, Class<E> type)
    {
        if (array == null || !array.getClass().isArray())
        {
            throw new IllegalArgumentException("Argument is not an array: " + array);
        }
        else if (type.isAssignableFrom(array.getClass().getComponentType())
                || Utilities.primitiveTypeFor(type) == array.getClass().getComponentType())
        {
            myValues = array;
        }
        else
        {
            throw new IllegalArgumentException("Array component type [" + array.getClass().getComponentType()
                    + "] is not compatible with declared list type [" + type + "]");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public E get(int index)
    {
        if (index < 0 || index >= size())
        {
            throw new IndexOutOfBoundsException("Index " + index + " not in range 0 to " + (size() - 1));
        }
        return (E)Array.get(myValues, index);
    }

    @Override
    public int size()
    {
        return Array.getLength(myValues);
    }

    /**
     * Get the value array.
     *
     * @return The array.
     */
    protected final Object getValues()
    {
        return myValues;
    }
}
