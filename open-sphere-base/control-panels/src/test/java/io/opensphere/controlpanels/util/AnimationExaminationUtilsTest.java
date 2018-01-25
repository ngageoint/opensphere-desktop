package io.opensphere.controlpanels.util;

import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationPlan.EndBehavior;
import io.opensphere.core.animation.impl.DefaultAnimationPlan;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/**
 * Test {@link AnimationExaminationUtils}.
 */
public class AnimationExaminationUtilsTest
{
    /** Some days. */
    private static String ourDays = "2013-12-01 00:00:00/2013-12-02 00:00:00,2013-12-02 00:00:00/2013-12-03 00:00:00,"
            + "2013-12-03 00:00:00/2013-12-04 00:00:00,2013-12-04 00:00:00/2013-12-05 00:00:00,"
            + "2013-12-05 00:00:00/2013-12-06 00:00:00,2013-12-06 00:00:00/2013-12-07 00:00:00,"
            + "2013-12-07 00:00:00/2013-12-08 00:00:00,2013-12-08 00:00:00/2013-12-09 00:00:00,"
            + "2013-12-09 00:00:00/2013-12-10 00:00:00,2013-12-10 00:00:00/2013-12-11 00:00:00,"
            + "2013-12-11 00:00:00/2013-12-12 00:00:00,2013-12-12 00:00:00/2013-12-13 00:00:00,"
            + "2013-12-13 00:00:00/2013-12-14 00:00:00,2013-12-14 00:00:00/2013-12-15 00:00:00,"
            + "2013-12-15 00:00:00/2013-12-16 00:00:00,2013-12-16 00:00:00/2013-12-17 00:00:00,"
            + "2013-12-17 00:00:00/2013-12-18 00:00:00,2013-12-18 00:00:00/2013-12-19 00:00:00,"
            + "2013-12-19 00:00:00/2013-12-20 00:00:00,2013-12-20 00:00:00/2013-12-21 00:00:00,"
            + "2013-12-21 00:00:00/2013-12-22 00:00:00,2013-12-22 00:00:00/2013-12-23 00:00:00,"
            + "2013-12-23 00:00:00/2013-12-24 00:00:00,2013-12-24 00:00:00/2013-12-25 00:00:00,"
            + "2013-12-25 00:00:00/2013-12-26 00:00:00,2013-12-26 00:00:00/2013-12-27 00:00:00,"
            + "2013-12-27 00:00:00/2013-12-28 00:00:00,2013-12-28 00:00:00/2013-12-29 00:00:00,"
            + "2013-12-29 00:00:00/2013-12-30 00:00:00,2013-12-30 00:00:00/2013-12-31 00:00:00,"
            + "2013-12-31 00:00:00/2014-01-01 00:00:00,2014-01-01 00:00:00/2014-01-02 00:00:00,"
            + "2014-01-02 00:00:00/2014-01-03 00:00:00,2014-01-03 00:00:00/2014-01-04 00:00:00,"
            + "2014-01-04 00:00:00/2014-01-05 00:00:00";

    /** Five weeks. */
    private static String ourFiveWeeks = "2013-12-01 00:00:00/2013-12-08 00:00:00,2013-12-08 00:00:00/2013-12-15 00:00:00,"
            + "2013-12-15 00:00:00/2013-12-22 00:00:00,2013-12-22 00:00:00/2013-12-29 00:00:00,"
            + "2013-12-29 00:00:00/2014-01-05 00:00:00";

    /** Three months with the first and last months expanded to weeks. */
    private static String ourThreeMonths1 = "2013-10-01 00:00:00/2013-11-01 00:00:00,"
            + "2013-09-29 00:00:00/2013-10-06 00:00:00,2013-10-06 00:00:00/2013-10-13 00:00:00,"
            + "2013-10-13 00:00:00/2013-10-20 00:00:00,2013-10-20 00:00:00/2013-10-27 00:00:00,"
            + "2013-10-27 00:00:00/2013-11-03 00:00:00,2013-11-01 00:00:00/2013-12-01 00:00:00,"
            + "2013-12-01 00:00:00/2014-01-01 00:00:00,2013-12-01 00:00:00/2013-12-08 00:00:00,"
            + "2013-12-08 00:00:00/2013-12-15 00:00:00,2013-12-15 00:00:00/2013-12-22 00:00:00,"
            + "2013-12-22 00:00:00/2013-12-29 00:00:00,2013-12-29 00:00:00/2014-01-05 00:00:00";

