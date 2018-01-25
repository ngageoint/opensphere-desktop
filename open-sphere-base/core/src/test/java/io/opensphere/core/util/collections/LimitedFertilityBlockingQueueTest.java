package io.opensphere.core.util.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link LimitedFertilityBlockingQueue}. */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class LimitedFertilityBlockingQueueTest
{
    /** Error msg. */
    private static final String SHOULD_HAVE_TIMED_OUT = "Should have timed out.";

    /**
     * Test for {@link LimitedFertilityBlockingQueue#poll()}.
     */
    @Test
    public void testPoll()
    {
        LimitedFertilityBlockingQueue.Factory<Integer> factory = new FactoryImplementation();
        LimitedFertilityBlockingQueue<Integer> queue = new LimitedFertilityBlockingQueue<>(10, factory);

        for (int index = 0; index < 10; ++index)
        {
            Assert.assertEquals(Integer.valueOf(index), queue.poll());
        }
        Assert.assertNull(queue.poll());

        Assert.assertTrue(queue.add(Integer.valueOf(11)));
        Assert.assertEquals(Integer.valueOf(11), queue.poll());
        Assert.assertNull(queue.poll());
    }

    /**
     * Test for
     * {@link LimitedFertilityBlockingQueue#poll(LimitedFertilityBlockingQueue.Factory)}
     * .
     */
    @Test
    public void testPollFactory()
    {
        LimitedFertilityBlockingQueue.Factory<Integer> factory = new FactoryImplementation();
        LimitedFertilityBlockingQueue<Integer> queue = new LimitedFertilityBlockingQueue<>(10, null);

        for (int index = 0; index < 10; ++index)
        {
            Assert.assertEquals(Integer.valueOf(index), queue.poll(factory));
        }
        Assert.assertNull(queue.poll(factory));

        Assert.assertTrue(queue.add(Integer.valueOf(11)));
        Assert.assertEquals(Integer.valueOf(11), queue.poll(factory));
        Assert.assertNull(queue.poll());
    }

    /**
     * Test for
     * {@link LimitedFertilityBlockingQueue#poll(LimitedFertilityBlockingQueue.Factory, java.util.function.Consumer)}
     * .
     */
    @Test
    public void testPollFactoryConsumer()
    {
        LimitedFertilityBlockingQueue.Factory<Integer> factory = new FactoryImplementation();
        ConsumerImplementation consumer = new ConsumerImplementation();
        LimitedFertilityBlockingQueue<Integer> queue = new LimitedFertilityBlockingQueue<>(10, null);

        for (int index = 0; index < 10; ++index)
        {
            Assert.assertEquals(Integer.valueOf(index), queue.poll(factory, consumer));
        }
        Assert.assertNull(queue.poll(factory, consumer));

        Assert.assertTrue(consumer.getValues().isEmpty());

        Assert.assertTrue(queue.add(Integer.valueOf(11)));
        Assert.assertEquals(Integer.valueOf(11), queue.poll(factory, consumer));
        Assert.assertNull(queue.poll());

        Assert.assertEquals(1, consumer.getValues().size());
        Assert.assertEquals(Integer.valueOf(11), consumer.getValues().get(0));
    }

    /**
     * Test for
     * {@link LimitedFertilityBlockingQueue#poll(LimitedFertilityBlockingQueue.Factory, long, TimeUnit)}
     * .
     *
     * @throws InterruptedException If the test fails.
     * @throws ExecutionException If the test fails.
     * @throws TimeoutException If the test fails.
     */
    @Test
    public void testPollFactoryLongTimeUnit() throws InterruptedException, ExecutionException, TimeoutException
    {
        final LimitedFertilityBlockingQueue.Factory<Integer> factory = new FactoryImplementation();
        final LimitedFertilityBlockingQueue<Integer> queue = new LimitedFertilityBlockingQueue<>(10, null);

        for (int index = 0; index < 10; ++index)
        {
            Assert.assertEquals(Integer.valueOf(index), queue.poll(factory, 1L, TimeUnit.NANOSECONDS));
        }

        Callable<Integer> task = new Callable<Integer>()
        {
            @Override
            public Integer call() throws InterruptedException
            {
                Integer poll = queue.poll(factory, 100L, TimeUnit.MILLISECONDS);
                return poll;
            }
        };
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<Integer> future1 = exec.submit(task);
        Assert.assertNull(future1.get());
        Future<Integer> future2 = exec.submit(task);
        try
        {
            future2.get(10L, TimeUnit.MILLISECONDS);
            Assert.fail(SHOULD_HAVE_TIMED_OUT);
        }
        catch (TimeoutException e)
        {
        }
        Assert.assertTrue(queue.add(Integer.valueOf(11)));
        Assert.assertEquals(Integer.valueOf(11), future2.get());
    }

    /**
     * Test for
     * {@link LimitedFertilityBlockingQueue#poll(io.opensphere.core.util.collections.LimitedFertilityBlockingQueue.Factory, Consumer, long, TimeUnit)}
     * .
     *
     * @throws InterruptedException If the test fails.
     * @throws ExecutionException If the test fails.
     * @throws TimeoutException If the test fails.
     */
    @Test
    public void testPollFactoryConsumerLongTimeUnit() throws InterruptedException, ExecutionException, TimeoutException
    {
        final LimitedFertilityBlockingQueue.Factory<Integer> factory = new FactoryImplementation();
        final ConsumerImplementation consumer = new ConsumerImplementation();
        final LimitedFertilityBlockingQueue<Integer> queue = new LimitedFertilityBlockingQueue<>(10, null);

        for (int index = 0; index < 10; ++index)
        {
            Assert.assertEquals(Integer.valueOf(index), queue.poll(factory, consumer, 1L, TimeUnit.NANOSECONDS));
        }

        Assert.assertEquals(0, consumer.getValues().size());

        Callable<Integer> task = new Callable<Integer>()
        {
            @Override
            public Integer call() throws InterruptedException
            {
                Integer poll = queue.poll(factory, consumer, 100L, TimeUnit.MILLISECONDS);
                return poll;
            }
        };
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<Integer> future1 = exec.submit(task);
        Assert.assertNull(future1.get());
        Future<Integer> future2 = exec.submit(task);
        try
        {
            future2.get(10L, TimeUnit.MILLISECONDS);
            Assert.fail(SHOULD_HAVE_TIMED_OUT);
        }
        catch (TimeoutException e)
        {
        }
        Assert.assertTrue(queue.add(Integer.valueOf(11)));
        Assert.assertEquals(Integer.valueOf(11), future2.get());

        Assert.assertEquals(1, consumer.getValues().size());
        Assert.assertEquals(Integer.valueOf(11), consumer.getValues().get(0));
    }

    /**
     * Test for {@link LimitedFertilityBlockingQueue#poll(long, TimeUnit)}.
     *
     * @throws InterruptedException If the test fails.
     * @throws ExecutionException If the test fails.
     * @throws TimeoutException If the test fails.
     */
    @Test
    public void testPollLongTimeUnit() throws InterruptedException, ExecutionException, TimeoutException
    {
        LimitedFertilityBlockingQueue.Factory<Integer> factory = new FactoryImplementation();
        final LimitedFertilityBlockingQueue<Integer> queue = new LimitedFertilityBlockingQueue<>(10, factory);

        for (int index = 0; index < 10; ++index)
        {
            Assert.assertEquals(Integer.valueOf(index), queue.poll(1L, TimeUnit.NANOSECONDS));
        }

        Callable<Integer> task = new Callable<Integer>()
        {
            @Override
            public Integer call() throws InterruptedException
            {
                Integer poll = queue.poll(100L, TimeUnit.MILLISECONDS);
                return poll;
            }
        };
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<Integer> future1 = exec.submit(task);
        Assert.assertNull(future1.get());
        Future<Integer> future2 = exec.submit(task);
        try
        {
            future2.get(10L, TimeUnit.MILLISECONDS);
            Assert.fail(SHOULD_HAVE_TIMED_OUT);
        }
        catch (TimeoutException e)
        {
        }
        Assert.assertTrue(queue.add(Integer.valueOf(11)));
        Assert.assertEquals(Integer.valueOf(11), future2.get());
    }

    /**
     * Test for {@link LimitedFertilityBlockingQueue#take()}.
     *
     * @throws InterruptedException If the test fails.
     * @throws ExecutionException If the test fails.
     */
    @Test
    public void testTake() throws InterruptedException, ExecutionException
    {
        LimitedFertilityBlockingQueue.Factory<Integer> factory = new FactoryImplementation();
        final LimitedFertilityBlockingQueue<Integer> queue = new LimitedFertilityBlockingQueue<>(10, factory);

        for (int index = 0; index < 10; ++index)
        {
            Assert.assertEquals(Integer.valueOf(index), queue.take());
        }

        Callable<Integer> task = new Callable<Integer>()
        {
            @Override
            public Integer call() throws InterruptedException
            {
                return queue.take();
            }
        };

        Future<Integer> future = Executors.newSingleThreadExecutor().submit(task);
        try
        {
            future.get(100L, TimeUnit.MILLISECONDS);
            Assert.fail(SHOULD_HAVE_TIMED_OUT);
        }
        catch (TimeoutException e)
        {
        }

        Assert.assertTrue(queue.add(Integer.valueOf(11)));
        Assert.assertEquals(Integer.valueOf(11), future.get());
    }

    /**
     * Test for
     * {@link LimitedFertilityBlockingQueue#take(LimitedFertilityBlockingQueue.Factory)}
     * .
     *
     * @throws InterruptedException If the test fails.
     * @throws ExecutionException If the test fails.
     */
    @Test
    public void testTakeFactory() throws InterruptedException, ExecutionException
    {
        final LimitedFertilityBlockingQueue.Factory<Integer> factory = new FactoryImplementation();
        final LimitedFertilityBlockingQueue<Integer> queue = new LimitedFertilityBlockingQueue<>(10, null);

        for (int index = 0; index < 10; ++index)
        {
            Assert.assertEquals(Integer.valueOf(index), queue.take(factory));
        }

        Callable<Integer> task = new Callable<Integer>()
        {
            @Override
            public Integer call() throws InterruptedException
            {
                return queue.take(factory);
            }
        };

        Future<Integer> future = Executors.newSingleThreadExecutor().submit(task);
        try
        {
            future.get(100L, TimeUnit.MILLISECONDS);
            Assert.fail(SHOULD_HAVE_TIMED_OUT);
        }
        catch (TimeoutException e)
        {
        }

        Assert.assertTrue(queue.add(Integer.valueOf(11)));
        Assert.assertEquals(Integer.valueOf(11), future.get());
    }

    /**
     * Test for
     * {@link LimitedFertilityBlockingQueue#take(LimitedFertilityBlockingQueue.Factory, Consumer)}
     * .
     *
     * @throws InterruptedException If the test fails.
     * @throws ExecutionException If the test fails.
     */
    @Test
    public void testTakeFactoryConsumer() throws InterruptedException, ExecutionException
    {
        final LimitedFertilityBlockingQueue.Factory<Integer> factory = new FactoryImplementation();
        final ConsumerImplementation consumer = new ConsumerImplementation();
        final LimitedFertilityBlockingQueue<Integer> queue = new LimitedFertilityBlockingQueue<>(10, null);

        for (int index = 0; index < 10; ++index)
        {
            Assert.assertEquals(Integer.valueOf(index), queue.take(factory, consumer));
        }

        Assert.assertTrue(consumer.getValues().isEmpty());

        Callable<Integer> task = new Callable<Integer>()
        {
            @Override
            public Integer call() throws InterruptedException
            {
                return queue.take(factory, consumer);
            }
        };

        Future<Integer> future = Executors.newSingleThreadExecutor().submit(task);
        try
        {
            future.get(100L, TimeUnit.MILLISECONDS);
            Assert.fail(SHOULD_HAVE_TIMED_OUT);
        }
        catch (TimeoutException e)
        {
        }

        Assert.assertTrue(queue.add(Integer.valueOf(11)));
        Assert.assertEquals(Integer.valueOf(11), future.get());

        Assert.assertEquals(1, consumer.getValues().size());
        Assert.assertEquals(Integer.valueOf(11), consumer.getValues().get(0));
    }

    /**
     * A consumer implementation.
     */
    private final class ConsumerImplementation implements Consumer<Integer>
    {
        /** The values received by the consumer implementation. */
        private final List<Integer> myValues = new ArrayList<>();

        @Override
        public void accept(Integer t)
        {
            myValues.add(t);
        }

        /**
         * Get the values received by consumer.
         *
         * @return The values received by consumer.
         */
        public List<Integer> getValues()
        {
            return myValues;
        }
    }

    /** A factory that returns increasing Integers. */
    private static final class FactoryImplementation implements LimitedFertilityBlockingQueue.Factory<Integer>
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** How many have been returned. */
        private int myCount;

        @Override
        public Integer create()
        {
            return Integer.valueOf(myCount++);
        }
    }
}
