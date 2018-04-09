package io.opensphere.core.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.lang.ThreadUtilities;
import org.junit.Assert;

/**
 * Test for {@link ProcrastinatingExecutor}.
 */
public class ProcrastinatingExecutorTest
{
    /**
     * Specific test for {@link ProcrastinatingExecutor#execute(Runnable)} that
     * submits one runner, waits for that one to start, and then submits another
     * runner while the first runner is still running. Then it verifies that the
     * delay before the second runner runs is correct.
     *
     * @throws InterruptedException If the executor is interrupted somehow.
     */
    @Test
    public void testLastRunner() throws InterruptedException
    {
        if (StringUtils.isEmpty(System.getenv("SLOW_MACHINE")))
        {
            int delayMilliseconds = 100;
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
            ProcrastinatingExecutor procrastinator = new ProcrastinatingExecutor(executor, delayMilliseconds);

            final AtomicReference<String> failureMsg = new AtomicReference<>();
            final AtomicLong lastRun = new AtomicLong();
            final AtomicLong lastSubmission = new AtomicLong();

            long sleepTimeMilliseconds = 500L;

            RunnableImplementation runner1 = new RunnableImplementation(sleepTimeMilliseconds, delayMilliseconds, 200, failureMsg,
                    lastRun, lastSubmission);
            RunnableImplementation runner2 = new RunnableImplementation(sleepTimeMilliseconds, delayMilliseconds, 200, failureMsg,
                    lastRun, lastSubmission);

            lastSubmission.set(System.nanoTime());
            procrastinator.execute(runner1);
            waitForRunner(runner1);
            lastSubmission.set(System.nanoTime());
            procrastinator.execute(runner2);
            waitForRunner(runner2);

            shutdownExecutor(executor);

            String msg = failureMsg.get();
            if (msg != null)
            {
                Assert.fail(msg);
            }
        }
    }

    /**
     * Test the maximum delay of a {@link ProcrastinatingExecutor}.
     *
     * @throws InterruptedException If the executor is interrupted somehow.
     */
    @Test
    public void testMaxDelay() throws InterruptedException
    {
        if (StringUtils.isEmpty(System.getenv("SLOW_MACHINE")))
        {
            final int minDelayMilliseconds = 200;
            final int maxDelayMilliseconds = 500;
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            ProcrastinatingExecutor procrastinator = new ProcrastinatingExecutor(executor, minDelayMilliseconds,
                    maxDelayMilliseconds);

            final AtomicReference<String> failureMsg = new AtomicReference<>();
            final AtomicLong lastRun = new AtomicLong();
            final AtomicLong lastSubmission = new AtomicLong();

            // Submit a new task every 50 ms so it keeps procrastinating. Once
            // 500
            // ms has passed, it should go ahead and run the latest task.

            long t0 = System.nanoTime();
            List<RunnableImplementation> runners = new ArrayList<>();
            while (failureMsg.get() == null)
            {
                RunnableImplementation runner = new RunnableImplementation(0, minDelayMilliseconds, maxDelayMilliseconds,
                        failureMsg, lastRun, lastSubmission);
                lastSubmission.set(System.nanoTime());
                procrastinator.execute(runner);

                ThreadUtilities.sleep(50);

                runners.add(runner);
            }

            Assert.assertTrue(lastRun.get() - t0 > maxDelayMilliseconds);

            for (int i = 0; i < runners.size(); ++i)
            {
                Assert.assertTrue(runners.get(i).isCalled() == (i == runners.size() - 1));
            }

            shutdownExecutor(executor);
        }
    }

    /**
     * Test the minimum delay of a {@link ProcrastinatingExecutor}.
     *
     * @throws InterruptedException If the executor is interrupted somehow.
     */
    @Test
    public void testMinDelay() throws InterruptedException
    {
        if (StringUtils.isEmpty(System.getenv("SLOW_MACHINE")))
        {
            final int delayMilliseconds = 100;
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            ProcrastinatingExecutor procrastinator = new ProcrastinatingExecutor(executor, delayMilliseconds);

            final AtomicReference<String> failureMsg = new AtomicReference<>();
            final AtomicLong lastRun = new AtomicLong();
            final AtomicLong lastSubmission = new AtomicLong();

            for (long sleepTimeMilliseconds = 5L; sleepTimeMilliseconds < 640L; sleepTimeMilliseconds *= 2L)
            {
                lastSubmission.set(System.nanoTime());
                List<RunnableImplementation> runners = new ArrayList<>();
                for (long delayBetweenSubmissions = 10L;; delayBetweenSubmissions *= 2L)
                {
                    RunnableImplementation runner = new RunnableImplementation(sleepTimeMilliseconds, delayMilliseconds,
                            Integer.MAX_VALUE, failureMsg, lastRun, lastSubmission);

                    // Try to avoid false failures by checking this at the last
                    // moment.
                    if (System.nanoTime() - lastSubmission.get() < delayMilliseconds * Constants.NANO_PER_MILLI)
                    {
                        procrastinator.execute(runner);
                        lastSubmission.set(System.nanoTime());
                    }
                    else
                    {
                        break;
                    }

                    ThreadUtilities.sleep(delayBetweenSubmissions);

                    runners.add(runner);
                }

                // Wait for the last runner to start.
                RunnableImplementation lastRunner = runners.get(runners.size() - 1);
                waitForRunner(lastRunner);

                // Make sure the other runners were not called.
                for (int index = 0; index < runners.size() - 1; ++index)
                {
                    Assert.assertFalse(runners.get(index).isCalled());
                }

                RunnableImplementation runner = new RunnableImplementation(sleepTimeMilliseconds, delayMilliseconds,
                        Integer.MAX_VALUE, failureMsg, lastRun, lastSubmission);

                lastSubmission.set(System.nanoTime());
                procrastinator.execute(runner);

                waitForRunner(runner);
            }

            shutdownExecutor(executor);

            String msg = failureMsg.get();
            if (msg != null)
            {
                Assert.fail(msg);
            }
        }
    }

