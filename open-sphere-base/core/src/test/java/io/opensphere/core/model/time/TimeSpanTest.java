package io.opensphere.core.model.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import io.opensphere.core.model.RangeRelationType;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.DateTimeFormats;

/** Test for {@link TimeSpan}. */
@SuppressWarnings("PMD.GodClass")
public class TimeSpanTest
{
    /** Test for {@link TimeSpan#clamp(TimeInstant)}. */
    @Test
    public void testClampTimeInstant()
    {
        assertEquals(TimeInstant.get(100), TimeSpan.get(100, 200).clamp(TimeInstant.get(50)));
        assertEquals(TimeInstant.get(100), TimeSpan.get(100, 200).clamp(TimeInstant.get(100)));
        assertEquals(TimeInstant.get(150), TimeSpan.get(100, 200).clamp(TimeInstant.get(150)));
        assertEquals(TimeInstant.get(200), TimeSpan.get(100, 200).clamp(TimeInstant.get(200)));
        assertEquals(TimeInstant.get(200), TimeSpan.get(100, 200).clamp(TimeInstant.get(250)));
    }

    /** Test for {@link TimeSpan#clamp(TimeSpan)}. */
    @Test
    public void testClampTimeSpan()
    {
        assertEquals(TimeSpan.get(100, 100), TimeSpan.get(100, 200).clamp(TimeSpan.get(50, 50)));
        assertEquals(TimeSpan.get(100, 150), TimeSpan.get(100, 200).clamp(TimeSpan.get(50, 100)));
        assertEquals(TimeSpan.get(100, 200), TimeSpan.get(100, 200).clamp(TimeSpan.get(50, 150)));
        assertEquals(TimeSpan.get(100, 200), TimeSpan.get(100, 200).clamp(TimeSpan.get(50, 200)));
        assertEquals(TimeSpan.get(100, 200), TimeSpan.get(100, 200).clamp(TimeSpan.get(50, 250)));
        assertEquals(TimeSpan.get(100, 100), TimeSpan.get(100, 200).clamp(TimeSpan.get(100, 100)));
        assertEquals(TimeSpan.get(100, 150), TimeSpan.get(100, 200).clamp(TimeSpan.get(100, 150)));
        assertEquals(TimeSpan.get(100, 200), TimeSpan.get(100, 200).clamp(TimeSpan.get(100, 200)));
        assertEquals(TimeSpan.get(100, 200), TimeSpan.get(100, 200).clamp(TimeSpan.get(100, 250)));
        assertEquals(TimeSpan.get(150, 150), TimeSpan.get(100, 200).clamp(TimeSpan.get(150, 150)));
        assertEquals(TimeSpan.get(150, 200), TimeSpan.get(100, 200).clamp(TimeSpan.get(150, 200)));
        assertEquals(TimeSpan.get(100, 200), TimeSpan.get(100, 200).clamp(TimeSpan.get(150, 250)));
        assertEquals(TimeSpan.get(200, 200), TimeSpan.get(100, 200).clamp(TimeSpan.get(200, 200)));
        assertEquals(TimeSpan.get(150, 200), TimeSpan.get(100, 200).clamp(TimeSpan.get(200, 250)));
        assertEquals(TimeSpan.get(200, 200), TimeSpan.get(100, 200).clamp(TimeSpan.get(250, 250)));
    }

    /**
     * Test for {@link TimeSpan#compareTo(TimeSpan)}.
     */
    @Test
    public void testCompareTo()
    {
        assertEquals(0, TimeSpan.ZERO.compareTo(TimeSpan.ZERO));
        assertEquals(0, TimeSpan.TIMELESS.compareTo(TimeSpan.TIMELESS));
        assertEquals(-1, TimeSpan.TIMELESS.compareTo(TimeSpan.ZERO));
        assertEquals(1, TimeSpan.ZERO.compareTo(TimeSpan.TIMELESS));

        assertEquals(-1, TimeSpan.get(0, 300).compareTo(TimeSpan.get(100, 200)));
        assertEquals(-1, TimeSpan.get(0, 200).compareTo(TimeSpan.get(100, 200)));
        assertEquals(-1, TimeSpan.get(0, 100).compareTo(TimeSpan.get(100, 200)));
        assertEquals(-1, TimeSpan.get(100, 100).compareTo(TimeSpan.get(100, 200)));
        assertEquals(-1, TimeSpan.get(100, 100).compareTo(TimeSpan.get(200, 200)));
        assertEquals(0, TimeSpan.get(100, 100).compareTo(TimeSpan.get(100, 100)));
        assertEquals(1, TimeSpan.get(200, 200).compareTo(TimeSpan.get(100, 100)));
        assertEquals(1, TimeSpan.get(100, 200).compareTo(TimeSpan.get(100, 100)));
        assertEquals(1, TimeSpan.get(100, 200).compareTo(TimeSpan.get(0, 100)));
        assertEquals(1, TimeSpan.get(100, 200).compareTo(TimeSpan.get(0, 200)));
        assertEquals(1, TimeSpan.get(100, 200).compareTo(TimeSpan.get(0, 300)));

        assertEquals(1, TimeSpan.get(-100, -100).compareTo(TimeSpan.get(-200, -100)));
        assertEquals(1, TimeSpan.get(-100, -100).compareTo(TimeSpan.get(-200, -200)));
        assertEquals(0, TimeSpan.get(-100, -100).compareTo(TimeSpan.get(-100, -100)));
        assertEquals(-1, TimeSpan.get(-200, -200).compareTo(TimeSpan.get(-100, -100)));
        assertEquals(-1, TimeSpan.get(-200, -100).compareTo(TimeSpan.get(-100, -100)));
    }

    /** Test for {@link TimeSpan#contains(TimeSpan)}. */
    @Test
    public void testContainsTimeSpan()
    {
        TimeSpan span = TimeSpan.get(100L, 200L);

        assertFalse(span.contains(TimeSpan.get(50L, 50L)));
        assertFalse(span.contains(TimeSpan.get(50L, 99L)));
        assertFalse(span.contains(TimeSpan.get(50L, 100L)));
        assertFalse(span.contains(TimeSpan.get(50L, 101L)));
        assertFalse(span.contains(TimeSpan.get(50L, 199L)));
        assertFalse(span.contains(TimeSpan.get(50L, 200L)));
        assertFalse(span.contains(TimeSpan.get(50L, 201L)));
        assertFalse(span.contains(TimeSpan.get(50L, 250L)));

        assertFalse(span.contains(TimeSpan.get(99L, 99L)));
        assertFalse(span.contains(TimeSpan.get(99L, 100L)));
        assertFalse(span.contains(TimeSpan.get(99L, 101L)));
        assertFalse(span.contains(TimeSpan.get(99L, 199L)));
        assertFalse(span.contains(TimeSpan.get(99L, 200L)));
        assertFalse(span.contains(TimeSpan.get(99L, 201L)));
        assertFalse(span.contains(TimeSpan.get(99L, 250L)));

        assertTrue(span.contains(TimeSpan.get(100L, 100L)));
        assertTrue(span.contains(TimeSpan.get(100L, 101L)));
        assertTrue(span.contains(TimeSpan.get(100L, 199L)));
        assertTrue(span.contains(TimeSpan.get(100L, 200L)));
        assertFalse(span.contains(TimeSpan.get(100L, 201L)));
        assertFalse(span.contains(TimeSpan.get(100L, 250L)));

        assertTrue(span.contains(TimeSpan.get(101L, 101L)));
        assertTrue(span.contains(TimeSpan.get(101L, 199L)));
        assertTrue(span.contains(TimeSpan.get(101L, 200L)));
        assertFalse(span.contains(TimeSpan.get(101L, 201L)));
        assertFalse(span.contains(TimeSpan.get(101L, 250L)));

        assertTrue(span.contains(TimeSpan.get(199L, 199L)));
        assertTrue(span.contains(TimeSpan.get(199L, 200L)));
        assertFalse(span.contains(TimeSpan.get(199L, 201L)));
        assertFalse(span.contains(TimeSpan.get(199L, 250L)));

        assertFalse(span.contains(TimeSpan.get(200L, 200L)));
        assertFalse(span.contains(TimeSpan.get(200L, 201L)));
        assertFalse(span.contains(TimeSpan.get(200L, 250L)));

        assertFalse(span.contains(TimeSpan.get(201L, 201L)));
        assertFalse(span.contains(TimeSpan.get(201L, 250L)));

        assertFalse(span.contains(TimeSpan.get(250L, 250L)));

        assertFalse(span.contains(TimeSpan.TIMELESS));
        assertTrue(TimeSpan.TIMELESS.contains(span));
    }

    /**
     * Test for {@link TimeSpan#equals(Object)}.
     */
    @SuppressWarnings("PMD.UseAssertEqualsInsteadOfAssertTrue")
    @Test
    public void testEquals()
    {
        TimeSpan one = TimeSpan.get(100L, 200L);
        TimeSpan sameAsOne = TimeSpan.get(100L, 200L);
        TimeSpan two = TimeSpan.get(100L, 201L);
        TimeSpan three = TimeSpan.get(99L, 200L);

        assertTrue(one.equals(sameAsOne));
        assertTrue(sameAsOne.equals(one));
        assertFalse(one.equals(two));
        assertFalse(one.equals(three));
    }

    /** Test for {@link ExtentAccumulator}. */
    @Test
    public void testExtentAccumulator()
    {
        ExtentAccumulator ea = new ExtentAccumulator();

        assertSame(TimeSpan.ZERO, ea.getExtent());

        ea.add(TimeSpan.get(50L, 100L));
        assertEquals(TimeSpan.get(50L, 100L), ea.getExtent());

        ea.add(TimeSpan.get(60L, 70L));
        assertEquals(TimeSpan.get(50L, 100L), ea.getExtent());

        ea.add(TimeSpan.get(100L, 100L));
        assertEquals(TimeSpan.get(50L, 100L), ea.getExtent());

        ea.add(TimeSpan.get(100L, 101L));
        assertEquals(TimeSpan.get(50L, 101L), ea.getExtent());

        ea.add(TimeSpan.get(49L, 50L));
        assertEquals(TimeSpan.get(49L, 101L), ea.getExtent());

        ea.add(TimeSpan.newUnboundedEndTimeSpan(55L));
        assertEquals(TimeSpan.newUnboundedEndTimeSpan(49L), ea.getExtent());

        ea.add(TimeSpan.newUnboundedEndTimeSpan(45L));
        assertEquals(TimeSpan.newUnboundedEndTimeSpan(45L), ea.getExtent());

        ea.add(TimeSpan.newUnboundedStartTimeSpan(55L));
        assertTrue(ea.getExtent().isTimeless());

        ea = new ExtentAccumulator();
        ea.add(TimeSpan.get(50L, 100L));
        ea.add(TimeSpan.newUnboundedStartTimeSpan(55L));
        assertEquals(TimeSpan.newUnboundedStartTimeSpan(100L), ea.getExtent());

        ea.add(TimeSpan.newUnboundedStartTimeSpan(105L));
        assertEquals(TimeSpan.newUnboundedStartTimeSpan(105L), ea.getExtent());

        ea = new ExtentAccumulator();
        ea.add(TimeSpan.newUnboundedEndTimeSpan(100L));
        assertEquals(TimeSpan.newUnboundedEndTimeSpan(100L), ea.getExtent());

        ea = new ExtentAccumulator();
        ea.add(TimeSpan.newUnboundedStartTimeSpan(100L));
        assertEquals(TimeSpan.newUnboundedStartTimeSpan(100L), ea.getExtent());
    }

