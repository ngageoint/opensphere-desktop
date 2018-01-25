package io.opensphere.core.util.collections.petrifyable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import gnu.trove.function.TDoubleFunction;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.procedure.TDoubleProcedure;

/**
 * Test for {@link PetrifyableTDoubleArrayList}.
 */
public class PetrifyableTDoubleArrayListTest
{
    /** A petrified list for testing. */
    private static final PetrifyableTDoubleArrayList PETRIFIED_LIST = getPetrifiedList();

    /** A double for testing. */
    private static final double TEST_DOUBLE = 10.;

    /** A collection of Doubles for testing. */
    private static final Collection<Double> TEST_DOUBLE_COLLECTION = Collections.singleton(Double.valueOf(TEST_DOUBLE));

    /** A double array for testing. */
    private static final double[] TEST_DOUBLE_ARRAY = new double[] { TEST_DOUBLE };

    /** A double array list for testing. */
    private static final TDoubleArrayList TEST_DOUBLE_ARRAY_LIST = TDoubleArrayList.wrap(TEST_DOUBLE_ARRAY);

    /**
     * Get a petrified list.
     *
     * @return The list
     */
    private static PetrifyableTDoubleArrayList getPetrifiedList()
    {
        int capacity = 20;
        PetrifyableTDoubleArrayList list = new PetrifyableTDoubleArrayList(capacity);
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
        double offset = 7;
        PetrifyableTDoubleArrayList list = new PetrifyableTDoubleArrayList(capacity);
        for (int index = 0; index < capacity; ++index)
        {
            list.add(index + offset);
        }

        Assert.assertFalse(list.isPetrified());
        list.petrify();
        Assert.assertTrue(list.isPetrified());

        double[] arr1 = list.toArray();
        double[] arr2 = list.toArray();

        Assert.assertNotSame(arr1, arr2);
        Assert.assertEquals(capacity, arr1.length);
        Assert.assertTrue(Arrays.equals(arr1, arr2));

        for (int index = 0; index < capacity; ++index)
        {
            Assert.assertEquals(index + offset, arr1[index], 0.);
        }

        Assert.assertEquals(TEST_DOUBLE - offset, list.binarySearch(TEST_DOUBLE), 0.);
        Assert.assertEquals(TEST_DOUBLE - offset, list.indexOf(TEST_DOUBLE), 0.);
        Assert.assertEquals(TEST_DOUBLE - offset, list.lastIndexOf(TEST_DOUBLE), 0.);
        Assert.assertTrue(list.contains(TEST_DOUBLE));
        Assert.assertTrue(list.containsAll(TEST_DOUBLE_ARRAY));
        Assert.assertTrue(list.containsAll(TEST_DOUBLE_ARRAY_LIST));
        list.forEach(new TDoubleProcedure()
        {
            @Override
            public boolean execute(double value)
            {
                return true;
            }
        });
        list.forEachDescending(new TDoubleProcedure()
        {
            @Override
            public boolean execute(double value)
            {
                return true;
            }
        });
        Assert.assertEquals(offset, list.get(0), 0.);
        TDoubleList grepResult = list.grep(new TDoubleProcedure()
        {
            @Override
            public boolean execute(double value)
            {
                return value == TEST_DOUBLE;
            }
        });
        Assert.assertEquals(1, grepResult.size());
        Assert.assertEquals(TEST_DOUBLE, grepResult.get(0), 0.);
        TDoubleList grepResult2 = list.inverseGrep(new TDoubleProcedure()
        {
            @Override
            public boolean execute(double value)
            {
                return value == TEST_DOUBLE;
            }
        });
        Assert.assertEquals(capacity - 1, grepResult2.size());
        Assert.assertFalse(grepResult2.contains(TEST_DOUBLE));
        Assert.assertFalse(list.isEmpty());
        for (TDoubleIterator iter = list.iterator(); iter.hasNext();)
        {
            iter.next();
        }
        Assert.assertEquals(capacity - 1 + offset, list.max(), 0.);
        Assert.assertEquals(offset, list.min(), 0.);
        Assert.assertEquals(5, list.subList(0, 5).size());
        Assert.assertEquals(190 + offset * capacity, list.sum(), 0.);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify1()
    {
        PETRIFIED_LIST.add(TEST_DOUBLE);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify2()
    {
        PETRIFIED_LIST.add(TEST_DOUBLE_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify3()
    {
        PETRIFIED_LIST.add(TEST_DOUBLE_ARRAY, 0, 1);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify1()
    {
        PETRIFIED_LIST.addAll(TEST_DOUBLE_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify2()
    {
        PETRIFIED_LIST.addAll(TEST_DOUBLE_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify3()
    {
        PETRIFIED_LIST.addAll(TEST_DOUBLE_ARRAY_LIST);
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
        PETRIFIED_LIST.fill(TEST_DOUBLE);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testFillAfterPetrify2()
    {
        PETRIFIED_LIST.fill(0, 1, TEST_DOUBLE);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify1()
    {
        PETRIFIED_LIST.insert(0, TEST_DOUBLE);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify2()
    {
        PETRIFIED_LIST.insert(0, TEST_DOUBLE_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify3()
    {
        PETRIFIED_LIST.insert(0, TEST_DOUBLE_ARRAY, 0, 1);
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
        PETRIFIED_LIST.remove(TEST_DOUBLE);
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
        PETRIFIED_LIST.removeAll(TEST_DOUBLE_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllAfterPetrify2()
    {
        PETRIFIED_LIST.removeAll(TEST_DOUBLE_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllAfterPetrify3()
    {
        PETRIFIED_LIST.removeAll(TEST_DOUBLE_ARRAY_LIST);
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
        PETRIFIED_LIST.replace(0, TEST_DOUBLE);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify1()
    {
        PETRIFIED_LIST.retainAll(TEST_DOUBLE_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify2()
    {
        PETRIFIED_LIST.retainAll(TEST_DOUBLE_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify3()
    {
        PETRIFIED_LIST.retainAll(TEST_DOUBLE_ARRAY_LIST);
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
        PETRIFIED_LIST.set(0, TEST_DOUBLE);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAfterPetrify2()
    {
        PETRIFIED_LIST.set(0, TEST_DOUBLE_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAfterPetrify3()
    {
        PETRIFIED_LIST.set(0, TEST_DOUBLE_ARRAY, 0, 1);
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
        PETRIFIED_LIST.transformValues(new TDoubleFunction()
        {
            @Override
            public double execute(double value)
            {
                return 0;
            }
        });
    }
}
