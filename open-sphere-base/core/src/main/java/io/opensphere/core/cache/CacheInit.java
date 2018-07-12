package io.opensphere.core.cache;

import java.util.ServiceLoader;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;

import io.opensphere.core.options.OptionsRegistry;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Initializer for the cache. This will attempt to find a {@link ServiceLoader}
 * for a {@link CacheFactory}, and create a {@link Cache} using the factory.
 */
public class CacheInit
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CacheInit.class);

    /** The created cache. */
    private Cache myCache;

    /** The executor for the cache. */
    private final ScheduledExecutorService myExecutor;

    /**
     * Construct the cache initializer.
     *
     * @param cacheExecutor The executor for the cache.
     */
    public CacheInit(ScheduledExecutorService cacheExecutor)
    {
        myExecutor = cacheExecutor;
    }

    /**
     * Initialize the cache.
     *
     * @param path The directory to contain the cache files.
     * @return The cache, or <code>null</code> if there is none.
     * @throws CacheException If there is an error initializing the cache.
     */
    @SuppressWarnings("PMD.PrematureDeclaration")
    public Cache initializeCache(String path) throws CacheException
    {
        final long start = System.nanoTime();

        final int rowLimit = Utilities.parseSystemProperty("opensphere.db.rowLimit", -1);
        if (rowLimit >= 0)
        {
            LOGGER.info("Database row limit is set to " + rowLimit);
        }

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Loading cache factories.");
        }

        CacheException cacheException = null;
        for (final CacheFactory cacheFactory : ServiceLoader.load(CacheFactory.class))
        {
            try
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Creating cache using cache factory [" + cacheFactory + "]");
                    LOGGER.debug("Creating cache at path '" + path + "'");
                }

                myCache = cacheFactory.create(path, rowLimit, myExecutor);
                final int millisecondsWait = Utilities.parseSystemProperty("opensphere.cache.delay.ms", Constants.MILLI_PER_UNIT);
                myCache.initialize(millisecondsWait);
            }
            catch (final ClassNotFoundException e)
            {
                LOGGER.warn("Failed to initialize cache using factory [" + cacheFactory.getClass().getName() + "]: " + e, e);
            }
            catch (final CacheException e)
            {
                cacheException = e;
                LOGGER.warn("Failed to initialize cache using factory [" + cacheFactory.getClass().getName() + "]: " + e, e);
                myCache = null;
            }
        }

        if (myCache == null)
        {
            if (cacheException != null)
            {
                LOGGER.error("No cache could be initialized.", cacheException);
                throw cacheException;
            }
            LOGGER.error("No cache could be initialized.");
        }

        // TODO: Allow the user to set an alternate db location in the
        // default db. Detect that here and switch the database to the
        // alternate location.

        LOGGER.info(StringUtilities.formatTimingMessage("Initialized cache in ", System.nanoTime() - start));

        return myCache;
    }

    /**
     * Initialize the cache options provider.
     *
     * @param optsRegistry The options registry.
     * @param prefsRegistry The preferences registry.
     */
    public void initializeCacheOptions(OptionsRegistry optsRegistry, PreferencesRegistry prefsRegistry)
    {
        if (myCache == null)
        {
            throw new IllegalStateException("Cache not initialized.");
        }
        else
        {
            Utilities.checkNull(optsRegistry, "optsRegistry");
            optsRegistry.addOptionsProvider(new CacheOptionsProvider(prefsRegistry, myCache));
        }
    }
}
