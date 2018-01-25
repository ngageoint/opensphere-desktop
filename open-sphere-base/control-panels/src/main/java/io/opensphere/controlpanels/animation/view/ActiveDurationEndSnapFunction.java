package io.opensphere.controlpanels.animation.view;

import java.math.RoundingMode;
import java.util.function.Supplier;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.timeline.ResolutionBasedSnapFunction;
import io.opensphere.controlpanels.timeline.SnapFunction;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.ObservableList;
import io.opensphere.core.util.Utilities;

/**
 * Function for selecting the snap-to locations of the active span end times.
 */
final class ActiveDurationEndSnapFunction implements SnapFunction
{
    /** A supplier for the end of the active span that is not being changed. */
    private final Supplier<TimeInstant> myAnchorTime;

    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** The resolution of the timeline display. */
    private final Supplier<Double> myMillisPerPixel;

    /**
     * A supplier for the starting point for the end of the active span that is
     * being changed.
     */
    private final Supplier<TimeInstant> mySnapOrigin;

    /**
     * Constructor.
     *
     * @param anchorTime A supplier for the end of the active span that is not
     *            being changed.
     * @param snapOrigin A supplier for the starting point for the end of the
     *            active span that is being changed.
     * @param animationModel The animation model.
     * @param millisPerPixel The resolution of the timeline display.
     */
    ActiveDurationEndSnapFunction(Supplier<TimeInstant> anchorTime, Supplier<TimeInstant> snapOrigin,
            AnimationModel animationModel, Supplier<Double> millisPerPixel)
    {
        myAnchorTime = Utilities.checkNull(anchorTime, "anchorTime");
        mySnapOrigin = Utilities.checkNull(snapOrigin, "snapOrigin");
        myAnimationModel = Utilities.checkNull(animationModel, "animationModel");
        myMillisPerPixel = Utilities.checkNull(millisPerPixel, "millisPerPixel");
    }

    @Override
    public TimeInstant getSnapDestination(TimeInstant time, RoundingMode mode)
    {
        Duration dataLoadDur = myAnimationModel.getSelectedDataLoadDuration().get();
        SnapFunction func;
        if (dataLoadDur == null || !myAnimationModel.getSnapToDataBoundaries().get().booleanValue())
        {
            func = new ResolutionBasedSnapFunction(myMillisPerPixel);
        }
        else
        {
            func = new SelectableDurationSnapFunction(myAnimationModel.getDataLoadDurations().get(), myAnchorTime.get(),
                    mySnapOrigin.get());
        }
        TimeInstant snapDestination = func.getSnapDestination(time, mode);

        ObservableList<TimeSpan> skippedIntervals = myAnimationModel.getSkippedIntervals();
        if (!skippedIntervals.isEmpty())
        {
            TimeSpan snapSpan = snapDestination.isBefore(myAnchorTime.get()) ? TimeSpan.get(snapDestination, myAnchorTime.get())
                    : TimeSpan.get(myAnchorTime.get(), snapDestination);
            for (TimeSpan skipped : skippedIntervals)
            {
                if (skipped.overlaps(snapSpan))
                {
                    return mySnapOrigin.get();
                }
            }
        }

        return snapDestination;
    }
}