    /**
     * Test
     * {@link ProcrastinatingExecutor#ProcrastinatingExecutor(ScheduledExecutorService, int, int)}
     * with a min delay greater than the max delay.
     *
     * @throws InterruptedException If the executor is interrupted somehow.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testMinDelayGreaterThanMaxDelay() throws InterruptedException
    {
        final int minDelayMilliseconds = 200;
        final int maxDelayMilliseconds = 199;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        new ProcrastinatingExecutor(executor, minDelayMilliseconds, maxDelayMilliseconds);
        shutdownExecutor(executor);
    }

    /**
     * Test
     * {@link ProcrastinatingExecutor#ProcrastinatingExecutor(ScheduledExecutorService, int)}
     * with a negative delay.
     *
     * @throws InterruptedException If the executor is interrupted somehow.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMinDelay1() throws InterruptedException
    {
        final int minDelayMilliseconds = -1;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        new ProcrastinatingExecutor(executor, minDelayMilliseconds);
        shutdownExecutor(executor);
    }

    /**
     * Test
     * {@link ProcrastinatingExecutor#ProcrastinatingExecutor(ScheduledExecutorService, int, int)}
     * with a negative delay.
     *
     * @throws InterruptedException If the executor is interrupted somehow.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMinDelay2() throws InterruptedException
    {
        final int minDelayMilliseconds = -1;
        final int maxDelayMilliseconds = 200;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        new ProcrastinatingExecutor(executor, minDelayMilliseconds, maxDelayMilliseconds);
        shutdownExecutor(executor);
    }

    /**
     * Test a {@link ProcrastinatingExecutor} with zero delay.
     *
     * @throws InterruptedException If the executor is interrupted somehow.
     */
    @Test
    public void testZeroDelay() throws InterruptedException
    {
        if (StringUtils.isEmpty(System.getenv("SLOW_MACHINE")))
        {
            // Use the FixedThreadPoolExecutor for this test because the
            // execution latency of ThreadPoolExecutor can get up to several
            // milliseconds
            // when tasks are submitted to it rapidly.
            ExecutorService executor = new FixedThreadPoolExecutor(1);
            ProcrastinatingExecutor procrastinator = new ProcrastinatingExecutor(executor);

            final AtomicReference<String> failureMsg = new AtomicReference<>();
            final AtomicLong lastRun = new AtomicLong();
            final AtomicLong lastSubmission = new AtomicLong();

            // Submit tasks in quick succession. Some of them will get cancelled
            // if they overlap, but the interval between executions should
            // remain small.

            int minDelayMilliseconds = 0;
            int maxDelayMilliseconds = 30;
            RunnableImplementation runner = null;
            for (int i = 0; i < 1000000 && failureMsg.get() == null; ++i)
            {
                runner = new RunnableImplementation(0, minDelayMilliseconds, maxDelayMilliseconds, failureMsg, lastRun,
                        lastSubmission);
                lastSubmission.set(System.nanoTime());
                procrastinator.execute(runner);
            }

            shutdownExecutor(executor);

            String msg = failureMsg.get();
            if (msg != null)
            {
                Assert.fail(msg);
            }

            // Ensure that the last runner was called.
            Assert.assertTrue(runner != null && runner.isCalled());
        }
    }

