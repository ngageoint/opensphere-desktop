package io.opensphere.myplaces.specific.tracks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.mgrs.UTM;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.time.ExtentAccumulator;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolylineGeometrySupport;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.myplaces.models.DataCouple;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.util.GroupUtils;
import io.opensphere.myplaces.util.PlacemarkUtils;
import io.opensphere.tracktool.model.Track;
import io.opensphere.tracktool.model.TrackNode;
import io.opensphere.tracktool.registry.TrackRegistry;
import io.opensphere.tracktool.registry.TrackRegistryListener;

/**
 * Handles adding and removing track data elements to the map.
 *
 */
public class TrackDataElementsController implements TrackRegistryListener
{
    /**
     * The precision.
     */
    private static final double ourPrecision = 1000d;

    /**
     * Map of tracks to data elements.
     */
    private final Map<String, List<Long>> myDataElementIds = New.map();

    /**
     * The mantle toolbox.
     */
    private final MantleToolbox myMantleToolbox;

    /**
     * The my places model.
     */
    private final MyPlacesModel myModel;

    /**
     * The track registry that stores all tracks in memory.
     */
    private final TrackRegistry myRegistry;

    /**
     * Map of tracks ids to data types.
     */
    private final Map<String, DataTypeInfo> myTrackIdsToDataTypes = New.map();

    /**
     * Constucts a new track data elements controller.
     *
     * @param tb The toolbox.
     * @param registry Notifies us of track adds and removes.
     * @param model The my places mode.
     */
    public TrackDataElementsController(Toolbox tb, TrackRegistry registry, MyPlacesModel model)
    {
        myModel = model;
        myRegistry = registry;
        myRegistry.addListener(this);
        myMantleToolbox = MantleToolboxUtils.getMantleToolbox(tb);
    }

    /**
     * Close all resources.
     */
    public void close()
    {
        myRegistry.removeListener(this);
    }

    @Override
    public void tracksAdded(Collection<Track> tracks)
    {
        for (Track track : tracks)
        {
            if (!myTrackIdsToDataTypes.containsKey(track.getId()))
            {
                DataCouple couple = GroupUtils.getDataTypeAndParent(track.getId(), myModel.getDataGroups());
                if (couple != null)
                {
                    createAndPublishDataElements(couple.getDataType(), track);
                }
            }
        }
    }

    @Override
    public void tracksRemoved(Collection<Track> tracks)
    {
        for (Track track : tracks)
        {
            if (myTrackIdsToDataTypes.containsKey(track.getId()))
            {
                DataTypeInfo dataType = myTrackIdsToDataTypes.get(track.getId());
                if (!dataType.isVisible() || GroupUtils.getDataTypeAndParent(track.getId(), myModel.getDataGroups()) == null)
                {
                    unpublishDataElements(track.getId(), dataType);
                }
            }
        }
    }

    /**
     * Unpublish data elements.
     *
     * @param key The track id.
     * @param dti The data type info.
     */
    public void unpublishDataElements(String key, DataTypeInfo dti)
    {
        List<Long> ids = myDataElementIds.get(key);
        long[] featureIds = new long[ids.size()];
        int index = 0;
        for (Long id : ids)
        {
            featureIds[index] = id.longValue();
            index++;
        }

        if (featureIds.length > 0)
        {
            myMantleToolbox.getDataTypeController().removeDataElements(dti, featureIds);
        }

        myDataElementIds.remove(key);
        DataTypeInfo removedDataType = myTrackIdsToDataTypes.remove(key);
        myMantleToolbox.getDataTypeController().removeDataType(removedDataType, this);
    }

    /**
     * Adds the data element ids to the element id map.
     *
     * @param key The track id.
     * @param dataType The data type with the data elements.
     * @param ids The data element ids.
     */
    private void addDataElementIds(String key, DataTypeInfo dataType, long[] ids)
    {
        List<Long> dataElementIds = new ArrayList<>();
        for (long id : ids)
        {
            dataElementIds.add(Long.valueOf(id));
        }

        if (!myDataElementIds.containsKey(key))
        {
            myDataElementIds.put(key, new ArrayList<Long>());
        }

        myDataElementIds.get(key).addAll(dataElementIds);
        myTrackIdsToDataTypes.put(key, dataType);
    }

