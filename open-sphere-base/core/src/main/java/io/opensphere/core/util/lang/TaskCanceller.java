package io.opensphere.core.util.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;

import edu.umd.cs.findbugs.annotations.Nullable;
import net.jcip.annotations.ThreadSafe;

/**
 * An object that may be used to wrap tasks that will be run on other threads.
 * When the tasks run, their threads will be registered with this class so that
 * if the task needs to be cancelled, the threads can be interrupted and their
 * {@link Cancellable}s called.
 * <p>
 * A task canceller may be constructed with a parent, in which case the threads
 * will also be added to the parent. Cancelling the parent will also cancel this
 * canceller. Cancelling this canceller will not cancel the parent.
 */
@ThreadSafe
public class TaskCanceller extends DefaultCancellable
{
    /** The parent. */
    @Nullable
    private final TaskCanceller myParent;

    /** The threads being used to run this task. */
    private final Collection<Thread> myThreads = Collections.synchronizedCollection(new ArrayList<Thread>(1));

    /**
     * Constructor for a task canceller with no parent.
     */
    public TaskCanceller()
    {
        this(null);
    }

    /**
     * Constructor for a task canceller with a parent.
     *
     * @param parent The parent.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public TaskCanceller(TaskCanceller parent)
    {
        myParent = parent;
        if (parent != null && parent.isCancelled())
        {
            super.cancel();
        }
    }

    @Override
    public void cancel()
    {
        if (!isCancelled())
        {
            super.cancel();
            cancelThreads();
        }
    }

    /**
     * Get the threads that are registered.
     *
     * @return The threads.
     */
    public Collection<Thread> getThreads()
    {
        return new ArrayList<>(myThreads);
    }

    @Override
    public boolean isCancelled()
    {
        return super.isCancelled() || myParent != null && myParent.isCancelled();
    }

    /**
     * Get a Callable that will register the thread that runs the given Callable
     * so that it can be interrupted if necessary using {@link #cancel()}.
     *
     * @param <T> The return type of the Callable.
     * @param c The input callable.
     * @return the callable
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public <T> Callable<T> wrap(Callable<T> c)
    {
        return () ->
        {
            addCurrentThread();
            try
            {
                if (isCancelled())
                {
                    throw new InterruptedException();
                }
                return c.call();
            }
            finally
            {
                removeCurrentThread();
            }
        };
    }

    /**
     * Get a Callable that will register the thread that runs the given Callable
     * so that it can be interrupted if necessary using {@link #cancel()}.
     *
     * @param <T> The return type of the Callable.
     * @param c The input callable.
     * @return the callable
     */
    @SuppressWarnings("PMD.AvoidRethrowingException")
    public <T> InterruptibleCallable<T> wrap(InterruptibleCallable<T> c)
    {
        return () ->
        {
            try
            {
                return wrap((Callable<T>)() -> c.call()).call();
            }
            catch (RuntimeException | Error | InterruptedException e1)
            {
                throw e1;
            }
            catch (Exception e2)
            {
                throw new ImpossibleException(e2);
            }
        };
    }

    /**
     * Get a Runnable that will register the thread that runs the given Runnable
     * so that it can be interrupted if necessary using {@link #cancel()}.
     *
     * @param r The r.
     * @return the runnable
     */
    @SuppressWarnings("PMD.AvoidRethrowingException")
    public Runnable wrap(Runnable r)
    {
        return () ->
        {
            try
            {
                wrap((Callable<Void>)() ->
                {
                    r.run();
                    return null;
                }).call();
            }
            catch (InterruptedException e)
            {
            }
            catch (RuntimeException | Error e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new ImpossibleException(e);
            }
        };
    }

    /**
     * Add the current thread to my threads.
     */
    protected final void addCurrentThread()
    {
        ThreadControl.addCancellable(this);
        myThreads.add(Thread.currentThread());
        if (myParent != null)
        {
            myParent.addCurrentThread();
        }
    }

    /**
     * Cancel threads.
     */
    protected void cancelThreads()
    {
        myThreads.forEach(t -> ThreadControl.cancelThread(t));
    }

    /**
     * Remove the current thread from my threads.
     */
    protected final void removeCurrentThread()
    {
        ThreadControl.removeCancellables(this);
        myThreads.remove(Thread.currentThread());
        if (myParent != null)
        {
            myParent.removeCurrentThread();
        }
    }
}
