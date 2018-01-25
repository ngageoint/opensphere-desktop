package io.opensphere.mantle.data.geom;

import io.opensphere.core.model.LatLonAlt;

/**
 * MapGeometrySupport for a single location based geometry.
 */
public interface MapLocationGeometrySupport extends MapGeometrySupport
{
    /**
     * Gets the location.
     *
     * @return the {@link LatLonAlt}
     */
    LatLonAlt getLocation();

    /**
     * Sets the location.
     *
     * @param loc - the location to set
     */
    void setLocation(LatLonAlt loc);
}
