package io.opensphere.mantle.toolbox;

import java.io.File;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.cache.CacheConfiguration;

/** Cache utilities. */
final class MantleCacheUtils
{
    /** The Constant BASE_VM_SIZE_MB. */
    private static final double BASE_VM_SIZE_MB = 800.0;

    /** The Constant BYTES_PER_KB. */
    private static final double BYTES_PER_KB = 1024.0;

    /** The Constant EXTRA_VM_PER_FIFTY_K_DOTS. */
    private static final double EXTRA_VM_PER_FIFTY_K_DOTS = 200.0;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MantleCacheUtils.class);

    /** The Constant ONEHUNDRED_TEN_PERCENT. */
    private static final double ONEHUNDRED_TEN_PERCENT = 1.1;

    /** Private constructor. */
    private MantleCacheUtils()
    {
    }

    /**
     * Gets the element cache configuration.
     *
     * @param pluginProperties the plugin properties
     * @return the element cache configuration
     */
    public static CacheConfiguration getElementCacheConfiguration(Properties pluginProperties)
    {
        CacheConfiguration cacheCfg = null;

        final String useDynamicClassesStr = pluginProperties.getProperty("useDynamicClassStorageInCache", "false");
        final boolean useDynamicClassStorageInCache = "true".equalsIgnoreCase(useDynamicClassesStr);

        final String elementCacheType = pluginProperties.getProperty("elementCacheType", "MEMORY");
        if ("DISK".equalsIgnoreCase(elementCacheType))
        {
            final int inMemPoolSize = getElementCacheInMemPoolSize(pluginProperties);
            int maxElements = getElementCacheMaxElements(pluginProperties);

            // If max elements is less than the the in memory pool size
            // expand it to 110% of the pool size.
            if (maxElements < inMemPoolSize)
            {
                maxElements = (int)(inMemPoolSize * ONEHUNDRED_TEN_PERCENT);
            }
            final String runtimeDir = StringUtilities.expandProperties(System.getProperty("opensphere.db.path"),
                    System.getProperties());
            File diskCacheParent = new File(runtimeDir + File.separator + "elementCache");
            final boolean useEncryption = StringUtils
                    .equalsIgnoreCase(pluginProperties.getProperty("elementDiskCacheUseEncryption", "false"), "true");
            final String diskCacheLocation = pluginProperties.getProperty("elementDiskCacheLocation",
                    diskCacheParent.getAbsolutePath());
            diskCacheParent = new File(diskCacheLocation);
            cacheCfg = CacheConfiguration.createDiskCachedConfiguration(maxElements, inMemPoolSize, diskCacheParent, false,
                    useEncryption, useDynamicClassStorageInCache);
        }
        else if ("REGISTRY".equalsIgnoreCase(elementCacheType))
        {
            final int inMemPoolSize = getElementCacheInMemPoolSize(pluginProperties);
            final int maxElements = getElementCacheMaxElements(pluginProperties);
            cacheCfg = CacheConfiguration.createRegistryCachedConfiguration(maxElements, inMemPoolSize, true,
                    useDynamicClassStorageInCache);
        }
        else
        {
            cacheCfg = CacheConfiguration.createUnlimitedInMemoryConfiguration(useDynamicClassStorageInCache);
        }
        return cacheCfg;
    }

    /**
     * Gets the element cache in mem pool size.
     *
     * @param pluginProperties the plugin properties
     * @return the element cache in mem pool size
     */
    private static int getElementCacheInMemPoolSize(Properties pluginProperties)
    {
        final String inMemPoolSizeStr = pluginProperties.getProperty("elementCacheInMemoryPoolSize", "DYNAMIC");
        int inMemPoolSize = 0;
        if ("DYNAMIC".equalsIgnoreCase(inMemPoolSizeStr))
        {
            final double totalMemoryMB = Runtime.getRuntime().maxMemory() / BYTES_PER_KB / BYTES_PER_KB;
            if (totalMemoryMB < 800)
            {
                inMemPoolSize = 100000;
            }
            else
            {
                final int multiplier = (int)Math.round((totalMemoryMB - BASE_VM_SIZE_MB) / EXTRA_VM_PER_FIFTY_K_DOTS);
                inMemPoolSize = 100000 + 50000 * multiplier;
            }
            LOGGER.info("Dynamically Setting DataElementCache In-Memory-Pool-Size to " + inMemPoolSize);
        }
        else if ("UNLIMITED".equalsIgnoreCase(inMemPoolSizeStr))
        {
            inMemPoolSize = Integer.MAX_VALUE;
        }
        else
        {
            try
            {
                inMemPoolSize = Integer.parseInt(inMemPoolSizeStr);
                if (inMemPoolSize < 0)
                {
                    inMemPoolSize = 0;
                }
            }
            catch (final NumberFormatException e)
            {
                inMemPoolSize = 300000;
                LOGGER.debug(e);
                LOGGER.error("\"elementCacheInMemoryPoolSize\" must be an integer value, found \"" + inMemPoolSizeStr
                        + "\", setting to 300000");
            }
        }
        return inMemPoolSize;
    }

    /**
     * Gets the element cache max elements.
     *
     * @param pluginProperties the plugin properties
     * @return the element cache max elements
     */
    private static int getElementCacheMaxElements(Properties pluginProperties)
    {
        final String maxElementsStr = pluginProperties.getProperty("elementCacheMaxElements", "UNLIMITED");
        int maxElements = Integer.MAX_VALUE;
        if (!"UNLIMITED".equals(maxElementsStr))
        {
            try
            {
                maxElements = Integer.parseInt(maxElementsStr);

                // Interpret negative or zero values as unlimited.
                if (maxElements <= 0)
                {
                    maxElements = Integer.MAX_VALUE;
                }
            }
            catch (final NumberFormatException e)
            {
                maxElements = 300000;
                LOGGER.debug(e);
                LOGGER.error("\"elementCacheMaxElements\" must be an integer value, found \"" + maxElementsStr
                        + "\", setting to 10000000");
            }
        }
        return maxElements;
    }
}
