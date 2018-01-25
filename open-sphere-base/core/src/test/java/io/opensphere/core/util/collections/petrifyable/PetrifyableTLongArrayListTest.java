package io.opensphere.core.util.collections.petrifyable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import gnu.trove.function.TLongFunction;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.procedure.TLongProcedure;

/**
 * Test for {@link PetrifyableTLongArrayList}.
 */
public class PetrifyableTLongArrayListTest
{
    /** A petrified list for testing. */
    private static final PetrifyableTLongArrayList PETRIFIED_LIST = getPetrifiedList();

    /** A long for testing. */
    private static final long TEST_LONG = 10L;

    /** A collection of Longs for testing. */
    private static final Collection<Long> TEST_LONG_COLLECTION = Collections.singleton(Long.valueOf(TEST_LONG));

    /** A long array for testing. */
    private static final long[] TEST_LONG_ARRAY = new long[] { TEST_LONG };

    /** A long array list for testing. */
    private static final TLongArrayList TEST_LONG_ARRAY_LIST = TLongArrayList.wrap(TEST_LONG_ARRAY);

    /**
     * Get a petrified list.
     *
     * @return The list
     */
    private static PetrifyableTLongArrayList getPetrifiedList()
    {
        int capacity = 20;
        PetrifyableTLongArrayList list = new PetrifyableTLongArrayList(capacity);
        for (int index = 0; index < capacity; ++index)
        {
            list.add(index);
        }

        Assert.assertFalse(list.isPetrified());
        list.petrify();
        Assert.assertTrue(list.isPetrified());
        return list;
    }

    /** General test. */
    @Test
    public void test()
    {
        int capacity = 20;
        long offset = 7;
        PetrifyableTLongArrayList list = new PetrifyableTLongArrayList(capacity);
        for (int index = 0; index < capacity; ++index)
        {
            list.add(index + offset);
        }

        Assert.assertFalse(list.isPetrified());
        list.petrify();
        Assert.assertTrue(list.isPetrified());

        long[] arr1 = list.toArray();
        long[] arr2 = list.toArray();

        Assert.assertNotSame(arr1, arr2);
        Assert.assertEquals(capacity, arr1.length);
        Assert.assertTrue(Arrays.equals(arr1, arr2));

        for (int index = 0; index < capacity; ++index)
        {
            Assert.assertEquals(index + offset, arr1[index]);
        }

        Assert.assertEquals(TEST_LONG - offset, list.binarySearch(TEST_LONG));
        Assert.assertEquals(TEST_LONG - offset, list.indexOf(TEST_LONG));
        Assert.assertEquals(TEST_LONG - offset, list.lastIndexOf(TEST_LONG));
        Assert.assertTrue(list.contains(TEST_LONG));
        Assert.assertTrue(list.containsAll(TEST_LONG_ARRAY));
        Assert.assertTrue(list.containsAll(TEST_LONG_ARRAY_LIST));
        list.forEach(new TLongProcedure()
        {
            @Override
            public boolean execute(long value)
            {
                return true;
            }
        });
        list.forEachDescending(new TLongProcedure()
        {
            @Override
            public boolean execute(long value)
            {
                return true;
            }
        });
        Assert.assertEquals(offset, list.get(0));
        TLongList grepResult = list.grep(new TLongProcedure()
        {
            @Override
            public boolean execute(long value)
            {
                return value == TEST_LONG;
            }
        });
        Assert.assertEquals(1, grepResult.size());
        Assert.assertEquals(TEST_LONG, grepResult.get(0));
        TLongList grepResult2 = list.inverseGrep(new TLongProcedure()
        {
            @Override
            public boolean execute(long value)
            {
                return value == TEST_LONG;
            }
        });
        Assert.assertEquals(capacity - 1, grepResult2.size());
        Assert.assertFalse(grepResult2.contains(TEST_LONG));
        Assert.assertFalse(list.isEmpty());
        for (TLongIterator iter = list.iterator(); iter.hasNext();)
        {
            iter.next();
        }
        Assert.assertEquals(capacity - 1 + offset, list.max());
        Assert.assertEquals(offset, list.min());
        Assert.assertEquals(5, list.subList(0, 5).size());
        Assert.assertEquals(190 + offset * capacity, list.sum());
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify1()
    {
        PETRIFIED_LIST.add(TEST_LONG);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify2()
    {
        PETRIFIED_LIST.add(TEST_LONG_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify3()
    {
        PETRIFIED_LIST.add(TEST_LONG_ARRAY, 0, 1);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify1()
    {
        PETRIFIED_LIST.addAll(TEST_LONG_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify2()
    {
        PETRIFIED_LIST.addAll(TEST_LONG_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify3()
    {
        PETRIFIED_LIST.addAll(TEST_LONG_ARRAY_LIST);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testClearAfterPetrify()
    {
        PETRIFIED_LIST.clear();
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testFillAfterPetrify1()
    {
        PETRIFIED_LIST.fill(TEST_LONG);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testFillAfterPetrify2()
    {
        PETRIFIED_LIST.fill(0, 1, TEST_LONG);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify1()
    {
        PETRIFIED_LIST.insert(0, TEST_LONG);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify2()
    {
        PETRIFIED_LIST.insert(0, TEST_LONG_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify3()
    {
        PETRIFIED_LIST.insert(0, TEST_LONG_ARRAY, 0, 1);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemoveAfterPetrify()
    {
        PETRIFIED_LIST.iterator().remove();
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAfterPetrify1()
    {
        PETRIFIED_LIST.remove(TEST_LONG);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAfterPetrify2()
    {
        PETRIFIED_LIST.remove(0, 1);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllAfterPetrify1()
    {
        PETRIFIED_LIST.removeAll(TEST_LONG_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllAfterPetrify2()
    {
        PETRIFIED_LIST.removeAll(TEST_LONG_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllAfterPetrify3()
    {
        PETRIFIED_LIST.removeAll(TEST_LONG_ARRAY_LIST);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAtAfterPetrify()
    {
        PETRIFIED_LIST.removeAt(1);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testReplaceAfterPetrify()
    {
        PETRIFIED_LIST.replace(0, TEST_LONG);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify1()
    {
        PETRIFIED_LIST.retainAll(TEST_LONG_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify2()
    {
        PETRIFIED_LIST.retainAll(TEST_LONG_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify3()
    {
        PETRIFIED_LIST.retainAll(TEST_LONG_ARRAY_LIST);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testReverseAfterPetrify1()
    {
        PETRIFIED_LIST.reverse();
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testReverseAfterPetrify2()
    {
        PETRIFIED_LIST.reverse(0, 5);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAfterPetrify1()
    {
        PETRIFIED_LIST.set(0, TEST_LONG);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAfterPetrify2()
    {
        PETRIFIED_LIST.set(0, TEST_LONG_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAfterPetrify3()
    {
        PETRIFIED_LIST.set(0, TEST_LONG_ARRAY, 0, 1);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSortAfterPetrify1()
    {
        PETRIFIED_LIST.sort();
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSortAfterPetrify2()
    {
        PETRIFIED_LIST.sort(0, 5);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testTransformValuesAfterPetrify2()
    {
        PETRIFIED_LIST.transformValues(new TLongFunction()
        {
            @Override
            public long execute(long value)
            {
                return 0;
            }
        });
    }
}
