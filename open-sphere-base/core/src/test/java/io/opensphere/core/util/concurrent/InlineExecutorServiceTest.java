package io.opensphere.core.util.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import io.opensphere.core.util.lang.ThreadUtilities;
import org.junit.Assert;

/**
 * Test for {@link InlineExecutorService}.
 */
public class InlineExecutorServiceTest
{
    /**
     * Test for {@link InlineExecutorService#awaitTermination(long, TimeUnit)}.
     *
     * @throws InterruptedException If the test is interrupted.
     */
    @Test
    public void testAwaitTermination() throws InterruptedException
    {
        ExecutorService pool = Executors.newFixedThreadPool(2);

        final CountDownLatch latch = new CountDownLatch(2);
        final AtomicBoolean flag1 = new AtomicBoolean();
        final AtomicBoolean flag2 = new AtomicBoolean();

        final ExecutorService service = new InlineExecutorService();
        pool.execute(new TestRunnable(service, latch, flag1, 100L));
        pool.execute(new TestRunnable(service, latch, flag2, 100L));

        latch.await();

        Assert.assertFalse(service.isShutdown());
        service.shutdown();
        Assert.assertTrue(service.isShutdown());
        Assert.assertFalse(service.isTerminated());
        Assert.assertFalse(service.awaitTermination(1, TimeUnit.MILLISECONDS));
        Assert.assertFalse(flag1.get());
        Assert.assertFalse(flag2.get());
        Assert.assertTrue(service.awaitTermination(1000, TimeUnit.MILLISECONDS));
        Assert.assertTrue(service.isShutdown());
        Assert.assertTrue(service.isTerminated());
        Assert.assertTrue(flag1.get());
        Assert.assertTrue(flag2.get());
    }

    /**
     * Test for {@link InlineExecutorService#shutdown()}.
     */
    @Test
    public void testShutdown()
    {
        Runnable command = new Runnable()
        {
            @Override
            public void run()
            {
            }
        };

        ExecutorService service = new InlineExecutorService();

        service.execute(command);

        Assert.assertFalse(service.isShutdown());

        service.shutdown();

        Assert.assertTrue(service.isShutdown());

        try
        {
            service.execute(command);
            Assert.fail("Command should not have been executed.");
        }
        catch (RejectedExecutionException e)
        {
            Assert.assertTrue(true);
        }
    }

    /**
     * Test for {@link InlineExecutorService#shutdownNow()}.
     *
     * @throws InterruptedException If the test is interrupted.
     */
    @Test
    public void testShutdownNow() throws InterruptedException
    {
        ExecutorService pool = Executors.newFixedThreadPool(2);

        final CountDownLatch latch = new CountDownLatch(2);
        final AtomicBoolean flag1 = new AtomicBoolean();
        final AtomicBoolean flag2 = new AtomicBoolean();

        final ExecutorService service = new InlineExecutorService();
        pool.execute(new TestRunnable(service, latch, flag1, 5000L));
        pool.execute(new TestRunnable(service, latch, flag2, 5000L));

        latch.await();

        Assert.assertFalse(service.isShutdown());
        service.shutdownNow();
        Assert.assertTrue(service.isShutdown());
        Assert.assertTrue(service.awaitTermination(1000L, TimeUnit.MILLISECONDS));
        Assert.assertTrue(flag1.get());
        Assert.assertTrue(flag2.get());
    }

    /**
     * Runnable to use in tests that submits an inner Runnable to an executor,
     * which counts down a latch when it starts running and sets a flag when
     * it's done.
     */
    private static class TestRunnable implements Runnable
    {
        /** A latch. */
        private final CountDownLatch myLatch;

        /** A flag. */
        private final AtomicBoolean myFlag;

        /** An executor to use. */
        private final Executor myExecutor;

        /** The time to sleep. */
        private final long mySleepMillis;

        /**
         * Constructor.
         *
         * @param executor An executor.
         * @param latch A latch.
         * @param flag A flag.
         * @param sleepMillis The time to sleep.
         */
        public TestRunnable(Executor executor, CountDownLatch latch, AtomicBoolean flag, long sleepMillis)
        {
            myExecutor = executor;
            myLatch = latch;
            myFlag = flag;
            mySleepMillis = sleepMillis;
        }

        @Override
        public void run()
        {
            myExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    myLatch.countDown();
                    ThreadUtilities.sleep(mySleepMillis);
                    myFlag.set(true);
                }
            });
        }
    }
}
