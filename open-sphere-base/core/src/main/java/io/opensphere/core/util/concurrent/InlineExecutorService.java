package io.opensphere.core.util.concurrent;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An executor service that simply runs commands submitted to it, on the current
 * thread. If commands are submitted from different threads, they will run
 * concurrently.
 */
public class InlineExecutorService extends AbstractExecutorService
{
    /** Condition used to await termination. */
    private final Condition myCondition;

    /** Lock used to synchronize the threads. */
    private final Lock myLock;

    /** Flag indicating if the executor is shutdown. */
    private volatile boolean myShutdown;

    /** The threads running tasks. */
    private final Collection<Thread> myThreads = new LinkedList<>();

    /**
     * Constructor.
     */
    public InlineExecutorService()
    {
        myLock = new ReentrantLock();
        myCondition = myLock.newCondition();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
    {
        long nanos = unit.toNanos(timeout);
        while (nanos > 0)
        {
            myLock.lock();
            try
            {
                if (isTerminated())
                {
                    return true;
                }
                nanos = myCondition.awaitNanos(nanos);
            }
            finally
            {
                myLock.unlock();
            }
        }
        return isTerminated();
    }

    @Override
    public final void execute(Runnable command)
    {
        myLock.lock();
        try
        {
            if (myShutdown)
            {
                throw new RejectedExecutionException();
            }
            myThreads.add(Thread.currentThread());
        }
        finally
        {
            myLock.unlock();
        }
        try
        {
            command.run();
        }
        finally
        {
            myLock.lock();
            try
            {
                myThreads.remove(Thread.currentThread());
                myCondition.signalAll();
            }
            finally
            {
                myLock.unlock();
            }
        }
    }

    @Override
    public boolean isShutdown()
    {
        return myShutdown;
    }

    @Override
    public boolean isTerminated()
    {
        myLock.lock();
        try
        {
            return myShutdown && myThreads.isEmpty();
        }
        finally
        {
            myLock.unlock();
        }
    }

    @Override
    public void shutdown()
    {
        myShutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow()
    {
        shutdown();

        myLock.lock();
        try
        {
            for (Thread thread : myThreads)
            {
                thread.interrupt();
            }
        }
        finally
        {
            myLock.unlock();
        }

        return Collections.emptyList();
    }
}
