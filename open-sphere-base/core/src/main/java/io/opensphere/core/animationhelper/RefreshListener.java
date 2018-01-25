package io.opensphere.core.animationhelper;

/**
 * Interface to an object wanting to be notified when geometries should be added
 * or removed as well as when those geometries images should be refreshed.
 */
public interface RefreshListener
{
    /**
     * Called when new data should be retrieved.
     *
     * @param forceIt True if the data should be refreshed regardless of the
     *            time on the timeline.
     */
    void refresh(boolean forceIt);

    /**
     * Called when new data should be retrieved. This call must return once all
     * data is retrieved.
     */
    void refreshNow();
}
