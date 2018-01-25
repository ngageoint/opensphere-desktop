package io.opensphere.core.util.concurrent;

import org.junit.Test;

import org.junit.Assert;

/**
 * Test for {@link InlineExecutor}.
 */
public class InlineExecutorTest
{
    /**
     * Test for {@link InlineExecutor#execute(Runnable)}.
     */
    @Test
    public void testExecute()
    {
        final Thread[] result = new Thread[1];
        new InlineExecutor().execute(new Runnable()
        {
            @Override
            public void run()
            {
                result[0] = Thread.currentThread();
            }
        });

        Assert.assertEquals(Thread.currentThread(), result[0]);
    }
}
