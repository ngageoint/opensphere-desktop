package io.opensphere.core.model.time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for static methods in {@link TimeSpanList}.
 */
public class TimeSpanListTest
{
    /**
     * Test for {@link TimeSpanList#clone(Collection)}.
     */
    @Test
    public void testClone()
    {
        final TimeSpan ts1 = TimeSpan.get(8L, 20L);
        TimeSpanList singleton = TimeSpanList.singleton(ts1);

        List<TimeSpan> tsList = new ArrayList<>();
        tsList.add(TimeSpan.get(10L, 20L));
        tsList.add(TimeSpan.get(30L, 40L));
        tsList.add(TimeSpan.get(50L, 60L));

        TimeSpanList clone1 = singleton.clone(tsList);
        Assert.assertEquals(tsList.size(), clone1.size());
        Assert.assertTrue(clone1.containsAll(tsList));

        testImmutable(clone1);

        TimeSpanList emptyList = TimeSpanList.emptyList();
        TimeSpanList clone2 = emptyList.clone(tsList);
        Assert.assertEquals(tsList.size(), clone2.size());
        Assert.assertTrue(clone2.containsAll(tsList));

        testImmutable(clone2);
    }

    /** Test for {@link TimeSpanList#emptyList()}. */
    @Test
    public void testEmptyList()
    {
        TimeSpanList emptyList = TimeSpanList.emptyList();
        Assert.assertTrue(emptyList.isEmpty());
        testImmutable(emptyList);

        Assert.assertSame(TimeSpan.ZERO, emptyList.getExtent());
    }

