package io.opensphere.mantle.data.cache;

import java.io.File;

/**
 * The CacheConfiguration for the DataElementCache.
 */
public final class CacheConfiguration
{
    /** The disk cache location. */
    private final File myDiskCacheLocation;

    /**
     * The max allowed elements in memory even accounting for those that are
     * scheduled for cache to permanent store.
     */
    private final int myMaxInMemory;

    /**
     * The number of elements allowed to reside in memory at all times.
     */
    private final int myPoolInMemory;

    /** The my remove from store on remove. */
    private final boolean myRemoveFromStoreOnRemove;

    /** The store type. */
    private final CacheStoreType myStoreType;

    /** The my use disk encryption. */
    private final boolean myUseDiskEncryption;

    /** The Use dynamic class storage for data elements. */
    private final boolean myUseDynamicClassStorageForDataElements;

    /**
     * Creates the disk cached configuration.
     *
     * @param maxInMemory the max allowed elements in memory
     * @param inMemoryPoolSize the preferred number of elements in memory
     * @param diskCacheLocation the disk cache location
     * @param removeFromStoreOnRemove the remove from store on remove
     * @param useEncryption the use encryption
     * @param useDynamicClasses the use dynamic classes
     * @return the cache configuration
     */
    public static CacheConfiguration createDiskCachedConfiguration(int maxInMemory, int inMemoryPoolSize, File diskCacheLocation,
            boolean removeFromStoreOnRemove, boolean useEncryption, boolean useDynamicClasses)
    {
        if (inMemoryPoolSize > maxInMemory)
        {
            throw new IllegalArgumentException("inMemoryPoolSize must be <= maxInMemory");
        }
        return new CacheConfiguration(CacheStoreType.DISK, maxInMemory, inMemoryPoolSize, diskCacheLocation,
                removeFromStoreOnRemove, useEncryption, useDynamicClasses);
    }

    /**
     * Creates the registry cached configuration.
     *
     * @param maxInMemory the max allowed elements in memory
     * @param inMemoryPoolSize the preferred number of elements in memory
     * @param removeFromStoreOnRemove the remove from store on remove
     * @param useDynamicClasses the use dynamic classes
     * @return the cache configuration
     */
    public static CacheConfiguration createRegistryCachedConfiguration(int maxInMemory, int inMemoryPoolSize,
            boolean removeFromStoreOnRemove, boolean useDynamicClasses)
    {
        if (inMemoryPoolSize > maxInMemory)
        {
            throw new IllegalArgumentException("inMemoryPoolSize must be <= maxInMemory");
        }
        return new CacheConfiguration(CacheStoreType.REGISTRY, maxInMemory, inMemoryPoolSize, null, removeFromStoreOnRemove,
                false, useDynamicClasses);
    }

    /**
     * Creates the unlimited in memory configuration with no persisted store.
     *
     * @param useDynamicClasses the use dynamic classes
     * @return the cache configuration
     */
    public static CacheConfiguration createUnlimitedInMemoryConfiguration(boolean useDynamicClasses)
    {
        return new CacheConfiguration(CacheStoreType.NONE, Integer.MAX_VALUE, Integer.MAX_VALUE, null, false, true,
                useDynamicClasses);
    }

    /**
     * Instantiates a new cache configuration.
     *
     * @param type the type
     * @param maxInMemory the max allowed elements in memory
     * @param poolInMemory the preferred number of elements in memory
     * @param diskCacheLocation the disk cache location
     * @param removeFromStoreOnRemove the remove from store on remove
     * @param useDiskEncryption the use disk encryption
     * @param useDynamicClassStorageForDataElements the use dynamic class
     *            storage for data elements
     */
    private CacheConfiguration(CacheStoreType type, int maxInMemory, int poolInMemory, File diskCacheLocation,
            boolean removeFromStoreOnRemove, boolean useDiskEncryption, boolean useDynamicClassStorageForDataElements)
    {
        myMaxInMemory = maxInMemory;
        myPoolInMemory = poolInMemory;
        myStoreType = type;
        myRemoveFromStoreOnRemove = removeFromStoreOnRemove;
        myUseDiskEncryption = useDiskEncryption;
        myDiskCacheLocation = diskCacheLocation;
        myUseDynamicClassStorageForDataElements = useDynamicClassStorageForDataElements;
    }

    /**
     * Gets the cache store type.
     *
     * @return the cache store type
     */
    public CacheStoreType getCacheStoreType()
    {
        return myStoreType;
    }

    /**
     * Gets the disk cache location.
     *
     * @return the disk cache location
     */
    public File getDiskCacheLocation()
    {
        return myDiskCacheLocation;
    }

    /**
     * Gets the in memory pool size.
     *
     * @return the in memory pool size
     */
    public int getInMemoryPoolSize()
    {
        return myPoolInMemory;
    }

    /**
     * Gets the max in memory.
     *
     * @return the max in memory
     */
    public int getMaxInMemory()
    {
        return myMaxInMemory;
    }

    /**
     * Removes the from store on remove.
     *
     * @return true, if successful
     */
    public boolean isRemoveFromStoreOnRemove()
    {
        return myRemoveFromStoreOnRemove;
    }

    /**
     * Checks if is unlimited.
     *
     * @return true, if is unlimited
     */
    public boolean isUnlimited()
    {
        return myPoolInMemory == Integer.MAX_VALUE;
    }

    /**
     * Checks if is use disk encryption.
     *
     * @return true, if is use disk encryption
     */
    public boolean isUseDiskEncryption()
    {
        return myUseDiskEncryption;
    }

    /**
     * Checks if is use dynamic class storage for data elements.
     *
     * @return true, if is use dynamic class storage for data elements
     */
    public boolean isUseDynamicClassStorageForDataElements()
    {
        return myUseDynamicClassStorageForDataElements;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("Cache Configuration Summary: \n" + "  Store Type       : ").append(myStoreType)
                .append("\n" + "  Encryption       : ").append(myUseDiskEncryption).append("\n" + "  RemoveOnRemove   : ")
                .append(myRemoveFromStoreOnRemove).append('\n');
        if (myStoreType == CacheStoreType.DISK)
        {
            sb.append("  DiskCacheLocation: ")
                    .append(myDiskCacheLocation == null ? "NULL" : myDiskCacheLocation.getAbsolutePath()).append('\n');
        }
        sb.append("  Pool In Memory   : ").append(myPoolInMemory).append("\n" + "  Max In Memory    : ").append(myMaxInMemory)
                .append("\n" + "  Dynamic Classes  : ").append(myUseDynamicClassStorageForDataElements).append('\n');

        return sb.toString();
    }
}
