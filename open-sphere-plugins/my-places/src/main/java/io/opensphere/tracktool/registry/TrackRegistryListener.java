package io.opensphere.tracktool.registry;

import java.util.Collection;

import io.opensphere.tracktool.model.Track;

/**
 * Listener interface for the Track Registry.
 */
public interface TrackRegistryListener
{
    /**
     * Track added.
     *
     * @param tracks the track
     */
    void tracksAdded(Collection<Track> tracks);

    /**
     * Track removed.
     *
     * @param tracks the track
     */
    void tracksRemoved(Collection<Track> tracks);
}
