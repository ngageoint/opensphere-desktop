package io.opensphere.geopackage.transformer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.mantle.GeoPackageDataTypeInfo;
import io.opensphere.geopackage.mantle.LayerActivationListener;
import io.opensphere.geopackage.model.GeoPackageTileLayer;
import io.opensphere.mantle.data.impl.DefaultTileLevelController;

/**
 * This class will put geopackage tiles on the map for all active
 * {@link GeoPackageTileLayer}s.
 */
public class GeoPackageLayerTransformer extends DefaultTransformer implements LayerActivationListener
{
    /**
     * The geometries for all active geopackage tile layers.
     */
    private final Map<String, List<AbstractTileGeometry<?>>> myGeometries = Collections.synchronizedMap(New.map());

    /**
     * Builds the initial set of geometries.
     */
    private final GeoPackageGeometryBuilder myGeometryBuilder;

    /**
     * The list of waiting layer activations.
     */
    private final List<GeoPackageDataTypeInfo> myPendingActivations = New.list();

    /**
     * Constructs a new transformer for geopackage tile layers.
     *
     * @param dataRegistry The data registry.
     * @param uiRegistry The system's ui registry.
     */
    public GeoPackageLayerTransformer(DataRegistry dataRegistry, UIRegistry uiRegistry)
    {
        super(dataRegistry);
        myGeometryBuilder = new GeoPackageGeometryBuilder(dataRegistry, uiRegistry);
    }

    @Override
    public synchronized void layerActivated(GeoPackageDataTypeInfo layer)
    {
        if (isOpen())
        {
            GeoPackageTileLayer tileLayer = (GeoPackageTileLayer)layer.getLayer();
            TileRenderProperties props = layer.getMapVisualizationInfo().getTileRenderProperties();
            GeoPackageDivider divider = new GeoPackageDivider(tileLayer);
            List<AbstractTileGeometry<?>> initialGeometries = myGeometryBuilder.buildGeometries(tileLayer,
                    tileLayer.getMinZoomLevel(), props, divider);
            setGeographicExtents(layer, initialGeometries);
            if (layer.getMapVisualizationInfo().getTileLevelController() instanceof DefaultTileLevelController)
            {
                DefaultTileLevelController tileLevelController = (DefaultTileLevelController)layer.getMapVisualizationInfo()
                        .getTileLevelController();
                tileLevelController.setMaxGeneration((int)(tileLayer.getMaxZoomLevel() - tileLayer.getMinZoomLevel()));
                tileLevelController.addDivider(divider);
                tileLevelController.addTileGeometries(initialGeometries);
            }

            if (myGeometries.putIfAbsent(layer.getTypeKey(), initialGeometries) != null)
            {
                myGeometries.get(layer.getTypeKey()).addAll(initialGeometries);
            }

            publishGeometries(initialGeometries, New.collection());
        }
        else
        {
            myPendingActivations.add(layer);
        }
    }

    @Override
    public void layerDeactivated(GeoPackageDataTypeInfo layer)
    {
        List<AbstractTileGeometry<?>> geometries = myGeometries.remove(layer.getTypeKey());
        if (geometries != null && !geometries.isEmpty())
        {
            publishGeometries(New.collection(), geometries);
        }
    }

    @Override
    public synchronized void open()
    {
        super.open();
        for (GeoPackageDataTypeInfo layer : myPendingActivations)
        {
            layerActivated(layer);
        }
    }

    /**
     * Sets the geographic position for the layer, so a user can double click on
     * it and fly to it.
     *
     * @param layer The layer to set the extents for.
     * @param initialGeometries The initial geometries and the minimum zoom
     *            level.
     */
    private void setGeographicExtents(GeoPackageDataTypeInfo layer, List<AbstractTileGeometry<?>> initialGeometries)
    {
        GeographicBoundingBox extents = null;
        for (AbstractTileGeometry<?> geometry : initialGeometries)
        {
            if (geometry.getBounds() instanceof GeographicBoundingBox)
            {
                if (extents == null)
                {
                    extents = (GeographicBoundingBox)geometry.getBounds();
                }
                else
                {
                    extents = (GeographicBoundingBox)extents.union((GeographicBoundingBox)geometry.getBounds());
                }
            }
        }

        if (extents != null)
        {
            layer.addBoundingBox(extents);
        }
    }
}
