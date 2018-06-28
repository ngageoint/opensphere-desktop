package io.opensphere.core.matchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import net.jcip.annotations.ThreadSafe;

/**
 * A utility that catches the unchecked exceptions thrown by a series of tasks,
 * and then provides the capability to re-throw the exceptions at another time.
 * This can be used during testing when assertions are made on a thread other
 * than the thread being tested, and then the errors can be thrown from the
 * thread under test so that the test will fail.
 */
@ThreadSafe
public class ErrorCatcher
{
    /** The tasks I'm catching errors for. */
    private final Collection<FutureTask<Void>> myTasks = new ArrayList<>();

    /**
     * Run the given task and catch any unchecked exceptions that come out of
     * it.
     *
     * @param task The task.
     */
    public void catchErrors(Runnable task)
    {
        FutureTask<Void> future = new FutureTask<>(task, null);
        future.run();
        synchronized (this)
        {
            myTasks.add(future);
        }
    }

    /**
     * Throw any exceptions that were caught when running my tasks.
     */
    public synchronized void throwErrors()
    {
        for (FutureTask<Void> task : myTasks)
        {
            try
            {
                task.get();
            }
            catch (ExecutionException e)
            {
                if (e.getCause() instanceof RuntimeException)
                {
                    throw (RuntimeException)e.getCause();
                }
                else
                {
                    throw (Error)e.getCause();
                }
            }
            catch (InterruptedException e)
            {
            }
        }
    }
}
