package io.opensphere.controlpanels.animation.view;

import java.math.RoundingMode;

import io.opensphere.controlpanels.timeline.ResolutionBasedSnapFunction;
import io.opensphere.controlpanels.timeline.SnapFunction;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.InconvertibleUnits;
import io.opensphere.core.units.duration.Duration;

/**
 * A snap function that ensures that the snapped-to duration is not larger than
 * a specified limit, but delegates to another snap function otherwise.
 */
class MaxDurationSnapFunction implements SnapFunction
{
    /** The maximum duration. */
    private final Duration myMaxDuration;

    /** The wrapped snap function. */
    private final ResolutionBasedSnapFunction myWrappedFunction;

    /** The end of the active span that is not being changed. */
    private final TimeInstant myAnchorTime;

    /**
     * The starting point for the end of the active span that is being changed.
     */
    private final TimeInstant mySnapOrigin;

    /**
     * Constructor.
     *
     * @param max The limit on the snapped-to duration.
     * @param wrappedFunction The wrapped snap function.
     * @param anchorTime The end of the active span that is not being changed.
     * @param snapOrigin The starting point for the end of the active span that
     *            is being changed.
     */
    public MaxDurationSnapFunction(Duration max, ResolutionBasedSnapFunction wrappedFunction, TimeInstant anchorTime,
            TimeInstant snapOrigin)
    {
        myMaxDuration = max;
        myWrappedFunction = wrappedFunction;
        myAnchorTime = anchorTime;
        mySnapOrigin = snapOrigin;
    }

    @Override
    public TimeInstant getSnapDestination(TimeInstant inputTime, RoundingMode mode)
    {
        Duration startDur = mySnapOrigin.minus(myAnchorTime);
        TimeInstant endTime = myWrappedFunction.getSnapDestination(inputTime, mode);
        Duration endDur = endTime.minus(myAnchorTime);

        Duration biggest = myMaxDuration;
        if (startDur.signum() < 0)
        {
            biggest = biggest.negate();
        }
        TimeInstant result;
        try
        {
            if (endDur.compareTo(biggest) == endDur.signum())
            {
                result = myAnchorTime.plus(biggest);
            }
            else
            {
                result = endTime;
            }
        }
        catch (InconvertibleUnits e)
        {
            result = myAnchorTime.plus(biggest);
        }
        return result;
    }
}
