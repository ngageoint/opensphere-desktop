package io.opensphere.mantle.data.cache.impl;

/**
 * A factory for creating DiskCacheReference objects based on the size of the
 * reference so that the DiskCacheReference objects are the minimum necessary
 * size to store the cache reference.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public final class DiskCacheReferenceFactory
{
    /**
     * Creates a new DiskCacheReference object.
     *
     * @param insertNum the insert num
     * @param pos the pos
     * @param size the size
     * @param originIdSize the origin id size
     * @param mgsOffset the mgs offset
     * @return the disk cache reference
     */
    public static CacheReference createDiskCacheReference(short insertNum, int pos, int size, byte originIdSize, int mgsOffset)
    {
        CacheReference ref = null;
        if (size < Short.MAX_VALUE && pos < Integer.MAX_VALUE)
        {
            ref = new ShortDiskCacheReference(insertNum, pos, (short)size, originIdSize, (short)mgsOffset);
        }
        else
        {
            ref = new IntDiskCacheReference(insertNum, pos, size, originIdSize, mgsOffset);
        }
        return ref;
    }

    /**
     * Instantiates a new disk cache reference factory.
     */
    private DiskCacheReferenceFactory()
    {
        // Don't allow instantiation.
    }
}
