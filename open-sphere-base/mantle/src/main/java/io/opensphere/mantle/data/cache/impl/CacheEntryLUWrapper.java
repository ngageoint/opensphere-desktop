package io.opensphere.mantle.data.cache.impl;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * A wrapper around a cache entry, used to track the last used time of the
 * entry.
 */
public class CacheEntryLUWrapper implements Comparable<CacheEntryLUWrapper>
{
    /**
     * The entry contained within the wrapper.
     */
    private final CacheEntry myEntry;

    /**
     * The system time at which the entry was last used, expressed in
     * milliseconds since Java epoch.
     */
    private final long myLastUsedTime;

    /**
     * Instantiates a new cache entry wrapper.
     *
     * @param pCacheEntry the cache entry to be wrapped.
     */
    public CacheEntryLUWrapper(CacheEntry pCacheEntry)
    {
        myEntry = pCacheEntry;
        myLastUsedTime = pCacheEntry.getLastUsedTime();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(CacheEntryLUWrapper o)
    {
        return Utilities.sameInstance(this, o) || myLastUsedTime == o.myLastUsedTime ? 0
                : myLastUsedTime < o.myLastUsedTime ? -1 : 1;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        CacheEntryLUWrapper other = (CacheEntryLUWrapper)obj;
        return myLastUsedTime == other.myLastUsedTime && EqualsHelper.equals(myEntry, other.myEntry);
    }

    /**
     * Gets the entry.
     *
     * @return the entry
     */
    public CacheEntry getEntry()
    {
        return myEntry;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myEntry == null ? 0 : myEntry.hashCode());
        result = prime * result + (int)(myLastUsedTime ^ myLastUsedTime >>> 32);
        return result;
    }
}
