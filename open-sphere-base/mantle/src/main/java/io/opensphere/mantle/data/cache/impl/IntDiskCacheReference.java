package io.opensphere.mantle.data.cache.impl;

import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.mantle.data.cache.CacheStoreType;

/**
 * The Class IntDiskCacheReference, for records with size less than
 * Integer.MAX_VALUE in bytes.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public class IntDiskCacheReference extends AbstractDiskCacheReference
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The mgs offset. */
    private final int myIntMGSOffset;

    /** The my position. */
    private final int myIntPosition;

    /** The size. */
    private final int myIntSize;

    /**
     * Instantiates a new int disk cache reference.
     *
     * @param insertNum the insert num
     * @param pos the pos
     * @param size the size
     * @param originIdSize the origin id size
     * @param mgsOffset the mgs offset
     */
    public IntDiskCacheReference(short insertNum, int pos, int size, byte originIdSize, int mgsOffset)
    {
        super(insertNum, originIdSize);
        myIntPosition = pos;
        myIntSize = size;
        myIntMGSOffset = mgsOffset;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        IntDiskCacheReference other = (IntDiskCacheReference)obj;
        return myIntMGSOffset == other.myIntMGSOffset && myIntPosition == other.myIntPosition && myIntSize == other.myIntSize;
    }

    @Override
    public int getMDIOffset()
    {
        return getOriginIdSize();
    }

    @Override
    public int getMDISize()
    {
        return myIntMGSOffset - getMDIOffset();
    }

    @Override
    public int getMGSOffset()
    {
        return myIntMGSOffset;
    }

    @Override
    public int getMGSSize()
    {
        return myIntSize - myIntMGSOffset;
    }

    @Override
    public int getPosition()
    {
        return myIntPosition;
    }

    @Override
    public int getSize()
    {
        return myIntSize;
    }

    @Override
    public CacheStoreType getType()
    {
        return CacheStoreType.DISK;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + HashCodeHelper.getHashCode(myIntMGSOffset);
        result = prime * result + HashCodeHelper.getHashCode(myIntPosition);
        result = prime * result + HashCodeHelper.getHashCode(myIntSize);
        return result;
    }
}
