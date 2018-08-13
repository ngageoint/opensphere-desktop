package io.opensphere.overlay;

import io.opensphere.core.PluginToolbox;
import io.opensphere.overlay.query.QueryActionManager;

/**
 * The Interface for the toolbox for the OverlayPlugin.
 */
public interface OverlayToolbox extends PluginToolbox
{
    /**
     * Gets the selection mode controller.
     *
     * @return the selection mode controller
     */
    SelectionModeController getSelectionModeController();

    /**
     * Gets the query action manager.
     * 
     * @return the query action manager.
     */
    QueryActionManager getQueryActionManager();
}
