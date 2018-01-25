package io.opensphere.tracktool.model.persist.v1;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;
import io.opensphere.tracktool.model.Track;
import io.opensphere.tracktool.model.TrackConverter;

/**
 * The Class JAXBTrackConfig.
 */
@XmlRootElement(name = "TrackConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBTrackConfig
{
    /** The Track list. */
    @XmlElement(name = "Track")
    private final List<JAXBTrack> myTrackList;

    /**
     * Instantiates a new jAXB track config.
     */
    public JAXBTrackConfig()
    {
        myTrackList = New.list();
    }

    /**
     * Instantiates a new jAXB track config.
     *
     * @param tracks the tracks
     */
    public JAXBTrackConfig(Collection<? extends Track> tracks)
    {
        myTrackList = tracks == null || tracks.isEmpty() ? New.<JAXBTrack>list() : New.<JAXBTrack>list(tracks.size());
        if (tracks != null)
        {
            for (Track t : tracks)
            {
                myTrackList.add(new JAXBTrack(t));
            }
            Collections.sort(myTrackList, new Track.CompareByNameIgnoreCase());
        }
    }

    /**
     * Gets the track list.
     *
     * @return the track list
     */
    public List<JAXBTrack> getTrackList()
    {
        return myTrackList;
    }

    /**
     * Gets a new list of converted tracks converted according to the provided
     * converter. If the converter is null a new list containing the underlying
     *
     * @param converter the converter
     * @return the track list {@link JAXBTrack} is provided.
     */
    public List<Track> getTrackList(TrackConverter converter)
    {
        List<Track> converted = myTrackList == null || myTrackList.isEmpty() ? New.<Track>list()
                : New.<Track>list(myTrackList.size());
        if (myTrackList != null)
        {
            for (JAXBTrack t : myTrackList)
            {
                if (converter != null)
                {
                    converted.add(converter.convert(t));
                }
                else
                {
                    converted.add(t);
                }
            }
        }
        return converted;
    }
}
