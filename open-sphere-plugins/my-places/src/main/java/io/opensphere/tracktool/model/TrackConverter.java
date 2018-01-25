package io.opensphere.tracktool.model;

/**
 * The Interface TrackConvertor.
 */
@FunctionalInterface
public interface TrackConverter
{
    /**
     * Converts the track in to another type of track.
     *
     * @param in the Track in.
     * @return the converted track.
     */
    Track convert(Track in);
}