    /** Test for {@link TimeSpan#isTimeless()}. */
    @Test
    public void testIsTimeless()
    {
        assertTrue(TimeSpan.TIMELESS.isTimeless());
        assertFalse(TimeSpan.get(0, 25).isTimeless());
    }

    /** Test for {@link TimeSpan#getTimeSpan()}. */
    @Test
    public void getTimeSpan()
    {
        TimeSpan span = TimeSpan.TIMELESS;

        assertTrue(span == span.getTimeSpan());
    }

    /** Test for {@link TimeSpan#isBefore(TimeSpan)}. */
    @Test
    public void testIsBefore()
    {
        TimeSpan beforeTimespan = TimeSpan.get(0, new Seconds(5));
        TimeSpan afterTimespan = TimeSpan.get(10000, new Seconds(5));

        assertTrue(beforeTimespan.isBefore(afterTimespan));
        assertFalse(afterTimespan.isBefore(beforeTimespan));
    }

    /** Test for {@link TimeSpan#isAfter(TimeSpan)}. */
    @Test
    public void testIsAfter()
    {
        TimeSpan beforeTimespan = TimeSpan.get(0, new Seconds(5));
        TimeSpan afterTimespan = TimeSpan.get(10000, new Seconds(5));

        assertFalse(beforeTimespan.isAfter(afterTimespan));
        assertTrue(afterTimespan.isAfter(beforeTimespan));
    }

    /** Test for {@link TimeSpan#overlaps(Collection)}. */
    @Test
    public void testOverlapsEmptyList()
    {
        TimeSpan span = TimeSpan.get(0, new Seconds(5));

        Collection<TimeSpan> emptyCollection = Collections.emptyList();

        assertFalse(span.overlaps(emptyCollection));
    }

    /** Test for {@link TimeSpan#overlaps(Collection)}. */
    @Test
    public void testOverlapsNonOverlap()
    {
        TimeSpan span = TimeSpan.get(0, new Seconds(5));

        Collection<TimeSpan> emptyCollection = Arrays.asList(TimeSpan.get(10000, new Seconds(5)),
                TimeSpan.get(20000, new Seconds(5)), TimeSpan.get(30000, new Seconds(5)));

        assertFalse(span.overlaps(emptyCollection));
    }

    /** Test for {@link TimeSpan#overlaps(Collection)}. */
    @Test
    public void testOverlapsOverlap()
    {
        TimeSpan span = TimeSpan.get(0, new Seconds(5));

        Collection<TimeSpan> emptyCollection = Arrays.asList(TimeSpan.get(1, new Seconds(5)), TimeSpan.get(20000, new Seconds(5)),
                TimeSpan.get(30000, new Seconds(5)));

        assertTrue(span.overlaps(emptyCollection));
    }

    /** Test for {@link TimeSpan#getMidpointInstant()}. */
    @Test
    public void testGetMidpointInstant()
    {
        TimeSpan span = TimeSpan.get(0, 2);

        TimeInstant instant = span.getMidpointInstant();

        assertEquals(1, instant.getEpochMillis());
    }

    /** Test for {@link TimeSpan#formsContiguousRange(TimeSpan)}. */
    @Test
    public void testFormsContiguousRange()
    {
        //@formatter:off
        TimeSpan[] spans = new TimeSpan[] {
            TimeSpan.get(50L, 99L),
            TimeSpan.get(50L, 100L),
            TimeSpan.get(50L, 101L),
            TimeSpan.get(50L, 199L),
            TimeSpan.get(50L, 200L),
            TimeSpan.get(50L, 201L),
            TimeSpan.get(99L, 99L),
            TimeSpan.get(99L, 100L),
            TimeSpan.get(99L, 101L),
            TimeSpan.get(99L, 199L),
            TimeSpan.get(99L, 200L),
            TimeSpan.get(99L, 201L),
            TimeSpan.get(100L, 100L),
            TimeSpan.get(100L, 101L),
            TimeSpan.get(100L, 199L),
            TimeSpan.get(100L, 200L),
            TimeSpan.get(100L, 201L),
            TimeSpan.get(101L, 101L),
            TimeSpan.get(101L, 199L),
            TimeSpan.get(101L, 200L),
            TimeSpan.get(101L, 201L),
            TimeSpan.get(199L, 199L),
            TimeSpan.get(199L, 200L),
            TimeSpan.get(199L, 201L),
            TimeSpan.get(200L, 200L),
            TimeSpan.get(200L, 201L),
            TimeSpan.get(201L, 201L),
            TimeSpan.newUnboundedStartTimeSpan(50L),
            TimeSpan.newUnboundedStartTimeSpan(99L),
            TimeSpan.newUnboundedStartTimeSpan(100L),
            TimeSpan.newUnboundedStartTimeSpan(101L),
            TimeSpan.newUnboundedStartTimeSpan(199L),
            TimeSpan.newUnboundedStartTimeSpan(200L),
            TimeSpan.newUnboundedStartTimeSpan(201L),
            TimeSpan.newUnboundedEndTimeSpan(50L),
            TimeSpan.newUnboundedEndTimeSpan(99L),
            TimeSpan.newUnboundedEndTimeSpan(100L),
            TimeSpan.newUnboundedEndTimeSpan(101L),
            TimeSpan.newUnboundedEndTimeSpan(199L),
            TimeSpan.newUnboundedEndTimeSpan(200L),
            TimeSpan.newUnboundedEndTimeSpan(201L),
            TimeSpan.TIMELESS, };
        //@formatter:on

        boolean[] expected = new boolean[903];

        // index1=0 index2=0..41 expected=0..41
        Arrays.fill(expected, 0, 12, true);
        Arrays.fill(expected, 27, 36, true);
        expected[41] = true;

        // index1=1 index2=1..41 expected=42..82
        Arrays.fill(expected, 42, 58, true);
        Arrays.fill(expected, 68, 78, true);
        expected[82] = true;

        // index1=2 index2=2..41 expected=83..122
        Arrays.fill(expected, 82, 102, true);
        Arrays.fill(expected, 108, 119, true);
        expected[122] = true;

        // index1=3 index2=3..41 expected=123..161
        Arrays.fill(expected, 123, 144, true);
        Arrays.fill(expected, 147, 159, true);
        expected[161] = true;

        // index1=4 index2=4..41 expected=162..199
        Arrays.fill(expected, 162, 184, true);
        Arrays.fill(expected, 185, 198, true);
        expected[199] = true;

        // index1=5 index2=5..41 expected=200..236
        Arrays.fill(expected, 200, 236, true);
        expected[236] = true;

        // index1=6 index2=6..41 expected=237..272
        Arrays.fill(expected, 237, 243, true);
        Arrays.fill(expected, 259, 267, true);
        expected[272] = true;

        // index1=7 index2=7..41 expected=273..307
        Arrays.fill(expected, 273, 283, true);
        Arrays.fill(expected, 294, 303, true);
        expected[307] = true;

        // index1=8 index2=8..41 expected=308..341
        Arrays.fill(expected, 308, 321, true);
        Arrays.fill(expected, 328, 338, true);
        expected[341] = true;

        // index1=9 index2=9..41 expected=342..374
        Arrays.fill(expected, 342, 357, true);
        Arrays.fill(expected, 361, 372, true);
        expected[374] = true;

        // index1=10 index2=10..41 expected=375..406
        Arrays.fill(expected, 375, 391, true);
        Arrays.fill(expected, 393, 405, true);
        expected[406] = true;

        // index1=11 index2=11..41 expected=407..437
        Arrays.fill(expected, 407, 423, true);
        Arrays.fill(expected, 424, 437, true);
        expected[437] = true;

        // index1=12 index2=12..41 expected=438..467
        Arrays.fill(expected, 438, 443, true);
        Arrays.fill(expected, 455, 463, true);
        expected[467] = true;

        // index1=13 index2=13..41 expected=468..496
        Arrays.fill(expected, 468, 476, true);
        Arrays.fill(expected, 484, 493, true);
        expected[496] = true;

        // index1=14 index2=14..41 expected=497..524
        Arrays.fill(expected, 497, 507, true);
        Arrays.fill(expected, 512, 522, true);
        expected[524] = true;

        // index1=15 index2=15..41 expected=525..551
        Arrays.fill(expected, 525, 536, true);
        Arrays.fill(expected, 539, 550, true);
        expected[551] = true;

        // index1=16 index2=16..41 expected=552..577
        Arrays.fill(expected, 552, 563, true);
        Arrays.fill(expected, 565, 577, true);
        expected[577] = true;

        // index1=17 index2=17..41 expected=578..602
        Arrays.fill(expected, 578, 582, true);
        Arrays.fill(expected, 591, 599, true);
        expected[602] = true;

        // index1=18 index2=18..41 expected=603..626
        Arrays.fill(expected, 603, 609, true);
        Arrays.fill(expected, 615, 624, true);
        expected[626] = true;

        // index1=19 index2=19..41 expected=627..649
        Arrays.fill(expected, 627, 634, true);
        Arrays.fill(expected, 638, 648, true);
        expected[649] = true;

        // index1=20 index2=20..41 expected=650..671
        Arrays.fill(expected, 650, 657, true);
        Arrays.fill(expected, 660, 671, true);
        expected[671] = true;

        // index1=21 index2=21..41 expected=672..692
        Arrays.fill(expected, 672, 675, true);
        Arrays.fill(expected, 682, 690, true);
        expected[692] = true;

        // index1=22 index2=22..41 expected=693..712
        Arrays.fill(expected, 693, 697, true);
        Arrays.fill(expected, 702, 711, true);
        expected[712] = true;

        // index1=23 index2=23..41 expected=713..731
        Arrays.fill(expected, 713, 717, true);
        Arrays.fill(expected, 721, 731, true);
        expected[731] = true;

        // index1=24 index2=24..41 expected=732..749
        Arrays.fill(expected, 732, 734, true);
        Arrays.fill(expected, 740, 748, true);
        expected[749] = true;

        // index1=25 index2=25..41 expected=750..766
        Arrays.fill(expected, 750, 752, true);
        Arrays.fill(expected, 757, 766, true);
        expected[766] = true;

        // index1=26 index2=26..41 expected=767..782
        Arrays.fill(expected, 767, 768, true);
        Arrays.fill(expected, 774, 782, true);
        expected[782] = true;

        // index1=27 index2=27..41 expected=783..797
        Arrays.fill(expected, 783, 791, true);
        expected[797] = true;

        // index1=28 index2=28..41 expected=798..811
        Arrays.fill(expected, 798, 806, true);
        expected[811] = true;

        // index1=29 index2=29..41 expected=812..824
        Arrays.fill(expected, 812, 820, true);
        expected[824] = true;

        // index1=30 index2=30..41 expected=825..836
        Arrays.fill(expected, 825, 833, true);
        expected[836] = true;

        // index1=31 index2=31..41 expected=837..847
        Arrays.fill(expected, 837, 845, true);
        expected[847] = true;

        // index1=32 index2=32..41 expected=848..857
        Arrays.fill(expected, 848, 856, true);
        expected[857] = true;

        // index1=33 index2=33..41 expected=858..866
        Arrays.fill(expected, 858, 903, true);

        int count = 0;
        for (int index1 = 0; index1 < spans.length; ++index1)
        {
            for (int index2 = index1; index2 < spans.length; ++index2)
            {
                assertEquals("For index1: " + index1 + ", index2: " + index2 + " expected index: " + count, expected[count],
                        spans[index1].formsContiguousRange(spans[index2]));
                assertEquals("For index1: " + index1 + ", index2: " + index2 + " expected index: " + count, expected[count],
                        spans[index2].formsContiguousRange(spans[index1]));
                ++count;
            }
        }
    }

