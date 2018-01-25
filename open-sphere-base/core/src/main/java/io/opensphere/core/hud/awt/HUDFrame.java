package io.opensphere.core.hud.awt;

/**
 * Interface for HUD frames. Implementors of this interface should be handled
 * through the InternalComponentRegistry.
 *
 * TODO There should be some mechanism for modifying opacity or other properties
 * for the frame. This should be kept separate from the RenderProperties, but
 * serve the same purpose.
 */
public interface HUDFrame
{
    /**
     * Get The title which identifies this frame.
     *
     * @return The frame's title.
     */
    String getTitle();

    /**
     * Determine whether the frame is visible.
     *
     * @return true when the frame is visible.
     */
    boolean isVisible();

    /**
     * Set the frame to visible or invisible.
     *
     * @param visible When true the frame will be visible, when false the frame
     *            will be invisible.
     */
    void setVisible(boolean visible);
}
