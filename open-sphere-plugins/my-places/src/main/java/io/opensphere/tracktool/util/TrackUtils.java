package io.opensphere.tracktool.util;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.BalloonStyle;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.LineStyle;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;
import io.opensphere.core.Toolbox;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.mp.impl.DefaultMapAnnotationPoint;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.util.ExtendedDataUtils;
import io.opensphere.myplaces.util.OptionsAccessor;
import io.opensphere.myplaces.util.PlacemarkUtils;
import io.opensphere.tracktool.model.Track;
import io.opensphere.tracktool.model.TrackNode;
import io.opensphere.tracktool.model.impl.DefaultTrack;

/**
 * Utility class for tracks.
 */
public final class TrackUtils
{
    /**
     * Creates a track from the arguments, with the default settings.
     *
     * @param toolbox the toolbox
     * @param id The id of the track.
     * @param name The name which uniquely identifies the track.
     * @param nodes The nodes which make up the track.
     * @return the track
     */
    public static DefaultTrack createDefaultTrack(Toolbox toolbox, String id, String name, Collection<TrackNode> nodes)
    {
        DefaultTrack track = new DefaultTrack(id, name, nodes, true);
        OptionsAccessor options = new OptionsAccessor(toolbox);
        Placemark defaultPlacemark = options.getDefaultPlacemark();
        fromKml(track, defaultPlacemark, toolbox);
        return track;
    }

    /**
     * Creates a track from the arguments, with the settings defined from a MapAnnotationPoint.
     *
     * @param toolbox the toolbox
     * @param id The id of the track.
     * @param name The name which uniquely identifies the track.
     * @param nodes The nodes which make up the track.
     * @param point The point which contains the data.
     * @return the track
     */
    public static DefaultTrack createCsvTrack(Toolbox toolbox, String id, String name, Collection<TrackNode> nodes,
            DefaultMapAnnotationPoint point)
    {
        DefaultTrack track = new DefaultTrack(id, name, nodes, true);
        track.setDescription(point.getDescription());
        track.setColor(point.getColor());
        track.setTextColor(point.getFontColor());
        track.setIsFillBubble(point.isFilled());
        track.setFont(point.getFont());
        track.setShowDescription(point.getAnnoSettings().isDesc());
        track.setShowName(point.getAnnoSettings().isTitle());
        track.setShowFieldTitles(point.getAnnoSettings().isFieldTitle());
        track.setIsShowBubble(!point.getAnnoSettings().isAnnohide());
        track.setIsShowDistance(point.getAnnoSettings().isDistance());
        track.setIsShowHeading(point.getAnnoSettings().isHeading());
        track.setShowVelocity(point.getAnnoSettings().isVelocity());
        track.setShowDuration(point.getAnnoSettings().isDuration());

        OptionsAccessor options = new OptionsAccessor(toolbox);
        Placemark defaultPlacemark = options.getDefaultPlacemark();
        String unit = ExtendedDataUtils.getString(defaultPlacemark.getExtendedData(), Constants.UNITS_ID);

        if (StringUtils.isNotEmpty(unit))
        {
            Collection<Class<? extends Length>> units = toolbox.getUnitsRegistry().getAvailableUnits(Length.class, true);
            for (Class<? extends Length> aUnit : units)
            {
                if (aUnit.getName().equals(unit))
                {
                    track.setDistanceUnit(aUnit);
                    break;
                }
            }
        }

        track.setOffset(new Vector2i(point.getxOffset(), point.getyOffset()));
        return track;
    }

