package io.opensphere.myplaces.specific.tracks;

import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.kml.gx.Track;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.specific.PlaceTypeController;
import io.opensphere.tracktool.registry.TrackRegistry;

/**
 * The controller specific to tracks.
 *
 */
public class TrackTypeController extends PlaceTypeController
{
    /**
     * The core toolbox.
     */
    private Toolbox myCoreToolbox;

    /**
     * The track registry.
     */
    private TrackRegistry myTrackRegistry;

    /**
     * Controls adding and removing track data elements.
     */
    private TrackDataElementsController myController;

    /**
     * The menu provider for the tracks on the map.
     */
    private TracksContextMenuProvider myGeometryMenuProvider;

    /**
     * The action manager to add context menu providers.
     */
    private ContextActionManager myActionManager;

    @Override
    public void close()
    {
        myController.close();
        if (myActionManager != null)
        {
            myActionManager.deregisterContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT,
                    GeometryContextKey.class, myGeometryMenuProvider);
        }
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return MapVisualizationType.USER_TRACK_ELEMENTS;
    }

    @Override
    public void initialize(Toolbox toolbox, MyPlacesModel model)
    {
        super.initialize(toolbox, model);
        myCoreToolbox = toolbox;
        myGeometryMenuProvider = new TracksContextMenuProvider(toolbox, model);
        myTrackRegistry = TrackRegistry.getInstance();
        myController = new TrackDataElementsController(myCoreToolbox, myTrackRegistry, model);

        if (myCoreToolbox.getUIRegistry() != null)
        {
            myActionManager = myCoreToolbox.getUIRegistry().getContextActionManager();
            myActionManager.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT,
                    GeometryContextKey.class, myGeometryMenuProvider);
        }
    }

    @Override
    public void setLocationInformation(MyPlacesDataTypeInfo dataType)
    {
        Placemark placemark = dataType.getKmlPlacemark();
        if (placemark.getGeometry() instanceof Track)
        {
            Track track = (Track)placemark.getGeometry();
            List<LatLonAlt> locations = New.list();
            for (Coordinate coord : track.getCoordinates())
            {
                LatLonAlt location = LatLonAlt.createFromDegreesMeters(coord.getLatitude(), coord.getLongitude(),
                        coord.getAltitude(), Altitude.ReferenceLevel.TERRAIN);
                locations.add(location);
            }

            dataType.addLocations(locations);
        }
    }
}
