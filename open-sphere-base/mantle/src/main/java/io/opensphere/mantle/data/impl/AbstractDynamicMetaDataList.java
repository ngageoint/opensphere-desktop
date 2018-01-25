package io.opensphere.mantle.data.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import io.opensphere.core.util.lang.ByteString;
import io.opensphere.mantle.data.element.DynamicMetaDataList;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * The Class AbstractDynamicMetaDataList.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public abstract class AbstractDynamicMetaDataList implements DynamicMetaDataList
{
    /** The NULL_BYTE_STRING. */
    public static final ByteString NULL_BYTE_STRING = new ByteString("null");

    /** The ZERO_NUMBER. */
    public static final Number ZERO_NUMBER = Integer.valueOf(0);

    /** The our dynamic enumeration registry. */
    private static DynamicEnumerationRegistry ourDynamicEnumerationRegistry;

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Gets the dynamic enum registry.
     *
     * @return the dynamic enum registry
     */
    public static DynamicEnumerationRegistry getDynamicEnumRegistry()
    {
        return ourDynamicEnumerationRegistry;
    }

    /**
     * Sets the dynamic enum registry.
     *
     * @param reg the new dynamic enum registry
     */
    public static void setDynamicEnumRegistry(DynamicEnumerationRegistry reg)
    {
        ourDynamicEnumerationRegistry = reg;
    }

    @Override
    public void add(int arg0, Object arg1)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Object arg0)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Object> arg0)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int arg0, Collection<? extends Object> arg1)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object arg0)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> arg0)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object arg0)
    {
        return -1;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public Iterator<Object> iterator()
    {
        return new Iterator<Object>()
        {
            private int myIndex = -1;

            @Override
            public boolean hasNext()
            {
                return myIndex + 1 < size();
            }

            @Override
            public Object next()
            {
                myIndex++;
                if (myIndex == size())
                {
                    throw new NoSuchElementException();
                }
                return get(myIndex);
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public boolean keysMutable()
    {
        return false;
    }

    @Override
    public int lastIndexOf(Object arg0)
    {
        return -1;
    }

    @Override
    public ListIterator<Object> listIterator()
    {
        return Collections.unmodifiableList(new ArrayList<Object>(size())).listIterator();
    }

    @Override
    public ListIterator<Object> listIterator(int arg0)
    {
        return Collections.unmodifiableList(new ArrayList<Object>(size())).listIterator(arg0);
    }

    @Override
    public DynamicMetaDataList newCopy(MetaDataProvider other)
    {
        DynamicMetaDataList val = newInstance();
        val.setEqualTo(other);
        return val;
    }

    @Override
    public DynamicMetaDataList newFromDecode(ObjectInputStream ois) throws IOException
    {
        DynamicMetaDataList val = newInstance();
        val.decode(ois);
        return val;
    }

    @Override
    public Object remove(int arg0)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object arg0)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> arg0)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeKey(String key)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> arg0)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Object> subList(int fromIndex, int toIndex)
    {
        checkIndexForOutOfBounds(fromIndex);
        checkIndexForOutOfBounds(toIndex);
        if (toIndex <= fromIndex)
        {
            throw new IndexOutOfBoundsException("toIndex must be > fromIndex");
        }
        List<Object> result = new ArrayList<>(toIndex - fromIndex);
        for (int i = fromIndex; i < toIndex; i++)
        {
            result.add(get(i));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] arg0)
    {
        return (T[])toArray();
    }

    @Override
    public boolean valuesMutable()
    {
        return true;
    }

    /**
     * Check index for out of bounds.
     *
     * @param index the index
     * @throws IndexOutOfBoundsException the index out of bounds exception
     */
    protected void checkIndexForOutOfBounds(int index) throws IndexOutOfBoundsException
    {
        if (index >= size() || index < 0)
        {
            throw new IndexOutOfBoundsException("Index must be 0 <= index < " + size());
        }
    }
}