    /**
     * Adds the data elements.
     *
     * @param track The track to add the data elements for.
     * @param dti The data type info representing the track.
     */
    private void addDataElements(Track track, DataTypeInfo dti)
    {
        List<? extends TrackNode> nodes = track.getNodes();
        List<MapDataElement> mdeList = New.list();
        if (!nodes.isEmpty())
        {
            int legCounter = 0;
            double cumulativeDistanceKM = 0.0;
            TrackNode startNode = null;
            for (TrackNode endNode : nodes)
            {
                if (startNode != null)
                {
                    legCounter++;
                    double segmentDistanceKM = GeographicBody3D.greatCircleDistanceM(startNode.getLocation(),
                            endNode.getLocation(), WGS84EarthConstants.RADIUS_MEAN_M) / Constants.UNIT_PER_KILO;
                    cumulativeDistanceKM += segmentDistanceKM;

                    NewSegmentData newSegmentData = new NewSegmentData();
                    newSegmentData.setDti(dti);
                    newSegmentData.setSegmentCounter(legCounter);
                    newSegmentData.setStartTime(startNode.getTime());
                    newSegmentData.setEndTime(endNode.getTime());
                    newSegmentData.setStartPosition(startNode.getLocation());
                    newSegmentData.setEndPosition(endNode.getLocation());
                    newSegmentData.setSegmentLengthKM(segmentDistanceKM);
                    newSegmentData.setCumulativeDistanceKM(cumulativeDistanceKM);

                    MapDataElement segment = createSegment(newSegmentData);
                    mdeList.add(segment);
                }
                startNode = endNode;
            }
        }

        if (!mdeList.isEmpty())
        {
            long[] ids = myMantleToolbox.getDataTypeController().addMapDataElements(dti, null, null, mdeList, this);
            addDataElementIds(track.getId(), dti, ids);
        }
    }

    /**
     * Creates the and publish data elements.
     *
     * @param dti The data type for which to publish data elements.
     * @param track The track to publish data elements for.
     */
    private void createAndPublishDataElements(DataTypeInfo dti, Track track)
    {
        if (dti instanceof DefaultDataTypeInfo && dti.getMetaDataInfo() == null)
        {
            DefaultMetaDataInfo mdi = new DefaultMetaDataInfo();
            mdi.setSpecialKeyDetector(myMantleToolbox.getColumnTypeDetector());
            mdi.addKey("Start Time", TimeSpan.class, this);
            mdi.addKey("End Time", TimeSpan.class, this);
            mdi.addKey("Duration", String.class, this);
            mdi.addKey("Start Lat", String.class, this);
            mdi.addKey("Start Lat (DMS)", String.class, this);
            mdi.addKey("Start Lon", String.class, this);
            mdi.addKey("Start Lon (DMS)", String.class, this);
            mdi.addKey("Start MGRS", String.class, this);
            mdi.addKey("Start Alt", Double.class, this);
            mdi.addKey("End Lat", String.class, this);
            mdi.addKey("End Lat (DMS)", String.class, this);
            mdi.addKey("End Lon", String.class, this);
            mdi.addKey("End Lon (DMS)", String.class, this);
            mdi.addKey("End MGRS", String.class, this);
            mdi.addKey("End Alt", Double.class, this);
            mdi.addKey("Heading (deg)", String.class, this);
            mdi.addKey("Distance (km)", String.class, this);
            mdi.addKey("Speed (km/hr)", String.class, this);
            mdi.addKey("Total Distance (km)", String.class, this);

            MapVisualizationInfo mvi = dti.getMapVisualizationInfo();
            mvi.setZOrder(ZOrderRenderProperties.TOP_Z - 2000, this);

            mdi.copyKeysToOriginalKeys();
            ((DefaultDataTypeInfo)dti).setMetaDataInfo(mdi);
            myMantleToolbox.getDataTypeController().addDataType(io.opensphere.myplaces.constants.Constants.MY_PLACES_LABEL,
                    io.opensphere.myplaces.constants.Constants.MY_PLACES_LABEL, dti, this);
        }

        addDataElements(track, dti);
    }