    /** Test for {@link TimeSpanList#getExtent()}. */
    @Test
    public void testGetExtent()
    {
        final TimeSpan ts0 = TimeSpan.get(8L, 20L);
        final TimeSpan ts1 = TimeSpan.get(4L, 6L);
        final TimeSpan ts2 = TimeSpan.get(2L, 2L);
        TimeSpanList list = new TimeSpanList()
        {
            /** Serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public TimeSpanList clone(Collection<? extends TimeSpan> spans)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public TimeSpan get(int index)
            {
                switch (index)
                {
                    case 0:
                        return ts0;
                    case 1:
                        return ts1;
                    case 2:
                        return ts2;
                    default:
                        throw new IllegalArgumentException();
                }
            }

            @Override
            public TimeSpanList intersection(TimeSpan ts)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public TimeSpanList intersection(TimeSpanList other)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public int size()
            {
                return 3;
            }

            @Override
            public TimeSpanList union(TimeSpan ts)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public TimeSpanList union(TimeSpanList other)
            {
                throw new UnsupportedOperationException();
            }
        };

        Assert.assertEquals(TimeSpan.get(2L, 20L), list.getExtent());
    }

    /** Test for {@link TimeSpanList#mergeOverlaps(List)}. */
    @Test
    public void testMergeOverlaps()
    {
        final TimeSpan ts0 = TimeSpan.get(8L, 20L);
        final TimeSpan ts1 = TimeSpan.get(1L, 6L);
        final TimeSpan ts2 = TimeSpan.get(2L, 2L);
        final TimeSpan ts3 = TimeSpan.get(19L, 25L);
        final TimeSpan ts4 = TimeSpan.get(24L, 26L);
        List<TimeSpan> list = new ArrayList<>();
        list.add(ts0);
        list.add(ts1);
        list.add(ts2);
        list.add(ts3);
        list.add(ts4);
        TimeSpanList.mergeOverlaps(list);

        Assert.assertEquals(2, list.size());
        Assert.assertEquals(TimeSpan.get(8L, 26L), list.get(0));
        Assert.assertEquals(TimeSpan.get(1L, 6L), list.get(1));
    }

    /**
     * Test single time span intersection.
     */
    @Test
    public void testSingleTimeSpanIntersection()
    {
        List<TimeSpan> tsList = new ArrayList<>();
        tsList.add(TimeSpan.get(10L, 20L));
        tsList.add(TimeSpan.get(30L, 40L));
        tsList.add(TimeSpan.get(50L, 60L));

        TimeSpanArrayList tsl = new TimeSpanArrayList(tsList);
        TimeSpan overlapTS = TimeSpan.get(15, 55);

        TimeSpanList result = tsl.intersection(overlapTS);
        Assert.assertEquals(result.size(), 3);
        Assert.assertEquals(result.get(0), TimeSpan.get(15L, 20L));
        Assert.assertEquals(result.get(1), TimeSpan.get(30L, 40L));
        Assert.assertEquals(result.get(2), TimeSpan.get(50L, 55L));

        result = tsl.intersection(TimeSpan.get(1L, 2L));
        Assert.assertEquals(result.size(), 0);
    }

    /** Test for {@link TimeSpanList#singleton(TimeSpan)}. */
    @Test
    public void testSingleton()
    {
        final TimeSpan ts = TimeSpan.get(8L, 20L);
        TimeSpanList singleton = TimeSpanList.singleton(ts);
        Assert.assertEquals(1, singleton.size());
        Assert.assertSame(ts, singleton.get(0));

        testImmutable(singleton);

        Assert.assertSame(ts, singleton.getExtent());
    }

    /**
     * Test time span list intersection.
     */
    @Test
    public void testTimeSpanListIntersection()
    {
        List<TimeSpan> tsList1 = new ArrayList<>();
        tsList1.add(TimeSpan.get(10L, 20L));
        tsList1.add(TimeSpan.get(30L, 40L));
        tsList1.add(TimeSpan.get(50L, 60L));

        List<TimeSpan> tsList2 = new ArrayList<>();
        tsList2.add(TimeSpan.get(32L, 35L));
        tsList2.add(TimeSpan.get(39L, 44L));
        tsList2.add(TimeSpan.get(47L, 80L));

        TimeSpanArrayList tsl1 = new TimeSpanArrayList(tsList1);
        TimeSpanArrayList tsl2 = new TimeSpanArrayList(tsList2);

        TimeSpanList result = tsl1.intersection(tsl2);
        Assert.assertEquals(result.size(), 3);
        Assert.assertEquals(result.get(0), TimeSpan.get(32L, 35L));
        Assert.assertEquals(result.get(1), TimeSpan.get(39L, 40L));
        Assert.assertEquals(result.get(2), TimeSpan.get(50L, 60L));
    }

    /**
     * Test that add fails.
     *
     * @param list The list.
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    protected void testAddFail(TimeSpanList list)
    {
        try
        {
            list.add(TimeSpan.get(0L, 1L));
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
        try
        {
            list.add(0, TimeSpan.get(0L, 1L));
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
        try
        {
            list.addAll(Arrays.asList(TimeSpan.get(0L, 1L), TimeSpan.get(1L, 2L)));
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
        try
        {
            list.addAll(0, Arrays.asList(TimeSpan.get(0L, 1L), TimeSpan.get(1L, 2L)));
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
    }

    /**
     * Test that clear fails.
     *
     * @param list The list.
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    protected void testClearFail(TimeSpanList list)
    {
        try
        {
            list.clear();
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
    }

    /**
     * Test that a list is immutable.
     *
     * @param list The list.
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    protected void testImmutable(TimeSpanList list)
    {
        testAddFail(list);
        if (!list.isEmpty())
        {
            testClearFail(list);
            testRemoveFail(list);
            try
            {
                list.retainAll(Arrays.asList(TimeSpan.get(0L, 1L), TimeSpan.get(1L, 2L)));
                Assert.fail();
            }
            catch (UnsupportedOperationException e)
            {
                // expected
            }
            try
            {
                ListIterator<TimeSpan> iter = list.listIterator();
                iter.next();
                iter.remove();
                Assert.fail();
            }
            catch (UnsupportedOperationException e)
            {
                // expected
            }
        }
        try
        {
            list.set(0, TimeSpan.get(0L, 1L));
            Assert.fail();
        }
        catch (UnsupportedOperationException | IndexOutOfBoundsException e)
        {
            // expected
        }
    }

    /**
     * Test that remove fails.
     *
     * @param list The list.
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    protected void testRemoveFail(TimeSpanList list)
    {
        try
        {
            list.remove(0);
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
        try
        {
            list.remove(list.get(0));
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
        try
        {
            list.removeAll(Collections.singleton(list.get(0)));
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
    }
}
