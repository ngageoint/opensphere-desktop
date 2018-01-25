package io.opensphere.mantle.data.element;

import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * A DataElement meant for display on the map.
 */
public interface MapDataElement extends DataElement
{
    /**
     * Gets the {@link MapGeometrySupport} for this MapDataElement.
     *
     * @return the {@link MapGeometrySupport}
     */
    MapGeometrySupport getMapGeometrySupport();

    /**
     * Sets the {@link MapGeometrySupport} for this MapDataElement.
     *
     * @param mgs - the map geometry support.
     */
    void setMapGeometrySupport(MapGeometrySupport mgs);
}
