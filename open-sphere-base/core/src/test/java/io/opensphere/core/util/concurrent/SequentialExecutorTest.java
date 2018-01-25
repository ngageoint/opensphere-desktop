package io.opensphere.core.util.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link SequentialExecutor}.
 */
public class SequentialExecutorTest
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SequentialExecutorTest.class);

    /**
     * Test for {@link SequentialExecutor}.
     *
     * @throws InterruptedException If the test is interrupted.
     */
    @Test
    public void test() throws InterruptedException
    {
        if (StringUtils.isEmpty(System.getenv("SLOW_MACHINE")))
        {
            final ExecutorService actualExecutor = Executors.newFixedThreadPool(5);
            SequentialExecutor executor = new SequentialExecutor(actualExecutor);

            int count = 100;
            TestRunnable[] arr = new TestRunnable[count];
            for (int index = 0; index < count;)
            {
                arr[index++] = new TestRunnable();
            }
            for (int index = 0; index < count;)
            {
                executor.execute(arr[index++]);
            }
            executor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    actualExecutor.shutdown();
                }
            });
            actualExecutor.awaitTermination(1L, TimeUnit.SECONDS);

            for (int index = 1; index < count; ++index)
            {
                Assert.assertTrue("Runnable at index " + (index - 1) + " did not run", arr[index - 1].getRunStop() > 0L);
                Assert.assertTrue("Start time at index " + index + " [" + arr[index].getRunStart()
                        + "] should have been >= stop time of index " + (index - 1) + " [" + arr[index - 1].getRunStop() + "]",
                        arr[index].getRunStart() >= arr[index - 1].getRunStop());
            }
        }
    }

    /** A Runnable for testing. */
    private static class TestRunnable implements Runnable
    {
        /** The start run time. */
        private long myRunStart;

        /** The end run time. */
        private long myRunStop;

        /**
         * Get the run start time.
         *
         * @return The time.
         */
        public long getRunStart()
        {
            return myRunStart;
        }

        /**
         * Get the run stop time.
         *
         * @return The time.
         */
        public long getRunStop()
        {
            return myRunStop;
        }

        @Override
        public void run()
        {
            myRunStart = System.nanoTime();
            try
            {
                Thread.sleep(1L);
            }
            catch (InterruptedException e)
            {
                LOGGER.warn(e, e);
            }
            myRunStop = System.nanoTime();
        }
    }
}
