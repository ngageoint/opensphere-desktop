package io.opensphere.core.util.collections.petrifyable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import gnu.trove.function.TIntFunction;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;

/**
 * Test for {@link PetrifyableTIntArrayList}.
 */
public class PetrifyableTIntArrayListTest
{
    /** A petrified list for testing. */
    private static final PetrifyableTIntArrayList PETRIFIED_LIST = getPetrifiedList();

    /** A int for testing. */
    private static final int TEST_INT = 10;

    /** A collection of Ints for testing. */
    private static final Collection<Integer> TEST_INT_COLLECTION = Collections.singleton(Integer.valueOf(TEST_INT));

    /** A int array for testing. */
    private static final int[] TEST_INT_ARRAY = new int[] { TEST_INT };

    /** A int array list for testing. */
    private static final TIntArrayList TEST_INT_ARRAY_LIST = TIntArrayList.wrap(TEST_INT_ARRAY);

    /**
     * Get a petrified list.
     *
     * @return The list
     */
    private static PetrifyableTIntArrayList getPetrifiedList()
    {
        int capacity = 20;
        PetrifyableTIntArrayList list = new PetrifyableTIntArrayList(capacity);
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
        int offset = 7;
        PetrifyableTIntArrayList list = new PetrifyableTIntArrayList(capacity);
        for (int index = 0; index < capacity; ++index)
        {
            list.add(index + offset);
        }

        Assert.assertFalse(list.isPetrified());
        list.petrify();
        Assert.assertTrue(list.isPetrified());

        int[] arr1 = list.toArray();
        int[] arr2 = list.toArray();

        Assert.assertNotSame(arr1, arr2);
        Assert.assertEquals(capacity, arr1.length);
        Assert.assertTrue(Arrays.equals(arr1, arr2));

        for (int index = 0; index < capacity; ++index)
        {
            Assert.assertEquals(index + offset, arr1[index]);
        }

        Assert.assertEquals(TEST_INT - offset, list.binarySearch(TEST_INT));
        Assert.assertEquals(TEST_INT - offset, list.indexOf(TEST_INT));
        Assert.assertEquals(TEST_INT - offset, list.lastIndexOf(TEST_INT));
        Assert.assertTrue(list.contains(TEST_INT));
        Assert.assertTrue(list.containsAll(TEST_INT_ARRAY));
        Assert.assertTrue(list.containsAll(TEST_INT_ARRAY_LIST));
        list.forEach(new TIntProcedure()
        {
            @Override
            public boolean execute(int value)
            {
                return true;
            }
        });
        list.forEachDescending(new TIntProcedure()
        {
            @Override
            public boolean execute(int value)
            {
                return true;
            }
        });
        Assert.assertEquals(offset, list.get(0));
        TIntList grepResult = list.grep(new TIntProcedure()
        {
            @Override
            public boolean execute(int value)
            {
                return value == TEST_INT;
            }
        });
        Assert.assertEquals(1, grepResult.size());
        Assert.assertEquals(TEST_INT, grepResult.get(0));
        TIntList grepResult2 = list.inverseGrep(new TIntProcedure()
        {
            @Override
            public boolean execute(int value)
            {
                return value == TEST_INT;
            }
        });
        Assert.assertEquals(capacity - 1, grepResult2.size());
        Assert.assertFalse(grepResult2.contains(TEST_INT));
        Assert.assertFalse(list.isEmpty());
        for (TIntIterator iter = list.iterator(); iter.hasNext();)
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
        PETRIFIED_LIST.add(TEST_INT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify2()
    {
        PETRIFIED_LIST.add(TEST_INT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify3()
    {
        PETRIFIED_LIST.add(TEST_INT_ARRAY, 0, 1);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify1()
    {
        PETRIFIED_LIST.addAll(TEST_INT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify2()
    {
        PETRIFIED_LIST.addAll(TEST_INT_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify3()
    {
        PETRIFIED_LIST.addAll(TEST_INT_ARRAY_LIST);
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
        PETRIFIED_LIST.fill(TEST_INT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testFillAfterPetrify2()
    {
        PETRIFIED_LIST.fill(0, 1, TEST_INT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify1()
    {
        PETRIFIED_LIST.insert(0, TEST_INT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify2()
    {
        PETRIFIED_LIST.insert(0, TEST_INT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify3()
    {
        PETRIFIED_LIST.insert(0, TEST_INT_ARRAY, 0, 1);
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
        PETRIFIED_LIST.remove(TEST_INT);
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
        PETRIFIED_LIST.removeAll(TEST_INT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllAfterPetrify2()
    {
        PETRIFIED_LIST.removeAll(TEST_INT_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllAfterPetrify3()
    {
        PETRIFIED_LIST.removeAll(TEST_INT_ARRAY_LIST);
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
        PETRIFIED_LIST.replace(0, TEST_INT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify1()
    {
        PETRIFIED_LIST.retainAll(TEST_INT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify2()
    {
        PETRIFIED_LIST.retainAll(TEST_INT_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify3()
    {
        PETRIFIED_LIST.retainAll(TEST_INT_ARRAY_LIST);
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
        PETRIFIED_LIST.set(0, TEST_INT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAfterPetrify2()
    {
        PETRIFIED_LIST.set(0, TEST_INT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAfterPetrify3()
    {
        PETRIFIED_LIST.set(0, TEST_INT_ARRAY, 0, 1);
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
        PETRIFIED_LIST.transformValues(new TIntFunction()
        {
            @Override
            public int execute(int value)
            {
                return 0;
            }
        });
    }
}