    /**
     * Test for {@link TimeSpan#fromISO8601String(String)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testFromISO8601String() throws ParseException
    {
        TimeSpan actual = TimeSpan.fromISO8601String("2010-03-13T12:35:17Z/2010-03-15T03:59:21Z");
        SimpleDateFormat fmt = new SimpleDateFormat(DateTimeFormats.DATE_TIME_FORMAT);
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        TimeSpan expected = TimeSpan.get(fmt.parse("2010-03-13 12:35:17"), fmt.parse("2010-03-15 03:59:21"));
        assertEquals(expected, actual);
    }

    /**
     * Test for {@link TimeSpan#fromISO8601String(String)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test(expected = ParseException.class)
    public void testFromISO8601StringParseException() throws ParseException
    {
        TimeSpan.fromISO8601String("2010-03-13T12:35:17Z");
    }

    /** Test for {@link TimeSpan#fromLongs(long, long, long, long)}. */
    @Test
    public void testFromLongsLongLongLongLong()
    {
        assertSame(TimeSpan.TIMELESS, TimeSpan.fromLongs(1L, 1L, 1L, 1L));
        assertEquals(TimeSpan.newUnboundedEndTimeSpan(5L), TimeSpan.fromLongs(5L, 10L, 4L, 10L));
        assertEquals(TimeSpan.newUnboundedStartTimeSpan(10L), TimeSpan.fromLongs(5L, 10L, 5L, 9L));
        assertEquals(TimeSpan.get(5L, 10L), TimeSpan.fromLongs(5L, 10L, 4L, 9L));
    }

    /** Test for {@link TimeSpan#getDuration()}. */
    @Test
    public void testGetDuration()
    {
        assertEquals(new Milliseconds(100L), TimeSpan.get(100L, 200L).getDuration());
        assertEquals(Months.ONE, TimeSpan.get(100L, Months.ONE).getDuration());
    }

    /** Test for {@link TimeSpan#getDurationMs()}. */
    @Test
    public void testGetDurationMs()
    {
        assertEquals(100L, TimeSpan.get(100L, 200L).getDurationMs());
    }

    /** Test for {@link TimeSpan#getDurationMs()} with an overflow. */
    @Test(expected = ArithmeticException.class)
    public void testGetDurationMsOverflow()
    {
        TimeSpan.get(Long.MIN_VALUE + 1, Long.MAX_VALUE - 1).getDurationMs();
    }

    /**
     * Test for {@link TimeSpan#getDurationMs()} with an unbounded time span.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDurationMsUnbounded1()
    {
        TimeSpan.TIMELESS.getDurationMs();
    }

    /**
     * Test for {@link TimeSpan#getDurationMs()} with an unbounded time span.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDurationMsUnbounded2()
    {
        TimeSpan.newUnboundedStartTimeSpan(0L).getDurationMs();
    }

    /**
     * Test for {@link TimeSpan#getDurationMs()} with an unbounded time span.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDurationMsUnbounded3()
    {
        TimeSpan.newUnboundedEndTimeSpan(0L).getDurationMs();
    }

    /** Test for {@link TimeSpan#getDuration()} with an unbounded time span. */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDurationUnbounded1()
    {
        TimeSpan.TIMELESS.getDuration();
    }

    /** Test for {@link TimeSpan#getDuration()} with an unbounded time span. */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDurationUnbounded2()
    {
        TimeSpan.newUnboundedStartTimeSpan(0L).getDuration();
    }

    /** Test for {@link TimeSpan#getDuration()} with an unbounded time span. */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDurationUnbounded3()
    {
        TimeSpan.newUnboundedEndTimeSpan(0L).getDuration();
    }

    /** Test for {@link TimeSpan#getEnd()}. */
    @Test
    public void testGetEnd()
    {
        long t1 = 1L;
        long t2 = 2L;
        TimeSpan span = TimeSpan.get(t1, t2);
        assertTrue(span.getEnd() == t2);
    }

    /** Test for {@link TimeSpan#getEndDate()}. */
    @Test
    public void testGetEndDate()
    {
        long t1 = 1L;
        long t2 = 2L;
        TimeSpan span = TimeSpan.get(t1, t2);
        assertTrue(span.getEndDate().getTime() == t2);
    }

    /** Test for {@link TimeSpan#getEndDate(Date)}. */
    @Test
    public void testGetEndDateDate()
    {
        assertEquals(new Date(40L), TimeSpan.TIMELESS.getEndDate(new Date(40L)));
        assertEquals(new Date(40L), TimeSpan.newUnboundedEndTimeSpan(30L).getEndDate(new Date(40L)));
        assertEquals(new Date(30L), TimeSpan.newUnboundedStartTimeSpan(30L).getEndDate(new Date(40L)));
    }

    /** Test for {@link TimeSpan#getEndDate()}. */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetEndDateUnbounded1()
    {
        TimeSpan.TIMELESS.getEndDate();
    }

    /** Test for {@link TimeSpan#getEndDate()}. */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetEndDateUnbounded2()
    {
        TimeSpan.newUnboundedEndTimeSpan(0L).getEndDate();
    }

    /** Test for {@link TimeSpan#getEnd(long)}. */
    @Test
    public void testGetEndLong()
    {
        assertEquals(40L, TimeSpan.TIMELESS.getEnd(40L));
        assertEquals(40L, TimeSpan.newUnboundedEndTimeSpan(30L).getEnd(40L));
        assertEquals(30L, TimeSpan.newUnboundedStartTimeSpan(30L).getEnd(40L));
    }

    /** Test for {@link TimeSpan#getEnd()}. */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetEndUnbounded1()
    {
        TimeSpan.TIMELESS.getEnd();
    }

    /** Test for {@link TimeSpan#getEnd()}. */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetEndUnbounded2()
    {
        TimeSpan.newUnboundedEndTimeSpan(0L).getEnd();
    }

    /** Test for {@link TimeSpan#getGapBetween(TimeSpan)}. */
    @Test
    public void testGetGapBetween()
    {
        Milliseconds zero = Milliseconds.ZERO;
        Milliseconds hundred = new Milliseconds(100L);

        assertEquals(0, zero.compareTo(TimeSpan.TIMELESS.getGapBetween(TimeSpan.TIMELESS)));
        assertEquals(0, zero.compareTo(TimeSpan.ZERO.getGapBetween(TimeSpan.ZERO)));

        assertEquals(0, hundred.compareTo(TimeSpan.ZERO.getGapBetween(TimeSpan.get(100L, 400L))));
        assertEquals(0, hundred.compareTo(TimeSpan.ZERO.getGapBetween(TimeSpan.get(-400L, -100L))));
        assertEquals(0, zero.compareTo(TimeSpan.TIMELESS.getGapBetween(TimeSpan.get(300L, 400L))));
        assertEquals(0, hundred.compareTo(TimeSpan.get(100L, 200L).getGapBetween(TimeSpan.get(300L, 400L))));
        assertEquals(0, zero.compareTo(TimeSpan.get(300L, 300L).getGapBetween(TimeSpan.get(300L, 400L))));
        assertEquals(0, zero.compareTo(TimeSpan.get(100L, 300L).getGapBetween(TimeSpan.get(300L, 400L))));
        assertEquals(0, zero.compareTo(TimeSpan.get(100L, 400L).getGapBetween(TimeSpan.get(300L, 400L))));
        assertEquals(0, zero.compareTo(TimeSpan.newUnboundedEndTimeSpan(100L).getGapBetween(TimeSpan.get(300L, 400L))));
        assertEquals(0, hundred.compareTo(TimeSpan.newUnboundedEndTimeSpan(300L).getGapBetween(TimeSpan.get(100L, 200L))));
        assertEquals(0,
                zero.compareTo(TimeSpan.newUnboundedEndTimeSpan(300L).getGapBetween(TimeSpan.newUnboundedEndTimeSpan(200L))));
        assertEquals(0, hundred.compareTo(TimeSpan.newUnboundedStartTimeSpan(200L).getGapBetween(TimeSpan.get(300L, 400L))));
        assertEquals(0, zero.compareTo(TimeSpan.newUnboundedStartTimeSpan(200L).getGapBetween(TimeSpan.get(200L, 400L))));
        assertEquals(0,
                zero.compareTo(TimeSpan.newUnboundedStartTimeSpan(200L).getGapBetween(TimeSpan.newUnboundedStartTimeSpan(300L))));

        assertEquals(0, hundred.compareTo(TimeSpan.get(100L, 400L).getGapBetween(TimeSpan.ZERO)));
        assertEquals(0, hundred.compareTo(TimeSpan.get(-400L, -100L).getGapBetween(TimeSpan.ZERO)));
        assertEquals(0, zero.compareTo(TimeSpan.get(300L, 400L).getGapBetween(TimeSpan.TIMELESS)));
        assertEquals(0, hundred.compareTo(TimeSpan.get(300L, 400L).getGapBetween(TimeSpan.get(100L, 200L))));
        assertEquals(0, zero.compareTo(TimeSpan.get(300L, 400L).getGapBetween(TimeSpan.get(300L, 300L))));
        assertEquals(0, zero.compareTo(TimeSpan.get(300L, 400L).getGapBetween(TimeSpan.get(100L, 300L))));
        assertEquals(0, zero.compareTo(TimeSpan.get(300L, 400L).getGapBetween(TimeSpan.get(100L, 400L))));
        assertEquals(0, zero.compareTo(TimeSpan.get(300L, 400L).getGapBetween(TimeSpan.newUnboundedEndTimeSpan(100L))));
        assertEquals(0, hundred.compareTo(TimeSpan.get(100L, 200L).getGapBetween(TimeSpan.newUnboundedEndTimeSpan(300L))));
        assertEquals(0,
                zero.compareTo(TimeSpan.newUnboundedEndTimeSpan(200L).getGapBetween(TimeSpan.newUnboundedEndTimeSpan(300L))));
        assertEquals(0, hundred.compareTo(TimeSpan.get(300L, 400L).getGapBetween(TimeSpan.newUnboundedStartTimeSpan(200L))));
        assertEquals(0, zero.compareTo(TimeSpan.get(200L, 400L).getGapBetween(TimeSpan.newUnboundedStartTimeSpan(200L))));
        assertEquals(0,
                zero.compareTo(TimeSpan.newUnboundedStartTimeSpan(300L).getGapBetween(TimeSpan.newUnboundedStartTimeSpan(200L))));
    }

