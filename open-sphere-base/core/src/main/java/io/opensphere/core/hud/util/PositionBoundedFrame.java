package io.opensphere.core.hud.util;

import java.awt.Rectangle;

/**
 * Interface for frames whose position can be bounded using a
 * {@link FrameBoundsHelper}.
 */
public interface PositionBoundedFrame
{
    /**
     * Get the bounds of the frame.
     *
     * @return the current bounds.
     */
    Rectangle getBounds();

    /** Reposition a frame due to a change in the inset distance. */
    void repositionForInsets();
}
