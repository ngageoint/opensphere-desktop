package io.opensphere.core.control.action;

/**
 * Interface for providers of single actions for a context.
 *
 * @param <T> The context menu key type.
 */
public interface ContextSingleActionProvider<T>
{
    /**
     * Do the action which this provider performs.
     *
     * @param contextId the context for the menus.
     * @param key Key for which the action will be performed.
     * @param x The x position of the mouse event.
     * @param y The y position of the mouse event.
     */
    void doAction(String contextId, T key, int x, int y);

    /**
     * Notify this provider that it is no longer valid and will not be called
     * when the action associated with the context is executed.
     */
    void invalidated();
}
