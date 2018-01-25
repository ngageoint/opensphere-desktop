package io.opensphere.core.util;

/** Interface for something that can be shown/hidden. */
public interface Showable
{
    /**
     * Sets the visibility of the component.
     *
     * @param visible true to become visible, false to become hidden
     */
    void setVisible(boolean visible);

    /**
     * Returns whether the component is visible.
     *
     * @return whether the component is visible
     */
    boolean isVisible();
}
