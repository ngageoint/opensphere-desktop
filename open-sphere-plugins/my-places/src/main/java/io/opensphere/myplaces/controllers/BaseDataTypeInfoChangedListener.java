package io.opensphere.myplaces.controllers;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesModel;

/**
 * Base class to handle my place changes.
 *
 * @param <T> The type of the change object.
 */
public abstract class BaseDataTypeInfoChangedListener<T>
{
    /**
     * The overall my places model.
     */
    private final MyPlacesModel myModel;

    /**
     * Constructor to specify the model.
     *
     * @param model The my places model.
     */
    protected BaseDataTypeInfoChangedListener(MyPlacesModel model)
    {
        myModel = model;
    }

    /**
     * Handles a change to a my place.
     *
     * @param changeObject Contains change information.
     * @param info The data type info representing the my place that changed.
     */
    protected void handleChange(T changeObject, DataTypeInfo info)
    {
        if (info instanceof MyPlacesDataTypeInfo)
        {
            MyPlacesDataTypeInfo dataType = (MyPlacesDataTypeInfo)info;
            Placemark placemark = dataType.getKmlPlacemark();

            boolean isVisible = placemark.isVisibility();
            if (isVisible)
            {
                placemark.setVisibility(false);
                myModel.notifyObservers();
            }

            makeChange(changeObject, placemark);

            if (isVisible)
            {
                dataType.getKmlPlacemark().setVisibility(true);
                myModel.notifyObservers();
            }
        }
    }

    /**
     * Makes the change to the my place.
     *
     * @param changeObject the change object.
     * @param placemark The placemark to make the change to.
     */
    protected abstract void makeChange(T changeObject, Placemark placemark);
}