    /** Test for {@link TimeSpan#getIntersection(TimeSpan)}. */
    @Test
    public void testGetIntersection()
    {
        TimeSpan ts100to200 = TimeSpan.get(100L, 200L);
        TimeSpan ts50to150 = TimeSpan.get(50L, 150L);
        TimeSpan ts150to250 = TimeSpan.get(150L, 250L);
        TimeSpan ts50to250 = TimeSpan.get(50L, 250L);
        TimeSpan ts50to200 = TimeSpan.get(50L, 200L);
        TimeSpan ts100250 = TimeSpan.get(100L, 250L);
        TimeSpan tsto100 = TimeSpan.newUnboundedStartTimeSpan(100L);
        TimeSpan tsto150 = TimeSpan.newUnboundedStartTimeSpan(150L);
        TimeSpan tsto200 = TimeSpan.newUnboundedStartTimeSpan(200L);
        TimeSpan ts100to = TimeSpan.newUnboundedEndTimeSpan(100L);
        TimeSpan ts150to = TimeSpan.newUnboundedEndTimeSpan(150L);
        TimeSpan ts200to = TimeSpan.newUnboundedEndTimeSpan(200L);

        TimeSpan expected;

        expected = TimeSpan.get(100L, 150L);
        assertEquals(expected, ts100to200.getIntersection(ts50to150));
        assertEquals(expected, ts50to150.getIntersection(ts100to200));

        expected = TimeSpan.get(150L, 200L);
        assertEquals(expected, ts100to200.getIntersection(ts150to250));
        assertEquals(expected, ts150to250.getIntersection(ts100to200));

        assertNull(ts50to150.getIntersection(ts150to250));
        assertNull(ts150to250.getIntersection(ts50to150));

        expected = ts100to200;
        assertSame(expected, ts100to200.getIntersection(ts50to250));
        assertSame(expected, ts50to250.getIntersection(ts100to200));

        expected = ts100to200;
        assertSame(expected, ts100to200.getIntersection(ts50to200));
        assertSame(expected, ts50to200.getIntersection(ts100to200));

        expected = ts100to200;
        assertEquals(expected, ts100to200.getIntersection(ts100250));
        assertSame(expected, ts100250.getIntersection(ts100to200));

        expected = ts100to200;
        assertSame(expected, ts100to200.getIntersection(TimeSpan.TIMELESS));
        assertSame(expected, TimeSpan.TIMELESS.getIntersection(ts100to200));

        expected = ts100to200;
        assertSame(expected, ts100to200.getIntersection(tsto200));
        assertSame(expected, tsto200.getIntersection(ts100to200));

        expected = TimeSpan.get(100L, 150L);
        assertEquals(expected, ts100to200.getIntersection(tsto150));
        assertEquals(expected, tsto150.getIntersection(ts100to200));

        assertNull(ts100to200.getIntersection(tsto100));
        assertNull(tsto100.getIntersection(ts100to200));

        expected = ts100to200;
        assertSame(expected, ts100to200.getIntersection(ts100to));
        assertSame(expected, ts100to.getIntersection(ts100to200));

        expected = TimeSpan.get(150L, 200L);
        assertEquals(expected, ts100to200.getIntersection(ts150to));
        assertEquals(expected, ts150to.getIntersection(ts100to200));

        assertNull(ts100to200.getIntersection(ts200to));
        assertNull(ts200to.getIntersection(ts100to200));
    }

    /** Test for {@link TimeSpan#getMidpoint()}. */
    @Test
    public void testGetMidpoint()
    {
        assertEquals(15L, TimeSpan.get(10L, 20L).getMidpoint());
        assertEquals(11L, TimeSpan.get(11L, 11L).getMidpoint());
        assertEquals(12L, TimeSpan.get(11L, 13L).getMidpoint());
        assertEquals(Long.MAX_VALUE - 100L, TimeSpan.get(Long.MAX_VALUE - 101L, Long.MAX_VALUE - 99L).getMidpoint());
    }

    /** Test for {@link TimeSpan#getMidpointDate()}. */
    @Test
    public void testGetMidpointDate()
    {
        long t1 = 10L;
        long t2 = 20L;
        TimeSpan span = TimeSpan.get(t1, t2);
        assertTrue(span.getMidpointDate().getTime() == 15L);
    }

    /**
     * Test for {@link TimeSpan#getMidpointDate()} with an unbounded time span.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMidpointDateUnbounded1()
    {
        TimeSpan.TIMELESS.getMidpointDate();
    }

    /**
     * Test for {@link TimeSpan#getMidpointDate()} with an unbounded time span.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMidpointDateUnbounded2()
    {
        TimeSpan.newUnboundedEndTimeSpan(0L).getMidpointDate();
    }

    /**
     * Test for {@link TimeSpan#getMidpointDate()} with an unbounded time span.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMidpointDateUnbounded3()
    {
        TimeSpan.newUnboundedStartTimeSpan(0L).getMidpointDate();
    }

    /** Test for {@link TimeSpan#getMidpoint()} with an unbounded time span. */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMidpointUnbounded1()
    {
        TimeSpan.TIMELESS.getMidpoint();
    }

    /** Test for {@link TimeSpan#getMidpoint()} with an unbounded time span. */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMidpointUnbounded2()
    {
        TimeSpan.newUnboundedEndTimeSpan(0L).getMidpoint();
    }

    /** Test for {@link TimeSpan#getMidpoint()} with an unbounded time span. */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMidpointUnbounded3()
    {
        TimeSpan.newUnboundedStartTimeSpan(0L).getMidpoint();
    }

    /** Test for {@link TimeSpan#getRelation(TimeSpan)}. */
    @Test
    public void testGetRelation()
    {
        assertEquals(RangeRelationType.EQUAL, TimeSpan.get(100L, 200L).getRelation(TimeSpan.get(100L, 200L)));
        assertEquals(RangeRelationType.EQUAL,
                TimeSpan.newUnboundedStartTimeSpan(100L).getRelation(TimeSpan.newUnboundedStartTimeSpan(100L)));
        assertEquals(RangeRelationType.EQUAL,
                TimeSpan.newUnboundedEndTimeSpan(100L).getRelation(TimeSpan.newUnboundedEndTimeSpan(100L)));
        assertEquals(RangeRelationType.EQUAL, TimeSpan.TIMELESS.getRelation(TimeSpan.TIMELESS));
        assertEquals(RangeRelationType.SUPERSET, TimeSpan.TIMELESS.getRelation(TimeSpan.get(100L, 200L)));
        assertEquals(RangeRelationType.SUPERSET, TimeSpan.newUnboundedStartTimeSpan(201L).getRelation(TimeSpan.get(100L, 200L)));
        assertEquals(RangeRelationType.SUPERSET, TimeSpan.newUnboundedEndTimeSpan(99L).getRelation(TimeSpan.get(100L, 200L)));
        assertEquals(RangeRelationType.SUBSET, TimeSpan.get(100L, 200L).getRelation(TimeSpan.TIMELESS));
        assertEquals(RangeRelationType.SUBSET, TimeSpan.get(100L, 200L).getRelation(TimeSpan.newUnboundedStartTimeSpan(201L)));
        assertEquals(RangeRelationType.SUBSET, TimeSpan.get(100L, 200L).getRelation(TimeSpan.newUnboundedEndTimeSpan(99L)));
        assertEquals(RangeRelationType.BEFORE, TimeSpan.get(100L, 199L).getRelation(TimeSpan.get(200L, 300L)));
        assertEquals(RangeRelationType.BEFORE, TimeSpan.newUnboundedStartTimeSpan(199L).getRelation(TimeSpan.get(200L, 300L)));
        assertEquals(RangeRelationType.BORDERS_BEFORE, TimeSpan.get(100L, 200L).getRelation(TimeSpan.get(200L, 300L)));
        assertEquals(RangeRelationType.BORDERS_BEFORE,
                TimeSpan.newUnboundedStartTimeSpan(200L).getRelation(TimeSpan.get(200L, 300L)));
        assertEquals(RangeRelationType.AFTER, TimeSpan.get(200L, 300L).getRelation(TimeSpan.get(100L, 199L)));
        assertEquals(RangeRelationType.AFTER, TimeSpan.newUnboundedEndTimeSpan(201L).getRelation(TimeSpan.get(100L, 200L)));
        assertEquals(RangeRelationType.BORDERS_AFTER, TimeSpan.get(200L, 300L).getRelation(TimeSpan.get(100L, 200L)));
        assertEquals(RangeRelationType.BORDERS_AFTER,
                TimeSpan.newUnboundedEndTimeSpan(200L).getRelation(TimeSpan.get(100L, 200L)));
        assertEquals(RangeRelationType.OVERLAPS_BACK_EDGE, TimeSpan.get(199L, 201L).getRelation(TimeSpan.get(100L, 200L)));
        assertEquals(RangeRelationType.OVERLAPS_BACK_EDGE,
                TimeSpan.newUnboundedEndTimeSpan(199L).getRelation(TimeSpan.get(100L, 200L)));
        assertEquals(RangeRelationType.OVERLAPS_FRONT_EDGE, TimeSpan.get(99L, 101L).getRelation(TimeSpan.get(100L, 200L)));
        assertEquals(RangeRelationType.OVERLAPS_FRONT_EDGE,
                TimeSpan.newUnboundedStartTimeSpan(101L).getRelation(TimeSpan.get(100L, 200L)));
    }

