package io.opensphere.controlpanels.animation.view;

import java.math.RoundingMode;
import java.util.Date;

import io.opensphere.controlpanels.timeline.SnapFunction;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.time.TimelineUtilities;

/**
 * Function for calculating a snap destination with the snap-to times being
 * multiples of a specified duration from a reference time.
 */
class DurationBasedSnapFunction implements SnapFunction
{
    /** The snap-to duration. */
    private final Duration myDuration;

    /** The reference time. */
    private final TimeInstant myReferenceTime;

    /**
     * Constructor.
     *
     * @param referenceTime The reference time.
     * @param duration The snap-to duration.
     */
    public DurationBasedSnapFunction(TimeInstant referenceTime, Duration duration)
    {
        myReferenceTime = Utilities.checkNull(referenceTime, "referenceTime");
        myDuration = Utilities.checkNull(duration, "duration");
    }

    @Override
    public TimeInstant getSnapDestination(TimeInstant inputTime, RoundingMode mode)
    {
        TimeInstant target = inputTime;
        long millis;
        if (mode == RoundingMode.HALF_UP)
        {
            millis = target.getEpochMillis() / 2 + target.plus(myDuration).getEpochMillis() / 2
                    - myReferenceTime.getEpochMillis();
        }
        else if (mode == RoundingMode.CEILING)
        {
            millis = target.plus(myDuration).getEpochMillis() - myReferenceTime.getEpochMillis() - 1L;
        }
        else if (mode == RoundingMode.FLOOR)
        {
            millis = target.getEpochMillis();
        }
        else
        {
            throw new IllegalArgumentException("RoundingMode " + mode + " not supported.");
        }

        if (myDuration instanceof Weeks || !myDuration.isConvertibleTo(Milliseconds.ONE))
        {
            millis = TimelineUtilities.roundDown(new Date(millis), myDuration).getTimeInMillis();
        }
        else
        {
            long mod = Milliseconds.get(myDuration).longValue();
            if (mod != 0L)
            {
                millis = millis / mod * mod;
            }
        }
        TimeInstant result = TimeInstant.get(millis + myReferenceTime.getEpochMillis());
        return result;
    }
}
