package io.opensphere.tracktool;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.callout.CalloutDragListener;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.units.UnitsProvider.UnitsChangeListener;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.callout.CalloutManager;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.tracktool.model.Track;
import io.opensphere.tracktool.registry.TrackRegistry;
import io.opensphere.tracktool.registry.TrackRegistryListener;

/**
 * Manager for dragging the label bubbles and handling context menus and units
 * changes and other events against existing tracks.
 */
public class CompletedTrackManager
{
    /** The manager for the track callouts. */
    private final CalloutManager<Track> myCalloutManager;

    /**
     * Subscriber for geometries from the callout manager.
     */
    private final GenericSubscriber<Geometry> myCalloutSubscriber = new GenericSubscriber<Geometry>()
    {
        @Override
        public void receiveObjects(Object source, Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
        {
            myTransformer.publishGeometries(adds, removes);
        }
    };

    /** The tool box used by plugins to interact with the rest of the system. */
    private final Toolbox myToolbox;

    /**
     * The registry which manages tracks. I will register tracks when they are
     * created through me and remove labels as required when tracks are removed
     * from the registry.
     */
    private final TrackRegistry myTrackRegistry;

    /** Listen for changes to the registry. */
    private final TrackRegistryListener myTrackRegistryListener = new TrackRegistryListener()
    {
        @Override
        public void tracksAdded(Collection<Track> tracks)
        {
            Collection<Geometry> adds = New.collection();
            for (Track track : tracks)
            {
                TrackArc arc = new TrackArc(track, track.getDistanceUnit(), track.getDurationUnit(), true);
                synchronized (myTracks)
                {
                    addTrackToMap(arc);
                    myTracks.put(arc.getTrack().getId(), arc);
                }
                arc.setLabelsOn(true);

                Class<? extends Length> distanceUnits = track.getDistanceUnit() == null
                        ? myToolbox.getUnitsRegistry().getPreferredUnits(Length.class) : track.getDistanceUnit();
                Class<? extends Duration> durationUnits = track.getDurationUnit() == null
                        ? myToolbox.getUnitsRegistry().getPreferredUnits(Duration.class) : track.getDurationUnit();
                myCalloutManager.addCallouts(arc.getTrack(), arc.createCallouts(distanceUnits, durationUnits), adds);
            }
            if (!adds.isEmpty())
            {
                myTransformer.publishGeometries(adds, Collections.<Geometry>emptyList());
            }
        }

        @Override
        public void tracksRemoved(Collection<Track> tracks)
        {
            Collection<Geometry> removes = New.collection();
            for (Track track : tracks)
            {
                TrackArc associatedArc = myTracks.get(track.getId());
                if (associatedArc != null)
                {
                    synchronized (myTracks)
                    {
                        removeTrackFromMap(associatedArc);
                    }
                    if (associatedArc.isLabelsOn())
                    {
                        associatedArc.setLabelsOn(false);
                    }

                    myCalloutManager.removeCallouts(track, removes);
                }
            }
            if (!removes.isEmpty())
            {
                myTransformer.publishGeometries(Collections.<Geometry>emptyList(), removes);
            }
        }
    };

    /** The arcs which I manage. */
    private final Map<String, TrackArc> myTracks = New.map();

    /** The transformer for publishing my associated geometries. */
    private final Transformer myTransformer;

    /** The listener to system duration unit changes. */
    private final UnitsChangeListener<Duration> myDurationUnitChangeListener = new UnitsChangeListener<Duration>()
    {
        @Override
        public void availableUnitsChanged(Class<Duration> superType, Collection<Class<? extends Duration>> newTypes)
        {
            /* intentionally blank */
        }

        /**
         * {@inheritDoc}
         *
         * @see io.opensphere.core.units.UnitsProvider.UnitsChangeListener#preferredUnitsChanged(java.lang.Class)
         */
        @Override
        public void preferredUnitsChanged(Class<? extends Duration> durationUnits)
        {
            Collection<Geometry> adds = New.collection();
            Collection<Geometry> removes = New.collection();

            synchronized (myTracks)
            {
                Collection<TrackArc> addArcs = New.collection(myTracks.size());
                for (TrackArc arc : myTracks.values())
                {
                    if (arc.getDurationUnits() == null)
                    {
                        if (arc.isLabelsOn())
                        {
                            myCalloutManager.removeCallouts(arc.getTrack(), removes);
                        }
                        TrackArc addArc = new TrackArc(arc.getTrack(), null, null, arc.isLabelsOn());
                        addArcs.add(addArc);
                        addArc.setLabelsOn(true);

                        Class<? extends Length> distanceUnits = myToolbox.getUnitsRegistry().getPreferredUnits(Length.class);
                        myCalloutManager.addCallouts(addArc.getTrack(), addArc.createCallouts(distanceUnits, durationUnits), adds);
                    }
                }
                for (TrackArc add : addArcs)
                {
                    addTrackToMap(add);
                }
            }

            if (!adds.isEmpty() || !removes.isEmpty())
            {
                myTransformer.publishGeometries(adds, removes);
            }
        }
    };

    /** The listener to system distance unit changes. */
    private final UnitsChangeListener<Length> myDistanceUnitChangeListener = new UnitsChangeListener<Length>()
    {
        @Override
        public void preferredUnitsChanged(final Class<? extends Length> distanceUnits)
        {
            Collection<Geometry> adds = New.collection();
            Collection<Geometry> removes = New.collection();

            synchronized (myTracks)
            {
                Collection<TrackArc> addArcs = New.collection(myTracks.size());
                for (TrackArc arc : myTracks.values())
                {
                    if (arc.getDistanceUnits() == null)
                    {
                        if (arc.isLabelsOn())
                        {
                            myCalloutManager.removeCallouts(arc.getTrack(), removes);
                        }
                        TrackArc addArc = new TrackArc(arc.getTrack(), null, null, arc.isLabelsOn());
                        addArcs.add(addArc);
                        addArc.setLabelsOn(true);

                        Class<? extends Duration> durationUnits = myToolbox.getUnitsRegistry().getPreferredUnits(Duration.class);
                        myCalloutManager.addCallouts(addArc.getTrack(), addArc.createCallouts(distanceUnits, durationUnits), adds);
                    }
                }
                for (TrackArc add : addArcs)
                {
                    addTrackToMap(add);
                }
            }

            if (!adds.isEmpty() || !removes.isEmpty())
            {
                myTransformer.publishGeometries(adds, removes);
            }
        }
    };

    /**
     * Constructor.
     *
     * @param toolbox The tool box used by plugins to interact with the rest of
     *            the system.
     * @param transformer The transformer for publishing my associated
     *            geometries.
     * @param trackRegistry the registry for tracks.
     * @param dragListener the callout drag listener.
     */
    public CompletedTrackManager(Toolbox toolbox, Transformer transformer, TrackRegistry trackRegistry,
            CalloutDragListener<Track> dragListener)
    {
        myTransformer = transformer;
        myToolbox = toolbox;

        myCalloutManager = new CalloutManager<>(
                toolbox.getControlRegistry(), "Track Tool", new ScheduledThreadPoolExecutor(1,
                        new NamedThreadFactory("Tracktool Plugin"), SuppressableRejectedExecutionHandler.getInstance()),
                dragListener);
        myCalloutManager.addSubscriber(myCalloutSubscriber);

        myTrackRegistry = trackRegistry;

        // Get all of the existing tracks for which we have missed the add
        // notification.
        Collection<Geometry> adds = New.collection();
        for (Track track : myTrackRegistry.getTracks())
        {
            TrackArc arc = new TrackArc(track, track.getDistanceUnit(), track.getDurationUnit(), false);
            addTrackToMap(arc);
            Class<? extends Length> distanceUnits = track.getDistanceUnit() == null ? myToolbox.getUnitsRegistry().getPreferredUnits(Length.class)
                    : track.getDistanceUnit();
            Class<? extends Duration> durationUnits = track.getDistanceUnit() == null ? myToolbox.getUnitsRegistry().getPreferredUnits(Duration.class)
                    : track.getDurationUnit();
            myCalloutManager.addCallouts(arc.getTrack(), arc.createCallouts(distanceUnits, durationUnits), adds);
            arc.setLabelsOn(true);
        }
        if (!adds.isEmpty())
        {
            myTransformer.publishGeometries(adds, Collections.<Geometry>emptyList());
        }

        myTrackRegistry.addListener(myTrackRegistryListener);
        myToolbox.getUnitsRegistry().getUnitsProvider(Length.class).addListener(myDistanceUnitChangeListener);
        myToolbox.getUnitsRegistry().getUnitsProvider(Duration.class).addListener(myDurationUnitChangeListener);
    }

    /** Perform any required cleanup. */
    public void close()
    {
        myCalloutManager.close();
        myTrackRegistry.removeListener(myTrackRegistryListener);
        clearAll();
    }

    /**
     * Add a track to my map of tracks and create and register the listener for
     * activation events. If another track with the same key already exists it
     * will be removed and it's listener will be de-registered.
     *
     * @param add the track to add.
     */
    private void addTrackToMap(TrackArc add)
    {
        TrackArc oldArc = myTracks.get(add.getTrack().getId());
        if (oldArc != null)
        {
            removeTrackFromMap(oldArc);
        }
        add.setLabelsOn(true);
        myTracks.put(add.getTrack().getId(), add);
    }

    /**
     * Remove all of my arcs and un-publish the associated geometries.
     */
    private void clearAll()
    {
        Collection<Geometry> removes = New.collection();
        synchronized (myTracks)
        {
            Collection<TrackArc> arcs = New.collection(myTracks.values());
            for (TrackArc arc : arcs)
            {
                removeTrackFromMap(arc);
                myCalloutManager.removeCallouts(arc.getTrack(), removes);
                if (arc.isLabelsOn())
                {
                    arc.setLabelsOn(false);
                }
            }
        }
        if (!removes.isEmpty())
        {
            myTransformer.publishGeometries(Collections.<Geometry>emptyList(), removes);
        }
    }

    /**
     * Remove a track from my map of tracks and de-register the listener for
     * activation events.
     *
     * @param remove the track to remove.
     */
    private void removeTrackFromMap(TrackArc remove)
    {
        myTracks.remove(remove.getTrack().getId());
    }
}
