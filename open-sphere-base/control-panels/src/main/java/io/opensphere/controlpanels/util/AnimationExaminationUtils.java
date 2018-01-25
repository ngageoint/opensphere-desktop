package io.opensphere.controlpanels.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.DurationUnitsProvider;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.util.lang.Pair;

/** Utilities for determining information about animation plans. */
public final class AnimationExaminationUtils
{
    /**
     * Get the span and the base duration for an animation plan.
     *
     * @param plan The plan for which the span and duration are desired.
     * @return The span and duration.
     */
    public static Pair<TimeSpan, Duration> getSpanAndDuration(AnimationPlan plan)
    {
        List<? extends TimeSpan> sequence = plan.getAnimationSequence();

        Duration maxDuration = Seconds.ZERO;
        for (TimeSpan span : sequence)
        {
            if (span.getDuration().compareTo(maxDuration) > 0)
            {
                maxDuration = span.getDuration();
            }
        }

        Duration duration = new DurationUnitsProvider().getLargestIntegerUnitType(maxDuration);
        if (duration == null || duration.getMagnitude().intValue() != 1)
        {
            duration = Months.ONE;
        }

        Date start = determineStartDate(sequence.get(0), duration.getClass());
        Date end = determineEndDate(sequence.get(sequence.size() - 1), duration.getClass());

        TimeSpan span = TimeSpan.get(start, end);
        return new Pair<TimeSpan, Duration>(span, duration);
    }

    /**
     * Determine the end date of the plan for the base duration. If the base
     * duration is months and the last month has been expanded, it may extend
     * into the following month, in this case it will be adjusted back to the
     * end of the month which has been expanded.
     *
     * @param lastSpan The last interval in the plan
     * @param duration The base duration of the plan.
     * @return the end of the plan's overall span.
     */
    private static Date determineEndDate(TimeSpan lastSpan, Class<? extends Duration> duration)
    {
        Date end;
        if (Months.class.equals(duration))
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(lastSpan.getEndDate());
            if (lastSpan.getDuration().compareTo(Weeks.ONE) <= 0 && cal.get(Calendar.DAY_OF_MONTH) < 7)
            {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.add(Calendar.DAY_OF_MONTH, -1);
            }
            else
            {
                cal.add(Calendar.DAY_OF_MONTH, -1);
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
            end = cal.getTime();
        }
        else if (Weeks.class.equals(duration))
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(lastSpan.getEndDate());
            cal.add(Calendar.DAY_OF_YEAR, -1);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            end = cal.getTime();
        }
        else
        {
            end = lastSpan.getEndDate();
        }
        return end;
    }

    /**
     * Determine the start date of the plan for the base duration. If the base
     * duration is months and the first month has been expanded, it may extend
     * into the preceding month, in this case it will be adjusted forward to the
     * beginning of the month which has been expanded.
     *
     * @param firstSpan The first interval in the plan
     * @param duration The base duration of the plan.
     * @return the start of the plan's overall span.
     */
    private static Date determineStartDate(TimeSpan firstSpan, Class<? extends Duration> duration)
    {
        Date start;
        if (Months.class.equals(duration) && firstSpan.getDuration().compareTo(Weeks.ONE) <= 0)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(firstSpan.getStartDate());
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            boolean addMonth = cal.get(Calendar.DAY_OF_MONTH) > daysInMonth - 7;
            cal.set(Calendar.DAY_OF_MONTH, 1);
            if (addMonth)
            {
                cal.add(Calendar.MONTH, 1);
            }
            start = cal.getTime();
        }
        else if (Weeks.class.equals(duration))
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(firstSpan.getStartDate());
            cal.set(Calendar.DAY_OF_WEEK, 1);
            start = cal.getTime();
        }
        else
        {
            start = firstSpan.getStartDate();
        }
        return start;
    }

    /** Disallow instantiation. */
    private AnimationExaminationUtils()
    {
    }
}
