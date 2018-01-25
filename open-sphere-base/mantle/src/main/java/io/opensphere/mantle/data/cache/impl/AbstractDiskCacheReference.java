package io.opensphere.mantle.data.cache.impl;

import io.opensphere.mantle.data.cache.CacheStoreType;

/**
 * The Class AbstractDiskCacheReference.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public abstract class AbstractDiskCacheReference extends CacheReference implements DiskCacheReference
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The insert number. */
    private final short myInsertNum;

    /** The my origin id size. */
    private final byte myOriginIdSize;

    /**
     * Instantiates a new short disk cache reference.
     *
     * @param insertNum the insert num
     * @param originIdSize the origin id size
     */
    public AbstractDiskCacheReference(short insertNum, byte originIdSize)
    {
        myInsertNum = insertNum;
        myOriginIdSize = originIdSize;
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
        AbstractDiskCacheReference other = (AbstractDiskCacheReference)obj;
        return myInsertNum == other.myInsertNum && myOriginIdSize == other.myOriginIdSize;
    }

    @Override
    public short getInsertNum()
    {
        return myInsertNum;
    }

    @Override
    public int getOriginIdSize()
    {
        return myOriginIdSize;
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
        result = prime * result + myInsertNum;
        result = prime * result + myOriginIdSize;
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("DiskCacheReference: InsertNum[").append(myInsertNum).append("] Pos[").append(getPosition()).append("] Size[")
                .append(getSize()).append("] Offset[").append(getMGSOffset()).append("] OrgIdSize[").append(myOriginIdSize)
                .append(']');
        return sb.toString();
    }
}
