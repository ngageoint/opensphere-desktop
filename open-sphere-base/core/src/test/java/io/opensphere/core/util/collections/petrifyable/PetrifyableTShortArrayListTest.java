package io.opensphere.core.util.collections.petrifyable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import gnu.trove.function.TShortFunction;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.list.TShortList;
import gnu.trove.list.array.TShortArrayList;
import gnu.trove.procedure.TShortProcedure;

/**
 * Test for {@link PetrifyableTShortArrayList}.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public class PetrifyableTShortArrayListTest
{
    /** A petrified list for testing. */
    private static final PetrifyableTShortArrayList PETRIFIED_LIST = getPetrifiedList();

    /** A short for testing. */
    private static final short TEST_SHORT = (short)10;

    /** A collection of Shorts for testing. */
    private static final Collection<Short> TEST_SHORT_COLLECTION = Collections.singleton(Short.valueOf(TEST_SHORT));

    /** A short array for testing. */
    private static final short[] TEST_SHORT_ARRAY = new short[] { TEST_SHORT };

    /** A short array list for testing. */
    private static final TShortArrayList TEST_SHORT_ARRAY_LIST = TShortArrayList.wrap(TEST_SHORT_ARRAY);

    /**
     * Get a petrified list.
     *
     * @return The list
     */
    private static PetrifyableTShortArrayList getPetrifiedList()
    {
        int capacity = 20;
        PetrifyableTShortArrayList list = new PetrifyableTShortArrayList(capacity);
        for (int index = 0; index < capacity; ++index)
        {
            list.add((short)index);
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
        short offset = (short)7;
        PetrifyableTShortArrayList list = new PetrifyableTShortArrayList(capacity);
        for (int index = 0; index < capacity; ++index)
        {
            list.add((short)(index + offset));
        }

        Assert.assertFalse(list.isPetrified());
        list.petrify();
        Assert.assertTrue(list.isPetrified());

        short[] arr1 = list.toArray();
        short[] arr2 = list.toArray();

        Assert.assertNotSame(arr1, arr2);
        Assert.assertEquals(capacity, arr1.length);
        Assert.assertTrue(Arrays.equals(arr1, arr2));

        for (int index = 0; index < capacity; ++index)
        {
            Assert.assertEquals(index + offset, arr1[index]);
        }

        Assert.assertEquals(TEST_SHORT - offset, list.binarySearch(TEST_SHORT));
        Assert.assertEquals(TEST_SHORT - offset, list.indexOf(TEST_SHORT));
        Assert.assertEquals(TEST_SHORT - offset, list.lastIndexOf(TEST_SHORT));
        Assert.assertTrue(list.contains(TEST_SHORT));
        Assert.assertTrue(list.containsAll(TEST_SHORT_ARRAY));
        Assert.assertTrue(list.containsAll(TEST_SHORT_ARRAY_LIST));
        list.forEach(new TShortProcedure()
        {
            @Override
            public boolean execute(short value)
            {
                return true;
            }
        });
        list.forEachDescending(new TShortProcedure()
        {
            @Override
            public boolean execute(short value)
            {
                return true;
            }
        });
        Assert.assertEquals(offset, list.get(0));
        TShortList grepResult = list.grep(new TShortProcedure()
        {
            @Override
            public boolean execute(short value)
            {
                return value == TEST_SHORT;
            }
        });
        Assert.assertEquals(1, grepResult.size());
        Assert.assertEquals(TEST_SHORT, grepResult.get(0));
        TShortList grepResult2 = list.inverseGrep(new TShortProcedure()
        {
            @Override
            public boolean execute(short value)
            {
                return value == TEST_SHORT;
            }
        });
        Assert.assertEquals(capacity - 1, grepResult2.size());
        Assert.assertFalse(grepResult2.contains(TEST_SHORT));
        Assert.assertFalse(list.isEmpty());
        for (TShortIterator iter = list.iterator(); iter.hasNext();)
        {
            iter.next();
        }
        Assert.assertEquals((short)(capacity - 1 + offset), list.max());
        Assert.assertEquals(offset, list.min());
        Assert.assertEquals(5, list.subList(0, 5).size());
        Assert.assertEquals((short)(190 + offset * capacity), list.sum());
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify1()
    {
        PETRIFIED_LIST.add(TEST_SHORT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify2()
    {
        PETRIFIED_LIST.add(TEST_SHORT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify3()
    {
        PETRIFIED_LIST.add(TEST_SHORT_ARRAY, 0, 1);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify1()
    {
        PETRIFIED_LIST.addAll(TEST_SHORT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify2()
    {
        PETRIFIED_LIST.addAll(TEST_SHORT_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify3()
    {
        PETRIFIED_LIST.addAll(TEST_SHORT_ARRAY_LIST);
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
        PETRIFIED_LIST.fill(TEST_SHORT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testFillAfterPetrify2()
    {
        PETRIFIED_LIST.fill(0, 1, TEST_SHORT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify1()
    {
        PETRIFIED_LIST.insert(0, TEST_SHORT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify2()
    {
        PETRIFIED_LIST.insert(0, TEST_SHORT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify3()
    {
        PETRIFIED_LIST.insert(0, TEST_SHORT_ARRAY, 0, 1);
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
        PETRIFIED_LIST.remove(TEST_SHORT);
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
        PETRIFIED_LIST.removeAll(TEST_SHORT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllAfterPetrify2()
    {
        PETRIFIED_LIST.removeAll(TEST_SHORT_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllAfterPetrify3()
    {
        PETRIFIED_LIST.removeAll(TEST_SHORT_ARRAY_LIST);
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
        PETRIFIED_LIST.replace(0, TEST_SHORT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify1()
    {
        PETRIFIED_LIST.retainAll(TEST_SHORT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify2()
    {
        PETRIFIED_LIST.retainAll(TEST_SHORT_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify3()
    {
        PETRIFIED_LIST.retainAll(TEST_SHORT_ARRAY_LIST);
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
        PETRIFIED_LIST.set(0, TEST_SHORT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAfterPetrify2()
    {
        PETRIFIED_LIST.set(0, TEST_SHORT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAfterPetrify3()
    {
        PETRIFIED_LIST.set(0, TEST_SHORT_ARRAY, 0, 1);
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
        PETRIFIED_LIST.transformValues(new TShortFunction()
        {
            @Override
            public short execute(short value)
            {
                return 0;
            }
        });
    }
}
