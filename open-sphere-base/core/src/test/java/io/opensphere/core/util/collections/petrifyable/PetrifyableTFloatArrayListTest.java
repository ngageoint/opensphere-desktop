package io.opensphere.core.util.collections.petrifyable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import gnu.trove.function.TFloatFunction;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.procedure.TFloatProcedure;

/**
 * Test for {@link PetrifyableTFloatArrayList}.
 */
public class PetrifyableTFloatArrayListTest
{
    /** A petrified list for testing. */
    private static final PetrifyableTFloatArrayList PETRIFIED_LIST = getPetrifiedList();

    /** A float for testing. */
    private static final float TEST_FLOAT = 10f;

    /** A collection of Floats for testing. */
    private static final Collection<Float> TEST_FLOAT_COLLECTION = Collections.singleton(Float.valueOf(TEST_FLOAT));

    /** A float array for testing. */
    private static final float[] TEST_FLOAT_ARRAY = new float[] { TEST_FLOAT };

    /** A float array list for testing. */
    private static final TFloatArrayList TEST_FLOAT_ARRAY_LIST = TFloatArrayList.wrap(TEST_FLOAT_ARRAY);

    /**
     * Get a petrified list.
     *
     * @return The list
     */
    private static PetrifyableTFloatArrayList getPetrifiedList()
    {
        int capacity = 20;
        PetrifyableTFloatArrayList list = new PetrifyableTFloatArrayList(capacity);
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
        float offset = 7;
        PetrifyableTFloatArrayList list = new PetrifyableTFloatArrayList(capacity);
        for (int index = 0; index < capacity; ++index)
        {
            list.add(index + offset);
        }

        Assert.assertFalse(list.isPetrified());
        list.petrify();
        Assert.assertTrue(list.isPetrified());

        float[] arr1 = list.toArray();
        float[] arr2 = list.toArray();

        Assert.assertNotSame(arr1, arr2);
        Assert.assertEquals(capacity, arr1.length);
        Assert.assertTrue(Arrays.equals(arr1, arr2));

        for (int index = 0; index < capacity; ++index)
        {
            Assert.assertEquals(index + offset, arr1[index], 0.);
        }

        Assert.assertEquals(TEST_FLOAT - offset, list.binarySearch(TEST_FLOAT), 0.);
        Assert.assertEquals(TEST_FLOAT - offset, list.indexOf(TEST_FLOAT), 0.);
        Assert.assertEquals(TEST_FLOAT - offset, list.lastIndexOf(TEST_FLOAT), 0.);
        Assert.assertTrue(list.contains(TEST_FLOAT));
        Assert.assertTrue(list.containsAll(TEST_FLOAT_ARRAY));
        Assert.assertTrue(list.containsAll(TEST_FLOAT_ARRAY_LIST));
        list.forEach(new TFloatProcedure()
        {
            @Override
            public boolean execute(float value)
            {
                return true;
            }
        });
        list.forEachDescending(new TFloatProcedure()
        {
            @Override
            public boolean execute(float value)
            {
                return true;
            }
        });
        Assert.assertEquals(offset, list.get(0), 0.);
        TFloatList grepResult = list.grep(new TFloatProcedure()
        {
            @Override
            public boolean execute(float value)
            {
                return value == TEST_FLOAT;
            }
        });
        Assert.assertEquals(1, grepResult.size());
        Assert.assertEquals(TEST_FLOAT, grepResult.get(0), 0.);
        TFloatList grepResult2 = list.inverseGrep(new TFloatProcedure()
        {
            @Override
            public boolean execute(float value)
            {
                return value == TEST_FLOAT;
            }
        });
        Assert.assertEquals(capacity - 1, grepResult2.size());
        Assert.assertFalse(grepResult2.contains(TEST_FLOAT));
        Assert.assertFalse(list.isEmpty());
        for (TFloatIterator iter = list.iterator(); iter.hasNext();)
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
        PETRIFIED_LIST.add(TEST_FLOAT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify2()
    {
        PETRIFIED_LIST.add(TEST_FLOAT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify3()
    {
        PETRIFIED_LIST.add(TEST_FLOAT_ARRAY, 0, 1);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify1()
    {
        PETRIFIED_LIST.addAll(TEST_FLOAT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify2()
    {
        PETRIFIED_LIST.addAll(TEST_FLOAT_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify3()
    {
        PETRIFIED_LIST.addAll(TEST_FLOAT_ARRAY_LIST);
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
        PETRIFIED_LIST.fill(TEST_FLOAT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testFillAfterPetrify2()
    {
        PETRIFIED_LIST.fill(0, 1, TEST_FLOAT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify1()
    {
        PETRIFIED_LIST.insert(0, TEST_FLOAT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify2()
    {
        PETRIFIED_LIST.insert(0, TEST_FLOAT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify3()
    {
        PETRIFIED_LIST.insert(0, TEST_FLOAT_ARRAY, 0, 1);
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
        PETRIFIED_LIST.remove(TEST_FLOAT);
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
        PETRIFIED_LIST.removeAll(TEST_FLOAT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllAfterPetrify2()
    {
        PETRIFIED_LIST.removeAll(TEST_FLOAT_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllAfterPetrify3()
    {
        PETRIFIED_LIST.removeAll(TEST_FLOAT_ARRAY_LIST);
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
        PETRIFIED_LIST.replace(0, TEST_FLOAT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify1()
    {
        PETRIFIED_LIST.retainAll(TEST_FLOAT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify2()
    {
        PETRIFIED_LIST.retainAll(TEST_FLOAT_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify3()
    {
        PETRIFIED_LIST.retainAll(TEST_FLOAT_ARRAY_LIST);
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
        PETRIFIED_LIST.set(0, TEST_FLOAT);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAfterPetrify2()
    {
        PETRIFIED_LIST.set(0, TEST_FLOAT_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAfterPetrify3()
    {
        PETRIFIED_LIST.set(0, TEST_FLOAT_ARRAY, 0, 1);
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
        PETRIFIED_LIST.transformValues(new TFloatFunction()
        {
            @Override
            public float execute(float value)
            {
                return 0;
            }
        });
    }
}
