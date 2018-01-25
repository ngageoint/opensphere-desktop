package io.opensphere.core.data;

import java.util.Iterator;
import java.util.NoSuchElementException;

import io.opensphere.core.data.util.DataModelCategory;

/**
 * An iterator over caching data registry data providers that filters the
 * providers based on a data model category.
 */
class CachingDataRegistryDataProviderIterator implements Iterator<CachingDataRegistryDataProvider>
{
    /** The filter category. */
    private final DataModelCategory myDataModelCategory;

    /** The nested iterator. */
    private final Iterator<CachingDataRegistryDataProvider> myIter;

    /** Reference to the next item. */
    private CachingDataRegistryDataProvider myNext;

    /**
     * Constructor.
     *
     * @param dataModelCategory The data model category to use for filtering.
     * @param iter The nested iterator.
     */
    CachingDataRegistryDataProviderIterator(DataModelCategory dataModelCategory, Iterator<CachingDataRegistryDataProvider> iter)
    {
        myDataModelCategory = dataModelCategory;
        myIter = iter;
    }

    @Override
    public boolean hasNext()
    {
        return getNext() != null;
    }

    @Override
    public synchronized CachingDataRegistryDataProvider next()
    {
        CachingDataRegistryDataProvider next = getNext();
        if (next == null)
        {
            throw new NoSuchElementException();
        }
        myNext = null;
        return next;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the next provider that provides data for my category.
     *
     * @return The next provider.
     */
    private synchronized CachingDataRegistryDataProvider getNext()
    {
        if (myNext == null)
        {
            while (myIter.hasNext())
            {
                myNext = myIter.next();
                if (myNext.providesDataFor(myDataModelCategory))
                {
                    break;
                }
                else
                {
                    myNext = null;
                }
            }
        }
        return myNext;
    }
}