    /**
     * Creates the segment.
     *
     * @param segmentData The segment data.
     * @return the map data element
     */
    private MapDataElement createSegment(NewSegmentData segmentData)
    {
        DataTypeInfo dti = segmentData.getDti();
        TimeSpan startTime = segmentData.getStartTime();
        TimeSpan endTime = segmentData.getEndTime();
        LatLonAlt startPosition = segmentData.getStartPosition();
        LatLonAlt endPosition = segmentData.getEndPosition();
        double segmentLengthKM = segmentData.getSegmentLengthKM();
        double cumulativeDistanceKM = segmentData.getCumulativeDistanceKM();

        DefaultMapPolylineGeometrySupport mgs = new DefaultMapPolylineGeometrySupport();
        mgs.addLocation(startPosition);
        mgs.addLocation(endPosition);
        if (dti instanceof MyPlacesDataTypeInfo)
        {
            Placemark placemark = ((MyPlacesDataTypeInfo)dti).getKmlPlacemark();
            Color color = PlacemarkUtils.getPlacemarkColor(placemark);
            mgs.setColor(color, this);
        }
        else
        {
            mgs.setColor(Color.ORANGE, this);
        }

        mgs.setLineWidth(2);
        mgs.setLineType(LineType.GREAT_CIRCLE);

        MGRSConverter converter = new MGRSConverter();

        double precision = ourPrecision;
        MDILinkedMetaDataProvider provider = new MDILinkedMetaDataProvider(dti.getMetaDataInfo());
        provider.setValue("Start Lat", Double.valueOf(Math.round(startPosition.getLatD() * precision) / precision));
        provider.setValue("Start Lat (DMS)", LatLonAlt.latToDMSString(startPosition.getLatD(), 3));
        provider.setValue("Start Lon", Double.valueOf(Math.round(startPosition.getLonD() * precision) / precision));
        provider.setValue("Start Lon (DMS)", LatLonAlt.lonToDMSString(startPosition.getLonD(), 3));

        UTM utmCoords = new UTM(new GeographicPosition(startPosition));
        provider.setValue("Start MGRS", converter.createString(utmCoords));
        if (startPosition.getAltitude() != null)
        {
            provider.setValue("Start Alt", Double.valueOf(startPosition.getAltitude().getMeters()));
        }

        provider.setValue("End Lat", Double.valueOf(Math.round(endPosition.getLatD() * precision) / precision));
        provider.setValue("End Lat (DMS)", LatLonAlt.latToDMSString(endPosition.getLatD(), 3));
        provider.setValue("End Lon", Double.valueOf(Math.round(endPosition.getLonD() * precision) / precision));
        provider.setValue("End Lon (DMS)", LatLonAlt.lonToDMSString(endPosition.getLonD(), 3));
        utmCoords = new UTM(new GeographicPosition(endPosition));
        provider.setValue("End MGRS", converter.createString(utmCoords));
        if (endPosition.getAltitude() != null)
        {
            provider.setValue("End Alt", Double.valueOf(endPosition.getAltitude().getMeters()));
        }

        double heading = GeographicBody3D.greatCircleAzimuthD(startPosition, endPosition);
        provider.setValue("Heading (deg)", Double.valueOf(Math.round(heading * precision) / precision));

        provider.setValue("Distance (km)", Double.valueOf(Math.round(segmentLengthKM * precision) / precision));

        TimeSpan overAllSpan = TimeSpan.TIMELESS;
        if (startTime.isBounded() && endTime.isBounded())
        {
            ExtentAccumulator accum = new ExtentAccumulator();
            accum.add(startTime);
            accum.add(endTime);
            overAllSpan = accum.getExtent();

            provider.setValue("Start Time", startTime);
            provider.setValue("End Time", endTime);

            provider.setValue("Duration", accum.getExtent().getDuration().toPrettyString());
        }
        if (overAllSpan.isBounded())
        {
            double hours = Hours.get(overAllSpan.getDuration()).doubleValue();
            provider.setValue("Speed (km/hr)", Double.valueOf(Math.round(segmentLengthKM / hours * precision) / precision));
            mgs.setTimeSpan(overAllSpan);
        }
        provider.setValue("Total Distance (km)", Double.valueOf(Math.round(cumulativeDistanceKM * precision) / precision));

        return new DefaultMapDataElement(0, overAllSpan, dti, provider, mgs);
    }
}
