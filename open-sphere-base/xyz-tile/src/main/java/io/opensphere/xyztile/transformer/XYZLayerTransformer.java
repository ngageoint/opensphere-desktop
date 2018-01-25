package io.opensphere.xyztile.transformer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.xyztile.model.XYZDataTypeInfo;

/**
 * The transformer that publishes the necessary geometries for Mapbox tile
 * layers.
 */
public class XYZLayerTransformer extends DefaultTransformer implements LayerActivationListener
{
    /**
     * The color listener.
     */
    private final EventListener<DataTypeInfoColorChangeEvent> myColorListener = this::handleDataTypeInfoColorChange;

    /** The event manager. */
    private final EventManager myEventManager;

    /**
     * The geometries for all active xyz tile layers.
     */
    private final Map<String, List<TileGeometry>> myGeometries = Collections.synchronizedMap(New.map());

    /**
     * Builds the initial set of geometries.
     */
    private final XYZGeometryBuilder myGeometryBuilder;

    /**
     * Handles all the different layer events that would cause us to render or
     * not render a layer's tile data.
     */
    private final LayerActivationHandler myHandler;

    /**
     * The max zoom observers that listen for any user changes to the max zoom
     * level, and then refreshed the tiles if changes occur.
     */
    private final Map<String, XYZMaxZoomObserver> myMaxZoomObservers = Collections.synchronizedMap(New.map());

    /**
     * The list of waiting layer activations.
     */
    private final List<XYZDataTypeInfo> myPendingActivations = New.list();

    /**
     * Constructs a new transformer for geopackage tile layers.
     *
     * @param dataRegistry The data registry.
     * @param uiRegistry The system's ui registry.
     * @param eventManager Used to listen for layer activation events.
     */
    public XYZLayerTransformer(DataRegistry dataRegistry, UIRegistry uiRegistry, EventManager eventManager)
    {
        super(dataRegistry);
        myEventManager = eventManager;
        myGeometryBuilder = new XYZGeometryBuilder(dataRegistry, uiRegistry);
        myHandler = new LayerActivationHandler(eventManager, this);
    }

    @Override
    public void close()
    {
        myHandler.close();
        myEventManager.unsubscribe(DataTypeInfoColorChangeEvent.class, myColorListener);
        super.close();
    }

    @Override
    public synchronized void layerActivated(XYZDataTypeInfo layer)
    {
        if (isOpen() && !myGeometries.containsKey(layer.getTypeKey()))
        {
            TileRenderProperties props = layer.getMapVisualizationInfo().getTileRenderProperties();
            List<TileGeometry> initialGeometries = myGeometryBuilder.buildTopGeometry(layer.getLayerInfo(), layer.getTypeKey(),
                    props);

            myGeometries.put(layer.getTypeKey(), initialGeometries);
            publishGeometries(initialGeometries, New.collection());
            myMaxZoomObservers.put(layer.getTypeKey(), new XYZMaxZoomObserver(layer, this));
        }
        else
        {
            myPendingActivations.add(layer);
        }
    }

    @Override
    public void layerDeactivated(XYZDataTypeInfo layer)
    {
        List<TileGeometry> geometry = myGeometries.remove(layer.getTypeKey());
        if (geometry != null)
        {
            publishGeometries(New.collection(), geometry);
            XYZMaxZoomObserver observer = myMaxZoomObservers.remove(layer.getTypeKey());
            if (observer != null)
            {
                observer.close();
            }
        }
    }

    @Override
    public synchronized void open()
    {
        super.open();
        myEventManager.subscribe(DataTypeInfoColorChangeEvent.class, myColorListener);
        for (XYZDataTypeInfo layer : myPendingActivations)
        {
            layerActivated(layer);
        }
    }

    /**
     * Handles a DataTypeInfoColorChangeEvent.
     *
     * @param event the event
     */
    private void handleDataTypeInfoColorChange(DataTypeInfoColorChangeEvent event)
    {
        DataTypeInfo dataType = event.getDataTypeInfo();
        if (dataType instanceof XYZDataTypeInfo)
        {
            int alpha = event.getColor().getAlpha();
            ThreadUtilities.runBackground(() -> setOpacity(dataType, alpha));
        }
    }

    /**
     * Sets the opacity of the data type's geometries.
     *
     * @param dataType the data type
     * @param opacity the opacity
     */
    private void setOpacity(DataTypeInfo dataType, int opacity)
    {
        List<TileGeometry> geometries = myGeometries.get(dataType.getTypeKey());
        if (geometries != null)
        {
            float fractionalOpacity = (float)opacity / ColorUtilities.COLOR_COMPONENT_MAX_VALUE;
            for (TileGeometry geometry : geometries)
            {
                geometry.getRenderProperties().setOpacity(fractionalOpacity);
            }
        }
    }
}
