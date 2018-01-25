package io.opensphere.wms.capabilities;

import io.opensphere.core.model.time.TimeSpan;

/**
 * Interface for layers within a WMS Capabilities document.
 */
public interface WMSCapsLayer
{
    /**
     * Gets the layer name. This will generally be unique among layers on a
     * given server, but uniqueness is not expressly guaranteed by the spec.
     *
     * @return the layer name
     */
    String getName();

    /**
     * Gets the time extent of the layer as a {@link TimeSpan}. If the layer
     * does not specify a time extent, this will return TimeSpan.TIMELESS.
     *
     * @return the layer's time extent
     */
    TimeSpan getTimeExtent();

    /**
     * Gets the layer title. This is a brief, human-readable name, generally
     * used for display purposes when referencing the layer.
     *
     * @return the layer title
     */
    String getTitle();
}
