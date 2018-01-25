package io.opensphere.myplaces.specific;

import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.myplaces.editor.MultiplePlacemarkEditor;
import io.opensphere.myplaces.editor.PlacesEditor;
import io.opensphere.myplaces.editor.controller.AnnotationEditController;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesEditListener;
import io.opensphere.myplaces.models.MyPlacesModel;

/**
 * Base class for controllers that control individual my place types, such as
 * points, rois.
 *
 */
public abstract class PlaceTypeController implements MyPlacesEditListener
{
    /**
     * The places model.
     */
    private MyPlacesModel myModel;

    /**
     * Edits multiple placemarks.
     */
    private MultiplePlacemarkEditor myMultipleEditor;

    /**
     * Edits a single point.
     */
    private PlacesEditor myPlaceEditor;

    /**
     * The places edit controller.
     */
    private AnnotationEditController myAnnotationEditController;

    /**
     * Closes the controller.
     */
    public abstract void close();

    /**
     * Gets the edit controller.
     *
     * @return The edit controller.
     */
    public AnnotationEditController getEditController()
    {
        return myAnnotationEditController;
    }

    /**
     * Gets the model.
     *
     * @return the model
     */
    public MyPlacesModel getModel()
    {
        return myModel;
    }

    /**
     * The map visualization type this controller controls.
     *
     * @return The visualization type.
     */
    public abstract MapVisualizationType getVisualizationType();

    /**
     * Initializes the controller.
     *
     * @param toolbox The toolbox.
     * @param model the model
     */
    public void initialize(Toolbox toolbox, MyPlacesModel model)
    {
        myModel = model;
        myAnnotationEditController = new AnnotationEditController(toolbox, myModel);
        myPlaceEditor = instantiatePlaceEditor(myAnnotationEditController);
        myMultipleEditor = new MultiplePlacemarkEditor(toolbox);
    }

    @Override
    public final void launchEditor(List<MyPlacesDataTypeInfo> dataTypes, DataGroupInfo dataGroup)
    {
        if (dataGroup instanceof MyPlacesDataGroupInfo)
        {
            if (dataTypes.size() == 1)
            {
                myPlaceEditor.editPlace(dataTypes.get(0), (MyPlacesDataGroupInfo)dataGroup, this);
            }
            else
            {
                myMultipleEditor.editPlaces(dataTypes, dataGroup, this);
            }
        }
    }

    /**
     * Launches the editor for a newly created placemark.
     *
     * @param placemark The newly created placemark.
     * @param dataGroup The data group the placemark will be contained in.
     */
    @Override
    public final void launchEditor(Placemark placemark, MyPlacesDataGroupInfo dataGroup)
    {
        myPlaceEditor.createPlace(placemark, dataGroup, this);
    }

    /**
     * Sets the location information in the data type info.
     *
     * @param dataType The data type to set location information for.
     */
    public abstract void setLocationInformation(MyPlacesDataTypeInfo dataType);

    /**
     * Instantiates the place editor.
     *
     * @param controller the controller used for the editor.
     * @return The place editor.
     */
    protected PlacesEditor instantiatePlaceEditor(AnnotationEditController controller)
    {
        return new PlacesEditor(controller);
    }
}
