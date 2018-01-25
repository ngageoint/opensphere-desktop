package io.opensphere.core.hud.awt;

/** Interface for handlers of bounds changes for internal frames. */
@FunctionalInterface
public interface HUDFrameBoundsHandler
{
    /**
     * Notification that the bounds have been set for the frame.
     *
     * @param x The x coordinate of the upper left corner of the frame.
     * @param y The y coordinate of the upper left corner of the frame.
     * @param width The width of the frame.
     * @param height The height of the frame.
     */
    void boundsSet(int x, int y, int width, int height);
}
