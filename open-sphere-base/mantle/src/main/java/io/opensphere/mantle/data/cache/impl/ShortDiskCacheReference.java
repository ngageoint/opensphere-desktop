package io.opensphere.mantle.data.cache.impl;

import io.opensphere.mantle.data.cache.CacheStoreType;

/**
 * The Class ShortDiskCacheReference, for records with size less than
 * Short.MAX_VALUE in bytes.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public class ShortDiskCacheReference extends AbstractDiskCacheReference
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The my mgs offset. */
    private final short myMGSOffset;

    /** The my position. */
    private final int myPosition;

    /** The my size. */
    private final short mySize;

    /**
     * Instantiates a new short disk cache reference.
     *
     * @param insertNum the insert num
     * @param pos the pos
     * @param size the size
     * @param originIdSize the origin id size
     * @param mgsOffset the mgs offset
     */
    public ShortDiskCacheReference(short insertNum, int pos, short size, byte originIdSize, short mgsOffset)
    {
        super(insertNum, originIdSize);
        myPosition = pos;
        mySize = size;
        myMGSOffset = mgsOffset;
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
        ShortDiskCacheReference other = (ShortDiskCacheReference)obj;
        return myMGSOffset == other.myMGSOffset && myPosition == other.myPosition && mySize == other.mySize;
    }

    @Override
    public int getMDIOffset()
    {
        return getOriginIdSize();
    }

    @Override
    public int getMDISize()
    {
        return myMGSOffset - getMDIOffset();
    }

    @Override
    public int getMGSOffset()
    {
        return myMGSOffset;
    }

    @Override
    public int getMGSSize()
    {
        return mySize - myMGSOffset;
    }

    @Override
    public int getPosition()
    {
        return myPosition;
    }

    @Override
    public int getSize()
    {
        return mySize;
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
        result = prime * result + myMGSOffset;
        result = prime * result + myPosition;
        result = prime * result + mySize;
        return result;
    }
}
