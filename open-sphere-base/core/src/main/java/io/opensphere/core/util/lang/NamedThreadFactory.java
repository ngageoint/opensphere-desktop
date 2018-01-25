package io.opensphere.core.util.lang;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * A thread factory that creates threads with a common name, thread group,
 * priority, and maximum priority.
 */
public class NamedThreadFactory implements ThreadFactory
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(NamedThreadFactory.class);

    /** The name used for threads created by this factory. */
    private final String myName;

    /** The initial priority given to threads created by this factory. */
    private final int myPriority;

    /** The number of threads created by this factory so far. */
    private final AtomicInteger myThreadCount = new AtomicInteger();

    /** The thread group for threads created by this factory. */
    private final ThreadGroup myThreadGroup;

    /**
     * Create a named thread factory using default thread priorities.
     *
     * @param name The name used for the thread group and the threads.
     */
    public NamedThreadFactory(String name)
    {
        this(name, Thread.NORM_PRIORITY, Thread.MAX_PRIORITY);
    }

    /**
     * Create a named thread factory.
     *
     * @param name The name used for the thread group (appended with "-group")
     *            and the threads (appended with a dash and the index of the
     *            thread within its group).
     * @param priority The priority used for threads created by this factory.
     * @param maxPriority The maximum priority allowed for threads created by
     *            this factory. (A thread created by this factory cannot be
     *            re-prioritized higher than its max priority.)
     */
    public NamedThreadFactory(String name, int priority, int maxPriority)
    {
        myName = name;
        myThreadGroup = new ThreadGroup(name + "-group");
        myThreadGroup.setMaxPriority(maxPriority);
        myPriority = priority;
    }

    @Override
    public Thread newThread(Runnable r)
    {
        return newThread(r, Integer.toString(myThreadCount.getAndIncrement()));
    }

    /**
     * Create a new thread for a runnable, providing the thread name.
     *
     * @param r The runnable.
     * @param threadName The thread name.
     * @return The new thread.
     */
    protected Thread newThread(Runnable r, String threadName)
    {
        Thread thread = new Thread(myThreadGroup, r, myName + "-" + threadName)
        {
            @Override
            public void run()
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Created thread " + getName());
                }
                try
                {
                    super.run();
                }
                finally
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Exiting thread " + getName());
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.setPriority(myPriority);
        return thread;
    }

    /**
     * A subordinate thread factory that names the thread it creates with a
     * specific name. This is intended for use with a single thread executor.
     */
    public class ThreadNamer implements ThreadFactory
    {
        /** The thread name. */
        private final String myThreadName;

        /**
         * Constructor.
         *
         * @param threadName The name to be used for any threads created by this
         *            factory.
         */
        public ThreadNamer(String threadName)
        {
            myThreadName = threadName;
        }

        @Override
        public Thread newThread(Runnable r)
        {
            return NamedThreadFactory.this.newThread(r, myThreadName);
        }
    }
}
