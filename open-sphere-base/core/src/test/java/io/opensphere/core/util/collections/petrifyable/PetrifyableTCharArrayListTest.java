package io.opensphere.core.util.collections.petrifyable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import gnu.trove.function.TCharFunction;
import gnu.trove.iterator.TCharIterator;
import gnu.trove.list.TCharList;
import gnu.trove.list.array.TCharArrayList;
import gnu.trove.procedure.TCharProcedure;

/**
 * Test for {@link PetrifyableTCharArrayList}.
 */
public class PetrifyableTCharArrayListTest
{
    /** A petrified list for testing. */
    private static final PetrifyableTCharArrayList PETRIFIED_LIST = getPetrifiedList();

    /** A char for testing. */
    private static final char TEST_CHAR = (char)10;

    /** A collection of Chars for testing. */
    private static final Collection<Character> TEST_CHAR_COLLECTION = Collections.singleton(Character.valueOf(TEST_CHAR));

    /** A char array for testing. */
    private static final char[] TEST_CHAR_ARRAY = new char[] { TEST_CHAR };

    /** A char array list for testing. */
    private static final TCharArrayList TEST_CHAR_ARRAY_LIST = TCharArrayList.wrap(TEST_CHAR_ARRAY);

    /**
     * Get a petrified list.
     *
     * @return The list
     */
    private static PetrifyableTCharArrayList getPetrifiedList()
    {
        int capacity = 20;
        PetrifyableTCharArrayList list = new PetrifyableTCharArrayList(capacity);
        for (int index = 0; index < capacity; ++index)
        {
            list.add((char)index);
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
        char offset = (char)7;
        PetrifyableTCharArrayList list = new PetrifyableTCharArrayList(capacity);
        for (int index = 0; index < capacity; ++index)
        {
            list.add((char)(index + offset));
        }

        Assert.assertFalse(list.isPetrified());
        list.petrify();
        Assert.assertTrue(list.isPetrified());

        char[] arr1 = list.toArray();
        char[] arr2 = list.toArray();

        Assert.assertNotSame(arr1, arr2);
        Assert.assertEquals(capacity, arr1.length);
        Assert.assertTrue(Arrays.equals(arr1, arr2));

        for (int index = 0; index < capacity; ++index)
        {
            Assert.assertEquals(index + offset, arr1[index]);
        }

        Assert.assertEquals(TEST_CHAR - offset, list.binarySearch(TEST_CHAR));
        Assert.assertEquals(TEST_CHAR - offset, list.indexOf(TEST_CHAR));
        Assert.assertEquals(TEST_CHAR - offset, list.lastIndexOf(TEST_CHAR));
        Assert.assertTrue(list.contains(TEST_CHAR));
        Assert.assertTrue(list.containsAll(TEST_CHAR_ARRAY));
        Assert.assertTrue(list.containsAll(TEST_CHAR_ARRAY_LIST));
        list.forEach(new TCharProcedure()
        {
            @Override
            public boolean execute(char value)
            {
                return true;
            }
        });
        list.forEachDescending(new TCharProcedure()
        {
            @Override
            public boolean execute(char value)
            {
                return true;
            }
        });
        Assert.assertEquals(offset, list.get(0));
        TCharList grepResult = list.grep(new TCharProcedure()
        {
            @Override
            public boolean execute(char value)
            {
                return value == TEST_CHAR;
            }
        });
        Assert.assertEquals(1, grepResult.size());
        Assert.assertEquals(TEST_CHAR, grepResult.get(0));
        TCharList grepResult2 = list.inverseGrep(new TCharProcedure()
        {
            @Override
            public boolean execute(char value)
            {
                return value == TEST_CHAR;
            }
        });
        Assert.assertEquals(capacity - 1, grepResult2.size());
        Assert.assertFalse(grepResult2.contains(TEST_CHAR));
        Assert.assertFalse(list.isEmpty());
        for (TCharIterator iter = list.iterator(); iter.hasNext();)
        {
            iter.next();
        }
        Assert.assertEquals((char)(capacity - 1 + offset), list.max());
        Assert.assertEquals(offset, list.min());
        Assert.assertEquals(5, list.subList(0, 5).size());
        Assert.assertEquals((char)(190 + offset * capacity), list.sum());
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify1()
    {
        PETRIFIED_LIST.add(TEST_CHAR);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify2()
    {
        PETRIFIED_LIST.add(TEST_CHAR_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAfterPetrify3()
    {
        PETRIFIED_LIST.add(TEST_CHAR_ARRAY, 0, 1);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify1()
    {
        PETRIFIED_LIST.addAll(TEST_CHAR_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify2()
    {
        PETRIFIED_LIST.addAll(TEST_CHAR_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllAfterPetrify3()
    {
        PETRIFIED_LIST.addAll(TEST_CHAR_ARRAY_LIST);
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
        PETRIFIED_LIST.fill(TEST_CHAR);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testFillAfterPetrify2()
    {
        PETRIFIED_LIST.fill(0, 1, TEST_CHAR);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify1()
    {
        PETRIFIED_LIST.insert(0, TEST_CHAR);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify2()
    {
        PETRIFIED_LIST.insert(0, TEST_CHAR_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertAfterPetrify3()
    {
        PETRIFIED_LIST.insert(0, TEST_CHAR_ARRAY, 0, 1);
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
        PETRIFIED_LIST.remove(TEST_CHAR);
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
        PETRIFIED_LIST.removeAll(TEST_CHAR_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllAfterPetrify2()
    {
        PETRIFIED_LIST.removeAll(TEST_CHAR_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllAfterPetrify3()
    {
        PETRIFIED_LIST.removeAll(TEST_CHAR_ARRAY_LIST);
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
        PETRIFIED_LIST.replace(0, TEST_CHAR);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify1()
    {
        PETRIFIED_LIST.retainAll(TEST_CHAR_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify2()
    {
        PETRIFIED_LIST.retainAll(TEST_CHAR_COLLECTION);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllAfterPetrify3()
    {
        PETRIFIED_LIST.retainAll(TEST_CHAR_ARRAY_LIST);
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
        PETRIFIED_LIST.set(0, TEST_CHAR);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAfterPetrify2()
    {
        PETRIFIED_LIST.set(0, TEST_CHAR_ARRAY);
    }

    /** Test mutation operations after petrify. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAfterPetrify3()
    {
        PETRIFIED_LIST.set(0, TEST_CHAR_ARRAY, 0, 1);
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
        PETRIFIED_LIST.transformValues(new TCharFunction()
        {
            @Override
            public char execute(char value)
            {
                return 0;
            }
        });
    }
}
