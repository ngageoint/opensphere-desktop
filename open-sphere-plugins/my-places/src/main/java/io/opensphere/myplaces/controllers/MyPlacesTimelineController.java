package io.opensphere.myplaces.controllers;

import java.awt.Color;
import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.timeline.StyledTimelineDatum;
import io.opensphere.core.timeline.TimelineDatum;
import io.opensphere.core.timeline.TimelineRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.util.ExtendedDataUtils;
import io.opensphere.myplaces.util.PlacemarkUtils;

/**
 * Class that updates the timeline with any timed my places.
 */
public class MyPlacesTimelineController
{
    /**
     * The order key for the my places layer.
     */
    private static final DefaultOrderParticipantKey ourKey = new DefaultOrderParticipantKey(
            DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY, DefaultOrderCategory.FEATURE_CATEGORY, "My Places");

    /**
     * Contains all of the user's my places.
     */
    private final MyPlacesModel myModel;

    /**
     * Used to put the my places on the top of the timeline.
     */
    private final OrderManagerRegistry myOrderManagerRegistry;

    /**
     * Used to add data to the timeline.
     */
    private final TimelineRegistry myTimelineRegistry;

    /**
     * Constructs a new {@link MyPlacesTimelineController}.
     *
     * @param timelineRegistry Used to add data to the timeline.
     * @param orderRegistry Used to put my places on the top of the timeline.
     * @param model Contains all of the user's my places.
     */
    public MyPlacesTimelineController(TimelineRegistry timelineRegistry, OrderManagerRegistry orderRegistry, MyPlacesModel model)
    {
        myTimelineRegistry = timelineRegistry;
        myModel = model;
        myOrderManagerRegistry = orderRegistry;
        putToTop();
        updateTimeline();
    }

    /**
     * Removes the myplaces order key.
     */
    public void close()
    {
        OrderManager manager = myOrderManagerRegistry.getOrderManager(ourKey);
        manager.deactivateParticipant(ourKey);
    }

    /**
     * Updates the timeline with my places points.
     */
    public final void updateTimeline()
    {
        Kml placesKml = myModel.getMyPlaces();

        Feature feature = placesKml.getFeature();

        if (feature instanceof Document)
        {
            Document doc = (Document)feature;
            List<Placemark> timedPoints = New.list();
            getTimedPlacemarks(doc.getFeature(), timedPoints);

            myTimelineRegistry.removeLayer(ourKey);
            if (!timedPoints.isEmpty())
            {
                Placemark mark = timedPoints.get(0);
                Color color = PlacemarkUtils.getPlacemarkColor(mark);
                myTimelineRegistry.addLayer(ourKey, "My Places", color, true);
                addPointsToTimeline(timedPoints);
            }
        }
    }

    /**
     * Adds the points to the timeline.
     *
     * @param points The points to add.
     */
    private void addPointsToTimeline(List<Placemark> points)
    {
        List<TimelineDatum> timelinePoints = New.list();
        long id = 0;
        for (Placemark mark : points)
        {
            TimelineDatum datum = new StyledTimelineDatum(id,
                    KMLSpatialTemporalUtils.timeSpanFromTimePrimitive(mark.getTimePrimitive()),
                    PlacemarkUtils.getPlacemarkColor(mark), mark.getName(), PlacemarkUtils.getPlacemarkTextColor(mark));
            timelinePoints.add(datum);
            id++;
        }
        myTimelineRegistry.addData(ourKey, timelinePoints);
    }

    /**
     * Recurses the kml tree and collects all placemarks with time information.
     *
     * @param features The features to collect time placemarks from.
     * @param timedPlacemarks The list to add placemarks with times to.
     */
    private void getTimedPlacemarks(List<Feature> features, List<Placemark> timedPlacemarks)
    {
        for (Feature feature : features)
        {
            if (feature instanceof Placemark)
            {
                Placemark placemark = (Placemark)feature;
                if (placemark.getTimePrimitive() != null && Boolean.TRUE.equals(placemark.isVisibility())
                        && ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_SHOW_IN_TIMELINE, true))
                {
                    timedPlacemarks.add(placemark);
                }
            }
            else if (feature instanceof Folder)
            {
                Folder folder = (Folder)feature;
                getTimedPlacemarks(folder.getFeature(), timedPlacemarks);
            }
        }
    }

    /**
     * Puts the my places dots and the very top z order.
     */
    private void putToTop()
    {
        OrderManager manager = myOrderManagerRegistry.getOrderManager(ourKey);
        manager.activateParticipant(ourKey);
        manager.moveToTop(ourKey);
    }
}
