package io.opensphere.myplaces.specific.points.editor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.mp.MapAnnotationPoint;
import io.opensphere.mantle.mp.event.impl.MapAnnotationCreatedEvent;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.editor.controller.AnnotationEditController;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesEditListener;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.specific.points.utils.PointUtils;
import io.opensphere.myplaces.util.ExtendedDataUtils;
import io.opensphere.myplaces.util.GroupUtils;
import io.opensphere.myplaces.util.OptionsAccessor;
import io.opensphere.myplaces.util.PlacemarkUtils;

/**
 * Allows the user to create a point by manually typing in the location.
 *
 */
public class ManualPointCreator
{
    /**
     * The tool box.
     */
    private final Toolbox myToolbox;

    /** The Place type controller. */
    private final MyPlacesModel myModel;

    /**
     * My type controller.
     */
    private final MyPlacesEditListener myEditListener;

    /**
     * Gets the default point.
     */
    private final OptionsAccessor myOptions;

    /** Listens for events related to creating a manual point. */
    private final transient EventListener<MapAnnotationCreatedEvent> myManualPointListener = new EventListener<MapAnnotationCreatedEvent>()
    {
        @Override
        public void notify(final MapAnnotationCreatedEvent event)
        {
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    if (event.getPointList() != null && !event.getPointList().isEmpty())
                    {
                        MyPlacesDataGroupInfo topDgi = getFolder(event);
                        if (topDgi != null)
                        {
                            for (MapAnnotationPoint point : event.getPointList())
                            {
                                Placemark placemark = PointUtils.toKml(topDgi.getKmlFolder(), point);
                                applyDefault(placemark);
                                placemark.setVisibility(Boolean.TRUE);
                                MyPlacesDataTypeInfo dataType = PlacemarkUtils.createDataType(placemark, myToolbox, this,
                                        myEditListener);
                                topDgi.addMember(dataType, this);
                            }
                        }
                    }
                }
            });
        }
    };

    /**
     * Gets the folder the points should be added to.
     *
     * @param event The event possibly containing the folder name.
     * @return The group to add the points to.
     */
    private MyPlacesDataGroupInfo getFolder(MapAnnotationCreatedEvent event)
    {
        MyPlacesDataGroupInfo topDgi = myModel.getDataGroups();

        if (StringUtils.isNotEmpty(event.getFolderName()))
        {
            DataGroupInfo dataGroup = null;
            for (DataGroupInfo group : topDgi.getChildren())
            {
                if (event.getFolderName().equals(group.getDisplayName()))
                {
                    dataGroup = group;
                    break;
                }
            }

            if (dataGroup == null)
            {
                topDgi = GroupUtils.createAndAddGroup(event.getFolderName(), topDgi, myToolbox, this);
            }
        }

        return topDgi;
    }

    /**
     * Constructs a new manual point creator.
     *
     * @param toolbox The toolbx.
     * @param controller the controller
     * @param editListener Edits the point editor dialog.
     * @param model The model.
     */
    public ManualPointCreator(Toolbox toolbox, AnnotationEditController controller, MyPlacesEditListener editListener,
            MyPlacesModel model)
    {
        myToolbox = toolbox;
        myEditListener = editListener;
        myModel = model;
        myOptions = new OptionsAccessor(toolbox);
        myToolbox.getEventManager().subscribe(MapAnnotationCreatedEvent.class, myManualPointListener);
    }

    /**
     * Applies the user defaults to the new my place.
     *
     * @param placemark The placemark to apply defaults to.
     */
    private void applyDefault(Placemark placemark)
    {
        Placemark theDefault = myOptions.getDefaultPlacemark();

        List<Data> extendedToNotOverride = New.list();
        for (Data extendedData : theDefault.getExtendedData().getData())
        {
            if (StringUtils.isNotEmpty(extendedData.getName()) && extendedData.getName().toLowerCase().contains("offset"))
            {
                extendedToNotOverride.add(extendedData);
            }
        }

        theDefault.getExtendedData().getData().removeAll(extendedToNotOverride);

        placemark.setExtendedData(theDefault.getExtendedData());
        placemark.setStyleSelector(theDefault.getStyleSelector());
    }

    /**
     * Creates the manual point.
     *
     * @param group the group
     */
    public void createManualPoint(MyPlacesDataGroupInfo group)
    {
        createManualPoint(group, null);
    }

    /**
     * Allows the user to create a point manually and adds the new point to the
     * group. If a valid position is included, the position values will be
     * displayed in the editor.
     *
     * @param group the group
     * @param pos the pos
     */
    public void createManualPoint(MyPlacesDataGroupInfo group, GeographicPosition pos)
    {
        MyPlacesDataGroupInfo currentGroup = group;

        if (group == null)
        {
            currentGroup = myModel.getDataGroups();
        }

        Placemark newPoint = myOptions.getDefaultPlacemark();

        ExtendedData extendedData = newPoint.getExtendedData();

        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_FEATURE_ON_ID, true);
        if (pos != null)
        {
            Point point = newPoint.createAndSetPoint();
            if(pos.getAlt() != null) {
                point.addToCoordinates(pos.getLatLonAlt().getLonD(), pos.getLatLonAlt().getLatD(), pos.getAlt().getMeters());
            } else {
                point.addToCoordinates(pos.getLatLonAlt().getLonD(), pos.getLatLonAlt().getLatD());
            }
        }

        myEditListener.launchEditor(newPoint, currentGroup);
    }
}