    /** Test for {@link TimeSpan#getStart()}. */
    @Test
    public void testGetStart()
    {
        long t1 = 1L;
        long t2 = 2L;
        TimeSpan span = TimeSpan.get(t1, t2);
        assertTrue(span.getStart() == t1);
    }

    /** Test for {@link TimeSpan#getStartDate()}. */
    @Test
    public void testGetStartDate()
    {
        long t1 = 1L;
        long t2 = 2L;
        TimeSpan span = TimeSpan.get(t1, t2);
        assertTrue(span.getStartDate().getTime() == t1);
    }

    /** Test for {@link TimeSpan#getStartDate(Date)}. */
    @Test
    public void testGetStartDateDate()
    {
        assertEquals(new Date(40L), TimeSpan.TIMELESS.getStartDate(new Date(40L)));
        assertEquals(new Date(30L), TimeSpan.newUnboundedEndTimeSpan(30L).getStartDate(new Date(40L)));
        assertEquals(new Date(40L), TimeSpan.newUnboundedStartTimeSpan(30L).getStartDate(new Date(40L)));
    }

    /** Test for {@link TimeSpan#getStartDate()}. */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetStartDateUnbounded1()
    {
        TimeSpan.TIMELESS.getStartDate();
    }

    /** Test for {@link TimeSpan#getStartDate()}. */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetStartDateUnbounded2()
    {
        TimeSpan.newUnboundedStartTimeSpan(0L).getStartDate();
    }

    /** Test for {@link TimeSpan#getStart(long)}. */
    @Test
    public void testGetStartLong()
    {
        assertEquals(40L, TimeSpan.TIMELESS.getStart(40L));
        assertEquals(30L, TimeSpan.newUnboundedEndTimeSpan(30L).getStart(40L));
        assertEquals(40L, TimeSpan.newUnboundedStartTimeSpan(30L).getStart(40L));
    }

    /** Test for {@link TimeSpan#getStart()}. */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetStartUnbounded1()
    {
        TimeSpan.TIMELESS.getStart();
    }

    /** Test for {@link TimeSpan#getStart()}. */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetStartUnbounded2()
    {
        TimeSpan.newUnboundedStartTimeSpan(0L).getStart();
    }

    /** Test for {@link TimeSpan#hashCode()}. */
    @Test
    public void testHashCode()
    {
        TimeSpan one = TimeSpan.get(100L, 200L);
        TimeSpan sameAsOne = TimeSpan.get(100L, 200L);
        TimeSpan two = TimeSpan.get(100L, 201L);
        TimeSpan three = TimeSpan.get(99L, 200L);

        assertTrue(one.hashCode() == sameAsOne.hashCode());
        assertFalse(one.hashCode() == two.hashCode());
        assertFalse(one.hashCode() == three.hashCode());
    }

    /** Test getting time span with illegal arguments. */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgument1()
    {
        TimeSpan.get(Long.MIN_VALUE, new Milliseconds(Long.MIN_VALUE));
    }

    /** Test getting time span with illegal arguments. */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgument2()
    {
        TimeSpan.get(Long.MAX_VALUE, Milliseconds.ONE);
    }

    /** Test getting time span with illegal arguments. */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgument3()
    {
        TimeSpan.get(1L, new Milliseconds(Long.MAX_VALUE));
    }

    /** Test getting time span with illegal arguments. */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgument4()
    {
        TimeSpan.get(new Milliseconds(Long.MIN_VALUE), Long.MAX_VALUE);
    }

    /** Test getting time span with illegal arguments. */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgument5()
    {
        TimeSpan.get(Milliseconds.ONE, Long.MIN_VALUE);
    }

    /** Test getting time span with illegal arguments. */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgument6()
    {
        TimeSpan.get(new Milliseconds(Long.MAX_VALUE), -2L);
    }

    /** Test that the fields in the class are final. */
    @Test
    public void testImmuatable()
    {
        Field[] declaredFields = TimeSpan.class.getDeclaredFields();
        for (Field field : declaredFields)
        {
            assertTrue(Modifier.isFinal(field.getModifiers()));
        }
    }

    /** Test for {@link TimeSpan#interpolate(TimeSpan, double)}. */
    @Test
    public void testInterpolate()
    {
        assertEquals(TimeSpan.get(100L, 200L), TimeSpan.get(100L, 200L).interpolate(TimeSpan.get(200L, 300L), 0.));
        assertEquals(TimeSpan.get(200L, 300L), TimeSpan.get(100L, 200L).interpolate(TimeSpan.get(200L, 300L), 1.));
        assertEquals(TimeSpan.get(125L, 225L), TimeSpan.get(100L, 200L).interpolate(TimeSpan.get(200L, 300L), .25));
        assertEquals(TimeSpan.get(175L, 275L), TimeSpan.get(100L, 200L).interpolate(TimeSpan.get(200L, 300L), .75));

        assertEquals(TimeSpan.get(200L, 300L), TimeSpan.get(200L, 300L).interpolate(TimeSpan.get(100L, 200L), 0.));
        assertEquals(TimeSpan.get(100L, 200L), TimeSpan.get(200L, 300L).interpolate(TimeSpan.get(100L, 200L), 1.));
        assertEquals(TimeSpan.get(175L, 275L), TimeSpan.get(200L, 300L).interpolate(TimeSpan.get(100L, 200L), .25));
        assertEquals(TimeSpan.get(125L, 225L), TimeSpan.get(200L, 300L).interpolate(TimeSpan.get(100L, 200L), .75));

        assertEquals(TimeSpan.get(150L), TimeSpan.get(100L).interpolate(TimeSpan.get(200L), 0.5));

        assertEquals(TimeSpan.TIMELESS, TimeSpan.TIMELESS.interpolate(TimeSpan.TIMELESS, 0.));
    }

    /** Test for {@link TimeSpan#interpolate(TimeSpan, double)}. */
    @Test(expected = IllegalArgumentException.class)
    public void testInterpolateIllegalUnbounded1()
    {
        TimeSpan.get(100L, 200L).interpolate(TimeSpan.TIMELESS, 0.);
    }

    /** Test for {@link TimeSpan#interpolate(TimeSpan, double)}. */
    @Test(expected = IllegalArgumentException.class)
    public void testInterpolateIllegalUnbounded2()
    {
        TimeSpan.TIMELESS.interpolate(TimeSpan.get(100L, 200L), 1.);
    }

    /** Test for {@link TimeSpan#interpolate(TimeSpan, double)}. */
    @Test(expected = IllegalArgumentException.class)
    public void testInterpolateIllegalUnbounded3()
    {
        TimeSpan.get(100L, 200L).interpolate(TimeSpan.newUnboundedEndTimeSpan(0L), 0.);
    }

    /** Test for {@link TimeSpan#interpolate(TimeSpan, double)}. */
    @Test(expected = IllegalArgumentException.class)
    public void testInterpolateIllegalUnbounded4()
    {
        TimeSpan.newUnboundedEndTimeSpan(0L).interpolate(TimeSpan.get(100L, 200L), 1.);
    }

    /** Test for {@link TimeSpan#interpolate(TimeSpan, double)}. */
    @Test(expected = IllegalArgumentException.class)
    public void testInterpolateIllegalUnbounded5()
    {
        TimeSpan.get(100L, 200L).interpolate(TimeSpan.newUnboundedStartTimeSpan(0L), 0.);
    }

    /** Test for {@link TimeSpan#interpolate(TimeSpan, double)}. */
    @Test(expected = IllegalArgumentException.class)
    public void testInterpolateIllegalUnbounded6()
    {
        TimeSpan.newUnboundedStartTimeSpan(0L).interpolate(TimeSpan.get(100L, 200L), 1.);
    }

    /** Test for {@link TimeSpan#interpolate(TimeSpan, double)}. */
    @Test
    public void testInterpolateUnbounded()
    {
        assertEquals(TimeSpan.newUnboundedEndTimeSpan(100L),
                TimeSpan.newUnboundedEndTimeSpan(100L).interpolate(TimeSpan.newUnboundedEndTimeSpan(200L), 0.));
        assertEquals(TimeSpan.newUnboundedEndTimeSpan(200L),
                TimeSpan.newUnboundedEndTimeSpan(100L).interpolate(TimeSpan.newUnboundedEndTimeSpan(200L), 1.));
        assertEquals(TimeSpan.newUnboundedEndTimeSpan(125L),
                TimeSpan.newUnboundedEndTimeSpan(100L).interpolate(TimeSpan.newUnboundedEndTimeSpan(200L), .25));
        assertEquals(TimeSpan.newUnboundedEndTimeSpan(175L),
                TimeSpan.newUnboundedEndTimeSpan(100L).interpolate(TimeSpan.newUnboundedEndTimeSpan(200L), .75));

        assertEquals(TimeSpan.newUnboundedEndTimeSpan(200L),
                TimeSpan.newUnboundedEndTimeSpan(200L).interpolate(TimeSpan.newUnboundedEndTimeSpan(100L), 0.));
        assertEquals(TimeSpan.newUnboundedEndTimeSpan(100L),
                TimeSpan.newUnboundedEndTimeSpan(200L).interpolate(TimeSpan.newUnboundedEndTimeSpan(100L), 1.));
        assertEquals(TimeSpan.newUnboundedEndTimeSpan(175L),
                TimeSpan.newUnboundedEndTimeSpan(200L).interpolate(TimeSpan.newUnboundedEndTimeSpan(100L), .25));
        assertEquals(TimeSpan.newUnboundedEndTimeSpan(125L),
                TimeSpan.newUnboundedEndTimeSpan(200L).interpolate(TimeSpan.newUnboundedEndTimeSpan(100L), .75));

        assertEquals(TimeSpan.newUnboundedStartTimeSpan(100L),
                TimeSpan.newUnboundedStartTimeSpan(100L).interpolate(TimeSpan.newUnboundedStartTimeSpan(200L), 0.));
        assertEquals(TimeSpan.newUnboundedStartTimeSpan(200L),
                TimeSpan.newUnboundedStartTimeSpan(100L).interpolate(TimeSpan.newUnboundedStartTimeSpan(200L), 1.));
        assertEquals(TimeSpan.newUnboundedStartTimeSpan(125L),
                TimeSpan.newUnboundedStartTimeSpan(100L).interpolate(TimeSpan.newUnboundedStartTimeSpan(200L), .25));
        assertEquals(TimeSpan.newUnboundedStartTimeSpan(175L),
                TimeSpan.newUnboundedStartTimeSpan(100L).interpolate(TimeSpan.newUnboundedStartTimeSpan(200L), .75));

        assertEquals(TimeSpan.newUnboundedStartTimeSpan(200L),
                TimeSpan.newUnboundedStartTimeSpan(200L).interpolate(TimeSpan.newUnboundedStartTimeSpan(100L), 0.));
        assertEquals(TimeSpan.newUnboundedStartTimeSpan(100L),
                TimeSpan.newUnboundedStartTimeSpan(200L).interpolate(TimeSpan.newUnboundedStartTimeSpan(100L), 1.));
        assertEquals(TimeSpan.newUnboundedStartTimeSpan(175L),
                TimeSpan.newUnboundedStartTimeSpan(200L).interpolate(TimeSpan.newUnboundedStartTimeSpan(100L), .25));
        assertEquals(TimeSpan.newUnboundedStartTimeSpan(125L),
                TimeSpan.newUnboundedStartTimeSpan(200L).interpolate(TimeSpan.newUnboundedStartTimeSpan(100L), .75));
    }

