package io.opensphere.core.util.swing.input.model;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.duration.Duration;

/**
 * A model for a time instant.
 */
public class TimeInstantModel extends AbstractViewModel<TimeInstant>
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Adds the duration to the model.
     *
     * @param duration the duration
     */
    public void plus(Duration duration)
    {
        set(get().plus(duration));
    }

    /**
     * Subtracts the duration from the model.
     *
     * @param duration the duration
     */
    public void minus(Duration duration)
    {
        set(get().minus(duration));
    }
}
