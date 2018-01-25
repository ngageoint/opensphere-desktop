package io.opensphere.xyztile.transformer;

import io.opensphere.xyztile.model.XYZDataTypeInfo;

/**
 * Interface to an object that wants notification when a layer has
 * become active or deactive.
 */
public interface LayerActivationListener
{
    /**
     * Called when a new tile layer has been activated.
     *
     * @param layer The activated tile layer.
     */
    void layerActivated(XYZDataTypeInfo layer);

    /**
     * Called when a tile layer has been deactivated and should not
     * be shown on the globe.
     *
     * @param layer The deactivated layer.
     */
    void layerDeactivated(XYZDataTypeInfo layer);
}