    /** Test getting time spans with large arguments. */
    @Test
    public void testLargeArguments1()
    {
        TimeSpan ts1 = TimeSpan.get(Long.MIN_VALUE, Seconds.ZERO);
        assertEquals(Long.MIN_VALUE, ts1.getStart());
        assertEquals(Long.MIN_VALUE, ts1.getEnd());
        TimeSpan ts2 = TimeSpan.get(Long.MIN_VALUE, new Milliseconds(Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, ts2.getStart());
        assertEquals(-1L, ts2.getEnd());
        TimeSpan ts3 = TimeSpan.get(0L, new Milliseconds(Long.MAX_VALUE));
        assertEquals(0L, ts3.getStart());
        assertEquals(Long.MAX_VALUE, ts3.getEnd());
        TimeSpan ts4 = TimeSpan.get(Long.MAX_VALUE, Seconds.ZERO);
        assertEquals(Long.MAX_VALUE, ts4.getStart());
        assertEquals(Long.MAX_VALUE, ts4.getEnd());
    }

    /** Test getting time spans with large arguments. */
    @Test
    public void testLargeArguments2()
    {
        TimeSpan ts1 = TimeSpan.get(Seconds.ZERO, Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, ts1.getStart());
        assertEquals(Long.MIN_VALUE, ts1.getEnd());
        TimeSpan ts2 = TimeSpan.get(new Milliseconds(Long.MAX_VALUE), Long.MAX_VALUE);
        assertEquals(0L, ts2.getStart());
        assertEquals(Long.MAX_VALUE, ts2.getEnd());
        TimeSpan ts3 = TimeSpan.get(new Milliseconds(Long.MAX_VALUE), -1L);
        assertEquals(-1L, ts3.getEnd());
        assertEquals(Long.MIN_VALUE, ts3.getStart());
        TimeSpan ts4 = TimeSpan.get(Seconds.ZERO, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, ts4.getStart());
        assertEquals(Long.MAX_VALUE, ts4.getEnd());
    }

    /** Test for {@link TimeSpan#get(Date, Date)}. */
    @Test
    public void testNewTimeSpan()
    {
        assertSame(TimeSpan.TIMELESS, TimeSpan.get((Date)null, (Date)null));
        assertEquals(TimeSpan.newUnboundedEndTimeSpan(5L), TimeSpan.get(new Date(5L), (Date)null));
        assertEquals(TimeSpan.newUnboundedStartTimeSpan(10L), TimeSpan.get((Date)null, new Date(10L)));
        assertEquals(TimeSpan.get(5L, 10L), TimeSpan.get(new Date(5L), new Date(10L)));
    }

    /** Test for {@link TimeSpan#overlaps(Date)}. */
    @Test
    public void testOverlapsDate()
    {
        TimeSpan span = TimeSpan.get(100L, 200L);

        assertFalse(span.overlaps(new Date(99L)));
        assertTrue(span.overlaps(new Date(100L)));
        assertTrue(span.overlaps(new Date(101L)));

        assertTrue(span.overlaps(new Date(199L)));
        assertFalse(span.overlaps(new Date(200L)));
        assertFalse(span.overlaps(new Date(201L)));

        span = TimeSpan.TIMELESS;

        assertTrue(span.overlaps(new Date(99L)));
        assertTrue(span.overlaps(new Date(100L)));
        assertTrue(span.overlaps(new Date(101L)));

        assertTrue(span.overlaps(new Date(199L)));
        assertTrue(span.overlaps(new Date(200L)));
        assertTrue(span.overlaps(new Date(201L)));
    }

    /** Test for {@link TimeSpan#overlaps(long)}. */
    @Test
    public void testOverlapsLong()
    {
        TimeSpan span = TimeSpan.get(100L, 200L);

        assertFalse(span.overlaps(99L));
        assertTrue(span.overlaps(100L));
        assertTrue(span.overlaps(101L));

        assertTrue(span.overlaps(199L));
        assertFalse(span.overlaps(200L));
        assertFalse(span.overlaps(201L));

        span = TimeSpan.TIMELESS;

        assertTrue(span.overlaps(99L));
        assertTrue(span.overlaps(100L));
        assertTrue(span.overlaps(101L));

        assertTrue(span.overlaps(199L));
        assertTrue(span.overlaps(200L));
        assertTrue(span.overlaps(201L));
    }

    /** Test for {@link TimeSpan#overlaps(TimeSpan)}. */
    @Test
    public void testOverlapsTimeSpan()
    {
        TimeSpan span = TimeSpan.get(100L, 200L);

        assertFalse(span.overlaps(TimeSpan.get(50L, 50L)));
        assertFalse(span.overlaps(TimeSpan.get(50L, 99L)));
        assertFalse(span.overlaps(TimeSpan.get(50L, 100L)));
        assertTrue(span.overlaps(TimeSpan.get(50L, 101L)));
        assertTrue(span.overlaps(TimeSpan.get(50L, 199L)));
        assertTrue(span.overlaps(TimeSpan.get(50L, 200L)));
        assertTrue(span.overlaps(TimeSpan.get(50L, 201L)));
        assertTrue(span.overlaps(TimeSpan.get(50L, 250L)));

        assertFalse(span.overlaps(TimeSpan.get(99L, 99L)));
        assertFalse(span.overlaps(TimeSpan.get(99L, 100L)));
        assertTrue(span.overlaps(TimeSpan.get(99L, 101L)));
        assertTrue(span.overlaps(TimeSpan.get(99L, 199L)));
        assertTrue(span.overlaps(TimeSpan.get(99L, 200L)));
        assertTrue(span.overlaps(TimeSpan.get(99L, 201L)));
        assertTrue(span.overlaps(TimeSpan.get(99L, 250L)));

        assertTrue(span.overlaps(TimeSpan.get(100L, 100L)));
        assertTrue(span.overlaps(TimeSpan.get(100L, 101L)));
        assertTrue(span.overlaps(TimeSpan.get(100L, 199L)));
        assertTrue(span.overlaps(TimeSpan.get(100L, 200L)));
        assertTrue(span.overlaps(TimeSpan.get(100L, 201L)));
        assertTrue(span.overlaps(TimeSpan.get(100L, 250L)));

        assertTrue(span.overlaps(TimeSpan.get(101L, 101L)));
        assertTrue(span.overlaps(TimeSpan.get(101L, 199L)));
        assertTrue(span.overlaps(TimeSpan.get(101L, 200L)));
        assertTrue(span.overlaps(TimeSpan.get(101L, 201L)));
        assertTrue(span.overlaps(TimeSpan.get(101L, 250L)));

        assertTrue(span.overlaps(TimeSpan.get(199L, 199L)));
        assertTrue(span.overlaps(TimeSpan.get(199L, 200L)));
        assertTrue(span.overlaps(TimeSpan.get(199L, 201L)));
        assertTrue(span.overlaps(TimeSpan.get(199L, 250L)));

        assertFalse(span.overlaps(TimeSpan.get(200L, 200L)));
        assertFalse(span.overlaps(TimeSpan.get(200L, 201L)));
        assertFalse(span.overlaps(TimeSpan.get(200L, 250L)));

        assertFalse(span.overlaps(TimeSpan.get(201L, 201L)));
        assertFalse(span.overlaps(TimeSpan.get(201L, 250L)));

        assertFalse(span.overlaps(TimeSpan.get(250L, 250L)));

        assertTrue(span.overlaps(TimeSpan.TIMELESS));
        assertTrue(TimeSpan.TIMELESS.overlaps(span));
    }

    /**
     * Test for {@link TimeSpan#overlaps(TimeSpan)} with an instantaneous time
     * span.
     */
    @Test
    public void testOverlapsTimeSpanInstant()
    {
        TimeSpan span = TimeSpan.get(100L, 100L);

        assertFalse(span.overlaps(TimeSpan.get(50L, 50L)));
        assertFalse(span.overlaps(TimeSpan.get(50L, 99L)));
        assertFalse(span.overlaps(TimeSpan.get(50L, 100L)));
        assertTrue(span.overlaps(TimeSpan.get(50L, 101L)));
        assertTrue(span.overlaps(TimeSpan.get(50L, 200L)));

        assertFalse(span.overlaps(TimeSpan.get(99L, 99L)));
        assertFalse(span.overlaps(TimeSpan.get(99L, 100L)));
        assertTrue(span.overlaps(TimeSpan.get(99L, 101L)));
        assertTrue(span.overlaps(TimeSpan.get(99L, 200L)));

        assertTrue(span.overlaps(TimeSpan.get(100L, 100L)));
        assertTrue(span.overlaps(TimeSpan.get(100L, 101L)));
        assertTrue(span.overlaps(TimeSpan.get(100L, 200L)));

        assertFalse(span.overlaps(TimeSpan.get(101L, 101L)));
        assertFalse(span.overlaps(TimeSpan.get(101L, 199L)));
        assertFalse(span.overlaps(TimeSpan.get(101L, 200L)));

        assertTrue(span.overlaps(TimeSpan.TIMELESS));
        assertTrue(TimeSpan.TIMELESS.overlaps(span));
    }

    /** Test for {@link TimeSpan#plus(Duration)}. */
    @Test
    public void testPlusZeroDuration()
    {
        TimeSpan span = TimeSpan.get(0, 10);

        assertEquals(span, span.plus(new Milliseconds(0)));
    }

    /** Test for {@link TimeSpan#plus(Duration)}. */
    @Test
    public void testPlus()
    {
        assertEquals(TimeSpan.get(150, 250), TimeSpan.get(100, 200).plus(new Milliseconds(50)));
        assertEquals(TimeSpan.get(50, 150), TimeSpan.get(100, 200).plus(new Milliseconds(-50)));

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2013, 3, 1);
        long start1 = cal.getTimeInMillis();

        cal.set(2013, 3, 2);
        long end1 = cal.getTimeInMillis();

        cal.set(2013, 4, 1);
        long start2 = cal.getTimeInMillis();

        cal.set(2013, 4, 2);
        long end2 = cal.getTimeInMillis();

        assertEquals(TimeSpan.get(start2, end2), TimeSpan.get(start1, end1).plus(Months.ONE));
    }

