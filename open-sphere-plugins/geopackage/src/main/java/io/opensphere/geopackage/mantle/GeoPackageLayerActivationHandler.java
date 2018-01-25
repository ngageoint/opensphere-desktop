package io.opensphere.geopackage.mantle;

import java.awt.Color;

import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.LayerType;
import io.opensphere.mantle.controller.event.impl.DataTypeRemovedEvent;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;

/**
 * Listens for when geopackage layers have been activated and notifies a
 * {@link LayerActivationListener} of the active layer.
 */
public class GeoPackageLayerActivationHandler extends AbstractActivationListener
{
    /**
     * The color changed listener.
     */
    private final EventListener<DataTypeInfoColorChangeEvent> myColorListener = this::handleColorChanged;

    /**
     * The event manager.
     */
    private final EventManager myEventManager;

    /**
     * The layer activation listener for feature layers.
     */
    private final LayerActivationListener myFeatureLayerListener;

    /**
     * The removed listener.
     */
    private final EventListener<DataTypeRemovedEvent> myRemovedListener = this::handleRemoved;

    /**
     * The listener wanting notification when a geopackage tile layer is
     * activated.
     */
    private final LayerActivationListener myTileListener;

    /**
     * The visibility listener.
     */
    private final EventListener<DataTypeVisibilityChangeEvent> myVisibilityListener = this::handleVisibilityChanged;

    /**
     * Constructs a new layer provider.
     *
     * @param eventManager The event manager.
     * @param tileLayerListener The listener wanting notification when a
     *            geopackage tile layer is activated.
     * @param featureLayerListener The layer activation listener for feature
     *            layers.
     */
    public GeoPackageLayerActivationHandler(EventManager eventManager, LayerActivationListener tileLayerListener,
            LayerActivationListener featureLayerListener)
    {
        myTileListener = tileLayerListener;
        myEventManager = eventManager;
        myEventManager.subscribe(DataTypeVisibilityChangeEvent.class, myVisibilityListener);
        myEventManager.subscribe(DataTypeInfoColorChangeEvent.class, myColorListener);
        myEventManager.subscribe(DataTypeRemovedEvent.class, myRemovedListener);
        myFeatureLayerListener = featureLayerListener;
    }

    /**
     * Unsubscribes from the event manager.
     */
    public void close()
    {
        myEventManager.unsubscribe(DataTypeVisibilityChangeEvent.class, myVisibilityListener);
        myEventManager.unsubscribe(DataTypeInfoColorChangeEvent.class, myColorListener);
        myEventManager.unsubscribe(DataTypeRemovedEvent.class, myRemovedListener);
    }

    /**
     * Gets the tile layer listener.
     *
     * @return The new tile layer listener.
     */
    public LayerActivationListener getTileLayerListener()
    {
        return myTileListener;
    }

    @Override
    public void handleCommit(boolean active, DataGroupInfo dgi, PhasedTaskCanceller canceller)
    {
        super.handleCommit(active, dgi, canceller);
        if (dgi == null)
        {
            return;
        }
        for (DataTypeInfo info : dgi.getMembers(false))
        {
            if (info instanceof GeoPackageDataTypeInfo)
            {
                GeoPackageDataTypeInfo geopackageInfo = (GeoPackageDataTypeInfo)info;
                GeoPackageLayer layer = geopackageInfo.getLayer();
                if (layer.getLayerType() == LayerType.TILE)
                {
                    if (active)
                    {
                        myTileListener.layerActivated(geopackageInfo);
                    }
                    else
                    {
                        myTileListener.layerDeactivated(geopackageInfo);
                    }
                }
                else if (layer.getLayerType() == LayerType.FEATURE && canceller != null)
                {
                    if (active)
                    {
                        myFeatureLayerListener.layerActivated(geopackageInfo);
                    }
                    else
                    {
                        myFeatureLayerListener.layerDeactivated(geopackageInfo);
                    }
                }
            }
        }
    }

    /**
     * Handles when the color has changed at sets the new opacity value on the
     * {@link TileRenderProperties}.
     *
     * @param event The color changed event.
     */
    private void handleColorChanged(DataTypeInfoColorChangeEvent event)
    {
        if (event.getDataTypeInfo() instanceof GeoPackageDataTypeInfo
                && ((GeoPackageDataTypeInfo)event.getDataTypeInfo()).getLayer().getLayerType() == LayerType.TILE)
        {
            Color color = event.getColor();
            event.getDataTypeInfo().getMapVisualizationInfo().getTileRenderProperties()
                    .setOpacity((float)color.getAlpha() / ColorUtilities.COLOR_COMPONENT_MAX_VALUE);
        }
    }

    /**
     * Handles removed changes and notifies the activation listener.
     *
     * @param event The event.
     */
    private void handleRemoved(DataTypeRemovedEvent event)
    {
        handleCommit(false, event.getDataType().getParent(), null);
    }

    /**
     * Handles visibility changes and notifies the activation listener.
     *
     * @param event The event.
     */
    private void handleVisibilityChanged(DataTypeVisibilityChangeEvent event)
    {
        handleCommit(event.isVisible(), event.getDataTypeInfo().getParent(), null);
    }
}
