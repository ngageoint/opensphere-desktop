package io.opensphere.core.util.collections;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link CompactLongArrayList}.
 */
public class CompactLongArrayListTest
{
    /** Run through the common operations. */
    @Test
    public void testAll()
    {
        CompactLongArrayList list = new CompactLongArrayList();

        list.add(1000000000000L);
        list.add(1000000000010L);
        list.add(1000000000020L);

        list.add(0L);
        list.add(10L);
        list.add(20L);

        list.add(1000000000000L);
        list.add(999999999999L);

        list.add(-100L);
        list.add(-200L);

        int i = 0;
        Assert.assertEquals(1000000000000L, list.get(i++));
        Assert.assertEquals(1000000000010L, list.get(i++));
        Assert.assertEquals(1000000000020L, list.get(i++));
        Assert.assertEquals(0L, list.get(i++));
        Assert.assertEquals(10L, list.get(i++));
        Assert.assertEquals(20L, list.get(i++));
        Assert.assertEquals(1000000000000L, list.get(i++));
        Assert.assertEquals(999999999999L, list.get(i++));
        Assert.assertEquals(-100L, list.get(i++));
        Assert.assertEquals(-200L, list.get(i++));
        Assert.assertEquals(4, list.getBlockSize());
        Assert.assertEquals(10, list.size());

        list.removeAt(4);

        i = 0;
        Assert.assertEquals(1000000000000L, list.get(i++));
        Assert.assertEquals(1000000000010L, list.get(i++));
        Assert.assertEquals(1000000000020L, list.get(i++));
        Assert.assertEquals(0L, list.get(i++));
        Assert.assertEquals(20L, list.get(i++));
        Assert.assertEquals(1000000000000L, list.get(i++));
        Assert.assertEquals(999999999999L, list.get(i++));
        Assert.assertEquals(-100L, list.get(i++));
        Assert.assertEquals(-200L, list.get(i++));
        Assert.assertEquals(4, list.getBlockSize());
        Assert.assertEquals(9, list.size());

        list.removeAt(4);
        list.removeAt(3);

        i = 0;
        Assert.assertEquals(1000000000000L, list.get(i++));
        Assert.assertEquals(1000000000010L, list.get(i++));
        Assert.assertEquals(1000000000020L, list.get(i++));
        Assert.assertEquals(1000000000000L, list.get(i++));
        Assert.assertEquals(999999999999L, list.get(i++));
        Assert.assertEquals(-100L, list.get(i++));
        Assert.assertEquals(-200L, list.get(i++));
        Assert.assertEquals(3, list.getBlockSize());
        Assert.assertEquals(7, list.size());

        list.clear();

        Assert.assertEquals(0, list.getBlockSize());
        Assert.assertEquals(0, list.size());
    }
}
