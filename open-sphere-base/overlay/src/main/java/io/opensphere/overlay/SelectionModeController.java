package io.opensphere.overlay;

import io.opensphere.core.util.SelectionMode;

/**
 * The Interface SelectionModeController.
 */
public interface SelectionModeController
{
    /**
     * Adds the selection mode change listener.
     *
     * @param listener the listener
     */
    void addSelectionModeChangeListener(SelectionModeChangeListener listener);

    /**
     * Gets the default selection mode. The default selection mode is always the
     * last selected mode, so it will match the selection mode whenever the
     * selection mode is not NONE.
     *
     * @return the default selection mode
     */
    SelectionMode getDefaultSelectionMode();

    /**
     * Gets the selection mode.
     *
     * @return the selection mode
     */
    SelectionMode getSelectionMode();

    /**
     * Removes the selection mode change listener.
     *
     * @param listener the listener
     */
    void removeSelectionModeChangeListener(SelectionModeChangeListener listener);

    /**
     * Sets the default selection mode.
     *
     * @param mode the mode
     */
    void setSelectionMode(SelectionMode mode);
}
