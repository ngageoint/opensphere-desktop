package io.opensphere.core.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Ignore;
import org.junit.Test;

import org.junit.Assert;

/**
 * Test for {@link FixedThreadPoolExecutor}.
 */
public class FixedThreadPoolExecutorTest
{
    /**
     * Run some tasks through an executor and get the average latency.
     *
     * @param executor The executor.
     * @param numTasks The number of tasks to execute.
     * @return The average latency in nanoseconds between a task being submitted
     *         and a task running.
     * @throws InterruptedException If the thread is interrupted.
     */
    protected static double testExecutor(Executor executor, int numTasks) throws InterruptedException
    {
        List<TestTask> list = new ArrayList<>(numTasks);
        for (int i = 0; i < numTasks; i++)
        {
            list.add(new TestTask());
        }
        for (TestTask task : list)
        {
            task.submit();
            executor.execute(task);
        }

        long time = 0;
        for (TestTask task : list)
        {
            time += task.getTimeToRun();
        }

        return (double)time / numTasks;
    }

    /**
     * This test verifies that {@link FixedThreadPoolExecutor} has lower latency
     * than {@link ThreadPoolExecutor}.
     *
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    @Ignore
    public void testExecute() throws InterruptedException
    {
        ThreadFactory factory = new ThreadFactoryImplementation(null);

        int nThreads = 20;
        int iterations = 100;
        double jdkLatency = 0.;
        double optimizedLatency = 0.;
        int totalIterations = 0;
        for (int numTasks = 1; numTasks <= 10000; numTasks *= 5)
        {
            totalIterations += iterations;
            for (int i = 0; i < iterations; i++)
            {
                ExecutorService jdkExecutor = Executors.newFixedThreadPool(nThreads, factory);

                jdkLatency += testExecutor(jdkExecutor, numTasks);
                jdkExecutor.shutdownNow();
            }
            for (int i = 0; i < iterations; i++)
            {
                FixedThreadPoolExecutor optimizedExecutor = new FixedThreadPoolExecutor(nThreads, factory);
                optimizedLatency += testExecutor(optimizedExecutor, numTasks);
                optimizedExecutor.shutdownNow();
            }
        }
        Assert.assertTrue("Average latency for " + FixedThreadPoolExecutor.class.getName() + " was "
                + (float)(optimizedLatency / totalIterations / 1e6) + " ms, which is slower than "
                + ThreadPoolExecutor.class.getName() + ", which took " + (float)(jdkLatency / totalIterations / 1e6) + " ms.",
                jdkLatency > optimizedLatency);
    }

    /**
     * Test for {@link ExecutorService#shutdown()} that ensures that the current
     * task is not interrupted and all queued tasks are allowed to complete.
     *
     * @throws InterruptedException If the test is interrupted.
     * @throws ExecutionException If the test fails.
     */
    @Test
    public void testShutdown() throws InterruptedException, ExecutionException
    {
        ThreadGroup group = createTestThreadGroup();
        ThreadFactory factory = new ThreadFactoryImplementation(group);
        FixedThreadPoolExecutor executor = new FixedThreadPoolExecutor(1, factory);

        Runnable task1 = EasyMock.createStrictMock(Runnable.class);
        task1.run();
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer() throws InterruptedException
            {
                Thread.sleep(500);
                return null;
            }
        });
        EasyMock.replay(task1);

        Runnable task2 = EasyMock.createStrictMock(Runnable.class);
        task2.run();
        EasyMock.replay(task2);

        executor.execute(task1);
        executor.execute(task2);

        executor.shutdown();

        Assert.assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        Thread.yield();
        if (group.activeCount() > 0)
        {
            // Try sleeping a bit to see if the threads finish up.
            Thread.sleep(500);
        }
        Assert.assertEquals(0, group.activeCount());

        EasyMock.verify(task1);
        EasyMock.verify(task2);
    }

    /**
     * Test for {@link ExecutorService#shutdown()} that ensures that new tasks
     * are not accepted.
     */
    @Test(expected = RejectedExecutionException.class)
    public void testShutdownLateSubmission()
    {
        ThreadFactory factory = new ThreadFactoryImplementation(createTestThreadGroup());
        FixedThreadPoolExecutor executor = new FixedThreadPoolExecutor(5, factory);
        executor.shutdown();

        // This should generate the exception.
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
            }
        });
    }

    /**
     * Test for {@link ExecutorService#shutdownNow()} that ensures that the
     * current task is interrupted and queued tasks are not run.
     *
     * @throws InterruptedException If the test is interrupted.
     */
    @Test
    public void testShutdownNow() throws InterruptedException
    {
        if (StringUtils.isEmpty(System.getenv("SLOW_MACHINE")))
        {
            ThreadGroup group = createTestThreadGroup();
            ThreadFactory factory = new ThreadFactoryImplementation(group);
            FixedThreadPoolExecutor executor = new FixedThreadPoolExecutor(1, factory);

            final CountDownLatch latch = new CountDownLatch(1);

            Runnable task1 = EasyMock.createStrictMock(Runnable.class);
            task1.run();
            EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
            {
                @Override
                public Void answer() throws InterruptedException
                {
                    latch.countDown();
                    Thread.sleep(500);
                    return null;
                }
            });
            EasyMock.replay(task1);

            Runnable task2 = EasyMock.createStrictMock(Runnable.class);
            EasyMock.replay(task2);
            Runnable task3 = EasyMock.createStrictMock(Runnable.class);
            EasyMock.replay(task3);
            Runnable task4 = EasyMock.createStrictMock(Runnable.class);
            EasyMock.replay(task4);

            Future<?> future1 = executor.submit(task1);
            executor.execute(task2);
            executor.execute(task3);
            executor.execute(task4);

            Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));

            List<Runnable> unrun = executor.shutdownNow();
            Assert.assertEquals(3, unrun.size());
            Assert.assertEquals(task2, unrun.get(0));
            Assert.assertEquals(task3, unrun.get(1));
            Assert.assertEquals(task4, unrun.get(2));

            Assert.assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

            Thread.yield();
            if (group.activeCount() > 0)
            {
                // Try sleeping a bit to see if the threads finish up.
                Thread.sleep(500);
            }
            Assert.assertEquals(0, group.activeCount());

            try
            {
                future1.get();
                Assert.fail("Should have been interrupted.");
            }
            catch (ExecutionException e)
            {
                Assert.assertEquals(InterruptedException.class, e.getCause().getCause().getClass());
            }

            EasyMock.verify(task1);
        }
    }

    /**
     * Test for {@link ExecutorService#shutdownNow()} that ensures that new
     * tasks are not accepted.
     */
    @Test(expected = RejectedExecutionException.class)
    public void testShutdownNowLateSubmission()
    {
        ThreadFactory factory = new ThreadFactoryImplementation(createTestThreadGroup());
        FixedThreadPoolExecutor executor = new FixedThreadPoolExecutor(5, factory);
        executor.shutdownNow();

        // This should generate the exception.
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
            }
        });
    }

    /**
     * Test for {@link ExecutorService#shutdown()} that ensures that the
     * executor shuts down when it has no tasks.
     *
     * @throws InterruptedException If the test is interrupted.
     * @throws ExecutionException If the test fails.
     */
    @Test
    public void testShutdownWithNoTasks() throws InterruptedException, ExecutionException
    {
        ThreadGroup group = createTestThreadGroup();
        ThreadFactory factory = new ThreadFactoryImplementation(group);
        FixedThreadPoolExecutor executor = new FixedThreadPoolExecutor(1, factory);

        // Run a future to make sure the executor is spun up.
        Future<?> future = executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
            }
        });
        future.get();

        executor.shutdown();

        Assert.assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));

        Thread.yield();
        if (group.activeCount() > 0)
        {
            // Try sleeping a bit to see if the threads finish up.
            Thread.sleep(500);
        }
        Assert.assertEquals(0, group.activeCount());
    }

    /**
     * Create a thread group for testing.
     *
     * @return The thread group.
     */
    private ThreadGroup createTestThreadGroup()
    {
        return new ThreadGroup("test");
    }

    /** A task to send in to the executor. */
    protected static class TestTask implements Runnable
    {
        /** Flag indicating if the task has run. */
        private boolean myComplete;

        /** The time this task was run by the executor. */
        private long myRunTime;

        /** The time this task was submitted to the executor. */
        private long mySubmitTime;

        /**
         * Get the number of nanoseconds it took to run this task after it was
         * submitted.
         *
         * @return The delta between the submit time and the run time.
         * @throws InterruptedException If the thread is interrupted.
         */
        public long getTimeToRun() throws InterruptedException
        {
            synchronized (this)
            {
                while (!myComplete)
                {
                    wait();
                }
            }
            return myRunTime - mySubmitTime;
        }

        @Override
        public void run()
        {
            myRunTime = System.nanoTime();

            synchronized (this)
            {
                myComplete = true;
                notifyAll();
            }
        }

        /**
         * Method called immediately before this task is submitted to the
         * executor.
         */
        public void submit()
        {
            mySubmitTime = System.nanoTime();
        }
    }

    /**
     * A basic thread factory that simply creates new threads.
     */
    protected static class ThreadFactoryImplementation implements ThreadFactory
    {
        /** The thread group, if any. */
        private final ThreadGroup myGroup;

        /**
         * Construct the factory.
         *
         * @param group The thread group, or <code>null</code>.
         */
        public ThreadFactoryImplementation(ThreadGroup group)
        {
            myGroup = group;
        }

        @Override
        public Thread newThread(Runnable r)
        {
            return new Thread(myGroup, r);
        }
    }
}
