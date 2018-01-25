package io.opensphere.overlay;

import io.opensphere.core.Toolbox;

/**
 * The Class OverlayToolboxUtils.
 */
public final class OverlayToolboxUtils
{
    /**
     * Gets the overlay toolbox.
     *
     * @param tb the tb
     * @return the overlay toolbox
     */
    public static OverlayToolbox getOverlayToolbox(Toolbox tb)
    {
        return tb.getPluginToolboxRegistry().getPluginToolbox(OverlayToolbox.class);
    }

    /**
     * Instantiates a new OverlayToolboxUtils.
     */
    private OverlayToolboxUtils()
    {
        // Nothing here.
    }
}
