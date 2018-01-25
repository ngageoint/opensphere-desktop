package io.opensphere.controlpanels.animation.view;

import java.math.RoundingMode;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.timeline.ResolutionBasedSnapFunction;
import io.opensphere.controlpanels.timeline.SnapFunction;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.Utilities;

/** Snap function for either end of the loop span. */
public class LoopSpanEndSnapFunction implements SnapFunction
{
    /** If the loop span should snap to data boundaries. */
    private final ObservableValue<Boolean> mySnapToDataBoundaries;

    /** The resolution of the timeline. */
    @Nullable
    private final Supplier<Double> myMillisPerPixel;

    /** The selected data load duration. */
    private final ObservableValue<Duration> mySelectedDataLoadDuration;

    /**
     * Constructor.
     *
     * @param animationModel The animation model.
     * @param millisPerPixel The resolution of the timeline display.
     */
    public LoopSpanEndSnapFunction(AnimationModel animationModel, @Nullable Supplier<Double> millisPerPixel)
    {
        Utilities.checkNull(animationModel, "animationModel");
        mySelectedDataLoadDuration = animationModel.getSelectedDataLoadDuration();
        mySnapToDataBoundaries = animationModel.getSnapToDataBoundaries();
        myMillisPerPixel = millisPerPixel;
    }

    @Override
    public TimeInstant getSnapDestination(TimeInstant time, RoundingMode mode)
    {
        TimeInstant loopStart;
        Duration selectedDataLoadDuration = mySelectedDataLoadDuration.get();
        if (selectedDataLoadDuration == null || !mySnapToDataBoundaries.get().booleanValue())
        {
            if (myMillisPerPixel == null)
            {
                loopStart = time;
            }
            else
            {
                loopStart = new ResolutionBasedSnapFunction(myMillisPerPixel).getSnapDestination(time, mode);
            }
        }
        else
        {
            DurationBasedSnapFunction snapFunc = new DurationBasedSnapFunction(TimeInstant.get(0), selectedDataLoadDuration);
            loopStart = snapFunc.getSnapDestination(time, mode);
        }

        return loopStart;
    }
}
