package io.opensphere.controlpanels.animation.view;

import java.math.RoundingMode;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.timeline.ResolutionBasedSnapFunction;
import io.opensphere.controlpanels.timeline.SnapFunction;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.ObservableList;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * Snap function for the active span end, for translation (not for changing the
 * active duration).
 */
public class ActiveSpanEndSnapFunction implements SnapFunction
{
    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** Snap function that snaps to skipped interval edges. */
    private final SnapFunction mySkippedIntervalSnapFunction = new SnapFunction()
    {
        @Override
        public TimeInstant getSnapDestination(TimeInstant time, RoundingMode mode)
        {
            TimeInstant snap;
            ObservableList<TimeSpan> skippedIntervals = myAnimationModel.getSkippedIntervals();
            if (skippedIntervals.isEmpty())
            {
                snap = time;
            }
            else
            {
                TimeSpan leftGuess = TimeSpan.get(myAnimationModel.getActiveSpanDuration().get(), time);
                TimeSpan rightGuess = leftGuess;

                boolean foundOverlap;
                do
                {
                    foundOverlap = false;
                    for (int index = 0; index < skippedIntervals.size(); ++index)
                    {
                        TimeSpan skipped = skippedIntervals.get(index);
                        if (skipped.overlaps(leftGuess))
                        {
                            leftGuess = TimeSpan.get(myAnimationModel.getActiveSpanDuration().get(), skipped.getStartInstant());
                            foundOverlap = true;
                        }
                        if (skipped.overlaps(rightGuess))
                        {
                            rightGuess = TimeSpan.get(skipped.getEndInstant(), myAnimationModel.getActiveSpanDuration().get());
                            foundOverlap = true;
                        }
                        if (foundOverlap)
                        {
                            break;
                        }
                    }
                }
                while (foundOverlap);

                TimeSpan guess;
                if (mode == RoundingMode.HALF_UP)
                {
                    guess = rightGuess.getEnd() - time.getEpochMillis() > time.getEpochMillis() - leftGuess.getEnd() ? leftGuess
                            : rightGuess;
                }
                else if (mode == RoundingMode.CEILING)
                {
                    guess = rightGuess;
                }
                else if (mode == RoundingMode.FLOOR)
                {
                    guess = leftGuess;
                }
                else
                {
                    throw new UnexpectedEnumException(mode);
                }

                snap = guess.getEndInstant();
            }
            return snap;
        }
    };

    /** Snap function that is simply resolution-based. */
    private final SnapFunction myResolutionSnapFunction;

    /**
     * Constructor.
     *
     * @param animationModel The animation model.
     * @param millisPerPixel The resolution of the timeline display.
     */
    public ActiveSpanEndSnapFunction(AnimationModel animationModel, @Nullable Supplier<Double> millisPerPixel)
    {
        myAnimationModel = Utilities.checkNull(animationModel, "animationModel");

        myResolutionSnapFunction = millisPerPixel == null ? new NoSnapSnapFunction()
                : new ResolutionBasedSnapFunction(millisPerPixel);
    }

    @Override
    public TimeInstant getSnapDestination(TimeInstant time, RoundingMode mode)
    {
        TimeInstant result;

        Duration selectedDataLoadDuration = myAnimationModel.getSelectedDataLoadDuration().get();
        if (selectedDataLoadDuration == null || !myAnimationModel.getSnapToDataBoundaries().get().booleanValue())
        {
            TimeInstant snap1 = myResolutionSnapFunction.getSnapDestination(time, mode);
            result = mySkippedIntervalSnapFunction.getSnapDestination(snap1, mode);
        }
        else
        {
            DurationBasedSnapFunction durationFunc = new DurationBasedSnapFunction(TimeInstant.get(0), selectedDataLoadDuration);

            result = time;
            TimeInstant snap;
            RoundingMode modeToUse = mode;
            while (true)
            {
                snap = mySkippedIntervalSnapFunction.getSnapDestination(result, modeToUse);
                result = durationFunc.getSnapDestination(snap, modeToUse);
                int compare = result.compareTo(snap);
                if (compare == 0)
                {
                    break;
                }
                else if (modeToUse == RoundingMode.HALF_UP)
                {
                    modeToUse = compare < 0 ? RoundingMode.FLOOR : RoundingMode.CEILING;
                }
            }
        }

        return result;
    }
}
