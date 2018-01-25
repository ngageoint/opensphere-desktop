package io.opensphere.core.util.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.opensphere.core.util.lang.ThreadUtilities;
import org.junit.Assert;

/**
 * Test for {@link InterruptingExecutor}.
 */
public class InterruptingExecutorTest
{
    /**
     * Test submitting a task to the executor after it has been stopped.
     */
    @Test(expected = RejectedExecutionException.class)
    public void testExecuteAfterShutdown()
    {
        Executor wrappedExecutor = Executors.newFixedThreadPool(1);
        long timeLimitMilliseconds = 36L;
        InterruptingExecutor exec = new InterruptingExecutor(wrappedExecutor, timeLimitMilliseconds);
        exec.shutdown();
        exec.execute(new RunnableImplementation(0L, 0L));
    }

    /**
     * Test submitting a task to the executor after it has been stopped.
     */
    @Test(expected = RejectedExecutionException.class)
    public void testExecuteAfterShutdownNow()
    {
        Executor wrappedExecutor = Executors.newFixedThreadPool(1);
        long timeLimitMilliseconds = 36L;
        InterruptingExecutor exec = new InterruptingExecutor(wrappedExecutor, timeLimitMilliseconds);
        exec.shutdownNow();
        exec.execute(new RunnableImplementation(0L, 0L));
    }

    /**
     * Test the executor.
     *
     * @throws InterruptedException If something weird happens.
     */
    @Test
    public void testExecutor() throws InterruptedException
    {
        Executor wrappedExecutor = Executors.newFixedThreadPool(1);
        long timeLimitMilliseconds = 36L;
        InterruptingExecutor exec = new InterruptingExecutor(wrappedExecutor, timeLimitMilliseconds);
        for (long sleepTime = 1L; sleepTime * 10 < timeLimitMilliseconds * 9; sleepTime *= 2)
        {
            RunnableImplementation command1 = new RunnableImplementation(sleepTime, 0L);
            synchronized (command1)
            {
                exec.execute(command1);
                command1.wait();
            }
            Assert.assertEquals("Command should not have been interrupted because time " + sleepTime + " is less than limit "
                    + timeLimitMilliseconds, 0, command1.getInterruptedCount());
        }

        RunnableImplementation command2 = new RunnableImplementation(timeLimitMilliseconds * 2L, timeLimitMilliseconds * 10L);
        synchronized (command2)
        {
            exec.execute(command2);
            command2.wait();
        }
        Assert.assertEquals("Command should have been interrupted once.", 1, command2.getInterruptedCount());

        exec.shutdown();
    }

    /**
     * Test submitting a task to the executor after it has been shutdown.
     */
    @Test
    public void testShutdown()
    {
        Executor wrappedExecutor = Executors.newFixedThreadPool(1);
        long timeLimitMilliseconds = 100000L;
        InterruptingExecutor exec = new InterruptingExecutor(wrappedExecutor, timeLimitMilliseconds);

        RunnableImplementation command = new RunnableImplementation(timeLimitMilliseconds, 0L);
        exec.execute(command);
        ThreadUtilities.sleep(50L);
        exec.shutdownNow();
        ThreadUtilities.sleep(50L);
        Assert.assertEquals("Command should have been interrupted once.", 1, command.getInterruptedCount());
    }

    /**
     * Runnable that keeps track of it it was interrupted.
     */
    private static final class RunnableImplementation implements Runnable
    {
        /** How many times was this thread interrupted. */
        private final AtomicInteger myInterruptedCount = new AtomicInteger();

        /** How long to sleep the first time. */
        private final long mySleepTime1;

        /** How long to sleep the second time. */
        private final long mySleepTime2;

        /**
         * Construct the runnable.
         *
         * @param sleepTime1 The time to sleep in the run method the first time.
         * @param sleepTime2 The time to sleep in the run method the second
         *            time.
         */
        public RunnableImplementation(long sleepTime1, long sleepTime2)
        {
            mySleepTime1 = sleepTime1;
            mySleepTime2 = sleepTime2;
        }

        /**
         * Get how many times this task was interrupted.
         *
         * @return The interrupted count.
         */
        public int getInterruptedCount()
        {
            return myInterruptedCount.get();
        }

        @Override
        public void run()
        {
            try
            {
                Thread.sleep(mySleepTime1);
            }
            catch (InterruptedException e)
            {
                myInterruptedCount.incrementAndGet();
            }
            try
            {
                Thread.sleep(mySleepTime2);
            }
            catch (InterruptedException e)
            {
                myInterruptedCount.incrementAndGet();
            }

            finally
            {
                synchronized (this)
                {
                    notifyAll();
                }
            }
        }
    }
}
