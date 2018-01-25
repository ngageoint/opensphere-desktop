package io.opensphere.core.geometry.constraint;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanArrayList;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.units.duration.Weeks;

/**
 * Test for {@link BoundedTimeConstraintTest}.
 */
public class BoundedTimeConstraintTest
{
    /**
     * Test constructing a bad constraint.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadConstruct1()
    {
        BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, new Seconds(3600), new Seconds(3600));
    }

    /**
     * Test constructing a bad constraint.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBadConstruct2()
    {
        BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, new Seconds(3601), new Seconds(3600));
    }

    /**
     * Test for {@link BoundedTimeConstraint#check(TimeSpan)}.
     */
    @Test
    public void testCheckTimeSpan()
    {
        BoundedTimeConstraint hourConstraint = BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, Seconds.ZERO,
                Hours.ONE);
        BoundedTimeConstraint dayConstraint = BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, Hours.ONE, Days.ONE);
        BoundedTimeConstraint weekConstraint = BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, Days.ONE, Weeks.ONE);
        BoundedTimeConstraint monthConstraint = BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, Weeks.ONE,
                new Days(31));
        BoundedTimeConstraint yearConstraint = BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, new Days(31),
                new Days(365));
        BoundedTimeConstraint unboundedConstraint = BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, null, null);

        TimeSpan hourSpan = TimeSpan.get(0, Hours.ONE);
        Assert.assertTrue(hourConstraint.check(hourSpan));
        Assert.assertFalse(dayConstraint.check(hourSpan));
        Assert.assertFalse(weekConstraint.check(hourSpan));
        Assert.assertFalse(monthConstraint.check(hourSpan));
        Assert.assertFalse(yearConstraint.check(hourSpan));
        Assert.assertTrue(unboundedConstraint.check(hourSpan));

        TimeSpan daySpan1 = TimeSpan.get(0, new Milliseconds(Hours.ONE).longValue() + 1L);
        Assert.assertFalse(hourConstraint.check(daySpan1));
        Assert.assertTrue(dayConstraint.check(daySpan1));
        Assert.assertFalse(weekConstraint.check(daySpan1));
        Assert.assertFalse(monthConstraint.check(daySpan1));
        Assert.assertFalse(yearConstraint.check(daySpan1));
        Assert.assertTrue(unboundedConstraint.check(hourSpan));

        TimeSpan daySpan2 = TimeSpan.get(0, Days.ONE);
        Assert.assertFalse(hourConstraint.check(daySpan2));
        Assert.assertTrue(dayConstraint.check(daySpan2));
        Assert.assertFalse(weekConstraint.check(daySpan2));
        Assert.assertFalse(monthConstraint.check(daySpan2));
        Assert.assertFalse(yearConstraint.check(daySpan2));
        Assert.assertTrue(unboundedConstraint.check(hourSpan));

        TimeSpan weekSpan1 = TimeSpan.get(0, new Milliseconds(Days.ONE).longValue() + 1L);
        Assert.assertFalse(hourConstraint.check(weekSpan1));
        Assert.assertFalse(dayConstraint.check(weekSpan1));
        Assert.assertTrue(weekConstraint.check(weekSpan1));
        Assert.assertFalse(monthConstraint.check(weekSpan1));
        Assert.assertFalse(yearConstraint.check(weekSpan1));
        Assert.assertTrue(unboundedConstraint.check(hourSpan));

        TimeSpan weekSpan2 = TimeSpan.get(0, Weeks.ONE);
        Assert.assertFalse(hourConstraint.check(weekSpan2));
        Assert.assertFalse(dayConstraint.check(weekSpan2));
        Assert.assertTrue(weekConstraint.check(weekSpan2));
        Assert.assertFalse(monthConstraint.check(weekSpan2));
        Assert.assertFalse(yearConstraint.check(weekSpan2));
        Assert.assertTrue(unboundedConstraint.check(hourSpan));

        TimeSpan monthSpan1 = TimeSpan.get(0, new Milliseconds(Weeks.ONE).longValue() + 1L);
        Assert.assertFalse(hourConstraint.check(monthSpan1));
        Assert.assertFalse(dayConstraint.check(monthSpan1));
        Assert.assertFalse(weekConstraint.check(monthSpan1));
        Assert.assertTrue(monthConstraint.check(monthSpan1));
        Assert.assertFalse(yearConstraint.check(monthSpan1));
        Assert.assertTrue(unboundedConstraint.check(hourSpan));

        TimeSpan monthSpan2 = TimeSpan.get(0, new Days(31));
        Assert.assertFalse(hourConstraint.check(monthSpan2));
        Assert.assertFalse(dayConstraint.check(monthSpan2));
        Assert.assertFalse(weekConstraint.check(monthSpan2));
        Assert.assertTrue(monthConstraint.check(monthSpan2));
        Assert.assertFalse(yearConstraint.check(monthSpan2));
        Assert.assertTrue(unboundedConstraint.check(hourSpan));

        TimeSpan yearSpan1 = TimeSpan.get(0, new Milliseconds(new Days(31)).longValue() + 1L);
        Assert.assertFalse(hourConstraint.check(yearSpan1));
        Assert.assertFalse(dayConstraint.check(yearSpan1));
        Assert.assertFalse(weekConstraint.check(yearSpan1));
        Assert.assertFalse(monthConstraint.check(yearSpan1));
        Assert.assertTrue(yearConstraint.check(yearSpan1));
        Assert.assertTrue(unboundedConstraint.check(hourSpan));

        TimeSpan yearSpan2 = TimeSpan.get(0, new Days(365));
        Assert.assertFalse(hourConstraint.check(yearSpan2));
        Assert.assertFalse(dayConstraint.check(yearSpan2));
        Assert.assertFalse(weekConstraint.check(yearSpan2));
        Assert.assertFalse(monthConstraint.check(yearSpan2));
        Assert.assertTrue(yearConstraint.check(yearSpan2));
        Assert.assertTrue(unboundedConstraint.check(hourSpan));
    }

    /**
     * Test for {@link BoundedTimeConstraint#check(java.util.Collection)}.
     */
    @Test
    public void testCheckTimeSpanList()
    {
        BoundedTimeConstraint hourConstraint = BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, Seconds.ZERO,
                Hours.ONE);
        BoundedTimeConstraint dayConstraint = BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, Hours.ONE, Days.ONE);
        BoundedTimeConstraint weekConstraint = BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, Days.ONE, Weeks.ONE);
        BoundedTimeConstraint monthConstraint = BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, Weeks.ONE,
                new Days(31));
        BoundedTimeConstraint yearConstraint = BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, new Days(31),
                new Days(365));

        TimeSpanList emptyList = new TimeSpanArrayList(Collections.<TimeSpan>emptySet());
        Assert.assertFalse(hourConstraint.check(emptyList));
        Assert.assertFalse(dayConstraint.check(emptyList));
        Assert.assertFalse(weekConstraint.check(emptyList));
        Assert.assertFalse(monthConstraint.check(emptyList));
        Assert.assertFalse(yearConstraint.check(emptyList));

        TimeSpan hourSpan = TimeSpan.get(0, Hours.ONE);
        TimeSpanList list1 = TimeSpanList.singleton(TimeSpan.get(0, Hours.ONE));
        Assert.assertTrue(hourConstraint.check(list1));
        Assert.assertFalse(dayConstraint.check(list1));
        Assert.assertFalse(weekConstraint.check(list1));
        Assert.assertFalse(monthConstraint.check(list1));
        Assert.assertFalse(yearConstraint.check(list1));

        TimeSpan daySpan1 = TimeSpan.get(hourSpan.getEnd(), hourSpan.getEnd() + new Milliseconds(Hours.ONE).longValue() + 1L);
        TimeSpanArrayList list2 = new TimeSpanArrayList(Arrays.asList(hourSpan, daySpan1));
        Assert.assertTrue(hourConstraint.check(list2));
        Assert.assertTrue(dayConstraint.check(list2));
        Assert.assertFalse(weekConstraint.check(list2));
        Assert.assertFalse(monthConstraint.check(list2));
        Assert.assertFalse(yearConstraint.check(list2));

        TimeSpan weekSpan1 = TimeSpan.get(daySpan1.getEnd(), daySpan1.getEnd() + new Milliseconds(Days.ONE).longValue() + 1L);
        TimeSpanArrayList list3 = new TimeSpanArrayList(Arrays.asList(hourSpan, daySpan1, weekSpan1));
        Assert.assertTrue(hourConstraint.check(list3));
        Assert.assertTrue(dayConstraint.check(list3));
        Assert.assertTrue(weekConstraint.check(list3));
        Assert.assertFalse(monthConstraint.check(list3));
        Assert.assertFalse(yearConstraint.check(list3));

        TimeSpan monthSpan1 = TimeSpan.get(weekSpan1.getEnd(), weekSpan1.getEnd() + new Milliseconds(Weeks.ONE).longValue() + 1L);
        TimeSpanArrayList list4 = new TimeSpanArrayList(Arrays.asList(hourSpan, daySpan1, weekSpan1, monthSpan1));
        Assert.assertTrue(hourConstraint.check(list4));
        Assert.assertTrue(dayConstraint.check(list4));
        Assert.assertTrue(weekConstraint.check(list4));
        Assert.assertTrue(monthConstraint.check(list4));
        Assert.assertFalse(yearConstraint.check(list4));

        TimeSpan yearSpan1 = TimeSpan.get(monthSpan1.getEnd(),
                monthSpan1.getEnd() + new Milliseconds(new Days(31)).longValue() + 1L);
        TimeSpanArrayList list5 = new TimeSpanArrayList(Arrays.asList(hourSpan, daySpan1, weekSpan1, monthSpan1, yearSpan1));
        Assert.assertTrue(hourConstraint.check(list5));
        Assert.assertTrue(dayConstraint.check(list5));
        Assert.assertTrue(weekConstraint.check(list5));
        Assert.assertTrue(monthConstraint.check(list5));
        Assert.assertTrue(yearConstraint.check(list5));
    }

    /**
     * Test the object pool.
     */
    @Test
    public void testPool()
    {
        BoundedTimeConstraint con = BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, Seconds.ZERO, Hours.ONE);
        Assert.assertSame(con, BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, Seconds.ZERO, Hours.ONE));
        Assert.assertNotSame(con, BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, Seconds.ONE, Hours.ONE));
        Assert.assertNotSame(con, BoundedTimeConstraint.getTimeConstraint(TimeSpan.TIMELESS, Seconds.ZERO, new Hours(2)));
        Assert.assertNotSame(con, BoundedTimeConstraint.getTimeConstraint(TimeSpan.get(0L, 3600L), Seconds.ZERO, Hours.ONE));
        Assert.assertNotSame(con, TimeConstraint.getTimeConstraint(TimeSpan.TIMELESS));
    }
}
