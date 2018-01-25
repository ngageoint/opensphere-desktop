package io.opensphere.analysis.util.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.collections.CollectionUtilities;

/** Tests {@link RangedLongList}. */
public class RangedLongListTest
{
    /** Test {@link RangedLongList#add(Long)}. */
    @Test
    public void testAdd()
    {
        RangedLongList list = newList();
        validateRangedLongList(list, 3, 1, 2, 3, 5, 1000000000000L, 1000000000001L, 1000000000002L);
        Assert.assertFalse(list.contains(4));
    }

    /** Test {@link RangedLongList#remove(Object)}. */
    @Test
    public void testRemove()
    {
        RangedLongList list = newList();
        boolean removed = list.remove(Long.valueOf(2));
        Assert.assertTrue(removed);
        validateRangedLongList(list, 4, 1, 3, 5, 1000000000000L, 1000000000001L, 1000000000002L);
    }

    /** Test {@link RangedLongList#addAll(Collection)}. */
    @Test
    public void testAddAll()
    {
        RangedLongList list = new RangedLongList();
        list.addAll(CollectionUtilities.listViewLong(1, 2, 3));
        validateRangedLongList(list, 1, 1, 2, 3);
    }

//    /** Test {@link RangedLongList#removeAll(Collection)}. */
//    @Test
//    public void testRemoveAll()
//    {
//        RangedLongList list = newList();
//        list.removeAll(CollectionUtilities.listViewLong(1, 2, 3));
//        validateList(list, 2, 5, 1000000000000L, 1000000000001L, 1000000000002L);
//    }
//
//    /** Test {@link RangedLongList#retainAll(Collection)}. */
//    @Test
//    public void testRetainAll()
//    {
//        RangedLongList list = newList();
//        list.retainAll(CollectionUtilities.listViewLong(1, 2, 3));
//        validateList(list, 1, 1, 2, 3);
//    }

    /** Test {@link RangedLongList#clear()}. */
    @Test
    public void testClear()
    {
        RangedLongList list = newList();
        list.clear();
        validateRangedLongList(list, 0);
    }

    /** Test {@link RangedLongList#remove(int)}. */
    @Test
    public void testRemoveIndex()
    {
        RangedLongList list = newList();
        Long value = list.remove(6);
        Assert.assertEquals(1000000000002L, value.longValue());
        validateRangedLongList(list, 3, 1, 2, 3, 5, 1000000000000L, 1000000000001L);
    }

    /**
     * Creates a new list with some values.
     *
     * @return the list
     */
    private static RangedLongList newList()
    {
        RangedLongList list = new RangedLongList();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(5);
        list.add(1000000000000L);
        list.add(1000000000001L);
        list.add(1000000000002L);
        return list;
    }

    /**
     * Validates the list against expected values.
     *
     * @param list the list
     * @param blockSize the expected block size
     * @param expectedValues the expected values
     */
    private static void validateRangedLongList(RangedLongList list, int blockSize, long... expectedValues)
    {
        validateList(list, expectedValues);
        Assert.assertEquals(blockSize, list.getBlockSize());
        if (expectedValues.length != 0)
        {
            Assert.assertEquals(Arrays.stream(expectedValues).min().orElse(Long.MAX_VALUE), list.min());
            Assert.assertEquals(Arrays.stream(expectedValues).max().orElse(Long.MIN_VALUE), list.max());
        }
    }

    /**
     * Validates the list against expected values.
     *
     * @param list the list
     * @param expectedValues the expected values
     */
    private static void validateList(List<? extends Long> list, long... expectedValues)
    {
        validateCollection(list, expectedValues);
        for (int i = 0; i < expectedValues.length; ++i)
        {
            Assert.assertEquals(expectedValues[i], list.get(i).longValue());
            Assert.assertEquals(i, list.indexOf(Long.valueOf(expectedValues[i])));
            Assert.assertEquals(i, list.lastIndexOf(Long.valueOf(expectedValues[i])));
        }
    }

    /**
     * Validates the collection against expected values.
     *
     * @param collection the collection
     * @param expectedValues the expected values
     */
    private static void validateCollection(Collection<? extends Long> collection, long... expectedValues)
    {
        Assert.assertEquals(expectedValues.length, collection.size());
        Assert.assertEquals(Boolean.valueOf(expectedValues.length == 0), Boolean.valueOf(collection.isEmpty()));
        int i = 0;
        for (Long collectionValue : collection)
        {
            Assert.assertTrue(collection.contains(collectionValue));
            Assert.assertEquals(expectedValues[i++], collectionValue.longValue());
        }
        i = 0;
        for (Object collectionValue : collection.toArray())
        {
            Assert.assertEquals(Long.valueOf(expectedValues[i++]), collectionValue);
        }
        Assert.assertTrue(collection.containsAll(CollectionUtilities.listView(expectedValues)));
    }
}