    /**
     * Fills the track with information from the placemark.
     *
     * @param track The track to set data for.
     * @param placemark The placemark containing the data.
     * @param toolbox The toolbox.
     */
    public static void fromKml(DefaultTrack track, Placemark placemark, Toolbox toolbox)
    {
        track.setDescription(placemark.getDescription());
        track.setColor(PlacemarkUtils.getPlacemarkColor(placemark));
        track.setTextColor(PlacemarkUtils.getPlacemarkTextColor(placemark));
        track.setIsFillBubble(ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_BUBBLE_FILLED_ID, false));
        track.setFont(PlacemarkUtils.getPlacemarkFont(placemark));
        track.setShowDescription(ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_DESC_ID, false));
        track.setShowName(ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_TITLE, true));
        track.setShowFieldTitles(ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_FIELD_TITLE, false));
        track.setIsShowBubble(!ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_ANNOHIDE_ID, false));
        track.setIsShowDistance(ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_DISTANCE_ID, true));
        track.setIsShowHeading(ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_HEADING_ID, true));
        track.setShowVelocity(ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_VELOCITY_ID, true));
        track.setShowDuration(ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_DURATION_ID, true));

        String unit = ExtendedDataUtils.getString(placemark.getExtendedData(), Constants.UNITS_ID);

        if (StringUtils.isNotEmpty(unit))
        {
            Collection<Class<? extends Length>> units = toolbox.getUnitsRegistry().getAvailableUnits(Length.class, true);
            for (Class<? extends Length> aUnit : units)
            {
                if (aUnit.getName().equals(unit))
                {
                    track.setDistanceUnit(aUnit);
                    break;
                }
            }
        }

        int xOffset = ExtendedDataUtils.getInt(placemark.getExtendedData(), Constants.X_OFFSET_ID, 10);
        int yOffset = ExtendedDataUtils.getInt(placemark.getExtendedData(), Constants.Y_OFFSET_ID, 10);

        track.setOffset(new Vector2i(xOffset, yOffset));
    }

    /**
     * Creates a placemark representing the track and adds it to a Folder.
     *
     * @param folder The folder to add to.
     * @param track The track to create.
     * @return The placemark representing the track.
     */
    public static Placemark toKml(Folder folder, Track track)
    {
        Placemark p = toKml(track);
        folder.addToFeature(p);
        return p;
    }

    /**
     * Create a Placemark to represent a Track.
     * @param track a Track
     * @return a Placemark
     */
    public static Placemark toKml(Track track)
    {
        Placemark placemark = new Placemark();
        placemark.setDescription(track.getDescription());
        placemark.setName(track.getName());
        placemark.setId(UUID.randomUUID().toString());
        placemark.setVisibility(Boolean.TRUE);

        Style style = PlacemarkUtils.setPlacemarkColor(placemark, track.getColor());

        BalloonStyle balloonStyle = style.createAndSetBalloonStyle();
        balloonStyle.setColor(style.getIconStyle().getColor());
        balloonStyle.setTextColor(ColorUtilities.convertToHexString(track.getTextColor(), 3, 2, 1, 0));

        LineStyle line = style.createAndSetLineStyle();
        line.setWidth(1d);
        line.setColor(style.getIconStyle().getColor());

        io.opensphere.kml.gx.Track gxTrack = new io.opensphere.kml.gx.Track();
        placemark.setGeometry(gxTrack);

        TimeSpan minimumTime = null;
        TimeSpan maximumTime = null;
        List<? extends TrackNode> nodes = track.getNodes();
        if (nodes != null)
        {
            for (TrackNode node : nodes)
            {
                LatLonAlt location = node.getLocation();
                TimeSpan timeSpan = node.getTime();
                Date coordDate = null;
                if (!timeSpan.equals(TimeSpan.TIMELESS))
                {
                    coordDate = timeSpan.getEndDate();
                    if (minimumTime == null || minimumTime.compareTo(timeSpan) > 0)
                    {
                        minimumTime = timeSpan;
                    }
                    if (maximumTime == null || maximumTime.compareTo(timeSpan) < 0)
                    {
                        maximumTime = timeSpan;
                    }
                }

                gxTrack.getCoordinates().add(new Coordinate(location.getLonD(), location.getLatD(), location.getAltM()));
                gxTrack.getWhen().add(coordDate);
            }
        }

        ExtendedData extendedData = placemark.createAndSetExtendedData();
        PlacemarkUtils.setPlacemarkFont(placemark, track.getFont());
        ExtendedDataUtils.putVisualizationType(extendedData, MapVisualizationType.USER_TRACK_ELEMENTS);
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DESC_ID, track.isShowDescription());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_TITLE, track.isShowName());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_FIELD_TITLE, track.isShowFieldTitles());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_BUBBLE_FILLED_ID, track.isFillBubble());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_ANNOHIDE_ID, !track.isShowBubble());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DISTANCE_ID, track.isShowDistance());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_HEADING_ID, track.isShowHeading());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_VELOCITY_ID, track.isShowVelocity());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DURATION_ID, track.isShowDuration());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_HEADING_DISTANCE_CAPABLE, true);
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_VELOCITY_CAPABLE, true);
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DURATION_CAPABLE, true);
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_LOCATION_CAPABLE, false);

        if (minimumTime != null && maximumTime != null)
        {
            TimeSpan trackSpan = TimeSpan.get(minimumTime.getStart(), maximumTime.getEnd());
            placemark.setTimePrimitive(KMLSpatialTemporalUtils.timeSpanToTimePrimitive(trackSpan));
        }

        return placemark;
    }

    /**
     * Can't instantiate.
     */
    private TrackUtils()
    {
    }
}
