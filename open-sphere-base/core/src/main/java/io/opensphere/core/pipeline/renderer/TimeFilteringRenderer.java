package io.opensphere.core.pipeline.renderer;

import io.opensphere.core.TimeManager;
import io.opensphere.core.model.time.TimeSpan;

/**
 * Interface for a renderer that can do time filtering.
 */
public interface TimeFilteringRenderer
{
    /**
     * Set the time span that overlaps all possible geometry time spans.
     *
     * @param span The group interval.
     * @return {@code true} if the interval was changed.
     */
    boolean setGroupInterval(TimeSpan span);

    /**
     * Set the time manager.
     *
     * @param timeManager The time manager.
     */
    void setTimeManager(TimeManager timeManager);
}
