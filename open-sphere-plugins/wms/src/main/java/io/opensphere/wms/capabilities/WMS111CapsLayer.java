package io.opensphere.wms.capabilities;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import net.opengis.wms._111.Extent;
import net.opengis.wms._111.Layer;

/**
 * A WMS 1.1.1 implementation of {@link WMSCapsLayer}.
 */
public class WMS111CapsLayer implements WMSCapsLayer
{
    /** The capabilities layer that this class wraps. */
    private final Layer myLayer;

    /**
     * Instantiates a new WMS 1.1.1 capabilities layer.
     *
     * @param layer the WMS v1.1.1 layer from the Capabilities document.
     */
    public WMS111CapsLayer(Layer layer)
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
        for (Extent extent : myLayer.getExtent())
        {
            if (extent.getName().equalsIgnoreCase("time"))
            {
                try
                {
                    return WMSTimeUtils.parseISOTimeExtent(extent.getvalue());
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
