package io.opensphere.controlpanels.animation.view;

import java.math.RoundingMode;
import java.util.Calendar;

import org.junit.Test;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.util.collections.New;
import org.junit.Assert;

/** Test {@link SelectableDurationSnapFunction}. */
public class SelectableDurationSnapFunctionTest
{
    /** Test {@link SelectableDurationSnapFunction}. */
    @Test
    public void testGetSnapDestination()
    {
        SelectableDurationSnapFunction func;

        Calendar cal = Calendar.getInstance();
        cal.clear();

        // Monday:
        cal.set(2013, 3, 1);
        TimeInstant anchorTime = TimeInstant.get(cal.getTime());

        cal.add(Calendar.WEEK_OF_YEAR, 1);
        long weekAway = cal.getTimeInMillis();

        // Wednesday:
        cal.set(2013, 4, 1);
        long monthAway = cal.getTimeInMillis();

        // Drag bigger than a month snap should stay at a month.
        TimeInstant inputTime = TimeInstant.get(monthAway);
        func = new SelectableDurationSnapFunction(New.set(Months.ONE, Weeks.ONE, Days.ONE), anchorTime, inputTime);
        Assert.assertEquals(inputTime, func.getSnapDestination(inputTime, RoundingMode.HALF_UP));
        Assert.assertEquals(inputTime, func.getSnapDestination(inputTime, RoundingMode.FLOOR));
        Assert.assertEquals(inputTime, func.getSnapDestination(inputTime, RoundingMode.CEILING));

        // Drag smaller than a month but less than 11.5 days ((30-7)/2), snap
        // should stay at a month.
        Assert.assertEquals(inputTime,
                func.getSnapDestination(inputTime.plus(new Days(-11.5).add(Milliseconds.ONE)), RoundingMode.HALF_UP));
        Assert.assertEquals(inputTime,
                func.getSnapDestination(inputTime.plus(new Days(-11.5).add(Milliseconds.ONE)), RoundingMode.FLOOR));
        Assert.assertEquals(TimeInstant.get(weekAway),
                func.getSnapDestination(inputTime.plus(new Days(-11)), RoundingMode.CEILING));

        // Drag smaller than a month and more than 11.5 days ((30-7)/2), snap
        // should go to a week.
        Assert.assertEquals(TimeInstant.get(weekAway),
                func.getSnapDestination(inputTime.plus(new Days(-11.5)), RoundingMode.HALF_UP));
        Assert.assertEquals(inputTime, func.getSnapDestination(inputTime.plus(new Days(-11.5)), RoundingMode.FLOOR));
        Assert.assertEquals(TimeInstant.get(weekAway),
                func.getSnapDestination(inputTime.plus(new Days(-11.5)), RoundingMode.CEILING));

        // Drag bigger than a week but less than 11.5 days ((30-7)/2), snap
        // should stay a week.
        inputTime = TimeInstant.get(weekAway);
        func = new SelectableDurationSnapFunction(New.set(Months.ONE, Weeks.ONE, Days.ONE), anchorTime, inputTime);
        Assert.assertEquals(inputTime,
                func.getSnapDestination(inputTime.plus(new Days(11.5).subtract(Milliseconds.ONE)), RoundingMode.HALF_UP));
        Assert.assertEquals(inputTime,
                func.getSnapDestination(inputTime.plus(new Days(11.5).subtract(Milliseconds.ONE)), RoundingMode.FLOOR));
        Assert.assertEquals(TimeInstant.get(monthAway),
                func.getSnapDestination(inputTime.plus(new Days(11.5).subtract(Milliseconds.ONE)), RoundingMode.CEILING));

        // Drag bigger than a week and more than 11.5 days ((30-7)/2), snap
        // should go to a month.
        Assert.assertEquals(TimeInstant.get(monthAway),
                func.getSnapDestination(inputTime.plus(new Days(11.5)), RoundingMode.HALF_UP));
        Assert.assertEquals(inputTime, func.getSnapDestination(inputTime.plus(new Days(11.5)), RoundingMode.FLOOR));
        Assert.assertEquals(TimeInstant.get(monthAway),
                func.getSnapDestination(inputTime.plus(new Days(11.5)), RoundingMode.CEILING));

        // Drag smaller than a week, but less than 3 days, snap should stay a
        // week.
        Assert.assertEquals(inputTime,
                func.getSnapDestination(inputTime.plus(new Days(-3).add(Milliseconds.ONE)), RoundingMode.HALF_UP));
        Assert.assertEquals(inputTime,
                func.getSnapDestination(inputTime.plus(new Days(-3).add(Milliseconds.ONE)), RoundingMode.FLOOR));
        Assert.assertEquals(anchorTime.plus(Days.ONE),
                func.getSnapDestination(inputTime.plus(new Days(-3).add(Milliseconds.ONE)), RoundingMode.CEILING));

        // Drag smaller than a week and more than 3 days, snap should go to a
        // day.
        Assert.assertEquals(anchorTime.plus(Days.ONE),
                func.getSnapDestination(inputTime.plus(new Days(-3)), RoundingMode.HALF_UP));
        Assert.assertEquals(inputTime, func.getSnapDestination(inputTime.plus(new Days(-3)), RoundingMode.FLOOR));
        Assert.assertEquals(anchorTime.plus(Days.ONE),
                func.getSnapDestination(inputTime.plus(new Days(-3)), RoundingMode.CEILING));

        // Drag bigger than a day but less than 3 days, snap should stay a day.
        inputTime = anchorTime.plus(Days.ONE);
        func = new SelectableDurationSnapFunction(New.set(Months.ONE, Weeks.ONE, Days.ONE), anchorTime, inputTime);
        Assert.assertEquals(inputTime,
                func.getSnapDestination(inputTime.plus(new Days(3).subtract(Milliseconds.ONE)), RoundingMode.HALF_UP));
        Assert.assertEquals(inputTime,
                func.getSnapDestination(inputTime.plus(new Days(3).subtract(Milliseconds.ONE)), RoundingMode.FLOOR));
        Assert.assertEquals(TimeInstant.get(weekAway),
                func.getSnapDestination(inputTime.plus(new Days(3).subtract(Milliseconds.ONE)), RoundingMode.CEILING));

        // Drag bigger than 3 days, snap should go to a week.
        Assert.assertEquals(TimeInstant.get(weekAway),
                func.getSnapDestination(inputTime.plus(new Days(3)), RoundingMode.HALF_UP));
        Assert.assertEquals(inputTime, func.getSnapDestination(inputTime.plus(new Days(3)), RoundingMode.FLOOR));
        Assert.assertEquals(TimeInstant.get(weekAway),
                func.getSnapDestination(inputTime.plus(new Days(3)), RoundingMode.CEILING));

        // Drag smaller than a day, snap should stay a day.
        Assert.assertEquals(inputTime, func.getSnapDestination(inputTime.plus(new Days(-1)), RoundingMode.HALF_UP));
        Assert.assertEquals(inputTime, func.getSnapDestination(inputTime.plus(new Days(-1)), RoundingMode.FLOOR));
        Assert.assertEquals(inputTime, func.getSnapDestination(inputTime.plus(new Days(-1)), RoundingMode.CEILING));
    }
}
