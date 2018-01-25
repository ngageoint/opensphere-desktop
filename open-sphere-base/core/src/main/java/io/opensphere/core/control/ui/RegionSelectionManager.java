package io.opensphere.core.control.ui;

import io.opensphere.core.util.SelectionMode;

/**
 * Interface for the manager of the selection region. There should only be one
 * of these at a time.
 */
public interface RegionSelectionManager
{
    /**
     * Relinquish the usurpation.
     *
     * @param usurpationContext The context which was usurping the default
     *            context.
     */
    void relinquishRegionContext(String usurpationContext);

    /**
     * Usurp the context for new regions and replace it with the usurpation
     * context.
     *
     * @param usurpationContext The context which replaces the default.
     * @param mode The selection mode to use during usurpation.
     */
    void usurpRegionContext(String usurpationContext, SelectionMode mode);
}
