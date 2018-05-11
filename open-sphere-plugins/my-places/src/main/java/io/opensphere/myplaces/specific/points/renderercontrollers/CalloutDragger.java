package io.opensphere.myplaces.specific.points.renderercontrollers;

import java.util.Map;

import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.callout.Callout;
import io.opensphere.mantle.mappoint.impl.CalloutDragListener;
import io.opensphere.mantle.mappoint.impl.MapPointTransformer;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.util.ExtendedDataUtils;

/**
 * Allows the user to drag callouts to a user specified position.
 *
 */
public class CalloutDragger implements CalloutDragListener
{
    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * The my places model.
     */
    private final MyPlacesModel myModel;

    /**
     * The point transformer.
     */
    private final MapPointTransformer myMapPointTransformer;

    /**
     * Listens for mouse drag.
     */
    private final HUDListenerHelper myHudListener;

    /**
     * Contains callouts to placemarks.
     */
    private final Map<Long, Placemark> myCalloutsToPlacemarks;

    /**
     * Constructs a new callout dragger.
     *
     * @param toolbox The toolbox.
     * @param model The my places model.
     * @param transformer The transformer.
     * @param calloutsToPlacemarks Map of callouts to their respective
     *            placemarks.
     */
    public CalloutDragger(Toolbox toolbox, MyPlacesModel model, MapPointTransformer transformer,
            Map<Long, Placemark> calloutsToPlacemarks)
    {
        myToolbox = toolbox;
        myModel = model;
        myCalloutsToPlacemarks = calloutsToPlacemarks;
        myMapPointTransformer = transformer;
        myMapPointTransformer.addCalloutDragListener(this);
        myHudListener = new HUDListenerHelper(myToolbox, myMapPointTransformer);
        myHudListener.initialize();
    }

    @Override
    public void callOutDragged(Callout callout, int xOffset, int yOffset)
    {
        Placemark placemark = myCalloutsToPlacemarks.get(Long.valueOf(callout.getId()));
        if (placemark != null)
        {
            ExtendedData extendedData = placemark.getExtendedData();

            ExtendedDataUtils.putInt(extendedData, Constants.X_OFFSET_ID, xOffset);
            ExtendedDataUtils.putInt(extendedData, Constants.Y_OFFSET_ID, yOffset);

            myModel.notifyObservers();
        }
    }

    /**
     * Closes the callout dragger.
     */
    public void close()
    {
        myHudListener.close();
    }
}
