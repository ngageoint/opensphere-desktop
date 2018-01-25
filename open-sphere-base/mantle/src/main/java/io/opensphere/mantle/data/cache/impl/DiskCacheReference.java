package io.opensphere.mantle.data.cache.impl;

/**
 * The Interface DiskCacheReference.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public interface DiskCacheReference
{
    /**
     * Gets the insert num.
     *
     * @return the insert num
     */
    short getInsertNum();

    /**
     * Gets the mDI offset.
     *
     * @return the mDI offset
     */
    int getMDIOffset();

    /**
     * Gets the mDI size.
     *
     * @return the mDI size
     */
    int getMDISize();

    /**
     * Gets the mGS offset.
     *
     * @return the mGS offset
     */
    int getMGSOffset();

    /**
     * Gets the mGS size.
     *
     * @return the mGS size
     */
    int getMGSSize();

    /**
     * Gets the origin id size.
     *
     * @return the origin id size
     */
    int getOriginIdSize();

    /**
     * Gets the position.
     *
     * @return the position
     */
    int getPosition();

    /**
     * Gets the size in bytes.
     *
     * @return the size
     */
    int getSize();
}
