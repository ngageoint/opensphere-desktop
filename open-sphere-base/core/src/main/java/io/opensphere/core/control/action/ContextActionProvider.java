package io.opensphere.core.control.action;

/**
 * Interface for providers of actions for a context.
 *
 * @param <T> The context menu key type.
 */
@FunctionalInterface
public interface ContextActionProvider<T>
{
    /**
     * Do the action which this provider performs.
     *
     * @param contextId the context for the menus.
     * @param key Key for which the action will be performed.
     * @param x The x position of the mouse event.
     * @param y The y position of the mouse event.
     * @return {@code true} if this provider accepted the action.
     */
    boolean doAction(String contextId, T key, int x, int y);
}
