package io.opensphere.controlpanels.animation.view;

import java.math.RoundingMode;

import io.opensphere.controlpanels.timeline.SnapFunction;
import io.opensphere.core.model.time.TimeInstant;

/** A no-op snap function. */
class NoSnapSnapFunction implements SnapFunction
{
    @Override
    public TimeInstant getSnapDestination(TimeInstant time, RoundingMode mode)
    {
        return time;
    }
}
