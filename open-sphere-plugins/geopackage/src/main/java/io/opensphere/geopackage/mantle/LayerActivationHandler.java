package io.opensphere.geopackage.mantle;

import java.io.File;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.registry.GenericRegistry;
import io.opensphere.geopackage.envoy.GeoPackageImageEnvoy;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.manager.GeoPackageManager;

/**
 * Handles when a geopackage tile layer is activated and initializes the
 * necessary objects so that tile layer can be viewed on the map.
 */
public class LayerActivationHandler implements LayerActivationListener
{
    /**
     * The system envoy registry.
     */
    private final GenericRegistry<Envoy> myEnvoyRegistry;

    /**
     * The {@link LayerActivationListener} that needs to know about tile layer
     * activate/deactivate state.
     */
    private final LayerActivationListener myTileActivationListener;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new activation handler.
     *
     * @param toolbox The system toolbox.
     * @param tileActivationListener The {@link LayerActivationListener} that
     *            needs to know about tile layer activate/deactivate state.
     */
    public LayerActivationHandler(Toolbox toolbox, LayerActivationListener tileActivationListener)
    {
        myToolbox = toolbox;
        myEnvoyRegistry = toolbox.getEnvoyRegistry();
        myTileActivationListener = tileActivationListener;
    }

    @Override
    public void layerActivated(GeoPackageDataTypeInfo layer)
    {
        String packageFile = layer.getLayer().getPackageFile();
        GeoPackage geoPackage = GeoPackageManager.open(new File(packageFile));
        GeoPackageImageEnvoy envoy = new GeoPackageImageEnvoy(myToolbox, geoPackage);

        myEnvoyRegistry.addObjectsForSource(packageFile, New.list(envoy));
        myTileActivationListener.layerActivated(layer);
    }

    @Override
    public void layerDeactivated(GeoPackageDataTypeInfo layer)
    {
        myTileActivationListener.layerDeactivated(layer);
        myEnvoyRegistry.removeObjectsForSource(layer.getLayer().getPackageFile());
    }
}
