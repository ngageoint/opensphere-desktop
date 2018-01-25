package io.opensphere.controlpanels.animation.view;

import java.math.RoundingMode;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.controlpanels.timeline.SnapFunction;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.InconvertibleUnits;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Function for calculating a snap destination with the snap-to times being
 * multiples of a specified duration from the Java epoch.
 */
public class SelectableDurationSnapFunction implements SnapFunction
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SelectableDurationSnapFunction.class);

    /**
     * The other time that, subtracted from the snap destination, makes the
     * selected duration.
     */
    private final TimeInstant myAnchorTime;

    /** The available durations. */
    private final Set<? extends Duration> myAvailableDurations;

    /**
     * The time that is usually at the start of the drag, but may change during
     * the drag action due to external constraints. The difference between the
     * anchorTime and the originalTime should give the original selected
     * duration.
     */
    private final TimeInstant myOriginalTime;

    /**
     * Constructor.
     *
     * @param availableDurations The available snap-to durations.
     * @param anchorTime The other time that, subtracted from the snap
     *            destination, makes the selected duration.
     * @param originalTime The time that is usually at the start of the drag,
     *            but may change during the drag action due to external
     *            constraints. The difference between the anchorTime and the
     *            originalTime should give the original selected duration.
     */
    public SelectableDurationSnapFunction(Set<? extends Duration> availableDurations, TimeInstant anchorTime,
            TimeInstant originalTime)
    {
        myAvailableDurations = New.unmodifiableSet(Utilities.checkNull(availableDurations, "availableDurations"));
        myAnchorTime = Utilities.checkNull(anchorTime, "anchorTime");
        myOriginalTime = Utilities.checkNull(originalTime, "originalTime");
    }

    @Override
    public TimeInstant getSnapDestination(TimeInstant inputTime, RoundingMode mode)
    {
        Duration startDur = myOriginalTime.minus(myAnchorTime);
        Duration endDur = inputTime.minus(myAnchorTime);

        int desiredCompareTo = endDur.compareTo(startDur);
        Duration selectedDur = null;
        for (Duration available : myAvailableDurations)
        {
            try
            {
                if (startDur.longValue() < 0)
                {
                    available = available.negate();
                }
                if (available.compareTo(startDur) == desiredCompareTo
                        && (selectedDur == null || selectedDur.compareTo(available) == desiredCompareTo))
                {
                    selectedDur = available;
                }
            }
            catch (InconvertibleUnits e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(e, e);
                }
            }
        }

        if (selectedDur == null)
        {
            return myOriginalTime;
        }

        TimeInstant result = myAnchorTime.plus(selectedDur);

        if (mode == RoundingMode.HALF_UP)
        {
            if (Math.abs(myOriginalTime.getEpochMillis() - result.getEpochMillis()) > Math
                    .abs(2 * (myOriginalTime.getEpochMillis() - inputTime.getEpochMillis())))
            {
                result = myOriginalTime;
            }
        }
        else if (mode == RoundingMode.FLOOR)
        {
            if (Math.abs(myOriginalTime.getEpochMillis() - result.getEpochMillis()) > Math
                    .abs(myOriginalTime.getEpochMillis() - inputTime.getEpochMillis()))
            {
                result = myOriginalTime;
            }
        }
        else if (mode != RoundingMode.CEILING)
        {
            throw new IllegalArgumentException("RoundingMode " + mode + " is not supported.");
        }
        return result;
    }
}
