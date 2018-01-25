package io.opensphere.core.util.collections;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import org.junit.Assert;

/**
 * Test for {@link ConcurrentLazyMap}.
 */
public class ConcurrentLazyMapTest
{
    /**
     * Test for {@link ConcurrentLazyMap#get(Object)}.
     *
     * @throws ExecutionException If there's an error.
     * @throws InterruptedException If there's an error.
     */
    @Test
    public void testGet() throws InterruptedException, ExecutionException
    {
        final AtomicInteger runningCount = new AtomicInteger();
        final AtomicBoolean pause5 = new AtomicBoolean(true);
        final AtomicBoolean pause6 = new AtomicBoolean(true);
        Map<Integer, Float> map = Collections.synchronizedMap(New.<Integer, Float>map());
        final Map<Integer, Float> clm = new ConcurrentLazyMap<Integer, Float>(map, Integer.class,
                new LazyMap.Factory<Integer, Float>()
                {
                    @Override
                    public Float create(Integer key)
                    {
                        AtomicBoolean pause = key.intValue() == 5 ? pause5 : pause6;
                        synchronized (pause)
                        {
                            while (pause.get())
                            {
                                try
                                {
                                    pause.wait();
                                }
                                catch (InterruptedException e)
                                {
                                }
                            }
                        }

                        return new Float(key.floatValue());
                    }
                });

        ExecutorService exec = Executors.newFixedThreadPool(3);
        Future<Float> future5a = exec.submit(new Callable<Float>()
        {
            @Override
            public Float call()
            {
                synchronized (runningCount)
                {
                    runningCount.incrementAndGet();
                    runningCount.notifyAll();
                }

                // This will block until pause is set to false.
                return clm.get(Integer.valueOf(5));
            }
        });

        Future<Float> future5b = exec.submit(new Callable<Float>()
        {
            @Override
            public Float call()
            {
                synchronized (runningCount)
                {
                    runningCount.incrementAndGet();
                    runningCount.notifyAll();
                }

                // This will block until pause is set to false.
                return clm.get(Integer.valueOf(5));
            }
        });

        Future<Float> future6 = exec.submit(new Callable<Float>()
        {
            @Override
            public Float call()
            {
                synchronized (runningCount)
                {
                    runningCount.incrementAndGet();
                    runningCount.notifyAll();
                }

                // This will block until pause is set to false.
                return clm.get(Integer.valueOf(6));
            }
        });

        // Make sure everything is running.
        synchronized (runningCount)
        {
            while (runningCount.get() < 3)
            {
                runningCount.wait();
            }
        }

        Assert.assertFalse(future5a.isDone());
        Assert.assertFalse(future5b.isDone());
        Assert.assertFalse(future6.isDone());

        // Let the request for 6 to run.
        synchronized (pause6)
        {
            pause6.set(false);
            pause6.notifyAll();
        }

        // Test that the request for 6 can complete while the requests for 5 are
        // still blocked.
        Assert.assertEquals(6f, future6.get().floatValue(), 0f);

        Assert.assertFalse(future5a.isDone());
        Assert.assertFalse(future5b.isDone());
        Assert.assertTrue(future6.isDone());

        // Let the requests for 5 run.
        synchronized (pause5)
        {
            pause5.set(false);
            pause5.notifyAll();
        }

        Assert.assertSame(future5a.get(), future5b.get());
        Assert.assertEquals(5f, future5a.get().floatValue(), 0f);
    }
}
