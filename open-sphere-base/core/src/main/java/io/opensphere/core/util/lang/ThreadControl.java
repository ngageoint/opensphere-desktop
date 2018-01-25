package io.opensphere.core.util.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;

/**
 * Control class for managing paused and cancelled threads. A thread can be
 * paused or cancelled using {@link #pauseThread(Thread)} or
 * {@link #cancelThread(Thread)}, which will cause the subject thread to be
 * interrupted. The interrupted thread should then call
 * {@link #pollThreadCancelled()} to determine if it has been cancelled, and
 * abort processing if so. If the thread has not been cancelled, the thread
 * should call {@link #waitForUnpause()} to suspend processing. If the thread
 * was paused, when it is time to unpause the thread,
 * {@link #unpauseThread(Thread)} should be called, which will notify the
 * subject thread to continue processing, and the call to
 * {@link #waitForUnpause()} will return. Long-running operations should call
 * {@link #check()} periodically, which will check for an interrupt and either
 * wait for the unpause or throw an {@link InterruptedException} if the thread
 * is cancelled.
 */
public final class ThreadControl
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ThreadControl.class);

    /** Cancelled threads. */
    private static Collection<Thread> ourCancelledThreads = Collections.synchronizedCollection(new LinkedList<Thread>());

    /**
     * Thread-local list of things to be cancelled if the thread is cancelled.
     */
    private static final Map<Thread, List<Cancellable>> ourCancellables = new WeakHashMap<Thread, List<Cancellable>>();

    /** Map of registered threads to locks. */
    private static final ConcurrentMap<Thread, CountDownLatch> ourThreadLatches = new ConcurrentHashMap<>();

    /**
     * Add something to be cancelled if this thread is cancelled.
     * <p>
     * <strong>Be careful adding Cancellables for pooled threads to not create a
     * memory leak. They must always be cancelled or removed.</strong>
     * </p>
     *
     * @param cancellable Something to be cancelled.
     */
    public static void addCancellable(Cancellable cancellable)
    {
        synchronized (ourCancellables)
        {
            List<Cancellable> list = ourCancellables.get(Thread.currentThread());
            if (list == null)
            {
                list = new ArrayList<>(1);
                ourCancellables.put(Thread.currentThread(), list);
            }

            list.add(Utilities.checkNull(cancellable, "cancellable"));
        }

        if (isThreadCancelled())
        {
            cancelThread(Thread.currentThread());
        }
    }

    /**
     * Cancel a thread. This marks the thread as cancelled and interrupts it.
     * The thread is expected to call {@link #pollThreadCancelled()} on
     * interrupt.
     *
     * @param thread The thread.
     */
    public static void cancelThread(Thread thread)
    {
        unpauseThread(thread, true);
        List<Cancellable> cancellables;
        synchronized (ourCancellables)
        {
            cancellables = ourCancellables.remove(thread);
        }
        if (cancellables != null)
        {
            for (Cancellable cancellable : cancellables)
            {
                cancellable.cancel();
            }
        }
        thread.interrupt();
    }

    /**
     * Check if the current thread is cancelled or paused. If the thread is
     * cancelled, clear the cancelled state and throw an
     * {@link InterruptedException}. If the thread is paused, wait until the
     * unpause occurs.
     *
     * @throws InterruptedException If the thread is cancelled.
     */
    public static void check() throws InterruptedException
    {
        boolean interrupted = Thread.interrupted();
        if (isThreadPaused())
        {
            waitForUnpause();
        }
        else if (interrupted)
        {
            ourCancelledThreads.remove(Thread.currentThread());
            throw new InterruptedException();
        }
    }

    /**
     * Clear the cancelled/paused state of a thread.
     *
     * @param thread The thread.
     */
    public static void clearState(Thread thread)
    {
        ourCancelledThreads.remove(thread);
        unpauseThread(thread, false);
    }

    /**
     * Get if the current thread is cancelled. This does not reset the cancelled
     * state.
     *
     * @return {@code true} if the thread was cancelled.
     */
    public static boolean isThreadCancelled()
    {
        return isThreadCancelled(Thread.currentThread());
    }

    /**
     * Get if a thread is cancelled. This does not reset the cancelled state.
     *
     * @param thread The thread.
     * @return {@code true} if the thread was cancelled.
     */
    public static boolean isThreadCancelled(Thread thread)
    {
        return thread.isInterrupted() && !isThreadPaused(thread);
    }

    /**
     * Determine if the current thread is currently paused.
     *
     * @return {@code true} if the thread is paused.
     */
    public static boolean isThreadPaused()
    {
        return isThreadPaused(Thread.currentThread());
    }

    /**
     * Determine if a thread is currently paused.
     *
     * @param thread The thread.
     * @return {@code true} if the thread is paused.
     */
    public static boolean isThreadPaused(Thread thread)
    {
        CountDownLatch latch = ourThreadLatches.get(thread);
        return latch != null && latch.getCount() > 0;
    }

    /**
     * Pause a thread. The thread will be interrupted, and expected to call
     * {@link #waitForUnpause()}.
     *
     * @param thread The thread.
     */
    public static void pauseThread(Thread thread)
    {
        CountDownLatch current = ourThreadLatches.putIfAbsent(thread, new CountDownLatch(1));
        if (current == null)
        {
            LOGGER.info("Pausing thread " + thread.getName());
        }
    }

    /**
     * Return if the current thread is cancelled and reset the cancelled state.
     *
     * @return {@code true} if the thread was cancelled.
     */
    public static boolean pollThreadCancelled()
    {
        return Thread.interrupted() && !isThreadPaused();
    }

    /**
     * Remove {@link Cancellable}s. This has no effect if the cancellable has
     * already been removed or cancelled.
     *
     * @param cancellable The cancellable.
     */
    public static void removeCancellables(Cancellable... cancellable)
    {
        synchronized (ourCancellables)
        {
            List<Cancellable> list = ourCancellables.get(Thread.currentThread());
            if (list != null)
            {
                list.removeAll(Arrays.asList(cancellable));
                if (list.isEmpty())
                {
                    ourCancellables.remove(Thread.currentThread());
                }
            }
        }
    }

    /**
     * Unpause a thread.
     *
     * @param thread The thread.
     */
    public static void unpauseThread(Thread thread)
    {
        unpauseThread(thread, false);
    }

    /**
     * Wait for this thread to be unpaused. If the thread is not paused, return
     * immediately.
     *
     * @throws InterruptedException If the thread is cancelled while being
     *             paused.
     */
    public static void waitForUnpause() throws InterruptedException
    {
        if (ourThreadLatches.isEmpty())
        {
            return;
        }
        // Loop in case a new latch was created as the old one was released.
        for (CountDownLatch latch; (latch = ourThreadLatches.get(Thread.currentThread())) != null;)
        {
            while (true)
            {
                try
                {
                    latch.await();
                    break;
                }
                catch (InterruptedException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(e, e);
                    }
                }
            }
        }
        if (ourCancelledThreads.remove(Thread.currentThread()))
        {
            throw new InterruptedException();
        }
    }

    /**
     * Unpause a thread.
     *
     * @param thread The thread.
     * @param cancelled Indicates if the thread is to be cancelled immediately
     *            after being unpaused.
     */
    private static void unpauseThread(Thread thread, boolean cancelled)
    {
        CountDownLatch latch = ourThreadLatches.remove(thread);
        if (latch != null)
        {
            if (cancelled)
            {
                ourCancelledThreads.add(thread);
            }
            else
            {
                LOGGER.info("Unpausing thread " + thread.getName());
            }
            latch.countDown();
        }
    }

    /** Disallow instantiation. */
    private ThreadControl()
    {
    }
}