    /**
     * Three months with the first and last months expanded to weeks and some of
     * the weeks turned off.
     */
    private static String ourThreeMonths2 = "2013-10-01 00:00:00/2013-11-01 00:00:00,"
            + "2013-09-29 00:00:00/2013-10-06 00:00:00,2013-10-06 00:00:00/2013-10-13 00:00:00,"
            + "2013-10-20 00:00:00/2013-10-27 00:00:00,2013-10-27 00:00:00/2013-11-03 00:00:00,"
            + "2013-11-01 00:00:00/2013-12-01 00:00:00,2013-12-01 00:00:00/2014-01-01 00:00:00,"
            + "2013-12-08 00:00:00/2013-12-15 00:00:00,2013-12-15 00:00:00/2013-12-22 00:00:00,"
            + "2013-12-22 00:00:00/2013-12-29 00:00:00";

    /**
     * Three months with the first month turned off but the overlapping week
     * turned on.
     */
    private static String ourThreeMonths3 = "2013-09-29 00:00:00/2013-10-06 00:00:00,"
            + "2013-10-06 00:00:00/2013-10-13 00:00:00,2013-10-13 00:00:00/2013-10-20 00:00:00,"
            + "2013-10-20 00:00:00/2013-10-27 00:00:00,2013-10-27 00:00:00/2013-11-03 00:00:00,"
            + "2013-11-01 00:00:00/2013-12-01 00:00:00,2013-12-01 00:00:00/2014-01-01 00:00:00";

    /**
     * Test {@link AnimationExaminationUtils#getSpanAndDuration(AnimationPlan)}.
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testGetSpanAndDuration() throws ParseException
    {
        // Test months.
        List<TimeSpan> sequence = getSpansForString(ourThreeMonths1);
        AnimationPlan plan = new DefaultAnimationPlan(sequence, EndBehavior.WRAP);
        Pair<TimeSpan, Duration> spanAndDur = AnimationExaminationUtils.getSpanAndDuration(plan);
        Date spanStart = new GregorianCalendar(2013, 9, 1).getTime();
        Date spanEnd = new GregorianCalendar(2013, 11, 31).getTime();
        TimeSpan monthSpan = TimeSpan.get(spanStart, spanEnd);

        Assert.assertEquals(monthSpan, spanAndDur.getFirstObject());
        Assert.assertEquals(Months.ONE, spanAndDur.getSecondObject());

        // This sequence is missing some weeks (especially the week that
        // overlaps the year boundary), but should give the same results as the
        // first set of intervals
        sequence = getSpansForString(ourThreeMonths2);
        plan = new DefaultAnimationPlan(sequence, EndBehavior.WRAP);
        spanAndDur = AnimationExaminationUtils.getSpanAndDuration(plan);
        Assert.assertEquals(monthSpan, spanAndDur.getFirstObject());
        Assert.assertEquals(Months.ONE, spanAndDur.getSecondObject());

        // This sequence is missing the first month and the first week extends
        // backwards into the previous month, but should give the same results
        // as the first set of intervals
        sequence = getSpansForString(ourThreeMonths3);
        plan = new DefaultAnimationPlan(sequence, EndBehavior.WRAP);
        spanAndDur = AnimationExaminationUtils.getSpanAndDuration(plan);
        Assert.assertEquals(monthSpan, spanAndDur.getFirstObject());
        Assert.assertEquals(Months.ONE, spanAndDur.getSecondObject());

        // Test weeks.
        sequence = getSpansForString(ourFiveWeeks);
        plan = new DefaultAnimationPlan(sequence, EndBehavior.WRAP);
        spanAndDur = AnimationExaminationUtils.getSpanAndDuration(plan);
        spanStart = new GregorianCalendar(2013, 11, 1).getTime();
        spanEnd = new GregorianCalendar(2014, 0, 4).getTime();
        Assert.assertEquals(TimeSpan.get(spanStart, spanEnd), spanAndDur.getFirstObject());
        Assert.assertEquals(Weeks.ONE, spanAndDur.getSecondObject());

        /* Test days - Because of the way the animation code treats days, the
         * end time needs to go to 00:00 on the day following the end of the
         * interval */
        sequence = getSpansForString(ourDays);
        plan = new DefaultAnimationPlan(sequence, EndBehavior.WRAP);
        spanAndDur = AnimationExaminationUtils.getSpanAndDuration(plan);
        spanEnd = new GregorianCalendar(2014, 0, 5).getTime();
        Assert.assertEquals(TimeSpan.get(spanStart, spanEnd), spanAndDur.getFirstObject());
        Assert.assertEquals(Days.ONE, spanAndDur.getSecondObject());
    }

    /**
     * Parse the string and produce a list of time spans.
     *
     * @param spansString The string which contains the time spans.
     * @return The time spans.
     * @throws ParseException If one of the tokens fails to parse.
     */
    private List<TimeSpan> getSpansForString(String spansString) throws ParseException
    {
        List<TimeSpan> spans = New.list();

        StringTokenizer intervalTok = new StringTokenizer(spansString, ",");
        while (intervalTok.hasMoreElements())
        {
            spans.add(TimeSpan.fromISO8601String(intervalTok.nextToken()));
        }

        return spans;
    }
}
