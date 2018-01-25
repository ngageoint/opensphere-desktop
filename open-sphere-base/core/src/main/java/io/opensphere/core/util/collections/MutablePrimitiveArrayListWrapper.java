package io.opensphere.core.util.collections;

import java.lang.reflect.Array;

import io.opensphere.core.util.Utilities;

/**
 * Mutable version of {@link PrimitiveArrayListWrapper}.
 *
 * @param <E> The type of object in the List.
 */
public class MutablePrimitiveArrayListWrapper<E> extends PrimitiveArrayListWrapper<E>
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new list wrapper.
     *
     * @param array The array to be wrapped.
     * @param type The type of the list.
     */
    public MutablePrimitiveArrayListWrapper(Object array, Class<E> type)
    {
        super(array, type);
    }

    @Override
    public E set(int index, E element)
    {
        Utilities.checkNull(element, "element");
        E lastValue = get(index);
        Array.set(getValues(), index, element);
        return lastValue;
    }
}
