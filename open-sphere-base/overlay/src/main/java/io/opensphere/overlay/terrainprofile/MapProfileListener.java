package io.opensphere.overlay.terrainprofile;

import java.util.List;

import io.opensphere.core.model.GeographicPosition;

/** Interface for listening for map profile updates. */
public interface MapProfileListener
{
    /** Remove the marker/highlight from map profile. */
    void removeMapMarker();

    /**
     * Update the marker/highlight for the map profile.
     *
     * @param markerEnds List of positions that define map profile marker.
     */
    void updateMapMarker(List<GeographicPosition> markerEnds);
}
