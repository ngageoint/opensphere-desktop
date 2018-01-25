package io.opensphere.myplaces.specific;

/**
 * Interface to a listener that is notified when a transformer has been opened
 * by the system.
 */
@FunctionalInterface
public interface OpenListener
{
    /**
     * Called when a transformer has been opened.
     *
     * @param renderer The renderer who was opened.
     */
    void opened(Renderer renderer);
}
