package io.opensphere.myplaces.specific.factory;

import java.util.Arrays;
import java.util.Collection;

import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.myplaces.specific.PlaceTypeController;
import io.opensphere.myplaces.specific.points.PointTypeController;
import io.opensphere.myplaces.specific.regions.RegionTypeController;
import io.opensphere.myplaces.specific.tracks.TrackTypeController;

/**
 * Creates the type controllers.
 *
 */
public final class TypeControllerFactory
{
    /**
     * The instance of this class.
     */
    private static final TypeControllerFactory ourInstance = new TypeControllerFactory();

    /**
     * The controllers.
     */
    private final Collection<? extends PlaceTypeController> myControllers;

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static TypeControllerFactory getInstance()
    {
        return ourInstance;
    }

    /**
     * Private constructor helps make this class a singleton.
     */
    private TypeControllerFactory()
    {
        myControllers = Arrays.asList(new PointTypeController(), new RegionTypeController(), new TrackTypeController());
    }

    /**
     * Gets the controller for the specified map visualization type.
     *
     * @param visType The visualization type.
     * @return The controller.
     */
    public PlaceTypeController getController(MapVisualizationType visType)
    {
        PlaceTypeController theController = null;

        for (PlaceTypeController controller : myControllers)
        {
            if (controller.getVisualizationType().equals(visType))
            {
                theController = controller;
                break;
            }
        }

        return theController;
    }

    /**
     * Gets the controllers.
     *
     * @return The type controllers.
     */
    public Collection<? extends PlaceTypeController> getControllers()
    {
        return myControllers;
    }
}
