package io.opensphere.controlpanels.timeline;

import java.math.RoundingMode;

import io.opensphere.core.model.time.TimeInstant;

/** Function for calculating a snap destination. */
@FunctionalInterface
public interface SnapFunction
{
    /**
     * Get the snap-to time closest to the input time.
     *
     * @param time The input time.
     * @param mode How to perform rounding. Implementations may ignore this
     *            parameter.
     * @return The rounded time.
     */
    TimeInstant getSnapDestination(TimeInstant time, RoundingMode mode);
}
