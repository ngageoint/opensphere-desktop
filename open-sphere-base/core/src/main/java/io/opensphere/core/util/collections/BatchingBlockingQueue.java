package io.opensphere.core.util.collections;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link BlockingQueue} that collects added objects into batches and notifies
 * observers when a batch is ready.
 *
 * @param <E> The type of objects in the queue.
 */
@net.jcip.annotations.ThreadSafe
public class BatchingBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>
{
    /**
     * The amount of time that the queue will wait before notifying the observer
     * that objects have been added.
     */
    private final long myDelayMilliseconds;

    /**
     * The executor that will execute the scheduled tasks.
     */
    private final ScheduledExecutorService myExecutor;

    /** The lock used to synchronize the timer task. */
    private final Lock myLock = new ReentrantLock();

    /** List of observers. */
    private final List<Observer> myObservers = new ArrayList<>();

    /** Wrapped blocking queue. */
    private final BlockingQueue<E> myQueue = new LinkedBlockingQueue<>();

    /** Reference to the future when one is pending. */
    private volatile Future<?> myFuture;

    /**
     * Construct a batching queue.
     *
     * @param executor An executor that will notify the observers when a batch
     *            is ready.
     * @param delayMilliseconds The amount of time that the queue will wait
     *            before notifying the observer that objects have been added.
     */
    public BatchingBlockingQueue(ScheduledExecutorService executor, long delayMilliseconds)
    {
        myExecutor = executor;
        myDelayMilliseconds = delayMilliseconds;
    }

    /**
     * Add an observer to be notified after objects are added to the queue.
     *
     * @param obs The observer.
     */
    public void addObserver(Observer obs)
    {
        synchronized (myObservers)
        {
            myObservers.add(obs);
        }
    }

    @Override
    public int drainTo(Collection<? super E> c)
    {
        return myQueue.drainTo(c);
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements)
    {
        return myQueue.drainTo(c, maxElements);
    }

    @Override
    public Iterator<E> iterator()
    {
        return myQueue.iterator();
    }

    /** Notify the observers that objects have been added. */
    public void notifyObservers()
    {
        // This locking scheme will not strictly prevent observers from getting
        // redundant notifications, but the performance impact should be
        // minimal.
        myLock.lock();
        try
        {
            if (myFuture != null)
            {
                myFuture.cancel(false);
                myFuture = null;
            }
            List<Observer> observers;
            synchronized (myObservers)
            {
                observers = new ArrayList<>(myObservers);
            }
            for (Observer obs : observers)
            {
                obs.objectsAdded();
            }
        }
        catch (RuntimeException | Error e)
        {
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            throw e;
        }
        finally
        {
            myLock.unlock();
        }
    }

    @Override
    public boolean offer(E e)
    {
        boolean added = myQueue.offer(e);
        if (added)
        {
            scheduleTask();
        }
        return added;
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException
    {
        boolean added = myQueue.offer(e, timeout, unit);
        if (added)
        {
            scheduleTask();
        }
        return added;
    }

    @Override
    public E peek()
    {
        return myQueue.peek();
    }

    @Override
    public E poll()
    {
        return myQueue.poll();
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException
    {
        return myQueue.poll(timeout, unit);
    }

    @Override
    public void put(E e) throws InterruptedException
    {
        myQueue.put(e);
        scheduleTask();
    }

    @Override
    public int remainingCapacity()
    {
        return myQueue.remainingCapacity();
    }

    /**
     * Remove an observer.
     *
     * @param obs The observer.
     */
    public void removeObserver(Observer obs)
    {
        synchronized (myObservers)
        {
            myObservers.remove(obs);
        }
    }

    @Override
    public int size()
    {
        return myQueue.size();
    }

    @Override
    public E take() throws InterruptedException
    {
        return myQueue.take();
    }

    /** Schedule a timer task if one isn't already scheduled. */
    private void scheduleTask()
    {
        myLock.lock();
        try
        {
            if (myFuture == null)
            {
                myFuture = myExecutor.schedule(this::notifyObservers, myDelayMilliseconds, TimeUnit.MILLISECONDS);
            }
        }
        finally
        {
            myLock.unlock();
        }
    }

    /** Observer to be notified after objects have been added to the queue. */
    @FunctionalInterface
    public interface Observer
    {
        /**
         * Called after objects have been added to the queue. Note that this
         * does not necessarily mean there are currently objects in the queue,
         * because they may have already been drained.
         */
        void objectsAdded();
    }
}
