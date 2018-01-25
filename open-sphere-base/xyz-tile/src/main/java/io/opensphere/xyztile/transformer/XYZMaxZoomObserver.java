package io.opensphere.xyztile.transformer;

import java.io.Closeable;
import java.util.Observable;
import java.util.Observer;

import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.xyztile.model.XYZDataTypeInfo;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Class that responds to max zoom level changes and refreshed the tiles on the
 * globe to reflect those changes.
 */
public class XYZMaxZoomObserver implements Observer, Closeable
{
    /**
     * Knows how to remove and add tiles to the globe.
     */
    private final LayerActivationListener myActivationHandler;

    /**
     * The layer we are watching.
     */
    private final XYZDataTypeInfo myLayer;

    /**
     * Constructs a new observer.
     *
     * @param layer the layer to watch.
     * @param activationHandler Knows how to remove and add tiles to the map.
     */
    public XYZMaxZoomObserver(XYZDataTypeInfo layer, LayerActivationListener activationHandler)
    {
        myActivationHandler = activationHandler;
        myLayer = layer;
        myLayer.getLayerInfo().addObserver(this);
    }

    @Override
    public void close()
    {
        myLayer.getLayerInfo().deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (XYZTileLayerInfo.MAX_LEVELS_PROP.equals(arg))
        {
            ThreadUtilities.runCpu(() ->
            {
                myActivationHandler.layerDeactivated(myLayer);
                myActivationHandler.layerActivated(myLayer);
            });
        }
    }
}
