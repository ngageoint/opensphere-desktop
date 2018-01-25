package io.opensphere.tracktool.model.impl;

import io.opensphere.tracktool.model.Track;
import io.opensphere.tracktool.model.TrackConverter;

/**
 * The Class DefaultTrackConverter.
 */
public class DefaultTrackConverter implements TrackConverter
{
    @Override
    public Track convert(Track in)
    {
        return new DefaultTrack(in);
    }
}
