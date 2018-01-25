package io.opensphere.core.model.time;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.junit.Assert;

/**
 * Test for {@link TimeSpanArrayList}.
 */
public class TimeSpanArrayListTest
{
    /**
     * Test for {@link TimeSpanArrayList#clone(java.util.Collection)}.
     */
    @Test
    public void testClone()
    {
        List<TimeSpan> list1 = new ArrayList<>();
        list1.add(TimeSpan.get(1L, 4L));
        list1.add(TimeSpan.get(5L, 7L));
        list1.add(TimeSpan.get(9L, 11L));

        TimeSpanList tsList = new TimeSpanArrayList(list1);

        List<TimeSpan> list2 = new ArrayList<>(list1);
        list2.add(TimeSpan.get(3L, 6L));
        list2.add(TimeSpan.get(10L, 12L));
        TimeSpanList clone = tsList.clone(list2);
        List<TimeSpan> expected = new ArrayList<>();
        expected.add(TimeSpan.get(1L, 7L));
        expected.add(TimeSpan.get(9L, 12L));

        Assert.assertEquals(list1.size(), tsList.size());
        Assert.assertTrue(tsList.containsAll(list1));
        Assert.assertEquals(expected.size(), clone.size());
        Assert.assertTrue(clone.containsAll(expected));
    }

    /**
     * Test for
     * {@link TimeSpanArrayList#TimeSpanArrayList(java.util.Collection)}.
     */
    @Test
    public void testConstruction()
    {
        List<TimeSpan> list = new ArrayList<>();
        list.add(TimeSpan.get(1L, 4L));
        list.add(TimeSpan.get(5L, 7L));
        list.add(TimeSpan.get(9L, 11L));

        TimeSpanList tsList = new TimeSpanArrayList(list);
        Assert.assertEquals(list.size(), tsList.size());
        Assert.assertTrue(tsList.containsAll(list));

        list.add(TimeSpan.get(3L, 6L));
        list.add(TimeSpan.get(10L, 12L));
        tsList = new TimeSpanArrayList(list);
        List<TimeSpan> expected = new ArrayList<>();
        expected.add(TimeSpan.get(1L, 7L));
        expected.add(TimeSpan.get(9L, 12L));
        Assert.assertEquals(expected.size(), tsList.size());
        Assert.assertTrue(tsList.containsAll(expected));
    }

    /**
     * Test for {@link TimeSpanArrayList#covers(TimeSpan)}.
     */
    @Test
    public void testCoversTimeSpan()
    {
        List<TimeSpan> list = new ArrayList<>();
        list.add(TimeSpan.get(1L, 4L));
        list.add(TimeSpan.get(5L, 7L));
        list.add(TimeSpan.get(9L, 11L));

        TimeSpanList tsList = new TimeSpanArrayList(list);

        Assert.assertTrue(tsList.covers(TimeSpan.get(1L, 4L)));
        Assert.assertFalse(tsList.covers(TimeSpan.get(1L, 5L)));
        Assert.assertFalse(tsList.covers(TimeSpan.get(8L, 11L)));
        Assert.assertFalse(tsList.covers(TimeSpan.get(11L, 12L)));
        Assert.assertFalse(tsList.covers(TimeSpan.get(8L, 12L)));

        list.add(TimeSpan.get(3L, 6L));
        tsList = new TimeSpanArrayList(list);
        Assert.assertTrue(tsList.covers(TimeSpan.get(1L, 7L)));
    }

    /**
     * Test for {@link TimeSpanArrayList#covers(TimeSpanList)}.
     */
    @Test
    public void testCoversTimeSpanList()
    {
        List<TimeSpan> list1 = new ArrayList<>();
        list1.add(TimeSpan.get(1L, 4L));
        list1.add(TimeSpan.get(5L, 7L));
        list1.add(TimeSpan.get(9L, 11L));

        List<TimeSpan> list2 = new ArrayList<>();
        list2.add(TimeSpan.get(2L, 4L));
        list2.add(TimeSpan.get(5L, 6L));
        list2.add(TimeSpan.get(9L, 10L));

        TimeSpanList tsList1 = new TimeSpanArrayList(list1);
        TimeSpanList tsList2 = new TimeSpanArrayList(list2);

        Assert.assertTrue(tsList1.covers(tsList2));
        Assert.assertFalse(tsList2.covers(tsList1));

        list2.add(TimeSpan.get(3L, 6L));
        tsList2 = new TimeSpanArrayList(list2);
        Assert.assertFalse(tsList1.covers(tsList2));
        Assert.assertFalse(tsList2.covers(tsList1));
    }

    /**
     * Test for {@link TimeSpanArrayList#indexOf(Object)}.
     */
    @Test
    public void testIndexOf()
    {
        List<TimeSpan> list = new ArrayList<>();
        for (int i = 49; i >= 0; --i)
        {
            list.add(TimeSpan.get(i, i + 1));
        }
        TimeSpanList tsList = new TimeSpanArrayList(list);

        Assert.assertEquals(10, tsList.indexOf(TimeSpan.get(39L, 40L)));
    }

    /**
     * Test for {@link TimeSpanArrayList#intersects(TimeSpan)}.
     */
    @Test
    public void testIntersects()
    {
        List<TimeSpan> list = new ArrayList<>();
        list.add(TimeSpan.get(1L, 4L));
        list.add(TimeSpan.get(5L, 7L));
        list.add(TimeSpan.get(9L, 11L));

        TimeSpanList tsList = new TimeSpanArrayList(list);
        Assert.assertFalse(tsList.intersects(TimeSpan.get(0L, 1L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(1L, 1L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(0L, 2L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(1L, 2L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(2L, 2L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(2L, 3L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(2L, 4L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(2L, 5L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(2L, 6L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(2L, 7L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(2L, 8L)));
        Assert.assertFalse(tsList.intersects(TimeSpan.get(4L, 4L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(5L, 5L)));
        Assert.assertFalse(tsList.intersects(TimeSpan.get(7L, 9L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(7L, 10L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(7L, 11L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(7L, 12L)));
        Assert.assertFalse(tsList.intersects(TimeSpan.get(7L, 7L)));
        Assert.assertTrue(tsList.intersects(TimeSpan.get(9L, 9L)));
        Assert.assertFalse(tsList.intersects(TimeSpan.get(11L, 11L)));
        Assert.assertFalse(tsList.intersects(TimeSpan.get(11L, 12L)));
    }
}
