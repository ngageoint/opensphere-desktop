package io.opensphere.geopackage.mantle;

/**
 * Interface to an object that wants notification when a GeoPackage layer has
 * become active or deactive.
 */
public interface LayerActivationListener
{
    /**
     * Called when a new geo package tile layer has been activated.
     *
     * @param layer The activated tile layer.
     */
    void layerActivated(GeoPackageDataTypeInfo layer);

    /**
     * Called when a geo package tile layer has been deactivated and should not
     * be shown on the globe.
     *
     * @param layer The deactivated layer.
     */
    void layerDeactivated(GeoPackageDataTypeInfo layer);
}
