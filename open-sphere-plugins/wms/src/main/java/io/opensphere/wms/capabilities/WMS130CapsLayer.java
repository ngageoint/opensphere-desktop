package io.opensphere.wms.capabilities;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import net.opengis.wms_130.Dimension;
import net.opengis.wms_130.Layer;

/**
 * A WMS 1.3.0 implementation of {@link WMSCapsLayer}.
 */
public class WMS130CapsLayer implements WMSCapsLayer
{
    /** The capabilities layer that this class wraps. */
    private final Layer myLayer;

    /**
     * Instantiates a new WMS 1.3.0 capabilities layer.
     *
     * @param layer the WMS v1.3.0 layer from the Capabilities document.
     */
    public WMS130CapsLayer(Layer layer)
    {
        myLayer = Utilities.checkNull(layer, "layer");
    }

    @Override
    public String getName()
    {
        return myLayer.getName();
    }

    @Override
    public TimeSpan getTimeExtent()
    {
        for (Dimension extent : myLayer.getDimension())
        {
            if (extent.getName().equalsIgnoreCase("time"))
            {
                try
                {
                    return WMSTimeUtils.parseISOTimeExtent(extent.getValue());
                }
                catch (IllegalArgumentException e)
                {
                    return TimeSpan.TIMELESS;
                }
            }
        }
        return TimeSpan.TIMELESS;
    }

    @Override
    public String getTitle()
    {
        return myLayer.getTitle();
    }
}
