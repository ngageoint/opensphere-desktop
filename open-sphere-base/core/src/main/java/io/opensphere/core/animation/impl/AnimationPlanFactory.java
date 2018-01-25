package io.opensphere.core.animation.impl;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.animation.AnimationPlan.EndBehavior;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.collections.New;

/**
 * Creates animation plans.
 */
public class AnimationPlanFactory
{
    /**
     * Creates a {@link DefaultAnimationPlan}.
     *
     * @param loopSpan the loop span
     * @param frameDuration the active span duration (which equals the advance
     *            duration)
     * @param skippedSpans the skipped time spans
     * @return the animation plan
     */
    public DefaultAnimationPlan createDefaultAnimationPlan(TimeSpan loopSpan, Duration frameDuration,
            Collection<? extends TimeSpan> skippedSpans)
    {
        List<TimeSpan> sequence = New.list();
        TimeSpan ts = TimeSpan.get(loopSpan.getStartInstant(), frameDuration);
        do
        {
            if (!ts.overlaps(skippedSpans))
            {
                sequence.add(ts);
            }
            ts = TimeSpan.get(ts.getEndInstant(), frameDuration);
        }
        while (ts.getStartInstant().compareTo(loopSpan.getEndInstant()) < 0);

        return new DefaultAnimationPlan(sequence, EndBehavior.WRAP);
    }

    /**
     * Creates a {@link DefaultContinuousAnimationPlan}.
     *
     * @param loopSpan the loop span
     * @param sequenceSpan the sequence span
     * @param activeDuration the active span duration
     * @param advanceDuration the advance duration
     * @param skippedSpans the skipped time spans
     * @return the animation plan
     */
    public DefaultContinuousAnimationPlan createDefaultContinuousAnimationPlan(TimeSpan loopSpan, TimeSpan sequenceSpan,
            Duration activeDuration, Duration advanceDuration, Collection<? extends TimeSpan> skippedSpans)
    {
        List<TimeSpan> sequence = New.list(sequenceSpan.subtract(skippedSpans));

        // Remove portions of the sequence that are too short.
        for (int index = sequence.size() - 1; index >= 0; --index)
        {
            TimeSpan part = sequence.get(index);
            if (part.getStartInstant().plus(activeDuration).isAfter(part.getEndInstant()))
            {
                sequence.remove(index);
            }
        }

        return new DefaultContinuousAnimationPlan(sequence, activeDuration, advanceDuration, EndBehavior.WRAP, loopSpan);
    }
}