    /** Test for {@link TimeSpan#minus(Duration)}. */
    @Test
    public void testMinusZeroDuration()
    {
        TimeSpan span = TimeSpan.get(0, 10);

        assertEquals(span, span.minus(new Milliseconds(0)));
    }

    /** Test for {@link TimeSpan#minus(Duration)}. */
    @Test
    public void testMinus()
    {
        assertEquals(TimeSpan.get(50, 150), TimeSpan.get(100, 200).minus(new Milliseconds(50)));
        assertEquals(TimeSpan.get(150, 250), TimeSpan.get(100, 200).minus(new Milliseconds(-50)));

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2013, 3, 1);
        long start1 = cal.getTimeInMillis();

        cal.set(2013, 3, 2);
        long end1 = cal.getTimeInMillis();

        cal.set(2013, 4, 1);
        long start2 = cal.getTimeInMillis();

        cal.set(2013, 4, 2);
        long end2 = cal.getTimeInMillis();

        assertEquals(TimeSpan.get(start1, end1), TimeSpan.get(start2, end2).minus(Months.ONE));
    }

    /** Test for {@link TimeSpan#simpleUnion(TimeSpan)}. */
    @Test
    public void testSimpleUnion()
    {
        assertEquals(TimeSpan.get(1L, 3L), TimeSpan.get(1L, 2L).simpleUnion(TimeSpan.get(2L, 3L)));
        assertEquals(TimeSpan.get(1L, 5L), TimeSpan.get(1L, 4L).simpleUnion(TimeSpan.get(2L, 5L)));
        assertEquals(TimeSpan.get(1L, 5L), TimeSpan.get(1L, 5L).simpleUnion(TimeSpan.get(1L, 5L)));
        assertEquals(TimeSpan.get(1L, 5L), TimeSpan.get(1L, 5L).simpleUnion(TimeSpan.get(1L, 3L)));
        assertEquals(TimeSpan.get(1L, 5L), TimeSpan.get(1L, 5L).simpleUnion(TimeSpan.get(3L, 5L)));
        assertEquals(TimeSpan.get(1L, 5L), TimeSpan.get(1L, 3L).simpleUnion(TimeSpan.get(1L, 5L)));
        assertEquals(TimeSpan.get(1L, 5L), TimeSpan.get(3L, 5L).simpleUnion(TimeSpan.get(1L, 5L)));
        assertEquals(TimeSpan.get(1L, 5L), TimeSpan.get(1L, 2L).simpleUnion(TimeSpan.get(4L, 5L)));
        assertEquals(TimeSpan.get(1L, 5L), TimeSpan.get(4L, 5L).simpleUnion(TimeSpan.get(1L, 2L)));
        assertEquals(TimeSpan.TIMELESS, TimeSpan.TIMELESS.simpleUnion(TimeSpan.get(1L, 2L)));
        assertEquals(TimeSpan.TIMELESS, TimeSpan.get(1L, 2L).simpleUnion(TimeSpan.TIMELESS));
    }

    /** Test for {@link TimeSpan#subtract(TimeSpan)}. */
    @Test
    public void testSubtract()
    {
        Collection<TimeSpan> expected = Collections.emptyList();

        TimeSpan span = TimeSpan.get(100L, 200L);

        assertEquals(expected, span.subtract(TimeSpan.get(100L, 200L)));
        assertEquals(expected, span.subtract(TimeSpan.TIMELESS));

        expected = Collections.singletonList(TimeSpan.get(100L, 150L));
        assertEquals(expected, span.subtract(TimeSpan.get(150L, 200L)));
        assertEquals(expected, span.subtract(TimeSpan.get(150L, 250L)));

        expected = Collections.singletonList(TimeSpan.get(150L, 200L));
        assertEquals(expected, span.subtract(TimeSpan.get(100L, 150L)));
        assertEquals(expected, span.subtract(TimeSpan.get(50L, 150L)));

        expected = Collections.singletonList(span);
        assertEquals(expected, span.subtract(TimeSpan.get(50L, 99L)));
        assertEquals(expected, span.subtract(TimeSpan.get(50L, 100L)));
        assertEquals(expected, span.subtract(TimeSpan.get(200L, 250L)));
        assertEquals(expected, span.subtract(TimeSpan.get(201L, 250L)));
        assertEquals(expected, span.subtract(TimeSpan.get(99L, 99L)));
        assertEquals(expected, span.subtract(TimeSpan.get(100L, 100L)));
        assertEquals(expected, span.subtract(TimeSpan.get(200L, 200L)));
        assertEquals(expected, span.subtract(TimeSpan.get(201L, 201L)));

        expected = Arrays.asList(TimeSpan.get(100L, 125L), TimeSpan.get(175L, 200L));
        assertEquals(expected, span.subtract(TimeSpan.get(125L, 175L)));
    }

    /** Test for {@link TimeSpan#subtract(Collection)}. */
    @Test
    public void testSubtractCollection()
    {
        TimeSpan span = TimeSpan.get(100L, 200L);
        Collection<TimeSpan> input;
        Collection<TimeSpan> expected;

        input = Arrays.asList(TimeSpan.get(110, 120), TimeSpan.get(130, 140), TimeSpan.get(150, 160));
        expected = Arrays.asList(TimeSpan.get(100, 110), TimeSpan.get(120, 130), TimeSpan.get(140, 150), TimeSpan.get(160, 200));
        assertEquals(expected, span.subtract(input));

        input = Arrays.asList(TimeSpan.get(80, 90), TimeSpan.get(130, 150), TimeSpan.get(135, 145), TimeSpan.get(155, 165),
                TimeSpan.get(160, 170));
        expected = Arrays.asList(TimeSpan.get(100, 130), TimeSpan.get(150, 155), TimeSpan.get(170, 200));
        assertEquals(expected, span.subtract(input));

        input = Collections.emptyList();
        expected = Collections.singletonList(span);
        assertEquals(expected, span.subtract(input));
    }

    /** Test for {@link TimeSpan#get()}. */
    @Test
    public void testGet()
    {
        TimeSpan span = TimeSpan.get();
        assertEquals(span.getStart(), span.getEnd());
    }

    /** Test for {@link TimeSpan#get(long)}. */
    @Test
    public void testGetLong()
    {
        TimeSpan span = TimeSpan.get(100L);
        assertEquals(span.getEnd(), span.getStart());
    }

    /** Test for {@link TimeSpan#get(TimeInstant)}. */
    @Test
    public void testGetTimeInstant()
    {
        TimeInstant instant = TimeInstant.get(0);

        TimeSpan span = TimeSpan.get(instant);

        assertEquals(span.getEnd(), span.getStart());
    }

    /** Test for {@link TimeSpan#get(Date)}. */
    @Test
    public void testGetNullDate()
    {
        assertEquals(TimeSpan.TIMELESS, TimeSpan.get((Date)null));
    }

    /** Test for {@link TimeSpan#get(TimeInstant, Duration)}. */
    @Test
    public void testGetTimeInstantDuration()
    {
        TimeInstant instant = TimeInstant.get(0);
        Duration duration = Seconds.ONE;
        TimeSpan span = TimeSpan.get(instant, duration);
        assertEquals(1000, span.getDurationMs());
    }

    /** Test for {@link TimeSpan#get(Date, Date)}. */
    @Test
    public void testGetDateDate()
    {
        Date d1 = new Date(1L);
        Date d2 = new Date(2L);
        TimeSpan span = TimeSpan.get(d1, d2);

        assertTrue(span.getStart() == d1.getTime());
        assertTrue(span.getEnd() == d2.getTime());
    }

    /** Test for {@link TimeSpan#get(Date, Date)} with dates out of order. */
    @Test(expected = IllegalArgumentException.class)
    public void testGetDateDateBackwards()
    {
        Date d1 = new Date(2L);
        Date d2 = new Date(1L);
        TimeSpan.get(d1, d2);
    }

    /** Test for {@link TimeSpan#get(Date, Date)} with one negative date. */
    @Test
    public void testGetDateDateNegative1()
    {
        Date d1 = new Date(-1L);
        Date d2 = new Date(2L);
        TimeSpan.get(d1, d2);
    }

    /** Test for {@link TimeSpan#get(Date, Date)} with negative dates. */
    @Test
    public void testGetDateDateNegative2()
    {
        Date d1 = new Date(-2L);
        Date d2 = new Date(-1L);
        TimeSpan.get(d1, d2);
    }

    /** Test for {@link TimeSpan#get(Date, Duration)}. */
    @Test
    public void testGetDateDuration()
    {
        Date d1 = new Date(1L);
        Duration dur = new Weeks(2);
        TimeSpan span = TimeSpan.get(d1, dur);

        assertTrue(span.getStart() == d1.getTime());
        assertEquals(1209600001L, span.getEnd());

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2013, 3, 1);
        long start = cal.getTimeInMillis();

        cal.set(2013, 4, 1);
        long end = cal.getTimeInMillis();

        assertEquals(TimeSpan.get(start, end), TimeSpan.get(start, Months.ONE));
    }

    /** Test for {@link TimeSpan#get(Duration, Date)}. */
    @Test
    public void testGetDurationDate()
    {
        Date date = new Date(1001L);
        Duration dur = new Seconds(1);
        TimeSpan span = TimeSpan.get(dur, date);

        assertTrue(span.getEnd() == date.getTime());
        assertEquals(1L, span.getStart());
    }

    /** Test for {@link TimeSpan#get(Duration, TimeInstant)}. */
    @Test
    public void testGetDurationTimeInstant()
    {
        long millisecondValue = 1001L;
        TimeInstant instant = TimeInstant.get(millisecondValue);
        Duration duration = new Seconds(1);
        TimeSpan span = TimeSpan.get(duration, instant);

        assertEquals(span.getEnd(), millisecondValue);
        assertEquals(1L, span.getStart());
    }

    /** Test for {@link TimeSpan#get(Duration, long)}. */
    @Test
    public void testGetDurationLong()
    {
        long t1 = 1001L;
        Duration dur = new Seconds(1);
        TimeSpan span = TimeSpan.get(dur, t1);

        assertTrue(span.getEnd() == t1);
        assertEquals(1L, span.getStart());
    }

    /**
     * Test for {@link TimeSpan#get(Duration, long)} with a negative duration.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetDurationLongBackwards()
    {
        long t1 = 2L;
        Duration dur = new Seconds(-1L);
        TimeSpan.get(dur, t1);
    }

    /**
     * Test for {@link TimeSpan#get(Duration, long)} with one unconvertable
     * time.
     */
    @Test
    public void testGetDurationLongUnconvertableDuration()
    {
        Duration duration = new Months(1);

        TimeSpan result = TimeSpan.get(duration, 2);

        assertTrue(result.getEndDate().after(result.getStartDate()));
    }

    /** Test for {@link TimeSpan#get(Duration, long)} with one negative time. */
    @Test
    public void testGetDurationLongNegative1()
    {
        long t1 = -1L;
        Duration dur = new Seconds(1L);
        TimeSpan span = TimeSpan.get(dur, t1);

        assertEquals(1000L, span.getEnd() - span.getStart());
    }

    /**
     * Test for {@link TimeSpan#get(Duration, long)} with a negative time and a
     * negative duration.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetDurationLongNegative2()
    {
        long t1 = -2L;
        Duration dur = new Seconds(-1L);
        TimeSpan.get(dur, t1);
    }

    /** Test for {@link TimeSpan#get(long, Duration)}. */
    @Test
    public void testGetLongDuration()
    {
        long t1 = 1L;
        Duration dur = new Seconds(1);
        TimeSpan span = TimeSpan.get(t1, dur);

        assertTrue(span.getStart() == t1);
        assertEquals(1001L, span.getEnd());

        boolean is32bit = "x86".equals(System.getProperty("os.arch"));
        assertEquals(is32bit ? 24 : 32, span.getSizeBytes());

        dur = Months.ONE;
        span = TimeSpan.get(t1, dur);

        assertTrue(span.getStart() == t1);
        assertEquals(2678400001L, span.getEnd());
        assertEquals(32, span.getSizeBytes());
    }

    /**
     * Test for {@link TimeSpan#get(long, Duration)} with a negative duration.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetLongDurationBackwards()
    {
        long t1 = 2L;
        Duration dur = new Seconds(-1L);
        TimeSpan.get(t1, dur);
    }

    /** Test for {@link TimeSpan#get(long, Duration)} with one negative time. */
    @Test
    public void testGetLongDurationNegative1()
    {
        long t1 = -1L;
        Duration dur = new Seconds(1L);
        TimeSpan.get(t1, dur);
    }

    /**
     * Test for {@link TimeSpan#get(long, Duration)} with a negative time and a
     * negative duration.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetLongDurationNegative2()
    {
        long t1 = -2L;
        Duration dur = new Seconds(-1L);
        TimeSpan.get(t1, dur);
    }

    /** Test for {@link TimeSpan#get(long, long)}. */
    @Test
    public void testGetLongLongZeros()
    {
        assertEquals(TimeSpan.ZERO, TimeSpan.get(0, 0));
    }

    /** Test for {@link TimeSpan#get(long, long)}. */
    @Test
    public void testGetLongLong()
    {
        long t1 = 1L;
        long t2 = 2L;
        TimeSpan span = TimeSpan.get(t1, t2);

        assertTrue(span.getStart() == t1);
        assertTrue(span.getEnd() == t2);
        boolean is32bit = "x86".equals(System.getProperty("os.arch"));
        assertEquals(is32bit ? 24 : 32, span.getSizeBytes());

        TimeSpan instant = TimeSpan.get(t1, t1);

        assertEquals(t1, instant.getStart());
        assertEquals(t1, instant.getEnd());
        assertEquals(is32bit ? 16 : 24, instant.getSizeBytes());

        // Test a time span within range of REFERENCE_TIME.
        long t3 = TimeSpan.REFERENCE_TIME - (long)Integer.MAX_VALUE * Constants.MILLI_PER_UNIT;
        long t4 = TimeSpan.REFERENCE_TIME + (long)Integer.MAX_VALUE * Constants.MILLI_PER_UNIT;
        TimeSpan span2 = TimeSpan.get(t3, t4);
        assertEquals(t3, span2.getStart());
        assertEquals(t4, span2.getEnd());
        assertEquals(is32bit ? 16 : 24, span2.getSizeBytes());

        // Test with a month duration.
        TimeSpan span3 = TimeSpan.get(t3, Months.ONE);
        long t5 = t3;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(t5);
        cal.add(Calendar.MONTH, 1);
        long t6 = cal.getTimeInMillis();
        assertEquals(t5, span3.getStart());
        assertEquals(t6, span3.getEnd());
        assertEquals(24, span3.getSizeBytes());
        assertEquals(Months.ONE, span3.getDuration());

        // Test an end time that's one second too late.
        long t7 = TimeSpan.REFERENCE_TIME - (long)Integer.MAX_VALUE * Constants.MILLI_PER_UNIT;
        long t8 = TimeSpan.REFERENCE_TIME + (long)Integer.MAX_VALUE * Constants.MILLI_PER_UNIT + Constants.MILLI_PER_UNIT;
        TimeSpan span4 = TimeSpan.get(t7, t8);
        assertEquals(t7, span4.getStart());
        assertEquals(t8, span4.getEnd());
        assertEquals(is32bit ? 24 : 32, span4.getSizeBytes());

        // Test an end time not on a second boundary.
        long t9 = TimeSpan.REFERENCE_TIME;
        long t10 = TimeSpan.REFERENCE_TIME + 1L;
        TimeSpan span5 = TimeSpan.get(t9, t10);
        assertEquals(t9, span5.getStart());
        assertEquals(t10, span5.getEnd());
        assertEquals(is32bit ? 24 : 32, span5.getSizeBytes());
    }

    /** Test for {@link TimeSpan#get(long, long)} with times out of order. */
    @Test(expected = IllegalArgumentException.class)
    public void testGetLongLongBackwards()
    {
        long t1 = 2L;
        long t2 = 1L;
        TimeSpan.get(t1, t2);
    }

    /** Test for {@link TimeSpan#get(long, long)} with one negative time. */
    @Test
    public void testGetLongLongNegative1()
    {
        long t1 = -1L;
        long t2 = 2L;
        TimeSpan.get(t1, t2);
    }

    /** Test for {@link TimeSpan#get(long, long)} with two negative times. */
    @Test
    public void testGetLongLongNegative2()
    {
        long t1 = -2L;
        long t2 = -1L;
        TimeSpan.get(t1, t2);
    }

    /**
     * Test for {@link TimeSpan#toISO8601String()}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testToISO8601String() throws ParseException
    {
        SimpleDateFormat fmt = new SimpleDateFormat(DateTimeFormats.DATE_TIME_FORMAT);
        TimeSpan timeSpan = TimeSpan.get(fmt.parse("2010-03-13 12:35:17"), fmt.parse("2010-03-15 03:59:21"));
        String actual = timeSpan.toISO8601String();
        String expected = "2010-03-13T12:35:17Z/2010-03-15T03:59:21Z";
        assertEquals(expected, actual);

        fmt = new SimpleDateFormat(DateTimeFormats.DATE_TIME_MILLIS_FORMAT);
        timeSpan = TimeSpan.get(fmt.parse("2010-03-13 12:35:17.001"), fmt.parse("2010-03-15 03:59:21.999"));
        actual = timeSpan.toISO8601String();
        expected = "2010-03-13T12:35:17.001Z/2010-03-15T03:59:21.999Z";
        assertEquals(expected, actual);
    }

    /** Test for {@link TimeSpan#toString()}. */
    @Test
    public void testToString()
    {
        long t1 = 1L;
        long t2 = 2L;
        TimeSpan span = TimeSpan.get(t1, t2);
        String string = span.toString();
        assertTrue(string.length() > 0);
        assertFalse(string.equals(span.getClass().getName() + "@" + Integer.toHexString(span.hashCode())));
    }

    /** Test for {@link TimeSpan#union(TimeSpan)}. */
    @Test
    public void testUnion()
    {
        assertEquals(TimeSpan.get(1L, 3L), TimeSpan.get(1L, 2L).union(TimeSpan.get(2L, 3L)));
        assertEquals(TimeSpan.get(1L, 5L), TimeSpan.get(1L, 4L).union(TimeSpan.get(2L, 5L)));
        assertEquals(TimeSpan.get(1L, 5L), TimeSpan.get(1L, 5L).union(TimeSpan.get(1L, 5L)));
        assertEquals(TimeSpan.get(1L, 5L), TimeSpan.get(1L, 5L).union(TimeSpan.get(1L, 3L)));
        assertEquals(TimeSpan.get(1L, 5L), TimeSpan.get(1L, 5L).union(TimeSpan.get(3L, 5L)));
        assertEquals(TimeSpan.get(1L, 5L), TimeSpan.get(1L, 3L).union(TimeSpan.get(1L, 5L)));
        assertEquals(TimeSpan.get(1L, 5L), TimeSpan.get(3L, 5L).union(TimeSpan.get(1L, 5L)));
        assertEquals(TimeSpan.TIMELESS, TimeSpan.TIMELESS.union(TimeSpan.get(1L, 2L)));
        assertEquals(TimeSpan.TIMELESS, TimeSpan.get(1L, 2L).union(TimeSpan.TIMELESS));
    }

    /** Test for {@link TimeSpan#union(TimeSpan)}. */
    @Test(expected = IllegalArgumentException.class)
    public void testUnionFail()
    {
        TimeSpan.get(1L, 2L).union(TimeSpan.get(4L, 5L));
    }

    /** Test for {@link TimeSpan#subDivide(int)}. */
    @Test(expected = IllegalArgumentException.class)
    public void testSubdivideBadArg()
    {
        TimeSpan.ZERO.subDivide(0);
    }

    /** Test for {@link TimeSpan#subDivide(int)}. */
    @Test(expected = IllegalArgumentException.class)
    public void testSubdivideUnbounded()
    {
        TimeSpan.newUnboundedStartTimeSpan(0).subDivide(1);
    }

    /** Test for {@link TimeSpan#subDivide(int)}. */
    @Test(expected = IllegalArgumentException.class)
    public void testSubdivideBadDivision()
    {
        TimeSpan.ZERO.subDivide(1);
    }

    /** Test for {@link TimeSpan#subDivide(int)}. */
    @Test
    public void testSubdivide()
    {
        List<TimeSpan> divisions = TimeSpan.get(0, 3).subDivide(2);
        assertEquals(2, divisions.size());
        assertEquals(0, divisions.get(0).getStart());
        assertEquals(2, divisions.get(0).getEnd());
        assertEquals(2, divisions.get(1).getStart());
        assertEquals(3, divisions.get(1).getEnd());
    }
}
