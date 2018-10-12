package io.opensphere.core.preferences;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.security.CipherFactory;
import io.opensphere.core.util.security.SecretKeyProviderException;

/**
 * Implementation of a preferences registry.
 */
@SuppressWarnings("PMD.GodClass")
public final class PreferencesRegistryImpl implements PreferencesRegistry
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PreferencesRegistryImpl.class);

    /** The service loader for persistence managers. */
    private static final ServiceLoader<PreferencesPersistenceManager> PERSISTENCE_MANAGERS = ServiceLoader
            .load(PreferencesPersistenceManager.class);

    /** Lock used for resetting preferences. */
    private final ReadWriteLock myResetLock = new ReentrantReadWriteLock();

    /** Preferences map. */
    private final LazyMap<String, InternalPreferencesIF> myTopicToPreferencesMap;

    /** Preferences options. */
    private final Map<String, PreferencesOptions> myTopicToPreferencesOptionsMap;

    /**
     * Constructor.
     *
     * @param eventExecutor An executor for sending events.
     * @param persistExecutor An executor for persisting preferences.
     */
    public PreferencesRegistryImpl(final Executor eventExecutor, final ScheduledExecutorService persistExecutor)
    {
        LazyMap.Factory<? super String, ? extends InternalPreferencesIF> factory = new LazyMap.Factory<String, InternalPreferencesIF>()
        {
            @Override
            public InternalPreferencesIF create(String topic)
            {
                PreferencesOptions opts = myTopicToPreferencesOptionsMap.get(topic);
                opts.lock();
                Collection<? extends PreferencesPersistenceManager> persistenceManagers = getPersistenceManagers(topic, opts);
                InternalPreferencesIF preferences = loadPreferences(topic, opts, persistenceManagers);
                if (preferences == null)
                {
                    preferences = new PreferencesImpl(topic);
                }

                preferences.setEventDispatchExecutor(eventExecutor);
                PreferencesPersistenceManager saveManager = getSaveManager(persistenceManagers);
                if (saveManager == null)
                {
                    LOGGER.warn("No persistence manager that supports saving preferences could be found.");
                }
                else
                {
                    preferences.setPersistExecutor(persistExecutor);
                    preferences.setPersistenceManager(saveManager);
                    if (opts.isCompressed())
                    {
                        preferences.setCompressed(true);
                    }
                    if (opts.getCipherFactory() != null)
                    {
                        preferences.setCipherFactory(opts.getCipherFactory());
                    }
                }
                return preferences;
            }
        };

        myTopicToPreferencesMap = new LazyMap<String, InternalPreferencesIF>(
                new ConcurrentHashMap<String, InternalPreferencesIF>(), String.class, factory);

        LazyMap.Factory<String, PreferencesOptions> prefOptionsFactory = new LazyMap.Factory<String, PreferencesOptions>()
        {
            @Override
            public PreferencesOptions create(String key)
            {
                return new PreferencesOptions();
            }
        };
        myTopicToPreferencesOptionsMap = new LazyMap<String, PreferencesOptions>(
                Collections.synchronizedMap(New.<String, PreferencesOptions>map()), String.class, prefOptionsFactory);
    }

    @Override
    public Preferences getPreferences(Class<?> aClass)
    {
        if (aClass != null)
        {
            return getPreferences(aClass.getName());
        }
        else
        {
            return null;
        }
    }

    @Override
    public Preferences getPreferences(String topic)
    {
        myResetLock.readLock().lock();
        try
        {
            return myTopicToPreferencesMap.get(topic);
        }
        finally
        {
            myResetLock.readLock().unlock();
        }
    }

    @Override
    public Set<String> getTopics()
    {
        return myTopicToPreferencesMap.keySet();
    }

    @Override
    public boolean hasTopic(Class<?> aClass)
    {
        return aClass != null && hasTopic(aClass.getName());
    }

    @Override
    public boolean hasTopic(String topic)
    {
        return myTopicToPreferencesMap.containsKey(topic);
    }

    @Override
    public void resetPreferences(Class<?> aClass, Object source)
    {
        if (aClass != null)
        {
            resetPreferences(aClass.getName(), null);
        }
    }

    @Override
    public void resetPreferences(String topic, Object source)
    {
        myResetLock.writeLock().lock();
        try
        {
            InternalPreferencesIF existingPrefs = myTopicToPreferencesMap.getIfExists(topic);
            for (PreferencesPersistenceManager persistenceManager : PERSISTENCE_MANAGERS)
            {
                if (persistenceManager.supportsSave())
                {
                    persistenceManager.delete(topic);
                }
            }
            if (existingPrefs != null)
            {
                PreferencesOptions opts = myTopicToPreferencesOptionsMap.get(topic);
                Collection<? extends PreferencesPersistenceManager> persistenceManagers = getPersistenceManagers(topic, opts);

                // Remove persistence managers that load from disk.
                for (Iterator<? extends PreferencesPersistenceManager> iter = persistenceManagers.iterator(); iter.hasNext();)
                {
                    PreferencesPersistenceManager preferencesPersistenceManager = iter.next();
                    if (preferencesPersistenceManager.supportsSave())
                    {
                        iter.remove();
                    }
                }

                InternalPreferencesIF loaded = loadPreferences(topic, opts, persistenceManagers);
                existingPrefs.replacePreferences(loaded, false, source);
            }
        }
        finally
        {
            myResetLock.writeLock().unlock();
        }
    }

    @Override
    public void setPreferencesCipherFactory(Class<?> aClass, CipherFactory cipherFactory)
    {
        setPreferencesCipherFactory(aClass.getName(), cipherFactory);
    }

    @Override
    public void setPreferencesCipherFactory(String topic, CipherFactory cipherFactory)
    {
        myTopicToPreferencesOptionsMap.get(topic).setCipherFactory(cipherFactory);
    }

    @Override
    public void setPreferencesCompression(Class<?> aClass, boolean flag)
    {
        setPreferencesCompression(aClass.getName(), flag);
    }

    @Override
    public void setPreferencesCompression(String topic, boolean flag)
    {
        myTopicToPreferencesOptionsMap.get(topic).setCompressed(flag);
    }

    /**
     * Get the persistence managers for a preferences topic.
     *
     * @param topic The preferences topic.
     * @param opts The preferences options.
     * @return The persistence managers.
     */
    private Collection<? extends PreferencesPersistenceManager> getPersistenceManagers(String topic, PreferencesOptions opts)
    {
        Collection<PreferencesPersistenceManager> results = New.collection();
        for (PreferencesPersistenceManager persistenceManager : PERSISTENCE_MANAGERS)
        {
            if ((!opts.isCompressed() || persistenceManager.supportsCompression()) && (opts.getCipherFactory() == null
                    || persistenceManager.supportsEncryption() && opts.getSecuredCipherFactory() != null))
            {
                results.add(persistenceManager);
            }
        }
        return results;
    }

    /**
     * Get a persistence manager that supports saving.
     *
     * @param persistenceManagers The possible persistence managers.
     * @return The manager, or {@code null} if one was not found.
     */
    private PreferencesPersistenceManager getSaveManager(Collection<? extends PreferencesPersistenceManager> persistenceManagers)
    {
        for (PreferencesPersistenceManager persistenceManager : persistenceManagers)
        {
            if (persistenceManager.supportsSave())
            {
                return persistenceManager;
            }
        }
        return null;
    }

    /**
     * Load preferences from the provided persistence managers in order. If
     * preferences are found for the topic in multiple persistence managers,
     * they will be merged.
     *
     * @param topic The preferences topic.
     * @param opts The preferences options.
     * @param persistenceManagers The persistence managers.
     * @return The loaded preferences.
     */
    private InternalPreferencesIF loadPreferences(String topic, PreferencesOptions opts,
            Collection<? extends PreferencesPersistenceManager> persistenceManagers)
    {
        InternalPreferencesIF preferences = null;
        for (PreferencesPersistenceManager persistenceManager : persistenceManagers)
        {
            InternalPreferencesIF loaded = persistenceManager.load(topic, opts.getSecuredCipherFactory(), opts.isCompressed());
            if (loaded != null)
            {
                if (preferences == null)
                {
                    preferences = loaded;
                }
                else
                {
                    preferences.merge(loaded);
                }
            }
        }
        return preferences;
    }

    /**
     * Options for a preferences topic.
     */
    private static class PreferencesOptions
    {
        /** A cipher factory used for crypto. */
        private CipherFactory myCipherFactory;

        /** Flag indicating if the preferences should be compressed on disk. */
        private boolean myCompressed;

        /**
         * Flag indicating if the preferences have been loaded, and so the
         * options are locked.
         */
        private boolean myLocked;

        /**
         * A cipher factory with the secret key secured, used only for initially
         * loading preferences.
         */
        private CipherFactory mySecuredCipherFactory;

        /**
         * Get the cipher factory.
         *
         * @return The cipher factory.
         */
        public synchronized CipherFactory getCipherFactory()
        {
            return myCipherFactory;
        }

        /**
         * Get the secured cipher factory. This should only be used for
         * initially loading preferences.
         *
         * @return The cipher factory.
         */
        public synchronized CipherFactory getSecuredCipherFactory()
        {
            return mySecuredCipherFactory;
        }

        /**
         * Get if the preferences should be compressed on-disk.
         *
         * @return {@code true} if the preferences should be compressed.
         */
        public synchronized boolean isCompressed()
        {
            return myCompressed;
        }

        /**
         * Lock the options.
         */
        public synchronized void lock()
        {
            myLocked = true;
        }

        /**
         * Set the cipher factory.
         *
         * @param cipherFactory The cipher factory.
         */
        public synchronized void setCipherFactory(CipherFactory cipherFactory)
        {
            if (myLocked)
            {
                if (!Objects.equals(cipherFactory, myCipherFactory))
                {
                    throw new IllegalStateException("Cipher factory cannot be set after preferences are loaded.");
                }
            }
            else
            {
                myCipherFactory = cipherFactory;
                try
                {
                    mySecuredCipherFactory = cipherFactory.secureSecretKey();
                }
                catch (SecretKeyProviderException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(e, e);
                    }
                    mySecuredCipherFactory = null;
                }
            }
        }

        /**
         * Set if the preferences should be compressed on-disk.
         *
         * @param compressed {@code true} if the preferences should be
         *            compressed.
         */
        public synchronized void setCompressed(boolean compressed)
        {
            if (myLocked)
            {
                if (myCompressed != compressed)
                {
                    throw new IllegalStateException("Compression cannot be set after preferences are loaded.");
                }
            }
            else
            {
                myCompressed = compressed;
            }
        }
    }
}
