package io.opensphere.myplaces.models;

import java.util.ArrayList;
import java.util.Collection;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.myplaces.constants.Constants;

/**
 * A default data type info used for my places nodes.
 *
 */
public class MyPlacesDataTypeInfo extends DefaultDataTypeInfo
{
    /**
     * The placemark for this data type.
     */
    private Placemark myKmlPlacemark;

    /**
     * Responsible for launching an editor.
     */
    private final MyPlacesEditListener myTypeController;

    /**
     * Constructs a new MyPlacesDataTypeInfo.
     *
     * @param tb The toolbox.
     * @param placemark The placemark this data type represents.
     * @param placeTypeController Responsible for launching an editor.
     */
    public MyPlacesDataTypeInfo(Toolbox tb, Placemark placemark, MyPlacesEditListener placeTypeController)
    {
        super(tb, Constants.MY_PLACES_LABEL, placemark.getId(), placemark.getName(), placemark.getName(), false);
        myKmlPlacemark = placemark;
        myTypeController = placeTypeController;
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean isEqual = super.equals(obj);
        if (isEqual)
        {
            if (obj instanceof MyPlacesDataTypeInfo)
            {
                isEqual = myKmlPlacemark.getId().equals(((MyPlacesDataTypeInfo)obj).getKmlPlacemark().getId());
            }
            else
            {
                isEqual = false;
            }
        }

        return isEqual;
    }

    /**
     * Gets the kml placemark this data type represents.
     *
     * @return The kml placemark.
     */
    public Placemark getKmlPlacemark()
    {
        return myKmlPlacemark;
    }

    @Override
    public boolean hasDetails()
    {
        return false;
    }

    @Override
    public int hashCode()
    {
        return myKmlPlacemark.getId().hashCode();
    }

    @Override
    public boolean isEditable()
    {
        return true;
    }

    @Override
    public boolean isFilterable()
    {
        return false;
    }

    @Override
    public void launchEditor(DataGroupInfo dataGroup, Collection<? extends DataTypeInfo> dataTypes)
    {
        if (myTypeController != null)
        {
            ArrayList<MyPlacesDataTypeInfo> places = new ArrayList<>();
            for (DataTypeInfo dataType : dataTypes)
            {
                if (dataType instanceof MyPlacesDataTypeInfo)
                {
                    places.add((MyPlacesDataTypeInfo)dataType);
                }
            }

            myTypeController.launchEditor(places, dataGroup);
        }
    }

    /**
     * Sets a new kml placemark for the data type.
     *
     * @param placemark The new placemark.
     */
    public void setKmlPlacemark(Placemark placemark)
    {
        myKmlPlacemark = placemark;
    }
}
