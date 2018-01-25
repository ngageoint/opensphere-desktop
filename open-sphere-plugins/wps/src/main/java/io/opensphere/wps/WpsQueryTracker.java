package io.opensphere.wps;

import io.opensphere.core.util.taskactivity.CancellableTaskActivity;

/**
 * A tracker used to monitor a single WPS query.
 */
public class WpsQueryTracker extends CancellableTaskActivity
{
    /**
     * creates a new tracker.
     */
    public WpsQueryTracker()
    {
        super();
    }
}
