package io.opensphere.mantle.data;

import java.io.Serializable;
import java.util.List;

import io.opensphere.core.model.time.TimeSpan;

/**
 * Stores a list of {@link TimeSpan}.
 */
public interface TimeExtents extends Serializable
{
    /**
     * Gets the smallest {@link TimeSpan} that wholly encloses the this class's
     * collection of {@link TimeSpan}s.
     *
     * @return the overall time extent
     */
    TimeSpan getExtent();

    /**
     * Gets the list of {@link TimeSpan}.
     *
     * @return the TimeSpan list
     */
    List<TimeSpan> getTimespans();
}
