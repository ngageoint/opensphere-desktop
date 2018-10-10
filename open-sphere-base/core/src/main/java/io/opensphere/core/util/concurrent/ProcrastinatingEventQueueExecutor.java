package io.opensphere.core.util.concurrent;

import java.util.concurrent.Executor;

import javax.swing.Timer;

/**
 * An executor that runs on the EDT but will only maintain one pending task at a
 * time.
 */
public class ProcrastinatingEventQueueExecutor implements Executor
{
    /** The length of the waiting period before new runnables are executed. */
    private final int myMinDelayMilliseconds;

    /** The Swing timer for the current pending task, if there is one. */
    private Timer myTimer;

    /**
     * Constructor.
     *
     * @param minDelayMilliseconds The minimum delay between when a task is
     *            submitted and when it is executed.
     */
    public ProcrastinatingEventQueueExecutor(int minDelayMilliseconds)
    {
        myMinDelayMilliseconds = minDelayMilliseconds;
    }

    @Override
    public synchronized void execute(final Runnable command)
    {
        if (myTimer != null)
        {
            myTimer.stop();
        }
        myTimer = new Timer(myMinDelayMilliseconds, e -> command.run());
        myTimer.setRepeats(false);
        myTimer.start();
    }
}
