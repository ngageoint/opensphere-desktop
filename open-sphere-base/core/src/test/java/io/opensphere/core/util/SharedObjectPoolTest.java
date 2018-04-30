package io.opensphere.core.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link SharedObjectPool}.
 */
public class SharedObjectPoolTest
{
    /**
     * Test for {@link SharedObjectPool#get(Object)}.
     */
    @Test
    @SuppressWarnings({ "PMD.IntegerInstantiation", "deprecation" })
    public void testGet()
    {
        SharedObjectPool<Integer> pool = new SharedObjectPool<>();

        Integer first = new Integer(1);
        Integer second = new Integer(1);
        Integer two = new Integer(2);

        Assert.assertNotSame(first, second);
        Assert.assertSame(first, pool.get(first));
        Assert.assertSame(first, pool.get(second));
        Assert.assertNotSame(second, pool.get(second));
        Assert.assertSame(two, pool.get(two));

        // Test that the pool is not holding on to references.
        first = new Integer(1);
        System.gc();
        Assert.assertSame(first, pool.get(first));
    }

    /**
     * Test for {@link SharedObjectPool#remove(Object)}.
     */
    @Test
    @SuppressWarnings({ "PMD.IntegerInstantiation", "deprecation" })
    public void testRemove()
    {
        SharedObjectPool<Integer> pool = new SharedObjectPool<>();

        Integer first = new Integer(1);
        Integer second = new Integer(1);

        Assert.assertNotSame(first, second);
        Assert.assertSame(first, pool.get(first));
        Assert.assertSame(first, pool.get(second));
        pool.remove(second);
        Assert.assertNotSame(first, pool.get(second));
        Assert.assertSame(second, pool.get(second));
    }
}
