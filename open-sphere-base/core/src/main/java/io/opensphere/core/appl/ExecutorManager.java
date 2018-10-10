package io.opensphere.core.appl;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.opensphere.core.MemoryManager;
import io.opensphere.core.MemoryManager.MemoryListener;
import io.opensphere.core.MemoryManager.Status;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.LazyMap.Factory;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.core.util.concurrent.FixedThreadPoolExecutor;
import io.opensphere.core.util.concurrent.InterruptingExecutor;
import io.opensphere.core.util.concurrent.PausingThreadPoolExecutor;
import io.opensphere.core.util.concurrent.PriorityThreadPoolExecutor;
import io.opensphere.core.util.concurrent.ReportingScheduledExecutorService;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * Manager for the high-level executors in the system.
 */
class ExecutorManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ExecutorManager.class);

    /** Initial thread priority for envoys and transformers. */
    private static final int MAX_THREAD_PRIORITY = 4;

    /** Priority allowed for envoys and transformers. */
    private static final int THREAD_PRIORITY = 3;

    /** An executor for the time animator. */
    private final ScheduledExecutorService myAnimatorExecutor;

    /** The thread pool configuration. */
    private ThreadPoolConfigs myConfigs;

    /** Factory for envoy thread pool executors. */
    private final Factory<String, ExecutorController> myEnvoyExecutorFactory = key ->
    {
        ThreadPoolConfig config = StreamUtilities.filterOne(myConfigs.getConfigs(),
                t -> Pattern.matches(t.getNamePattern(), key));

        if (config == null)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("No configuration key found for thread pool with key " + key);
            }
            config = new ThreadPoolConfig();
            config.setMinimumThreadCount(0);
            config.setNormalThreadCount(10);
            config.setRestrictedThreadCount(1);
        }

        String name = "Envoy[" + key + "]";
        PausingThreadPoolExecutor pausingThreadPoolExecutor = new PausingThreadPoolExecutor(config.getNormalThreadCount(),
                config.getNormalThreadCount(), 20, TimeUnit.SECONDS,
                new NamedThreadFactory(name, THREAD_PRIORITY, MAX_THREAD_PRIORITY),
                SuppressableRejectedExecutionHandler.getInstance());
        pausingThreadPoolExecutor.allowCoreThreadTimeOut(true);
        ExecutorController holder = new ExecutorController(name, pausingThreadPoolExecutor, config);
        if (myMemoryManager != null)
        {
            holder.adjustToMemoryStatus(myMemoryManager.getMemoryStatus());
        }
        else
        {
            LOGGER.info("Created thread pool " + name + " with size " + holder.getExecutor().getCorePoolSize());
        }
        return holder;
    };

    /** An executor for envoy work. */
    private final Map<String, ExecutorController> myEnvoyExecutorMap = LazyMap
            .create(Collections.synchronizedMap(New.<String, ExecutorController>map()), String.class, myEnvoyExecutorFactory);

    /** The memory listener used to reduce pool sizes when memory gets low. */
    @Nullable
    private MemoryListener myMemoryListener;

    /** The optional memory manager. */
    @Nullable
    private MemoryManager myMemoryManager;

    /** The thread factory for the pipeline. */
    private final NamedThreadFactory myPipelineThreadFactory = new NamedThreadFactory("Pipeline");

    /** The preferences event executor. */
    private final ExecutorService myPreferencesEventExecutor;

    /** The preferences persist executor. */
    private final ScheduledExecutorService myPreferencesPersistExecutor;

    /** An executor for transformer work. */
    private final ExecutorService myTransformerExecutor;

    /** The thread factory for the transformers. */
    private final NamedThreadFactory myTransformerThreadFactory;

    /**
     * Constructor.
     */
    public ExecutorManager()
    {
        // Load the common timer to get its thread started in the main thread
        // group.
        CommonTimer.init();

        myTransformerThreadFactory = new NamedThreadFactory("Transformer", THREAD_PRIORITY, MAX_THREAD_PRIORITY);
        myTransformerExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1L, TimeUnit.MINUTES,
                new SynchronousQueue<Runnable>(), myTransformerThreadFactory, SuppressableRejectedExecutionHandler.getInstance());

        NamedThreadFactory preferencesThreadFactory = new NamedThreadFactory("Preferences");
        myPreferencesEventExecutor = new ThreadPoolExecutor(1, 1, 20, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                preferencesThreadFactory.new ThreadNamer("event"), SuppressableRejectedExecutionHandler.getInstance());
        myPreferencesPersistExecutor = new ReportingScheduledExecutorService(new ScheduledThreadPoolExecutor(1,
                preferencesThreadFactory.new ThreadNamer("persist"), SuppressableRejectedExecutionHandler.getInstance()));
        myAnimatorExecutor = new ReportingScheduledExecutorService(new ScheduledThreadPoolExecutor(1,
                new NamedThreadFactory("TimeAnimator"), SuppressableRejectedExecutionHandler.getInstance()));
    }

    /**
     * Create an executor for the cache. The caller owns the executor.
     *
     * @return The cache executor.
     */
    public ScheduledExecutorService createCacheExecutor()
    {
        return new ReportingScheduledExecutorService(
                new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("Cache", Thread.MIN_PRIORITY, Thread.MIN_PRIORITY),
                        SuppressableRejectedExecutionHandler.getInstance()));
    }

    /**
     * Create an executor for the data registry. The caller owns the executor.
     *
     * @return The data registry executor.
     */
    public ExecutorService createDataRegistryExecutor()
    {
        return new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                new NamedThreadFactory("DataRegistry"), SuppressableRejectedExecutionHandler.getInstance());
    }

    /**
     * Get the executor for the time animator.
     *
     * @return The executor.
     */
    public ScheduledExecutorService getAnimatorExecutor()
    {
        return myAnimatorExecutor;
    }

    /**
     * Get an envoy executor for the given thread pool.
     *
     * @param threadPoolName The name of the thread pool.
     * @return The envoy executor.
     * @throws IllegalArgumentException If the thread pool configuration has not
     *             been set.
     */
    public ThreadPoolExecutor getEnvoyExecutor(String threadPoolName) throws IllegalArgumentException
    {
        if (myConfigs == null)
        {
            throw new IllegalStateException("Cannot get envoy executor before setting thread pool configuration.");
        }
        return myEnvoyExecutorMap.get(threadPoolName).getExecutor();
    }

    /**
     * Get the executor for geometry data retrievers.
     *
     * @return The executor.
     */
    public ExecutorService getGeometryDataRetrieverExecutor()
    {
        final int priority = 3;
        final int maxPriority = 4;
        final long timeoutSeconds = 30L;
        ThreadPoolExecutor executor = new PriorityThreadPoolExecutor(10, 10, timeoutSeconds, TimeUnit.SECONDS,
                new PriorityBlockingQueue<Runnable>(), new NamedThreadFactory("GeometryDataRetriever", priority, maxPriority),
                SuppressableRejectedExecutionHandler.getInstance());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    /**
     * Get the executor for the map manager.
     *
     * @return The map update executor.
     */
    public ExecutorService getMapExecutor()
    {
        return Executors.newFixedThreadPool(1, new NamedThreadFactory("View").new ThreadNamer("update"));
    }

    /**
     * Get the executor service for the pipeline.
     *
     * @return The pipeline executor.
     */
    public ExecutorService getPipelineExecutorService()
    {
        int threadCount = Math.max(Runtime.getRuntime().availableProcessors() - 2, 1);
        return new FixedThreadPoolExecutor(threadCount, myPipelineThreadFactory,
                SuppressableRejectedExecutionHandler.getInstance());
//        final long timeLimitMilliseconds = 100L;
//        ScheduledExecutorService pipelineExecutor = new InterruptingExecutor(exec, timeLimitMilliseconds);
    }

    /**
     * Get the scheduled executor service for the pipeline.
     *
     * @return The scheduled executor service.
     */
    public ScheduledExecutorService getPipelineScheduledExecutorService()
    {
        final long timeLimitMilliseconds = 100L;
        ScheduledThreadPoolExecutor schedExec = new ScheduledThreadPoolExecutor(1, myPipelineThreadFactory,
                SuppressableRejectedExecutionHandler.getInstance());
        return new InterruptingExecutor(new ReportingScheduledExecutorService(schedExec), timeLimitMilliseconds);
    }

    /**
     * Get the preferences event executor.
     *
     * @return The executor.
     */
    public Executor getPreferencesEventExecutor()
    {
        return myPreferencesEventExecutor;
    }

    /**
     * Get the preferences persist executor.
     *
     * @return The executor.
     */
    public ScheduledExecutorService getPreferencesPersistExecutor()
    {
        return myPreferencesPersistExecutor;
    }

    /**
     * Accessor for the transformerExecutor.
     *
     * @return The transformerExecutor.
     */
    public ExecutorService getTransformerExecutor()
    {
        return myTransformerExecutor;
    }

    /**
     * Accessor for the transformer thread factory.
     *
     * @return The transformer thread factory.
     */
    public ThreadFactory getTransformerThreadFactory()
    {
        return myTransformerThreadFactory;
    }

    /**
     * Set the thread pool configs. This must be set before any calls to
     * {@link #getEnvoyExecutor(String)}.
     *
     * @param configs The configs.
     */
    public void setConfigs(ThreadPoolConfigs configs)
    {
        myConfigs = Utilities.checkNull(configs, "configs");
    }

    /**
     * Set the memory manager to be used by this executor manager to size its
     * pools.
     *
     * @param memoryManager The memory manager.
     */
    public synchronized void setMemoryManager(MemoryManager memoryManager)
    {
        if (myMemoryManager != null && myMemoryListener != null)
        {
            myMemoryManager.removeMemoryListener(myMemoryListener);
        }
        myMemoryManager = memoryManager;
        if (memoryManager != null)
        {
            myMemoryListener = (oldStatus, newStatus) -> myEnvoyExecutorMap.values().stream()
                    .forEach(e -> e.adjustToMemoryStatus(newStatus));
            myMemoryManager.addMemoryListener(myMemoryListener);
        }
    }

    /**
     * Ask the executors to shutdown immediately, and wait a moment for them to
     * terminate.
     */
    public synchronized void shutdown()
    {
        if (myMemoryManager != null)
        {
            myMemoryManager.removeMemoryListener(myMemoryListener);
            myMemoryListener = null;
            myMemoryManager = null;
        }

        SuppressableRejectedExecutionHandler.getInstance().setSuppressed(true);
        myAnimatorExecutor.shutdown();
        for (ExecutorController envoyExecutor : myEnvoyExecutorMap.values())
        {
            envoyExecutor.getExecutor().shutdownNow();
        }
        myTransformerExecutor.shutdownNow();
        myPreferencesPersistExecutor.shutdown();
        for (ExecutorController envoyExecutor : myEnvoyExecutorMap.values())
        {
            try
            {
                envoyExecutor.getExecutor().awaitTermination(1, TimeUnit.SECONDS);
            }
            catch (InterruptedException e)
            {
            }
        }
        try
        {
            myTransformerExecutor.awaitTermination(1, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
        }
        try
        {
            myPreferencesEventExecutor.awaitTermination(1, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
        }
    }

    /**
     * Controller for an executor.
     */
    private static class ExecutorController
    {
        /** The config. */
        private final ThreadPoolConfig myConfig;

        /** The held executor. */
        private final PausingThreadPoolExecutor myExecutor;

        /** The name. */
        private final String myName;

        /**
         * Constructor.
         *
         * @param name The name for the thread pool.
         * @param executor The executor.
         * @param config The config.
         */
        public ExecutorController(String name, PausingThreadPoolExecutor executor, ThreadPoolConfig config)
        {
            myName = name;
            myExecutor = executor;
            myConfig = config;
        }

        /**
         * Adjust the thread pool size according to the memory status.
         *
         * @param memoryStatus The memory status.
         */
        public void adjustToMemoryStatus(Status memoryStatus)
        {
            int envoyThreadPoolSize;
            switch (memoryStatus)
            {
                case CRITICAL:
                    envoyThreadPoolSize = myConfig.getMinimumThreadCount();
                    break;
                case NOMINAL:
                    envoyThreadPoolSize = myConfig.getNormalThreadCount();
                    break;
                case WARNING:
                    envoyThreadPoolSize = myConfig.getRestrictedThreadCount();
                    break;
                default:
                    throw new UnexpectedEnumException(memoryStatus);
            }
            if (myExecutor.getCorePoolSize() != envoyThreadPoolSize)
            {
                LOGGER.info("Setting " + getName() + " thread pool size to " + envoyThreadPoolSize);
                myExecutor.setCorePoolSize(envoyThreadPoolSize);
            }
        }

        /**
         * Get the executor.
         *
         * @return The executor.
         */
        public PausingThreadPoolExecutor getExecutor()
        {
            return myExecutor;
        }

        /**
         * Get the name of the thread pool.
         *
         * @return The name.
         */
        public String getName()
        {
            return myName;
        }
    }
}
