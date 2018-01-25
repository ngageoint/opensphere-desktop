package io.opensphere.myplaces.specific.tracks;

import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.callout.CalloutDragListener;
import io.opensphere.core.math.Vector2i;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.models.DataCouple;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.util.ExtendedDataUtils;
import io.opensphere.myplaces.util.GroupUtils;
import io.opensphere.tracktool.model.Track;

/**
 * The callout drag listener saves the new offsets for a given track node.
 */
public class TrackCalloutDragListener implements CalloutDragListener<Track>
{
    /**
     * The places model.
     */
    private final MyPlacesModel myModel;

    /**
     * Constructs a new call drag listener.
     *
     * @param model The place model.
     */
    public TrackCalloutDragListener(MyPlacesModel model)
    {
        myModel = model;
    }

    @Override
    public void calloutDragged(Track key, Vector2i offset, int index)
    {
        Placemark placemark = null;
        DataCouple couple = GroupUtils.getDataTypeAndParent(key.getId(), myModel.getDataGroups());
        DataTypeInfo dataType = couple.getDataType();

        StringBuilder indexStringBuilder = new StringBuilder();
        if (index < key.getNodes().size() - 1)
        {
            indexStringBuilder.append('_');
            indexStringBuilder.append(index);
        }

        String indexString = indexStringBuilder.toString();

        if (dataType instanceof MyPlacesDataTypeInfo)
        {
            MyPlacesDataTypeInfo dataTypeInfo = (MyPlacesDataTypeInfo)dataType;
            placemark = dataTypeInfo.getKmlPlacemark();
            ExtendedData extendedData = placemark.getExtendedData();

            ExtendedDataUtils.putInt(extendedData, Constants.X_OFFSET_ID + indexString, offset.getX());
            ExtendedDataUtils.putInt(extendedData, Constants.Y_OFFSET_ID + indexString, offset.getY());

            myModel.notifyObservers();
        }
    }
}