    /**
     * Test a {@link ProcrastinatingExecutor} with zero delay. This test checks
     * that when long-running tasks are submitted concurrently, the last one
     * gets executed.
     *
     * @throws InterruptedException If the executor is interrupted somehow.
     */
    @Test
    public void testZeroDelay2() throws InterruptedException
    {
        // Use the FixedThreadPoolExecutor for this test because the execution
        // latency of ThreadPoolExecutor can get up to several milliseconds when
        // tasks are submitted to it rapidly.
        ExecutorService executor = new FixedThreadPoolExecutor(10);
        ProcrastinatingExecutor procrastinator = new ProcrastinatingExecutor(executor);

        final AtomicReference<String> failureMsg = new AtomicReference<>();
        final AtomicLong lastRun = new AtomicLong();
        final AtomicLong lastSubmission = new AtomicLong();

        // Submit tasks in quick succession. Some of them will get cancelled if
        // they overlap, but the interval between executions should remain
        // small.

        int minDelayMilliseconds = 0;
        int maxDelayMilliseconds = 10;
        RunnableImplementation runner = null;
        for (int i = 0; i < 100000 && failureMsg.get() == null; ++i)
        {
            runner = new RunnableImplementation(1000, minDelayMilliseconds, maxDelayMilliseconds, failureMsg, lastRun,
                    lastSubmission);
            lastSubmission.set(System.nanoTime());
            procrastinator.execute(runner);
        }

        // Ensure that the last runner was called.
        waitForRunner(runner);

        shutdownExecutor(executor);

        String msg = failureMsg.get();
        if (msg != null)
        {
            Assert.fail(msg);
        }
    }

    /**
     * Shutdown the executor or fail after 10 seconds.
     *
     * @param executor The executor.
     * @throws InterruptedException If the wait is interrupted.
     */
    private void shutdownExecutor(ExecutorService executor) throws InterruptedException
    {
        executor.shutdown();
        if (!executor.awaitTermination(10L, TimeUnit.SECONDS))
        {
            Assert.fail("Executor did not terminate in time.");
        }
    }

    /**
     * Wait for a runner to be started.
     *
     * @param runner The runner.
     * @throws InterruptedException If the wait call is interrupted.
     */
    private void waitForRunner(RunnableImplementation runner) throws InterruptedException
    {
        synchronized (runner)
        {
            while (!runner.isCalled())
            {
                runner.wait(1000L);
                if (!runner.isCalled())
                {
                    Assert.fail("Timeout waiting for runner to be called.");
                }
            }
        }
    }

    /**
     * A runnable for the test.
     */
    private static final class RunnableImplementation implements Runnable
    {
        /** Flag indicating if this runnable has been run. */
        private boolean myCalled;

        /** The expected minimum delay between runs. */
        private final int myMinDelayMilliseconds;

        /** The failure message. */
        private final AtomicReference<String> myFailureMsg;

        /** The time that the last run completed. */
        private final AtomicLong myLastRun;

        /** The time that the last runnable was submitted. */
        private final AtomicLong myLastSubmission;

        /** How much time for this runnable to sleep. */
        private final long mySleepTimeMilliseconds;

        /** The expected maximum delay between runs. */
        private final int myMaxDelayMilliseconds;

        /**
         * Constructor.
         *
         * @param sleepTimeMilliseconds How much time for this runnable to
         *            sleep.
         * @param minDelayMilliseconds The expected minimum delay between runs.
         * @param maxDelayMilliseconds The expected maximum delay between runs.
         * @param failureMsg Atomic reference that holds a failure message.
         * @param lastRun Atomic long that holds the time that the last run
         *            completed.
         * @param lastSubmission Atomic long that holds the time that the last
         *            run was submitted.
         */
        public RunnableImplementation(long sleepTimeMilliseconds, int minDelayMilliseconds, int maxDelayMilliseconds,
                AtomicReference<String> failureMsg, AtomicLong lastRun, AtomicLong lastSubmission)
        {
            mySleepTimeMilliseconds = sleepTimeMilliseconds;
            myMinDelayMilliseconds = minDelayMilliseconds;
            myMaxDelayMilliseconds = maxDelayMilliseconds;
            myFailureMsg = failureMsg;
            myLastRun = lastRun;
            myLastSubmission = lastSubmission;
        }

        @Override
        public void run()
        {
            long lastSubmission = myLastSubmission.get();

            synchronized (this)
            {
                myCalled = true;
                notifyAll();
            }

            long lastTime = myLastRun.get();

            long now = System.nanoTime();
            long et;
            if (lastTime > 0)
            {
                et = (now - lastTime) / Constants.NANO_PER_MILLI;
                if (et < myMinDelayMilliseconds)
                {
                    myFailureMsg.set(
                            "Time between executions was " + et + " ms, but should have been at least " + myMinDelayMilliseconds);
                }
                else if (et > myMaxDelayMilliseconds)
                {
                    myFailureMsg.set(
                            "Time between executions was " + et + " ms, but should have been at most " + myMaxDelayMilliseconds);
                }
            }

            et = (System.nanoTime() - lastSubmission) / Constants.NANO_PER_MILLI;
            if (et < myMinDelayMilliseconds)
            {
                myFailureMsg.set("Time between submission and execution was " + et + " ms, but should have been at least "
                        + myMinDelayMilliseconds);
            }

            if (mySleepTimeMilliseconds > 0)
            {
                ThreadUtilities.sleep(mySleepTimeMilliseconds);
            }

            myLastRun.set(System.nanoTime());
        }

        /**
         * Accessor for the called.
         *
         * @return The called.
         */
        synchronized boolean isCalled()
        {
            return myCalled;
        }
    }
}
