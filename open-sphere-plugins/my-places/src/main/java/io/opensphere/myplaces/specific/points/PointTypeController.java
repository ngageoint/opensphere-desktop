package io.opensphere.myplaces.specific.points;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.TerrainUtil;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.mp.event.impl.CreateMapAnnotationPointEvent;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.myplaces.editor.PlacesEditor;
import io.opensphere.myplaces.editor.controller.AnnotationEditController;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.options.AnnotationOptionsProvider;
import io.opensphere.myplaces.specific.PlaceTypeController;
import io.opensphere.myplaces.specific.points.editor.ManualPointCreator;
import io.opensphere.myplaces.specific.points.editor.PointContextMenuProvider;
import io.opensphere.myplaces.specific.points.layercontrollers.PointEditor;

/**
 * Controls specific interactions related to points.
 *
 */
public class PointTypeController extends PlaceTypeController
{
    /**
     * Adds the point context menus to the layer panel.
     */
    private PointContextMenuProvider myContextProvider;

    /** The Context menu provider. */
    private transient PointGeometryContextMenuProvider myContextMenuProvider;

    /**
     * The my places model.
     */
    private MyPlacesModel myModel;

    /**
     * The toolbox.
     */
    private Toolbox myToolbox;

    /** The Mouse moved listener. */
    private final DiscreteEventAdapter myMouseMovedListener = new DiscreteEventAdapter("MapPoints", "Map Point Cursor Position",
            "Creates a new map point based on clicked position on the map")
    {
        @Override
        public void eventOccurred(InputEvent event)
        {
            if (event instanceof MouseEvent)
            {
                final MouseEvent mouseEvent = (MouseEvent)event;
                if (getEditController().isPickingMode() && mouseEvent.getID() == MouseEvent.MOUSE_CLICKED
                        && mouseEvent.getButton() == MouseEvent.BUTTON1)
                {
                    GeographicPosition pos = getEditController().convertPointToGeographicPosition(mouseEvent.getPoint());
                    if (pos != null)
                    {
                        // check if the altitude seems weird:
                        double altitude = TerrainUtil.getInstance().getElevationInMeters(myToolbox.getMapManager(), pos);

                        pos = new GeographicPosition(LatLonAlt.createFromDegreesMeters(pos.getLat().getMagnitude(),
                                pos.getLon().getMagnitude(), Altitude.createFromMeters(altitude, ReferenceLevel.ELLIPSOID)));

                        createAnnotationPointFromPosition(pos);
                    }
                }
            }
        }

        @Override
        public boolean isReassignable()
        {
            return false;
        }
    };

    /** Listens for notification that a new map point should be created. */
    private final transient EventListener<CreateMapAnnotationPointEvent> myCreatePointListener = new EventListener<CreateMapAnnotationPointEvent>()
    {
        @Override
        public void notify(final CreateMapAnnotationPointEvent event)
        {
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    createAnnotationPointFromPosition(event.getPosition());
                }
            });
        }
    };

    @Override
    public void close()
    {
        myContextMenuProvider.close();
        myToolbox.getUIRegistry().getContextActionManager().deregisterContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT,
                DataGroupInfo.DataGroupContextKey.class, myContextProvider);
    }

    /**
     * Initiate the creation of an annotation point.
     *
     * @param pos The geographic position.
     */
    public void createAnnotationPointFromPosition(GeographicPosition pos)
    {
        Set<DataGroupInfo> dgis = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController().getDataGroupInfoSet();
        DataGroupInfo topDgi = StreamUtilities.filterOne(dgis, dgi -> dgi.getProviderType().equals("My Places"));

        ManualPointCreator pc = new ManualPointCreator(myToolbox, getEditController(), this, myModel);
        pc.createManualPoint((MyPlacesDataGroupInfo)topDgi, pos);
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return MapVisualizationType.ANNOTATION_POINTS;
    }

    @SuppressWarnings("unused")
    @Override
    public void initialize(Toolbox toolbox, MyPlacesModel model)
    {
        super.initialize(toolbox, model);
        myModel = model;
        myToolbox = toolbox;

        myContextProvider = new PointContextMenuProvider(toolbox, getEditController(), this, model);
        myToolbox.getUIRegistry().getContextActionManager().registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT,
                DataGroupInfo.DataGroupContextKey.class, myContextProvider);
        toolbox.getUIRegistry().getOptionsRegistry().addOptionsProvider(new AnnotationOptionsProvider(toolbox));
        ControlContext context = myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        context.addListener(myMouseMovedListener, new DefaultMouseBinding(MouseEvent.MOUSE_MOVED),
                new DefaultMouseBinding(MouseEvent.MOUSE_CLICKED));

        // TODO figure out another way to initialize this so we keep the
        // reference around
        new PlaceMarkToolbarButton(getEditController());

        myContextMenuProvider = new PointGeometryContextMenuProvider(this, getEditController(), model);

        myToolbox.getEventManager().subscribe(CreateMapAnnotationPointEvent.class, myCreatePointListener);
    }

    @Override
    public void setLocationInformation(MyPlacesDataTypeInfo dataType)
    {
        Placemark placemark = dataType.getKmlPlacemark();
        if (placemark.getGeometry() instanceof Point)
        {
            Point point = (Point)placemark.getGeometry();
            List<Coordinate> coords = point.getCoordinates();
            if (!coords.isEmpty())
            {
                Coordinate position = coords.get(0);
                LatLonAlt location = LatLonAlt.createFromDegreesMeters(position.getLatitude(), position.getLongitude(),
                        position.getAltitude(), Altitude.ReferenceLevel.ELLIPSOID);
                dataType.setBoundingBox(null);
                dataType.addLocations(Collections.singleton(location));
            }
        }
    }

    @Override
    protected PlacesEditor instantiatePlaceEditor(AnnotationEditController controller)
    {
        return new PointEditor(controller);
    }
}
