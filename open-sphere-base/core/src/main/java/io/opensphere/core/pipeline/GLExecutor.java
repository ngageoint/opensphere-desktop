package io.opensphere.core.pipeline;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import io.opensphere.core.pipeline.util.RepaintListener;

/**
 * An executor that puts tasks into a queue to eventually be drained on the GL
 * thread.
 */
public class GLExecutor implements Executor
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GLExecutor.class);

    /** A queue of runnables to be executed in the render loop. */
    private final BlockingQueue<Runnable> myGLQueue = new LinkedBlockingQueue<>();

    /** The repaint listener. */
    private final RepaintListener myRepaintListener;

    /**
     * Constructor.
     *
     * @param repaintListener The repaint listener to be called when something
     *            is added to the queue.
     */
    public GLExecutor(RepaintListener repaintListener)
    {
        myRepaintListener = repaintListener;
    }

    @Override
    public void execute(Runnable command)
    {
        while (true)
        {
            try
            {
                myGLQueue.put(command);

                // Trigger a repaint to be sure that the GL queue gets
                // processed.
                myRepaintListener.repaint();

                break;
            }
            catch (InterruptedException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Put into GL queue was interrupted.");
                }
            }
        }
    }

    /**
     * Get a special executor that determines whether to use the given executor
     * or the GL executor based on the size of the GL queue.
     *
     * @param standardExecutor The normal executor.
     * @return The load-sensitive executor.
     */
    public final Executor getLoadSensitiveExecutor(final Executor standardExecutor)
    {
        return command ->
        {
            if (myGLQueue.size() < 10)
            {
                GLExecutor.this.execute(command);
            }
            else
            {
                standardExecutor.execute(command);
            }
        };
    }

    /**
     * Poll the GL queue.
     *
     * @return A task.
     */
    public Runnable poll()
    {
        return myGLQueue.poll();
    }
}
