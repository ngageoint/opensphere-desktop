package io.opensphere.controlpanels.layers.base;

import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;

/**
 * The listener interface for receiving notifications.
 */
public interface DiscoveryDataLayerChangeListener
{
    /**
     * data groups changed.
     */
    void dataGroupsChanged();

    /**
     * Data group visibility changed.
     *
     * @param event the event
     */
    void dataGroupVisibilityChanged(DataTypeVisibilityChangeEvent event);

    /**
     * Refresh tree label request.
     */
    void refreshTreeLabelRequest();

    /**
     * Repaint request.
     */
    void treeRepaintRequest();
}
