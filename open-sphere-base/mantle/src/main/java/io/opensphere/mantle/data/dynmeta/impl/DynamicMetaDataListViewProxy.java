package io.opensphere.mantle.data.dynmeta.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import io.opensphere.mantle.data.dynmeta.DynamicMetadataDataTypeController;

/**
 * The Class DynamicColumnMetaDataListViewProxy.
 */
public class DynamicMetaDataListViewProxy implements List<Object>
{
    /** The Dyn column coordinator. */
    private DynamicMetadataDataTypeController myDynColumnCoordinator;

    /** The Element cache id. */
    private long myElementCacheId;

    /** The Underlying list. */
    private List<Object> myUnderlyingList;

    /**
     * Instantiates a new dynamic column meta data list view proxy.
     *
     * @param deCacheId the de cache id
     * @param underlyingList the underlying list
     * @param cdr the cdr
     */
    public DynamicMetaDataListViewProxy(long deCacheId, List<Object> underlyingList, DynamicMetadataDataTypeController cdr)
    {
        myElementCacheId = deCacheId;
        myUnderlyingList = underlyingList;
        myDynColumnCoordinator = cdr;
    }

    @Override
    public void add(int index, Object element)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Object e)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Object> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends Object> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(int index)
    {
        if (myDynColumnCoordinator.isDynamicColumnIndex(index))
        {
            return myDynColumnCoordinator.getValue(myElementCacheId, index);
        }
        return myUnderlyingList == null ? null : myUnderlyingList.get(index);
    }

    @Override
    public int indexOf(Object o)
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
        return new Iterator<>()
        {
            private int myCurIndex = -1;

            @Override
            public boolean hasNext()
            {
                return myCurIndex + 1 < size();
            }

            @Override
            public Object next()
            {
                myCurIndex++;
                if (myCurIndex == size())
                {
                    throw new NoSuchElementException();
                }
                return get(myCurIndex);
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return -1;
    }

    @Override
    public ListIterator<Object> listIterator()
    {
        return Collections.unmodifiableList(new ArrayList<>(this)).listIterator();
    }

    @Override
    public ListIterator<Object> listIterator(int arg0)
    {
        return Collections.unmodifiableList(new ArrayList<>(this)).listIterator(arg0);
    }

    @Override
    public Object remove(int index)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object set(int index, Object element)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the underlying info.
     *
     * @param deCacheId the de cache id
     * @param underlyingList the underlying list
     * @param cdr the cdr
     */
    public void setUnderlyingInfo(long deCacheId, List<Object> underlyingList, DynamicMetadataDataTypeController cdr)
    {
        myElementCacheId = deCacheId;
        myUnderlyingList = underlyingList;
        myDynColumnCoordinator = cdr;
    }

    @Override
    public int size()
    {
        return myUnderlyingList.size() + myDynColumnCoordinator.getDynamicColumnCount();
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

    @Override
    public Object[] toArray()
    {
        Object[] result = new Object[size()];
        for (int i = 0; i < size(); i++)
        {
            result[i] = get(i);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a)
    {
        return (T[])toArray();
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
