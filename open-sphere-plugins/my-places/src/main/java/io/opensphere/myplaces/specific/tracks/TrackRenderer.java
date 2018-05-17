package io.opensphere.myplaces.specific.tracks;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.models.MyPlacesEditListener;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.specific.OpenListener;
import io.opensphere.myplaces.specific.RenderGroup;
import io.opensphere.myplaces.specific.Renderer;
import io.opensphere.myplaces.util.ExtendedDataUtils;
import io.opensphere.tracktool.TrackToolTransformer;
import io.opensphere.tracktool.model.Track;
import io.opensphere.tracktool.model.TrackNode;
import io.opensphere.tracktool.model.impl.DefaultTrack;
import io.opensphere.tracktool.model.impl.DefaultTrackNode;
import io.opensphere.tracktool.registry.TrackRegistry;
import io.opensphere.tracktool.util.TrackUtils;

/**
 * Renders tracks under my places.
 *
 */
public class TrackRenderer implements Renderer, Transformer
{
    /**
     * The model containing all my places data.
     */
    private final MyPlacesModel myModel;

    /**
     * The open listener.
     */
    private OpenListener myOpenListener;

    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * The track registry.
     */
    private final TrackRegistry myTrackRegistry;

    /** The Track transformer. */
    private final TrackToolTransformer myTrackTransformer;

    /**
     * Constructs a new track renderer.
     *
     * @param toolbox The toolbox.
     * @param model The my places model.
     * @param editListener The listener that listens for edits.
     */
    public TrackRenderer(Toolbox toolbox, MyPlacesModel model, MyPlacesEditListener editListener)
    {
        myToolbox = toolbox;
        myTrackRegistry = TrackRegistry.getInstance();
        myTrackTransformer = new TrackToolTransformer(toolbox, myTrackRegistry, model, editListener,
                new TrackCalloutDragListener(model));
        myModel = model;
    }

    @Override
    public void addSubscriber(GenericSubscriber<Geometry> subscriber)
    {
        myTrackTransformer.addSubscriber(subscriber);
    }

    @Override
    public boolean canRender()
    {
        return myTrackTransformer.isOpen();
    }

    @Override
    public void close()
    {
        myTrackTransformer.close();
    }

    @Override
    public String getDescription()
    {
        return myTrackTransformer.getDescription();
    }

    @Override
    public MapVisualizationType getRenderType()
    {
        return MapVisualizationType.USER_TRACK_ELEMENTS;
    }

    @Override
    public Transformer getTransformer()
    {
        return this;
    }

    @Override
    public void open()
    {
        myTrackTransformer.open();
        if (myOpenListener != null)
        {
            myOpenListener.opened(this);
        }
    }

    @Override
    public void publishGeometries(Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
    {
        myTrackTransformer.publishGeometries(adds, removes);
    }

    @Override
    public void removeSubscriber(GenericSubscriber<Geometry> subscriber)
    {
        myTrackTransformer.removeSubscriber(subscriber);
    }

    @Override
    public void render(RenderGroup group)
    {
        Set<String> knownPlacemarks = New.set();

        renderDisplayed(group, knownPlacemarks);
        handleAnyDeletes(knownPlacemarks);
    }

    @Override
    public void setOpenListener(OpenListener openListener)
    {
        myOpenListener = openListener;
    }

    /**
     * Converts the placemark to a track model.
     *
     * @param placemark The placemark to convert.
     * @return The track create from the placemark.
     */
    private Track fromKml(Placemark placemark)
    {
        DefaultTrack track = null;

        if (placemark.getGeometry() instanceof io.opensphere.kml.gx.Track)
        {
            io.opensphere.kml.gx.Track gxTrack = (io.opensphere.kml.gx.Track)placemark.getGeometry();
            List<TrackNode> trackNodes = New.list();

            int index = 0;
            for (Coordinate coord : gxTrack.getCoordinates())
            {
                Date when = gxTrack.getWhen().get(index);
                // Instead of always being timeless, the default timespan for
                // the node should be whatever the track has as its full span
                TimeSpan time = KMLSpatialTemporalUtils.timeSpanFromTimePrimitive(placemark.getTimePrimitive());
                if (when != null)
                {
                    time = TimeSpan.get(when);
                }

                LatLonAlt position = LatLonAlt.createFromDegreesMeters(coord.getLatitude(), coord.getLongitude(),
                        coord.getAltitude(), Altitude.ReferenceLevel.TERRAIN);
                DefaultTrackNode node = new DefaultTrackNode(position, time, null);

                int xOffset = ExtendedDataUtils.getInt(placemark.getExtendedData(), Constants.X_OFFSET_ID + "_" + index, 10);
                int yOffset = ExtendedDataUtils.getInt(placemark.getExtendedData(), Constants.Y_OFFSET_ID + "_" + index, 10);

                node.setOffset(new Vector2i(xOffset, yOffset));

                trackNodes.add(node);

                index++;
            }

            track = new DefaultTrack(placemark.getId(), placemark.getName(), trackNodes,
                    ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_ANIMATE, true));
            TrackUtils.fromKml(track, placemark, myToolbox);
        }

        return track;
    }

    /**
     * Removes any tracks from the display that have been deleted.
     *
     * @param knownPlacemarks All existing tracks.
     */
    private void handleAnyDeletes(Set<String> knownPlacemarks)
    {
        List<Track> removeTracks = New.list();
        for (Track track : myTrackRegistry.getTracks())
        {
            if (!knownPlacemarks.contains(track.getId()))
            {
                removeTracks.add(track);
            }
        }

        myTrackRegistry.remove(removeTracks);
    }

    /**
     * Renders the visible tracks.
     *
     * @param group Contains the tracks to render.
     * @param knownPlacemarks The set to add to in order to handle deletes.
     */
    private void renderDisplayed(RenderGroup group, Set<String> knownPlacemarks)
    {
        for (Placemark placemark : group.getFeaturesToRender())
        {
            knownPlacemarks.add(placemark.getId());
            Track existingTrack = myTrackRegistry.getTrack(placemark.getId());
            if (existingTrack == null)
            {
                Track track = fromKml(placemark);
                myTrackRegistry.add(track, myToolbox);
            }

            DataTypeInfo dataType = myModel.getDataGroups().getMemberById(placemark.getId(), true);
            if (dataType != null)
            {
                boolean isAnimate = ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_ANIMATE, true);
                LoadsTo loadsTo = isAnimate ? LoadsTo.TIMELINE : LoadsTo.STATIC;
                if (dataType.getBasicVisualizationInfo().getLoadsTo() != loadsTo)
                {
                    dataType.getBasicVisualizationInfo().setLoadsTo(loadsTo, this);
                }
            }
        }
    }
}
