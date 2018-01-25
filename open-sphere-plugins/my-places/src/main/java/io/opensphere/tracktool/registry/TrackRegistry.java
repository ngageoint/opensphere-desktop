package io.opensphere.tracktool.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesEditListener;
import io.opensphere.tracktool.model.Track;
import io.opensphere.tracktool.model.TrackNode;
import io.opensphere.tracktool.model.impl.DefaultTrack;
import io.opensphere.tracktool.model.impl.DefaultTrackNode;
import io.opensphere.tracktool.util.TrackUtils;

/**
 * The Class TrackRegistryImpl.
 */
public final class TrackRegistry
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TrackRegistry.class);

    /** The Constant TRACK_CONFIG_PREFERENCES_KEY. */
    public static final String TRACK_CONFIG_PREFERENCES_KEY = "TrackRegistry";

    /** The Change support. */
    private final WeakChangeSupport<TrackRegistryListener> myChangeSupport = new WeakChangeSupport<>();

    /**
     * The track registry.
     */
    private static final TrackRegistry ourInstance = new TrackRegistry();

    /** The executor to use for notification of registrant changes. */
    private final ThreadPoolExecutor myExecutor = new ThreadPoolExecutor(1, 1, 5000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("TrackRegistryChangeSupport"));

    /** The Map lock. */
    private final ReentrantReadWriteLock myMapLock = new ReentrantReadWriteLock();

    /** The Track name to track map. */
    private final Map<String, Track> myTracks = New.map();

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static TrackRegistry getInstance()
    {
        return ourInstance;
    }

    /**
     * Constructor.
     *
     */
    private TrackRegistry()
    {
        myExecutor.allowCoreThreadTimeOut(true);
    }

    /**
     * Add a track to the registry.
     *
     * @param track The track to be added.
     * @param toolbox The toolbox.
     */
    public void add(Track track, Toolbox toolbox)
    {
        myMapLock.writeLock().lock();
        try
        {
            if (myTracks.get(track.getId()) != null)
            {
                LOGGER.error("Track already exists with name : " + track.getName());
                return;
            }
            addTrack(track, toolbox);
        }
        finally
        {
            myMapLock.writeLock().unlock();
        }
        notifyTracksAdded(Collections.singletonList(track));
    }

    /**
     * Adds the listener.
     *
     * @param listener the listener
     */
    public void addListener(TrackRegistryListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Creates and registers a new track, without showing the editor to the user.
     *
     * @param nodes the nodes which make up the track.
     * @param toolbox The toolbox.
     * @param rootDataGroup The root my places data group.
     * @param editListener Listens for when the user wants to edit the track.
     */
    public void createNewTrackFromNodes(List<TrackNode> nodes, Toolbox toolbox, MyPlacesDataGroupInfo rootDataGroup,
            MyPlacesEditListener editListener)
    {
        String defaultTrackName;
        for (int i = 0; i < Integer.MAX_VALUE; ++i)
        {
            defaultTrackName = "Track " + i;
            if (getTrack(defaultTrackName) == null)
            {
                break;
            }
        }

        DefaultTrack track = TrackUtils.createDefaultTrack(toolbox, UUID.randomUUID().toString(), "", nodes);

        Placemark placemark = TrackUtils.toKml(new Folder(), track);

        EventQueueUtilities.runOnEDT(() ->
        {
            editListener.launchEditor(placemark, rootDataGroup);
        });
    }

    /**
     * Creates and registers a new track.
     *
     * @param positions the positions which make up the track.
     * @param toolbox The toolbox.
     * @param rootDataGroup The root my places data group.
     * @param editListener Listens for when the user wants to edit tracks.
     */
    public void createNewTrackFromPositions(List<Pair<GeographicPosition, AbstractRenderableGeometry>> positions, Toolbox toolbox,
            MyPlacesDataGroupInfo rootDataGroup, MyPlacesEditListener editListener)
    {
        List<TrackNode> nodes = New.list(positions.size());
        for (Pair<GeographicPosition, AbstractRenderableGeometry> position : positions)
        {
            DefaultTrackNode node;
            if (position.getSecondObject() == null || position.getSecondObject().getConstraints().getTimeConstraint() == null)
            {
                node = new DefaultTrackNode(position.getFirstObject().getLatLonAlt());
            }
            else
            {
                node = new DefaultTrackNode(position.getFirstObject().getLatLonAlt(),
                        position.getSecondObject().getConstraints().getTimeConstraint().getTimeSpan());
            }
            nodes.add(node);
        }
        createNewTrackFromNodes(nodes, toolbox, rootDataGroup, editListener);
    }

    /**
     * Get the track whose name matches the given name.
     *
     * @param trackId Then name of the track which is desired.
     * @return The track with the given name or {@code null} if not available.
     */
    public Track getTrack(String trackId)
    {
        Track result = null;
        myMapLock.readLock().lock();
        try
        {
            result = myTracks.get(trackId);
        }
        finally
        {
            myMapLock.readLock().unlock();
        }
        return result;
    }

    /**
     * Get all of the registered tracks.
     *
     * @return all of the registered tracks.
     */
    public Collection<Track> getTracks()
    {
        Collection<Track> tracks = null;
        myMapLock.readLock().lock();
        try
        {
            tracks = New.collection(myTracks.values());
        }
        finally
        {
            myMapLock.readLock().unlock();
        }
        return tracks;
    }

    /**
     * Notify interested parties when track have been added.
     *
     * @param tracks the tracks
     */
    public void notifyTracksAdded(final Collection<Track> tracks)
    {
        myChangeSupport.notifyListeners(new Callback<TrackRegistryListener>()
        {
            @Override
            public void notify(TrackRegistryListener listener)
            {
                listener.tracksAdded(tracks);
            }
        }, myExecutor);
    }

    /**
     * Notify interested parties when track have been removed.
     *
     * @param tracks the tracks
     */
    public void notifyTracksRemoved(final Collection<Track> tracks)
    {
        myChangeSupport.notifyListeners(new Callback<TrackRegistryListener>()
        {
            @Override
            public void notify(TrackRegistryListener listener)
            {
                listener.tracksRemoved(tracks);
            }
        }, myExecutor);
    }

    /**
     * Remove tracks from the registry.
     *
     * @param tracks the tracks to be removed.
     */
    public void remove(Collection<Track> tracks)
    {
        Collection<Track> removed = New.collection(tracks.size());
        myMapLock.writeLock().lock();
        try
        {
            for (Track track : tracks)
            {
                if (myTracks.remove(track.getId()) != null)
                {
                    removed.add(track);
                }
            }
        }
        finally
        {
            myMapLock.writeLock().unlock();
        }

        if (!removed.isEmpty())
        {
            notifyTracksRemoved(removed);
        }
    }

    /**
     * Removes the listener.
     *
     * @param listener the listener
     */
    public void removeListener(TrackRegistryListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Put the track in the map and generate the data type and data group info
     * objects for the track.
     *
     * @param track The track to add.
     * @param toolbox The toolbox.
     */
    private void addTrack(Track track, Toolbox toolbox)
    {
        myTracks.put(track.getId(), track);
    }
}
