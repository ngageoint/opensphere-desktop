package io.opensphere.overlay;

import io.opensphere.core.PluginToolbox;

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

    ControlsLayoutManager getControlsLayoutManager();
}
