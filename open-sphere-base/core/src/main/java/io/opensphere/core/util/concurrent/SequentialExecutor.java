package io.opensphere.core.util.concurrent;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;

import io.opensphere.core.util.Utilities;

/**
 * An executor that uses another executor to run tasks, but makes sure the tasks
 * are run sequentially. The tasks may be run on different threads, depending on
 * the executor provided in the constructor, but the next task will not start
 * until the preceding task is complete.
 */
public class SequentialExecutor implements Executor
{
    /** The wrapped executor. */
    private final Executor myExecutor;

    /**
     * Flag indicating if I have a task pending or running in the nested
     * executor.
     */
    private boolean myPending;

    /** Wrapper task. */
    private final Runnable myTask = () ->
    {
        try
        {
            Runnable job;
            synchronized (myWorkQueue)
            {
                job = myWorkQueue.poll();
            }
            if (job != null)
            {
                job.run();
            }
        }
        finally
        {
            boolean needExecute;
            synchronized (myWorkQueue)
            {
                if (myWorkQueue.isEmpty())
                {
                    myPending = false;
                    needExecute = false;
                }
                else
                {
                    needExecute = true;
                }
            }
            if (needExecute)
            {
                myExecutor.execute(myTask);
            }
        }
    };

    /** The work queue. */
    private final Queue<Runnable> myWorkQueue = new LinkedList<>();

    /**
     * Construct the sequential executor.
     *
     * @param executor The wrapped executor which will be used to run the tasks.
     */
    public SequentialExecutor(Executor executor)
    {
        myExecutor = Utilities.checkNull(executor, "executor");
    }

    @Override
    public void execute(Runnable command)
    {
        boolean needExecute;
        synchronized (myWorkQueue)
        {
            myWorkQueue.add(command);
            if (myPending)
            {
                needExecute = false;
            }
            else
            {
                needExecute = true;
                myPending = true;
            }
        }
        if (needExecute)
        {
            myExecutor.execute(myTask);
        }
    }
}
