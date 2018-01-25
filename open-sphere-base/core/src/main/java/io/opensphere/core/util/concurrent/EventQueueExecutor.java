package io.opensphere.core.util.concurrent;

import java.awt.EventQueue;
import java.time.Duration;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

/**
 * An executor that runs on the AWT event queue.
 */
public class EventQueueExecutor implements Executor
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(EventQueueExecutor.class);

    @Override
    public void execute(Runnable command)
    {
        EventQueue.invokeLater(command);
//        invokeLaterInstrumented(command);
    }

    /**
     * Wrapper for {@link EventQueue#invokeLater(Runnable)} that provides
     * instrumentation.
     *
     * @param runnable the runnable to run later
     */
    public static void invokeLaterInstrumented(Runnable runnable)
    {
        EventQueue.invokeLater(getInstrumentedRunnable(runnable, Duration.ofMillis(200)));
    }

    /**
     * Wraps the runnable in a runnable that reports on execution time.
     *
     * @param runnable the runnable to instrument
     * @param threshold the reporting threshold
     * @return the instrumented runnable
     */
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    private static Runnable getInstrumentedRunnable(Runnable runnable, Duration threshold)
    {
        Exception e = new Exception();
        return new Runnable()
        {
            @Override
            public void run()
            {
                long start = System.nanoTime();

                runnable.run();

                Duration runTime = Duration.ofNanos(System.nanoTime() - start);
                if (runTime.compareTo(threshold) > 0)
                {
                    LOGGER.error("Task took " + runTime.toMillis() + " milliseconds to run", e);
                }
            }
        };
    }
}
