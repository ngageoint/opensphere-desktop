package io.opensphere.overlay;

import io.opensphere.core.util.SelectionMode;

/**
 * The listener interface for selection mode changes.
 *
 * @see SelectionMode
 */
@FunctionalInterface
public interface SelectionModeChangeListener
{
    /**
     * Default selection mode changed.
     *
     * @param mode the mode
     */
    void selectionModeChanged(SelectionMode mode);
}
