package io.opensphere.mantle.data.cache.impl;

import java.util.List;

import gnu.trove.procedure.TObjectProcedure;

/**
 * A procedure that scans the elements and collects candidates for cleaning.
 */
public class CleanScanProcedure implements TObjectProcedure<CacheEntry>
{
    /**
     * The count of elements that have been stored to disk and are not in
     * memory.
     */
    private int myAlreadyCached;

    /**
     * The count of elements that are currently in memory.
     */
    private int myInMemory;

    /**
     * The count of elements that have not yet been stored to disk.
     */
    private int myNotCached;

    /**
     * The my remove candidates.
     */
    private final List<CacheEntryLUWrapper> myRemoveCandidates;

    /**
     * Constructor.
     *
     * @param removeCandidates Return collection of removal candidates.
     */
    public CleanScanProcedure(List<CacheEntryLUWrapper> removeCandidates)
    {
        myRemoveCandidates = removeCandidates;
    }

    /**
     * {@inheritDoc}
     *
     * @see gnu.trove.procedure.TObjectProcedure#execute(java.lang.Object)
     */
    @Override
    public boolean execute(CacheEntry ref)
    {
        if (ref != null)
        {
            if (ref.getCacheReference() == null)
            {
                myNotCached++;
            }
            else if (ref.getLoadedElementData() == null)
            {
                myAlreadyCached++;
            }
            else
            {
                myInMemory++;
                myRemoveCandidates.add(new CacheEntryLUWrapper(ref));
            }
        }
        return true;
    }

    /**
     * Gets the count of elements that have been stored to disk and are not in
     * memory.
     *
     * @return The count.
     */
    public int getAlreadyCached()
    {
        return myAlreadyCached;
    }

    /**
     * Gets the count of elements that are currently in memory.
     *
     * @return The count.
     */
    public int getInMemory()
    {
        return myInMemory;
    }

    /**
     * Gets the count of elements that have not yet been stored to disk.
     *
     * @return the not cached
     */
    public int getNotCached()
    {
        return myNotCached;
    }
}
